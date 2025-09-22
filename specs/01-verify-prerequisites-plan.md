# OCI Credentials and Prerequisites Verification Plan

## Implementation Script

**Linked Script**: `oci-deployment/scripts/01-verify-prerequisites.sh`

This plan is implemented by the above script to verify all prerequisites before deployment.

## Overview

This plan ensures all Oracle Cloud Infrastructure (OCI) credentials, tools, and access permissions are properly configured before beginning the quiz application deployment.

## Current Configuration Review

### Environment Variables from .env
The `.env` file contains the following OCI configuration structure (actual values are kept secure):

```bash
# OCI Infrastructure
COMPARTMENT_ID=ocid1.tenancy.oc1..<your-tenancy-ocid>
VCN_ID=ocid1.vcn.oc1.ap-mumbai-1.<your-vcn-ocid>
SUBNET_ID=ocid1.subnet.oc1.ap-mumbai-1.<your-subnet-ocid>
LB_SUBNET_ID=ocid1.subnet.oc1.ap-mumbai-1.<your-lb-subnet-ocid>

# OCIR Configuration
OCIR_REGION=ap-mumbai-1
OCIR_NAMESPACE=<your-namespace>
OCIR_USERNAME=<your-namespace>/<your-email>
OCIR_AUTH_TOKEN=<your-auth-token>
DOCKER_IMAGE=ap-mumbai-1.ocir.io/<your-namespace>/quiz-app:latest
```

**Note**: All sensitive values are stored securely in the `.env` file and not exposed in documentation.

## Verification Checklist

### 1. OCI CLI Installation and Configuration

#### 1.1 Check OCI CLI Installation
```bash
# Verify OCI CLI is installed
oci --version
# Expected output: Oracle Cloud Infrastructure CLI version X.X.X

# If not installed, install using:
# macOS (recommended)
brew install oci-cli

# Alternative installation methods:
# Linux/macOS script
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"

# Or using pip
pip install oci-cli
```

#### 1.2 Verify OCI CLI Configuration
```bash
# Check if OCI config exists
ls -la ~/.oci/
# Should show: config, oci_api_key.pem (or similar key files)

# Display current OCI configuration
oci iam region list --config-file ~/.oci/config
# Should list available regions including ap-mumbai-1

# Test basic CLI functionality
oci iam availability-domain list --compartment-id $COMPARTMENT_ID
```

#### 1.3 Configuration File Structure
Verify `~/.oci/config` contains proper configuration:
```ini
[DEFAULT]
user=ocid1.user.oc1..<your-user-ocid>
fingerprint=<your-key-fingerprint>
tenancy=ocid1.tenancy.oc1..<your-tenancy-ocid>
region=ap-mumbai-1
key_file=~/.oci/oci_api_key.pem
```

**Note**: Replace placeholder values with your actual OCI credentials from the console.

### 2. Authentication Verification

#### 2.1 Test API Key Authentication
```bash
# Test authentication with identity service
oci iam user get --user-id $(oci iam user list --query "data[0].id" --raw-output)

# Test compartment access
oci iam compartment get --compartment-id $COMPARTMENT_ID
```

#### 2.2 Verify User Permissions
```bash
# Check user groups and policies
oci iam user list-groups --user-id $(oci iam user list --query "data[0].id" --raw-output)

# Test if user can access compute service
oci compute shape list --compartment-id $COMPARTMENT_ID --region ap-mumbai-1
```

### 3. Network Infrastructure Validation

#### 3.1 Verify VCN Configuration
```bash
# Validate VCN exists and is accessible
oci network vcn get --vcn-id $VCN_ID

# Check VCN details
oci network vcn list --compartment-id $COMPARTMENT_ID --display-name "*" --query "data[?id=='$VCN_ID']"
```

#### 3.2 Verify Subnet Configuration
```bash
# Validate compute subnet
oci network subnet get --subnet-id $SUBNET_ID

# Check if subnet allows public IPs (required for compute instance)
oci network subnet get --subnet-id $SUBNET_ID --query "data.{name:\"display-name\",cidr:\"cidr-block\",public:\"prohibit-public-ip-on-vnic\"}"

# Validate load balancer subnet (if needed)
oci network subnet get --subnet-id $LB_SUBNET_ID
```

