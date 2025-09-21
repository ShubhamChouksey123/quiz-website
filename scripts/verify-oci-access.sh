#!/bin/bash

# Quiz Website - OCI Access Verification Script
# This script verifies all necessary credentials and access before deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
WARNINGS=0

print_header() {
    echo -e "${BLUE}=====================================================${NC}"
    echo -e "${BLUE}        OCI Access Verification Script${NC}"
    echo -e "${BLUE}=====================================================${NC}"
    echo ""
}

print_section() {
    echo -e "${BLUE}>>> $1${NC}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
    ((PASSED++))
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
    ((FAILED++))
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
    ((WARNINGS++))
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

check_command() {
    if command -v "$1" &> /dev/null; then
        print_success "$1 is installed"
        return 0
    else
        print_error "$1 is not installed"
        return 1
    fi
}

check_oci_cli() {
    print_section "Checking OCI CLI Installation"
    
    if check_command "oci"; then
        local version=$(oci --version 2>/dev/null)
        print_info "Version: $version"
    else
        print_error "OCI CLI not found. Install with:"
        echo "  bash -c \"\$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)\""
        return 1
    fi
}

check_oci_config() {
    print_section "Checking OCI Configuration"
    
    local config_file="$HOME/.oci/config"
    if [[ -f "$config_file" ]]; then
        print_success "OCI config file exists: $config_file"
        
        # Check if default profile exists
        if grep -q "\[DEFAULT\]" "$config_file"; then
            print_success "DEFAULT profile found"
        else
            print_error "DEFAULT profile not found in config"
            return 1
        fi
        
        # Check required fields
        local required_fields=("user" "fingerprint" "tenancy" "region" "key_file")
        for field in "${required_fields[@]}"; do
            if grep -q "^$field=" "$config_file"; then
                print_success "$field is configured"
            else
                print_error "$field is missing from config"
                return 1
            fi
        done
        
        # Check if private key file exists
        local key_file=$(grep "^key_file=" "$config_file" | cut -d'=' -f2 | tr -d ' ')
        if [[ -f "$key_file" ]]; then
            print_success "Private key file exists: $key_file"
        else
            print_error "Private key file not found: $key_file"
            return 1
        fi
    else
        print_error "OCI config file not found: $config_file"
        print_info "Run: oci setup config"
        return 1
    fi
}

test_oci_authentication() {
    print_section "Testing OCI Authentication"
    
    print_info "Testing connection to OCI..."
    if oci iam region list --output table > /dev/null 2>&1; then
        print_success "Successfully authenticated with OCI"
        
        # Get current user info
        local user_ocid=$(oci iam user list --output json 2>/dev/null | jq -r '.data[0].id' 2>/dev/null || echo "unknown")
        if [[ "$user_ocid" != "unknown" && "$user_ocid" != "null" ]]; then
            print_info "Current user OCID: $user_ocid"
        fi
    else
        print_error "Failed to authenticate with OCI"
        print_info "Check your configuration with: oci setup config"
        return 1
    fi
}

check_required_tools() {
    print_section "Checking Required Tools"
    
    local tools=("kubectl" "docker" "jq")
    local all_tools_present=true
    
    for tool in "${tools[@]}"; do
        if ! check_command "$tool"; then
            all_tools_present=false
        fi
    done
    
    if [[ "$all_tools_present" == "false" ]]; then
        print_info "Install missing tools:"
        print_info "  kubectl: curl -LO \"https://dl.k8s.io/release/\$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/amd64/kubectl\""
        print_info "  docker: Install Docker Desktop"
        print_info "  jq: brew install jq"
        return 1
    fi
}

test_compute_access() {
    print_section "Testing Compute Service Access"
    
    print_info "Listing compute instances..."
    local compartment_id="${COMPARTMENT_ID:-}"
    
    if [[ -z "$compartment_id" ]]; then
        # Try to get root compartment
        compartment_id=$(oci iam compartment list --compartment-id-in-subtree true --access-level ACCESSIBLE --include-root --query "data[?\"lifecycle-state\"=='ACTIVE'] | [0].id" --raw-output 2>/dev/null)
    fi
    
    if [[ -n "$compartment_id" && "$compartment_id" != "null" ]]; then
        local instances=$(oci compute instance list --compartment-id "$compartment_id" --output json 2>/dev/null)
        if [[ $? -eq 0 ]]; then
            local instance_count=$(echo "$instances" | jq '.data | length' 2>/dev/null || echo "0")
            print_success "Successfully accessed compute service"
            print_info "Found $instance_count compute instances in compartment"
            
            # Show instance details if any exist
            if [[ "$instance_count" -gt 0 ]]; then
                echo "$instances" | jq -r '.data[] | "\(.id) | \(."display-name") | \(."lifecycle-state")"' 2>/dev/null | while read line; do
                    print_info "Instance: $line"
                done
            fi
        else
            print_error "Failed to list compute instances"
            return 1
        fi
    else
        print_error "Could not determine compartment ID"
        print_info "Set COMPARTMENT_ID environment variable or ensure you have access to compartments"
        return 1
    fi
}

test_container_engine_access() {
    print_section "Testing Container Engine (OKE) Access"
    
    local compartment_id="${COMPARTMENT_ID:-}"
    if [[ -z "$compartment_id" ]]; then
        compartment_id=$(oci iam compartment list --compartment-id-in-subtree true --access-level ACCESSIBLE --include-root --query "data[?\"lifecycle-state\"=='ACTIVE'] | [0].id" --raw-output 2>/dev/null)
    fi
    
    if [[ -n "$compartment_id" && "$compartment_id" != "null" ]]; then
        if oci ce cluster list --compartment-id "$compartment_id" --output json > /dev/null 2>&1; then
            print_success "Successfully accessed Container Engine service"
        else
            print_error "Failed to access Container Engine service"
            return 1
        fi
    else
        print_warning "Cannot test OKE access without valid compartment ID"
    fi
}

test_database_access() {
    print_section "Testing Database Service Access"
    
    local compartment_id="${COMPARTMENT_ID:-}"
    if [[ -z "$compartment_id" ]]; then
        compartment_id=$(oci iam compartment list --compartment-id-in-subtree true --access-level ACCESSIBLE --include-root --query "data[?\"lifecycle-state\"=='ACTIVE'] | [0].id" --raw-output 2>/dev/null)
    fi
    
    if [[ -n "$compartment_id" && "$compartment_id" != "null" ]]; then
        if oci psql db-system list --compartment-id "$compartment_id" --output json > /dev/null 2>&1; then
            print_success "Successfully accessed PostgreSQL service"
        else
            print_error "Failed to access PostgreSQL service"
            return 1
        fi
    else
        print_warning "Cannot test PostgreSQL access without valid compartment ID"
    fi
}

check_environment_variables() {
    print_section "Checking Environment Variables"
    
    local required_vars=("COMPARTMENT_ID" "VCN_ID" "SUBNET_ID" "LB_SUBNET_ID")
    local optional_vars=("GMAIL_USERNAME" "GMAIL_PASSWORD")
    local ocir_vars=("OCIR_REGION" "OCIR_NAMESPACE" "OCIR_USERNAME" "OCIR_AUTH_TOKEN")
    
    print_info "Required variables (will be prompted if missing):"
    for var in "${required_vars[@]}"; do
        if [[ -n "${!var}" ]]; then
            print_success "$var is set"
        else
            print_warning "$var is not set (will be prompted during deployment)"
        fi
    done
    
    echo ""
    print_info "Optional variables (will be prompted if missing):"
    for var in "${optional_vars[@]}"; do
        if [[ -n "${!var}" ]]; then
            print_success "$var is set"
        else
            print_warning "$var is not set (will be prompted during deployment)"
        fi
    done
    
    echo ""
    print_info "OCIR variables (for Oracle Container Registry):"
    for var in "${ocir_vars[@]}"; do
        if [[ -n "${!var}" ]]; then
            print_success "$var is set"
        else
            print_warning "$var is not set (OCIR testing will be skipped)"
        fi
    done
}

test_docker_access() {
    print_section "Testing Docker Access"
    
    if docker info > /dev/null 2>&1; then
        print_success "Docker daemon is running"
        print_info "Using Oracle Container Registry (OCIR) for container images"
    else
        print_error "Docker daemon is not running"
        print_info "Start Docker Desktop or Docker daemon"
        return 1
    fi
}

test_ocir_access() {
    print_section "Testing Oracle Container Registry (OCIR) Access"
    
    # Check if OCIR variables are set
    local ocir_region="${OCIR_REGION:-}"
    local ocir_namespace="${OCIR_NAMESPACE:-}"
    local ocir_username="${OCIR_USERNAME:-}"
    local ocir_auth_token="${OCIR_AUTH_TOKEN:-}"
    
    if [[ -z "$ocir_region" || -z "$ocir_namespace" || -z "$ocir_username" || -z "$ocir_auth_token" ]]; then
        print_warning "OCIR configuration incomplete"
        print_info "Missing variables: OCIR_REGION, OCIR_NAMESPACE, OCIR_USERNAME, or OCIR_AUTH_TOKEN"
        print_info "Set these in .env file or environment to test OCIR access"
        return 0
    fi
    
    local ocir_endpoint="$ocir_region.ocir.io"
    print_info "Testing OCIR endpoint: $ocir_endpoint"
    print_info "OCIR namespace: $ocir_namespace"
    print_info "OCIR username: $ocir_username"
    
    # Test OCIR authentication
    print_info "Testing OCIR authentication..."
    if echo "$ocir_auth_token" | docker login "$ocir_endpoint" --username "$ocir_username" --password-stdin > /dev/null 2>&1; then
        print_success "Successfully authenticated with OCIR"
        
        # Test repository access by attempting to pull a minimal image
        local test_repo="$ocir_endpoint/$ocir_namespace/hello-world"
        print_info "Testing repository access with: $test_repo"
        
        # Try to pull Oracle's hello-world equivalent or create a test push
        if docker pull hello-world > /dev/null 2>&1; then
            print_info "Tagging test image for OCIR..."
            docker tag hello-world "$test_repo:test" > /dev/null 2>&1
            
            print_info "Testing push to OCIR repository..."
            if docker push "$test_repo:test" > /dev/null 2>&1; then
                print_success "Successfully pushed test image to OCIR"
                print_info "Test repository: $test_repo:test"
                
                # Clean up test image
                docker rmi "$test_repo:test" > /dev/null 2>&1
                docker rmi hello-world > /dev/null 2>&1
                
                # Try to delete the test image from OCIR (optional, may fail due to permissions)
                print_info "Cleaning up test image from OCIR..."
                if oci artifacts container image delete --image-id "$test_repo:test" --force > /dev/null 2>&1; then
                    print_info "Test image cleaned up from OCIR"
                else
                    print_info "Note: Test image remains in OCIR (manual cleanup may be needed)"
                fi
            else
                print_error "Failed to push test image to OCIR"
                print_info "This could indicate:"
                print_info "  • Repository doesn't exist (will be created on first push)"
                print_info "  • Insufficient permissions"
                print_info "  • Network connectivity issues"
                
                # Clean up local images
                docker rmi "$test_repo:test" > /dev/null 2>&1
                docker rmi hello-world > /dev/null 2>&1
                return 1
            fi
        else
            print_warning "Cannot pull hello-world for OCIR test"
            print_info "OCIR authentication successful, but cannot test push without base image"
        fi
        
        # Test repository listing if possible
        print_info "Testing repository listing..."
        local repository_list=$(oci artifacts container repository list --compartment-id "${COMPARTMENT_ID:-}" --output json 2>/dev/null)
        if [[ $? -eq 0 ]]; then
            local repo_count=$(echo "$repository_list" | jq '.data | length' 2>/dev/null || echo "0")
            print_success "Successfully listed OCIR repositories"
            print_info "Found $repo_count repositories in compartment"
            
            if [[ "$repo_count" -gt 0 ]]; then
                echo "$repository_list" | jq -r '.data[] | "\(.display-name) | \(.namespace) | \(.state)"' 2>/dev/null | head -5 | while read line; do
                    print_info "Repository: $line"
                done
            fi
        else
            print_warning "Could not list OCIR repositories (may need compartment access)"
        fi
        
        # Logout from OCIR
        docker logout "$ocir_endpoint" > /dev/null 2>&1
        
    else
        print_error "Failed to authenticate with OCIR"
        print_info "Check your OCIR credentials:"
        print_info "  • OCIR_USERNAME should be: <tenancy-namespace>/<username>"
        print_info "  • OCIR_AUTH_TOKEN should be a valid auth token"
        print_info "  • OCIR_REGION should match your tenancy region"
        print_info "  • OCIR_NAMESPACE should be your tenancy namespace"
        return 1
    fi
}

get_ocir_namespace() {
    print_section "Getting OCIR Namespace"
    
    print_info "Retrieving tenancy namespace for OCIR..."
    local namespace=$(oci os ns get --query "data" --raw-output 2>/dev/null)
    
    if [[ -n "$namespace" && "$namespace" != "null" ]]; then
        print_success "Tenancy namespace: $namespace"
        
        # Check if it matches environment variable
        if [[ -n "${OCIR_NAMESPACE:-}" ]]; then
            if [[ "$namespace" == "${OCIR_NAMESPACE}" ]]; then
                print_success "OCIR_NAMESPACE matches tenancy namespace"
            else
                print_warning "OCIR_NAMESPACE ($OCIR_NAMESPACE) doesn't match tenancy namespace ($namespace)"
                print_info "Consider updating OCIR_NAMESPACE in .env file"
            fi
        else
            print_info "Set OCIR_NAMESPACE=$namespace in your .env file"
        fi
    else
        print_error "Failed to retrieve tenancy namespace"
        print_info "This is required for OCIR access"
        return 1
    fi
}

show_summary() {
    echo ""
    echo -e "${BLUE}=====================================================${NC}"
    echo -e "${BLUE}                   SUMMARY${NC}"
    echo -e "${BLUE}=====================================================${NC}"
    echo -e "${GREEN}Passed: $PASSED${NC}"
    echo -e "${RED}Failed: $FAILED${NC}"
    echo -e "${YELLOW}Warnings: $WARNINGS${NC}"
    echo ""
    
    if [[ $FAILED -eq 0 ]]; then
        print_success "All critical checks passed! Ready for deployment."
        echo ""
        print_info "Next steps:"
        print_info "1. Run: ./scripts/deploy.sh"
        print_info "2. Or build Docker image only: ./scripts/deploy.sh --docker-only"
        echo ""
    else
        print_error "Some critical checks failed. Please fix issues before deployment."
        echo ""
        print_info "Common fixes:"
        print_info "1. Configure OCI CLI: oci setup config"
        print_info "2. Install missing tools (see above)"
        print_info "3. Start Docker daemon"
        print_info "4. Set required environment variables"
        echo ""
        exit 1
    fi
}

main() {
    print_header
    
    # Load environment variables from .env file if it exists
    if [[ -f ".env" ]]; then
        print_info "Loading environment variables from .env file..."
        set -a
        source .env
        set +a
    fi
    
    # Core checks
    check_oci_cli || true
    check_oci_config || true
    test_oci_authentication || true
    check_required_tools || true
    
    # Service access checks
    test_compute_access || true
    test_container_engine_access || true
    test_database_access || true
    get_ocir_namespace || true
    
    # Environment and Docker checks
    check_environment_variables || true
    test_docker_access || true
    test_ocir_access || true
    
    show_summary
}

# Show help
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "Usage: $0 [--help]"
    echo ""
    echo "This script verifies OCI access and deployment prerequisites:"
    echo "  • OCI CLI installation and configuration"
    echo "  • Authentication with OCI services"
    echo "  • Required tools (kubectl, docker, jq)"
    echo "  • Service access (Compute, OKE, PostgreSQL)"
    echo "  • OCIR (Oracle Container Registry) access"
    echo "  • Environment variables"
    echo "  • Docker connectivity"
    echo ""
    echo "Environment variables (optional):"
    echo "  COMPARTMENT_ID    - OCI compartment OCID"
    echo "  VCN_ID           - Virtual Cloud Network OCID"
    echo "  SUBNET_ID        - Subnet OCID for OKE nodes"
    echo "  LB_SUBNET_ID     - Load Balancer subnet OCID"
    echo "  GMAIL_USERNAME   - Gmail SMTP username"
    echo "  GMAIL_PASSWORD   - Gmail app password"
    echo ""
    echo "OCIR variables (for Oracle Container Registry):"
    echo "  OCIR_REGION      - OCI region for OCIR (e.g., ap-mumbai-1)"
    echo "  OCIR_NAMESPACE   - Tenancy namespace for OCIR"
    echo "  OCIR_USERNAME    - OCIR username (<namespace>/<username>)"
    echo "  OCIR_AUTH_TOKEN  - OCIR authentication token"
    exit 0
fi

main "$@"