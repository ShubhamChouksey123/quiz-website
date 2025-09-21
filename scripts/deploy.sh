#!/bin/bash

# Quiz Website - Complete OCI Deployment Script
# This script deploys containerized PostgreSQL in OKE cluster and deploys the quiz application
#
# DEPLOYMENT ARCHITECTURE:
# - Containerized PostgreSQL: Running in OKE cluster with persistent storage
# - Spring Boot Application: Deployed as containers in OKE cluster
# - Storage: OCI Block Storage for PostgreSQL data persistence
# - Networking: Internal Kubernetes service discovery for database connectivity
#
# FEATURES:
# - Automated PostgreSQL container deployment with persistent volumes
# - Database validation and health checks
# - Secure password management via Kubernetes secrets
# - Service discovery for application-database connectivity
# - Comprehensive logging and error handling
#
# PREREQUISITES:
# - OCI CLI configured with appropriate permissions
# - kubectl configured for target OKE cluster
# - Docker installed for image building
# - .env file with required environment variables

# Exit immediately if a command exits with a non-zero status.
set -e

# Function to load environment variables from .env file
load_env_file() {
    local env_file="${1:-.env}"
    
    # Get the directory where this script is located
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local project_root="$(dirname "$script_dir")"
    
    # Try multiple locations for .env file
    local env_paths=("$env_file" "$project_root/.env")
    
    for env_path in "${env_paths[@]}"; do
        if [[ -f "$env_path" ]]; then
            echo -e "\033[0;34m[INFO]\033[0m Loading environment variables from $env_path..."
            
            # Export all variables from .env file
            set -a
            source "$env_path"
            set +a
            
            echo -e "\033[0;32m[SUCCESS]\033[0m Environment variables loaded successfully"
            
            # Verify key variables are loaded
            local loaded_vars=()
            [[ -n "$COMPARTMENT_ID" ]] && loaded_vars+=("COMPARTMENT_ID")
            [[ -n "$VCN_ID" ]] && loaded_vars+=("VCN_ID")
            [[ -n "$SUBNET_ID" ]] && loaded_vars+=("SUBNET_ID")
            [[ -n "$LB_SUBNET_ID" ]] && loaded_vars+=("LB_SUBNET_ID")
            [[ -n "$DB_ADMIN_PASSWORD" ]] && loaded_vars+=("DB_ADMIN_PASSWORD")
            [[ -n "$OCIR_USERNAME" ]] && loaded_vars+=("OCIR_USERNAME")
            [[ -n "$OCIR_AUTH_TOKEN" ]] && loaded_vars+=("OCIR_AUTH_TOKEN")
            [[ -n "$GMAIL_USERNAME" ]] && loaded_vars+=("GMAIL_USERNAME")
            [[ -n "$GMAIL_PASSWORD" ]] && loaded_vars+=("GMAIL_PASSWORD")
            
            if [[ ${#loaded_vars[@]} -gt 0 ]]; then
                echo -e "\033[0;34m[INFO]\033[0m Loaded variables: ${loaded_vars[*]}"
            fi
            
            return 0
        fi
    done
    
    echo -e "\033[0;33m[WARNING]\033[0m No .env file found in current directory or project root"
    echo -e "\033[0;33m[WARNING]\033[0m Checked paths: ${env_paths[*]}"
    echo -e "\033[0;33m[WARNING]\033[0m You may need to set environment variables manually"
    return 1
}

# Load environment variables from .env file
load_env_file

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration - will be set interactively or via environment variables
COMPARTMENT_ID="${COMPARTMENT_ID:-}"
VCN_ID="${VCN_ID:-}"
SUBNET_ID="${SUBNET_ID:-}"
LB_SUBNET_ID="${LB_SUBNET_ID:-}"
DB_ADMIN_PASSWORD="${DB_ADMIN_PASSWORD:-}"
DOCKER_IMAGE="${DOCKER_IMAGE:-}"
OCIR_REGION="${OCIR_REGION:-ap-mumbai-1}"
OCIR_NAMESPACE="${OCIR_NAMESPACE:-}"
OCIR_USERNAME="${OCIR_USERNAME:-}"
OCIR_AUTH_TOKEN="${OCIR_AUTH_TOKEN:-}"
GMAIL_USERNAME="${GMAIL_USERNAME:-}"
GMAIL_PASSWORD="${GMAIL_PASSWORD:-}"
REGION="${OCI_CLI_REGION:-}" # Using OCI_CLI_REGION if available

# Application configuration
APP_NAME="quiz-app"
DB_NAME="quiz_db"
CLUSTER_NAME="quiz-app-cluster"

# Function to test PostgreSQL service availability in a region
test_postgresql_service_in_region() {
    local test_region="$1"
    local original_region="$OCI_CLI_REGION"
    
    print_status "Testing PostgreSQL service in region: $test_region"
    
    # Temporarily set the region
    export OCI_CLI_REGION="$test_region"
    
    # Test if we can list shapes (basic service availability test)
    local shapes_test=$(oci psql shape-summary list-shapes --compartment-id "$COMPARTMENT_ID" --query 'data.items[0].id' --raw-output 2>/dev/null)
    
    # Test if we can create a database system (more comprehensive test with dry run equivalent)
    local create_test_result=""
    if [[ -n "$shapes_test" && "$shapes_test" != "null" ]]; then
        # Try a minimal create command to see if we get the KeyError
        create_test_result=$(oci psql db-system create \
            --compartment-id "$COMPARTMENT_ID" \
            --display-name "Test PostgreSQL Service" \
            --db-version "14" \
            --shape "VM.Standard.E5.Flex" \
            --instance-ocpu-count 1 \
            --instance-memory-size-in-gbs 16 \
            --storage-details '{"sizeInGBs":20}' \
            --network-details "{\"subnetId\":\"$SUBNET_ID\"}" \
            --credentials "{\"username\":\"testuser\",\"passwordDetails\":{\"password\":\"TempPassword123!\"}}" \
            --query 'data.id' \
            --raw-output 2>&1 | head -1)
    fi
    
    # Restore original region
    export OCI_CLI_REGION="$original_region"
    
    # Analyze results
    if [[ -n "$shapes_test" && "$shapes_test" != "null" ]]; then
        if [[ "$create_test_result" == *"KeyError"* ]]; then
            print_warning "Region $test_region: PostgreSQL shapes available but create API has KeyError issue"
            return 1
        elif [[ "$create_test_result" == *"ServiceError"* ]] || [[ "$create_test_result" == *"InvalidParameter"* ]]; then
            print_success "Region $test_region: PostgreSQL service appears functional (expected parameter errors)"
            return 0
        elif [[ "$create_test_result" == *"ocid1"* ]]; then
            print_success "Region $test_region: PostgreSQL service fully functional"
            return 0
        else
            print_warning "Region $test_region: PostgreSQL service status unclear"
            return 1
        fi
    else
        print_warning "Region $test_region: PostgreSQL service not available or shapes not accessible"
        return 1
    fi
}

# Function to find a working PostgreSQL region
find_working_postgresql_region() {
    print_status "Searching for a working PostgreSQL region..."
    
    # List of popular regions to test (prioritizing regions close to Mumbai)
    local regions_to_test=(
        "ap-hyderabad-1"    # India region, closest to Mumbai
        "ap-singapore-1"    # SE Asia
        "ap-seoul-1"        # NE Asia  
        "us-ashburn-1"      # US East
        "eu-frankfurt-1"    # Europe
        "ap-sydney-1"       # Oceania
        "us-phoenix-1"      # US West
    )
    
    print_status "Testing regions in order of preference..."
    
    for region in "${regions_to_test[@]}"; do
        if test_postgresql_service_in_region "$region"; then
            print_success "Found working PostgreSQL region: $region"
            return 0
        fi
    done
    
    print_error "No working PostgreSQL regions found in tested set"
    print_status "You may need to:"
    print_status "  1. Try other regions manually"
    print_status "  2. Contact Oracle support about PostgreSQL service issues"
    print_status "  3. Use Autonomous Database as an alternative"
    return 1
}

# Function to switch region and update configuration
switch_to_region() {
    local new_region="$1"
    
    print_status "Switching deployment to region: $new_region"
    
    # Update environment variables
    export OCI_CLI_REGION="$new_region"
    export REGION="$new_region"
    
    # Update OCIR configuration for new region
    if [[ "$new_region" == "ap-hyderabad-1" ]]; then
        export OCIR_REGION="ap-hyderabad-1"
    elif [[ "$new_region" == "ap-singapore-1" ]]; then
        export OCIR_REGION="ap-singapore-1"
    elif [[ "$new_region" == "us-ashburn-1" ]]; then
        export OCIR_REGION="us-ashburn-1"
    elif [[ "$new_region" == "eu-frankfurt-1" ]]; then
        export OCIR_REGION="eu-frankfurt-1"
    else
        export OCIR_REGION="$new_region"
    fi
    
    # Update Docker image with new region
    DOCKER_IMAGE="${OCIR_REGION}.ocir.io/${OCIR_NAMESPACE}/quiz-app:latest"
    
    print_status "Updated configuration:"
    print_status "  Region: $REGION"
    print_status "  OCIR Region: $OCIR_REGION"
    print_status "  Docker Image: $DOCKER_IMAGE"
    
    print_warning "Note: You may need to push your Docker image to the new OCIR region"
    print_status "Run: ./scripts/deploy.sh --cloudshell-only (to build in new region)"
}

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_section() {
    echo -e "\n${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}\n"
}

# Function to check if required tools are installed
check_prerequisites() {
    print_status "Checking prerequisites..."

    # Check OCI CLI
    if ! command -v oci &>/dev/null; then
        print_error "OCI CLI is not installed. Please install it first."
        exit 1
    fi

    # Check kubectl
    if ! command -v kubectl &>/dev/null; then
        print_error "kubectl is not installed. Please install it first."
        exit 1
    fi

    # Check if OCI CLI is configured
    if ! oci iam user list &>/dev/null; then
        print_error "OCI CLI is not configured. Run 'oci setup config' first."
        exit 1
    fi

    # Check OCIR access
    print_status "Verifying OCIR access..."
    if ! oci os ns get &>/dev/null; then
        print_error "Cannot access OCI Object Storage namespace (required for OCIR)."
        print_error "Please check your OCI CLI configuration and permissions."
        exit 1
    fi

    print_success "Prerequisites check passed"
}

# Function to read configuration
read_config() {
    print_status "Checking configuration..."

    # Check which values are already set
    local missing_values=false

    if [[ -z "$COMPARTMENT_ID" ]]; then
        missing_values=true
    else
        print_success "COMPARTMENT_ID loaded from environment"
    fi

    if [[ -z "$VCN_ID" ]]; then
        missing_values=true
    else
        print_success "VCN_ID loaded from environment"
    fi

    if [[ -z "$SUBNET_ID" ]]; then
        missing_values=true
    else
        print_success "SUBNET_ID loaded from environment"
    fi

    if [[ -z "$LB_SUBNET_ID" ]]; then
        missing_values=true
    else
        print_success "LB_SUBNET_ID loaded from environment"
    fi

    if [[ -z "$OCIR_USERNAME" ]]; then
        missing_values=true
    else
        print_success "OCIR_USERNAME loaded from environment"
        # Validate OCIR username format
        if [[ "$OCIR_USERNAME" != *"/"* ]]; then
            print_error "OCIR_USERNAME must be in format: namespace/username"
            print_error "Current value: $OCIR_USERNAME"
            exit 1
        fi
    fi

    if [[ -z "$OCIR_AUTH_TOKEN" ]]; then
        missing_values=true
    else
        print_success "OCIR_AUTH_TOKEN loaded from environment"
    fi

    if [[ -z "$GMAIL_USERNAME" ]]; then
        print_warning "GMAIL_USERNAME not set (email features will be disabled)"
    else
        print_success "GMAIL_USERNAME loaded from environment"
    fi

    if [[ -z "$GMAIL_PASSWORD" ]]; then
        print_warning "GMAIL_PASSWORD not set (email features will be disabled)"
    else
        print_success "GMAIL_PASSWORD loaded from environment"
    fi

    # Only prompt for missing required values
    if [[ "$missing_values" == true ]]; then
        print_status "Please provide the missing configuration values..."

        if [[ -z "$COMPARTMENT_ID" ]]; then
            read -p "Enter your OCI Compartment OCID: " COMPARTMENT_ID
        fi

        if [[ -z "$VCN_ID" ]]; then
            read -p "Enter your VCN OCID: " VCN_ID
        fi

        if [[ -z "$SUBNET_ID" ]]; then
            read -p "Enter your Subnet OCID: " SUBNET_ID
        fi

        if [[ -z "$LB_SUBNET_ID" ]]; then
            read -p "Enter your Load Balancer Subnet OCID (can be same as above): " LB_SUBNET_ID
        fi

        if [[ -z "$OCIR_USERNAME" ]]; then
            read -p "Enter your OCIR username (format: namespace/username): " OCIR_USERNAME
        fi
        
        if [[ -z "$OCIR_AUTH_TOKEN" ]]; then
            read -s -p "Enter your OCIR auth token: " OCIR_AUTH_TOKEN
            echo
        fi
    fi

    # Always prompt for sensitive values if not set
    if [[ -z "$DB_ADMIN_PASSWORD" ]]; then
        read -s -p "Enter PostgreSQL admin password: " DB_ADMIN_PASSWORD
        echo
    fi

    # Set Docker image name if not provided (OCIR only)
    if [[ -z "$DOCKER_IMAGE" ]]; then
        # Determine OCIR namespace if not set
        if [[ -z "$OCIR_NAMESPACE" ]]; then
            print_status "Auto-detecting OCIR namespace..."
            OCIR_NAMESPACE=$(oci os ns get --raw-output 2>/dev/null)
            if [[ -z "$OCIR_NAMESPACE" ]]; then
                print_error "Failed to auto-detect OCIR namespace"
                read -p "Enter your OCIR namespace manually: " OCIR_NAMESPACE
            else
                print_success "Auto-detected OCIR namespace: $OCIR_NAMESPACE"
            fi
        fi

        # Validate that we have the required OCIR configuration
        if [[ -z "$OCIR_USERNAME" ]] || [[ -z "$OCIR_AUTH_TOKEN" ]]; then
            print_error "OCIR_USERNAME and OCIR_AUTH_TOKEN are required for OCIR deployment"
            exit 1
        fi

        # OCIR format: region.ocir.io/tenancy-namespace/repo:tag
        DOCKER_IMAGE="${OCIR_REGION}.ocir.io/${OCIR_NAMESPACE}/quiz-app:latest"
        print_success "Using OCIR image: $DOCKER_IMAGE"
    fi

    # Offer to set Gmail credentials if not provided
    if [[ -z "$GMAIL_USERNAME" ]] || [[ -z "$GMAIL_PASSWORD" ]]; then
        echo
        read -p "Do you want to configure Gmail SMTP for email features? (y/N): " setup_gmail
        if [[ $setup_gmail =~ ^[Yy]$ ]]; then
            if [[ -z "$GMAIL_USERNAME" ]]; then
                read -p "Enter your Gmail username: " GMAIL_USERNAME
            fi
            if [[ -z "$GMAIL_PASSWORD" ]]; then
                read -s -p "Enter your Gmail app password: " GMAIL_PASSWORD
                echo
            fi
        fi
    fi

    # Get home region to use for kubectl config
    if [[ -z "$REGION" ]]; then
        REGION=$(oci iam region-subscription list --query 'data[?"is-home-region"] | [0]."region-name"' --raw-output)
    fi

    print_status "Using region: $REGION"
}

# Function to choose and execute build method
choose_and_build() {
    print_section "🏗️ Docker Image Build Options"
    
    # Check Docker Desktop status
    if docker info &>/dev/null && ! docker info 2>&1 | grep -q "Sign-in enforcement"; then
        print_status "Docker Desktop is available and ready"
        print_status ""
        print_status "Choose build method:"
        print_status "1. Local build (Docker Desktop) - Fast, requires Docker Desktop sign-in"
        print_status "2. Cloud Shell build (OCI remote) - No local Docker required"
        print_status ""
        read -p "Select build method (1/2) [2]: " build_choice
        
        case "${build_choice:-2}" in
            1)
                print_status "Using local Docker Desktop build"
                build_and_push_docker
                ;;
            2)
                print_status "Using OCI Cloud Shell remote build"
                build_and_push_docker_cloudshell
                ;;
            *)
                print_error "Invalid choice. Using Cloud Shell build as default."
                build_and_push_docker_cloudshell
                ;;
        esac
    else
        print_status "Docker Desktop is not available or requires sign-in"
        print_status "Using OCI Cloud Shell for remote build (recommended)"
        print_status ""
        read -p "Proceed with Cloud Shell build? (Y/n): " confirm_cloud
        if [[ "$confirm_cloud" =~ ^[Nn]$ ]]; then
            print_error "Build cancelled. Cannot proceed without Docker image."
            exit 1
        fi
        build_and_push_docker_cloudshell
    fi
}
# Function to build Docker image using OCI Cloud Shell (remote build)
build_and_push_docker_cloudshell() {
    print_section "🏗️ Building Docker image using OCI Cloud Shell"
    
    # Create a temporary directory for cloud shell deployment
    CLOUD_SHELL_DIR="/tmp/quiz-app-build-$(date +%s)"
    mkdir -p "$CLOUD_SHELL_DIR"
    
    # Copy necessary files to cloud shell directory (only app files, no system directories)
    print_status "Preparing clean build context for Cloud Shell..."
    
    # Copy application files from app directory
    cp app/Dockerfile "$CLOUD_SHELL_DIR/"
    cp app/pom.xml "$CLOUD_SHELL_DIR/"
    cp app/mvnw "$CLOUD_SHELL_DIR/"
    cp app/mvnw.cmd "$CLOUD_SHELL_DIR/"
    cp -r app/src "$CLOUD_SHELL_DIR/"
    
    # Copy target directory if it exists (for faster builds)
    if [[ -d "app/target" ]]; then
        cp -r app/target "$CLOUD_SHELL_DIR/"
    fi
    
    # Create a comprehensive build script for cloud shell
    cat > "$CLOUD_SHELL_DIR/cloudshell-build.sh" << 'EOF'
#!/bin/bash
set -e

# Color functions for output
print_status() { echo -e "\e[34m[INFO]\e[0m $1"; }
print_success() { echo -e "\e[32m[SUCCESS]\e[0m $1"; }
print_error() { echo -e "\e[31m[ERROR]\e[0m $1"; }
print_section() { echo -e "\n\e[34m============================================\e[0m"; echo -e "\e[34m$1\e[0m"; echo -e "\e[34m============================================\e[0m\n"; }

print_section "🏗️ OCI Cloud Shell Docker Build"

# Load environment variables
if [[ -f ".env" ]]; then
    print_status "Loading environment variables..."
    source .env
else
    print_error "Environment file not found. Please ensure .env is uploaded."
    exit 1
fi

# Validate required variables
if [[ -z "$DOCKER_IMAGE" ]] || [[ -z "$OCIR_REGION" ]] || [[ -z "$OCIR_USERNAME" ]] || [[ -z "$OCIR_AUTH_TOKEN" ]]; then
    print_error "Missing required environment variables:"
    print_error "DOCKER_IMAGE: ${DOCKER_IMAGE:-MISSING}"
    print_error "OCIR_REGION: ${OCIR_REGION:-MISSING}"
    print_error "OCIR_USERNAME: ${OCIR_USERNAME:-MISSING}"
    print_error "OCIR_AUTH_TOKEN: ${OCIR_AUTH_TOKEN:-MISSING}"
    exit 1
fi

print_status "Build configuration:"
print_status "  Docker Image: $DOCKER_IMAGE"
print_status "  OCIR Region: $OCIR_REGION"
print_status "  OCIR Username: $OCIR_USERNAME"
print_status "  Build Directory: $(pwd)"

# Verify we have the required files
print_status "Verifying build context..."
required_files=("Dockerfile" "pom.xml" "src")
for file in "${required_files[@]}"; do
    if [[ ! -e "$file" ]]; then
        print_error "Required file/directory missing: $file"
        print_error "Current directory contents:"
        ls -la
        exit 1
    fi
done
print_success "All required files present"

# Check if Docker is available in Cloud Shell
if ! command -v docker &> /dev/null; then
    print_error "Docker not found in Cloud Shell"
    print_error "Cloud Shell should have Docker pre-installed"
    exit 1
fi

# Login to OCIR
print_status "Logging into OCIR..."
if ! printf '%s' "$OCIR_AUTH_TOKEN" | docker login "$OCIR_REGION.ocir.io" -u "$OCIR_USERNAME" --password-stdin; then
    print_error "Failed to login to OCIR"
    print_error "Please verify your OCIR credentials are correct"
    exit 1
fi
print_success "Successfully logged into OCIR"

# Build the Docker image
print_status "Building Docker image: $DOCKER_IMAGE"
print_status "This may take several minutes for dependency downloads..."

if ! docker build -t "$DOCKER_IMAGE" .; then
    print_error "Failed to build Docker image"
    print_error "Check the build logs above for specific errors"
    exit 1
fi
print_success "Docker image built successfully"

# Push to OCIR
print_status "Pushing image to OCIR..."
if ! docker push "$DOCKER_IMAGE"; then
    print_error "Failed to push Docker image to OCIR"
    exit 1
fi
print_success "Docker image pushed successfully to OCIR: $DOCKER_IMAGE"

# Show image information
print_status "Verifying image in OCIR..."
if docker inspect "$DOCKER_IMAGE" &>/dev/null; then
    IMAGE_SIZE=$(docker inspect "$DOCKER_IMAGE" --format='{{.Size}}' | numfmt --to=iec)
    print_success "Image size: $IMAGE_SIZE"
fi

# Cleanup
docker logout "$OCIR_REGION.ocir.io" 2>/dev/null || true
print_success "✅ Build and push completed successfully!"
print_status "Your image is now available at: $DOCKER_IMAGE"
print_status ""
print_status "Next steps:"
print_status "1. Return to your local machine"
print_status "2. Run: ./scripts/verify-ocir-image.sh"
print_status "3. Continue with deployment: ./scripts/deploy.sh"
EOF

    chmod +x "$CLOUD_SHELL_DIR/cloudshell-build.sh"
    
    # Create environment file for cloud shell
    cat > "$CLOUD_SHELL_DIR/.env" << EOF
DOCKER_IMAGE="$DOCKER_IMAGE"
OCIR_REGION="$OCIR_REGION"
OCIR_USERNAME="$OCIR_USERNAME"
OCIR_AUTH_TOKEN="$OCIR_AUTH_TOKEN"
EOF

    # Create a README for cloud shell
    cat > "$CLOUD_SHELL_DIR/README-CLOUDSHELL.md" << EOF
# OCI Cloud Shell Docker Build

## Quick Start
1. Upload all files in this directory to OCI Cloud Shell
2. Run: \`chmod +x cloudshell-build.sh && ./cloudshell-build.sh\`

## What this does
- Builds Docker image for the quiz application
- Pushes to Oracle Container Registry (OCIR)
- Uses your OCIR credentials from .env file

## Files included
- \`cloudshell-build.sh\` - Build and push script
- \`.env\` - Environment variables (OCIR credentials)
- All application source files from \`app/\` directory

## After completion
Your Docker image will be available at:
\`$DOCKER_IMAGE\`

You can then continue with the rest of the deployment from your local machine.
EOF
    
    # Create a tar file for easy upload
    TARFILE="/tmp/quiz-app-cloudshell-$(date +%s).tar.gz"
    tar -czf "$TARFILE" -C "$CLOUD_SHELL_DIR" .
    
    print_section "📦 Cloud Shell Build Package Ready"
    print_success "Build package created: $TARFILE"
    print_status ""
    print_status "🚀 Next Steps:"
    print_status "1. Open OCI Cloud Shell: https://cloud.oracle.com/cloudshell"
    print_status "2. Upload the build package: $TARFILE"
    print_status "3. Extract: tar -xzf $(basename "$TARFILE")"
    print_status "4. Run: chmod +x cloudshell-build.sh && ./cloudshell-build.sh"
    print_status ""
    print_status "💡 The build script will:"
    print_status "  ✓ Login to OCIR with your credentials"
    print_status "  ✓ Build the Docker image"
    print_status "  ✓ Push to OCIR: $DOCKER_IMAGE"
    print_status ""
    print_status "📋 Alternative: Copy individual files to Cloud Shell"
    print_status "  Files are prepared in: $CLOUD_SHELL_DIR"
    print_status ""
    
    # Offer to open Cloud Shell
    if command -v open &> /dev/null; then
        read -p "Open OCI Cloud Shell in browser? (Y/n): " open_browser
        if [[ ! "$open_browser" =~ ^[Nn]$ ]]; then
            print_status "Opening OCI Cloud Shell..."
            open "https://cloud.oracle.com/cloudshell"
        fi
    else
        print_status "Please open: https://cloud.oracle.com/cloudshell"
    fi
    
    print_status ""
    read -p "Press Enter after you have completed the build in Cloud Shell..."
    
    # Cleanup temporary files but keep the tar for reference
    rm -rf "$CLOUD_SHELL_DIR"
    
    print_success "✅ Cloud Shell build process completed!"
    print_status "Build package preserved at: $TARFILE"
    print_status "You can delete it manually when no longer needed."
}

# Function to build and push Docker image (local - requires Docker Desktop)
build_and_push_docker() {
    print_section "🏗️ Building Docker image locally"
    print_warning "This requires Docker Desktop to be running and signed in"
    print_status "For alternative without Docker Desktop sign-in, use Cloud Shell build option"
    print_status "Building Docker image..."

    # Check if Docker is running
    if ! docker info &>/dev/null; then
        print_error "Docker daemon is not running. Please start Docker Desktop or Docker service first."
        exit 1
    fi

    # Check for Docker Desktop sign-in enforcement
    if docker info 2>&1 | grep -q "Sign-in enforcement"; then
        print_error "Docker Desktop requires sign-in."
        print_status "Please open Docker Desktop and sign in with your Docker account"
        print_status "This is required to build images for Oracle Container Registry (OCIR)"
        exit 1
    fi

    # Navigate to app directory
    cd app

    # Build the Docker image
    print_status "Building Docker image: $DOCKER_IMAGE"

    if ! docker build -t "$DOCKER_IMAGE" .; then
        print_error "Failed to build Docker image."
        cd ..
        exit 1
    fi
    print_success "Docker image built successfully."

    # Skip push if requested
    if [[ "$SKIP_DOCKER" == "true" ]]; then
        print_warning "Skipping image push - using local image for deployment."
        cd ..
        return 0
    fi

    # Login to OCIR and push
    print_status "Pushing image to Oracle Container Registry..."
    
    # Extract OCIR registry host from image name
    REGISTRY_HOST=$(echo "$DOCKER_IMAGE" | cut -d'/' -f1)
    print_status "Logging into OCIR: $REGISTRY_HOST"
    
    # Validate OCIR username format before attempting login
    if [[ "$OCIR_USERNAME" != *"/"* ]]; then
        print_error "OCIR_USERNAME must be in format: namespace/username"
        print_error "Example: bmx5u3gkmcih/user@example.com"
        print_error "Current value: $OCIR_USERNAME"
        cd ..
        exit 1
    fi
    
    # Use printf for better handling of special characters in auth token
    if ! printf '%s' "$OCIR_AUTH_TOKEN" | docker login "$REGISTRY_HOST" -u "$OCIR_USERNAME" --password-stdin; then
        print_error "Failed to login to OCIR"
        print_error "Please check your OCIR credentials:"
        print_error "  Username: $OCIR_USERNAME"
        print_error "  Registry: $REGISTRY_HOST"
        print_error "Ensure Docker Desktop is running and signed in to your organization"
        cd ..
        exit 1
    fi
    print_success "Successfully logged into OCIR"

    if ! docker push "$DOCKER_IMAGE"; then
        print_error "Failed to push Docker image to OCIR"
        cd ..
        exit 1
    fi
    print_success "Docker image pushed successfully to OCIR: $DOCKER_IMAGE"

    # Return to project root
    cd ..
}

# Function to create Autonomous Database PostgreSQL (alternative to PostgreSQL service)
create_autonomous_postgresql() {
    print_status "Creating Autonomous Database PostgreSQL..."
    
    # Clean up any invalid database ID file
    if [[ -f .autonomous_db_id ]]; then
        stored_id=$(cat .autonomous_db_id 2>/dev/null)
        if [[ "$stored_id" == *"Error"* ]] || [[ "$stored_id" == *"Usage"* ]] || [[ "$stored_id" == "null" ]]; then
            print_warning "Found invalid autonomous database ID file, cleaning up..."
            rm -f .autonomous_db_id
        fi
    fi
    
    # Check for existing Autonomous Database
    existing_adb=$(oci db autonomous-database list \
        --compartment-id "$COMPARTMENT_ID" \
        --display-name "Quiz Autonomous PostgreSQL" \
        --query 'data[0].id' \
        --raw-output 2>/dev/null || echo "null")
    
    if [[ "$existing_adb" != "null" && -n "$existing_adb" ]]; then
        print_success "Autonomous Database already exists with ID: $existing_adb"
        echo "$existing_adb" >.autonomous_db_id
        return 0
    fi
    
    print_status "Creating new Autonomous Database PostgreSQL..."
    print_status "Configuration: 1 OCPU, 1TB storage, PostgreSQL engine"
    
    # Autonomous Database has a 30-character password limit
    ADB_PASSWORD="${DB_ADMIN_PASSWORD:0:30}"
    print_status "Debug: Using truncated password (${#ADB_PASSWORD} chars) for Autonomous Database"
    
    # Create Autonomous Database with PostgreSQL engine
    ADB_ID=$(oci db autonomous-database create \
        --compartment-id "$COMPARTMENT_ID" \
        --display-name "Quiz Autonomous PostgreSQL" \
        --db-name "QUIZDB" \
        --cpu-core-count 1 \
        --data-storage-size-in-tbs 1 \
        --admin-password "$ADB_PASSWORD" \
        --db-workload "DW" \
        --is-auto-scaling-enabled false \
        --wait-for-state AVAILABLE \
        --query 'data.id' \
        --raw-output 2>&1)
    
    if [[ $? -eq 0 && -n "$ADB_ID" && "$ADB_ID" != "null" ]]; then
        print_success "Autonomous Database created successfully with ID: $ADB_ID"
        echo "$ADB_ID" >.autonomous_db_id
        return 0
    else
        print_error "Failed to create Autonomous Database"
        print_error "Command output: $ADB_ID"
        return 1
    fi
}

# Function to deploy PostgreSQL as a container in OKE cluster
deploy_containerized_postgresql() {
    print_status "Preparing containerized PostgreSQL deployment..."
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is created and kubectl is configured."
        return 1
    fi
    
    # Create PostgreSQL manifests with actual password
    local postgres_manifest="k8s/postgres/postgres.yaml"
    
    if [[ ! -f "$postgres_manifest" ]]; then
        print_error "PostgreSQL manifest file not found: $postgres_manifest"
        return 1
    fi
    
    # Replace password placeholder with actual password
    print_status "Configuring PostgreSQL with secure password..."
    sed "s/PLACEHOLDER_PASSWORD/$DB_ADMIN_PASSWORD/g" "$postgres_manifest" > "/tmp/postgres-configured.yaml"
    
    # Deploy PostgreSQL to OKE cluster
    print_status "Deploying PostgreSQL to OKE cluster..."
    if kubectl apply -f "/tmp/postgres-configured.yaml"; then
        print_success "PostgreSQL deployment manifests applied successfully"
        
        # Wait for PostgreSQL to be ready
        print_status "Waiting for PostgreSQL to be ready..."
        if kubectl wait --for=condition=ready pod -l app=postgres --timeout=300s; then
            print_success "PostgreSQL is ready and running in OKE cluster"
            
            # Save connection info
            cat > .db_connection_info << EOF
DATABASE_TYPE=containerized-postgresql
DATABASE_HOST=postgres-service.default.svc.cluster.local
DATABASE_PORT=5432
DATABASE_NAME=quiz_db
DATABASE_USER=postgres
DATABASE_PASSWORD=$DB_ADMIN_PASSWORD
KUBERNETES_SERVICE=postgres-service
NAMESPACE=default
EOF
            print_success "Database connection info saved to .db_connection_info"
            return 0
        else
            print_error "Timeout waiting for PostgreSQL to be ready"
            return 1
        fi
    else
        print_error "Failed to deploy PostgreSQL to OKE cluster"
        return 1
    fi
    
    # Clean up temporary file
    rm -f "/tmp/postgres-configured.yaml"
}

# Function to check PostgreSQL status
check_containerized_postgresql() {
    print_status "Checking containerized PostgreSQL status..."
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # Check PostgreSQL deployment
    print_status "📋 PostgreSQL Deployment Status:"
    kubectl get deployment postgres || print_warning "PostgreSQL deployment not found"
    
    # Check PostgreSQL pods
    print_status "🔍 PostgreSQL Pod Status:"
    kubectl get pods -l app=postgres -o wide || print_warning "No PostgreSQL pods found"
    
    # Check PostgreSQL service
    print_status "🌐 PostgreSQL Service Status:"
    kubectl get svc postgres-service || print_warning "PostgreSQL service not found"
    
    # Check persistent volume claim
    print_status "💾 PostgreSQL Storage Status:"
    kubectl get pvc postgres-pvc || print_warning "PostgreSQL PVC not found"
    
    # Test database connectivity
    print_status "🔌 Testing Database Connectivity:"
    if kubectl exec deployment/postgres -- pg_isready -U postgres >/dev/null 2>&1; then
        print_success "✅ PostgreSQL is accepting connections"
        
        # Get database size and connection count
        local db_size=$(kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -t -c "SELECT pg_size_pretty(pg_database_size('quiz_db'));" 2>/dev/null | xargs)
        local connection_count=$(kubectl exec deployment/postgres -- psql -U postgres -t -c "SELECT count(*) FROM pg_stat_activity;" 2>/dev/null | xargs)
        
        echo "  📊 Database Size: ${db_size:-Unknown}"
        echo "  👥 Active Connections: ${connection_count:-Unknown}"
    else
        print_error "❌ PostgreSQL is not accepting connections"
    fi
    
    # Show resource usage
    print_status "📈 Resource Usage:"
    kubectl top pod -l app=postgres 2>/dev/null || print_warning "Resource metrics not available"
    
    return 0
}

# Function to backup PostgreSQL database
backup_containerized_postgresql() {
    print_status "Creating backup of containerized PostgreSQL database..."
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # Check if PostgreSQL is running
    if ! kubectl get pod -l app=postgres -o jsonpath='{.items[0].status.phase}' | grep -q "Running"; then
        print_error "PostgreSQL pod is not running. Cannot create backup."
        return 1
    fi
    
    # Create backup filename with timestamp
    local backup_file="postgresql-backup-$(date +%Y%m%d-%H%M%S).sql"
    
    print_status "Creating backup: $backup_file"
    
    # Create database backup
    if kubectl exec deployment/postgres -- pg_dump -U postgres quiz_db > "$backup_file"; then
        print_success "✅ Database backup created successfully: $backup_file"
        
        # Show backup size
        local backup_size=$(ls -lh "$backup_file" | awk '{print $5}')
        echo "  📁 Backup Size: $backup_size"
        
        return 0
    else
        print_error "❌ Failed to create database backup"
        return 1
    fi
}

# Function to restore PostgreSQL database
restore_containerized_postgresql() {
    local backup_file="$1"
    
    if [[ -z "$backup_file" ]]; then
        print_error "Please provide backup file path"
        echo "Usage: $0 --restore-postgresql <backup_file.sql>"
        return 1
    fi
    
    if [[ ! -f "$backup_file" ]]; then
        print_error "Backup file not found: $backup_file"
        return 1
    fi
    
    print_status "Restoring PostgreSQL database from: $backup_file"
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # Check if PostgreSQL is running
    if ! kubectl get pod -l app=postgres -o jsonpath='{.items[0].status.phase}' | grep -q "Running"; then
        print_error "PostgreSQL pod is not running. Cannot restore database."
        return 1
    fi
    
    # Confirm restoration
    echo -e "\033[1;31m⚠️  WARNING: This will replace all data in the quiz_db database!\033[0m"
    read -p "Are you sure you want to proceed? (yes/no): " confirm
    
    if [[ "$confirm" != "yes" ]]; then
        print_warning "Database restoration cancelled"
        return 1
    fi
    
    # Restore database
    print_status "Restoring database..."
    if kubectl exec -i deployment/postgres -- psql -U postgres quiz_db < "$backup_file"; then
        print_success "✅ Database restored successfully from: $backup_file"
        return 0
    else
        print_error "❌ Failed to restore database"
        return 1
    fi
}

# Function to scale PostgreSQL deployment
scale_containerized_postgresql() {
    local replicas="${1:-1}"
    
    print_status "Scaling PostgreSQL deployment to $replicas replicas..."
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # Validate replicas number
    if ! [[ "$replicas" =~ ^[0-9]+$ ]] || [[ "$replicas" -lt 0 ]] || [[ "$replicas" -gt 3 ]]; then
        print_error "Invalid replica count. Please provide a number between 0 and 3."
        return 1
    fi
    
    # Scale deployment
    if kubectl scale deployment postgres --replicas="$replicas"; then
        print_success "✅ PostgreSQL deployment scaled to $replicas replicas"
        
        # Wait for scaling to complete
        print_status "Waiting for scaling to complete..."
        kubectl wait --for=condition=available --timeout=300s deployment/postgres
        
        # Show current status
        kubectl get pods -l app=postgres
        
        return 0
    else
        print_error "❌ Failed to scale PostgreSQL deployment"
        return 1
    fi
}

# Function to view PostgreSQL logs
view_postgresql_logs() {
    print_status "Viewing PostgreSQL logs..."
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # Show recent logs
    kubectl logs -l app=postgres --tail=100 --follow
}

# Function to open PostgreSQL shell
open_postgresql_shell() {
    print_status "Opening PostgreSQL shell..."
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # Check if PostgreSQL is running
    if ! kubectl get pod -l app=postgres -o jsonpath='{.items[0].status.phase}' | grep -q "Running"; then
        print_error "PostgreSQL pod is not running."
        return 1
    fi
    
    print_status "Connecting to PostgreSQL database..."
    print_status "💡 You are now connected to the quiz_db database"
    print_status "💡 Type \\q to exit the PostgreSQL shell"
    
    # Open interactive psql session
    kubectl exec -it deployment/postgres -- psql -U postgres -d quiz_db
}

# Function to show detailed PostgreSQL status
show_postgresql_status() {
    print_status "📊 Detailed PostgreSQL Status Report"
    echo "==========================================="
    
    # Check if kubectl is configured
    if ! kubectl cluster-info >/dev/null 2>&1; then
        print_error "kubectl is not configured. Please ensure OKE cluster is accessible."
        return 1
    fi
    
    # 1. Deployment Information
    echo
    print_status "🚀 Deployment Information:"
    kubectl describe deployment postgres | grep -E "(Name:|Namespace:|CreationTimestamp:|Replicas:|StrategyType:|Conditions:)" || true
    
    # 2. Pod Details
    echo
    print_status "🔍 Pod Details:"
    kubectl get pods -l app=postgres -o wide
    
    # 3. Service Information
    echo
    print_status "🌐 Service Information:"
    kubectl describe svc postgres-service | grep -E "(Name:|Namespace:|Type:|IP:|Port:|Endpoints:)" || true
    
    # 4. Storage Information
    echo
    print_status "💾 Storage Information:"
    kubectl describe pvc postgres-pvc | grep -E "(Name:|Namespace:|StorageClass:|Status:|Volume:|Capacity:)" || true
    
    # 5. Database Statistics (if accessible)
    echo
    print_status "📈 Database Statistics:"
    if kubectl exec deployment/postgres -- pg_isready -U postgres >/dev/null 2>&1; then
        echo "  🟢 Status: PostgreSQL is accepting connections"
        
        # Database size
        local db_size=$(kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -t -c "SELECT pg_size_pretty(pg_database_size('quiz_db'));" 2>/dev/null | xargs)
        echo "  📊 Database Size: ${db_size:-Unknown}"
        
        # Connection count
        local connection_count=$(kubectl exec deployment/postgres -- psql -U postgres -t -c "SELECT count(*) FROM pg_stat_activity;" 2>/dev/null | xargs)
        echo "  👥 Active Connections: ${connection_count:-Unknown}"
        
        # Table count
        local table_count=$(kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | xargs)
        echo "  🗃️  Tables Count: ${table_count:-Unknown}"
        
        # Last backup info (if available)
        if [[ -f ".db_connection_info" ]]; then
            echo "  🔧 Connection Config: Available in .db_connection_info"
        fi
    else
        echo "  🔴 Status: PostgreSQL is not accepting connections"
    fi
    
    # 6. Resource Usage
    echo
    print_status "📊 Resource Usage:"
    kubectl top pod -l app=postgres 2>/dev/null || echo "  ⚠️  Resource metrics not available (metrics-server might not be installed)"
    
    # 7. Recent Events
    echo
    print_status "📅 Recent Events:"
    kubectl get events --field-selector involvedObject.name=postgres-deployment --sort-by='.lastTimestamp' | tail -5 || true
    
    echo
    print_success "✅ PostgreSQL status report completed"
}
}

# Function to create PostgreSQL database with fallback to Autonomous Database
create_postgresql() {
    print_status "Setting up PostgreSQL database..."
    
    # Check if we're in a single-region tenancy
    subscribed_regions=$(oci iam region-subscription list --query 'data[*]."region-name"' --raw-output 2>/dev/null | wc -l)
    
    if [[ "$subscribed_regions" -eq 1 ]]; then
        print_status "Single-region tenancy detected. Testing PostgreSQL service in current region..."
        
        # Test PostgreSQL service in current region
        if test_postgresql_service_in_region "$REGION" >/dev/null 2>&1; then
            print_status "PostgreSQL service is available. Proceeding with PostgreSQL creation..."
            create_postgresql_in_current_region
            return $?
        else
            print_warning "PostgreSQL service has issues in current region (KeyError: 'systemType')"
            print_status "Falling back to Autonomous Database PostgreSQL..."
            
            # Ask user for preference
            echo
            read -p "Would you like to use Autonomous Database PostgreSQL instead? (Y/n): " use_autonomous
            if [[ ! "$use_autonomous" =~ ^[Nn]$ ]]; then
                create_autonomous_postgresql
                return $?
            else
                print_error "PostgreSQL service is not working. Deployment cannot continue."
                print_status "Please try again later or contact Oracle support."
                return 1
            fi
        fi
    else
        # Multi-region tenancy - try the original logic
        if create_postgresql_in_current_region; then
            return 0
        fi
        
        print_warning "PostgreSQL creation failed in current region: $REGION"
        print_status "Searching for alternative regions with working PostgreSQL service..."
        
        if find_working_postgresql_region; then
            # Try other regions as before
            local working_regions=(
                "ap-hyderabad-1"
                "ap-singapore-1" 
                "ap-seoul-1"
                "us-ashburn-1"
                "eu-frankfurt-1"
                "ap-sydney-1"
                "us-phoenix-1"
            )
            
            for region in "${working_regions[@]}"; do
                if test_postgresql_service_in_region "$region" >/dev/null 2>&1; then
                    print_status "Switching to working region: $region"
                    switch_to_region "$region"
                    
                    if create_postgresql_in_current_region; then
                        print_success "PostgreSQL database created successfully in region: $region"
                        return 0
                    fi
                fi
            done
        fi
        
        print_error "Failed to create PostgreSQL database in any tested region"
        
        # Offer multiple alternatives when both PostgreSQL and Autonomous Database fail
        echo
        print_status "Database service options:"
        print_status "1. Use containerized PostgreSQL in OKE cluster (Recommended)"
        print_status "2. Skip database creation and configure manually later"
        print_status "3. Exit and contact Oracle support"
        echo
        read -p "Choose option (1-3) [1]: " db_option
        db_option=${db_option:-1}
        
        case $db_option in
            1)
                print_status "Will deploy PostgreSQL as a container in OKE cluster"
                export USE_CONTAINERIZED_DB=true
                return 0
                ;;
            2)
                print_warning "Skipping database creation - you'll need to configure database manually"
                export SKIP_DATABASE=true
                return 0
                ;;
            3)
                print_error "Deployment cancelled. Please contact Oracle support about:"
                print_error "1. PostgreSQL service KeyError: 'systemType' issue"
                print_error "2. Autonomous Database feature not enabled"
                return 1
                ;;
            *)
                print_error "Invalid option. Using containerized PostgreSQL."
                export USE_CONTAINERIZED_DB=true
                return 0
                ;;
        esac
    fi
}

# Function to create PostgreSQL database in current region
create_postgresql_in_current_region() {
    
    # List all PostgreSQL databases in the compartment
    existing_databases=$(oci psql db-system-collection list-db-systems \
        --compartment-id "$COMPARTMENT_ID" \
        --query 'data.items[*].{"Name":"display-name","ID":"id","State":"lifecycle-state"}' \
        --output json 2>/dev/null)
    
    if [[ $? -eq 0 && -n "$existing_databases" ]]; then
        # Check if any database exists with our target name
        target_db_id=$(echo "$existing_databases" | jq -r '.[] | select(.Name == "Quiz PostgreSQL DB") | .ID' 2>/dev/null)
        
        if [[ -n "$target_db_id" && "$target_db_id" != "null" ]]; then
            target_db_state=$(echo "$existing_databases" | jq -r '.[] | select(.Name == "Quiz PostgreSQL DB") | .State' 2>/dev/null)
            print_success "Found existing PostgreSQL database: Quiz PostgreSQL DB"
            print_status "  Database ID: $target_db_id"
            print_status "  Current State: $target_db_state"
            
            # Save the database ID
            echo "$target_db_id" >.db_system_id
            
            if [[ "$target_db_state" == "ACTIVE" ]]; then
                print_success "Existing PostgreSQL database is ACTIVE and ready for use"
                return 0
            elif [[ "$target_db_state" == "CREATING" ]]; then
                print_status "Existing PostgreSQL database is still being created..."
                print_status "Waiting for database creation to complete..."
                
                # Wait for the existing database to become ACTIVE
                print_status "Monitoring database creation progress..."
                local max_wait=1800  # 30 minutes
                local wait_interval=30
                local elapsed=0
                
                while [[ $elapsed -lt $max_wait ]]; do
                    current_state=$(oci psql db-system get \
                        --db-system-id "$target_db_id" \
                        --query 'data."lifecycle-state"' \
                        --raw-output 2>/dev/null)
                    
                    if [[ "$current_state" == "ACTIVE" ]]; then
                        print_success "PostgreSQL database creation completed successfully"
                        return 0
                    elif [[ "$current_state" == "FAILED" ]]; then
                        print_error "PostgreSQL database creation failed"
                        return 1
                    else
                        print_status "Database state: $current_state (waiting ${wait_interval}s...)"
                        sleep $wait_interval
                        elapsed=$((elapsed + wait_interval))
                    fi
                done
                
                print_error "Timeout waiting for database creation (${max_wait}s)"
                return 1
            else
                print_warning "Existing database is in state: $target_db_state"
                print_status "This may require manual intervention"
                return 1
            fi
        fi
        
        # Check if there are other databases that might conflict
        other_dbs=$(echo "$existing_databases" | jq -r '.[].Name' 2>/dev/null | grep -v "Quiz PostgreSQL DB" || echo "")
        if [[ -n "$other_dbs" ]]; then
            print_status "Found other PostgreSQL databases in compartment:"
            echo "$other_dbs" | while read -r db_name; do
                echo "  - $db_name"
            done
        fi
    fi
    
    print_status "No existing 'Quiz PostgreSQL DB' found. Creating new PostgreSQL database..."
    
    # Debug: Print the values being used
    print_status "Debug: COMPARTMENT_ID = $COMPARTMENT_ID"
    print_status "Debug: SUBNET_ID = $SUBNET_ID"  
    print_status "Debug: DB_ADMIN_PASSWORD length = ${#DB_ADMIN_PASSWORD}"
    print_status "Debug: Shape = VM.Standard.E5.Flex (1 OCPU, 16GB RAM, 20GB Storage)"

    print_status "Creating new PostgreSQL database system..."
    print_status "This process typically takes 10-15 minutes..."
    
    DB_SYSTEM_ID=$(oci psql db-system create \
        --compartment-id "$COMPARTMENT_ID" \
        --display-name "Quiz PostgreSQL DB" \
        --db-version "14" \
        --shape "VM.Standard.E5.Flex" \
        --instance-ocpu-count 1 \
        --instance-memory-size-in-gbs 16 \
        --storage-details '{"sizeInGBs":20}' \
        --network-details "{\"subnetId\":\"$SUBNET_ID\"}" \
        --credentials "{\"username\":\"postgres\",\"passwordDetails\":{\"password\":\"$DB_ADMIN_PASSWORD\"}}" \
        --wait-for-state SUCCEEDED \
        --query 'data.id' \
        --raw-output 2>&1)
    
    # Check if creation was successful
    if [[ $? -eq 0 && -n "$DB_SYSTEM_ID" && "$DB_SYSTEM_ID" != "null" ]]; then
        print_success "PostgreSQL database created successfully with ID: $DB_SYSTEM_ID"
        echo "$DB_SYSTEM_ID" >.db_system_id
        return 0
    else
        print_error "Failed to create PostgreSQL database"
        print_error "Command output: $DB_SYSTEM_ID"
        print_error "Please check the error message and verify your configuration."
        
        # Provide helpful guidance
        print_status "Common issues:"
        print_status "  1. Insufficient capacity in the region"
        print_status "  2. Network configuration issues"
        print_status "  3. Service limits exceeded"
        print_status "  4. Invalid subnet configuration"
        
        return 1
    fi
}

# Function to get database connection details
get_db_connection() {
    print_status "Getting database connection details..."

    # Check if using containerized database
    if [[ "$USE_CONTAINERIZED_DB" == "true" ]]; then
        if [[ -f .db_connection_info ]]; then
            print_status "Using containerized PostgreSQL connection details"
            source .db_connection_info
            DB_HOST="$DATABASE_HOST"
            print_success "Database host: $DB_HOST (containerized)"
            echo "$DB_HOST" >.db_host
            return 0
        else
            print_error "Containerized database connection info not found"
            return 1
        fi
    fi

    # Check if database creation was skipped
    if [[ "$SKIP_DATABASE" == "true" ]]; then
        print_warning "Database creation was skipped. Using placeholder connection details."
        DB_HOST="localhost"
        echo "$DB_HOST" >.db_host
        print_status "You'll need to configure database connection manually later"
        return 0
    fi

    # Standard managed database logic
    if [[ -f .db_system_id ]]; then
        DB_SYSTEM_ID=$(cat .db_system_id)
    else
        read -p "Enter your PostgreSQL DB System OCID: " DB_SYSTEM_ID
    fi

    DB_HOST=$(oci psql db-system get \
        --db-system-id "$DB_SYSTEM_ID" \
        --query 'data.instances[0]."primary-db-endpoint"."fqdn"' \
        --raw-output || { print_error "Failed to get database host. Exiting."; exit 1; })

    print_success "Database host: $DB_HOST"
    echo "$DB_HOST" >.db_host
}

# Function to validate PostgreSQL database system
validate_postgresql() {
    print_status "Validating PostgreSQL database system..."
    
    if [[ -f .db_system_id ]]; then
        DB_SYSTEM_ID=$(cat .db_system_id)
    else
        print_error "No database system ID found. PostgreSQL creation may have failed."
        return 1
    fi
    
    print_status "Checking database system status..."
    
    # Get database system details
    db_details=$(oci psql db-system get \
        --db-system-id "$DB_SYSTEM_ID" \
        --query 'data.{State:"lifecycle-state",DisplayName:"display-name",Shape:shape,DbVersion:"db-version",InstanceCount:"instance-count"}' \
        --output json 2>/dev/null)
    
    if [[ $? -ne 0 || -z "$db_details" ]]; then
        print_error "Failed to retrieve database system details"
        return 1
    fi
    
    # Parse and display database information
    db_state=$(echo "$db_details" | jq -r '.State // "UNKNOWN"')
    db_name=$(echo "$db_details" | jq -r '.DisplayName // "UNKNOWN"')
    db_shape=$(echo "$db_details" | jq -r '.Shape // "UNKNOWN"')
    db_version=$(echo "$db_details" | jq -r '.DbVersion // "UNKNOWN"')
    instance_count=$(echo "$db_details" | jq -r '.InstanceCount // "UNKNOWN"')
    
    print_status "Database System Details:"
    echo "  Name: $db_name"
    echo "  State: $db_state"
    echo "  Shape: $db_shape"
    echo "  Version: $db_version"
    echo "  Instances: $instance_count"
    
    # Check if database is in ACTIVE state
    if [[ "$db_state" != "ACTIVE" ]]; then
        print_error "Database system is not in ACTIVE state. Current state: $db_state"
        if [[ "$db_state" == "CREATING" ]]; then
            print_warning "Database is still being created. This may take 10-15 minutes."
            print_status "You can monitor progress in Oracle Cloud Console: Databases → PostgreSQL"
        fi
        return 1
    fi
    
    print_success "Database system is ACTIVE"
    
    # Get database connection details
    print_status "Retrieving database connection information..."
    
    db_connection=$(oci psql db-system get \
        --db-system-id "$DB_SYSTEM_ID" \
        --query 'data.instances[0]."primary-db-endpoint"' \
        --output json 2>/dev/null)
    
    if [[ $? -ne 0 || -z "$db_connection" ]]; then
        print_error "Failed to retrieve database connection details"
        return 1
    fi
    
    DB_HOST=$(echo "$db_connection" | jq -r '.fqdn // "UNKNOWN"')
    DB_PORT=$(echo "$db_connection" | jq -r '.port // "5432"')
    
    if [[ "$DB_HOST" == "UNKNOWN" || -z "$DB_HOST" ]]; then
        print_error "Failed to get database host information"
        return 1
    fi
    
    print_status "Database Connection Details:"
    echo "  Host: $DB_HOST"
    echo "  Port: $DB_PORT"
    echo "  Database: postgres"
    echo "  Username: postgres"
    
    # Save connection details
    echo "$DB_HOST" >.db_host
    echo "$DB_PORT" >.db_port
    
    # Test network connectivity to database
    print_status "Testing network connectivity to database..."
    if command -v nc >/dev/null 2>&1; then
        if timeout 10 nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null; then
            print_success "Network connectivity to database: OK"
        else
            print_warning "Network connectivity test failed. This may be normal if running from outside OCI network."
            print_status "Database connectivity will be tested from within the Kubernetes cluster during deployment."
        fi
    else
        print_warning "netcat (nc) not available. Skipping network connectivity test."
    fi
    
    # Check database configuration
    print_status "Retrieving database configuration..."
    
    config_details=$(oci psql db-system get \
        --db-system-id "$DB_SYSTEM_ID" \
        --query 'data.{ConfigId:"config-id",StorageDetails:"storage-details",SystemType:"system-type"}' \
        --output json 2>/dev/null)
    
    if [[ $? -eq 0 && -n "$config_details" ]]; then
        storage_size=$(echo "$config_details" | jq -r '.StorageDetails."size-in-g-bs" // "UNKNOWN"')
        system_type=$(echo "$config_details" | jq -r '.SystemType // "UNKNOWN"')
        
        print_status "Database Configuration:"
        echo "  Storage Size: ${storage_size}GB"
        echo "  System Type: $system_type"
    fi
    
    print_success "PostgreSQL database validation completed successfully"
    return 0
}

# Function to validate PostgreSQL prerequisites
validate_postgresql_prerequisites() {
    print_status "Validating PostgreSQL deployment prerequisites..."
    
    # Check if jq is available (needed for JSON parsing)
    if ! command -v jq >/dev/null 2>&1; then
        print_warning "jq is not installed. Installing jq for JSON parsing..."
        if command -v brew >/dev/null 2>&1; then
            brew install jq >/dev/null 2>&1 || {
                print_error "Failed to install jq via brew. Please install jq manually."
                return 1
            }
        elif command -v apt-get >/dev/null 2>&1; then
            sudo apt-get update >/dev/null 2>&1 && sudo apt-get install -y jq >/dev/null 2>&1 || {
                print_error "Failed to install jq via apt-get. Please install jq manually."
                return 1
            }
        else
            print_error "Cannot automatically install jq. Please install jq manually and retry."
            return 1
        fi
        print_success "jq installed successfully"
    fi
    
    # Validate subnet configuration for PostgreSQL
    print_status "Validating subnet configuration..."
    
    subnet_details=$(oci network subnet get \
        --subnet-id "$SUBNET_ID" \
        --query 'data.{Name:"display-name",State:"lifecycle-state",CIDR:"cidr-block"}' \
        --output json 2>/dev/null)
    
    if [[ $? -eq 0 && -n "$subnet_details" ]]; then
        subnet_name=$(echo "$subnet_details" | jq -r '.Name // "UNKNOWN"')
        subnet_state=$(echo "$subnet_details" | jq -r '.State // "UNKNOWN"')
        subnet_cidr=$(echo "$subnet_details" | jq -r '.CIDR // "UNKNOWN"')
        
        print_status "Subnet Details:"
        echo "  Name: $subnet_name"
        echo "  State: $subnet_state"
        echo "  CIDR: $subnet_cidr"
        
        if [[ "$subnet_state" != "AVAILABLE" ]]; then
            print_error "Subnet is not in AVAILABLE state: $subnet_state"
            return 1
        fi
    else
        print_warning "Could not retrieve subnet details"
    fi
    
    # Check PostgreSQL service availability in the region
    print_status "Checking PostgreSQL service availability..."
    
    # Test if we can list database systems (validates service access)
    test_access=$(oci psql db-system-collection list-db-systems \
        --compartment-id "$COMPARTMENT_ID" \
        --limit 1 \
        --query 'data.items[0].id' \
        --raw-output 2>/dev/null || echo "test_failed")
    
    if [[ "$test_access" == "test_failed" ]]; then
        print_error "Cannot access PostgreSQL service. Check your permissions and region."
        return 1
    fi
    
    print_success "PostgreSQL prerequisites validation completed"
    return 0
}

# Function to create OKE cluster
create_oke_cluster() {
    print_status "Creating OKE cluster: $CLUSTER_NAME"
    
    # Check if cluster already exists
    existing_cluster=$(oci ce cluster list \
        --compartment-id "$COMPARTMENT_ID" \
        --name "$CLUSTER_NAME" \
        --query 'data[0].id' \
        --raw-output 2>/dev/null || echo "null")
    
    if [[ "$existing_cluster" != "null" && -n "$existing_cluster" ]]; then
        print_success "OKE cluster already exists with ID: $existing_cluster"
        echo "$existing_cluster" >.cluster_id
        return 0
    fi

    print_status "Creating new OKE cluster..."
    CLUSTER_ID=$(oci ce cluster create \
        --compartment-id "$COMPARTMENT_ID" \
        --name "$CLUSTER_NAME" \
        --vcn-id "$VCN_ID" \
        --kubernetes-version "v1.31.1" \
        --service-lb-subnet-ids "[\"$LB_SUBNET_ID\"]" \
        --wait-for-state SUCCEEDED \
        --query 'data.id' \
        --raw-output 2>&1 || { 
            print_error "Failed to create OKE cluster. Command output above."
            print_error "If you've reached the cluster limit, please delete an existing cluster or request a limit increase."
            exit 1
        })

    print_success "OKE cluster created with ID: $CLUSTER_ID"
    echo "$CLUSTER_ID" >.cluster_id
}

# Function to create node pool
create_node_pool() {
    print_status "Creating node pool..."

    if [[ -f .cluster_id ]]; then
        CLUSTER_ID=$(cat .cluster_id)
    else
        read -p "Enter your OKE Cluster OCID: " CLUSTER_ID
    fi
    
    # Check if node pool already exists
    existing_node_pool=$(oci ce node-pool list \
        --compartment-id "$COMPARTMENT_ID" \
        --cluster-id "$CLUSTER_ID" \
        --name "quiz-app-nodes" \
        --query 'data[0].id' \
        --raw-output 2>/dev/null || echo "null")
    
    if [[ "$existing_node_pool" != "null" && -n "$existing_node_pool" ]]; then
        print_success "Node pool already exists with ID: $existing_node_pool"
        return 0
    fi

    print_status "Creating new node pool..."
    # Get availability domain
    AD=$(oci iam availability-domain list \
        --compartment-id "$COMPARTMENT_ID" \
        --query 'data[0].name' \
        --raw-output || { print_error "Failed to get Availability Domain. Exiting."; exit 1; })

    NODE_POOL_ID=$(oci ce node-pool create \
        --cluster-id "$CLUSTER_ID" \
        --compartment-id "$COMPARTMENT_ID" \
        --name "quiz-app-nodes" \
        --node-shape "VM.Standard.E4.Flex" \
        --node-shape-config '{"memoryInGBs": 8, "ocpus": 2}' \
        --size 2 \
        --placement-configs "[{\"availabilityDomain\": \"$AD\", \"subnetId\": \"$SUBNET_ID\"}]" \
        --wait-for-state SUCCEEDED \
        --query 'data.id' \
        --raw-output || { print_error "Failed to create node pool. Exiting."; exit 1; })

    print_success "Node pool created with ID: $NODE_POOL_ID"
}

# Function to configure kubectl
configure_kubectl() {
    print_status "Configuring kubectl..."

    if [[ -f .cluster_id ]]; then
        CLUSTER_ID=$(cat .cluster_id)
    else
        read -p "Enter your OKE Cluster OCID: " CLUSTER_ID
    fi

    oci ce cluster create-kubeconfig \
        --cluster-id "$CLUSTER_ID" \
        --file "$HOME/.kube/config" \
        --region "$REGION" \
        --token-version 2.0.0 \
        --kube-endpoint PRIVATE_ENDPOINT

    print_success "kubectl configured successfully"
}

# Function to update Kubernetes configuration
update_k8s_config() {
    print_status "Updating Kubernetes configuration..."

    if [[ -f .db_host ]]; then
        DB_HOST=$(cat .db_host)
    else
        read -p "Enter your PostgreSQL host: " DB_HOST
    fi

    # Create temporary ConfigMap with actual values
    cat >/tmp/configmap-postgres.yaml <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: quiz-app-config
  namespace: default
  labels:
    app: quiz-app
    tier: backend
data:
  SPRING_PROFILES_ACTIVE: "oci-postgres"
  OCI_POSTGRES_HOST: "$DB_HOST"
  OCI_POSTGRES_PORT: "5432"
  OCI_POSTGRES_DB: "postgres"
  OCI_POSTGRES_USERNAME: "postgres"
  SERVER_PORT: "8080"
  GMAIL_SMTP_USERNAME: "$GMAIL_USERNAME"
  GMAIL_SMTP_HOST: "smtp.gmail.com"
  GMAIL_SMTP_PORT: "587"
  ADMIN_EMAIL: "shubhamchouksey1998@gmail.com"
  ADMIN_RECEIVER_NAME: "Shubham Chouksey"
  JAVA_OPTS: "-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseContainerSupport"
---
apiVersion: v1
kind: Secret
metadata:
  name: quiz-app-secrets
  namespace: default
  labels:
    app: quiz-app
    tier: backend
type: Opaque
data:
  OCI_POSTGRES_PASSWORD: "$(echo -n "$DB_ADMIN_PASSWORD" | base64)"
  GMAIL_SMTP_PASSWORD: "$(echo -n "$GMAIL_PASSWORD" | base64)"
EOF

    # Update deployment with actual Docker image from OCIR
    sed "s|your-registry/quiz-app:latest|$DOCKER_IMAGE|g" k8s/deployment.yaml >/tmp/deployment.yaml

    print_success "Kubernetes configuration updated"
}

# Function to deploy application
deploy_application() {
    print_status "Deploying application to OKE..."

    # Apply configurations
    kubectl apply -f /tmp/configmap-postgres.yaml
    kubectl apply -f /tmp/deployment.yaml
    kubectl apply -f k8s/service.yaml
    kubectl apply -f k8s/hpa.yaml

    print_success "Application deployed successfully"

    # Wait for deployment to be ready
    print_status "Waiting for deployment to be ready..."
    kubectl wait --for=condition=available --timeout=300s deployment/quiz-app

    # Get LoadBalancer IP
    print_status "Getting LoadBalancer IP..."
    sleep 60 # Wait for LoadBalancer to be provisioned

    LB_IP=$(kubectl get svc quiz-app-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

    if [[ -n "$LB_IP" ]]; then
        print_success "Application is available at: http://$LB_IP"
    else
        print_warning "LoadBalancer IP not yet available. Check later with: kubectl get svc quiz-app-service"
    fi
}

# Function to initialize database
init_database() {
    print_status "Initializing database..."

    # Get a running pod
    POD_NAME=$(kubectl get pods -l app=quiz-app -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)

    if [[ -n "$POD_NAME" ]]; then
        print_status "Using pod: $POD_NAME"

        # Test database connection
        print_status "Testing database connection..."
        if kubectl exec "$POD_NAME" -- nc -zv "$DB_HOST" 5432; then
            print_success "Database connection successful"
        else
            print_error "Cannot connect to database"
        fi
    else
        print_warning "No running pods found to initialize database"
    fi
}

# Main execution
main() {
    echo "==========================================="
    echo "Quiz Website - Complete OCI Deployment"
    echo "==========================================="

    check_prerequisites
    read_config

    echo
    echo "Deployment Plan:"
    echo "1. Build and push Docker image"
    echo "2. Create PostgreSQL database"
    echo "3. Create OKE cluster"
    echo "4. Create node pool"
    echo "5. Configure kubectl"
    echo "6. Deploy application"
    echo "7. Initialize database"
    echo
    
    print_status "Configuration Summary:"
    echo "  Docker Image: $DOCKER_IMAGE"
    echo "  Registry: Oracle Container Registry (OCIR)"
    echo "  OCIR Region: $OCIR_REGION"
    echo "  OCIR Namespace: $OCIR_NAMESPACE"
    echo "  PostgreSQL Database: $DB_NAME"
    echo "  OKE Cluster: $CLUSTER_NAME"
    echo "  Region: $REGION"
    if [[ -n "$GMAIL_USERNAME" ]]; then
        echo "  Gmail User: $GMAIL_USERNAME"
    else
        echo "  Gmail User: Not configured"
    fi
    echo
    
    read -p 'Do you want to proceed? (y/N): ' confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        print_warning "Deployment cancelled"
        exit 0
    fi
    
    # Build and push Docker image unless skipped
    if [[ "$SKIP_DOCKER" != "true" ]]; then
        choose_and_build
    else
        print_warning "Skipping Docker build and push"
    fi
    
    # Validate PostgreSQL prerequisites before creation
    validate_postgresql_prerequisites
    
    # Create or verify PostgreSQL database
    print_status "Setting up PostgreSQL database..."
    if ! create_postgresql; then
        # Check if user chose to skip database or use containerized option
        if [[ "$SKIP_DATABASE" == "true" ]]; then
            print_warning "Database creation skipped. You'll need to configure database manually."
        elif [[ "$USE_CONTAINERIZED_DB" == "true" ]]; then
            print_warning "Will deploy containerized PostgreSQL after OKE cluster is ready."
        else
            print_error "PostgreSQL database setup failed. Deployment cannot continue."
            print_status "Troubleshooting steps:"
            print_status "  1. Check Oracle Cloud Console: Databases → PostgreSQL → DB Systems"
            print_status "  2. Verify compartment has sufficient capacity"
            print_status "  3. Check subnet configuration and security rules"
            print_status "  4. Review service limits in your tenancy"
            print_status "  5. Try running: ./scripts/deploy.sh --validate-postgresql"
            exit 1
        fi
    fi
    
    # Validate PostgreSQL after creation/verification (skip for containerized DB)
    if [[ "$USE_CONTAINERIZED_DB" != "true" && "$SKIP_DATABASE" != "true" ]]; then
        print_status "Validating PostgreSQL database system..."
        if ! validate_postgresql; then
            print_error "PostgreSQL validation failed. Deployment cannot continue."
            print_status "The database exists but may not be ready yet."
            print_status "Please check the PostgreSQL database in Oracle Cloud Console:"
            print_status "  Navigate to: Databases → PostgreSQL → DB Systems"
            print_status "  Look for: Quiz PostgreSQL DB"
            print_status "  Wait for status to become 'Active' then retry deployment"
            exit 1
        fi
    fi
    
    create_oke_cluster
    create_node_pool
    configure_kubectl
    
    # Deploy containerized PostgreSQL if selected
    if [[ "$USE_CONTAINERIZED_DB" == "true" ]]; then
        print_status "Deploying containerized PostgreSQL to OKE cluster..."
        if ! deploy_containerized_postgresql; then
            print_error "Failed to deploy containerized PostgreSQL"
            exit 1
        fi
    fi
    
    get_db_connection
    update_k8s_config
    deploy_application
    init_database

    print_success "🎉 Deployment completed successfully!"
    echo
    echo "📋 Next steps:"
    echo "1. Get LoadBalancer IP: kubectl get svc quiz-app-service"
    echo "2. Access your application at: http://<EXTERNAL-IP>"
    echo "3. Create database user if needed"
    echo "4. Set up monitoring and alerting"
    echo "5. Configure domain and SSL if required"
    echo
    echo "📊 Useful commands:"
    echo "  View pods: kubectl get pods"
    echo "  View logs: kubectl logs -l app=quiz-app"
    echo "  Scale app: kubectl scale deployment quiz-app --replicas=3"
}

# Function to show help
show_help() {
    echo "Quiz Website Deployment Script"
    echo
    echo "Usage: $0 [OPTIONS]"
    echo
    echo "DEPLOYMENT OPTIONS:"
    echo "  -h, --help                   Show this help message"
    echo "  --docker-only                Only build and push Docker image (with build method choice)"
    echo "  --cloudshell-only            Only build using OCI Cloud Shell (no local Docker)"
    echo "  --skip-docker                Skip Docker build and push"
    echo
    echo "DATABASE OPTIONS:"
    echo "  --validate-postgresql        Only validate existing PostgreSQL database"
    echo "  --test-postgresql-regions    Test PostgreSQL service across multiple regions"
    echo "  --deploy-postgresql          Deploy containerized PostgreSQL to OKE cluster"
    echo "  --check-postgresql           Check status of containerized PostgreSQL"
    echo "  --backup-postgresql          Create backup of containerized PostgreSQL database"
    echo "  --restore-postgresql [FILE]  Restore PostgreSQL database from backup file"
    echo "  --scale-postgresql [REPLICAS] Scale PostgreSQL deployment (default: 1)"
    echo
    echo "MONITORING OPTIONS:"
    echo "  --postgres-logs              View PostgreSQL container logs"
    echo "  --postgres-shell             Open shell in PostgreSQL container"
    echo "  --postgres-status            Show detailed PostgreSQL status and metrics"
    echo
    echo "Build Methods:"
    echo "  Local:      Requires Docker Desktop (must be signed in to organization)"
    echo "  Cloud Shell: Remote build in OCI Cloud Shell (no local Docker required)"
    echo
    echo "Environment Variables:"
    echo "  COMPARTMENT_ID    OCI Compartment OCID"
    echo "  VCN_ID           OCI VCN OCID"
    echo "  SUBNET_ID        OCI Subnet OCID"
    echo "  LB_SUBNET_ID     OCI Load Balancer Subnet OCID"
    echo "  DB_ADMIN_PASSWORD PostgreSQL admin password"
    echo "  DOCKER_IMAGE     Full name of the Docker image"
    echo '  OCIR_REGION      OCIR region (default: ap-mumbai-1)'
    echo '  OCIR_NAMESPACE   OCIR tenancy namespace'
    echo '  OCIR_USERNAME    OCIR username (format: namespace/username)'
    echo "  OCIR_AUTH_TOKEN  OCIR authentication token"
    echo "  GMAIL_USERNAME   Gmail username for SMTP"
    echo "  GMAIL_PASSWORD   Gmail app password"
    echo
    echo "EXAMPLES:"
    echo "  # Full deployment"
    echo "  $0"
    echo
    echo "  # Deploy only PostgreSQL"
    echo "  $0 --deploy-postgresql"
    echo
    echo "  # Check PostgreSQL status"
    echo "  $0 --check-postgresql"
    echo
    echo "  # Create database backup"
    echo "  $0 --backup-postgresql"
    echo
    echo "  # Scale PostgreSQL (for high availability)"
    echo "  $0 --scale-postgresql 2"
}

# Handle command line arguments
SKIP_DOCKER=false
case "${1:-}" in
    "-h"|"--help")
        show_help
        exit 0
        ;;
    "--docker-only")
        check_prerequisites
        read_config
        choose_and_build
        exit 0
        ;;
    "--cloudshell-only")
        check_prerequisites
        read_config
        build_and_push_docker_cloudshell
        exit 0
        ;;
    "--skip-docker")
        SKIP_DOCKER=true
        ;;
    "--validate-postgresql")
        check_prerequisites
        read_config
        validate_postgresql_prerequisites
        if validate_postgresql; then
            print_success "✅ PostgreSQL validation completed successfully"
        else
            print_error "❌ PostgreSQL validation failed"
            exit 1
        fi
        exit 0
        ;;
    "--test-postgresql-regions")
        check_prerequisites
        read_config
        print_section "🌍 Testing PostgreSQL Service Across Regions"
        if find_working_postgresql_region; then
            print_success "✅ Found working PostgreSQL regions"
            print_status "💡 Recommendation: Use one of the working regions for deployment"
        else
            print_error "❌ No working PostgreSQL regions found"
            print_status "💡 Consider using Autonomous Database as an alternative"
        fi
        exit 0
        ;;
    "--deploy-postgresql")
        check_prerequisites
        read_config
        print_section "🐘 Deploying Containerized PostgreSQL"
        if deploy_containerized_postgresql; then
            print_success "✅ PostgreSQL deployment completed successfully"
        else
            print_error "❌ PostgreSQL deployment failed"
            exit 1
        fi
        exit 0
        ;;
    "--check-postgresql")
        check_prerequisites
        if check_containerized_postgresql; then
            print_success "✅ PostgreSQL status check completed"
        else
            print_error "❌ PostgreSQL status check failed"
            exit 1
        fi
        exit 0
        ;;
    "--backup-postgresql")
        check_prerequisites
        if backup_containerized_postgresql; then
            print_success "✅ PostgreSQL backup completed successfully"
        else
            print_error "❌ PostgreSQL backup failed"
            exit 1
        fi
        exit 0
        ;;
    "--restore-postgresql")
        check_prerequisites
        if restore_containerized_postgresql "$2"; then
            print_success "✅ PostgreSQL restore completed successfully"
        else
            print_error "❌ PostgreSQL restore failed"
            exit 1
        fi
        exit 0
        ;;
    "--scale-postgresql")
        check_prerequisites
        if scale_containerized_postgresql "$2"; then
            print_success "✅ PostgreSQL scaling completed successfully"
        else
            print_error "❌ PostgreSQL scaling failed"
            exit 1
        fi
        exit 0
        ;;
    "--postgres-logs")
        check_prerequisites
        view_postgresql_logs
        exit 0
        ;;
    "--postgres-shell")
        check_prerequisites
        open_postgresql_shell
        exit 0
        ;;
    "--postgres-status")
        check_prerequisites
        show_postgresql_status
        exit 0
        ;;
    *)
        if [[ -n "$1" ]]; then
            print_error "Invalid option: $1"
            show_help
            exit 1
        fi
        ;;
esac

# Run main function
main "$@"