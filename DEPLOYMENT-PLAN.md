# Quiz Website - Deployment Plan

## 📋 **Pre-Deployment Verification Strategy**

### Phase 1: Environment Validation
**Objective**: Ensure all prerequisites are met before attempting deployment

#### 1.1 Credential Verification
```bash
# Run verification script
./scripts/verify-oci-access.sh
```

**What it validates**:
- ✅ OCI CLI installation and configuration
- ✅ Authentication with OCI APIs
- ✅ Access to required OCI services (Compute, OKE, PostgreSQL)
- ✅ Required tools (kubectl, docker, jq)
- ✅ Environment variables setup
- ✅ Docker Hub connectivity

#### 1.2 Manual Prerequisites Checklist
- [ ] OCI account with appropriate permissions
- [ ] Docker Hub account created
- [ ] Gmail app password generated (for SMTP)
- [ ] VCN and subnets created via OCI Console
- [ ] Required OCIDs collected

---

## 🚀 **Deployment Execution Plan**

### Phase 2: Infrastructure Setup
**Estimated Time**: 15-20 minutes

#### 2.1 Docker Image Preparation
```bash
# Option A: Build and deploy everything
./scripts/deploy.sh

# Option B: Build Docker image only (for testing)
./scripts/deploy.sh --docker-only
```

**Actions**:
1. Maven clean and package Spring Boot application
2. Build optimized Docker image with JRE 17
3. Tag image with latest and timestamp
4. Push to Docker Hub registry

#### 2.2 OCI PostgreSQL Database Creation
**Service**: OCI Database for PostgreSQL (Free Tier)
**Configuration**:
- **Compute**: 1 OCPU, 1GB RAM
- **Storage**: 20GB (expandable)
- **Networking**: Private subnet access
- **Security**: Database admin user with strong password

**Validation Steps**:
- Database status = AVAILABLE
- Network security rules configured
- Connection test from deployment subnet

#### 2.3 OKE Cluster Provisioning
**Service**: Oracle Container Engine for Kubernetes
**Configuration**:
- **Cluster Type**: Enhanced cluster
- **Kubernetes Version**: Latest stable
- **Node Pool**: 2 worker nodes (E2.1.Micro for free tier)
- **Networking**: Private worker nodes, public API endpoint

**Validation Steps**:
- Cluster status = ACTIVE
- Node pool status = ACTIVE
- kubectl configuration downloaded
- Basic connectivity test

---

### Phase 3: Application Deployment
**Estimated Time**: 10-15 minutes

#### 3.1 Kubernetes Resources Creation
**Deployment Order**:
1. **ConfigMap**: Database connection parameters
2. **Secret**: Sensitive credentials (DB password, Gmail credentials)
3. **Deployment**: Application pods with resource limits
4. **Service**: LoadBalancer for external access
5. **HPA**: Horizontal Pod Autoscaler for scaling

#### 3.2 Service Configuration
```yaml
# Key configurations applied
Resources:
  requests: 256Mi memory, 250m CPU
  limits: 512Mi memory, 500m CPU
Replicas: 2 (initial)
Auto-scaling: 2-10 pods based on CPU (70% threshold)
Health checks: Readiness and liveness probes
```

#### 3.3 Network Access Setup
- **LoadBalancer Service**: Exposes application externally
- **Security Groups**: Allow HTTP/HTTPS traffic
- **Database Security**: Private access from OKE subnet only

---

## 🔍 **Validation & Testing Plan**

### Phase 4: Post-Deployment Verification

#### 4.1 Infrastructure Health Checks
```bash
# Cluster status
kubectl cluster-info

# Application pods
kubectl get pods -l app=quiz-app

# Service external IP
kubectl get svc quiz-app-service

# Database connectivity test
kubectl exec -it <pod-name> -- nc -zv <db-host> 5432
```

#### 4.2 Application Functionality Tests
1. **Web Interface**: Access via LoadBalancer external IP
2. **Database Operations**: Create/read quiz data
3. **Email Service**: Test registration/password reset emails
4. **Auto-scaling**: Generate load to test HPA

