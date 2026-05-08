# SmartShare — Intelligent File Compression & Secure Link Sharing Platform

<div align="center">

![SmartShare](https://img.shields.io/badge/SmartShare-v1.0.0-blue?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=for-the-badge&logo=redis)
![MinIO](https://img.shields.io/badge/MinIO-Object_Storage-C72E49?style=for-the-badge&logo=minio)
![Firebase](https://img.shields.io/badge/Firebase-Auth-FFCA28?style=for-the-badge&logo=firebase)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)

**SmartShare** is a production-ready, full-stack file management platform that lets users securely upload, compress, deduplicate, version, and share files via protected short links — with real-time analytics, rate limiting, role-based access control, and a fully featured user profile system.

</div>

---

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
  - [1. Infrastructure](#1-infrastructure-docker)
  - [2. Backend Setup](#2-backend-setup-spring-boot)
  - [3. Frontend Setup](#3-frontend-setup-react--vite)
- [Route Map](#-route-map)
- [API Reference](#-api-reference)
- [Security Model](#-security-model)
- [Admin Access](#-admin-access)
- [Project Structure](#-project-structure)

---

## ✨ Features

### 🔐 Authentication & Security
- **Email/Password Login & Registration** via Firebase Authentication
- **Google OAuth Sign-In / Sign-Up** — one-click login with Google account; auto-creates a user profile on first login
- **Firebase ID token verification** on every protected API request (server-side)
- **Role-based routing** — Admin users are automatically redirected to the Admin Dashboard; regular users access the standard workspace
- **Route guards** at both the frontend (React) and backend (Spring Security) layers

---

### 📁 File Management
- **Smart Upload** — files are compressed and deduplicated automatically before storage
- **File Name Conflict Detection** — detects if a file with the same name already exists before upload, prompting a version decision
- **My Files** — grid/list view of all uploaded files with download, share, delete, and preview actions
- **File Preview** — in-browser preview for images (PNG, JPEG, GIF, WebP) and PDFs via a tabbed detail panel
- **File Download** — direct download from the file details panel and the search results page
- **File Deletion** — full cascade cleanup: removes the file record, all short links, all analytics events, the MinIO object (if no other references exist), and Redis cache entries

---

### 🗂️ File Versioning
- **Upload New Version** — re-upload a file under the same name to create a new version, keeping the full version history
- **Version History Panel** — view all past versions of a file, with upload timestamps and size information
- **Switch Active Version** — promote any historical version back to be the "current" version; short link caches are automatically invalidated
- **Replace Mode** — optionally mark the previous version as replaced when uploading a new one
- **Deduplication Across Versions** — if a new version has identical content (same SHA-256 hash), the existing physical object is reused — no duplicate storage

---

### 🗜️ Compression & Deduplication
- **GZIP Compression** via a pluggable `CompressionStrategy` pattern (extensible to other codecs)
- **Content-aware strategy selection** — the factory selects an optimal strategy based on file extension (`.txt`, `.json`, `.log`, etc.)
- **SHA-256 Content Hashing** — every file is hashed on upload; if the hash already exists in storage, the file bytes are not re-uploaded
- **Per-file compression metrics** — original size vs. compressed size tracked per version and displayed in the UI
- **System-wide Bandwidth Savings** — aggregated deduplication and compression savings shown on the dashboard

---

### 🔗 Short Link Sharing
- **Generate secure short links** for any file with a single click
- **Password protection** — optionally protect a link with a password; password is transmitted via `X-Download-Password` header, never in the URL
- **Download limits** — set a maximum number of downloads per link; the link is automatically deactivated once reached
- **Expiry dates** — set a link expiry date; expired links gracefully display an "expired" message to the recipient
- **Link status tracking** — each link shows its status: `ACTIVE`, `EXPIRED`, `LIMIT_REACHED`, or `PASSWORD_PROTECTED`
- **Copy link** — one-click clipboard copy of the short URL
- **Delete links** — remove any short link individually, with confirmation warning that its analytics will also be deleted
- **Redis-cached resolution** — short link lookups are cached in Redis for sub-millisecond resolution; cache is invalidated on file version switch or deletion

---

### 🔍 Unified Search
- **Search by file name** — partial, case-insensitive match against file names
- **Search by tag** — match against auto-generated content tags
- **Combined results** — a single search query checks both file names and tags, merging and deduplicating results so no file appears twice
- **Search from anywhere** — the global navbar search bar navigates directly to search results on Enter
- **Search result actions** — download, share, or delete files directly from the search results grid

---

### 🏷️ Auto-Tagging
- **Automatic metadata tag generation** on every file upload and version switch
- **Tag-based discovery** — browse files by tag from the Search page
- **Tag analytics** — per-file tag list visible in the File Details panel
- **User tag summary** — see all tags across your files, sorted by usage frequency
- **Popular tags** — system-wide tag leaderboard visible in the Admin Dashboard

---

### 📊 Analytics & Insights
- **Dashboard** — overview cards for total files, total downloads, and total bandwidth saved
- **File Detail Analytics** (`/files/:id`) — per-file breakdown of:
  - Download count and recent download history
  - All associated short links and their statuses
  - Compression ratio and size savings
  - Auto-generated content tags
  - File preview
- **Top Downloads** (`/analytics`) — ranked list of most-downloaded files
- **Bandwidth Savings** (`/bandwidth`) — personal and system-wide storage metrics showing compression and deduplication gains
- **Device & Browser Analytics** — download events record client device type (Desktop/Mobile/Tablet) and browser (Chrome, Firefox, Safari, Edge, Opera)

---

### 🛡️ Rate Limiting & Abuse Protection
- **Redis-backed Sliding Window Rate Limiter** — distributed-system-safe, accurate per-window counting
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
- **App preferences**: Language, Timezone, Default Link Expiry duration, Email Notifications toggle
- **Live navbar update** — display name and avatar reflect profile changes instantly without a page reload

---

### 🔓 Secure Public File Access
- Short links are publicly accessible at `/f/:shortCode` — no login required for recipients
- Graceful error handling for expired, limit-reached, and password-protected links
- Password entry UI for password-protected links
- Download triggers a streamed binary response directly from MinIO via the backend

---

### 🧑‍💼 Admin Dashboard
- **System overview** — total users, total files, total downloads, total storage used
- **Upload trends** — daily upload count chart for the past 7 days
- **Top uploaders** — ranked list of most active users (privacy-safe: no PII beyond email)
- **Popular tags** — system-wide top-20 tags by usage
- **Recent activity log** — last 10 download events across the platform (privacy-safe)
- **Active users** — count of unique users active in the last 24 hours

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client (Browser)                         │
│              React 18 + Vite 5 + TailwindCSS 3                  │
│         Firebase Auth SDK  │  Axios  │  Recharts                │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS / REST
┌──────────────────────────────▼──────────────────────────────────┐
│                   Spring Boot 3.2 (Java 21)                     │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  Controllers│  │   Services   │  │  Security / Firebase  │   │
│  │  (REST API) │  │ (Business    │  │  Token Verification   │   │
│  │             │  │  Logic)      │  │  Rate Limit Intercept │   │
│  └──────┬──────┘  └──────┬───────┘  └──────────────────────┘   │
│         │                │                                       │
│  ┌──────▼────────────────▼──────────────────────────────────┐   │
│  │             Data / Infrastructure Layer                   │   │
│  │  PostgreSQL │  Redis Cache  │  MinIO Object Storage       │   │
│  └───────────────────────────────────────────────────────────┘   │
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
       ├── Duplicate found → reuse existing storage object
       │
       └── New file → GZIP compress → upload to MinIO
               │
               ▼
3. Save FileEntity + FileGroupEntity to PostgreSQL
       │
       ▼
4. Auto-generate tags → save to tags table
       │
       ▼
5. Return upload metadata to frontend
```

---

## 🧰 Tech Stack

| Layer | Technology | Version |
|---|---|---|
| **Backend Framework** | Spring Boot | 3.2.5 |
| **Language** | Java | 21 |
| **Database** | PostgreSQL | 15 |
| **Cache / Rate Limiting** | Redis | 7 |
| **Object Storage** | MinIO | Latest |
| **Authentication** | Firebase Authentication | — |
| **Frontend Framework** | React | 18 |
| **Build Tool** | Vite | 5 |
| **Styling** | TailwindCSS | 3 |
| **Charts** | Recharts | — |
| **HTTP Client** | Axios | — |
| **Icons** | Lucide React | — |
| **Infrastructure** | Docker Compose | — |

---

## ⚙️ Prerequisites

- **Java 21+**
- **Node.js 18+** and npm
- **Docker & Docker Compose**
- A **Firebase project** with **Email/Password** and **Google** sign-in providers enabled

---

## 🚀 Getting Started

### 1. Infrastructure (Docker)

Start PostgreSQL, Redis, and MinIO with Docker Compose:

```bash
cd infrastructure
docker-compose up -d
```

| Service | Port |
|---|---|
| PostgreSQL | `5432` |
| Redis | `6379` |
| MinIO API | `9000` |
| MinIO Console | `9001` |

> **MinIO Console:** Open `http://localhost:9001` and log in with `minioadmin` / `minioadmin` to browse stored files.

---

### 2. Backend Setup (Spring Boot)

#### Firebase Service Account

1. Go to **Firebase Console → Project Settings → Service Accounts**
2. Click **Generate new private key** and download the JSON file
3. Rename it to `firebase-service-account.json` and place it at:
   ```
   backend/src/main/resources/firebase-service-account.json
   ```

#### Environment Configuration

Create `backend/src/main/resources/application-dev.properties` or set environment variables:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/smartshare
spring.datasource.username=postgres
spring.datasource.password=postgres

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# MinIO
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=smartshare

# Firebase
firebase.service-account.path=classpath:firebase-service-account.json
```

#### Run the Backend

```bash
cd backend

# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The API will be available at **`http://localhost:8080`**.

> The backend uses **Spring Boot Docker Compose integration** — if Docker is running, it can auto-start infrastructure services.

---

### 3. Frontend Setup (React + Vite)

#### Firebase Console Setup

In the Firebase Console, enable the following under **Authentication → Sign-in method**:
- ✅ Email/Password
- ✅ Google

#### Environment Variables

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

# Comma-separated list of admin email addresses
VITE_ADMIN_EMAILS=admin@yourdomain.com
```

#### Install & Run

```bash
cd frontend
npm install
npm run dev
```

The app will be available at **`http://localhost:5173`**.

---

## 🗺️ Route Map

| Path | Access | Description |
|---|---|---|
| `/login` | Public | Email/Password & Google login |
| `/register` | Public | Email/Password & Google sign-up |
| `/f/:shortCode` | Public | Secure file download via short link |
| `/dashboard` | Auth | Overview stats, recent activity cards |
| `/upload` | Auth | Upload a new file (with duplicate detection) |
| `/files` | Auth | My uploaded files — grid view with actions |
| `/files/:fileId` | Auth | File details: preview, analytics, versions, links |
| `/search` | Auth | Search files by name or tag |
| `/analytics` | Auth | Top downloaded files leaderboard |
| `/bandwidth` | Auth | Compression & deduplication savings |
| `/settings` | Auth | Account settings and user profile |
| `/admin` | Admin only | System admin dashboard |

---

## 📦 API Reference

### Authentication
All protected endpoints require the header:
```
Authorization: Bearer <Firebase ID Token>
```

### Files

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/files/my-files` | List all current-version files for the authenticated user |
| `GET` | `/api/files/:id/details` | Full file analytics and metadata |
| `GET` | `/api/files/:id/preview` | Stream the file content (binary) |
| `DELETE` | `/api/files/:id` | Delete a file and all related data |
| `POST` | `/api/files/:id/check-duplicate` | Check if a filename already exists |

### File Versioning

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/files/upload` | Upload a new file (creates v1) |
| `POST` | `/api/files/:groupId/versions` | Upload a new version to an existing file group |
| `GET` | `/api/files/:groupId/versions` | List all versions of a file group |
| `PUT` | `/api/files/:groupId/versions/:versionId/activate` | Switch the active version |

### Short Links

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/shortlinks` | Create a short link for a file |
| `GET` | `/api/shortlinks/file/:fileId` | Get all short links for a file |
| `DELETE` | `/api/shortlinks/:id` | Delete a short link |
| `GET` | `/f/:shortCode` | Resolve and download via short link (public) |

### Tags & Search

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tags/search/unified?q={keyword}` | Search by file name **and** tag (combined) |
| `GET` | `/api/tags/search/{tag}` | Search files by a single tag |
| `GET` | `/api/tags/search?tags={tag1,tag2}` | Search files by multiple tags (intersection) |
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
| `GET` | `/api/analytics/bandwidth/system` | System-wide bandwidth savings |
| `GET` | `/api/analytics/top-downloads` | Top downloaded files |

### Admin (Admin-only)

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/overview` | Full system telemetry |

---

## 🔒 Security Model

| Concern | Implementation |
|---|---|
| **Token Verification** | Firebase Admin SDK verifies every ID token server-side |
| **User Auto-Provisioning** | Backend auto-creates a `UserEntity` on first authenticated request |
| **Admin Authorization** | Admin endpoints verify the user's email against a server-side whitelist |
| **Short Link Passwords** | Transmitted via `X-Download-Password` header — never in the URL or logs |
| **Rate Limiting** | Redis sliding-window limiter protects uploads, downloads, auth, and password attempts |
| **URL Validation** | LinkedIn, GitHub, and Portfolio URLs validated server-side for correct prefixes |
| **CORS** | Configured to allow only the frontend origin |
| **Route Guards** | Frontend `ProtectedRoute` and `AdminRoute` components prevent unauthorized navigation |

---

## 🧑‍💼 Admin Access

Set one or more admin emails in `frontend/.env`:

```env
VITE_ADMIN_EMAILS=admin@example.com,owner@example.com
```

- Admin users are automatically redirected to `/admin` on login
- Admin users cannot access regular user routes (`/dashboard`, `/files`, etc.)
- The Admin Dashboard shows system-wide telemetry without exposing private user data

---

## 📂 Project Structure

```
SmartShare/
├── infrastructure/
│   └── docker-compose.yml          # PostgreSQL, Redis, MinIO
│
├── backend/                        # Spring Boot application
│   └── src/main/java/com/smartshare/
│       ├── controller/             # REST controllers
│       │   ├── admin/              # Admin dashboard endpoints
│       │   ├── analytics/          # Analytics endpoints
│       │   ├── auth/               # Auth endpoints
│       │   ├── download/           # Short link resolution
│       │   ├── file/               # File management endpoints
│       │   ├── shortlink/          # Short link CRUD
│       │   ├── tagging/search/     # Tag & unified search
│       │   └── user/               # User profile endpoints
│       ├── service/
│       │   ├── compression/        # GZIP strategy + factory
│       │   ├── deduplication/      # SHA-256 hash dedup
│       │   ├── file/               # File CRUD, versioning, preview
│       │   ├── ratelimit/          # Rate limit service (download/upload/auth/password)
│       │   ├── shortlink/          # Short link lifecycle
│       │   ├── storage/            # MinIO object storage
│       │   ├── tagging/            # Auto-tag generation & search
│       │   └── upload/             # Upload orchestration
│       ├── security/
│       │   ├── firebase/           # Firebase token filter & AuthenticatedUser
│       │   └── ratelimit/          # SlidingWindowRateLimiter (Redis)
│       ├── model/
│       │   ├── entity/             # JPA entities (File, FileGroup, User, Tag, ShortLink, …)
│       │   └── dto/                # Request/Response DTOs
│       ├── repository/             # Spring Data JPA repositories
│       └── config/                 # SecurityConfig, MinioConfig, RedisConfig, DatabaseFixRunner
│
└── frontend/                       # React + Vite application
    └── src/
        ├── api/                    # Axios client with auth interceptor
        ├── auth/                   # Firebase config & auth helpers
        ├── components/
        │   ├── layout/             # AppLayout, Navbar, Sidebar
        │   └── ui/                 # FileCard, Loader, ShortLinkModal, …
        ├── context/                # AuthContext (Firebase state)
        ├── hooks/                  # Custom React hooks
        ├── pages/
        │   ├── Dashboard.jsx       # Overview stats
        │   ├── Upload.jsx          # File upload with duplicate detection
        │   ├── MyFiles.jsx         # File grid
        │   ├── FileDetails.jsx     # Per-file analytics, versions, links, preview
        │   ├── TagSearch.jsx       # Unified name + tag search
        │   ├── Analytics.jsx       # Top downloads leaderboard
        │   ├── BandwidthSavings.jsx# Compression savings metrics
        │   ├── Settings.jsx        # User profile & preferences
        │   ├── AdminDashboard.jsx  # Admin system overview
        │   ├── FileAccess.jsx      # Public short link download page
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
Built with ❤️ using Spring Boot, React, and Firebase.
</div>
