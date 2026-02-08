# Software Developer Portfolio App

A full-stack bilingual (EN/PL) developer portfolio application designed to showcase projects, professional experience, and technical skills. Built for software developers who want a modern, customizable online presence with a powerful admin panel for content management.

The application features a clean, responsive design with smooth animations, comprehensive admin controls for all content sections, drag-and-drop reordering, media management, and full internationalization support. It demonstrates modern full-stack development practices including OAuth authentication, RESTful API design, and containerized deployment.

**Live:** [https://jbytow.pl](https://jbytow.pl)

## Features

- **Bilingual Support**: Full English and Polish language support with seamless switching
- **OAuth Authentication**: Secure login via Google
- **Comprehensive Admin Panel**:
  - Manage posts, experiences, skills, and interests
  - Drag-and-drop reordering for all content sections
  - Rich text editor (TipTap) for content creation
  - Media library with image/video/PDF uploads
  - Customizable site settings (hero, stats, footer, SEO)
- **Content Sections**:
  - About Me with editable tags and interests gallery
  - Professional Experience timeline with achievements
  - Projects (personal & professional) with case studies
  - Skills organized by customizable categories
  - Contact form
- **Modern UI/UX**:
  - Responsive mobile-first design
  - Framer Motion animations
  - Dark theme with gradient accents

## Built With

### Backend
- Java 21
- Spring Boot 3.2
- Spring Security with OAuth2 (Google)
- PostgreSQL 16
- Flyway migrations
- JWT authentication

### Frontend
- React 18
- TypeScript
- Vite
- Tailwind CSS
- React Query (TanStack Query v5)
- Framer Motion
- i18next (internationalization)
- TipTap (rich text editor)
- dnd-kit (drag and drop)

### Deployment Infrastructure
- **Proxmox VE** - Hypervisor for virtualization
- **Ubuntu Server** - Container VM operating system
- **Docker + Docker Compose** - Containerization and orchestration
- **NGINX** - Reverse proxy and static file serving
- **Cloudflare Tunnel** - Secure public access without exposed ports

## Test Coverage

The project includes comprehensive unit tests for backend services:

```
Tests run: 125, Failures: 0, Errors: 0
```

**Test Breakdown:**
- `PostServiceTest` - 22 tests (CRUD, publishing, slug generation)
- `ExperienceServiceTest` - 14 tests (CRUD, locale handling)
- `InterestServiceTest` - 12 tests (CRUD, ordering)
- `SkillCategoryServiceTest` - 14 tests (CRUD, nested skills)
- `PostControllerTest` - 9 tests (REST endpoints, localization)
- `AdminPostControllerTest` - 8 tests (authenticated endpoints)
- `JwtServiceTest` - 16 tests (token generation, validation)
- Converter tests - 30 tests (Category, MediaType converters)

**Run tests:**
```bash
cd backend
.\mvnw.cmd test                    # Windows
./mvnw test                        # Linux/Mac
```

## Configuration Highlights

### Environment-Based Configuration
- Separate profiles for development and production
- Externalized configuration via environment variables
- Secure secrets management (JWT keys, OAuth credentials)

### Database
- Flyway migrations for version-controlled schema changes
- JSONB columns for flexible data (achievements, stats, social links)
- Automatic timestamps with `@PrePersist` and `@PreUpdate`

### Security
- JWT-based stateless authentication
- OAuth2 integration with Google and GitHub
- Role-based access control (admin email whitelist)
- CORS configuration for frontend-backend communication

### Media Handling
- File uploads stored on disk with configurable path
- Support for images, videos, PDFs, and YouTube embeds
- Media URL resolution for both dev and production environments

### Internationalization
- Locale-aware API responses based on `Accept-Language` header
- Dual-column storage for all translatable content (EN/PL)
- Frontend i18next integration with language context

## Challenges During Development

### 1. React Query v5 Migration
The project upgraded from React Query v4 to v5, which removed the `onSuccess` callback from `useQuery`. Required refactoring to use `useEffect` for state synchronization with query data.

### 2. OAuth2 Session Management
Implementing stateless JWT authentication while maintaining OAuth2 login flow required careful coordination between Spring Security's OAuth2 client and custom JWT generation.

### 3. Bilingual Content Architecture
Designing database schema and DTOs to efficiently handle dual-language content while keeping the API clean. Solution: locale-aware getter methods in entities and service-level DTO mapping.

### 4. Media URL Resolution
Handling different media URL formats between development (localhost) and production (domain) environments. Implemented a `getMediaUrl` utility function that correctly resolves paths based on environment.

### 5. Drag-and-Drop with Server Sync
Implementing optimistic UI updates for drag-and-drop reordering while maintaining data consistency with the backend. Used local state with server sync on drag end.

### 6. Cloudflare Tunnel Integration
Configuring secure public access through Cloudflare Tunnel while maintaining proper HTTPS termination and WebSocket support for development hot reload.

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Node.js 20+ (for local frontend development)
- Java 21+ (for local backend development)

### Quick Start with Docker

1. Clone the repository:
```bash
git clone https://github.com/jbytow/softwareDevPortfolioApp.git
cd softwareDevPortfolioApp
```

2. Create environment file:
```bash
cp .env.example .env
```

3. Configure OAuth credentials in `.env` (optional, for admin access):
```
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
ADMIN_EMAILS=your_email@example.com
```

4. Start the application:
```bash
docker-compose up --build
```

5. Access the application:
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080/api
   - Admin Panel: http://localhost:5173/admin

### Local Development

#### Backend
```bash
cd backend
.\mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Linux/Mac
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Project Structure

```
├── backend/
│   ├── src/main/java/com/portfolio/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── security/        # Security & JWT
│   │   └── service/         # Business logic
│   ├── src/test/java/       # Unit tests
│   └── src/main/resources/
│       ├── db/migration/    # Flyway migrations
│       └── application.yml  # Configuration
├── frontend/
│   ├── src/
│   │   ├── admin/           # Admin panel components
│   │   ├── components/      # Shared components
│   │   ├── contexts/        # React contexts
│   │   ├── i18n/            # Translations (en.json, pl.json)
│   │   ├── lib/             # Utilities (queryKeys, mediaUrl)
│   │   ├── pages/           # Public pages
│   │   ├── services/        # API services
│   │   └── types/           # TypeScript types
│   └── nginx.conf           # Nginx configuration
├── nginx/                   # Production nginx config
├── docker-compose.yml       # Development setup
└── docker-compose.prod.yml  # Production setup
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | portfolio |
| `POSTGRES_USER` | Database user | portfolio |
| `POSTGRES_PASSWORD` | Database password | portfolio123 |
| `JWT_SECRET` | JWT signing key | (dev key) |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | - |
| `GOOGLE_CLIENT_SECRET` | Google OAuth secret | - |
| `ADMIN_EMAILS` | Comma-separated admin emails | admin@example.com |

## API Endpoints

### Public
- `GET /api/posts` - List published posts
- `GET /api/posts/{slug}` - Get post by slug
- `GET /api/categories` - List categories
- `GET /api/settings` - Get site settings
- `GET /api/experiences` - List experiences
- `GET /api/interests` - List interests
- `GET /api/skill-categories/with-skills` - Skills grouped by category

### Admin (requires authentication)
- `GET/POST/PUT/DELETE /api/admin/posts` - Manage posts
- `GET/POST/PUT/DELETE /api/admin/experiences` - Manage experiences
- `GET/POST/PUT/DELETE /api/admin/interests` - Manage interests
- `GET/POST/PUT/DELETE /api/admin/skill-categories` - Manage skill categories
- `GET/POST/PUT/DELETE /api/admin/soft-skills` - Manage skills
- `GET/POST /api/admin/media` - Manage media
- `GET/PUT /api/admin/settings` - Manage settings

## License

MIT