#### 4.3 Security Validation
- [ ] Database accessible only from private subnet
- [ ] No hardcoded credentials in deployed application
- [ ] SSL/TLS configuration (if custom domain used)
- [ ] Resource limits enforced

---

## 📊 **Monitoring & Maintenance Plan**

### Phase 5: Operational Excellence

#### 5.1 Cost Monitoring
**Free Tier Resources**:
- PostgreSQL database (Always Free)
- OKE worker nodes (2x E2.1.Micro covered by free tier)

**Potential Costs**:
- LoadBalancer: ~$5-10/month
- Additional compute if scaling beyond free tier
- Egress data transfer charges

#### 5.2 Maintenance Tasks
- **Weekly**: Check application logs and performance
- **Monthly**: Review resource utilization and costs
- **Quarterly**: Update Docker images and Kubernetes versions

#### 5.3 Backup Strategy
- **Database**: OCI automatic backups (7-day retention)
- **Application**: Docker images stored in Docker Hub
- **Configuration**: Kubernetes manifests in Git repository

---

## 🛠️ **Troubleshooting Runbook**

### Common Issues & Solutions

#### Issue 1: OCI Authentication Fails
**Symptoms**: verify-oci-access.sh shows authentication errors
**Solutions**:
```bash
# Reconfigure OCI CLI
oci setup config

# Verify configuration
cat ~/.oci/config

# Test authentication
oci iam region list
```

#### Issue 2: Docker Build Fails
**Symptoms**: Maven build errors or Docker daemon issues
**Solutions**:
```bash
# Check Java version
java -version

# Verify Docker daemon
docker info

# Manual build test
cd app && mvn clean package
```

#### Issue 3: Database Connection Issues
**Symptoms**: Application pods can't connect to PostgreSQL
**Solutions**:
1. Verify security group rules allow port 5432
2. Check database status in OCI Console
3. Validate connection parameters in ConfigMap

#### Issue 4: LoadBalancer Not Getting External IP
**Symptoms**: Service shows <pending> for EXTERNAL-IP
**Solutions**:
1. Check load balancer subnet has internet gateway
2. Verify security lists allow traffic
3. Check OCI service limits

---

## 📈 **Success Metrics**

### Deployment Success Criteria
- [ ] All verification script checks pass
- [ ] PostgreSQL database status = AVAILABLE
- [ ] OKE cluster status = ACTIVE
- [ ] Application pods status = Running
- [ ] LoadBalancer has external IP assigned
- [ ] Web application accessible via browser
- [ ] Database connectivity confirmed
- [ ] Email functionality working

### Performance Targets
- **Application Startup**: < 2 minutes
- **Response Time**: < 500ms for quiz pages
- **Availability**: > 99.5% uptime
- **Auto-scaling**: Triggers within 30 seconds of load increase

---

## 🎯 **Next Steps After Deployment**

### Immediate (Day 1)
1. Test all application features thoroughly
2. Configure monitoring and alerting
3. Document application URLs and credentials

### Short-term (Week 1)
1. Set up custom domain (optional)
2. Configure SSL certificates
3. Implement CI/CD pipeline

### Medium-term (Month 1)
1. Set up comprehensive monitoring (Prometheus/Grafana)
2. Implement automated backups
3. Security audit and hardening

### Long-term (Quarter 1)
1. Performance optimization based on usage patterns
2. Cost optimization review
3. Disaster recovery planning

---

## 📞 **Support Resources**

### Documentation Links
- [OCI CLI Configuration](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/cliinstall.htm)
- [OKE User Guide](https://docs.oracle.com/en-us/iaas/Content/ContEng/home.htm)
- [PostgreSQL on OCI](https://docs.oracle.com/en-us/iaas/postgresql/index.html)

### Emergency Contacts
- **OCI Support**: Available through OCI Console
- **Application Issues**: Check logs with `kubectl logs -l app=quiz-app`
- **Infrastructure Issues**: Use OCI Console monitoring and support

---

**🎉 This plan ensures a systematic, validated deployment of your quiz website to Oracle Cloud Infrastructure with minimal risk and maximum reliability!**