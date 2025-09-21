#!/bin/bash

# OCI Prerequisites Verification Script
# Based on: specs/oci-credentials-verification-plan.md
#
# This script verifies all prerequisites for OCI deployment including:
# - OCI CLI installation and configuration
# - Authentication and permissions
# - Network infrastructure
# - OCIR access
# - Required tools

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

# Check if we're in the correct directory
if [ ! -f "../.env" ]; then
    log_error "Please run this script from the oci-deployment/scripts directory"
    log_error "The .env file should be in the project root"
    exit 1
fi

# Load environment variables
log_info "Loading environment variables..."
source ../../.env

# Verify required environment variables
log_info "Verifying environment variables..."
required_vars=("COMPARTMENT_ID" "VCN_ID" "SUBNET_ID" "OCIR_REGION" "OCIR_NAMESPACE" "OCIR_USERNAME" "OCIR_AUTH_TOKEN")

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        log_error "Required environment variable $var is not set"
        exit 1
    fi
done
log_success "All required environment variables are set"

echo ""
log_info "=== OCI Prerequisites Verification ==="
echo ""

# 1. OCI CLI Installation and Configuration
log_info "1. Checking OCI CLI Installation..."
if ! command -v oci &> /dev/null; then
    log_error "OCI CLI is not installed"
    echo ""
    echo "Install OCI CLI using one of these methods:"
    echo "  macOS: brew install oci-cli"
    echo "  pip:   pip install oci-cli"
    echo "  script: bash -c \"\$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)\""
    exit 1
fi

OCI_VERSION=$(oci --version 2>/dev/null | head -n 1)
log_success "OCI CLI is installed: $OCI_VERSION"

# Check OCI configuration
log_info "Checking OCI configuration..."
if [ ! -f ~/.oci/config ]; then
    log_error "OCI configuration file not found at ~/.oci/config"
    echo ""
    echo "Run 'oci setup config' to configure OCI CLI"
    exit 1
fi

if [ ! -f ~/.oci/oci_api_key.pem ]; then
    log_warning "Default API key file not found at ~/.oci/oci_api_key.pem"
    log_info "Checking if alternative key file is configured..."
fi

log_success "OCI configuration files found"

# 2. Authentication Verification
log_info "2. Testing OCI Authentication..."
if ! oci iam region list --config-file ~/.oci/config > /dev/null 2>&1; then
    log_error "OCI authentication failed"
    echo ""
    echo "Check your OCI configuration:"
    echo "  1. Verify ~/.oci/config has correct user, tenancy, and key_file"
    echo "  2. Ensure API key file exists and has correct permissions"
    echo "  3. Verify fingerprint matches the key"
    exit 1
fi
log_success "OCI authentication successful"

# Test compartment access
log_info "Testing compartment access..."
if ! oci iam compartment get --compartment-id "$COMPARTMENT_ID" > /dev/null 2>&1; then
    log_error "Cannot access compartment: $COMPARTMENT_ID"
    echo ""
    echo "Verify:"
    echo "  1. Compartment OCID is correct"
    echo "  2. User has permissions to access the compartment"
    exit 1
fi
log_success "Compartment access verified"

# 3. Network Infrastructure Validation
log_info "3. Verifying Network Infrastructure..."

# Check VCN
log_info "Checking VCN access..."
if ! oci network vcn get --vcn-id "$VCN_ID" > /dev/null 2>&1; then
    log_error "Cannot access VCN: $VCN_ID"
    exit 1
fi
log_success "VCN access verified"

# Check Subnet
log_info "Checking subnet access..."
if ! oci network subnet get --subnet-id "$SUBNET_ID" > /dev/null 2>&1; then
    log_error "Cannot access subnet: $SUBNET_ID"
    exit 1
fi

# Check if subnet allows public IPs
SUBNET_INFO=$(oci network subnet get --subnet-id "$SUBNET_ID" --query "data.{public:\"prohibit-public-ip-on-vnic\"}" 2>/dev/null)
if echo "$SUBNET_INFO" | grep -q '"public": true'; then
    log_warning "Subnet prohibits public IP assignment - you may need to use a different subnet"
else
    log_success "Subnet allows public IP assignment"
fi

# 4. Compute Service Access Verification
log_info "4. Testing Compute Service Access..."
if ! oci compute shape list --compartment-id "$COMPARTMENT_ID" --region "$OCIR_REGION" > /dev/null 2>&1; then
    log_error "Cannot access compute service"
    echo ""
    echo "Verify user has permissions for compute service in region: $OCIR_REGION"
    exit 1
fi

# Check for free tier eligible shapes
log_info "Checking for free tier eligible compute shapes..."
FREE_SHAPES=$(oci compute shape list --compartment-id "$COMPARTMENT_ID" --region "$OCIR_REGION" \
    --query "data[?contains(shape,'Standard.E2.1.Micro') || contains(shape,'Standard.A1.Flex')].[shape]" \
    --output table 2>/dev/null | grep -E "(Standard\.E2\.1\.Micro|Standard\.A1\.Flex)" | wc -l)

if [ "$FREE_SHAPES" -eq 0 ]; then
    log_warning "No free tier eligible shapes found - verify region and permissions"
else
    log_success "Free tier eligible compute shapes available"
fi

