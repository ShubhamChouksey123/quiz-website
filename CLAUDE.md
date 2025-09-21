# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

### Building and Running the Application
- **Build and start app**: `./start.sh` (from root directory)
  - Kills existing service on port 8080
  - Builds with Maven using `./mvnw clean install`
  - Applies code formatting with `./mvnw spotless:apply`
  - Starts the Spring Boot application with environment variables from `app/.env`

- **Manual Maven commands** (from `app/` directory):
  - Build: `./mvnw clean install`
  - Format code: `./mvnw spotless:apply`
  - Run tests: `./mvnw test`

### Docker
- **Build image**: `docker build -t quiz-app .`
- **Run container**: `docker run -p 8080:8080 quiz-app`

## Code Architecture

### Technology Stack
- **Backend**: Spring Boot 3.1.4 with Java 17
- **Database**: PostgreSQL with Hibernate ORM (custom configuration, excludes Spring Boot's auto-configuration)
- **Frontend**: Thymeleaf templates with static HTML/CSS/JS assets
- **Email**: Gmail SMTP integration for notifications
- **Build**: Maven with Spotless code formatting

### Project Structure
- **Root**: Contains `start.sh`, `Dockerfile`, and project documentation
- **app/**: Main Spring Boot application
  - `src/main/java/com/shubham/app/`: Java source code
  - `src/main/resources/`: Configuration, SQL scripts, and static web assets
  - `src/test/java/`: Unit tests (limited coverage)

### Key Architectural Patterns
- **MVC Architecture**: Controllers handle web requests, render Thymeleaf templates
- **Repository Pattern**: DAO layer with Hibernate for database operations
- **Service Layer**: Business logic separated from controllers
- **Component Scanning**: `@ComponentScan({"com.shubham"})` for dependency injection

### Core Functionality Areas
1. **Quiz System**: Question management, quiz sessions, scoring, leaderboard
2. **Email Service**: Gmail integration for contact forms and notifications
3. **HR Information**: Contact form handling and resume services
4. **Admin Interface**: Question management and system administration
5. **Twitter Integration**: Social media features (rizzle package)

### Database Configuration
- Uses custom Hibernate configuration (excludes Spring Boot JPA auto-config)
- Requires `.env` file in `app/` directory with PostgreSQL credentials
- Database schema in `app/src/main/resources/db.sql`

### Environment Setup
- Requires `app/.env` file with database and email configuration
- Uses environment variable injection in startup script
- PostgreSQL database required for full functionality

### Code Formatting
- Spotless Maven plugin enforces Google Java Format
- Automatically formats code on build
- Must run `./mvnw spotless:apply` before commits