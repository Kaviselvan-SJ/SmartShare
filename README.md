# SmartShare — Intelligent File Compression & Secure Link Sharing Platform

<div align="center">

![SmartShare](https://img.shields.io/badge/SmartShare-v2.0.0-blue?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/Neon_PostgreSQL-Serverless-4169E1?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Upstash_Redis-Serverless-DC382D?style=for-the-badge&logo=redis)
![AWS S3](https://img.shields.io/badge/AWS_S3-Object_Storage-FF9900?style=for-the-badge&logo=amazon-s3)
![Firebase](https://img.shields.io/badge/Firebase-Auth-FFCA28?style=for-the-badge&logo=firebase)
![Render](https://img.shields.io/badge/Render-Backend-46E3B7?style=for-the-badge&logo=render)
![Vercel](https://img.shields.io/badge/Vercel-Frontend-000000?style=for-the-badge&logo=vercel)

**SmartShare** is a production-ready, full-stack file management platform that lets users securely upload, compress, deduplicate, version, and share files via protected short links — with real-time analytics, rate limiting, role-based access control, and a fully featured user profile system.

**Live:** `https://smartshareio.vercel.app` | **API:** `https://smartshare-backend.onrender.com`

</div>

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Local Development](#-local-development)
- [Production Deployment](#-production-deployment)
  - [Backend on Render](#1-backend-on-render)
  - [Frontend on Vercel](#2-frontend-on-vercel)
  - [Environment Variables Reference](#3-environment-variables-reference)
- [Route Map](#-route-map)
- [API Reference](#-api-reference)
- [Security Model](#-security-model)
- [Admin Access](#-admin-access)
- [Project Structure](#-project-structure)

---

## ✨ Features

### 🔐 Authentication & Security
- **Email/Password Login & Registration** via Firebase Authentication
- **Google OAuth Sign-In / Sign-Up** — one-click login with Google; auto-creates a user profile on first login
- **Firebase ID token verification** on every protected API request (server-side via Firebase Admin SDK)
- **Role-based routing** — Admin users redirected to the Admin Dashboard; regular users access the standard workspace
- **Route guards** at both the frontend (React) and backend (Spring Security) layers

---

### 📁 File Management
- **Smart Upload** — files are compressed and deduplicated automatically before storage
- **File Name Conflict Detection** — detects if a file with the same name exists, prompting a version decision
- **My Files** — grid/list view of all uploaded files with download, share, delete, and preview actions
- **File Preview** — in-browser preview for images (PNG, JPEG, GIF, WebP) and PDFs via a tabbed detail panel
- **File Download** — direct download from the file details panel and search results
- **File Deletion** — full cascade cleanup: removes the file record, all short links, all analytics events, the S3 object (if no other references exist), and Redis cache entries

---

### 🗂️ File Versioning
- **Upload New Version** — re-upload a file under the same name to create a new version, keeping full history
- **Version History Panel** — view all past versions with timestamps and size info
- **Switch Active Version** — promote any historical version back to "current"; short link caches auto-invalidate
- **Replace Mode** — mark the previous version as replaced when uploading a new one
- **Deduplication Across Versions** — if a new version has identical content (same SHA-256), the existing S3 object is reused

---

### 🗜️ Compression & Deduplication
- **GZIP Compression** via a pluggable `CompressionStrategy` pattern (extensible to other codecs)
- **Content-aware strategy selection** — factory selects the optimal strategy based on file extension
- **SHA-256 Content Hashing** — every file is hashed on upload; existing hash = no re-upload
- **Per-file compression metrics** — original vs. compressed size tracked and displayed in the UI
- **System-wide Bandwidth Savings** — aggregated deduplication and compression savings on the dashboard

---

### 🔗 Short Link Sharing
- **Generate secure short links** for any file with a single click
- **Password protection** — protect a link with a password; transmitted via `X-Download-Password` header, never in the URL
- **Download limits** — set a maximum number of downloads; link auto-deactivates once reached
- **Expiry dates** — expired links show an "Expired" message to the recipient
- **Link status tracking** — `ACTIVE`, `EXPIRED`, `LIMIT_REACHED`, `PASSWORD_PROTECTED`
- **Copy link** — one-click clipboard copy of the short URL
- **Delete links** — remove any short link, with cascade deletion of its analytics
- **Redis-cached resolution** — sub-millisecond lookup; cache invalidated on version switch or deletion

---

### 🔍 Unified Search
- **Search by file name** — partial, case-insensitive match
- **Search by tag** — match against auto-generated content tags
- **Combined results** — single query checks both, merging and deduplicating results
- **Global search bar** — navigates directly to results from the navbar
- **Search result actions** — download, share, or delete files from the results grid

---

### 🏷️ Auto-Tagging
- **Automatic tag generation** on every upload and version switch
- **Tag-based discovery** — browse files by tag from the Search page
- **User tag summary** — all tags across your files, sorted by usage frequency
- **Popular tags** — system-wide leaderboard in the Admin Dashboard

---

### 📊 Analytics & Insights
- **Dashboard** — overview cards: total files, total downloads, total bandwidth saved
- **File Detail Analytics** (`/files/:id`) — per-file breakdown: download history, short link statuses, compression stats, tags, preview
- **Top Downloads** (`/analytics`) — ranked list of most-downloaded files
- **Bandwidth Savings** (`/bandwidth`) — personal and system-wide compression and deduplication metrics
- **Device & Browser Analytics** — events record device type (Desktop/Mobile/Tablet) and browser

---

### 🛡️ Rate Limiting & Abuse Protection
- **Redis-backed Sliding Window Rate Limiter** — distributed-safe, accurate per-window counting
- **Protected endpoints:**

| Action | Limit | Window |
|---|---|---|
| File downloads (by IP) | 20 requests | 60 seconds |
| File uploads (by user) | 10 requests | 60 seconds |
| Authentication attempts (by IP) | 15 requests | 10 minutes |
| Password-protected link attempts (by IP) | 5 attempts | 10 minutes |

- **HTTP 429 responses** with `Retry-After` header on limit exceeded
- **Automatic lockout clearing** on successful password authentication

---

### 👤 User Profile & Settings
- **Account Settings page** (`/settings`) — accessible from the navbar avatar dropdown
- **Basic profile**: Display Name, Profile Image URL
- **Professional details**: Organization, Location, Bio, Job Profile (dropdown), Experience Level
- **Social links**: LinkedIn, GitHub, Portfolio URL (server-side URL prefix validation)
- **App preferences**: Language, Timezone, Default Link Expiry, Email Notifications toggle
- **Live navbar update** — display name and avatar update instantly without a page reload

---

### 🔓 Secure Public File Access
- Short links are publicly accessible at `/f/:shortCode` — **no login required** for recipients
- Graceful UI states for: password-protected, download limit reached, expired, not found
- Password entry form with retry support for protected links
- Auto-downloads on page load for public (non-password) links

---

### 🧑‍💼 Admin Dashboard
- **System overview** — total users, total files, total downloads, total storage used
- **Upload trends** — daily upload count chart for the past 7 days
- **Top uploaders** — ranked list of most active users
- **Popular tags** — system-wide top-20 tags by usage
- **Recent activity log** — last 10 download events (privacy-safe)
- **Active users** — unique users active in the last 24 hours

---

### 🏥 Health Check
```
GET /api/health
→ { "status": "UP" }
```
Used by Render for deployment health monitoring.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                  Client (Browser / Vercel)                      │
│           React 18 + Vite 5 + TailwindCSS 3                     │
│      Firebase Auth SDK  │  Axios  │  Recharts                   │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS / REST
┌──────────────────────────────▼──────────────────────────────────┐
│              Spring Boot 3.2 (Java 21) — Render                 │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────────┐   │
│  │ Controllers │  │   Services   │  │ Security / Firebase   │   │
│  │ (REST API)  │  │ (Business    │  │ Token Verification    │   │
│  │             │  │  Logic)      │  │ Rate Limit Intercept  │   │
│  └──────┬──────┘  └──────┬───────┘  └───────────────────────┘   │
│         │                │                                      │
│  ┌──────▼────────────────▼───────────────────────────────────┐  │
│  │             Data / Infrastructure Layer                   │  │
│  │  Neon PostgreSQL │  Upstash Redis  │  AWS S3              │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow — File Upload

```
User selects file
       │
       ▼
1. Check for filename conflict (version prompt if exists)
       │
       ▼
2. SHA-256 hash of file content (deduplication check)
       │
       ├── Duplicate found → reuse existing S3 object
       │
       └── New file → GZIP compress → upload to AWS S3
               │
               ▼
3. Save FileEntity + FileGroupEntity to Neon PostgreSQL
       │
       ▼
4. Auto-generate tags → save to tags table
       │
       ▼
5. Return upload metadata to frontend
```

### Data Flow — Short Link Download

```
User opens /f/:shortCode (frontend)
       │
       ▼
FileAccess.jsx auto-triggers download attempt
       │
       ├── 200 OK → blob stream → browser download ✅
       │
       ├── 401 → shows password entry form → retry with X-Download-Password header
       │
       ├── 404 + "limit" → shows "Download Limit Reached" card
       │
       └── 404 + "expir" → shows "Link Expired" card
```

---

## 🧰 Tech Stack

| Layer | Technology | Version / Service |
|---|---|---|
| **Backend Framework** | Spring Boot | 3.2.5 |
| **Language** | Java | 21 |
| **Database** | Neon PostgreSQL (serverless) | — |
| **Cache / Rate Limiting** | Upstash Redis (serverless, TLS) | — |
| **Object Storage** | AWS S3 (SDK v2) | — |
| **Authentication** | Firebase Authentication | — |
| **Frontend Framework** | React | 18 |
| **Build Tool** | Vite | 5 |
| **Styling** | TailwindCSS | 3 |
| **Charts** | Recharts | — |
| **HTTP Client** | Axios | — |
| **Icons** | Lucide React | — |
| **Backend Hosting** | Render | — |
| **Frontend Hosting** | Vercel | — |

---

## 💻 Local Development

### Prerequisites

- Java 21+
- Node.js 18+ and npm
- Docker & Docker Compose
- A Firebase project with **Email/Password** and **Google** sign-in enabled
- An AWS account with an S3 bucket

### 1. Infrastructure (Docker)

Start PostgreSQL and Redis locally:

```bash
cd infrastructure
docker-compose up -d
```

| Service | Port |
|---|---|
| PostgreSQL | `5432` |
| Redis | `6379` |

### 2. Backend Setup

#### Firebase Service Account (local dev)

1. Go to **Firebase Console → Project Settings → Service Accounts**
2. Click **Generate new private key** and download the JSON
3. Place it at `backend/src/main/resources/firebase-service-account.json`

#### Environment Variables

Create `backend/.env`:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/smartshare
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_SSL=false

# AWS S3
AWS_S3_BUCKET_NAME=your-bucket-name
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1

# Firebase
FIREBASE_PROJECT_ID=your-firebase-project-id
# Leave FIREBASE_SERVICE_ACCOUNT_JSON blank to use the local JSON file


```

> **Note:** The `spring-dotenv` library automatically loads `.env` on startup — no manual export needed.

#### Run

```bash
cd backend

# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

API available at **`http://localhost:8080`**.

### 3. Frontend Setup

Create `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api

# Firebase — from Firebase Console → Project Settings → Your Apps
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_project.firebasestorage.app
VITE_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id

# Comma-separated admin emails
VITE_ADMIN_EMAILS=admin@yourdomain.com
```

```bash
cd frontend
npm install
npm run dev
```

App available at **`http://localhost:5173`**.

---

## 🚀 Production Deployment

### 1. Backend on Render

1. **Connect** your GitHub repo to Render → **New Web Service**
2. **Root directory:** `backend`
3. **Runtime:** Docker (uses the `Dockerfile` in `backend/`)
4. **Health check path:** `/api/health`
5. **Set environment variables** (see table below)

Render automatically injects `PORT` — no action needed.

### 2. Frontend on Vercel

1. **Connect** your GitHub repo to Vercel → **New Project**
2. **Root directory:** `frontend`
3. **Framework:** Vite (auto-detected)
4. **Set environment variables** (see table below)

> `frontend/vercel.json` configures SPA routing so page refreshes and direct URL access work correctly on all React Router routes.

### 3. Environment Variables Reference

#### Backend (Render)

| Variable | Description | Example |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Must be `prod` | `prod` |
| `DB_URL` | Full Neon JDBC URL | `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require` |
| `DB_USERNAME` | Neon DB username | `neondb_owner` |
| `DB_PASSWORD` | Neon DB password | `...` |
| `REDIS_HOST` | Upstash Redis endpoint | `xxx.upstash.io` |
| `REDIS_PORT` | Upstash Redis port (TLS) | `6380` |
| `REDIS_PASSWORD` | Upstash Redis password | `...` |
| `REDIS_SSL` | Enable TLS | `true` |
| `AWS_ACCESS_KEY_ID` | AWS IAM access key | `AKIA...` |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM secret key | `...` |
| `AWS_REGION` | S3 bucket region | `ap-south-2` |
| `AWS_S3_BUCKET_NAME` | S3 bucket name | `smartshare-storage` |
| `FIREBASE_PROJECT_ID` | Firebase project ID | `my-project-abc` |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Full contents of `firebase-service-account.json` | `{"type":"service_account",...}` |
| `FRONTEND_URL` | Deployed frontend URL (used in short link generation + CORS) | `https://smartshare.vercel.app` |
| `ADMIN_EMAILS` | Comma-separated admin emails | `admin@example.com` |

#### Frontend (Vercel)

| Variable | Description | Example |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API base URL | `https://smartshare-backend.onrender.com/api` |
| `VITE_FIREBASE_API_KEY` | Firebase web API key | `AIza...` |
| `VITE_FIREBASE_AUTH_DOMAIN` | Firebase auth domain | `project.firebaseapp.com` |
| `VITE_FIREBASE_PROJECT_ID` | Firebase project ID | `my-project-abc` |
| `VITE_FIREBASE_STORAGE_BUCKET` | Firebase storage bucket | `project.firebasestorage.app` |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | Firebase sender ID | `123456789` |
| `VITE_FIREBASE_APP_ID` | Firebase app ID | `1:123:web:abc` |
| `VITE_ADMIN_EMAILS` | Comma-separated admin emails | `admin@example.com` |

---

## 🗺️ Route Map

| Path | Access | Description |
|---|---|---|
| `/login` | Public | Email/Password & Google login |
| `/register` | Public | Email/Password & Google sign-up |
| `/f/:shortCode` | Public | Secure file download page (password, limit, expiry UI) |
| `/dashboard` | Auth | Overview stats and recent activity |
| `/upload` | Auth | Upload a new file (with duplicate detection) |
| `/files` | Auth | My files — grid view with actions |
| `/files/:fileId` | Auth | File details: preview, analytics, versions, links |
| `/search` | Auth | Search files by name or tag |
| `/analytics` | Auth | Top downloaded files leaderboard |
| `/bandwidth` | Auth | Compression & deduplication savings |
| `/settings` | Auth | Account settings and user profile |
| `/admin` | Admin only | System admin dashboard |

---

## 📦 API Reference

### Authentication
All protected endpoints require:
```
Authorization: Bearer <Firebase ID Token>
```

### Files

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/files/upload` | Upload a new file (creates v1) |
| `GET` | `/api/files/my-files` | List all current-version files for the authenticated user |
| `GET` | `/api/files/:id/details` | Full file analytics and metadata |
| `GET` | `/api/files/:id/preview` | Stream the file content (binary) |
| `DELETE` | `/api/files/:id` | Delete a file and all related data |
| `POST` | `/api/files/:id/check-duplicate` | Check if a filename already exists |

### File Versioning

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/files/:groupId/versions` | Upload a new version to an existing file group |
| `GET` | `/api/files/:groupId/versions` | List all versions of a file group |
| `PUT` | `/api/files/:groupId/versions/:versionId/activate` | Switch the active version |

### Short Links

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/shortlinks/create` | Create a short link for a file |
| `GET` | `/api/shortlinks/file/:fileId` | Get all short links for a file |
| `DELETE` | `/api/shortlinks/:shortCode` | Delete a short link |
| `GET` | `/f/:shortCode` | Resolve and stream file via short link (public) |

### Tags & Search

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tags/search/unified?q={keyword}` | Search by file name **and** tag (combined, deduped) |
| `GET` | `/api/tags/search/{tag}` | Search files by a single tag |
| `GET` | `/api/tags/user` | Get all tags for the authenticated user's files |
| `GET` | `/api/tags/popular` | Get system-wide popular tags |

### User Profile

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/user/profile` | Get the authenticated user's profile |
| `PUT` | `/api/user/profile` | Update the authenticated user's profile |

### Analytics

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/analytics/dashboard/overview` | Dashboard summary stats |
| `GET` | `/api/analytics/bandwidth/user` | Personal bandwidth savings |
| `GET` | `/api/analytics/bandwidth/system` | System-wide bandwidth savings (public) |
| `GET` | `/api/analytics/top-downloads` | Top downloaded files |

### Admin (Admin-only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/overview` | Full system telemetry |

### Health

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Health check — `{"status":"UP"}` |

---

## 🔒 Security Model

| Concern | Implementation |
|---|---|
| **Token Verification** | Firebase Admin SDK verifies every ID token server-side |
| **User Auto-Provisioning** | Backend auto-creates a `UserEntity` on first authenticated request |
| **Admin Authorization** | Admin endpoints verify the user's email against a server-side whitelist (`ADMIN_EMAILS`) |
| **Short Link Passwords** | Transmitted via `X-Download-Password` header — never in the URL or logs |
| **Rate Limiting** | Redis sliding-window limiter protects uploads, downloads, auth, and password attempts |
| **URL Validation** | LinkedIn, GitHub, and Portfolio URLs validated server-side for correct prefixes |
| **CORS** | Configured to allow only `FRONTEND_URL` (+ `localhost` for dev) |
| **Production Profile** | Test endpoints (`/api/public/test/**`) are disabled via `@Profile("!prod")` |
| **Route Guards** | Frontend `ProtectedRoute` and `AdminRoute` components prevent unauthorized navigation |
| **Non-root Docker** | Production container runs as a non-root OS user |

---

## 🧑‍💼 Admin Access

Set admin emails in the backend env:
```env
ADMIN_EMAILS=admin@example.com,owner@example.com
```
And in the frontend env:
```env
VITE_ADMIN_EMAILS=admin@example.com,owner@example.com
```

- Admin users are automatically redirected to `/admin` on login
- Admin users cannot access regular user routes
- The Admin Dashboard exposes system-wide telemetry without revealing private user data

---

## 📂 Project Structure

```
SmartShare/
├── infrastructure/
│   └── docker-compose.yml          # Local PostgreSQL + Redis
│
├── backend/                        # Spring Boot application
│   ├── Dockerfile                  # Multi-stage JDK 21 → JRE 21 Alpine
│   ├── .dockerignore
│   ├── .env.example                # All required env vars documented
│   └── src/main/java/com/smartshare/
│       ├── controller/
│       │   ├── admin/              # Admin dashboard endpoints
│       │   ├── analytics/          # Analytics endpoints
│       │   ├── auth/               # Auth endpoints
│       │   ├── download/           # Short link resolution + streaming
│       │   ├── file/               # File management endpoints
│       │   ├── health/             # GET /api/health
│       │   ├── shortlink/          # Short link CRUD
│       │   ├── tagging/            # Tag & unified search
│       │   └── user/               # User profile endpoints
│       ├── service/
│       │   ├── compression/        # GZIP strategy + factory pattern
│       │   ├── deduplication/      # SHA-256 hash dedup
│       │   ├── download/           # Download validation + streaming
│       │   ├── file/               # File CRUD, versioning, preview
│       │   ├── ratelimit/          # Sliding window rate limiter
│       │   ├── shortlink/          # Short link lifecycle
│       │   ├── storage/            # AWS S3 storage service (SDK v2)
│       │   ├── tagging/            # Auto-tag generation & search
│       │   └── upload/             # Upload orchestration
│       ├── security/
│       │   ├── firebase/           # Firebase token filter
│       │   └── ratelimit/          # Redis sliding window limiter
│       ├── model/
│       │   ├── entity/             # JPA entities (File, FileGroup, User, Tag, ShortLink…)
│       │   └── dto/                # Request/Response DTOs
│       ├── repository/             # Spring Data JPA repositories
│       └── config/
│           ├── firebase/           # FirebaseInitializer (env-var + classpath fallback)
│           ├── redis/              # RedisConfig (Upstash TLS support)
│           ├── s3/                 # S3Config (AWS SDK v2)
│           └── SecurityConfig.java # CORS, auth filter chain
│
└── frontend/                       # React + Vite application
    ├── vercel.json                 # SPA rewrite — all routes → index.html
    └── src/
        ├── api/                    # Axios client with auth interceptor
        ├── auth/                   # Firebase config & auth helpers
        ├── components/
        │   ├── layout/             # AppLayout, Navbar, Sidebar
        │   └── ui/                 # FileCard, Loader, ShortLinkModal…
        ├── context/                # AuthContext (Firebase state)
        ├── hooks/                  # Custom React hooks
        ├── pages/
        │   ├── Dashboard.jsx       # Overview stats
        │   ├── Upload.jsx          # File upload with duplicate detection
        │   ├── MyFiles.jsx         # File grid with actions
        │   ├── FileDetails.jsx     # Per-file analytics, versions, links, preview
        │   ├── TagSearch.jsx       # Unified name + tag search
        │   ├── Analytics.jsx       # Top downloads leaderboard
        │   ├── BandwidthSavings.jsx# Compression savings metrics
        │   ├── Settings.jsx        # User profile & preferences
        │   ├── AdminDashboard.jsx  # Admin system overview
        │   ├── FileAccess.jsx      # Public short link download page (state machine UI)
        │   ├── Login.jsx           # Login page
        │   └── Register.jsx        # Registration page
        └── routes/                 # ProtectedRoute, AdminRoute, AdminRouteGuard
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "feat: add my feature"`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

<div align="center">
Built with ❤️ using Spring Boot, React, AWS S3, and Firebase.
</div>
