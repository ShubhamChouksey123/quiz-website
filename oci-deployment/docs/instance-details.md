# OCI Compute Instance Details

**Created**: Sat Sep 22 22:05:00 IST 2025
**Status**: ✅ RUNNING

## Instance Information 

| Property | Value |
|----------|-------|
| **Instance ID** | `ocid1.instance.oc1.ap-mumbai-1.anrg6ljrsob7awicfu6ixptydvlb3fh2njsdfzazfqptq4bx2hdy3euq5l3a` |
| **Instance Name** | quiz-app-server |
| **Public IP** | **161.118.188.237** |
| **Private IP** | 10.0.20.25 |
| **Shape** | VM.Standard.A1.Flex |
| **OCPUs** | 2 |
| **Memory** | 4GB |
| **Region** | ap-mumbai-1 |
| **Availability Domain** | rWnQ:AP-MUMBAI-1-AD-1 |
| **Operating System** | Oracle Linux 8 |
| **Image ID** | `ocid1.image.oc1.ap-mumbai-1.aaaaaaaa7s3kysup4osdkdnqucmdx4p54ydo2mrtbue7hrs36a3cwilhdygq` |
| **Subnet ID** | Load Balancer subnet (allows public IP) |

## Network Configuration

- **VCN**: Load Balancer subnet (allows public IP)
- **CIDR**: 10.0.20.0/24
- **Public IP**: 161.118.188.237
- **Private IP**: 10.0.20.25
- **SSH Access**: Port 22
- **Application Access**: Port 8080
- **Security Lists**: Inherited from OKE configuration

## Access Information

### SSH Connection
```bash
ssh -i ~/.ssh/id_rsa opc@161.118.188.237
```

### Application URLs (after deployment)
- **Quiz Application**: http://161.118.188.237:8080
- **Health Check**: http://161.118.188.237:8080/about

## Instance Management Commands

### View Instance Status
```bash
oci compute instance get --instance-id ocid1.instance.oc1.ap-mumbai-1.anrg6ljrsob7awicfu6ixptydvlb3fh2njsdfzazfqptq4bx2hdy3euq5l3a \
  --query "data.{state:\"lifecycle-state\",name:\"display-name\"}"
```

### Stop Instance
```bash
oci compute instance action --instance-id ocid1.instance.oc1.ap-mumbai-1.anrg6ljrsob7awicfu6ixptydvlb3fh2njsdfzazfqptq4bx2hdy3euq5l3a --action STOP --wait-for-state STOPPED
```

### Start Instance
```bash
oci compute instance action --instance-id ocid1.instance.oc1.ap-mumbai-1.anrg6ljrsob7awicfu6ixptydvlb3fh2njsdfzazfqptq4bx2hdy3euq5l3a --action START --wait-for-state RUNNING
```

### Terminate Instance (⚠️ Destructive)
```bash
oci compute instance terminate --instance-id ocid1.instance.oc1.ap-mumbai-1.anrg6ljrsob7awicfu6ixptydvlb3fh2njsdfzazfqptq4bx2hdy3euq5l3a --wait-for-state TERMINATED
```

## Software Configuration

### Installed Software (via Cloud-Init)
- **Docker**: Container runtime
- **Docker Compose**: Container orchestration
- **Git**: Version control
- **curl, wget**: Network utilities
- **vim, htop**: System utilities
- **OCI CLI**: Oracle Cloud Infrastructure CLI

**Note**: Cloud-init installation may take 10-15 minutes after instance creation.

### Application Directory Structure
- **Base Directory**: `/opt/quiz-app/`
- **Data Directory**: `/opt/quiz-app/data/postgres`
- **Logs Directory**: `/opt/quiz-app/logs`
- **Scripts Directory**: `/opt/quiz-app/scripts`

## Resource Usage (Free Tier)

### Current Allocation
- **Compute Instances**: 4/6 used (after this instance)
- **ARM OCPUs**: 2/4 used
- **ARM Memory**: 4GB/24GB used
- **Block Volumes**: 2 used
- **Public IPs**: 1 used

### Cost Verification
- **Compute Cost**: $0.00 (Always Free tier)
- **Storage Cost**: $0.00 (Within free limits)
- **Network Cost**: $0.00 (Within free limits)
- **Total Monthly Cost**: $0.00

## Current Status

### ✅ Instance Creation: COMPLETE
- Instance successfully created and running
- Public IP assigned: 161.118.188.237
- Private IP assigned: 10.0.20.25
- Network interfaces: AVAILABLE

