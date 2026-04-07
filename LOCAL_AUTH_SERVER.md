# Local Auth Server

This repo now includes a small Java auth service that can run in two modes:

- PostgreSQL-backed when `DATABASE_URL` is set
- In-memory fallback when `DATABASE_URL` is missing

It implements the endpoints the app already calls:

- `POST /auth/sign-up`
- `POST /auth/sign-in`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
- `POST /workouts/sync`
- `GET /health`

## Local Run

From the project root:

```powershell
.\gradlew.bat :local-auth-server:run
```

The server listens on:

- `http://localhost:3000` on your machine
- `http://10.0.2.2:3000` from the Android emulator

If `DATABASE_URL` is not set, the server starts in in-memory mode for local dev.

## App Config

For emulator debug builds, the Android app already falls back to `http://10.0.2.2:3000`.

If you want to set it explicitly, add this to `local.properties`:

```properties
authBaseUrl=http://10.0.2.2:3000
```

## Railway

Railway will provide `PORT` automatically. This service now reads it from the environment.

Set these variables on the Railway service:

- `DATABASE_URL`

If your Railway Postgres service is attached correctly, Railway usually injects `DATABASE_URL` for you. The service will create its own tables on startup.

Recommended Railway start command:

```text
./gradlew :local-auth-server:run
```

Recommended health check path:

```text
/health
```

## Data Model

The service bootstraps these tables automatically in Postgres:

- `app_users`
- `auth_sessions`

## Notes

- In-memory mode resets whenever you stop the server.
- Create an account with Sign Up first, then Sign In.
- Passwords are hashed with PBKDF2 before storage.
- Tokens are opaque random strings stored server-side.
- For production mobile builds, point the Android app at the Railway HTTPS URL.
