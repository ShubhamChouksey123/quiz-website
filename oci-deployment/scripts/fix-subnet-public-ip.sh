#!/bin/bash

# Subnet Public IP Fix Script
# Based on: specs/subnet-public-ip-fix-plan.md
#
# This script modifies the existing subnet to allow public IP assignment
# enabling compute instances to have public internet access

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the correct directory and load environment variables
if [ ! -f "../../.env" ]; then
    log_error "Please run this script from the oci-deployment/scripts directory"
    log_error "The .env file should be in the project root"
    exit 1
fi

# Load environment variables
log_info "Loading environment variables..."
source ../../.env

# Verify required environment variables
required_vars=("COMPARTMENT_ID" "VCN_ID" "SUBNET_ID")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        log_error "Required environment variable $var is not set"
        exit 1
    fi
done

echo ""
log_info "=== Subnet Public IP Fix ==="
echo ""

# Phase 1: Pre-Modification Verification
log_info "Phase 1: Pre-Modification Verification"
echo ""

# Check current subnet state
log_info "1.1 Checking current subnet configuration..."
SUBNET_INFO=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.{name:\"display-name\",prohibit:\"prohibit-public-ip-on-vnic\",cidr:\"cidr-block\",state:\"lifecycle-state\"}" 2>/dev/null)

if [ $? -ne 0 ]; then
    log_error "Failed to retrieve subnet information"
    log_error "Check subnet ID: $SUBNET_ID"
    exit 1
fi

echo "$SUBNET_INFO"

# Check if subnet already allows public IPs
PROHIBIT_PUBLIC_IP=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.\"prohibit-public-ip-on-vnic\"" --raw-output 2>/dev/null)

if [ "$PROHIBIT_PUBLIC_IP" = "false" ]; then
    log_success "Subnet already allows public IP assignment!"
    log_info "No modification needed. You can proceed with infrastructure creation."
    exit 0
elif [ "$PROHIBIT_PUBLIC_IP" = "true" ]; then
    log_warning "Subnet currently prohibits public IP assignment"
    log_info "Proceeding with modification..."
else
    log_error "Unable to determine subnet public IP policy"
    exit 1
fi

echo ""

# Check Internet Gateway
log_info "1.2 Verifying Internet Gateway exists..."
IG_COUNT=$(oci network internet-gateway list --compartment-id "$COMPARTMENT_ID" --vcn-id "$VCN_ID" --query "length(data)" --output json 2>/dev/null || echo "0")

if [ "$IG_COUNT" -eq 0 ]; then
    log_error "No Internet Gateway found in VCN"
    log_error "Internet Gateway is required for public IP access"
    log_error "Please create an Internet Gateway first"
    exit 1
else
    log_success "$IG_COUNT Internet Gateway(s) found"
fi

# Check Route Table
log_info "1.3 Checking route table configuration..."
ROUTE_TABLE_ID=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.\"route-table-id\"" --raw-output 2>/dev/null)

if [ -n "$ROUTE_TABLE_ID" ]; then
    log_success "Route table found: $ROUTE_TABLE_ID"

    # Check for internet route
    INTERNET_ROUTES=$(oci network route-table get --rt-id "$ROUTE_TABLE_ID" --query "data.\"route-rules\"[?destination=='0.0.0.0/0']" --output json 2>/dev/null)

    if echo "$INTERNET_ROUTES" | grep -q "0.0.0.0/0"; then
        log_success "Internet route (0.0.0.0/0) found in route table"
    else
        log_warning "No internet route found in route table"
        log_warning "You may need to add a route to 0.0.0.0/0 via Internet Gateway"
    fi
else
    log_error "Could not retrieve route table information"
    exit 1
fi

# Check Security Lists
log_info "1.4 Checking security list configuration..."
SECURITY_LISTS=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.\"security-list-ids\"" --output json 2>/dev/null)
SECURITY_LIST_COUNT=$(echo "$SECURITY_LISTS" | jq 'length' 2>/dev/null || echo "0")

if [ "$SECURITY_LIST_COUNT" -gt 0 ]; then
    log_success "$SECURITY_LIST_COUNT security list(s) attached to subnet"
    log_info "Security lists will be reviewed after subnet modification"
else
    log_warning "No security lists found - this may cause connectivity issues"
fi

echo ""

# Phase 2: Subnet Modification
log_info "Phase 2: Subnet Modification"
echo ""

log_info "2.1 Updating subnet to allow public IP assignment..."
log_warning "This operation will modify your subnet configuration"

# Confirm modification
echo -n "Do you want to proceed with subnet modification? (y/N): "
read -r CONFIRM

if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    log_info "Operation cancelled by user"
    exit 0
fi

# Perform subnet update
log_info "Updating subnet configuration..."
UPDATE_RESULT=$(oci network subnet update \
    --subnet-id "$SUBNET_ID" \
    --prohibit-public-ip-on-vnic false \
    --wait-for-state AVAILABLE 2>&1)