### ✅ Infrastructure Verification: COMPLETE
- **SSH Status**: WORKING ✅ (Security rules added, connection established)
- **Docker Installation**: COMPLETE ✅ (v26.1.3 with Compose v2.27.0)
- **Application Directory**: COMPLETE ✅ (Full directory structure created)
- **Network Connectivity**: COMPLETE ✅ (Internet access working)
- **Software Installation**: COMPLETE ✅ (All required tools installed)

### 🔄 Verification Results (Updated: Mon Sep 22 18:15:00 GMT 2025)
- **Instance State**: RUNNING ✅
- **Public IP Assignment**: 161.118.188.237 ✅
- **Private IP Assignment**: 10.0.20.25 ✅
- **SSH Connectivity**: ✅ WORKING (`ssh -i ~/.ssh/id_rsa opc@161.118.188.237`)
- **Docker Installation**: ✅ COMPLETE (Docker 26.1.3, Docker Compose v2.27.0)
- **Application Directory**: ✅ COMPLETE (`/opt/quiz-app/` with proper permissions)
- **Network Security**: ✅ CONFIGURED (Ingress/egress rules added)
- **Software Stack**: ✅ COMPLETE (Git 2.43.7, Curl, Vim installed)

### 🔧 Issues Resolved During Verification

#### **1. SSH Connection Timeout**
- **Issue**: SSH connection timed out, port 22 not accessible
- **Root Cause**: Security list had no ingress rules configured
- **Resolution**: Added comprehensive ingress rules for SSH (22), HTTP (80), HTTPS (443), and application (8080)
- **Status**: ✅ RESOLVED

#### **2. Cloud-Init Package Installation Failure**
- **Issue**: Cloud-init failed to install Docker and development tools
- **Root Cause**: Security list had no egress rules, preventing outbound internet access
- **Resolution**: Added egress rules for all outbound traffic (0.0.0.0/0)
- **Status**: ✅ RESOLVED

#### **3. Docker Installation Failure**
- **Issue**: Docker not installed due to cloud-init package failures
- **Root Cause**: Network connectivity issues during cloud-init
- **Resolution**: Manual installation of Docker CE after network fix
- **Status**: ✅ RESOLVED

### 📋 Next Steps

1. **Infrastructure Ready**: ✅ All verification steps complete
2. **SSH Access Available**: `ssh -i ~/.ssh/id_rsa opc@161.118.188.237`
3. **Ready for Deployment**: Run `./03-deploy-application.sh`
4. **Application URL**: http://161.118.188.237:8080 (after deployment)

## Troubleshooting

### Common Issues
- **SSH Connection Refused**: Wait 2-3 minutes for instance initialization
- **SSH Connection Timeout**: Cloud-init may still be running (wait 10-15 minutes)
- **Cloud-Init In Progress**: Check `/opt/quiz-app/.cloud-init-complete`
- **Docker Not Available**: Cloud-init may still be installing software

### Diagnostic Commands
```bash
# Check instance status
oci compute instance get --instance-id ocid1.instance.oc1.ap-mumbai-1.anrg6ljrsob7awicfu6ixptydvlb3fh2njsdfzazfqptq4bx2hdy3euq5l3a

# Check cloud-init status (once SSH is available)
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 "sudo cloud-init status"

# Check system logs (once SSH is available)
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 "sudo journalctl -u cloud-init -f"
```

### Infrastructure Timeline ⏰
- **Instance Created**: Sat Sep 22 22:05:00 IST 2025 (16:48:03 UTC)
- **Initial Issues**: SSH timeout and cloud-init failures due to missing security rules
- **Network Fix Applied**: Mon Sep 22 18:06:00 GMT 2025 (Security rules added)
- **Verification Completed**: Mon Sep 22 18:15:00 GMT 2025
- **Total Setup Time**: ~1.5 hours (including troubleshooting and manual fixes)

### Current Behavior (Fully Functional)
- **SSH Access**: WORKING - Immediate connection available
- **Docker Service**: ACTIVE and ready for container operations
- **Network Connectivity**: Full internet access for package downloads and OCIR
- **Instance State**: RUNNING and fully operational

## Security Notes

- **SSH Access**: Restricted to your SSH key pair
- **Firewall**: Configured for ports 22, 80, 443, 8080
- **Security Lists**: Inherits secure OKE configuration
- **Updates**: Automatic security updates enabled
- **User Access**: Primary user is `opc` with sudo privileges

---

**Instance Status**: ✅ RUNNING - Fully operational and ready for deployment
**Documentation Created**: Sat Sep 22 22:05:00 IST 2025
**Documentation Updated**: Mon Sep 22 18:15:00 GMT 2025
**Infrastructure Ready**: ✅ All verification steps completed successfully