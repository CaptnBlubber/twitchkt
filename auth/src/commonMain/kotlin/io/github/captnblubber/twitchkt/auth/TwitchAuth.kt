package io.github.captnblubber.twitchkt.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.encodeURLParameter
import io.github.captnblubber.twitchkt.error.TwitchApiException

class TwitchAuth(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
    private val authBaseUrl: String = "https://id.twitch.tv/oauth2",
) {
    fun authorizationUrl(
        scopes: Set<TwitchScope>,
        redirectUri: String,
        state: String? = null,
    ): String =
        buildString {
            append("$authBaseUrl/authorize")
            append("?client_id=$clientId")
            append("&redirect_uri=${redirectUri.encodeURLParameter()}")
            append("&response_type=code")
            append("&scope=${scopes.joinToString(" ") { it.value }.encodeURLParameter()}")
            if (state != null) {
                append("&state=${state.encodeURLParameter()}")
            }
        }

    suspend fun exchangeCode(
        code: String,
        redirectUri: String,
    ): TokenResponse {
        val response =
            httpClient.submitForm(
                url = "$authBaseUrl/token",
                formParameters =
                    Parameters.build {
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                        append("code", code)
                        append("grant_type", "authorization_code")
                        append("redirect_uri", redirectUri)
                    },
            )
        if (response.status.value !in 200..299) {
            throw mapError(response.status.value, response.bodyAsText())
        }
        return response.body()
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val response =
            httpClient.submitForm(
                url = "$authBaseUrl/token",
                formParameters =
                    Parameters.build {
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                        append("refresh_token", refreshToken)
                        append("grant_type", "refresh_token")
                    },
            )
        if (response.status.value !in 200..299) {
            throw mapError(response.status.value, response.bodyAsText())
        }
        return response.body()
    }

    suspend fun validate(accessToken: String): ValidationResponse {
        val response =
            httpClient.get("$authBaseUrl/validate") {
                header("Authorization", "OAuth $accessToken")
            }
        if (response.status.value !in 200..299) {
            throw mapError(response.status.value, response.bodyAsText())
        }
        return response.body()
    }

    private fun mapError(
        statusCode: Int,
        body: String,
    ): TwitchApiException =
        when (statusCode) {
            400 -> TwitchApiException.BadRequest(body)
            401 -> TwitchApiException.Unauthorized(body)
            403 -> TwitchApiException.Forbidden(body)
            else -> TwitchApiException.ServerError(statusCode, body)
        }
}
