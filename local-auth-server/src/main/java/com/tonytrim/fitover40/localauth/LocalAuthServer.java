package com.tonytrim.fitover40.localauth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class LocalAuthServer {
    private static final int DEFAULT_PORT = 3000;
    private static final long ACCESS_TOKEN_TTL_SECONDS = 3600L;
    private static final long REFRESH_TOKEN_TTL_SECONDS = 60L * 60L * 24L * 30L;
    private static final int HASH_ITERATIONS = 120_000;
    private static final int HASH_KEY_LENGTH = 256;
    private static final Pattern JSON_STRING_FIELD =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");

    private final TokenGenerator tokenGenerator = new TokenGenerator();
    private final Storage storage;
    private final int port;

    private LocalAuthServer(Storage storage, int port) {
        this.storage = storage;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        Storage storage = createStorage();
        int port = parsePort(System.getenv("PORT"));
        new LocalAuthServer(storage, port).start();
    }

    private static Storage createStorage() throws SQLException {
        String databaseUrl = trimToNull(System.getenv("DATABASE_URL"));
        if (databaseUrl != null) {
            PostgresStorage postgresStorage = new PostgresStorage(databaseUrl);
            postgresStorage.initialize();
            System.out.println("Using PostgreSQL storage.");
            return postgresStorage;
        }
        System.out.println("DATABASE_URL is not set. Falling back to in-memory storage.");
        return new InMemoryStorage();
    }

    private static int parsePort(String portValue) {
        if (portValue == null || portValue.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(portValue.trim());
        } catch (NumberFormatException error) {
            throw new IllegalStateException("Invalid PORT value: " + portValue, error);
        }
    }

    private void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/health", this::handleHealth);
        server.createContext("/auth/sign-up", exchange -> handleJsonPost(exchange, this::handleSignUp));
        server.createContext("/auth/sign-in", exchange -> handleJsonPost(exchange, this::handleSignIn));
        server.createContext("/auth/refresh", exchange -> handleJsonPost(exchange, this::handleRefresh));
        server.createContext("/auth/logout", exchange -> handleJsonPost(exchange, this::handleLogout));
        server.createContext("/auth/me", this::handleCurrentUser);
        server.createContext("/workouts/sync", this::handleWorkoutSync);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Auth server running on port " + port);
        if (port == DEFAULT_PORT) {
            System.out.println("Local URL: http://localhost:" + port);
            System.out.println("Android emulator URL: http://10.0.2.2:" + port);
        }
        System.out.println("Press Ctrl+C to stop.");
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        addCommonHeaders(exchange.getResponseHeaders());
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, messageBody("Method not allowed."));
            return;
        }
        sendJson(exchange, 200, "{\"status\":\"ok\"}");
    }

    private void handleJsonPost(HttpExchange exchange, JsonHandler handler) throws IOException {
        addCommonHeaders(exchange.getResponseHeaders());
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, messageBody("Method not allowed."));
            return;
        }
        String body = readBody(exchange);
        Map<String, String> payload = parseFlatJsonObject(body);
        try {
            handler.handle(exchange, payload);
        } catch (StorageException error) {
            sendJson(exchange, 500, messageBody(error.getMessage()));
        }
    }

    private void handleSignUp(HttpExchange exchange, Map<String, String> payload) throws IOException, StorageException {
        String email = normalizeEmail(payload.get("email"));
        String password = payload.get("password");
        String displayName = firstNonBlank(payload.get("displayName"), payload.get("name"));

        if (email == null || password == null || password.length() < 8) {
            sendJson(exchange, 400, messageBody("Email and password with at least 8 characters are required."));
            return;
        }
        if (storage.findUserByEmail(email) != null) {
            sendJson(exchange, 409, messageBody("An account with that email already exists."));
            return;
        }

        PasswordHash passwordHash = PasswordHash.create(password);
        UserRecord user = storage.createUser(email, displayName, passwordHash.hashBase64(), passwordHash.saltBase64());
        AuthSession session = createSessionForUser(user);
        sendJson(exchange, 200, authResponseJson("Account created.", user, session));
    }

    private void handleSignIn(HttpExchange exchange, Map<String, String> payload) throws IOException, StorageException {
        String email = normalizeEmail(payload.get("email"));
        String password = payload.get("password");

        if (email == null || password == null) {
            sendJson(exchange, 400, messageBody("Email and password are required."));
            return;
        }

        UserRecord user = storage.findUserByEmail(email);
        if (user == null || !PasswordHash.matches(password, user.passwordHash(), user.passwordSalt())) {
            sendJson(exchange, 401, messageBody("Invalid email or password."));
            return;
        }

        AuthSession session = createSessionForUser(user);
        sendJson(exchange, 200, authResponseJson("Signed in.", user, session));
    }

    private void handleRefresh(HttpExchange exchange, Map<String, String> payload) throws IOException, StorageException {
        String refreshToken = trimToNull(payload.get("refreshToken"));
        if (refreshToken == null) {
            sendJson(exchange, 400, messageBody("Refresh token is required."));
            return;
        }

        StoredSession storedSession = storage.findSessionByRefreshToken(refreshToken);
        if (storedSession == null || storedSession.refreshExpiresAtEpochSeconds() < Instant.now().getEpochSecond()) {
            sendJson(exchange, 401, messageBody("Invalid refresh token."));
            return;
        }

        UserRecord user = storage.findUserById(storedSession.userId());
        if (user == null) {
            sendJson(exchange, 401, messageBody("Invalid refresh token."));
            return;
        }

        storage.deleteSessionByRefreshToken(refreshToken);
        AuthSession session = createSessionForUser(user);
        sendJson(exchange, 200, authResponseJson("Session refreshed.", user, session));
    }

    private void handleLogout(HttpExchange exchange, Map<String, String> payload) throws IOException, StorageException {
        String refreshToken = trimToNull(payload.get("refreshToken"));
        if (refreshToken != null) {
            storage.deleteSessionByRefreshToken(refreshToken);
        }
        sendJson(exchange, 200, messageBody("Signed out."));
    }

    private void handleCurrentUser(HttpExchange exchange) throws IOException {
        addCommonHeaders(exchange.getResponseHeaders());
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, messageBody("Method not allowed."));
            return;
        }

        try {
            UserRecord user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, messageBody("Missing or invalid authorization token."));
                return;
            }

            sendJson(exchange, 200, "{\"user\":" + userJson(user) + "}");
        } catch (StorageException error) {
            sendJson(exchange, 500, messageBody(error.getMessage()));
        }
    }

    private void handleWorkoutSync(HttpExchange exchange) throws IOException {
        addCommonHeaders(exchange.getResponseHeaders());
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, messageBody("Method not allowed."));
            return;
        }

        try {
            UserRecord user = authenticate(exchange);
            if (user == null) {
                sendJson(exchange, 401, messageBody("Missing or invalid authorization token."));
                return;
            }

            sendJson(exchange, 200,
                    "{\"message\":\"Sync completed.\",\"synced\":{\"runWorkouts\":0,\"strengthWorkouts\":0,\"exerciseSets\":0}}");
        } catch (StorageException error) {
            sendJson(exchange, 500, messageBody(error.getMessage()));
        }
    }

    private UserRecord authenticate(HttpExchange exchange) throws StorageException {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String accessToken = authorization.substring("Bearer ".length()).trim();
        if (accessToken.isEmpty()) {
            return null;
        }
        StoredSession storedSession = storage.findSessionByAccessToken(accessToken);
        if (storedSession == null || storedSession.accessExpiresAtEpochSeconds() < Instant.now().getEpochSecond()) {
            return null;
        }
        return storage.findUserById(storedSession.userId());
    }

    private AuthSession createSessionForUser(UserRecord user) throws StorageException {
        long now = Instant.now().getEpochSecond();
        long accessExpiresAt = now + ACCESS_TOKEN_TTL_SECONDS;
        long refreshExpiresAt = now + REFRESH_TOKEN_TTL_SECONDS;
        String accessToken = tokenGenerator.nextToken();
        String refreshToken = tokenGenerator.nextToken();
        AuthSession session = new AuthSession(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
        storage.createSession(user.id(), session);
        return session;
    }

    private static void addCommonHeaders(Headers headers) {
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private static Map<String, String> parseFlatJsonObject(String body) {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher matcher = JSON_STRING_FIELD.matcher(body == null ? "" : body);
        while (matcher.find()) {
            values.put(matcher.group(1), unescapeJson(matcher.group(2)));
        }
        return values;
    }

    private static String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String firstNonBlank(String first, String second) {
        String normalizedFirst = trimToNull(first);
        if (normalizedFirst != null) {
            return normalizedFirst;
        }
        return trimToNull(second);
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        } finally {
            exchange.close();
        }
    }

    private static String authResponseJson(String message, UserRecord user, AuthSession session) {
        return "{"
                + "\"message\":\"" + escapeJson(message) + "\","
                + "\"accessToken\":\"" + escapeJson(session.accessToken()) + "\","
                + "\"refreshToken\":\"" + escapeJson(session.refreshToken()) + "\","
                + "\"tokenType\":\"Bearer\","
                + "\"expiresIn\":" + ACCESS_TOKEN_TTL_SECONDS + ","
                + "\"expiresAt\":" + session.accessExpiresAtEpochSeconds() + ","
                + "\"user\":" + userJson(user)
                + "}";
    }

    private static String userJson(UserRecord user) {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("\"id\":\"").append(escapeJson(user.id())).append("\",")
                .append("\"email\":\"").append(escapeJson(user.email())).append("\"");
        if (user.displayName() != null && !user.displayName().isBlank()) {
            builder.append(",\"displayName\":\"").append(escapeJson(user.displayName())).append("\"");
        }
        builder.append("}");
        return builder.toString();
    }

    private static String messageBody(String message) {
        return "{\"message\":\"" + escapeJson(message) + "\"}";
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    @FunctionalInterface
    private interface JsonHandler {
        void handle(HttpExchange exchange, Map<String, String> payload) throws IOException, StorageException;
    }

    private interface Storage {
        UserRecord findUserByEmail(String email) throws StorageException;

        UserRecord findUserById(String userId) throws StorageException;

        UserRecord createUser(String email, String displayName, String passwordHash, String passwordSalt) throws StorageException;

        void createSession(String userId, AuthSession session) throws StorageException;

        StoredSession findSessionByAccessToken(String accessToken) throws StorageException;

        StoredSession findSessionByRefreshToken(String refreshToken) throws StorageException;

        void deleteSessionByRefreshToken(String refreshToken) throws StorageException;
    }

    private static final class InMemoryStorage implements Storage {
        private final Map<String, UserRecord> usersByEmail = new ConcurrentHashMap<>();
        private final Map<String, UserRecord> usersById = new ConcurrentHashMap<>();
        private final Map<String, StoredSession> sessionsByAccessToken = new ConcurrentHashMap<>();
        private final Map<String, StoredSession> sessionsByRefreshToken = new ConcurrentHashMap<>();

        @Override
        public UserRecord findUserByEmail(String email) {
            return usersByEmail.get(email);
        }

        @Override
        public UserRecord findUserById(String userId) {
            return usersById.get(userId);
        }

        @Override
        public UserRecord createUser(String email, String displayName, String passwordHash, String passwordSalt) {
            UserRecord user = new UserRecord(UUID.randomUUID().toString(), email, displayName, passwordHash, passwordSalt);
            usersByEmail.put(email, user);
            usersById.put(user.id(), user);
            return user;
        }

        @Override
        public void createSession(String userId, AuthSession session) {
            StoredSession storedSession = new StoredSession(
                    userId,
                    session.accessToken(),
                    session.refreshToken(),
                    session.accessExpiresAtEpochSeconds(),
                    session.refreshExpiresAtEpochSeconds()
            );
            sessionsByAccessToken.put(session.accessToken(), storedSession);
            sessionsByRefreshToken.put(session.refreshToken(), storedSession);
        }

        @Override
        public StoredSession findSessionByAccessToken(String accessToken) {
            return sessionsByAccessToken.get(accessToken);
        }

        @Override
        public StoredSession findSessionByRefreshToken(String refreshToken) {
            return sessionsByRefreshToken.get(refreshToken);
        }

        @Override
        public void deleteSessionByRefreshToken(String refreshToken) {
            StoredSession storedSession = sessionsByRefreshToken.remove(refreshToken);
            if (storedSession != null) {
                sessionsByAccessToken.remove(storedSession.accessToken());
            }
        }
    }

    private static final class PostgresStorage implements Storage {
        private final String databaseUrl;

        private PostgresStorage(String databaseUrl) {
            this.databaseUrl = databaseUrl;
        }

        private void initialize() throws SQLException {
            try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
                statement.execute("""
                        create table if not exists app_users (
                            id uuid primary key,
                            email text not null unique,
                            password_hash text not null,
                            password_salt text not null,
                            display_name text,
                            created_at timestamptz not null default now()
                        )
                        """);
                statement.execute("""
                        create table if not exists auth_sessions (
                            refresh_token text primary key,
                            access_token text not null unique,
                            user_id uuid not null references app_users(id) on delete cascade,
                            access_expires_at bigint not null,
                            refresh_expires_at bigint not null,
                            created_at timestamptz not null default now()
                        )
                        """);
                statement.execute("create index if not exists idx_auth_sessions_access_token on auth_sessions(access_token)");
                statement.execute("create index if not exists idx_auth_sessions_user_id on auth_sessions(user_id)");
            }
        }

        @Override
        public UserRecord findUserByEmail(String email) throws StorageException {
            String sql = "select id, email, display_name, password_hash, password_salt from app_users where email = ?";
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapUser(resultSet) : null;
                }
            } catch (SQLException error) {
                throw new StorageException("Database error while loading user by email.", error);
            }
        }

        @Override
        public UserRecord findUserById(String userId) throws StorageException {
            String sql = "select id, email, display_name, password_hash, password_salt from app_users where id = ?::uuid";
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapUser(resultSet) : null;
                }
            } catch (SQLException error) {
                throw new StorageException("Database error while loading user by id.", error);
            }
        }

        @Override
        public UserRecord createUser(String email, String displayName, String passwordHash, String passwordSalt) throws StorageException {
            String sql = """
                    insert into app_users (id, email, password_hash, password_salt, display_name)
                    values (?::uuid, ?, ?, ?, ?)
                    returning id, email, display_name, password_hash, password_salt
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                String id = UUID.randomUUID().toString();
                statement.setString(1, id);
                statement.setString(2, email);
                statement.setString(3, passwordHash);
                statement.setString(4, passwordSalt);
                statement.setString(5, displayName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new StorageException("User creation failed.");
                    }
                    return mapUser(resultSet);
                }
            } catch (SQLException error) {
                throw new StorageException("Database error while creating user.", error);
            }
        }

        @Override
        public void createSession(String userId, AuthSession session) throws StorageException {
            String sql = """
                    insert into auth_sessions (refresh_token, access_token, user_id, access_expires_at, refresh_expires_at)
                    values (?, ?, ?::uuid, ?, ?)
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, session.refreshToken());
                statement.setString(2, session.accessToken());
                statement.setString(3, userId);
                statement.setLong(4, session.accessExpiresAtEpochSeconds());
                statement.setLong(5, session.refreshExpiresAtEpochSeconds());
                statement.executeUpdate();
            } catch (SQLException error) {
                throw new StorageException("Database error while creating session.", error);
            }
        }

        @Override
        public StoredSession findSessionByAccessToken(String accessToken) throws StorageException {
            String sql = """
                    select user_id::text, access_token, refresh_token, access_expires_at, refresh_expires_at
                    from auth_sessions
                    where access_token = ?
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, accessToken);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapSession(resultSet) : null;
                }
            } catch (SQLException error) {
                throw new StorageException("Database error while loading access token.", error);
            }
        }

        @Override
        public StoredSession findSessionByRefreshToken(String refreshToken) throws StorageException {
            String sql = """
                    select user_id::text, access_token, refresh_token, access_expires_at, refresh_expires_at
                    from auth_sessions
                    where refresh_token = ?
                    """;
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, refreshToken);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next() ? mapSession(resultSet) : null;
                }
            } catch (SQLException error) {
                throw new StorageException("Database error while loading refresh token.", error);
            }
        }

        @Override
        public void deleteSessionByRefreshToken(String refreshToken) throws StorageException {
            String sql = "delete from auth_sessions where refresh_token = ?";
            try (Connection connection = openConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, refreshToken);
                statement.executeUpdate();
            } catch (SQLException error) {
                throw new StorageException("Database error while deleting session.", error);
            }
        }

        private Connection openConnection() throws SQLException {
            return DriverManager.getConnection(databaseUrl);
        }

        private static UserRecord mapUser(ResultSet resultSet) throws SQLException {
            return new UserRecord(
                    resultSet.getString("id"),
                    resultSet.getString("email"),
                    resultSet.getString("display_name"),
                    resultSet.getString("password_hash"),
                    resultSet.getString("password_salt")
            );
        }

        private static StoredSession mapSession(ResultSet resultSet) throws SQLException {
            return new StoredSession(
                    resultSet.getString("user_id"),
                    resultSet.getString("access_token"),
                    resultSet.getString("refresh_token"),
                    resultSet.getLong("access_expires_at"),
                    resultSet.getLong("refresh_expires_at")
            );
        }
    }

    private static final class PasswordHash {
        private static final SecureRandom RANDOM = new SecureRandom();

        private final String hashBase64;
        private final String saltBase64;

        private PasswordHash(String hashBase64, String saltBase64) {
            this.hashBase64 = hashBase64;
            this.saltBase64 = saltBase64;
        }

        static PasswordHash create(String password) {
            byte[] salt = new byte[16];
            RANDOM.nextBytes(salt);
            byte[] hash = hashPassword(password.toCharArray(), salt);
            return new PasswordHash(
                    Base64.getEncoder().encodeToString(hash),
                    Base64.getEncoder().encodeToString(salt)
            );
        }

        static boolean matches(String password, String hashBase64, String saltBase64) {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            byte[] expectedHash = Base64.getDecoder().decode(hashBase64);
            byte[] candidateHash = hashPassword(password.toCharArray(), salt);
            return java.security.MessageDigest.isEqual(expectedHash, candidateHash);
        }

        String hashBase64() {
            return hashBase64;
        }

        String saltBase64() {
            return saltBase64;
        }

        private static byte[] hashPassword(char[] password, byte[] salt) {
            try {
                PBEKeySpec spec = new PBEKeySpec(password, salt, HASH_ITERATIONS, HASH_KEY_LENGTH);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                return factory.generateSecret(spec).getEncoded();
            } catch (GeneralSecurityException error) {
                throw new IllegalStateException("Password hashing failed.", error);
            }
        }
    }

    private static final class TokenGenerator {
        private final SecureRandom secureRandom = new SecureRandom();

        String nextToken() {
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
    }

    private static final class StorageException extends Exception {
        private StorageException(String message) {
            super(message);
        }

        private StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private record UserRecord(String id, String email, String displayName, String passwordHash, String passwordSalt) {
        private UserRecord {
            Objects.requireNonNull(id);
            Objects.requireNonNull(email);
            Objects.requireNonNull(passwordHash);
            Objects.requireNonNull(passwordSalt);
        }
    }

    private record AuthSession(
            String accessToken,
            String refreshToken,
            long accessExpiresAtEpochSeconds,
            long refreshExpiresAtEpochSeconds
    ) {
    }

    private record StoredSession(
            String userId,
            String accessToken,
            String refreshToken,
            long accessExpiresAtEpochSeconds,
            long refreshExpiresAtEpochSeconds
    ) {
    }
}
