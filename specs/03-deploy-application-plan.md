# Application Deployment Plan

## Implementation Script

**Linked Script**: `oci-deployment/scripts/03-deploy-application.sh`

This plan is implemented by the above script to deploy the Spring Boot 3 quiz application with PostgreSQL database.

## Overview

Deploy the quiz application to the OCI compute instance using a remote Docker build approach with containerized PostgreSQL database.

## Prerequisites

📋 **Infrastructure Status**: See `oci-deployment/docs/instance-details.md` for current infrastructure status and specifications.

📋 **Deployment Progress**: See `oci-deployment/docs/deployment-checklist.md` for overall deployment progress tracking.

### Application Requirements (To be verified)
- [ ] Environment variables configured (`.env` file)
- [ ] OCIR authentication working
- [ ] Application source code accessible
- [ ] PostgreSQL data directory permissions correct

## Deployment Strategy - Remote Build Approach

### Phase 1: Environment Configuration
**Objective**: Set up environment variables and OCIR authentication

1. **Environment Variables Setup**
   - Create `.env` file on instance with production configuration
   - Database credentials (PostgreSQL admin password)
   - Email configuration (Gmail SMTP for contact form)
   - OCIR image reference for deployment

2. **OCIR Authentication**
   - Configure Docker login to Oracle Container Registry
   - Test registry connectivity and push permissions
   - Verify OCIR namespace and image path

3. **Source Code Transfer**
   - Copy complete application source to `/opt/quiz-app/source/`
   - Include `app/` directory with Spring Boot application
   - Transfer `Dockerfile` for multi-stage build
   - Set proper file permissions and ownership

### Phase 2: Application Build Process
**Objective**: Build the Spring Boot application Docker image on OCI instance

1. **Multi-Stage Docker Build**
   - **Stage 1**: Maven build with `maven:3.8.5-openjdk-17`
     - Apply code formatting with Spotless
     - Compile and package JAR (`mvn clean package -DskipTests`)
   - **Stage 2**: Runtime image with `openjdk:17.0.1-jdk-slim`
     - Copy compiled JAR
     - Expose port 8080
     - Configure entry point

2. **Image Tagging and Registry Push**
   - Tag image with OCIR path: `ap-mumbai-1.ocir.io/NAMESPACE/quiz-app:latest`
   - Push to Oracle Container Registry
   - Verify image availability in OCIR

3. **Build Verification**
   - Test local image functionality
   - Verify image layers and size optimization
   - Confirm Java 17 runtime compatibility

### Phase 3: Database Container Deployment
**Objective**: Deploy PostgreSQL database with persistent storage

1. **PostgreSQL Container Configuration**
   - Image: `postgres:15-alpine`
   - Environment variables: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
   - Data persistence: `/opt/quiz-app/data/postgres:/var/lib/postgresql/data`
   - Port mapping: `5432:5432` (internal to Docker network)

2. **Data Directory Setup**
   - Ensure `/opt/quiz-app/data/postgres` has correct permissions (999:999)
   - Set directory permissions to 700 for security
   - Create database initialization if needed

3. **Database Container Deployment**
   ```bash
   docker run -d \
     --name quiz-postgres \
     --restart unless-stopped \
     -e POSTGRES_DB=quiz_app \
     -e POSTGRES_USER=quiz_admin \
     -e POSTGRES_PASSWORD=${DB_ADMIN_PASSWORD} \
     -v /opt/quiz-app/data/postgres:/var/lib/postgresql/data \
     -v /opt/quiz-app/logs:/var/log/postgresql \
     postgres:15-alpine
   ```

### Phase 4: Application Container Deployment
**Objective**: Deploy the quiz application container with database connectivity

1. **Docker Compose Configuration**
   Create `docker-compose.yml` in `/opt/quiz-app/`:
   ```yaml
   version: '3.8'
   services:
     quiz-postgres:
       image: postgres:15-alpine
       container_name: quiz-postgres
       restart: unless-stopped
       environment:
         POSTGRES_DB: quiz_app
         POSTGRES_USER: quiz_admin
         POSTGRES_PASSWORD: ${DB_ADMIN_PASSWORD}
       volumes:
         - ./data/postgres:/var/lib/postgresql/data
         - ./logs:/var/log/postgresql
       networks:
         - quiz-network

     quiz-app:
       image: ${DOCKER_IMAGE}
       container_name: quiz-app
       restart: unless-stopped
       ports:
         - "8080:8080"
       environment:
         SPRING_DATASOURCE_URL: "jdbc:postgresql://quiz-postgres:5432/quiz_app"
         SPRING_DATASOURCE_USERNAME: quiz_admin
         SPRING_DATASOURCE_PASSWORD: ${DB_ADMIN_PASSWORD}
         GMAIL_USERNAME: ${GMAIL_USERNAME}
         GMAIL_PASSWORD: ${GMAIL_PASSWORD}
       depends_on:
         - quiz-postgres
       networks:
         - quiz-network

   networks:
     quiz-network:
       driver: bridge
   ```

2. **Application Deployment**
   ```bash
   cd /opt/quiz-app
   docker-compose up -d
   ```

