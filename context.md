# Project Context

## Overview

This is a software developer portfolio application designed to showcase projects, professional experience, and technical skills. The application supports bilingual content (English and Polish) and features a comprehensive admin panel for content management.

**Live at:** [https://jbytow.pl](https://jbytow.pl)

## Architecture

### Backend (Spring Boot)

The backend follows a layered architecture:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic layer
- **Repositories**: Data access layer using Spring Data JPA
- **Entities**: JPA entities mapped to PostgreSQL tables
- **DTOs**: Data transfer objects for API communication

Key components:
- **OAuth2 Authentication**: Supports Google login
- **JWT Tokens**: Stateless authentication for API requests
- **Flyway**: Database migrations management (13+ migrations)
- **JPA Converters**: Custom converters for enum types (Category, MediaType)

### Frontend (React + TypeScript)

The frontend is a single-page application with:

- **React Router**: Client-side routing
- **React Query (TanStack Query v5)**: Server state management and caching
- **Context API**: Authentication and language state
- **i18next**: Internationalization
- **TipTap**: Rich text editor for content creation
- **Tailwind CSS**: Utility-first styling
- **Framer Motion**: Animations
- **dnd-kit**: Drag-and-drop functionality

### Database Schema

Main entities:
- **posts**: Content for personal and professional projects
- **experiences**: Work experience entries with achievements (JSONB)
- **soft_skills**: Skills with category reference
- **skill_categories**: Customizable skill groupings
- **interests**: Personal interests with up to 3 images each
- **media**: Uploaded files (images, videos, PDFs)
- **site_settings**: Global site configuration (singleton)
- **users**: OAuth user accounts

### Content Categories

Posts are organized by category:
1. `PERSONAL_PROJECT` - Personal projects and side work
2. `PROFESSIONAL_PROJECT` - Professional/client projects

Separate entities for:
- **Experiences** - Professional work history
- **Soft Skills** - Skills organized by categories
- **Interests** - Personal interests (displayed on About page)

## Key Design Decisions

### Bilingual Content
All user-facing text fields have `_en` and `_pl` suffixes (e.g., `title_en`, `title_pl`). Entities have locale-aware getter methods:
```java
public String getTitle(String locale) {
    return "pl".equalsIgnoreCase(locale) ? titlePl : titleEn;
}
```

### PostgreSQL Enums → VARCHAR
Originally used PostgreSQL native enums for `category` and `media_type`. Converted to VARCHAR with JPA Converters for better Hibernate compatibility and easier maintenance.

### JSONB for Flexible Data
Used for complex/array fields:
- `achievements_en`, `achievements_pl` in experiences
- `stats_items` in site_settings
- `social_links` in site_settings

### OAuth-Only Authentication
No traditional username/password login. Admin access requires:
1. OAuth login (Google)
2. Email must be in the `ADMIN_EMAILS` whitelist

### Stateless JWT
After OAuth login, a JWT token is issued. All subsequent API requests use this token. No server-side sessions.

### Media Storage
Files are stored on the filesystem (configurable via `UPLOAD_PATH`). In production, uses Docker volume mount for persistence.

### Drag-and-Drop Reordering
All list-based content (experiences, skills, interests, posts) supports drag-and-drop reordering with:
- Optimistic UI updates
- Server sync on drag end
- `displayOrder` field for persistence

## Development Notes

### Running Locally
The project uses Docker Compose for local development. The frontend Dockerfile includes nginx configuration that proxies `/oauth2/*` and `/login/oauth2/*` to the backend for OAuth flow.

### Running Tests
```bash
cd backend
.\mvnw.cmd test                    # Windows
./mvnw test                        # Linux/Mac
```

Unit tests cover:
- Service layer (PostService, ExperienceService, InterestService, SkillCategoryService)
- Controller layer (PostController, AdminPostController)
- Security (JwtService)
- Converters (CategoryConverter, MediaTypeConverter)

### Adding New Content Entities
1. Create entity class with bilingual fields and locale getter
2. Create repository with custom queries if needed
3. Create DTOs (main, create request, update request)
4. Create service with CRUD operations
5. Create controllers (public + admin)
6. Add Flyway migration for database table
7. Update frontend types, API services, query keys
8. Create admin components (list, form)
9. Add routes to App.tsx
10. Add translations to en.json and pl.json

### Media URL Resolution
The `getMediaUrl` utility handles different URL formats:
- Full URLs (http/https) - returned as-is
- Relative paths - prefixed with API base URL
- Handles both dev (localhost) and production environments

## Security Considerations

- JWT secret should be at least 32 characters in production
- OAuth credentials should never be committed
- `ADMIN_EMAILS` controls who can access the admin panel
- CORS is configured to allow only the frontend origin
- File uploads are validated by MIME type

## Production Deployment

Current production setup:
1. **Proxmox VE** - Hypervisor running on home server
2. **Ubuntu Server VM** - Container for the application
3. **Docker Compose** - Orchestration with `docker-compose.prod.yml`
4. **NGINX** - Reverse proxy handling SSL termination
5. **Cloudflare Tunnel** - Secure public access without exposed ports
6. **PostgreSQL** - Running in Docker container with volume persistence
7. **Media uploads** - Stored in Docker volume mount

Deployment steps:
1. Push to GitHub
2. SSH into server
3. Pull latest changes
4. Run `docker-compose -f docker-compose.prod.yml up --build -d`
5. Verify with `docker-compose logs -f`

### Environment Configuration
- Use `.env` file for secrets (not committed)
- Configure OAuth redirect URIs for production domain
- Set proper `JWT_SECRET` (min 32 characters)
- Configure `ADMIN_EMAILS` for admin access
