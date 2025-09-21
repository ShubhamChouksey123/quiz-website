#!/bin/bash

# Script to verify Docker image deployment to OCIR
# Usage: ./scripts/verify-ocir-image.sh [image-name]

set -e

# Load environment variables
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

# Default image name
IMAGE_NAME="${1:-quiz-app}"
FULL_IMAGE_NAME="$OCIR_REGION.ocir.io/$OCIR_NAMESPACE/$IMAGE_NAME"

print_section "🔍 OCIR Image Verification"

print_status "Checking for image: $FULL_IMAGE_NAME"
print_status "OCIR Region: $OCIR_REGION"
print_status "OCIR Namespace: $OCIR_NAMESPACE"
print_status ""

# Check if repository exists using multiple approaches
print_status "1. Checking if repository exists..."

# Try with the configured compartment first
REPO_CHECK=$(oci artifacts container repository list \
    --compartment-id "$COMPARTMENT_ID" \
    --display-name "$IMAGE_NAME" \
    --query 'data.items[0].id' \
    --raw-output 2>/dev/null || echo "null")

# If not found, try with tenancy root compartment
if [[ "$REPO_CHECK" == "null" || -z "$REPO_CHECK" ]]; then
    print_status "   Checking in tenancy root compartment..."
    TENANCY_ID=$(oci iam tenancy get --tenancy-id "$(oci config get tenancy)" --query 'data.id' --raw-output 2>/dev/null || echo "")
    if [[ -n "$TENANCY_ID" ]]; then
        REPO_CHECK=$(oci artifacts container repository list \
            --compartment-id "$TENANCY_ID" \
            --display-name "$IMAGE_NAME" \
            --query 'data.items[0].id' \
            --raw-output 2>/dev/null || echo "null")
    fi
fi

# Also try searching by namespace
if [[ "$REPO_CHECK" == "null" || -z "$REPO_CHECK" ]]; then
    print_status "   Searching across all accessible compartments..."
    REPO_CHECK=$(oci search resource structured-search \
        --query-text "query ContainerRepository resources where displayName = '$IMAGE_NAME'" \
        --query 'data.items[0].identifier' \
        --raw-output 2>/dev/null || echo "null")
fi

if [[ "$REPO_CHECK" == "null" || -z "$REPO_CHECK" ]]; then
    print_error "❌ Repository '$IMAGE_NAME' not found in OCIR"
    print_status ""
    print_status "This means the Docker image has not been pushed yet."
    print_status "Repository will be created automatically when you first push an image."
    exit 1
else
    print_success "✅ Repository '$IMAGE_NAME' exists"
    REPO_ID="$REPO_CHECK"
fi

# List images in the repository with better error handling
print_status ""
print_status "2. Checking for images in repository..."

# Try different approaches to find images
IMAGES_FOUND=false

# Method 1: Using compartment ID
if [[ -n "$COMPARTMENT_ID" ]]; then
    IMAGES=$(oci artifacts container image list \
        --compartment-id "$COMPARTMENT_ID" \
        --repository-name "$IMAGE_NAME" \
        --query 'data.items[].{tag:display-name,digest:digest,created:time-created,size:size-in-bytes}' \
        --output table 2>/dev/null || echo "")
    
    if [[ -n "$IMAGES" && "$IMAGES" != "" ]]; then
        IMAGES_FOUND=true
    fi
fi

# Method 2: Try with tenancy root if not found
if [[ "$IMAGES_FOUND" == false ]]; then
    print_status "   Checking in tenancy root compartment..."
    TENANCY_ID=$(oci iam tenancy get --tenancy-id "$(oci config get tenancy)" --query 'data.id' --raw-output 2>/dev/null || echo "")
    if [[ -n "$TENANCY_ID" ]]; then
        IMAGES=$(oci artifacts container image list \
            --compartment-id "$TENANCY_ID" \
            --repository-name "$IMAGE_NAME" \
            --query 'data.items[].{tag:display-name,digest:digest,created:time-created,size:size-in-bytes}' \
            --output table 2>/dev/null || echo "")
        
        if [[ -n "$IMAGES" && "$IMAGES" != "" ]]; then
            IMAGES_FOUND=true
        fi
    fi
fi

# Method 3: Direct OCIR API check using curl
if [[ "$IMAGES_FOUND" == false ]]; then
    print_status "   Checking OCIR registry directly..."
    # Try to check if the repository exists via OCIR API
    REGISTRY_CHECK=$(curl -s -f -u "$OCIR_USERNAME:$OCIR_AUTH_TOKEN" \
        "https://$OCIR_REGION.ocir.io/v2/$OCIR_NAMESPACE/$IMAGE_NAME/tags/list" 2>/dev/null || echo "")
    
    if [[ -n "$REGISTRY_CHECK" && "$REGISTRY_CHECK" != *"errors"* ]]; then
        IMAGES_FOUND=true
        IMAGES="Registry API confirms image exists"
    fi
fi

if [[ "$IMAGES_FOUND" == false ]]; then
    print_warning "⚠️  No images found in repository '$IMAGE_NAME'"
    print_status ""
    print_status "The repository may exist but contains no images."
    print_status "This suggests the build/push process may not have completed successfully."
else
    print_success "✅ Images found in repository:"
    echo "$IMAGES"
fi

# Try to pull image information directly using multiple methods
print_status ""
print_status "3. Testing image accessibility..."

MANIFEST_ACCESSIBLE=false

# Method 1: OCI CLI image get
MANIFEST_CHECK=$(oci artifacts container image get \
    --image-id "$FULL_IMAGE_NAME:latest" 2>/dev/null || echo "failed")

