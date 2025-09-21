# OCI Migration Plan - Quiz Website

## Project Overview

Migrating the quiz website from Render.com (expired free tier) to Oracle Cloud Infrastructure (OCI) free tier to maintain zero-cost hosting.

### Current State
- **Application**: Spring Boot 3 application with PostgreSQL database
- **Previous Hosting**: Render.com (free tier expired)
- **Source Code**: GitHub repository - https://github.com/ShubhamChouksey123/quiz-website
- **Technology Stack**: Java 17, Spring Boot 3.1.4, PostgreSQL, Thymeleaf, Docker

### Target State
- **Cloud Provider**: Oracle Cloud Infrastructure (OCI)
- **Database**: OCI Database service or Container-based PostgreSQL
- **Container Registry**: Oracle Container Image Registry (OCIR)
- **Compute**: OCI Compute Instance (free tier eligible)
- **Cost**: $0 (using OCI Always Free resources)

## Migration Objectives

1. **Zero Cost Deployment**: Utilize only OCI Always Free tier resources
2. **Minimal Downtime**: Ensure smooth transition from current state
3. **Maintain Functionality**: All existing features should work identically
4. **Scalability**: Setup should allow for future growth within free limits

## OCI Free Tier Resources Available

### Always Free Compute
- 2 AMD-based Compute VMs (VM.Standard.E2.1.Micro: 1/8 OCPU, 1GB RAM)
- 4 ARM-based Ampere A1 cores and 24GB RAM (can be split across instances)

### Always Free Database
- 2 Oracle Autonomous Databases (ATP or ADW) - 1 OCPU, 20GB storage each
- **Note**: No free PostgreSQL database service, will need alternative approach

### Always Free Storage & Networking
- 200GB Block Volume storage
- 10GB Object Storage
- 1 Virtual Cloud Network (VCN)
- 1 Load Balancer

## High-Level Migration Steps

1. **Database Setup** - Deploy PostgreSQL using container approach
2. **Container Registry** - Push Docker image to OCIR
3. **Compute Instance** - Create and configure OCI compute instance
4. **Application Deployment** - Deploy containerized application
5. **Testing & Validation** - Ensure full functionality

## Key Considerations

### Database Strategy
Since OCI doesn't offer free PostgreSQL, we will use:
1. **Container-based PostgreSQL**: Run PostgreSQL in Docker container on compute instance ✅ **SELECTED**

**Implementation**: Deploy PostgreSQL as a Docker container alongside the application container on the same OCI compute instance. This approach minimizes code changes, eliminates network latency between app and database, and efficiently utilizes the free tier compute resources.

### Security & Networking
- Configure VCN with appropriate security rules
- Ensure database is only accessible from application
- Setup HTTPS/SSL for production access

### Resource Optimization
- Use smallest compute shapes that meet requirements
- Implement efficient container resource limits
- Monitor usage to stay within free tier limits

## Success Criteria

- [ ] Application accessible via public IP
- [ ] All quiz functionality working (create, take, score quizzes)
- [ ] Email functionality operational
- [ ] Database persistence working
- [ ] Performance comparable to previous deployment
- [ ] Zero ongoing costs