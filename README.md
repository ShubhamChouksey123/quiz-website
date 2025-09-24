
# Quiz Website

## Overview

Welcome to my quiz website repository! This digital platform serves as a showcase of my journey as a software engineer, spotlighting my skills, projects, and experiences. Built with HTML, CSS, and JavaScript for the frontend, and Java with Spring Boot for the backend, it offers visitors profound insights into my expertise and creative endeavors. Leveraging PostgreSQL for database management and Gmail SMTP protocol for seamless communication, it stands as a testament to my commitment to delivering robust and dynamic web solutions.

## Current Deployment Status

**🌐 Live Application**: http://161.118.188.237:8080 ✅ **OPERATIONAL**

- **Platform**: Oracle Cloud Infrastructure (OCI) - Always Free Tier
- **Instance**: VM.Standard.A1.Flex (2 OCPUs, 4GB RAM, ARM-based)
- **Database**: PostgreSQL container co-located with application
- **Deployment**: Docker containerized using remote build approach
- **Cost**: $0.00 monthly (within OCI Always Free limits)

### Previous Deployments
- **Render.com**: [Quiz-Website](https://quiz-website-g8d7.onrender.com/) (migrated from)
- **Monitoring**: [Uptime-Robot-URL](https://uptimerobot.com/dashboard)
- **Scheduled Tasks**: [Cron-Job-URL](https://console.cron-job.org/jobs)




## Features

- Engage users with a collection of random multiple-choice questions sourced from a database.
- Assess user performance by evaluating quiz submissions and comparing scores with other participants.
- Classify scores as above average or otherwise, providing valuable feedback to users.
- Maintain an updated leader board showcasing top performers in the quiz.

## Getting Started

### Access the Live Application
Visit the **[Quiz Website](http://161.118.188.237:8080)** to start taking quizzes! Navigate through user-friendly interfaces offering options to start new quizzes, access leader boards, view results, and connect through the contact page.

### Local Development
For local development, see the [Local Development Guide](#local-development) section below.

## OCI Deployment Documentation

This project has been successfully deployed to Oracle Cloud Infrastructure (OCI). For detailed deployment information:

- **📋 Deployment Overview**: [`specs/README.md`](specs/README.md) - Complete OCI deployment strategy and project structure
- **✅ Deployment Checklist**: [`oci-deployment/docs/deployment-checklist.md`](oci-deployment/docs/deployment-checklist.md) - Phase-by-phase completion tracking
- **🖥️ Instance Details**: [`oci-deployment/docs/instance-details.md`](oci-deployment/docs/instance-details.md) - Complete instance specifications and management commands
- **📁 Implementation Files**: [`oci-deployment/`](oci-deployment/) - All deployment scripts, configurations, and documentation

### Quick Deployment Status
- **Prerequisites**: ✅ Complete
- **Infrastructure**: ✅ Complete
- **Application Deployment**: ✅ Complete
- **Validation**: 🎯 Ready to proceed

## Local Development

### Useful Commands and Procedures

#### Docker
```shell script
# Build docker image
docker build -t quiz-app .

# Run container
docker run -p 8080:8080 quiz-app

# View all images
docker images
```

#### Java Server
```shell script
# Start application (from root directory)
./start.sh

# Manual start (ensure app/.env exists)
env $(cat app/.env | grep -v "^#" | xargs) java -jar app/target/app-0.0.1-SNAPSHOT.jar
```

#### Maven Commands (from app/ directory)
```shell script
# Build and install
./mvnw clean install

# Format code
./mvnw spotless:apply

# Run tests
./mvnw test
```

### Sample .env File
Create `app/.env` file with your database credentials:
```shell script
# Quiz Website Database credentials
QUIZ_DB_HOST=localhost
QUIZ_DB_PORT=5432
QUIZ_DB_NAME=quiz
QUIZ_DB_USERNAME=postgres
QUIZ_DB_PASSWORD=root
QUIZ_DB_CONFIG_QUERY_STRING=allowPublicKeyRetrieval=true&useSSL=false&sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false&createDatabaseIfNotExist=true
```

## License

This project operates under an open-source model and is available under the [MIT License](LICENSE).

## Connect and Collaborate

I'm enthusiastic about exploring new opportunities, collaborations, and engaging discussions. Whether you have a project in mind, an innovative idea to explore, or simply wish to connect and share insights, I'm just an email or phone call away:

- Email: shubhamchouksey1998@gmail.com
- Phone: +91 9479917417

Let's embark on new journeys together and craft innovative solutions. Thank you for visiting my quiz website, and I eagerly anticipate connecting with you!



## Authors

- [@Shubham Chouksey](https://github.com/ShubhamChouksey123)