#### 3.3 Security List and Route Table Verification
```bash
# Check security lists for the subnet
oci network security-list list --compartment-id $COMPARTMENT_ID --vcn-id $VCN_ID

# Verify route table configuration
oci network route-table list --compartment-id $COMPARTMENT_ID --vcn-id $VCN_ID
```

### 4. Compute Service Access Verification

#### 4.1 Test Compute Instance Creation Permissions
```bash
# List available compute shapes (free tier eligible)
oci compute shape list --compartment-id $COMPARTMENT_ID --region ap-mumbai-1 \
  --query "data[?contains(shape,'Standard.E2.1.Micro') || contains(shape,'Standard.A1.Flex')]"

# Check availability domains
oci iam availability-domain list --compartment-id $COMPARTMENT_ID

# List available images (Oracle Linux)
oci compute image list --compartment-id $COMPARTMENT_ID --operating-system "Oracle Linux" \
  --query "data[0:3].{name:\"display-name\",id:id}"
```

#### 4.2 SSH Key Preparation
```bash
# Verify SSH key exists or create one
if [ ! -f ~/.ssh/id_rsa ]; then
  ssh-keygen -t rsa -b 4096 -C "quiz-app-deployment"
fi

# Display public key for compute instance
cat ~/.ssh/id_rsa.pub
```

### 5. OCIR (Oracle Container Registry) Access Verification

#### 5.1 Docker Installation Check
```bash
# Verify Docker is installed and running
docker --version
docker info

# Start Docker if not running
# macOS: Start Docker Desktop
# Linux: sudo systemctl start docker
```

#### 5.2 OCIR Authentication Test
```bash
# Load environment variables
source .env

# Test OCIR login
echo $OCIR_AUTH_TOKEN | docker login ${OCIR_REGION}.ocir.io -u $OCIR_USERNAME --password-stdin

# Expected output: Login Succeeded
```

#### 5.3 OCIR Repository Access
```bash
# Test repository access by trying to pull a basic image
docker pull ${OCIR_REGION}.ocir.io/${OCIR_NAMESPACE}/hello-world:latest || echo "Repository access test (expected to fail if no images exist)"

# List repositories in OCIR (if accessible)
oci artifacts container repository list --compartment-id $COMPARTMENT_ID
```

#### 5.4 Test Image Push Capabilities
```bash
# Create a simple test image
echo "FROM alpine:latest" > Dockerfile.test
echo "RUN echo 'OCI test successful'" >> Dockerfile.test

# Build test image
docker build -t oci-test:latest -f Dockerfile.test .

# Tag for OCIR
docker tag oci-test:latest ${OCIR_REGION}.ocir.io/${OCIR_NAMESPACE}/oci-test:latest

# Test push to OCIR
docker push ${OCIR_REGION}.ocir.io/${OCIR_NAMESPACE}/oci-test:latest

# Clean up test image
docker rmi oci-test:latest ${OCIR_REGION}.ocir.io/${OCIR_NAMESPACE}/oci-test:latest
rm Dockerfile.test
```

### 6. Required Tools Installation

#### 6.1 Essential Tools Checklist
```bash
# Git (for repository operations)
git --version

# curl (for API testing)
curl --version

# jq (for JSON processing)
jq --version

# Docker Compose (for container orchestration)
docker-compose --version

# Install missing tools:
# macOS: brew install git curl jq docker-compose
# Ubuntu/Debian: sudo apt-get install git curl jq docker-compose
# CentOS/RHEL: sudo yum install git curl jq docker-compose
```

### 7. Free Tier Limits Verification

#### 7.1 Check Current Resource Usage
```bash
# List existing compute instances
oci compute instance list --compartment-id $COMPARTMENT_ID --lifecycle-state RUNNING

# Check block volume usage
oci bv volume list --compartment-id $COMPARTMENT_ID

# Verify free tier eligibility
oci limits quota list --compartment-id $COMPARTMENT_ID --service-name compute
```

