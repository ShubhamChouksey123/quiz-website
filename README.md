
# Quiz Website

## Overview


Welcome to my quiz website repository! This digital platform serves as a showcase of my journey as a software engineer, spotlighting my skills, projects, and experiences. Built with HTML, CSS, and JavaScript for the frontend, and Java with Spring Boot for the backend, it offers visitors profound insights into my expertise and creative endeavors. 

**This project is deployed on Oracle Cloud Infrastructure (OCI) using:**
- **OKE (Oracle Kubernetes Engine)** for container orchestration
- **Containerized PostgreSQL** for database management (deployed within the OKE cluster)
- **Oracle Container Registry (OCIR)** for container image storage
- **OCI Block Storage** for persistent database storage

- The master build is live at: [Quiz-Website](https://quiz-website-g8d7.onrender.com/)
- To ensure continuous uptime, a monitoring system is in place: [Uptime-Robot-URL](https://uptimerobot.com/dashboard)
- Scheduled tasks are configured to access the website every 10 minutes: [Cron-Job-URL](https://console.cron-job.org/jobs)

## Architecture

### Cloud Infrastructure
- **Platform**: Oracle Cloud Infrastructure (OCI)
- **Container Orchestration**: Oracle Kubernetes Engine (OKE)
- **Database**: Containerized PostgreSQL (deployed in OKE cluster)
- **Storage**: OCI Block Storage (50Gi persistent volume)
- **Networking**: OCI VCN with public/private subnets
- **Container Registry**: Oracle Container Registry (OCIR)

### Database Setup
This project uses a **containerized PostgreSQL database** deployed within the OKE cluster, providing:
- ✅ **High Availability** - Kubernetes-managed container lifecycle
- ✅ **Persistent Storage** - OCI Block Storage for data persistence
- ✅ **Cost Effective** - No separate managed database instance required
- ✅ **Scalable** - Can be scaled using Kubernetes deployments
- ✅ **Integrated Monitoring** - Native Kubernetes observability


## Features

- Engage users with a collection of random multiple-choice questions sourced from a database.
- Assess user performance by evaluating quiz submissions and comparing scores with other participants.
- Classify scores as above average or otherwise, providing valuable feedback to users.
- Maintain an updated leader board showcasing top performers in the quiz.

## Getting Started

Embark on your quiz journey by visiting the [Quiz Website](https://quiz-website-g8d7.onrender.com/). Navigate through user-friendly interfaces offering options to start new quizzes, access leader boards, view results, and connect through the contact page.




## Useful Commands and Procedures

Frequently used commands for managing the Quiz Website on OCI.

### Database Management (Containerized PostgreSQL)

#### Check Database Status
```bash
# Check PostgreSQL pod status
kubectl get pods -l app=postgres

# Check PostgreSQL service
kubectl get svc postgres-service

# Check persistent volume
kubectl get pvc postgres-pvc

# View PostgreSQL logs
kubectl logs -l app=postgres --tail=50
```

#### Database Connection
```bash
# Connect to PostgreSQL database
kubectl exec -it deployment/postgres -- psql -U postgres -d quiz_db

# Test database connectivity
kubectl exec deployment/postgres -- pg_isready -U postgres

# List all databases
kubectl exec deployment/postgres -- psql -U postgres -c "\l"

# Check database size and tables
kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -c "\dt"
```

#### Database Operations
```bash
# Create database backup
kubectl exec deployment/postgres -- pg_dump -U postgres quiz_db > backup.sql

# Restore database from backup
kubectl exec -i deployment/postgres -- psql -U postgres quiz_db < backup.sql

# Monitor database performance
kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -c "SELECT * FROM pg_stat_activity;"
```

#### Database Scaling and Management
```bash
# Scale PostgreSQL deployment (if needed)
kubectl scale deployment postgres --replicas=1

# Restart PostgreSQL pod
kubectl rollout restart deployment/postgres

# Update PostgreSQL configuration
kubectl edit configmap postgres-config
```

### OKE Cluster Management

#### Cluster Status
```bash
# Check cluster connectivity
kubectl cluster-info

# View all resources
kubectl get all

# Check node status
kubectl get nodes -o wide

# View cluster events
kubectl get events --sort-by='.lastTimestamp'
```

#### Application Deployment
```bash
# Deploy application
kubectl apply -f k8s/

# Check application status
kubectl get deployments
kubectl get pods
kubectl get services

# View application logs
kubectl logs -l app=quiz-app --tail=100
```

### Docker Commands

#### Local Development
```bash
# Build docker image
docker build -t quiz-app .

# Build with specific tag
docker build -t quiz-app:latest .

# View all images
docker images

# Run locally with database connection
docker run -p 8080:8080 quiz-app
```

#### Oracle Container Registry (OCIR)
```bash
# Login to OCIR
echo "$OCIR_AUTH_TOKEN" | docker login ap-mumbai-1.ocir.io -u "$OCIR_USERNAME" --password-stdin

# Tag image for OCIR
docker tag quiz-app:latest ap-mumbai-1.ocir.io/bmx5u3gkmcih/quiz-app:latest

# Push to OCIR
docker push ap-mumbai-1.ocir.io/bmx5u3gkmcih/quiz-app:latest
```

### Deployment Scripts

#### Automated Deployment
```bash
# Full deployment (builds image, creates database, deploys to OKE)
./scripts/deploy.sh

# Deploy without building Docker image
./scripts/deploy.sh --skip-docker

# Validate PostgreSQL deployment
./scripts/deploy.sh --validate-postgresql
```

### Troubleshooting

#### Common Issues and Solutions

**PostgreSQL Pod Not Starting**
```bash
# Check pod status and events
kubectl describe pod -l app=postgres
kubectl get events --field-selector involvedObject.name=postgres-deployment

# Common solutions:
# 1. Check persistent volume claim
kubectl get pvc postgres-pvc
# 2. Verify storage class availability
kubectl get storageclass
# 3. Check volume mount permissions
kubectl exec -it deployment/postgres -- ls -la /var/lib/postgresql/data
```

**Database Connection Issues**
```bash
# Verify service endpoints
kubectl get endpoints postgres-service

# Test internal cluster connectivity
kubectl run test-pod --image=postgres:13 --rm -it -- bash
# Inside test pod: psql -h postgres-service.default.svc.cluster.local -U postgres -d quiz_db

# Check service DNS resolution
kubectl exec deployment/postgres -- nslookup postgres-service.default.svc.cluster.local
```

**Application Deployment Failures**
```bash
# Check deployment status
kubectl rollout status deployment/quiz-app

# View detailed pod information
kubectl describe pod -l app=quiz-app

# Check application logs
kubectl logs -l app=quiz-app --previous
```

**OCI Resource Issues**
```bash
# Check OKE cluster health
kubectl get nodes
kubectl top nodes

# Verify OCI resources in console
# Navigate to: Containers & Artifacts > Kubernetes Clusters (OKE)
# Check: Compute > Instances for worker nodes
# Monitor: Observability > Monitoring for cluster metrics
```

#### Monitoring and Maintenance

**Database Health Monitoring**
```bash
# Monitor database connections
kubectl exec deployment/postgres -- psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"

# Check database size
kubectl exec deployment/postgres -- psql -U postgres -c "SELECT pg_size_pretty(pg_database_size('quiz_db'));"

# Monitor resource usage
kubectl top pod -l app=postgres
```

**Regular Maintenance Tasks**
```bash
# Update database statistics
kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -c "ANALYZE;"

# Check database integrity
kubectl exec deployment/postgres -- psql -U postgres -d quiz_db -c "REINDEX DATABASE quiz_db;"

# Clean up old logs
kubectl logs -l app=postgres --tail=0
```

## Project Structure

```
quiz-website/
├── Dockerfile                     # Application container configuration
├── README.md                      # Project documentation
├── app/                          # Java Spring Boot application
│   ├── Dockerfile               # App-specific Docker configuration  
│   ├── pom.xml                  # Maven dependencies and build configuration
│   └── src/                     # Application source code
├── k8s/                         # Kubernetes manifests
│   └── postgres/               # PostgreSQL deployment configurations
│       └── postgres.yaml      # Complete PostgreSQL Kubernetes setup
├── scripts/                     # Deployment and utility scripts
│   └── deploy.sh              # Automated deployment script
└── docs/                       # Additional documentation
```

## Environment Configuration

### Database Connection Configuration (Containerized)

The application automatically connects to the containerized PostgreSQL database using Kubernetes service discovery. Configuration is managed through:

- **Service Name**: `postgres-service.default.svc.cluster.local`
- **Port**: `5432`
- **Database**: `quiz_db`
- **Credentials**: Managed via Kubernetes secrets

### Legacy Environment Variables (for local development)
```shell script
# Quiz Website Database credentials (local development only)
QUIZ_DB_HOST=localhost
QUIZ_DB_PORT=5432
QUIZ_DB_NAME=quiz_db
QUIZ_DB_USERNAME=postgres
QUIZ_DB_PASSWORD=root
QUIZ_DB_CONFIG_QUERY_STRING=allowPublicKeyRetrieval=true&useSSL=false&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false&createDatabaseIfNotExist=true
```

## Java Application Management

### Local Development Server

* Ensure proper configuration for containerized database connection
```shell script
# For local development with containerized PostgreSQL
kubectl port-forward svc/postgres-service 5432:5432 &
env $(cat app/.env | grep -v "^#" | xargs) java -jar app/target/app-0.0.1-SNAPSHOT.jar
```

## Support and Resources

- **OCI Documentation**: [Oracle Cloud Infrastructure Documentation](https://docs.oracle.com/en-us/iaas/)
- **OKE Documentation**: [Oracle Container Engine for Kubernetes](https://docs.oracle.com/en-us/iaas/Content/ContEng/home.htm)
- **PostgreSQL on Kubernetes**: [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)
- **Spring Boot**: [Spring Boot Reference Documentation](https://spring.io/projects/spring-boot)

## License

This project operates under an open-source model and is available under the [MIT License](LICENSE).

## Connect and Collaborate

I'm enthusiastic about exploring new opportunities, collaborations, and engaging discussions. Whether you have a project in mind, an innovative idea to explore, or simply wish to connect and share insights, I'm just an email or phone call away:

- Email: shubhamchouksey1998@gmail.com
- Phone: +91 9479917417

Let's embark on new journeys together and craft innovative solutions. Thank you for visiting my quiz website, and I eagerly anticipate connecting with you!



## Authors

- [@Shubham Chouksey](https://github.com/ShubhamChouksey123)

