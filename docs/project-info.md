# Quiz Website - Project Documentation

## Architecture Overview

The Quiz Website is deployed on Oracle Cloud Infrastructure (OCI) using a containerized architecture with Kubernetes orchestration.

### Current Deployment Status

- **Production Environment**: OCI Oracle Container Engine for Kubernetes (OKE)
- **Database**: Containerized PostgreSQL running in OKE cluster
- **Application**: Spring Boot containerized application
- **Load Balancer**: OCI Load Balancer for high availability
- **Storage**: OCI Block Storage for PostgreSQL data persistence

## Infrastructure Components

### Oracle Cloud Infrastructure (OCI)
- **OKE Cluster**: `shubham-quiz-website` cluster running in ap-mumbai-1 region
- **Compute**: VM.Standard.E4.Flex instances for worker nodes
- **Storage**: OCI Block Storage (50Gi) for PostgreSQL persistent volumes
- **Networking**: VCN with public/private subnets for secure deployment
- **Container Registry**: Oracle Container Image Registry (OCIR) for image storage

### Containerized PostgreSQL Database
- **Version**: PostgreSQL 13 (containerized)
- **Storage**: Persistent volume with OCI Block Storage
- **High Availability**: Single replica with persistent data
- **Connection**: Internal Kubernetes service discovery
- **Security**: Kubernetes secrets for credential management

#### Database Configuration
```yaml
Service Name: postgres-service.default.svc.cluster.local
Port: 5432
Database: quiz_db
User: postgres
Namespace: default
Storage: 50Gi OCI Block Storage
```

## Database Management

### Available Management Tools

#### 1. Primary Deployment Script
```bash
# Deploy PostgreSQL to OKE cluster
./scripts/deploy.sh --deploy-postgresql

# Check PostgreSQL status
./scripts/deploy.sh --check-postgresql

# Full application deployment
./scripts/deploy.sh
```

#### 2. Dedicated Database Manager
```bash
# Comprehensive database status
./scripts/db-manager.sh status

# Create database backup
./scripts/db-manager.sh backup

# Restore from backup
./scripts/db-manager.sh restore backup-file.sql.gz

# Open database shell
./scripts/db-manager.sh shell

# Monitor performance
./scripts/db-manager.sh monitor

# Database maintenance tasks
./scripts/db-manager.sh maintenance
```

#### 3. Automated Backup Scheduler
```bash
# Create automated backup with 7-day retention
./scripts/backup-scheduler.sh

# Create backup with custom retention
./scripts/backup-scheduler.sh 14

# List existing backups
./scripts/backup-scheduler.sh list
```

### Database Operations

#### Essential Commands
```bash
# Check database pod status
kubectl get pods -l app=postgres

# View database logs
kubectl logs -l app=postgres --tail=50

# Test database connectivity
kubectl exec deployment/postgres -- pg_isready -U postgres

# Access database shell
kubectl exec -it deployment/postgres -- psql -U postgres -d quiz_db

# Monitor resource usage
kubectl top pod -l app=postgres
```

#### Backup and Recovery
```bash
# Manual backup
kubectl exec deployment/postgres -- pg_dump -U postgres quiz_db > backup.sql

# Compressed backup
kubectl exec deployment/postgres -- pg_dump -U postgres quiz_db | gzip > backup.sql.gz

# Restore from backup
kubectl exec -i deployment/postgres -- psql -U postgres quiz_db < backup.sql

# Restore from compressed backup
gunzip -c backup.sql.gz | kubectl exec -i deployment/postgres -- psql -U postgres quiz_db
```

## Application Development

### Local Development Setup

#### Prerequisites
```bash
# Port forward PostgreSQL for local access
kubectl port-forward svc/postgres-service 5432:5432 &

# Verify connection
psql -h localhost -p 5432 -U postgres -d quiz_db
```

#### Environment Configuration (Local Development)
```shell script
# Local development environment variables
QUIZ_DB_HOST=localhost
QUIZ_DB_PORT=5432
QUIZ_DB_NAME=quiz_db
QUIZ_DB_USERNAME=postgres
QUIZ_DB_PASSWORD=<your-password>
```

#### Application Startup
```shell script
# Start application locally (with port forwarding active)
mvn spring-boot:run

# Or using JAR file
java -jar app/target/app-0.0.1-SNAPSHOT.jar
```

### Container Development

#### Docker Operations
```bash
# Build application image
docker build -t quiz-app:latest .

# Build and tag for OCIR
docker build -t ap-mumbai-1.ocir.io/bmx5u3gkmcih/quiz-app:latest .

# Push to Oracle Container Registry
echo "$OCIR_AUTH_TOKEN" | docker login ap-mumbai-1.ocir.io -u "$OCIR_USERNAME" --password-stdin
docker push ap-mumbai-1.ocir.io/bmx5u3gkmcih/quiz-app:latest
```