if [ $? -eq 0 ]; then
    log_success "Subnet updated successfully!"
else
    log_error "Failed to update subnet:"
    echo "$UPDATE_RESULT"
    exit 1
fi

echo ""

# Phase 3: Post-Modification Verification
log_info "Phase 3: Post-Modification Verification"
echo ""

log_info "3.1 Verifying subnet modification..."
sleep 5  # Wait for changes to propagate

NEW_PROHIBIT_STATUS=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.\"prohibit-public-ip-on-vnic\"" --raw-output 2>/dev/null)

if [ "$NEW_PROHIBIT_STATUS" = "false" ]; then
    log_success "✅ Subnet now allows public IP assignment!"
else
    log_error "❌ Subnet modification may have failed"
    log_error "Current prohibit-public-ip-on-vnic: $NEW_PROHIBIT_STATUS"
    exit 1
fi

# Display updated subnet information
log_info "3.2 Updated subnet configuration:"
UPDATED_SUBNET_INFO=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.{name:\"display-name\",prohibit:\"prohibit-public-ip-on-vnic\",cidr:\"cidr-block\",state:\"lifecycle-state\"}" 2>/dev/null)
echo "$UPDATED_SUBNET_INFO"

echo ""

# Phase 4: Security Configuration Recommendations
log_info "Phase 4: Security Configuration Recommendations"
echo ""

log_info "4.1 Checking security list rules..."

# Get first security list for review
FIRST_SL_ID=$(echo "$SECURITY_LISTS" | jq -r '.[0]' 2>/dev/null)

if [ -n "$FIRST_SL_ID" ] && [ "$FIRST_SL_ID" != "null" ]; then
    log_info "Reviewing security list: $FIRST_SL_ID"

    # Check for SSH access
    SSH_RULES=$(oci network security-list get --security-list-id "$FIRST_SL_ID" \
        --query "data.\"ingress-security-rules\"[?\"destination-port-range\".min==\`22\`]" \
        --output json 2>/dev/null || echo "[]")

    SSH_RULE_COUNT=$(echo "$SSH_RULES" | jq 'length' 2>/dev/null || echo "0")

    if [ "$SSH_RULE_COUNT" -gt 0 ]; then
        log_success "SSH access rules found in security list"
    else
        log_warning "No SSH access rules found"
        log_warning "You may need to add SSH (port 22) ingress rules"
    fi

    # Check for HTTP access
    HTTP_RULES=$(oci network security-list get --security-list-id "$FIRST_SL_ID" \
        --query "data.\"ingress-security-rules\"[?\"destination-port-range\".min==\`8080\`]" \
        --output json 2>/dev/null || echo "[]")

    HTTP_RULE_COUNT=$(echo "$HTTP_RULES" | jq 'length' 2>/dev/null || echo "0")

    if [ "$HTTP_RULE_COUNT" -gt 0 ]; then
        log_success "Application access rules found in security list"
    else
        log_warning "No application access rules found"
        log_warning "You may need to add HTTP (port 8080) ingress rules"
    fi
fi

echo ""

# Success Summary
log_success "=== Subnet Public IP Fix Complete! ==="
echo ""
log_info "Summary of changes:"
echo "  ✅ Subnet now allows public IP assignment"
echo "  ✅ Internet Gateway verified"
echo "  ✅ Route table configuration checked"
echo "  ✅ Security lists reviewed"
echo ""

log_info "Next steps:"
echo "  1. Review security list rules if warnings were shown above"
echo "  2. Re-run prerequisites verification: ./01-verify-prerequisites.sh"
echo "  3. Proceed with infrastructure creation: ./02-create-infrastructure.sh"
echo "  4. Test compute instance creation with public IP"
echo ""

log_info "Rollback command (if needed):"
echo "  oci network subnet update --subnet-id $SUBNET_ID --prohibit-public-ip-on-vnic true"
echo ""

# Save results
echo "Subnet public IP fix completed successfully at $(date)" > ../subnet-fix-status.log
echo "Subnet ID: $SUBNET_ID" >> ../subnet-fix-status.log
echo "Status: prohibit-public-ip-on-vnic = false" >> ../subnet-fix-status.log

log_info "Status saved to ../subnet-fix-status.log"
echo ""

# Offer to run prerequisites verification
log_info "Would you like to run prerequisites verification now to confirm the fix?"
echo -n "Run ./01-verify-prerequisites.sh? (y/N): "
read -r RUN_PREREQ

if [[ "$RUN_PREREQ" =~ ^[Yy]$ ]]; then
    log_info "Running prerequisites verification..."
    echo ""
    ./01-verify-prerequisites.sh
else
    log_info "You can run ./01-verify-prerequisites.sh manually when ready"
fi