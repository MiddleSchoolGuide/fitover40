package com.tonytrim.fitover40.data.account

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class AccountProfile(
    val id: String?,
    val email: String,
    val displayName: String?
)

class AccountApi(private val baseUrl: String) {

    fun fetchProfile(authorizationHeader: String): AccountProfile {
        check(baseUrl.isNotBlank()) {
            "Missing auth base URL. Set authBaseUrl in local.properties or FITOVER40_AUTH_BASE_URL."
        }

        val endpoint = baseUrl.trimEnd('/') + "/auth/me"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            doInput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authorizationHeader)
        }

        val statusCode = connection.responseCode
        val body = (if (statusCode in 200..299) connection.inputStream else connection.errorStream)?.use { stream ->
            BufferedReader(InputStreamReader(stream)).readText()
        }.orEmpty()

        if (statusCode !in 200..299) {
            val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
            throw IllegalStateException(message?.takeIf { it.isNotBlank() }
                ?: "Profile fetch failed with HTTP $statusCode.")
        }

        val root = JSONObject(body.ifBlank { "{}" })
        val data = root.optJSONObject("data") ?: root
        val user = data.optJSONObject("user") ?: root.optJSONObject("user") ?: data

        val email = user.optString("email").ifBlank {
            data.optString("email")
        }.ifBlank {
            throw IllegalStateException("Profile response did not include email.")
        }

        return AccountProfile(
            id = user.opt("id")?.toString() ?: data.opt("userId")?.toString(),
            email = email,
            displayName = user.optString("displayName").ifBlank {
                user.optString("name")
            }.ifBlank { null }
        )
    }
}
