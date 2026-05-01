# SmartShare — Intelligent File Compression & Secure Link Sharing Platform

## Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Firebase Project

## Phase 1 Setup

### 1. Infrastructure Setup
Start the required databases and services using Docker Compose:
```bash
cd infrastructure
docker-compose up -d
```
This will start:
- PostgreSQL (Port 5432)
- Redis (Port 6379)
- MinIO (Ports 9000, 9001)

### 2. Backend Setup (Spring Boot)
Configure the backend properties:
1. Copy `backend/.env.example` to `backend/.env` and update the values if necessary.
2. Obtain a Firebase service account key (JSON) from your Firebase Console.
3. Place the JSON file securely and update the `FirebaseConfig` class to load it.

Build and run the backend:
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

### 3. Frontend Setup (React + Vite)
Configure the frontend environment:
1. Copy `frontend/.env.example` to `frontend/.env.local`.
2. Update the Firebase keys from your Firebase Console project settings.

Install dependencies and start the Vite dev server:
```bash
cd frontend
npm install axios react-router-dom firebase
npm install -D tailwindcss postcss autoprefixer
npm install
npm run dev
```

## Architecture
- **Backend**: Spring Boot, PostgreSQL (Metadata), Redis (Caching), MinIO (Object Storage)
- **Frontend**: React, Vite, TailwindCSS
- **Authentication**: Firebase Authentication (ID Token Validation)
