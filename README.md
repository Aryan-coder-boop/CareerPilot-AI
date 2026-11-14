# CareerPilot AI — Platform Architecture & Implementation

> **Professional AI-Powered Career and Technical Interview Preparation Platform**  
> *Phase 1 Implementation: Modular Monolith Foundation, Spring Security with JWT & Refresh Token Rotation, PostgreSQL 16 + pgvector Docker Environment, and React + Vite + Tailwind CSS Authentication & Dashboard.*

---

## 1. Overview & Vision

**CareerPilot AI** is an intelligent career acceleration platform designed to help software engineers bridge technical skill gaps, optimize resumes for real job descriptions, and master interviews through realistic AI-driven mock interviews and customized learning roadmaps.

### Phase 1 & 2 Implementation Scope
- **Backend**: Completely independent Spring Boot 3.3.x service in Java 17 with Maven, Spring Data JPA, Spring Security 6, stateless JWT authentication, refresh token rotation, Flyway database migrations, Apache PDFBox text extraction, and Google Gemini AI integration with heuristic fallback.
- **Data Layer**: Docker Compose deployment of **PostgreSQL 16** with native **`pgvector`** extension support, healthchecks, and persistent volume storage.
- **Frontend**: Clean Single Page Application (SPA) built with **React**, **Vite**, and **Tailwind CSS**, featuring responsive Login, Registration, and comprehensive AI Resume Analyzer with drag-and-drop upload, 4-pillar quality scoring, actionable suggestions, and past resume history.
- **Security & Guardrails**: BCrypt password hashing, stateless token verification, 15-minute access tokens with 7-day rotated refresh tokens, strict PDF magic-byte validation, 5 MB file size limit, rate limiting (max 5 uploads per 10 mins per user), and strict non-hallucination prompt delimiters.

---

## 2. Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17, Spring Boot 3.3.2, Spring Security 6, Spring Data JPA, Hibernate, Flyway, Apache PDFBox 3.0.2 |
| **Authentication** | Stateless JWT (JJWT 0.12.5), BCrypt Password Hashing, Refresh Token Rotation |
| **AI Integration** | Google Gemini API (`gemini-2.5-flash`), Schema-Enforced JSON, Heuristic Offline Fallback |
| **Database** | PostgreSQL 16, `pgvector` extension (HNSW vector indexing ready), Docker |
| **Frontend** | React 18, Vite 5, Tailwind CSS 3, Axios (with 401 retry interceptor), React Router v6 |

---

## 3. Directory Structure

```
CareerPilot-AI/
├── .env.example                     # Environment variable blueprint
├── .gitignore                       # Multi-tier gitignore (Java, Node, Docker)
├── ARCHITECTURE.md                  # Comprehensive architectural blueprint (preserved)
├── README.md                        # Project documentation & run guide
├── docker-compose.yml               # PostgreSQL 16 with pgvector & persistent storage
│
├── careerpilot-backend/             # Java 17 / Spring Boot 3 Modular Monolith
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd              # Maven wrapper (v3.9.9)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/careerpilot/
│   │   │   │   ├── CareerPilotApplication.java
│   │   │   │   ├── common/
│   │   │   │   │   ├── config/      # SecurityConfig, CorsConfig
│   │   │   │   │   ├── exception/   # GlobalExceptionHandler, Custom Exceptions
│   │   │   │   │   ├── payload/     # ApiResponse<T>, ErrorDetails
│   │   │   │   │   └── security/    # JwtTokenProvider, JwtAuthenticationFilter, UserPrincipal
│   │   │   │   └── modules/auth/
│   │   │   │       ├── controller/  # AuthController
│   │   │   │       ├── dto/         # RegisterRequest, LoginRequest, AuthResponse, etc.
│   │   │   │       ├── entity/      # User, Role, RefreshToken
│   │   │   │       ├── repository/  # UserRepository, RefreshTokenRepository
│   │   │   │       └── service/     # AuthService
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/    # V1__init_schema.sql (Postgres + pgvector)
│   │   └── test/                    # JUnit 5 & MockMvc Authentication Test Suite
│
└── careerpilot-frontend/            # React + Vite + Tailwind CSS Client
    ├── package.json
    ├── vite.config.js               # Dev server with /api proxy to Spring Boot
    ├── tailwind.config.js
    ├── postcss.config.js
    ├── index.html
    └── src/
        ├── context/AuthContext.jsx  # Global auth state & session synchronization
        ├── services/apiClient.js    # Axios client with auto-refresh queue interceptor
        ├── services/authService.js  # Auth API endpoints wrapper
        ├── components/
        │   ├── layout/              # Navbar, Footer
        │   └── ui/                  # Alert, Spinner
        ├── features/
        │   ├── auth/                # LoginForm, RegisterForm, LoginPage, RegisterPage
        │   ├── dashboard/           # DashboardPage (Session info, status indicators)
        │   └── landing/             # LandingPage
        └── routes/                  # AppRoutes, ProtectedRoute
```

