# Local Development Setup

This directory contains the Docker configuration for running the Quiz Website locally.

## Quick Start

```bash
# Start the application
docker-compose --env-file .env.local up --build -d

# View logs
docker-compose logs -f quiz-app

# Stop the application
docker-compose down

# Rebuild after code changes
docker-compose --env-file .env.local up --build -d
```

## Access

- **Application**: http://localhost:8080
- **PostgreSQL**: localhost:5432 (internal Docker network only)

## Configuration

- `docker-compose.yml`: Main container orchestration file
- `.env.local`: Local environment variables (dummy values for development)

## Features

- Builds application from local source code
- Uses development profile with SQL logging enabled
- PostgreSQL database with persistent volumes
- Health checks for both services
- Optimized for local development workflow

## Development Workflow

### Making Code Changes

Every time you make code changes, you need to rebuild the Docker image since the application is built inside the container.

**Standard workflow:**
```bash
# 1. Make code changes in your IDE
# 2. Stop and rebuild containers
docker-compose down
docker-compose --env-file .env.local up --build -d

# 3. Test changes at http://localhost:8080
# 4. Check logs if needed
docker-compose logs -f quiz-app
```

**Faster rebuild (restart only app service):**
```bash
# Rebuild and restart just the quiz-app service
docker-compose --env-file .env.local up --build -d quiz-app
```

### Why rebuild is needed:

The Dockerfile performs these steps for every build:
1. **Copies source code** into build container
2. **Runs Maven build** (`mvn spotless:apply` + `mvn clean install`)
3. **Creates final JAR** (app-0.0.1-SNAPSHOT.jar)
4. **Packages into runtime image**

Any code changes require rebuilding the JAR inside Docker.

### Development Tips:

- Use `--build` flag to force rebuild of Docker image
- Docker caches build layers, so subsequent builds are faster
- Database data persists between rebuilds (stored in Docker volume)
- Build typically takes 1-2 minutes depending on code changes
- Maven dependencies are cached to speed up builds

## Notes

- The application builds from the parent directory's source code
- Database data persists between container restarts
- Environment variables are configured for local testing (email functionality may not work with dummy credentials)