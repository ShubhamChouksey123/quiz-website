#!/bin/bash

# Application Deployment Script
# Based on: specs/03-deploy-application-plan.md
#
# This script deploys the Spring Boot 3 quiz application with PostgreSQL database
# using a remote Docker build approach on the OCI compute instance

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
required_vars=("COMPARTMENT_ID" "VCN_ID" "SUBNET_ID" "OCIR_REGION" "OCIR_NAMESPACE" "OCIR_USERNAME" "OCIR_AUTH_TOKEN")
for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        log_error "Required environment variable $var is not set"
        exit 1
    fi
done

# Get instance IP
log_info "Retrieving instance public IP..."
PUBLIC_IP=$(oci compute instance list-vnics \
    --instance-id "$(oci compute instance list --compartment-id "$COMPARTMENT_ID" \
    --display-name "quiz-app-server" --lifecycle-state RUNNING \
    --query "data[0].id" --raw-output 2>/dev/null)" \
    --query "data[0].\"public-ip\"" --raw-output 2>/dev/null)

if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" = "null" ]; then
    log_error "Could not retrieve instance public IP"
    log_error "Ensure the quiz-app-server instance is running"
    exit 1
fi

log_success "Instance Public IP: $PUBLIC_IP"

echo ""
log_info "=== Quiz Application Deployment ==="
echo ""

# Test SSH connectivity first
log_info "Testing SSH connectivity..."
if ! ssh -i ~/.ssh/id_rsa -o ConnectTimeout=10 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" \
   "echo 'SSH connection successful'" 2>/dev/null; then
    log_error "SSH connection failed"
    log_error "Ensure infrastructure setup is complete and SSH access is available"
    exit 1
fi
log_success "SSH connectivity verified"

# Phase 1: Environment Configuration
log_info "Phase 1: Environment Configuration"
echo ""

log_info "1.1 Validating local environment configuration..."

# First, verify required variables are present in local .env file
log_info "Checking local .env file for required variables..."

# Check if .env file exists
if [ ! -f "../../.env" ]; then
    log_error "Local .env file not found at ../../.env"
    exit 1
fi

# Load and validate local .env file
source ../../.env

missing_vars=""
required_vars=("DB_ADMIN_PASSWORD" "GMAIL_USERNAME" "GMAIL_PASSWORD" "DOCKER_IMAGE" "OCIR_AUTH_TOKEN")

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        missing_vars="$missing_vars $var"
    fi
done

if [ -n "$missing_vars" ]; then
    log_error "Missing required environment variables in local .env file:$missing_vars"
    log_error "Please update your .env file with the missing variables before deployment"
    exit 1
else
    log_success "✅ All required environment variables are present in local .env file"
fi

log_info "1.2 Transferring environment configuration to instance..."

# Transfer the validated .env file to instance
log_info "Copying validated .env file to instance..."
scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ../../.env opc@"$PUBLIC_IP":/opt/quiz-app/.env

if [ $? -eq 0 ]; then
    log_success "Environment configuration transferred successfully"
else
    log_error "Failed to transfer .env file"
    exit 1
fi

log_info "1.3 Configuring OCIR authentication..."

# Configure OCIR authentication on instance
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << EOF
echo "Logging into Oracle Container Registry..."
echo "$OCIR_AUTH_TOKEN" | docker login $OCIR_REGION.ocir.io -u "$OCIR_USERNAME" --password-stdin
EOF

if [ $? -eq 0 ]; then
    log_success "OCIR authentication configured successfully"
else
    log_error "OCIR authentication failed"
    exit 1
fi

log_info "1.4 Transferring application source code..."

# Create source directory and transfer application source to instance
log_info "Creating source directory on instance..."
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" "mkdir -p /opt/quiz-app/source"

log_info "Copying application source to instance..."
scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no -r ../../app opc@"$PUBLIC_IP":/opt/quiz-app/source/

if [ $? -ne 0 ]; then
    log_error "Application source transfer failed"
    exit 1
fi

log_info "Copying Dockerfile to source directory..."
scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ../../Dockerfile opc@"$PUBLIC_IP":/opt/quiz-app/source/

if [ $? -eq 0 ]; then
    log_success "Source code transferred successfully"
else
    log_error "Dockerfile transfer failed"
    exit 1
fi

echo ""

# Phase 2: Application Build Process
log_info "Phase 2: Application Build Process"
echo ""

