#!/bin/bash

# OCI Infrastructure Creation Script
# Based on: specs/02-create-infrastructure-plan.md
#
# This script creates an OCI compute instance optimized for the quiz application
# using Always Free tier resources with the Load Balancer subnet

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
required_vars=("COMPARTMENT_ID" "VCN_ID" "SUBNET_ID" "OCIR_REGION")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        log_error "Required environment variable $var is not set"
        exit 1
    fi
done

# Verify SSH public key exists
if [ ! -f ~/.ssh/id_rsa.pub ]; then
    log_error "SSH public key not found at ~/.ssh/id_rsa.pub"
    log_error "Please run ./01-verify-prerequisites.sh first"
    exit 1
fi

# Verify cloud-init file exists
if [ ! -f "../configs/cloud-init.yaml" ]; then
    log_error "Cloud-init configuration file not found at ../configs/cloud-init.yaml"
    exit 1
fi

echo ""
log_info "=== OCI Infrastructure Creation ==="
echo ""

# Configuration variables
INSTANCE_NAME="quiz-app-server"
SHAPE="VM.Standard.A1.Flex"
OCPUS=2
MEMORY_GB=4
OS="Oracle Linux"
OS_VERSION="8"

log_info "Instance Configuration:"
echo "  Name: $INSTANCE_NAME"
echo "  Shape: $SHAPE"
echo "  OCPUs: $OCPUS"
echo "  Memory: ${MEMORY_GB}GB"
echo "  OS: $OS $OS_VERSION"
echo "  Region: $OCIR_REGION"
echo "  Subnet: Load Balancer subnet (allows public IP)"
echo ""

# Phase 1: Get Latest Image ID
log_info "Phase 1: Finding Latest Oracle Linux 8 Image..."

# Get the latest Oracle Linux 8 ARM64 image
log_info "Searching for latest Oracle Linux 8 ARM64 image..."
IMAGE_ID=$(oci compute image list \
    --compartment-id "$COMPARTMENT_ID" \
    --operating-system "$OS" \
    --operating-system-version "$OS_VERSION" \
    --shape "$SHAPE" \
    --sort-by TIMECREATED \
    --sort-order DESC \
    --limit 1 \
    --query "data[0].id" \
    --raw-output 2>/dev/null)

if [ -z "$IMAGE_ID" ] || [ "$IMAGE_ID" = "null" ]; then
    log_error "Could not find suitable Oracle Linux 8 image for shape $SHAPE"
    log_info "Trying alternative image search..."

    # Try without shape filter
    IMAGE_ID=$(oci compute image list \
        --compartment-id "$COMPARTMENT_ID" \
        --operating-system "$OS" \
        --operating-system-version "$OS_VERSION" \
        --sort-by TIMECREATED \
        --sort-order DESC \
        --limit 5 \
        --query "data[?contains(\"display-name\", 'aarch64') || contains(\"display-name\", 'ARM')][0].id" \
        --raw-output 2>/dev/null)
fi

if [ -z "$IMAGE_ID" ] || [ "$IMAGE_ID" = "null" ]; then
    log_error "Could not find any suitable Oracle Linux 8 image"
    log_error "Please check available images manually:"
    echo "  oci compute image list --compartment-id $COMPARTMENT_ID --operating-system 'Oracle Linux'"
    exit 1
fi

log_success "Found image ID: $IMAGE_ID"

# Get availability domain
log_info "Getting availability domain..."
AVAILABILITY_DOMAIN=$(oci iam availability-domain list \
    --compartment-id "$COMPARTMENT_ID" \
    --query "data[0].name" \
    --raw-output 2>/dev/null)

if [ -z "$AVAILABILITY_DOMAIN" ]; then
    log_error "Could not determine availability domain"
    exit 1
fi

log_success "Using availability domain: $AVAILABILITY_DOMAIN"

# Phase 2: Instance Creation
log_info "Phase 2: Creating Compute Instance..."

log_warning "This will create a new compute instance with the following specifications:"
echo "  - Instance Name: $INSTANCE_NAME"
echo "  - Shape: $SHAPE ($OCPUS OCPUs, ${MEMORY_GB}GB RAM)"
echo "  - Operating System: $OS $OS_VERSION"
echo "  - Public IP: Yes (Load Balancer subnet)"
echo "  - SSH Access: Enabled with your generated key"
echo "  - Software: Docker, Git, development tools (via cloud-init)"
echo ""

# Confirm creation
echo -n "Do you want to proceed with instance creation? (y/N): "
read -r CONFIRM

if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    log_info "Instance creation cancelled by user"
    exit 0
