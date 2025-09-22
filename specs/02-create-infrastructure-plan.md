# Infrastructure Creation Plan - OCI Compute Instance

## Implementation Script

**Linked Script**: `oci-deployment/scripts/02-create-infrastructure.sh`

This plan is implemented by the above script to create the OCI compute instance.

## Overview

Create an OCI compute instance optimized for the quiz application deployment using the Always Free tier resources with the Load Balancer subnet that supports public IP assignment.

## Current Status

**✅ Prerequisites Complete:**
- OCI CLI authenticated and configured
- Network infrastructure verified (VCN, subnet, Internet Gateway, routing)
- Subnet supports public IP assignment (`oke-svclbsubnet` - 10.0.20.0/24)
- SSH key pair generated
- Free tier resources available (3/6 instances used)

## Infrastructure Requirements

### Compute Instance Specifications
- **Shape**: `VM.Standard.A1.Flex` (ARM-based Ampere A1)
- **OCPUs**: 2 (within 4 OCPU free tier limit)
- **Memory**: 4 GB (within 24 GB free tier limit)
- **Operating System**: Oracle Linux 8 (Always Free)
- **Boot Volume**: 47 GB (default, within free tier)
- **Public IP**: Yes (using Load Balancer subnet)
- **SSH Access**: Enabled with generated key pair

### Network Configuration
- **VCN**: Existing VCN (verified)
- **Subnet**: Load Balancer subnet (`oke-svclbsubnet-quick-shubham-quiz-website-a17e84f34-regional`)
- **CIDR**: `10.0.20.0/24`
- **Public IP**: Auto-assigned
- **Security List**: Inherit from subnet (OKE-configured)

### Software Installation (via Cloud-Init)
- Docker and Docker Compose
- Git, curl, wget, vim, htop
- OCI CLI (for container registry access)
- Firewall configuration (ports 22, 80, 443, 8080)
- Application directory structure (`/opt/quiz-app`)

## Implementation Strategy

### Phase 1: Instance Creation
1. **Prepare Cloud-Init Configuration**
   - Use existing `oci-deployment/configs/cloud-init.yaml`
   - Includes all required software and configurations
   - Sets up application directories with proper permissions

2. **Create Compute Instance**
   - Use OCI CLI to launch instance
   - Apply cloud-init configuration
   - Assign public IP from Load Balancer subnet
   - Configure SSH access

3. **Verify Instance Readiness**
   - Wait for instance to reach RUNNING state
   - Verify SSH connectivity
   - Confirm cloud-init completion
   - Test Docker installation

### Phase 2: Security Configuration
1. **Firewall Rules Verification**
   - Ensure ports 22 (SSH), 8080 (app), 80/443 (HTTP/HTTPS) are open
   - Verify security list inheritance from OKE setup
   - Apply any additional security configurations

2. **SSH Access Setup**
   - Test SSH connection with generated key pair
   - Verify user permissions (opc user in docker group)
   - Confirm sudo access for administrative tasks

### Phase 3: Application Environment Preparation
1. **Docker Registry Authentication**
   - Configure OCIR authentication on instance
   - Test ability to pull/push images
   - Verify connectivity to Oracle Container Registry

2. **Application Directory Structure**
   - Verify `/opt/quiz-app` directory creation
   - Confirm proper permissions for application deployment
   - Test volume mounts for PostgreSQL data persistence

## Resource Allocation Strategy

### Free Tier Optimization
```
Current Usage: 3/6 instances, 2 block volumes
After Creation: 4/6 instances, 2 block volumes
Remaining: 2 instances available for future use

ARM A1 Resources:
- Using: 2 OCPUs (out of 4 available)
- Using: 4 GB RAM (out of 24 GB available)
- Remaining: 2 OCPUs, 20 GB RAM for scaling
```

### Cost Verification
- **Compute**: $0 (Always Free tier)
- **Storage**: $0 (Boot volume within free limit)
- **Networking**: $0 (VCN and public IP within free limits)
- **Total Expected Cost**: $0.00

## Technical Specifications

### Instance Launch Command
```bash
oci compute instance launch \
  --compartment-id $COMPARTMENT_ID \
  --availability-domain "VHFQ:AP-MUMBAI-1-AD-1" \
  --display-name "quiz-app-server" \
  --image-id <oracle-linux-8-image-id> \
  --shape "VM.Standard.A1.Flex" \
  --shape-config '{"ocpus": 2, "memory_in_gbs": 4}' \
  --subnet-id $SUBNET_ID \
  --assign-public-ip true \
  --ssh-authorized-keys-file ~/.ssh/id_rsa.pub \
  --user-data-file oci-deployment/configs/cloud-init.yaml \
  --wait-for-state RUNNING
```