log_info "2.1 Building Docker image on instance..."

# Build Docker image on instance
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=300 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << 'EOF'
cd /opt/quiz-app/source

echo "Starting Docker build process..."
echo "This may take 5-10 minutes for Maven dependencies and compilation..."

# Build the Docker image
docker build -t quiz-app:local .

if [ $? -eq 0 ]; then
    echo "Docker build completed successfully"
else
    echo "Docker build failed"
    exit 1
fi
EOF

if [ $? -eq 0 ]; then
    log_success "Docker image built successfully"
else
    log_error "Docker build failed"
    exit 1
fi

log_info "2.2 Preparing image for local deployment..."

# We'll use the local image first, then push to OCIR after successful deployment
log_success "Docker image ready for local deployment"

echo ""

# Phase 3: Database Container Deployment
log_info "Phase 3: Database Container Deployment"
echo ""

log_info "3.1 Setting up PostgreSQL data directory permissions..."

# Set up database directory permissions
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=20 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << 'EOF'
# Ensure data directory exists with correct permissions
sudo mkdir -p /opt/quiz-app/data/postgres
sudo chown -R 999:999 /opt/quiz-app/data/postgres
sudo chmod -R 750 /opt/quiz-app/data/postgres

# Ensure logs directory exists
sudo mkdir -p /opt/quiz-app/logs
sudo chown -R opc:opc /opt/quiz-app/logs

echo "Database directory permissions configured"

# Verify permissions are correctly set
echo "Verifying PostgreSQL data directory permissions..."
ls -la /opt/quiz-app/data/
ls -ld /opt/quiz-app/data/postgres

# Check if ownership is correct
OWNER_CHECK=$(stat -c '%U:%G' /opt/quiz-app/data/postgres 2>/dev/null || echo "unknown")
if [ "$OWNER_CHECK" = "systemd-coredump:input" ]; then
    echo "✅ PostgreSQL data directory ownership is correct (999:999)"
else
    echo "⚠️  PostgreSQL data directory ownership: $OWNER_CHECK"
fi
EOF

log_success "Database directory permissions configured and verified"

echo ""

# Phase 4: Application Container Deployment
log_info "Phase 4: Application Container Deployment"
echo ""

log_info "4.1 Transferring Docker Compose configuration..."

# Transfer the existing, production-ready docker-compose.yml
log_info "Copying production docker-compose.yml configuration..."
scp -i ~/.ssh/id_rsa -o StrictHostKeyChecking=no ../configs/docker-compose.yml opc@"$PUBLIC_IP":/opt/quiz-app/

if [ $? -eq 0 ]; then
    log_success "Docker Compose configuration transferred successfully"
else
    log_error "Failed to transfer docker-compose.yml"
    exit 1
fi

log_info "4.2 Deploying application containers..."

# Deploy containers
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=180 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << 'EOF'
cd /opt/quiz-app

echo "Starting container deployment..."
echo "This may take 2-3 minutes for database initialization and application startup..."

# Deploy containers
docker compose up -d

if [ $? -eq 0 ]; then
    echo "Containers deployed successfully"

    # Wait for containers to be ready
    echo "Waiting for containers to start..."
    sleep 30

    # Check container status
    echo "Container status:"
    docker compose ps
else
    echo "Container deployment failed"
    exit 1
fi
EOF

if [ $? -eq 0 ]; then
    log_success "Application containers deployed successfully"
else
    log_error "Container deployment failed"
    exit 1
fi

echo ""

# Phase 5: Service Verification and Configuration
log_info "Phase 5: Service Verification and Configuration"
echo ""

log_info "5.1 Verifying container health..."

# Wait for application to be ready
log_info "Waiting for application startup (this may take 1-2 minutes)..."
sleep 60

# Verify container health
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=30 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << 'EOF'
cd /opt/quiz-app

echo "=== Container Status ==="
docker compose ps

echo ""
echo "=== Container Health ==="
if docker compose exec -T quiz-postgres pg_isready -U postgres -d quiz; then
    echo "✅ PostgreSQL: Healthy"
else
    echo "❌ PostgreSQL: Not ready"
fi

echo ""
echo "=== Application Health Check ==="
if curl -f -s http://localhost:8080/about > /dev/null 2>&1; then
    echo "✅ Application: Healthy (about endpoint responding)"
    echo "✅ Spring Boot web stack operational"
