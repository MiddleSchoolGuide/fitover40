package com.tonytrim.fitover40.data.auth

import com.tonytrim.fitover40.data.pref.AuthSession
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import kotlin.math.max

data class AuthPayload(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class RefreshPayload(
    val refreshToken: String
)

data class AuthResponse(
    val session: AuthSession,
    val message: String? = null
)

class AuthApi(private val baseUrl: String) {

    fun signIn(payload: AuthPayload): AuthResponse = post("/auth/sign-in", payload)

    fun signUp(payload: AuthPayload): AuthResponse = post("/auth/sign-up", payload)

    fun refresh(refreshToken: String): AuthResponse = postRefresh("/auth/refresh", RefreshPayload(refreshToken))

    fun logout(refreshToken: String?) {
        if (refreshToken.isNullOrBlank()) return
        runCatching {
            postRefresh("/auth/logout", RefreshPayload(refreshToken))
        }
    }

    private fun post(path: String, payload: AuthPayload): AuthResponse {
        val requestBody = JSONObject().apply {
            put("email", payload.email)
            put("password", payload.password)
            payload.displayName?.takeIf { it.isNotBlank() }?.let { put("displayName", it) }
            payload.displayName?.takeIf { it.isNotBlank() }?.let { put("name", it) }
        }.toString()
        return execute(path, requestBody, payload.email)
    }

    private fun postRefresh(path: String, payload: RefreshPayload): AuthResponse {
        val requestBody = JSONObject().apply {
            put("refreshToken", payload.refreshToken)
        }.toString()
        return execute(path, requestBody, fallbackEmail = "")
    }

    private fun execute(path: String, requestBody: String, fallbackEmail: String): AuthResponse {
        check(baseUrl.isNotBlank()) {
            "Missing auth base URL. Set authBaseUrl in local.properties or FITOVER40_AUTH_BASE_URL."
        }

        val endpoint = baseUrl.trimEnd('/') + path
        val (statusCode, body) = try {
            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            connection.outputStream.bufferedWriter().use { it.write(requestBody) }

            val statusCode = connection.responseCode
            val body = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).readText()
            }.orEmpty()
            statusCode to body
        } catch (error: ConnectException) {
            throw IllegalStateException(
                "Could not connect to $endpoint. Start the backend server or set authBaseUrl to the correct host.",
                error
            )
        } catch (error: UnknownHostException) {
            throw IllegalStateException(
                "Could not resolve ${URL(endpoint).host}. Check authBaseUrl in local.properties.",
                error
            )
        } catch (error: SocketTimeoutException) {
            throw IllegalStateException(
                "Timed out calling $endpoint. Check that the backend is running and reachable from the emulator.",
                error
            )
        }

        if (statusCode !in 200..299) {
            val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
            throw IllegalStateException(message?.takeIf { it.isNotBlank() }
                ?: "Authentication failed with HTTP $statusCode.")
        }

        return parseResponse(body, fallbackEmail)
    }

    private fun parseResponse(body: String, fallbackEmail: String): AuthResponse {
        val root = JSONObject(body.ifBlank { "{}" })
        val data = root.optJSONObject("data") ?: root
        val user = data.optJSONObject("user") ?: root.optJSONObject("user")

        val accessToken = data.optString("accessToken")
            .ifBlank { data.optString("token") }
            .ifBlank { root.optString("accessToken") }
            .ifBlank { root.optString("token") }

        if (accessToken.isBlank()) {
            throw IllegalStateException("Auth response did not include an access token.")
        }

        val refreshToken = data.optString("refreshToken").ifBlank {
            root.optString("refreshToken")
        }.ifBlank { null }

        val tokenType = data.optString("tokenType").ifBlank {
            root.optString("tokenType")
        }.ifBlank { "Bearer" }

        val expiresAtEpochSeconds = data.optLong("expiresAt").takeIf { it > 0 }
            ?: root.optLong("expiresAt").takeIf { it > 0 }
            ?: data.optLong("expiresAtEpochSeconds").takeIf { it > 0 }
            ?: root.optLong("expiresAtEpochSeconds").takeIf { it > 0 }
            ?: data.optLong("expiresIn").takeIf { it > 0 }?.let { seconds ->
                (System.currentTimeMillis() / 1000L) + max(seconds, 60L)
            }
            ?: root.optLong("expiresIn").takeIf { it > 0 }?.let { seconds ->
                (System.currentTimeMillis() / 1000L) + max(seconds, 60L)
            }

        val email = user?.optString("email")?.ifBlank { null }
            ?: data.optString("email").ifBlank { null }
            ?: root.optString("email").ifBlank { null }
            ?: fallbackEmail

        val userId = user?.opt("id")?.toString()
            ?: data.opt("userId")?.toString()
            ?: root.opt("userId")?.toString()

        val displayName = user?.optString("displayName")?.ifBlank {
            user.optString("name")
        }?.ifBlank { null }

        return AuthResponse(
            session = AuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = tokenType,
                expiresAtEpochSeconds = expiresAtEpochSeconds,
                email = email,
                userId = userId,
                displayName = displayName
            ),
            message = root.optString("message").ifBlank { null }
        )
    }
}
