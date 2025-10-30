# OCI Deployment - Quiz Website

Comprehensive deployment of the Spring Boot 3 quiz website from Render.com to Oracle Cloud Infrastructure (OCI) using Always Free tier resources.

## Project Structure

```
quiz-website/
├── specs/                           # Planning and specification documents
│   ├── README.md                    # This file - project overview
│   ├── 01-verify-prerequisites-plan.md
│   ├── 02-create-infrastructure-plan.md
│   ├── 03-deploy-application-plan.md
│   ├── 04-validate-deployment-plan.md
│   └── [supporting documents...]
├── oci-deployment/                  # Implementation files
│   ├── scripts/                    # Executable deployment scripts
│   │   ├── 01-verify-prerequisites.sh
│   │   ├── 02-create-infrastructure.sh
│   │   ├── fix-subnet-public-ip.sh
│   │   └── [future scripts...]
│   ├── configs/                    # Configuration files
│   │   ├── cloud-init.yaml        # Instance initialization
│   │   └── docker-compose.yml     # Container orchestration
│   └── docs/                      # Implementation documentation
│       ├── deployment-checklist.md  # Detailed progress tracking
│       └── instance-details.md     # Current instance information
└── .env                           # Environment variables (secure)
```

## Deployment Strategy

**Remote Build Approach**: Docker image built directly on OCI compute instance (no local Docker required)
- **Database**: PostgreSQL container co-located with application
- **Registry**: Oracle Container Image Registry (OCIR)
- **Compute**: VM.Standard.A1.Flex (ARM-based, 2 OCPUs, 4GB RAM)
- **Network**: Load Balancer subnet with public IP support
- **Cost**: $0.00 (OCI Always Free tier)

## Quick Start

### Prerequisites
- OCI CLI installed and configured
- SSH key pair generated
- Environment variables configured in `.env` file
- **Note**: Docker NOT required locally

### Deployment Steps

1. **Verify Prerequisites**
   ```bash
   cd oci-deployment/scripts
   ./01-verify-prerequisites.sh
   ```

2. **Create Infrastructure**
   ```bash
   ./02-create-infrastructure.sh
   ```

3. **Deploy Application** (Planned)
   ```bash
   ./03-deploy-application.sh
   ```

4. **Validate Deployment** (Planned)
   ```bash
   ./04-validate-deployment.sh
   ```

## Application Architecture

### Technology Stack
- **Framework**: Spring Boot 3.1.4 with Java 17
- **Database**: PostgreSQL with Hibernate ORM (custom configuration)
- **Frontend**: Thymeleaf templates with HTML/CSS/JavaScript
- **Email**: Gmail SMTP integration for contact forms
- **Build**: Maven with Spotless code formatting (Google Java Format)
- **Connection Pool**: C3P0 for database connection management

### Data Models
- **Question**: Quiz questions with multiple-choice options, difficulty levels, categories, and approval workflow
- **QuizSubmission**: User quiz attempts with scores and timestamps
- **ContactQuery**: Contact form submissions
- **HRInfo**: HR/recruitment information management

## 🎯 Notable Features

✅ **Question Approval Workflow** - Admin can APPROVE/REJECT/EDIT questions before publishing
✅ **Score Comparison** - Users see if they're above/below average after quiz completion
✅ **Leaderboard System** - Top performers displayed on home page with score rankings
✅ **Email Integration** - Gmail SMTP for contact form notifications and queries
✅ **14 Question Categories** - GENERAL, HISTORY, FINANCE, SPORTS, SCIENCE_AND_TECHNOLOGY, ENGINEERING, ENTERTAINMENT, GEOGRAPHY, LITERATURE, FOOD_AND_CUISINE, NATURE_AND_WILDLIFE, MYTHOLOGY_AND_RELIGION, POLITICS, MUSIC
✅ **Difficulty Levels** - Questions categorized by difficulty (EASY, MEDIUM, HARD)
✅ **Random Question Selection** - Dynamic quiz generation from database
✅ **Admin Dashboard** - Question management interface with filtering and CRUD operations
✅ **Flash Messages** - POST-redirect-GET pattern with success/error notifications
✅ **Validation** - Jakarta validation on all entities and form submissions
✅ **Code Quality** - Automated formatting with Spotless Maven plugin
✅ **Responsive Design** - Thymeleaf templates with mobile-friendly layout

## Application Routes

### Public-Facing Routes (QuizController)

| Method | Route | Description | Handler |
|--------|-------|-------------|---------|
| GET | `/`, `/home`, `/index` | Home page with leaderboard | `QuizController.java:26` |
| GET | `/about` | About page | `QuizController.java:38` |
| GET | `/services` | Services page | `QuizController.java:43` |
| GET | `/quiz` | Quiz taking page with random questions | `QuizController.java:48` |
| POST | `/submit-quiz` | Process quiz submission | `QuizController.java:54` |
| GET | `/result` | Display quiz results and score comparison | `QuizController.java:73` |
| GET | `/shop` | Shop/store page | `QuizController.java:92` |
| GET | `/contact` | Contact form page | `QuizController.java:97` |
| POST | `/submit-contact` | Submit contact query (sends email) | `QuizController.java:102` |
| GET | `/leaderboard` | Full leaderboard page | `QuizController.java:119` |

