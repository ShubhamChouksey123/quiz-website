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
- **PostgreSQL**: localhost:5432 (accessible from host when using hybrid setup)

## Configuration

- `docker-compose.yml`: Main container orchestration file
- `.env.local`: Local environment variables (dummy values for development)

## Features

- Builds application from local source code
- Uses development profile with SQL logging enabled
- PostgreSQL database with persistent volumes
- Health checks for both services
- Optimized for local development workflow

## Development Options

### Option 1: Full Docker Setup (Containerized Development)

Run both the application and PostgreSQL in Docker containers. Best for production-like environment testing.

```bash
# Start both services
docker-compose --env-file .env.local up --build -d

# View application logs
docker-compose logs -f quiz-app

# Stop all services
docker-compose down
```

### Option 2: Hybrid Setup (Recommended for Development)

Run only PostgreSQL in Docker while running the application outside Docker. This provides faster development cycles with full debugging capabilities.

#### Start PostgreSQL Only:
```bash
# Start only the PostgreSQL database
docker-compose --env-file .env.local up -d postgres

# Check PostgreSQL status
docker-compose --env-file .env.local ps postgres

# View PostgreSQL logs
docker-compose --env-file .env.local logs postgres
```

#### Start Application Outside Docker:
```bash
# From the project root directory
./start.sh
```

#### Stop PostgreSQL:
```bash
# Stop PostgreSQL container
docker-compose --env-file .env.local stop postgres

# Or completely remove PostgreSQL container (data persists in volume)
docker-compose --env-file .env.local down postgres
```

#### Benefits of Hybrid Setup:
- ✅ **Faster development cycles** - No Docker rebuild needed for code changes
- ✅ **Full debugging support** - Use IDE debugger, breakpoints, hot reload
- ✅ **Faster startup** - Spring Boot starts directly without Docker overhead
- ✅ **Live code changes** - Automatic recompilation and restart
- ✅ **Same database** - Uses the same PostgreSQL setup as full Docker

### Option 3: Individual Service Management

You can start/stop services individually:

```bash
# Start only PostgreSQL
docker-compose --env-file .env.local up -d postgres

# Start only the application (requires PostgreSQL to be running)
docker-compose --env-file .env.local up -d quiz-app

# Stop individual services
docker-compose --env-file .env.local stop postgres
docker-compose --env-file .env.local stop quiz-app
```

## Development Workflow

### For Full Docker Setup (Option 1)

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

### For Hybrid Setup (Option 2) - Recommended

#### Making Code Changes:
```bash
# 1. Make code changes in your IDE
# 2. The ./start.sh script automatically:
#    - Runs mvn spotless:apply (code formatting)
#    - Runs mvn clean install (builds the project)
#    - Starts the Spring Boot application
# 3. Test changes at http://localhost:8080
# 4. Application logs appear directly in your terminal
```

#### Daily Development Workflow:
```bash
# Start PostgreSQL (once per day)
docker-compose --env-file .env.local up -d postgres

# Start application (run this after each code change)
./start.sh

# When done for the day
docker-compose --env-file .env.local stop postgres
```

### Development Tips:

#### For Full Docker Setup:
- Use `--build` flag to force rebuild of Docker image
- Docker caches build layers, so subsequent builds are faster
- Database data persists between rebuilds (stored in Docker volume)
- Build typically takes 1-2 minutes depending on code changes
- Maven dependencies are cached to speed up builds

#### For Hybrid Setup:
- PostgreSQL runs in Docker with persistent data
- Application builds and runs directly on your machine
- Much faster development cycles (no Docker rebuild needed)
- Full IDE debugging and hot reload support
- Database connection string: `jdbc:postgresql://localhost:5432/quiz`

## Notes

- The application builds from the parent directory's source code
- Database data persists between container restarts
- Environment variables are configured for local testing (email functionality may not work with dummy credentials)