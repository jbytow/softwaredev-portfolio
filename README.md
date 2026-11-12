# Software Developer Portfolio App

A full-stack bilingual (EN/PL) developer portfolio application designed to showcase projects, professional experience, and technical skills. Built for software developers who want a modern, customizable online presence with a powerful admin panel for content management.

The application features a clean, responsive design with smooth animations, comprehensive admin controls for all content sections, drag-and-drop reordering, media management, and full internationalization support. It demonstrates modern full-stack development practices including OAuth authentication, RESTful API design, and containerized deployment.

**Live:** [https://jbytow.pl](https://jbytow.pl)

## Features

- **Bilingual Support**: Full English and Polish language support with seamless switching, for the public site and the admin panel
- **OAuth Authentication**: Secure login via Google, restricted to an allowlist of admin e-mails
- **Comprehensive Admin Panel**:
  - Manage posts, experiences, skills, interests, achievements and RPG stats
  - Drag-and-drop reordering with rollback when the server rejects the change
  - Rich text editor (TipTap) for content creation, sharing one extension list with the public renderer
  - Media library with image/video/PDF uploads and YouTube links, paged by 100
  - Draft preview rendered with the same component as the public post page
  - Customizable site settings (hero, stats, footer, SEO, RPG class title)
  - Session-expired dialog that keeps unsaved forms, and a warning before leaving a dirty form
- **Content Sections**:
  - About Me with editable tags and interests gallery
  - Professional Experience timeline with achievements
  - Projects (personal & professional) with case studies
  - Skills organized by customizable categories
  - Contact form with a honeypot field and per-visitor rate limiting
- **Modern UI/UX**:
  - Responsive mobile-first design, keyboard-accessible cards, lightbox and carousel
  - Framer Motion animations that respect `prefers-reduced-motion`
  - Seven colour presets painted before React starts (no flash of the default theme), plus an optional RPG "Nerd Mode"
  - Self-hosted fonts, a 404 page, per-page error boundaries and load errors with a retry button
  - YouTube embeds load only after a click (privacy-friendly `youtube-nocookie` player)

## Built With

### Backend
- Java 21
- Spring Boot 4.1
- Spring Security with OAuth2 (Google)
- PostgreSQL 16
- Flyway migrations
- JWT authentication (jjwt)

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
- Vitest + Testing Library

### Deployment Infrastructure
- **Proxmox VE** - Hypervisor for virtualization
- **Ubuntu Server** - Container VM operating system
- **Docker + Docker Compose** - Containerization and orchestration
- **NGINX** - Reverse proxy listening on the loopback interface only, with per-visitor rate limiting
- **Cloudflare Tunnel** - Secure public access without exposed ports (`cloudflared` runs on the same VM)

## Tests and Quality Checks

The backend has around 500 tests (as of 2026-11-12; the number changes as the code does):

- Service tests on JUnit 5 and Mockito
- Controller tests on `@WebMvcTest`, including request validation for every admin endpoint
- `SecurityConfigTest`, which drives the real Spring Security chain (401 for anonymous API calls, 403 without the admin role or with a foreign `Origin`)
- Database tests on Testcontainers with PostgreSQL 16: `PostRepositoryTest` and `MigrationSchemaTest`, the latter running the Flyway migrations and validating the resulting schema against the entities
- `JsonWireFormatTest`, which pins the JSON shape of the public responses

Every push and pull request runs the GitHub Actions workflow in `.github/workflows/ci.yml`: backend tests with PostgreSQL on Testcontainers, frontend typecheck, lint, Vitest and build, and `nginx -t` on the edge config. Dependabot (`.github/dependabot.yml`) opens weekly grouped update PRs for Maven, npm, the Dockerfiles and the Actions.

Database tests need a working Docker daemon. On a machine where Docker runs inside WSL, point the tests at it:

```bash
export DOCKER_HOST=tcp://127.0.0.1:2375
export TESTCONTAINERS_RYUK_DISABLED=true
```

**Run backend tests:**
```bash
cd backend
.\mvnw.cmd test                    # Windows
./mvnw test                        # Linux/Mac
```

The frontend has around 320 Vitest tests with Testing Library on jsdom. They cover i18n key parity between EN and PL (and keys that no component consumes), the API client, the rich text renderer and editor, the media components, the theme bootstrap script and its CSP hash, cache invalidation after admin mutations, and accessibility of the public pages. Lint allows no warnings.

**Run frontend checks:**
```bash
cd frontend
npm run typecheck
npm run lint
npm test
npm run build
```

## Configuration Highlights

### Environment-Based Configuration
- Separate profiles for development and production
- Externalized configuration via environment variables; the production compose file refuses to start without the required ones
- Secure secrets management (JWT keys, OAuth credentials); the backend refuses to start with a JWT secret shorter than 32 bytes or equal to the development default

### Database
- Flyway migrations (`V1`..`V8`) for version-controlled schema changes, validated against the entities on startup (`ddl-auto: validate`)
- JSONB columns for flexible data (achievements, stats, social links)
- Automatic timestamps with `@PrePersist` and `@PreUpdate`

### Security
- JWT-based stateless authentication in an HttpOnly cookie
- OAuth2 integration with Google; only verified e-mails on the admin allowlist get the admin role, re-checked on every request
- Anonymous API calls get 401 instead of a redirect to Google; admin writes with a foreign `Origin` or `Referer` get 403
- Client errors answer with a uniform JSON envelope and one log line, never a stack trace
- CORS configuration for frontend-backend communication; a Content-Security-Policy in report-only mode at the edge

### Media Handling
- File uploads stored on disk under random UUID names, with one MIME map deciding extension, type and subdirectory; paths are confined to the upload directory
- Support for images, videos, PDFs, and YouTube embeds (watch, shorts, live, embed, youtu.be and nocookie links)
- Uploaded files are served with a one-year immutable cache header; deleting a post deletes its files

### Internationalization
- Locale-aware API responses based on `Accept-Language` header; public post responses carry only the requested language
- Dual-column storage for all translatable content (EN/PL)
- Frontend i18next integration with language context; a test keeps EN and PL keys in sync

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
- Node.js 24+ (for local frontend development)
- Java 21+ (for local backend development)

### Quick Start with Docker

1. Clone the repository:
```bash
git clone https://github.com/jbytow/softwaredev-portfolio.git
cd softwaredev-portfolio
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
docker compose up --build
```

5. Access the application (the development compose publishes its ports on `127.0.0.1` only):
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
│   │   ├── config/          # Security, exception handling
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── security/        # JWT, OAuth2 handler, origin check
│   │   ├── service/         # Business logic
│   │   └── util/            # Locale and slug helpers
│   ├── src/test/java/       # Unit, controller, security and database tests
│   └── src/main/resources/
│       ├── db/migration/    # Flyway migrations
│       └── application.yml  # Configuration
├── frontend/
│   ├── src/
│   │   ├── admin/           # Admin panel pages, forms, hooks and components
│   │   ├── components/      # Shared components
│   │   ├── contexts/        # React contexts (auth, language, theme, nerd mode)
│   │   ├── hooks/           # Shared hooks
│   │   ├── i18n/            # Translations (en.json, pl.json) and the key parity test
│   │   ├── lib/             # Utilities (queryKeys, mediaUrl, apiError, rich text extensions)
│   │   ├── pages/           # Public pages
│   │   ├── services/        # API client
│   │   ├── styles/          # Global styles and self-hosted fonts
│   │   ├── test/            # Vitest setup and cross-cutting tests
│   │   └── types/           # TypeScript types
│   └── nginx.conf           # Nginx configuration of the frontend container
├── nginx/                   # Edge nginx config (rate limits, headers, CSP)
├── docs/                    # API reference and operations runbook
├── .github/                 # CI workflow and Dependabot
├── docker-compose.yml       # Development setup
└── docker-compose.prod.yml  # Production setup
```

## Environment Variables

The production compose file refuses to start without `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `ADMIN_EMAILS` and `FRONTEND_URL`. Optional entries are `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CONTACT_EMAIL` and `VITE_API_URL`.

The full list with defaults lives in [`.env.example`](.env.example). Startup checks and per-variable behaviour are in [docs/operations.md](docs/operations.md).

## API Endpoints

The full REST reference (public and admin endpoints, authentication, response envelope, error codes, removed routes) lives in [docs/api.md](docs/api.md).

## Documentation

- [docs/api.md](docs/api.md): REST reference (public and admin endpoints, authentication, response envelope, error codes).
- [docs/operations.md](docs/operations.md): production runbook (deploying backend, frontend and edge changes, verification, periodic tasks, backups, restore).
- `context.md`: append-only project journal.

## Project Status

The public site and the admin panel work end to end (Google login, bilingual content, media library, RPG mode). The November 2026 audit, ported from the twin marketing-portfolio project, hardened authentication, error handling, uploads, the edge proxy and the dependency set, upgraded the backend to Spring Boot 4.1, translated the admin panel, and added database, security and frontend test suites with CI. Open operational items (backups schedule, database role without superuser, switching the CSP from report-only to enforcing) are listed in [docs/operations.md](docs/operations.md).

## License

MIT