### Administrative Routes (AdminController)

| Method | Route | Description | Handler |
|--------|-------|-------------|---------|
| GET | `/admin` | Admin dashboard with question listing | `AdminController.java:36` |
| GET | `/admin?approvalLevel=PENDING` | Filter questions by approval status | `AdminController.java:36` |
| POST | `/change-category` | Change question approval level | `AdminController.java:44` |
| GET | `/add-question` | Add new or edit existing question | `AdminController.java:69` |
| POST | `/submit-add-question` | Save question to database | `AdminController.java:76` |

### Application Flow Examples

**Quiz Taking Flow:**
```
1. GET /quiz → Display random questions from database
2. User answers questions → JavaScript collects responses
3. POST /submit-quiz → Server processes answers (redirect with flash attributes)
4. GET /result → Calculate score, compare with average, display results + leaderboard
```

**Admin Question Management:**
```
1. GET /admin → View all questions (default view)
2. GET /admin?approvalLevel=PENDING → Filter pending questions
3. Click "Edit" on question → GET /add-question (with questionId)
4. Modify question details → POST /submit-add-question
5. Redirect to /admin with success message
```

**Contact Form Flow:**
```
1. GET /contact → Display contact form
2. User fills form (name, email, phone, message)
3. POST /submit-contact → Send email via Gmail SMTP
4. Redirect to /home with flash message: "Thank you for contacting..."
```

## Current Project Status

- **Prerequisites**: ✅ Complete (OCI access verified, tools installed)
- **Infrastructure**: ✅ Complete (instance fully functional, all verification steps passed)
- **Application Deployment**: ✅ **COMPLETE** (all 6 phases successful, application live)
- **Validation**: 🎯 **READY TO PROCEED** (application deployed and operational)

🌐 **Live Application**: http://161.118.188.237:8080 ✅ **OPERATIONAL** (configurable via QUIZ_APP_URL env var)

📋 **Detailed Status**: See `oci-deployment/docs/deployment-checklist.md` for phase-by-phase completion tracking.

🖥️ **Infrastructure Details**: See `oci-deployment/docs/instance-details.md` for complete instance specifications, network configuration, and verification results.

## Script-Plan Organization

Each deployment script has a corresponding planning document:

| Script | Plan Document | Status | Description |
|--------|---------------|--------|-------------|
| `01-verify-prerequisites.sh` | `01-verify-prerequisites-plan.md` | ✅ Complete | OCI credentials and tools verification |
| `02-create-infrastructure.sh` | `02-create-infrastructure-plan.md` | ✅ Complete | OCI compute instance creation |
| `03-deploy-application.sh` | `03-deploy-application-plan.md` | ✅ Complete | Application deployment with Docker |
| `04-validate-deployment.sh` | `04-validate-deployment-plan.md` | 📋 Planned | Deployment validation and testing |
| `05-add-questions-automated.js` | `05-automated-question-addition-plan.md` | ✅ Complete | Automated question addition via API |

## Supporting Documentation

| Document | Purpose | Status |
|----------|---------|--------|
| `oci-migration-overview.md` | High-level migration strategy | ✅ Complete |
| `fix-subnet-public-ip-plan.md` | Subnet issue resolution (obsolete) | ✅ Resolved |

## Key Technical Decisions

- **Container Strategy**: Co-located PostgreSQL container (no OCI Database service)
- **Network Solution**: Using Load Balancer subnet for public IP capability
- **Build Strategy**: Remote Docker build on OCI instance
- **Registry**: OCIR (no Docker Hub access constraint)
- **Architecture**: ARM-based compute for optimal free tier utilization

## Resource Usage (Free Tier)

- **Compute**: 1 instance (4/6 used), 2 OCPUs (2/4 used), 4GB RAM (4/24 used)
- **Storage**: Boot volume within free limits
- **Network**: 1 public IP, existing VCN infrastructure
- **Cost**: $0.00 monthly

## Usage Guidelines

### For Implementation
1. Read the plan document first to understand the strategy
2. Execute the corresponding script
3. Refer back to the plan for troubleshooting

### For Documentation
- Each plan document references its implementation script
- Each script references its corresponding plan document
- Cross-references maintained for complete traceability

## File Naming Convention

- **Scripts**: `##-action-name.sh` (e.g., `01-verify-prerequisites.sh`)
- **Plans**: `##-action-name-plan.md` (e.g., `01-verify-prerequisites-plan.md`)
- **Supporting**: `descriptive-name.md` (e.g., `oci-migration-overview.md`)

This organization ensures clear linkage between planning and implementation phases.