#### 7.2 Free Tier Resource Limits
```
Always Free Compute:
- 2x VM.Standard.E2.1.Micro instances (1/8 OCPU, 1GB RAM each)
- 4x ARM-based Ampere A1 cores (can be split across instances)
- 24GB ARM-based memory (can be distributed)

Always Free Storage:
- 200GB total Block Volume storage
- 10GB Object Storage

Always Free Networking:
- 1 VCN with associated resources
- 1 Load Balancer (10 Mbps)
```

## Pre-Deployment Validation Script

### Complete Verification Script
```bash
#!/bin/bash
# oci-verification.sh

set -e
source .env

echo "=== OCI Prerequisites Verification ==="

# 1. OCI CLI
echo "1. Checking OCI CLI..."
oci --version || { echo "ERROR: OCI CLI not installed"; exit 1; }

# 2. Authentication
echo "2. Testing OCI Authentication..."
oci iam compartment get --compartment-id $COMPARTMENT_ID > /dev/null || { echo "ERROR: Authentication failed"; exit 1; }

# 3. Network Resources
echo "3. Verifying Network Resources..."
oci network vcn get --vcn-id $VCN_ID > /dev/null || { echo "ERROR: VCN not accessible"; exit 1; }
oci network subnet get --subnet-id $SUBNET_ID > /dev/null || { echo "ERROR: Subnet not accessible"; exit 1; }

# 4. Compute Access
echo "4. Testing Compute Service Access..."
oci compute shape list --compartment-id $COMPARTMENT_ID --region ap-mumbai-1 > /dev/null || { echo "ERROR: Compute service not accessible"; exit 1; }

# 5. OCIR Access
echo "5. Testing OCIR Access..."
echo $OCIR_AUTH_TOKEN | docker login ${OCIR_REGION}.ocir.io -u $OCIR_USERNAME --password-stdin || { echo "ERROR: OCIR authentication failed"; exit 1; }

# 6. SSH Key
echo "6. Checking SSH Key..."
[ -f ~/.ssh/id_rsa.pub ] || { echo "ERROR: SSH public key not found"; exit 1; }

echo "✅ All prerequisites verified successfully!"
echo "Ready for OCI deployment."
```

## Troubleshooting Common Issues

### Issue 1: OCI CLI Configuration Problems
```bash
# Fix configuration
oci setup config

# Or manually create config file
mkdir -p ~/.oci
# Follow OCI documentation for API key setup
```

### Issue 2: Authentication Failures
```bash
# Regenerate API key
# Go to OCI Console -> Profile -> API Keys -> Add API Key
# Update ~/.oci/config with new fingerprint

# Test new configuration
oci iam region list
```

### Issue 3: OCIR Authentication Issues
```bash
# Regenerate Auth Token
# Go to OCI Console -> Profile -> Auth Tokens -> Generate Token
# Update .env file with new token

# Test authentication
docker logout ${OCIR_REGION}.ocir.io
echo $OCIR_AUTH_TOKEN | docker login ${OCIR_REGION}.ocir.io -u $OCIR_USERNAME --password-stdin
```

### Issue 4: Network Access Problems
```bash
# Check security lists allow required ports
oci network security-list get --security-list-id <security-list-id>

# Verify route table has internet gateway
oci network route-table get --rt-id <route-table-id>
```

## Success Criteria

- [ ] OCI CLI installed and configured correctly
- [ ] API key authentication working
- [ ] User has necessary permissions for Compute and OCIR
- [ ] VCN and subnet accessible and properly configured
- [ ] Docker installed and OCIR authentication successful
- [ ] SSH key pair generated and accessible
- [ ] All required tools installed (git, curl, jq, docker-compose)
- [ ] Free tier limits verified and within bounds
- [ ] Complete verification script runs without errors

## Next Steps

After successful verification:
1. Proceed with Docker image build and push to OCIR
2. Create compute instance using the verified network configuration
3. Deploy application using the validated credentials and tools

This verification ensures a smooth deployment process without authentication or permission issues.