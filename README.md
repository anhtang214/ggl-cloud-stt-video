# HATA Notetaking AI

A cloud-native web application that automatically transcribes lecture and meeting videos into structured notes using Google Cloud AI services.

Users upload an MP4 video, and the application extracts the audio, transcribes speech using Google Cloud Speech-to-Text, then generates a concise bullet-point summary using Gemini via Vertex AI — all within a single authenticated session.

## Team

| Name |
|------|
| Anh Tang |
| Rosie Ha Vu |

## Features

- Google OAuth2 login — users authenticate with their Google account
- Video upload with automatic audio extraction (MP4 to WAV via JAVE2/FFmpeg)
- Audio stored on Google Cloud Storage; transcription runs entirely in the cloud
- Long-form speech transcription with Vietnamese and English language support as default (will support more languages based on users' choices)
- AI-generated summary using Gemini 2.0 Flash (Vertex AI)
- Per-user transcription history stored in MySQL with status tracking (Processing / Completed / Failed)
- REST API endpoints for fetching transcription records by user

## Architecture

```
Browser (Thymeleaf + Bootstrap)
    |
    | POST /upload (multipart)
    v
UploadController
    |
    |-- VideoProcessingService
    |       |-- Upload video  --> Google Cloud Storage
    |       |-- Extract WAV audio from MP4 (JAVE2/FFmpeg)
    |       `-- Upload audio  --> Google Cloud Storage
    |
    |-- SpeechService
    |       |-- Transcribe audio URI --> Google Cloud Speech-to-Text
    |       `-- Summarize transcript --> Gemini 2.0 Flash (Vertex AI)
    |
    `-- TranscriptionService
            `-- Persist result --> MySQL (JPA/Hibernate)
```

Credentials are loaded once at startup via a `@Configuration` bean (`GcpConfig`) and injected into all services — no hardcoded secrets anywhere in the codebase.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2 |
| Security | Spring Security, OAuth2 (Google) |
| Frontend | Thymeleaf, Bootstrap 5 |
| AI — Transcription | Google Cloud Speech-to-Text v1 |
| AI — Summarisation | Gemini 2.0 Flash via Vertex AI |
| File Storage | Google Cloud Storage |
| Audio Processing | JAVE2 (FFmpeg wrapper) |
| Database | MySQL 8, Spring Data JPA |
| Build | Maven |

## Project Status

Currently in active development. Core pipeline (upload → transcription → summary) is being integrated. 

## Local Setup

### Prerequisites

- Java 17+
- Maven
- MySQL 8
- A Google Cloud project with the following APIs enabled:
  - Cloud Speech-to-Text
  - Cloud Storage
  - Vertex AI
- A GCP service account key file

### Configuration

Copy the example properties file and fill in your values:

```
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Required values in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/speech_recognition_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET

# GCP
gcp.project-id=YOUR_GCP_PROJECT_ID
gcp.bucket-name=YOUR_BUCKET_NAME
gcp.credentials.location=config/service-account.json
```

Place your GCP service account JSON at `config/service-account.json` (excluded from version control).

### Run

```bash
mvn spring-boot:run
```

Application starts at `http://localhost:8080`.