---

## 4. Getting Started

### 4.1 Prerequisites
- **JDK 17** (e.g. Eclipse Adoptium Temurin 17)
- **Node.js** (v20+ or v24) and **npm**
- **Docker** and **Docker Compose** (for PostgreSQL with `pgvector`)

### 4.2 Start the Database (Docker Compose)
In the project root, run:
```bash
docker compose up -d
```
This spins up a container running PostgreSQL 16 with the native `pgvector` extension enabled, exposed on port `5432`.

### 4.3 Configure Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Default connection parameters:
- **Host**: `localhost:5432`
- **Database**: `careerpilot_db`
- **User**: `careerpilot`
- **Password**: `secret_password`

### 4.4 Run the Backend Service
Navigate to `careerpilot-backend` and launch using the bundled Maven wrapper:

**On Windows (PowerShell / CMD):**
```powershell
cd careerpilot-backend
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
cd careerpilot-backend
./mvnw spring-boot:run
```

The server will automatically run Flyway migrations (`V1__init_schema.sql`) and start on **`http://localhost:8080`**.

### 4.5 Run the Frontend Client
In a separate terminal, navigate to `careerpilot-frontend`:
```bash
cd careerpilot-frontend
npm install
npm run dev
```
Open **`http://localhost:5173`** in your browser.

---

## 5. REST API Documentation (Phase 1 Auth)

All API responses follow the unified envelope:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-10-31T18:30:00Z"
}
```

### 5.1 Register Account
- **Endpoint**: `POST /api/v1/auth/register`
- **Body**:
  ```json
  {
    "fullName": "Aryan Developer",
    "email": "developer@careerpilot.ai",
    "password": "SecurePassword123!"
  }
  ```
- **Response**: `201 Created` with access token, refresh token, and user profile.

### 5.2 Login
- **Endpoint**: `POST /api/v1/auth/login`
- **Body**:
  ```json
  {
    "email": "developer@careerpilot.ai",
    "password": "SecurePassword123!"
  }
  ```
- **Response**: `200 OK` with fresh tokens.

### 5.3 Refresh Access Token
- **Endpoint**: `POST /api/v1/auth/refresh`
- **Body**:
  ```json
  {
    "refreshToken": "your-refresh-token-uuid"
  }
  ```
- **Response**: `200 OK` with rotated access and refresh tokens.

### 5.4 Get Current User Profile
- **Endpoint**: `GET /api/v1/auth/me`
- **Headers**: `Authorization: Bearer <accessToken>`
- **Response**: `200 OK` with user details.

---

## 6. Resume Analyzer REST APIs (Phase 2)

All resume endpoints require `Authorization: Bearer <accessToken>`.

### 6.1 Upload & Analyze Resume
- **Endpoint**: `POST /api/v1/resumes/upload`
- **Content-Type**: `multipart/form-data`
- **Form Field**: `file` (PDF file, max 5 MB)
- **Response**: `201 Created` with structured analysis, 4-pillar quality breakdown, and suggestions.

### 6.2 List Resumes
- **Endpoint**: `GET /api/v1/resumes`
- **Response**: `200 OK` with array of past uploaded resume summaries.

### 6.3 Get Resume Detail
- **Endpoint**: `GET /api/v1/resumes/{id}`
- **Response**: `200 OK` with full parsed sections and score report (ensures user-specific isolation).

### 6.4 Delete Resume
- **Endpoint**: `DELETE /api/v1/resumes/{id}`
- **Response**: `200 OK` (removes database record and local disk file).

---

## 7. Testing & Verification

Run the automated backend test suite:
```powershell
cd careerpilot-backend
.\mvnw.cmd test
```

### Verified Test Results (23 Tests Passing):
- `com.careerpilot.auth.AuthControllerTest`: 3 tests passed
- `com.careerpilot.auth.AuthServiceTest`: 2 tests passed
- `com.careerpilot.security.JwtTokenProviderTest`: 3 tests passed
- `com.careerpilot.resume.PdfValidatorTest`: 4 tests passed (valid PDF, non-PDF extension, invalid magic bytes, file > 5 MB)
- `com.careerpilot.resume.PdfTextExtractorServiceTest`: 2 tests passed (selectable text extraction, insufficient selectable text rejection)
- `com.careerpilot.resume.ResumeServiceTest`: 4 tests passed (rate limiting, upload & persistence, user isolation, file & DB deletion)
- `com.careerpilot.resume.ResumeControllerTest`: 3 tests passed (listing, detail retrieval, deletion)
- `com.careerpilot.resume.GeminiResumeAnalyzerServiceTest`: 2 tests passed (structured heuristic extraction, non-hallucination verification)