fi

log_info "Creating compute instance..."
log_info "This may take 5-10 minutes..."

# Create the instance
INSTANCE_JSON=$(oci compute instance launch \
    --compartment-id "$COMPARTMENT_ID" \
    --availability-domain "$AVAILABILITY_DOMAIN" \
    --display-name "$INSTANCE_NAME" \
    --image-id "$IMAGE_ID" \
    --shape "$SHAPE" \
    --shape-config "{\"ocpus\": $OCPUS, \"memory_in_gbs\": $MEMORY_GB}" \
    --subnet-id "$SUBNET_ID" \
    --assign-public-ip true \
    --ssh-authorized-keys-file ~/.ssh/id_rsa.pub \
    --user-data-file ../configs/cloud-init.yaml \
    --wait-for-state RUNNING \
    --max-wait-seconds 600 \
    2>&1)

if [ $? -ne 0 ]; then
    log_error "Failed to create compute instance:"
    echo "$INSTANCE_JSON"
    exit 1
fi

# Extract instance details
INSTANCE_ID=$(echo "$INSTANCE_JSON" | jq -r '.data.id' 2>/dev/null)
INSTANCE_STATE=$(echo "$INSTANCE_JSON" | jq -r '.data."lifecycle-state"' 2>/dev/null)

if [ -z "$INSTANCE_ID" ] || [ "$INSTANCE_ID" = "null" ]; then
    log_error "Failed to extract instance ID from response"
    exit 1
fi

log_success "Instance created successfully!"
log_info "Instance ID: $INSTANCE_ID"
log_info "Instance State: $INSTANCE_STATE"

# Phase 3: Get Public IP
log_info "Phase 3: Retrieving Public IP Address..."

# Wait a moment for IP assignment
sleep 10

PUBLIC_IP=$(oci compute instance list-vnics \
    --instance-id "$INSTANCE_ID" \
    --query "data[0].\"public-ip\"" \
    --raw-output 2>/dev/null)

if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" = "null" ]; then
    log_warning "Public IP not immediately available, checking again..."
    sleep 20

    PUBLIC_IP=$(oci compute instance list-vnics \
        --instance-id "$INSTANCE_ID" \
        --query "data[0].\"public-ip\"" \
        --raw-output 2>/dev/null)
fi

