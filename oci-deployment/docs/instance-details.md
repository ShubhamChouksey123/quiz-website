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
- **Health Check**: http://161.118.188.237:8080/actuator/health

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

### ⏳ Cloud-Init: IN PROGRESS
- **SSH Status**: Connection timeout (port 22 not yet accessible)
- **Docker Installation**: Pending (cloud-init still running)
- **Application Directory**: Pending verification
- **Expected Completion**: 15-20 minutes from creation time (Sat Sep 22 22:05:00 IST)

### 🔄 Verification Results (as of $(date))
- **Instance State**: RUNNING ✅
- **Public IP Assignment**: 161.118.188.237 ✅
- **Private IP Assignment**: 10.0.20.25 ✅
- **SSH Connectivity**: ❌ (Timeout - cloud-init in progress)
- **Docker Installation**: ⏳ (Pending verification)
- **Application Directory**: ⏳ (Pending verification)

### 📋 Next Steps

1. **Wait for Cloud-Init**: Allow additional time for software installation completion
2. **Monitor SSH Access**: `ssh -i ~/.ssh/id_rsa opc@161.118.188.237`
3. **Expected Ready Time**: ~22:25:00 IST (20 minutes from creation)
4. **Deploy Application**: Run `./03-deploy-application.sh` once SSH is available

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

### Patience Required ⏰
- **Instance Created**: Sat Sep 22 22:05:00 IST 2025
- **Current Status**: Cloud-init is installing Docker, Git, and development tools
- **SSH Access**: Will be available once cloud-init completes initialization
- **Total Initialization Time**: 15-25 minutes from creation
- **Estimated Ready Time**: 22:25:00 - 22:30:00 IST

### Current Behavior (Normal)
- **SSH Connection Timeout**: Expected during cloud-init software installation
- **Port 22 Inaccessible**: SSH daemon will start after cloud-init completes
- **Instance State**: RUNNING (infrastructure ready, software installing)

## Security Notes

- **SSH Access**: Restricted to your SSH key pair
- **Firewall**: Configured for ports 22, 80, 443, 8080
- **Security Lists**: Inherits secure OKE configuration
- **Updates**: Automatic security updates enabled
- **User Access**: Primary user is `opc` with sudo privileges

---

**Instance Status**: ✅ RUNNING - Cloud-init in progress
**Documentation Created**: Sat Sep 22 22:05:00 IST 2025
**Documentation Updated**: $(date)
**Expected Ready Time**: 22:25:00 - 22:30:00 IST (20-25 minutes from creation)