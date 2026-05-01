# SmartShare — Intelligent File Compression & Secure Link Sharing Platform

SmartShare is a full-stack platform that lets users securely upload, compress, deduplicate, and share files via protected short links. It features real-time analytics, role-based access control, a comprehensive user profile system, and Google OAuth — all built on a Spring Boot backend with a React + Vite frontend.

---

## ✨ Feature Overview

### 🔐 Authentication
- **Email/Password Login & Registration** via Firebase Authentication
- **Google OAuth Sign-In / Sign-Up** — one-click login with Google account, auto-creates user profile on first login
- **Role-based routing** — Admin users are redirected to the Admin Dashboard; regular users access the standard workspace
- Firebase ID tokens verified server-side on every request

### 📁 File Management
- **Upload files** with automatic compression and deduplication
- **My Files** page — list, view, and delete your uploaded files
- **File deletion** with full cascade cleanup (links, analytics, MinIO object, Redis cache)
- **Short link generation** with optional password protection, download limits, and expiry dates
- **Copy link** button on each short link
- **Delete individual links** — with confirmation warning that link analytics will also be deleted

### 📊 Analytics & Insights
- **Dashboard** — overview cards for total files, downloads, and bandwidth saved
- **File Detail Analytics** page — per-file breakdown of downloads, links, compression ratio, tags, and recent activity
- **Short link status tracking** — ACTIVE, EXPIRED, LIMIT_REACHED, PASSWORD_PROTECTED
- **Bandwidth Savings** — personal and system-wide storage metrics
- **Admin Dashboard** — system telemetry including usage trends, popular tags, storage stats, and recent activity log (privacy-safe, last 10 entries)

### 👤 User Profile & Settings
- **Account Settings page** (`/settings`) accessible from the Navbar dropdown
- **Advanced profile fields**: Display Name, Profile Image URL, Organization, Location, Bio
- **Professional info**: Job Profile (dropdown), Experience Level
- **Social links**: LinkedIn, GitHub, Portfolio (with server-side URL validation)
- **App preferences**: Language, Timezone, Default Link Expiry, Email Notifications toggle
- Profile data is persisted to PostgreSQL and reflected instantly in the Navbar

### 🔗 Secure Public File Access
- Short links accessible at `/f/:shortCode`
- Handles expired, password-protected, and download-limit-reached links gracefully
- Password transmitted via request header (`X-Download-Password`), never in URL
- Browser/device detection for analytics: Edge, Chrome, Firefox, Safari, Opera, Mobile, Tablet, Desktop

### 🔍 Tag Search
- Search files by auto-generated content tags
- Tag analytics visible in dashboard and per-file detail view

---

## 🏗️ Architecture

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 3.2, Java 17 |
| **Database** | PostgreSQL (metadata & user data) |
| **Cache** | Redis (short link resolution) |
| **Object Storage** | MinIO (compressed file blobs) |
| **Frontend** | React 18, Vite 5, TailwindCSS 3 |
| **Authentication** | Firebase Authentication |
| **Charts** | Recharts |

---

## ⚙️ Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- A Firebase project with **Email/Password** and **Google** sign-in providers enabled

---

## 🚀 Getting Started

### 1. Infrastructure

Start PostgreSQL, Redis, and MinIO with Docker Compose:

```bash
cd infrastructure
docker-compose up -d
```

Services started:
| Service | Port(s) |
|---|---|
| PostgreSQL | 5432 |
| Redis | 6379 |
| MinIO (API) | 9000 |
| MinIO (Console) | 9001 |

---

### 2. Backend Setup (Spring Boot)

#### Environment Configuration

Create `backend/src/main/resources/application.properties` or set environment variables:

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/smartshare
spring.datasource.username=smartshare
spring.datasource.password=smartshare

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# MinIO
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=smartshare

# Firebase Service Account
firebase.service-account.path=classpath:firebase-service-account.json
```

#### Firebase Service Account

1. Go to **Firebase Console → Project Settings → Service Accounts**
2. Click **Generate new private key** and download the JSON
3. Place it at `backend/src/main/resources/firebase-service-account.json`

#### Run the Backend

```bash
cd backend
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # Linux / macOS
```

The API will be available at `http://localhost:8080`.

---

### 3. Frontend Setup (React + Vite)

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

#### Firebase Console Setup

In the Firebase Console, enable the following under **Authentication → Sign-in method**:
- ✅ Email/Password
- ✅ Google

#### Install & Run

```bash
cd frontend
npm install
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## 🗺️ Route Map

| Path | Access | Description |
|---|---|---|
| `/login` | Public | Email/Password & Google login |
| `/register` | Public | Email/Password & Google sign-up |
| `/f/:shortCode` | Public | Secure file download via short link |
| `/dashboard` | User | Overview stats and recent activity |
| `/upload` | User | Upload a new file |
| `/files` | User | My uploaded files |
| `/files/:id` | User | Per-file analytics and link management |
| `/search` | User | Search files by tag |
| `/analytics` | User | Top downloaded files |
| `/bandwidth` | User | Bandwidth and compression savings |
| `/settings` | User | Account settings and profile management |
| `/admin` | Admin only | System admin dashboard |

---

## 🧑‍💼 Admin Access

Set the admin email(s) in `frontend/.env`:

```env
VITE_ADMIN_EMAILS=admin@yourdomain.com
```

Admin users are automatically redirected to `/admin` on login and cannot access regular user routes.

---

## 📦 Key API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/files/upload` | Upload a file |
| `GET` | `/api/files/my-files` | List the current user's files |
| `DELETE` | `/api/files/:id` | Delete a file and all related data |
| `GET` | `/api/files/:id/details` | Full file analytics detail |
| `GET` | `/api/user/profile` | Get current user profile |
| `PUT` | `/api/user/profile` | Update current user profile |
| `GET` | `/api/analytics/dashboard/overview` | Dashboard stats |
| `GET` | `/api/analytics/bandwidth/user` | Personal bandwidth savings |
| `GET` | `/api/admin/overview` | Admin system overview |
| `GET` | `/f/:shortCode` | Resolve and download via short link |

---

## 🛡️ Security Notes

- All API routes (except `/f/:shortCode`) require a valid Firebase ID token in the `Authorization: Bearer <token>` header.
- The backend auto-creates a user record on first authenticated request (supports both email/password and Google OAuth).
- Admin access is enforced both frontend (route guards) and backend (email whitelist check).
- Short link passwords are transmitted via `X-Download-Password` header — never in the URL.
- URL fields (LinkedIn, GitHub, Portfolio) are validated server-side for correct prefixes.