### Cloud-Init Integration
- **Configuration File**: `oci-deployment/configs/cloud-init.yaml`
- **Software Installation**: Docker, Git, development tools
- **Directory Setup**: Application directories with proper permissions
- **Security**: Firewall configuration and user setup
- **Monitoring**: Basic system monitoring scripts

## Security Considerations

### Network Security
- **Public IP**: Necessary for application access and deployment
- **SSH Access**: Restricted to specific key pair
- **Firewall**: Configured for required ports only
- **Security Lists**: Inherit secure OKE configuration

### Instance Security
- **Non-root User**: Primary operations as `opc` user
- **Docker Security**: User added to docker group for container management
- **System Updates**: Automatic security updates configured
- **Log Management**: Log rotation configured to prevent disk usage issues

## Validation Tests

### Post-Creation Verification
1. **Instance Status Check**
   ```bash
   oci compute instance get --instance-id <instance-id> \
     --query "data.{state:\"lifecycle-state\",name:\"display-name\"}"
   ```

2. **Network Connectivity Test**
   ```bash
   # Test SSH connectivity
   ssh -i ~/.ssh/id_rsa opc@<public-ip>

   # Test internet connectivity from instance
   ssh -i ~/.ssh/id_rsa opc@<public-ip> "curl -I google.com"
   ```

3. **Software Installation Verification**
   ```bash
   # Verify Docker installation
   ssh -i ~/.ssh/id_rsa opc@<public-ip> "docker --version"

   # Verify cloud-init completion
   ssh -i ~/.ssh/id_rsa opc@<public-ip> "cat /opt/quiz-app/.cloud-init-complete"
   ```

4. **Application Directory Structure**
   ```bash
   # Verify directory structure
   ssh -i ~/.ssh/id_rsa opc@<public-ip> "ls -la /opt/quiz-app/"

   # Test permissions
   ssh -i ~/.ssh/id_rsa ocp@<public-ip> "touch /opt/quiz-app/test-file"
   ```

## Risk Assessment

### Low Risk ✅
- **Free Tier Usage**: Well within limits
- **Existing Network**: Using proven OKE infrastructure
- **Standard Configuration**: Oracle Linux with standard software stack

### Medium Risk ⚠️
- **Public IP Exposure**: Increased attack surface (mitigated by security lists)
- **Resource Limits**: ARM architecture may have software compatibility considerations
- **Single Instance**: No redundancy (acceptable for development/personal project)

### Mitigation Strategies
- **Security**: Leverage existing OKE security configurations
- **Monitoring**: Implement basic system monitoring
- **Backup Strategy**: Focus on application data and configuration backup
- **Recovery Plan**: Document instance recreation process

## Success Criteria

### Primary Objectives
- [ ] Compute instance created successfully in RUNNING state
- [ ] Public IP assigned and accessible
- [ ] SSH connectivity established
- [ ] Cloud-init completed without errors
- [ ] Docker and required software installed
- [ ] Application directories created with proper permissions

### Network Validation
- [ ] Instance can access internet (for Docker pulls, updates)
- [ ] SSH access working from development machine
- [ ] Security lists allow required application ports
- [ ] OCIR connectivity established for Docker registry access

### Application Readiness
- [ ] `/opt/quiz-app` directory structure complete
- [ ] PostgreSQL data directory prepared
- [ ] Docker daemon running and accessible
- [ ] System monitoring scripts operational

## Next Steps After Completion

1. **Update Deployment Checklist**: Mark infrastructure creation as complete
2. **Proceed to Application Deployment**: Run `./03-deploy-application.sh`
3. **Source Code Transfer**: Copy application source to instance
4. **Docker Build**: Build application image on instance
5. **Container Deployment**: Deploy PostgreSQL and application containers

## Timeline Estimation

- **Instance Creation**: 5-10 minutes
- **Cloud-Init Execution**: 10-15 minutes
- **Verification Tests**: 5 minutes
- **Total Duration**: 20-30 minutes

## Implementation Files

### Required Files
- `oci-deployment/scripts/02-create-infrastructure.sh` - Main creation script
- `oci-deployment/configs/cloud-init.yaml` - Instance initialization ✅ (exists)
- `.env` - Environment variables ✅ (configured)
- `~/.ssh/id_rsa.pub` - SSH public key ✅ (generated)

### Documentation Updates
- Update deployment checklist with infrastructure status
- Record instance details for future reference
- Document public IP and connection details