if [[ "$MANIFEST_CHECK" != "failed" ]]; then
    MANIFEST_ACCESSIBLE=true
    print_success "✅ Image manifest accessible via OCI CLI"
    
    # Extract creation time and size info
    CREATED=$(echo "$MANIFEST_CHECK" | jq -r '.data."time-created"' 2>/dev/null || echo "unknown")
    SIZE=$(echo "$MANIFEST_CHECK" | jq -r '.data."manifest-size-in-bytes"' 2>/dev/null || echo "unknown")
    
    print_status "   Created: $CREATED"
    print_status "   Manifest Size: $SIZE bytes"
fi

# Method 2: Direct OCIR registry API check
if [[ "$MANIFEST_ACCESSIBLE" == false ]]; then
    print_status "   Trying direct OCIR API access..."
    
    # Check manifest via Docker Registry API
    MANIFEST_API_CHECK=$(curl -s -f -u "$OCIR_USERNAME:$OCIR_AUTH_TOKEN" \
        -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
        "https://$OCIR_REGION.ocir.io/v2/$OCIR_NAMESPACE/$IMAGE_NAME/manifests/latest" 2>/dev/null || echo "failed")
    
    if [[ "$MANIFEST_API_CHECK" != "failed" && -n "$MANIFEST_API_CHECK" ]]; then
        MANIFEST_ACCESSIBLE=true
        print_success "✅ Image manifest accessible via OCIR API"
        
        # Extract some basic info from manifest
        if command -v jq &>/dev/null; then
            ARCH=$(echo "$MANIFEST_API_CHECK" | jq -r '.architecture // "unknown"' 2>/dev/null || echo "unknown")
            print_status "   Architecture: $ARCH"
        fi
    fi
fi

if [[ "$MANIFEST_ACCESSIBLE" == false ]]; then
    print_warning "⚠️  Cannot access image manifest"
    print_status "   This could indicate:"
    print_status "   - Image hasn't been pushed yet"
    print_status "   - Permissions issue"
    print_status "   - Network connectivity problem"
fi

# Test Docker login and pull (optional)
print_status ""
print_status "4. Testing Docker access to OCIR..."

if command -v docker &>/dev/null; then
    # Test login
    if printf '%s' "$OCIR_AUTH_TOKEN" | docker login "$OCIR_REGION.ocir.io" --username="$OCIR_USERNAME" --password-stdin &>/dev/null; then
        print_success "✅ Docker login to OCIR successful"
        
        # Test if image can be pulled
        print_status "   Testing image pull..."
        if docker pull "$FULL_IMAGE_NAME:latest" &>/dev/null; then
            print_success "✅ Image can be pulled successfully"
            
            # Get image info
            IMAGE_INFO=$(docker inspect "$FULL_IMAGE_NAME:latest" --format='{{.Created}} | {{.Size}} bytes' 2>/dev/null || echo "unknown")
            print_status "   Image Details: $IMAGE_INFO"
            
            # Clean up pulled image
            docker rmi "$FULL_IMAGE_NAME:latest" &>/dev/null || true
        else
            print_warning "⚠️  Cannot pull image (may not exist or access issue)"
        fi
        
        # Logout
        docker logout "$OCIR_REGION.ocir.io" &>/dev/null || true
    else
        print_error "❌ Docker login to OCIR failed"
        print_status "   Check your OCIR credentials"
    fi
else
    print_warning "⚠️  Docker not available for image pull test"
fi

# Summary with improved logic
print_section "📋 Verification Summary"

if [[ "$IMAGES_FOUND" == true && "$MANIFEST_ACCESSIBLE" == true ]]; then
    print_success "🎉 SUCCESS: Docker image successfully deployed to OCIR!"
    print_status ""
    print_status "Image Details:"
    print_status "  • Repository: $IMAGE_NAME"
    print_status "  • Full Image: $FULL_IMAGE_NAME:latest"
    print_status "  • Region: $OCIR_REGION"
    print_status "  • Namespace: $OCIR_NAMESPACE"
    print_status ""
    print_status "✅ Your image is ready for Kubernetes deployment!"
elif [[ "$IMAGES_FOUND" == true ]]; then
    print_success "✅ LIKELY SUCCESS: Image appears to be in OCIR!"
    print_status ""
    print_status "Image Details:"
    print_status "  • Repository: $IMAGE_NAME"
    print_status "  • Full Image: $FULL_IMAGE_NAME:latest"
    print_status "  • Region: $OCIR_REGION"
    print_status "  • Namespace: $OCIR_NAMESPACE"
    print_status ""
    print_status "Note: Some API access limitations, but image should be available for deployment."
    print_status "✅ Proceed with Kubernetes deployment!"
elif [[ "$REPO_CHECK" != "null" ]]; then
    print_warning "⚠️  PARTIAL: Repository exists but image verification incomplete"
    print_status ""
    print_status "Possible reasons:"
    print_status "  1. Image push is still in progress"
    print_status "  2. Image is in a different compartment"
    print_status "  3. API access limitations"
    print_status ""
    print_status "Next steps:"
    print_status "  1. Check Cloud Shell build process completed successfully"
    print_status "  2. Try deploying anyway - image might be accessible to Kubernetes"
    print_status "  3. Re-run build if deployment fails"
else
    print_error "❌ FAILED: Image not found in OCIR"
    print_status ""
    print_status "Troubleshooting:"
    print_status "  1. Complete the Cloud Shell build process"
    print_status "  2. Check for errors in build script output"
    print_status "  3. Verify OCIR credentials are correct"
    print_status "  4. Ensure Docker build and push completed in Cloud Shell"
fi

print_status ""
print_status "💡 To proceed with deployment regardless of verification status:"
print_status "   ./scripts/deploy.sh --skip-docker"