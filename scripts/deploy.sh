#!/bin/bash

# Quiz Website - Complete OCI Deployment Script
# This script creates OCI PostgreSQL database, OKE cluster, and deploys the application

# Exit immediately if a command exits with a non-zero status.
set -e

# Load environment variables from .env file if it exists
if [[ -f ".env" ]]; then
    echo -e "\033[0;34m[INFO]\033[0m Loading environment variables from .env file..."
    set -a
    source .env
    set +a
fi

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

# Function to create PostgreSQL database
create_postgresql() {
    print_status "Creating OCI PostgreSQL database..."

    DB_SYSTEM_ID=$(oci psql db-system create \
        --compartment-id "$COMPARTMENT_ID" \
        --display-name "Quiz PostgreSQL DB" \
        --db-version "14" \
        --shape "VM.Standard.E4.Flex" \
        --shape-config '{"ocpus": 1, "memoryInGBs": 4}' \
        --storage-details '{"sizeInGBs":20}' \
        --network-details "{\"subnetId\":\"$SUBNET_ID\"}" \
        --credentials "{\"username\":\"postgres\",\"passwordDetails\":{\"password\":\"$DB_ADMIN_PASSWORD\"}}" \
        --wait-for-state SUCCEEDED \
        --query 'data.id' \
        --raw-output || { print_error "Failed to create PostgreSQL database. Exiting."; exit 1; })

    print_success "PostgreSQL database created with ID: $DB_SYSTEM_ID"
    echo "$DB_SYSTEM_ID" >.db_system_id
}

# Function to get database connection details
get_db_connection() {
    print_status "Getting database connection details..."

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

# Function to create OKE cluster
create_oke_cluster() {
    print_status "Creating OKE cluster: $CLUSTER_NAME"

    CLUSTER_ID=$(oci ce cluster create \
        --compartment-id "$COMPARTMENT_ID" \
        --name "$CLUSTER_NAME" \
        --vcn-id "$VCN_ID" \
        --kubernetes-version "v1.28.2" \
        --service-lb-subnet-ids "[\"$LB_SUBNET_ID\"]" \
        --wait-for-state SUCCEEDED \
        --query 'data.id' \
        --raw-output || { print_error "Failed to create OKE cluster. Exiting."; exit 1; })

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
    
    create_postgresql
    create_oke_cluster
    create_node_pool
    configure_kubectl
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
    echo "OPTIONS:"
    echo "  -h, --help         Show this help message"
    echo "  --docker-only      Only build and push Docker image (with build method choice)"
    echo "  --cloudshell-only  Only build using OCI Cloud Shell (no local Docker)"
    echo "  --skip-docker      Skip Docker build and push"
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