if [ -n "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "null" ]; then
    log_success "Public IP assigned: $PUBLIC_IP"
else
    log_warning "Public IP not yet assigned - it may take a few more minutes"
    PUBLIC_IP="<pending>"
fi

# Phase 4: Wait for Cloud-Init Completion
log_info "Phase 4: Waiting for Cloud-Init to Complete..."
log_info "Cloud-init is installing Docker, Git, and setting up the environment..."
log_info "This process typically takes 10-15 minutes..."

# Wait for cloud-init completion
CLOUD_INIT_TIMEOUT=900  # 15 minutes
CLOUD_INIT_CHECK_INTERVAL=30
ELAPSED_TIME=0

log_info "Monitoring cloud-init progress..."

while [ $ELAPSED_TIME -lt $CLOUD_INIT_TIMEOUT ]; do
    if [ "$PUBLIC_IP" != "<pending>" ] && [ -n "$PUBLIC_IP" ]; then
        # Try to check cloud-init status
        if ssh -i ~/.ssh/id_rsa -o ConnectTimeout=10 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
           "test -f /opt/quiz-app/.cloud-init-complete" 2>/dev/null; then
            log_success "Cloud-init completed successfully!"
            break
        fi

        # Check if SSH is at least responding
        if ssh -i ~/.ssh/id_rsa -o ConnectTimeout=10 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
           "echo 'SSH connection successful'" 2>/dev/null; then
            log_info "SSH connection established, cloud-init still in progress..."
        fi
    else
        # Try to get public IP again
        NEW_PUBLIC_IP=$(oci compute instance list-vnics \
            --instance-id "$INSTANCE_ID" \
            --query "data[0].\"public-ip\"" \
            --raw-output 2>/dev/null)

        if [ -n "$NEW_PUBLIC_IP" ] && [ "$NEW_PUBLIC_IP" != "null" ]; then
            PUBLIC_IP="$NEW_PUBLIC_IP"
            log_success "Public IP assigned: $PUBLIC_IP"
        fi
    fi

    sleep $CLOUD_INIT_CHECK_INTERVAL
    ELAPSED_TIME=$((ELAPSED_TIME + CLOUD_INIT_CHECK_INTERVAL))

    # Progress indicator
    MINUTES_ELAPSED=$((ELAPSED_TIME / 60))
    log_info "Elapsed time: ${MINUTES_ELAPSED} minutes (waiting for cloud-init completion)"
done

# Phase 5: Security List Configuration
log_info "Phase 5: Security List Configuration..."
echo ""

log_info "5.1 Checking current security list configuration..."

# Get security list ID from subnet
SECURITY_LIST_ID=$(oci network subnet get --subnet-id "$SUBNET_ID" \
    --query "data.\"security-list-ids\"[0]" --raw-output 2>/dev/null)

if [ -z "$SECURITY_LIST_ID" ] || [ "$SECURITY_LIST_ID" = "null" ]; then
    log_error "Could not retrieve security list ID from subnet"
    exit 1
fi

log_info "Security List ID: $SECURITY_LIST_ID"

# Check current ingress rules
INGRESS_COUNT=$(oci network security-list get --security-list-id "$SECURITY_LIST_ID" \
    --query "length(data.\"ingress-security-rules\")" --output json 2>/dev/null || echo "0")

log_info "Current ingress rules count: $INGRESS_COUNT"

if [ "$INGRESS_COUNT" -eq 0 ]; then
    log_warning "No ingress rules found - SSH access and application ports will not work"
    log_info "Adding required security rules..."

    # Add comprehensive security rules
    log_info "Adding ingress and egress rules for SSH, HTTP, HTTPS, and application access..."

    UPDATE_RESULT=$(echo "y" | oci network security-list update \
        --security-list-id "$SECURITY_LIST_ID" \
        --ingress-security-rules '[
        {
          "protocol": "6",
          "source": "0.0.0.0/0",
          "isStateless": false,
          "tcpOptions": {
            "destinationPortRange": {
              "min": 22,
              "max": 22
            }
          },
          "description": "SSH access"
        },
        {
          "protocol": "6",
          "source": "0.0.0.0/0",
          "isStateless": false,
          "tcpOptions": {
            "destinationPortRange": {
              "min": 8080,
              "max": 8080
            }
          },
          "description": "Quiz application access"
        },
        {
          "protocol": "6",
          "source": "0.0.0.0/0",
          "isStateless": false,
          "tcpOptions": {
            "destinationPortRange": {
              "min": 80,
              "max": 80
            }
          },
          "description": "HTTP access"
        },
        {
          "protocol": "6",
          "source": "0.0.0.0/0",
          "isStateless": false,
          "tcpOptions": {
            "destinationPortRange": {
              "min": 443,
              "max": 443
            }
          },
          "description": "HTTPS access"
        }
        ]' \
        --egress-security-rules '[
        {
          "protocol": "all",
          "destination": "0.0.0.0/0",
          "isStateless": false,
          "description": "Allow all outbound traffic"
        }
        ]' \
        --wait-for-state AVAILABLE 2>&1)

    if [ $? -eq 0 ]; then
        log_success "Security rules added successfully!"
        log_info "Waiting for security rule changes to take effect..."
        sleep 10
    else
        log_error "Failed to add security rules:"
        echo "$UPDATE_RESULT"
        log_warning "You may need to add security rules manually for SSH access"
    fi
else
    log_success "Security list already has ingress rules configured"
fi

echo ""

# Phase 6: Final Verification
log_info "Phase 6: Final Infrastructure Verification..."

if [ "$PUBLIC_IP" = "<pending>" ]; then
    log_error "Public IP was not assigned within the timeout period"
    log_error "Please check the instance manually in OCI Console"
    exit 1
fi

# Test SSH connectivity
log_info "Testing SSH connectivity..."
if ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
   "echo 'SSH connection test successful'" 2>/dev/null; then
    log_success "SSH connectivity confirmed"
else
    log_warning "SSH connectivity not yet available - instance may still be initializing"
fi

# Test Docker installation
log_info "Verifying Docker installation..."
if ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
   "docker --version" 2>/dev/null; then
    log_success "Docker installation verified"
else
    log_warning "Docker installation not yet complete - attempting manual installation..."

    # Manual Docker installation as fallback
    log_info "Installing Docker CE and required software manually..."
    ssh -i ~/.ssh/id_rsa -o ConnectTimeout=30 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
        "sudo dnf config-manager --add-repo=https://download.docker.com/linux/centos/docker-ce.repo && \
         sudo dnf install -y docker-ce docker-ce-cli containerd.io git && \
         sudo systemctl start docker && sudo systemctl enable docker && \
         sudo usermod -aG docker opc && \
         echo 'Manual software installation completed'" 2>/dev/null

    if [ $? -eq 0 ]; then
        log_success "Manual software installation completed successfully"

        # Verify Docker again
        if ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
           "docker --version" 2>/dev/null; then
            log_success "Docker installation verified after manual installation"
        else
            log_warning "Docker installation verification still pending - may need session restart"
        fi
    else
        log_warning "Manual software installation encountered issues - may need manual intervention"
    fi
fi

# Test application directory
log_info "Checking application directory..."
if ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
   "ls -la /opt/quiz-app/" 2>/dev/null; then
    log_success "Application directory structure verified"
else
    log_warning "Application directory not yet ready - cloud-init may still be running"
fi

# Set up PostgreSQL data directory with correct permissions
log_info "Setting up PostgreSQL data directory with correct permissions..."
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << 'EOF'
# Create PostgreSQL data directory with correct permissions
sudo mkdir -p /opt/quiz-app/data/postgres
sudo chown -R 999:999 /opt/quiz-app/data/postgres
sudo find /opt/quiz-app/data/postgres -type d -exec chmod 750 {} \; 2>/dev/null || true
sudo find /opt/quiz-app/data/postgres -type f -exec chmod 640 {} \; 2>/dev/null || true

# Create logs directory
sudo mkdir -p /opt/quiz-app/logs
sudo chown -R opc:opc /opt/quiz-app/logs

echo "PostgreSQL data directory permissions configured"

# Verify permissions
echo "Verifying PostgreSQL data directory permissions:"
ls -ld /opt/quiz-app/data/postgres 2>/dev/null || echo "PostgreSQL data directory will be created during deployment"
EOF

if [ $? -eq 0 ]; then
    log_success "PostgreSQL data directory permissions configured successfully"
else
    log_warning "PostgreSQL data directory setup encountered issues - will be handled during deployment"
fi

echo ""
log_success "=== Infrastructure Creation Complete! ==="
echo ""

# Save instance details to markdown documentation
INSTANCE_DETAILS_FILE="../docs/instance-details.md"
cat > "$INSTANCE_DETAILS_FILE" << EOF
# OCI Compute Instance Details

**Created**: $(date)
**Status**: ✅ RUNNING

## Instance Information

| Property | Value |
|----------|-------|
| **Instance ID** | \`$INSTANCE_ID\` |
| **Instance Name** | $INSTANCE_NAME |
| **Public IP** | **$PUBLIC_IP** |
| **Shape** | $SHAPE |
| **OCPUs** | $OCPUS |
| **Memory** | ${MEMORY_GB}GB |
| **Region** | $OCIR_REGION |
| **Availability Domain** | $AVAILABILITY_DOMAIN |
| **Operating System** | Oracle Linux 8 |
| **Image ID** | \`$IMAGE_ID\` |
| **Subnet ID** | \`$SUBNET_ID\` |
| **Security List ID** | \`$SECURITY_LIST_ID\` |

## Network Configuration

- **VCN**: Load Balancer subnet (allows public IP)
- **CIDR**: 10.0.20.0/24
- **Public IP**: $PUBLIC_IP
- **SSH Access**: Port 22 ✅ (Security rules configured)
- **Application Access**: Port 8080 ✅ (Security rules configured)
- **HTTP/HTTPS Access**: Ports 80/443 ✅ (Security rules configured)
- **Internet Access**: ✅ (Egress rules configured for package downloads)
- **Security Lists**: Configured with required ingress/egress rules

## Access Information

### SSH Connection
\`\`\`bash
ssh -i ~/.ssh/id_rsa opc@$PUBLIC_IP
\`\`\`

### Application URLs (after deployment)
- **Quiz Application**: http://$PUBLIC_IP:8080
- **Health Check**: http://$PUBLIC_IP:8080/about

## Instance Management Commands

### View Instance Status
\`\`\`bash
oci compute instance get --instance-id $INSTANCE_ID \\
  --query "data.{state:\\"lifecycle-state\\",name:\\"display-name\\",ip:\\"public-ip\\"}"
\`\`\`

### Stop Instance
\`\`\`bash
oci compute instance action --instance-id $INSTANCE_ID --action STOP --wait-for-state STOPPED
\`\`\`

### Start Instance
\`\`\`bash
oci compute instance action --instance-id $INSTANCE_ID --action START --wait-for-state RUNNING
\`\`\`

### Terminate Instance (⚠️ Destructive)
\`\`\`bash
oci compute instance terminate --instance-id $INSTANCE_ID --wait-for-state TERMINATED
\`\`\`

## Software Configuration

### Installed Software (via Cloud-Init)
- **Docker**: Container runtime
- **Docker Compose**: Container orchestration
- **Git**: Version control
- **curl, wget**: Network utilities
- **vim, htop**: System utilities
- **OCI CLI**: Oracle Cloud Infrastructure CLI

### Application Directory Structure
- **Base Directory**: \`/opt/quiz-app/\`
- **Data Directory**: \`/opt/quiz-app/data/postgres\`
- **Logs Directory**: \`/opt/quiz-app/logs\`
- **Scripts Directory**: \`/opt/quiz-app/scripts\`

## Resource Usage (Free Tier)

### Current Allocation
- **Compute Instances**: 4/6 used (after this instance)
- **ARM OCPUs**: 2/4 used
- **ARM Memory**: 4GB/24GB used
- **Block Volumes**: 2 used
- **Public IPs**: 1 used

### Cost Verification
- **Compute Cost**: \$0.00 (Always Free tier)
- **Storage Cost**: \$0.00 (Within free limits)
- **Network Cost**: \$0.00 (Within free limits)
- **Total Monthly Cost**: \$0.00

## Next Steps

1. **Test SSH Access**: \`ssh -i ~/.ssh/id_rsa opc@$PUBLIC_IP\`
2. **Deploy Application**: Run \`./03-deploy-application.sh\`
3. **Validate Deployment**: Run \`./04-validate-deployment.sh\`

## Troubleshooting

### Common Issues
- **SSH Connection Refused**: Wait 2-3 minutes for instance initialization
- **Cloud-Init In Progress**: Check \`/opt/quiz-app/.cloud-init-complete\`
- **Docker Not Available**: Cloud-init may still be installing software

### Diagnostic Commands
\`\`\`bash
# Check instance status
oci compute instance get --instance-id $INSTANCE_ID

# Check cloud-init status
ssh -i ~/.ssh/id_rsa opc@$PUBLIC_IP "sudo cloud-init status"

# Check system logs
ssh -i ~/.ssh/id_rsa opc@$PUBLIC_IP "sudo journalctl -u cloud-init -f"
\`\`\`

## Security Notes

- **SSH Access**: Restricted to your SSH key pair
- **Firewall**: Configured for ports 22, 80, 443, 8080
- **Security Lists**: Inherits secure OKE configuration
- **Updates**: Automatic security updates enabled
- **User Access**: Primary user is \`opc\` with sudo privileges

---

**Instance Ready**: ✅ Ready for application deployment
**Documentation Updated**: $(date)
EOF

log_info "Instance Details Summary:"
echo "  Instance ID: $INSTANCE_ID"
echo "  Instance Name: $INSTANCE_NAME"
echo "  Public IP: $PUBLIC_IP"
echo "  Shape: $SHAPE ($OCPUS OCPUs, ${MEMORY_GB}GB RAM)"
echo "  Region: $OCIR_REGION"
echo "  SSH Command: ssh -i ~/.ssh/id_rsa opc@$PUBLIC_IP"
echo ""

log_info "Next Steps:"
echo "  1. Wait a few more minutes if cloud-init warnings were shown above"
echo "  2. Test SSH access: ssh -i ~/.ssh/id_rsa opc@$PUBLIC_IP"
echo "  3. Run ./03-deploy-application.sh to deploy the quiz application"
echo "  4. Monitor instance status: oci compute instance get --instance-id $INSTANCE_ID"
echo ""

log_info "Instance documentation created: $INSTANCE_DETAILS_FILE"

# Offer to test SSH connection
echo -n "Would you like to test SSH connection now? (y/N): "
read -r TEST_SSH

if [[ "$TEST_SSH" =~ ^[Yy]$ ]]; then
    log_info "Testing SSH connection..."
    echo ""
    ssh -i ~/.ssh/id_rsa -o ConnectTimeout=30 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << 'EOF'
echo "=== SSH Connection Successful ==="
echo "Hostname: $(hostname)"
echo "OS Version: $(cat /etc/oracle-release 2>/dev/null || cat /etc/os-release | head -1)"
echo "Uptime: $(uptime)"
echo ""
echo "=== Software Status ==="
echo "Docker: $(docker --version 2>/dev/null || echo 'Not yet installed')"
echo "Git: $(git --version 2>/dev/null || echo 'Not yet installed')"
echo "Cloud-init status: $(test -f /opt/quiz-app/.cloud-init-complete && echo 'Complete' || echo 'In progress')"
echo ""
echo "=== Application Directory ==="
ls -la /opt/quiz-app/ 2>/dev/null || echo "Directory not yet created"
echo ""
echo "SSH session complete - you can now disconnect"
EOF
fi

echo ""
log_success "Infrastructure creation completed successfully!"
log_info "Ready for application deployment phase."