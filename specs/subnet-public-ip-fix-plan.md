# Subnet Public IP Fix - Specification Plan

## Overview

Resolve the subnet public IP assignment limitation to enable compute instance creation with public internet access for the quiz application deployment.

## Current Issue

**Problem**: The current subnet prohibits public IP assignment on VNICs (Virtual Network Interface Cards)
- **Subnet ID**: `SUBNET_ID` from .env file
- **Current Setting**: `prohibit-public-ip-on-vnic: true`
- **Impact**: Cannot create compute instances with public IP access
- **Requirement**: Need public IP for SSH access and web application accessibility

## Solution Strategy

**Option Selected**: Modify Current Subnet (Option 2)
- **Approach**: Update existing subnet configuration to allow public IP assignment
- **Benefits**: No additional resources, no cost increase, minimal infrastructure changes
- **Risk Level**: Low - configuration change only

## Technical Requirements

### 1. Subnet Configuration Update
```bash
# Target Configuration Change
Current: prohibit-public-ip-on-vnic = true
Target:  prohibit-public-ip-on-vnic = false
```

### 2. Prerequisites for Modification
- **OCI CLI Access**: Already verified ✅
- **Network Admin Permissions**: Required for subnet modification
- **VCN Access**: Already verified ✅
- **Subnet Write Permissions**: Need to verify

### 3. Security Considerations
- **Security Lists**: Ensure appropriate ingress/egress rules
- **Route Tables**: Verify internet gateway routing exists
- **Network Security**: Maintain security while enabling public access

## Implementation Steps

### Phase 1: Pre-Modification Verification
1. **Verify Current Subnet State**
   ```bash
   oci network subnet get --subnet-id $SUBNET_ID \
     --query "data.{name:\"display-name\",prohibit:\"prohibit-public-ip-on-vnic\",cidr:\"cidr-block\"}"
   ```

2. **Check Security Lists**
   ```bash
   oci network subnet get --subnet-id $SUBNET_ID \
     --query "data.\"security-list-ids\"[*]" --output table
   ```

3. **Verify Route Table Configuration**
   ```bash
   oci network subnet get --subnet-id $SUBNET_ID \
     --query "data.\"route-table-id\"" --output table
   ```

4. **Check for Internet Gateway**
   ```bash
   oci network internet-gateway list --compartment-id $COMPARTMENT_ID --vcn-id $VCN_ID
   ```

### Phase 2: Subnet Modification
1. **Update Subnet Configuration**
   ```bash
   oci network subnet update \
     --subnet-id $SUBNET_ID \
     --prohibit-public-ip-on-vnic false \
     --wait-for-state AVAILABLE
   ```

2. **Verify Modification Success**
   ```bash
   oci network subnet get --subnet-id $SUBNET_ID \
     --query "data.{name:\"display-name\",prohibit:\"prohibit-public-ip-on-vnic\",state:\"lifecycle-state\"}"
   ```

### Phase 3: Security Configuration Validation
1. **Ensure Security List Allows Required Ports**
   - SSH (Port 22): For remote access
   - HTTP (Port 80): For web traffic (future)
   - HTTPS (Port 443): For secure web traffic (future)
   - Application (Port 8080): For quiz application

2. **Verify Route Table Has Internet Access**
   - Confirm route to 0.0.0.0/0 via Internet Gateway
   - Ensure proper routing for public internet access

### Phase 4: Test Public IP Assignment
1. **Test Instance Creation (Dry Run)**
   ```bash
   # Test if public IP can now be assigned (without actually creating instance)
   oci compute instance launch --dry-run \
     --compartment-id $COMPARTMENT_ID \
     --subnet-id $SUBNET_ID \
     --assign-public-ip true \
     # ... other parameters
   ```

## Security Requirements

### Required Security List Rules
```bash
# Ingress Rules (Inbound Traffic)
- SSH: Protocol TCP, Port 22, Source: YOUR_IP/32 (restricted access)
- HTTP: Protocol TCP, Port 80, Source: 0.0.0.0/0 (web access)
- HTTPS: Protocol TCP, Port 443, Source: 0.0.0.0/0 (secure web access)
- Application: Protocol TCP, Port 8080, Source: 0.0.0.0/0 (quiz app)

# Egress Rules (Outbound Traffic)
- All Traffic: Protocol All, Destination: 0.0.0.0/0 (for updates, OCIR access)
```

### Route Table Requirements
```bash
# Required Routes
- Destination: 0.0.0.0/0, Target: Internet Gateway (for public internet access)
- Local VCN traffic: Handled automatically
```

## Risk Assessment

### Low Risk Items ✅
- **Configuration Change**: Simple boolean flag modification
- **Reversible**: Can be changed back if needed
- **No Resource Creation**: No additional costs
- **Existing Infrastructure**: Uses current VCN setup

### Medium Risk Items ⚠️
- **Security Exposure**: Public IPs increase attack surface
- **Network Changes**: Could affect existing resources in subnet
- **Access Control**: Need proper security list configuration

### Mitigation Strategies
- **Restricted SSH Access**: Limit SSH to specific IP addresses
- **Firewall Rules**: Implement proper security list rules
- **Monitoring**: Monitor for unusual network activity
- **Backup Plan**: Document rollback procedure

## Cost Impact

**Expected Cost**: $0
- **No Additional Resources**: Modifying existing subnet configuration
- **No Instance Creation**: Just enabling capability
- **Free Tier Compatible**: Works within OCI Always Free limits

## Rollback Plan

### If Issues Arise
1. **Revert Subnet Configuration**
   ```bash
   oci network subnet update \
     --subnet-id $SUBNET_ID \
     --prohibit-public-ip-on-vnic true
   ```

2. **Alternative Approaches**
   - Use NAT Gateway for outbound-only access
   - Create bastion host in different subnet
   - Use OCI Cloud Shell for management

## Success Criteria

### Primary Goals
- [ ] Subnet allows public IP assignment (`prohibit-public-ip-on-vnic: false`)
- [ ] Security lists properly configured for required ports
- [ ] Route table includes internet gateway route
- [ ] Test instance creation succeeds with public IP

### Verification Tests
- [ ] `oci network subnet get` shows `prohibit-public-ip-on-vnic: false`
- [ ] Security list inspection shows required ports open
- [ ] Route table shows 0.0.0.0/0 -> Internet Gateway route
- [ ] Dry-run instance creation with public IP succeeds

## Next Steps After Completion

1. **Re-run Prerequisites Verification**: Execute `./01-verify-prerequisites.sh` to confirm the subnet public IP issue is resolved
2. **Update Infrastructure Creation Script**: Modify `02-create-infrastructure.sh` to use public IP
3. **Update Deployment Checklist**: Mark subnet issue as resolved
4. **Proceed with Instance Creation**: Run infrastructure creation script
5. **Validate Network Connectivity**: Test SSH and application access

## Implementation Timeline

1. **Phase 1**: Pre-modification verification (5 minutes)
2. **Phase 2**: Subnet modification (2 minutes)
3. **Phase 3**: Security validation (5 minutes)
4. **Phase 4**: Testing (3 minutes)

**Total Estimated Time**: 15 minutes

## Documentation Updates Required

After successful completion:
- Update `deployment-checklist.md` to mark subnet issue resolved
- Update `oci-credentials-verification-plan.md` with new subnet status
- Document security configuration in infrastructure creation plan