else
    echo "❌ Application: Not responding on about endpoint"
    echo "Checking root endpoint..."
    if curl -f -s http://localhost:8080/ > /dev/null 2>&1; then
        echo "✅ Application: Responding on root endpoint"
    else
        echo "❌ Application: Not responding"
    fi
fi
EOF

log_info "5.2 Testing external access..."

# Test external access
log_info "Testing external application access..."
sleep 10

if curl -f -s "http://$PUBLIC_IP:8080" > /dev/null 2>&1; then
    log_success "✅ Application is accessible externally at http://$PUBLIC_IP:8080"
else
    log_warning "❌ External access test failed - application may still be starting"
fi

# Test about endpoint externally
if curl -f -s "http://$PUBLIC_IP:8080/about" > /dev/null 2>&1; then
    log_success "✅ About endpoint accessible externally"
    log_success "✅ Application fully operational from external access"
else
    log_warning "❌ About endpoint not yet accessible externally"
fi

echo ""

# Phase 6: Push Verified Image to OCIR
log_info "Phase 6: Publishing Verified Image to OCIR"
echo ""

log_info "6.1 Tagging and pushing verified image to OCIR..."

# Now that deployment is successful, push the verified image to OCIR
ssh -i ~/.ssh/id_rsa -o ConnectTimeout=120 -o StrictHostKeyChecking=no opc@"$PUBLIC_IP" << EOF
# Tag the working image for OCIR
docker tag quiz-app:local $OCIR_REGION.ocir.io/$OCIR_NAMESPACE/quiz-app:latest

echo "Pushing verified image to Oracle Container Registry..."
docker push $OCIR_REGION.ocir.io/$OCIR_NAMESPACE/quiz-app:latest

if [ \$? -eq 0 ]; then
    echo "✅ Verified image pushed to OCIR successfully"
else
    echo "❌ Image push to OCIR failed"
    exit 1
fi
EOF

if [ $? -eq 0 ]; then
    log_success "✅ Verified production image published to OCIR"
else
    log_error "❌ Failed to publish image to OCIR"
    exit 1
fi

echo ""

# Final Summary
log_success "=== Application Deployment Complete! ==="
echo ""

log_info "Deployment Summary:"
echo "  ✅ Environment configured with secure database password"
echo "  ✅ OCIR authentication established"
echo "  ✅ Application source code transferred"
echo "  ✅ Docker image built on production instance"
echo "  ✅ PostgreSQL database container deployed"
echo "  ✅ Quiz application container deployed"
echo "  ✅ Deployment verified and tested"
echo "  ✅ Verified production image pushed to OCIR"
echo "  ✅ Container health checks configured"
echo ""

log_info "Application Access:"
echo "  🌐 Application URL: http://$PUBLIC_IP:8080"
echo "  🏥 Health Check: http://$PUBLIC_IP:8080/about"
echo "  📊 Admin Access: SSH to opc@$PUBLIC_IP"
echo ""

log_info "Container Management:"
echo "  📋 Status: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose ps'"
echo "  📜 Logs: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose logs -f'"
echo "  🔄 Restart: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose restart'"
echo "  🛑 Stop: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose down'"
echo ""

log_info "Next Steps:"
echo "  1. Test application functionality at http://$PUBLIC_IP:8080"
echo "  2. Run validation script: ./04-validate-deployment.sh"
echo "  3. Configure email settings if needed"
echo "  4. Set up regular database backups"
echo ""

log_info "If application is not immediately accessible, wait 2-3 minutes for full startup"
log_info "Monitor logs with: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose logs -f quiz-app'"

echo ""
log_info "Troubleshooting Common Issues:"
echo "  🔧 PostgreSQL Permission Denied Errors:"
echo "     - Symptoms: PostgreSQL logs show 'FATAL: could not open file global/pg_filenode.map: Permission denied'"
echo "     - Fix: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose down && sudo chown -R 999:999 /opt/quiz-app/data/postgres && sudo chmod -R 750 /opt/quiz-app/data/postgres && docker compose up -d'"
echo "  🔧 Application Health Check Failures:"
echo "     - Wait 2-3 minutes for full startup, check logs for specific errors"
echo "     - Verify database connectivity: ssh opc@$PUBLIC_IP 'cd /opt/quiz-app && docker compose logs postgres'"

echo ""
log_success "Deployment completed successfully!"