# Check availability domains
log_info "Checking availability domains..."
AD_COUNT=$(oci iam availability-domain list --compartment-id "$COMPARTMENT_ID" \
    --query "length(data)" --output json 2>/dev/null || echo "0")
if [ "$AD_COUNT" -eq 0 ]; then
    log_error "No availability domains found"
    exit 1
fi
log_success "$AD_COUNT availability domain(s) found"

# 5. SSH Key Verification
log_info "5. Checking SSH Key..."
if [ ! -f ~/.ssh/id_rsa ]; then
    log_warning "SSH private key not found at ~/.ssh/id_rsa"
    log_info "Generating new SSH key pair..."
    ssh-keygen -t rsa -b 4096 -C "quiz-app-deployment" -f ~/.ssh/id_rsa -N ""
    log_success "SSH key pair generated"
else
    log_success "SSH key pair found"
fi

if [ ! -f ~/.ssh/id_rsa.pub ]; then
    log_error "SSH public key not found at ~/.ssh/id_rsa.pub"
    exit 1
fi

# 6. Docker Installation Check
log_info "6. Verifying Docker Installation..."
if ! command -v docker &> /dev/null; then
    log_error "Docker is not installed"
    echo ""
    echo "Install Docker:"
    echo "  macOS: Install Docker Desktop from https://docker.com"
    echo "  Linux: Follow Docker installation guide for your distribution"
    exit 1
fi

DOCKER_VERSION=$(docker --version 2>/dev/null)
log_success "Docker is installed: $DOCKER_VERSION"

# Check if Docker daemon is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker daemon is not running"
    echo ""
    echo "Start Docker:"
    echo "  macOS: Start Docker Desktop application"
    echo "  Linux: sudo systemctl start docker"
    exit 1
fi
log_success "Docker daemon is running"

# 7. OCIR Authentication Test
log_info "7. Testing OCIR Authentication..."
if ! echo "$OCIR_AUTH_TOKEN" | docker login "${OCIR_REGION}.ocir.io" -u "$OCIR_USERNAME" --password-stdin > /dev/null 2>&1; then
    log_error "OCIR authentication failed"
    echo ""
    echo "Check:"
    echo "  1. OCIR_AUTH_TOKEN is valid (regenerate if needed)"
    echo "  2. OCIR_USERNAME format: <namespace>/<username>"
    echo "  3. Auth token has not expired"
    exit 1
fi
log_success "OCIR authentication successful"

# Test OCIR repository access
log_info "Testing OCIR repository access..."
if oci artifacts container repository list --compartment-id "$COMPARTMENT_ID" > /dev/null 2>&1; then
    REPO_COUNT=$(oci artifacts container repository list --compartment-id "$COMPARTMENT_ID" --query "length(data.items)" --output json 2>/dev/null || echo "0")
    log_success "OCIR repository access verified ($REPO_COUNT repositories found)"
else
    log_warning "OCIR repository access test failed - may not have existing repositories"
fi

# 8. Required Tools Check
log_info "8. Checking Required Tools..."

# Check git
if ! command -v git &> /dev/null; then
    log_error "Git is not installed"
    echo "Install: brew install git (macOS) or use your package manager"
    exit 1
fi
log_success "Git is installed: $(git --version)"

# Check curl
if ! command -v curl &> /dev/null; then
    log_error "curl is not installed"
    echo "Install: brew install curl (macOS) or use your package manager"
    exit 1
fi
log_success "curl is installed: $(curl --version | head -n1)"

# Check jq
if ! command -v jq &> /dev/null; then
    log_warning "jq is not installed (recommended for JSON processing)"
    echo "Install: brew install jq (macOS) or use your package manager"
else
    log_success "jq is installed: $(jq --version)"
fi

# Check docker-compose
if ! command -v docker-compose &> /dev/null; then
    log_warning "docker-compose is not installed"
    echo "Install: brew install docker-compose (macOS) or use your package manager"
    echo "Or use 'docker compose' (newer versions) instead"
else
    log_success "docker-compose is installed: $(docker-compose --version)"
fi

# 9. Free Tier Limits Check
log_info "9. Checking Current Resource Usage..."

# Check existing compute instances
RUNNING_INSTANCES=$(oci compute instance list --compartment-id "$COMPARTMENT_ID" --lifecycle-state RUNNING \
    --query "length(data)" --output json 2>/dev/null || echo "0")
log_info "Currently running compute instances: $RUNNING_INSTANCES"

if [ "$RUNNING_INSTANCES" -ge 6 ]; then
    log_warning "High number of running instances - verify free tier limits"
fi

# Check block volumes
VOLUME_COUNT=$(oci bv volume list --compartment-id "$COMPARTMENT_ID" \
    --query "length(data)" --output json 2>/dev/null || echo "0")
log_info "Block volumes in use: $VOLUME_COUNT"

echo ""
log_success "=== All Prerequisites Verified Successfully! ==="
echo ""
log_info "You are ready to proceed with OCI deployment."
echo ""
log_info "Next steps:"
echo "  1. Run ./02-build-and-push.sh to build and push Docker image"
echo "  2. Run ./03-create-infrastructure.sh to create compute instance"
echo "  3. Run ./04-deploy-application.sh to deploy the application"
echo "  4. Run ./05-validate-deployment.sh to validate the deployment"
echo ""

# Save verification results
echo "Prerequisites verification completed successfully at $(date)" > ../verification-status.log
log_info "Verification status saved to ../verification-status.log"