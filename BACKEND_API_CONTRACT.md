# FitOver40 Backend API Contract

Base URL:

```text
https://your-api.example.com
```

Authentication:

- Native app auth uses bearer tokens
- Every protected endpoint requires:

```http
Authorization: Bearer <accessToken>
```

- Content type for request and response:

```http
Content-Type: application/json
Accept: application/json
```

## 1. Sign Up

`POST /auth/sign-up`

Request:

```json
{
  "email": "user@example.com",
  "password": "correct horse battery staple",
  "displayName": "Tony Trim"
}
```

Response `200`:

```json
{
  "message": "Account created.",
  "accessToken": "jwt-access-token",
  "refreshToken": "long-lived-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "displayName": "Tony Trim"
  }
}
```

## 2. Sign In

`POST /auth/sign-in`

Request:

```json
{
  "email": "user@example.com",
  "password": "correct horse battery staple"
}
```

Response `200`:

```json
{
  "message": "Signed in.",
  "accessToken": "jwt-access-token",
  "refreshToken": "long-lived-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "displayName": "Tony Trim"
  }
}
```

## 3. Refresh Token

`POST /auth/refresh`

Request:

```json
{
  "refreshToken": "long-lived-refresh-token"
}
```

Response `200`:

```json
{
  "message": "Session refreshed.",
  "accessToken": "new-jwt-access-token",
  "refreshToken": "rotated-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "displayName": "Tony Trim"
  }
}
```

Notes:

- Backend may rotate refresh tokens on every refresh
- If refresh token is invalid, return `401`

## 4. Logout

`POST /auth/logout`

Request:

```json
{
  "refreshToken": "rotated-refresh-token"
}
```

Response `200`:

```json
{
  "message": "Signed out."
}
```

## 5. Current User

`GET /auth/me`

Response `200`:

```json
{
  "user": {
    "id": "usr_123",
    "email": "user@example.com",
    "displayName": "Tony Trim"
  }
}
```

## 6. Workout Sync

`POST /workouts/sync`

Purpose:

- Upsert all local workouts from the device into the backend
- Client may resend already-synced records
- Backend should deduplicate by `userId + deviceId + localId + entityType`, or by a server-managed mapping

Request:

```json
{
  "deviceId": "android-local-device",
  "appVersion": "1.0.0",
  "runWorkouts": [
    {
      "localId": 14,
      "date": 1775510400000,
      "durationSeconds": 1800,
      "intervalsCompleted": 8,
      "estimatedCalories": 210,
      "planName": "Starter Intervals",
      "trackingMode": "Outside",
      "distanceMeters": 2450.5
    }
  ],
  "strengthWorkouts": [
    {
      "localId": 27,
      "date": 1775510400000,
      "durationSeconds": 1500,
      "planName": "Strength Foundations"
    }
  ],
  "exerciseSets": [
    {
      "localId": 92,
      "workoutLocalId": 27,
      "exerciseName": "Glute Bridges",
      "setNumber": 1,
      "plannedReps": 10,
      "actualReps": 10,
      "weight": 0.0,
      "date": 1775510400000
    }
  ]
}
```

Response `200`:

```json
{
  "message": "Sync completed.",
  "synced": {
    "runWorkouts": 1,
    "strengthWorkouts": 1,
    "exerciseSets": 1
  }
}
```

Backend behavior:

- Accept partial empty arrays
- Upsert instead of creating duplicates
- Reject malformed records with `400`
- Reject missing/invalid auth with `401`

## 7. Error Format

All non-2xx responses should use:

```json
{
  "message": "Human-readable error message"
}
```

Examples:

- `400`: invalid payload
- `401`: invalid credentials or expired session
- `403`: account not allowed
- `409`: duplicate account on sign-up
- `500`: unexpected server error
