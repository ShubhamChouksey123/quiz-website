# Database Deployment Plan - PostgreSQL on OCI

## Overview

Since OCI doesn't provide free PostgreSQL database service, we'll deploy PostgreSQL as a Docker container on the same compute instance as the application, optimizing for the free tier constraints.

## Database Architecture

### Option 1: Co-located PostgreSQL Container (Recommended) ✅ **SELECTED**
- **Approach**: Run PostgreSQL container alongside application container
- **Benefits**: Simple setup, no network latency, stays within free tier
- **Considerations**: Shares compute resources with application

### Option 2: Separate Database Instance
- **Approach**: Dedicated compute instance for PostgreSQL
- **Benefits**: Resource isolation, better performance
- **Considerations**: Uses second free tier compute instance

**Selected Approach**: Option 1 - Co-located PostgreSQL for resource efficiency

## Database Configuration

### PostgreSQL Container Setup
```yaml
# Docker Compose configuration
services:
  postgres:
    image: postgres:14-alpine
    container_name: quiz-postgres
    environment:
      POSTGRES_DB: quiz
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_ADMIN_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Database Initialization
- Create database with name `quiz` (not `quiz_db` as PostgreSQL container will auto-create this)
- **No manual schema creation needed** - Hibernate auto-DDL is enabled with `hibernate.hbm2ddl.auto=update`
- Hibernate will automatically create tables and schema on first application startup
- Database user permissions handled by default PostgreSQL setup

## Data Migration Strategy

**No Data Migration Required** - This is a fresh deployment for a personal project. Starting with a clean database instance.

### Fresh Start Approach
1. **Clean Database**: Start with empty PostgreSQL database
2. **Hibernate Auto-Creation**: Let Hibernate create all necessary tables on first run
3. **Test Data**: Can be added through the application interface after deployment
4. **No Legacy Data**: No existing production data to migrate from Render

## Storage Strategy

### Volume Configuration
- **Data Volume**: `/var/lib/postgresql/data` - persistent data storage
- **Backup Volume**: `/backups` - automated backup storage
- **Size Allocation**: 10GB for database data (within 200GB free block storage)

### Backup Strategy

**Future Scope** - Backup implementation will be added later as needed.
- Initial deployment focuses on core functionality
- Data persistence handled through Docker volumes
- Manual backups can be performed if needed using standard PostgreSQL tools

## Performance Optimization

### Memory Configuration
- `shared_buffers`: 128MB (1/4 of available RAM)
- `effective_cache_size`: 512MB
- `maintenance_work_mem`: 64MB
- `wal_buffers`: 16MB

### Connection Settings
- `max_connections`: 20 (sufficient for single application)
- `connection_timeout`: 30s

## Security Configuration

### Network Security
- Database only accessible from application container
- No external database access
- Use Docker internal networking

### Authentication
- Strong password from environment variables
- Database user with minimal required privileges
- Regular password rotation schedule

## Environment Configuration

### Required Environment Variables
```bash
# Database Configuration
POSTGRES_DB=quiz
POSTGRES_USER=postgres
POSTGRES_PASSWORD=${DB_ADMIN_PASSWORD}

# Application Connection
QUIZ_DB_HOST=postgres  # Docker service name
QUIZ_DB_PORT=5432
QUIZ_DB_NAME=quiz
QUIZ_DB_USERNAME=postgres
QUIZ_DB_PASSWORD=${DB_ADMIN_PASSWORD}
```

## Monitoring & Health Checks

**Future Scope** - Advanced monitoring will be implemented later.

### Basic Health Checks (Included in Docker Compose)
- Container health checks using `pg_isready` command
- Docker Compose dependency management
- Application connection validation on startup

### Advanced Monitoring (Future Implementation)
- Detailed performance monitoring
- Query analysis and optimization
- Resource utilization tracking
- Automated alerting system

## Deployment Steps

1. **Prepare Docker Compose**: Configure PostgreSQL service with proper environment variables
2. **Start PostgreSQL Container**: Launch database container and verify health
3. **Configure Networking**: Setup Docker internal networking for app-database communication
4. **Test Database**: Verify PostgreSQL is accessible and ready
5. **Deploy Application**: Start application container with database connection
6. **Verify Schema Creation**: Confirm Hibernate auto-creates tables successfully

## Rollback Plan

### In Case of Issues
1. **Container Rollback**: Revert to previous PostgreSQL image
2. **Data Recovery**: Restore from latest backup
3. **Network Troubleshooting**: Reset container networking
4. **Application Fallback**: Temporary read-only mode if needed

## Success Criteria

- [ ] PostgreSQL container running stable and healthy
- [ ] Database `quiz` created automatically by container
- [ ] Application connects to database without errors
- [ ] Hibernate auto-creates all necessary tables on first startup
- [ ] All CRUD operations working (create, read, update, delete)
- [ ] Data persistence across container restarts verified
- [ ] Performance within acceptable limits for OCI free tier resources
- [ ] No manual schema management required