3. **Container Health Verification**
   - Verify PostgreSQL container startup and readiness
   - Check application container startup and health
   - Monitor container logs for errors

### Phase 5: Service Verification and Configuration
**Objective**: Verify deployment success and configure monitoring

1. **Application Connectivity Tests**
   - Test HTTP endpoint: `curl http://localhost:8080`
   - Test health endpoint: `curl http://localhost:8080/actuator/health`
   - Verify database schema creation (Hibernate auto-DDL)

2. **External Access Verification**
   - Test public access: `curl http://161.118.188.237:8080`
   - Verify security list allows port 8080 traffic
   - Test application functionality through browser

3. **Logging and Monitoring Setup**
   - Configure log rotation for container logs
   - Set up basic monitoring scripts
   - Create health check automation

## Technical Specifications

### Application Configuration
- **Framework**: Spring Boot 3.1.4 with Java 17
- **Database**: PostgreSQL 15 with Hibernate ORM
- **Architecture**: ARM64 (Ampere A1 compatible)
- **Port**: 8080 (application), 5432 (database internal)
- **Resource Limits**: 2 OCPUs, 4GB RAM available

### Environment Variables Required
```bash
# Database Configuration
DB_ADMIN_PASSWORD=secure-generated-password

# Email Configuration (for contact form)
GMAIL_USERNAME=your-app-email@gmail.com
GMAIL_PASSWORD=your-app-specific-password

# Docker Image Reference
DOCKER_IMAGE=ap-mumbai-1.ocir.io/NAMESPACE/quiz-app:latest
```

### Network Configuration
- **Internal**: Docker bridge network for container communication
- **External**: Port 8080 exposed to public internet
- **Security**: PostgreSQL port 5432 not exposed externally

## Success Criteria

### Primary Objectives
- [ ] Application accessible via http://161.118.188.237:8080
- [ ] Home page loads correctly with proper styling
- [ ] Database connectivity established (no connection errors)
- [ ] Health check endpoint responding: `/actuator/health`
- [ ] Quiz functionality operational (create, take, submit)
- [ ] Contact form email functionality working

### Technical Validation
- [ ] All containers running with `restart: unless-stopped` policy
- [ ] PostgreSQL data persisted in `/opt/quiz-app/data/postgres`
- [ ] Application logs accessible in `/opt/quiz-app/logs`
- [ ] Docker images successfully pushed to OCIR
- [ ] No critical errors in application or database logs

### Performance Verification
- [ ] Application startup time < 60 seconds
- [ ] Database schema auto-creation successful
- [ ] Memory usage < 3GB (within 4GB instance limit)
- [ ] Response time < 2 seconds for main pages

## Risk Mitigation

### Container Resource Management
- **Memory Limits**: Set container memory limits to prevent OOM
- **Restart Policies**: Configure automatic restart on failure
- **Health Checks**: Implement container health monitoring

### Data Persistence
- **Database Backup**: Regular PostgreSQL data backups
- **Volume Management**: Proper Docker volume configuration
- **Disaster Recovery**: Document data recovery procedures

### Network Security
- **Internal Communication**: Use Docker networks for container-to-container
- **External Access**: Only port 8080 exposed publicly
- **SSL/TLS**: Consider future HTTPS implementation

## Troubleshooting Guide

### Common Issues and Solutions
1. **Docker Build Failures**
   - Check internet connectivity for Maven dependencies
   - Verify Dockerfile syntax and base image availability
   - Check disk space for image layers

2. **Container Startup Issues**
   - Verify environment variables are set correctly
   - Check container logs: `docker logs quiz-app`
   - Ensure PostgreSQL is ready before app startup

3. **Database Connection Problems**
   - Verify PostgreSQL container is running
   - Check database credentials and connection string
   - Test network connectivity between containers

4. **OCIR Authentication Issues**
   - Verify auth token validity
   - Check OCIR namespace and region configuration
   - Test Docker login to registry

## Post-Deployment Tasks

### Immediate Actions
1. **Functionality Testing**: Run comprehensive application tests
2. **Performance Monitoring**: Monitor resource usage and response times
3. **Log Review**: Check for any warnings or errors in logs
4. **Documentation Update**: Record deployment details and configuration

### Future Enhancements
- SSL/TLS certificate installation
- Automated backup scheduling
- Application performance monitoring
- Container orchestration improvements

## Implementation Timeline

- **Phase 1** (Environment Setup): 15-20 minutes
- **Phase 2** (Build Process): 10-15 minutes
- **Phase 3** (Database Deployment): 5-10 minutes
- **Phase 4** (Application Deployment): 10-15 minutes
- **Phase 5** (Verification): 10-15 minutes

**Total Estimated Time**: 50-75 minutes

## Next Steps After Completion

1. **Run Validation Script**: `./04-validate-deployment.sh`
2. **Comprehensive Testing**: Verify all application features
3. **Performance Optimization**: Monitor and tune resource usage
4. **Documentation Update**: Complete deployment documentation
5. **Backup Strategy**: Implement data backup procedures