#### Kubernetes Deployment
```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get deployments
kubectl get pods
kubectl get services

# View application logs
kubectl logs -l app=quiz-app --tail=100

# Scale application
kubectl scale deployment quiz-app --replicas=3
```

## Monitoring and Maintenance

### Health Checks
```bash
# Application health
kubectl get pods -l app=quiz-app
kubectl logs -l app=quiz-app --tail=20

# Database health
kubectl get pods -l app=postgres
kubectl exec deployment/postgres -- pg_isready -U postgres

# Service endpoints
kubectl get services
kubectl get endpoints
```

### Performance Monitoring
```bash
# Resource usage
kubectl top nodes
kubectl top pods

# Database performance
./scripts/db-manager.sh monitor 300  # Monitor for 5 minutes

# Database analysis
./scripts/db-manager.sh analyze
```

### Maintenance Tasks
```bash
# Database maintenance
./scripts/db-manager.sh maintenance

# Application restart
kubectl rollout restart deployment/quiz-app

# PostgreSQL restart
kubectl rollout restart deployment/postgres

# Scale resources
kubectl scale deployment postgres --replicas=1
kubectl scale deployment quiz-app --replicas=2
```

## Security Best Practices

### Database Security
- Passwords managed via Kubernetes secrets
- Network isolation within Kubernetes cluster
- No external database exposure (internal service only)
- Regular backup encryption and rotation

### Application Security
- Container images scanned for vulnerabilities
- Non-root user execution in containers
- Resource limits and quotas applied
- TLS encryption for external traffic

## Backup Strategy

### Automated Backups
- **Frequency**: Daily backups via cron scheduler
- **Retention**: 7-day default retention (configurable)
- **Compression**: Gzip compression for space efficiency
- **Verification**: Automatic backup integrity checks

### Manual Backup Procedures
```bash
# Create immediate backup
./scripts/db-manager.sh backup emergency-backup

# Create backup with specific retention
./scripts/backup-scheduler.sh 30  # 30-day retention

# Verify backup integrity
./scripts/db-manager.sh test
```

## Troubleshooting

### Common Issues

#### PostgreSQL Pod Not Starting
```bash
# Check pod events
kubectl describe pod -l app=postgres

# Check persistent volume
kubectl get pvc postgres-pvc

# Check storage class
kubectl get storageclass

# View detailed logs
kubectl logs -l app=postgres --previous
```

#### Database Connection Issues
```bash
# Test service connectivity
kubectl get endpoints postgres-service

# Test internal resolution
kubectl exec deployment/postgres -- nslookup postgres-service.default.svc.cluster.local

# Check database logs
kubectl logs -l app=postgres --tail=100
```

#### Application Deployment Issues
```bash
# Check deployment status
kubectl rollout status deployment/quiz-app

# View pod details
kubectl describe pod -l app=quiz-app

# Check environment variables
kubectl exec deployment/quiz-app -- env | grep -i db
```

### Performance Issues
```bash
# Monitor database performance
./scripts/db-manager.sh monitor

# Check resource limits
kubectl describe pod -l app=postgres

# Analyze slow queries
./scripts/db-manager.sh analyze
```

## Development Roadmap

### Completed Features
- ✅ Containerized PostgreSQL deployment
- ✅ Kubernetes orchestration with OKE
- ✅ Automated backup and restore system
- ✅ Comprehensive monitoring and management tools
- ✅ High availability architecture
- ✅ Persistent storage with OCI Block Storage

### Future Enhancements
- [ ] PostgreSQL high availability with read replicas
- [ ] Advanced monitoring with Prometheus/Grafana
- [ ] Automated scaling based on metrics
- [ ] Enhanced security with HashiCorp Vault
- [ ] CI/CD pipeline with OCI DevOps
- [ ] Database connection pooling optimization

### Application Features (TODO)
- [ ] Resume storage and management system
- [ ] OAuth2 integration with custom client credentials
- [ ] Question categorization system
- [ ] Random question selection algorithm
- [ ] Enhanced contact page with auto-clear modals
- [ ] Comprehensive about page content

## Contact and Support

### Project Maintainer
- **Name**: Shubham Chouksey
- **Email**: shubhamchouksey1998@gmail.com
- **Phone**: +91 9479917417
- **GitHub**: [@ShubhamChouksey123](https://github.com/ShubhamChouksey123)

### Documentation Resources
- **Main README**: [README.md](../README.md)
- **Deployment Guide**: [scripts/deploy.sh](../scripts/deploy.sh)
- **Database Manager**: [scripts/db-manager.sh](../scripts/db-manager.sh)
- **Backup Scheduler**: [scripts/backup-scheduler.sh](../scripts/backup-scheduler.sh)

For technical support or collaboration opportunities, please reach out via email or phone. The project is actively maintained and contributions are welcome.
