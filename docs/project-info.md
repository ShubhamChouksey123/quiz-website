
# Quiz Website - OCI Deployment Information

## Current Live Application

**🌐 Production URL**: http://161.118.188.237:8080 ✅ **OPERATIONAL**

- **Platform**: Oracle Cloud Infrastructure (OCI) - Always Free Tier
- **Instance**: VM.Standard.A1.Flex (2 OCPUs, 4GB RAM, ARM-based)
- **Public IP**: 161.118.188.237
- **Database**: PostgreSQL container co-located with application
- **Deployment**: Docker containerized using remote build approach
- **Monthly Cost**: $0.00 (within OCI Always Free limits)

## Legacy Monitoring Systems (Render.com Migration)

- **Previous URL**: [Quiz-Website](https://quiz-website-g8d7.onrender.com/) (migrated from)
- **Uptime Monitoring**: [Uptime Robot Dashboard](https://uptimerobot.com/dashboard)
- **Scheduled Tasks**: [Cron Job Console](https://console.cron-job.org/jobs) (10-minute intervals)

## Complete Deployment Documentation

For detailed deployment information and management:

- **📋 Deployment Overview**: [`specs/README.md`](../specs/README.md) - Complete OCI deployment strategy
- **✅ Deployment Status**: [`oci-deployment/docs/deployment-checklist.md`](../oci-deployment/docs/deployment-checklist.md) - Phase tracking
- **🖥️ Instance Management**: [`oci-deployment/docs/instance-details.md`](../oci-deployment/docs/instance-details.md) - Instance specifications and commands
- **🔧 Implementation**: [`oci-deployment/scripts/`](../oci-deployment/scripts/) - Deployment scripts
- **⚙️ Configurations**: [`oci-deployment/configs/`](../oci-deployment/configs/) - Docker and cloud-init configs

## Deployment Status Summary

| Phase | Status | Description |
|-------|--------|-------------|
| Prerequisites | ✅ Complete | OCI access, tools, SSH keys |
| Infrastructure | ✅ Complete | Compute instance created and configured |
| Application Deployment | ✅ Complete | Docker containers running |
| Validation | 🎯 Ready | Application operational, ready for validation |

# OCI Instance Management Commands

## Container Management (Live Application)

### Check Application Status
```shell script
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 'cd /opt/quiz-app && docker compose ps'
```

### View Application Logs
```shell script
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 'cd /opt/quiz-app && docker compose logs -f quiz-app'
```

### View Database Logs
```shell script
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 'cd /opt/quiz-app && docker compose logs -f postgres'
```

### Restart Application
```shell script
ssh -i ~/.ssh/id_rsa opc@161.118.188.237 'cd /opt/quiz-app && docker compose restart'
```

## Local Development Commands

### Docker Commands
```shell script
# Build docker image
docker build -t quiz-app .

# Run container locally
docker run -p 8080:8080 quiz-app

# View all images
docker images
```

### Java Server (Local)
```shell script
# Start application (from root directory)
./start.sh

# Manual start (ensure app/.env exists)
env $(cat app/.env | grep -v "^#" | xargs) java -jar app/target/app-0.0.1-SNAPSHOT.jar
```

### Maven Commands (from app/ directory)
```shell script
# Build and install
./mvnw clean install

# Format code
./mvnw spotless:apply

# Run tests
./mvnw test
```

## Local Environment Configuration

### Sample .env File (for Local Development)
Create `app/.env` file:
```shell script
# Quiz Website Database credentials
QUIZ_DB_HOST=localhost
QUIZ_DB_PORT=5432
QUIZ_DB_NAME=quiz
QUIZ_DB_USERNAME=postgres
QUIZ_DB_PASSWORD=root
QUIZ_DB_CONFIG_QUERY_STRING=allowPublicKeyRetrieval=true&useSSL=false&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false&createDatabaseIfNotExist=true
```

## Project TODO Items

### Application Features
* Database for storing resume send information
* Creating our own client-id and secret key
* Add category to be added in database and in Java entity class
* Contact page making the success modal clear after 30 seconds
* Random questions from the database
* About Page: content enhancement

### Infrastructure Enhancements
* SSL/TLS certificate implementation
* Custom domain name setup
* Automated backup system
* CI/CD pipeline integration
* Monitoring and alerting setup






## Authors

- [@Shubham Chouksey](https://github.com/ShubhamChouksey123)
