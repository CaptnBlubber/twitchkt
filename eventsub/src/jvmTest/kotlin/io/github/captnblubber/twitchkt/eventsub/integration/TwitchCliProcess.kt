package io.github.captnblubber.twitchkt.eventsub.integration

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Helper for interacting with the Twitch CLI mock servers started by the shell script.
 * This class does NOT start/stop the servers — that's the script's responsibility.
 * It only triggers events and fetches mock tokens.
 */
object TwitchCliProcess {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Triggers a mock EventSub event via the Twitch CLI.
     *
     * @param eventType the EventSub event type (e.g. `channel.follow`).
     * @param sessionId the WebSocket session ID from the welcome message.
     * @param version optional version flag for types with multiple versions (e.g. `channel.update`).
     */
    fun triggerEvent(
        eventType: String,
        sessionId: String,
        version: String? = null,
    ) {
        val args =
            buildList {
                add("twitch")
                add("event")
                add("trigger")
                add(eventType)
                add("--transport=websocket")
                add("--session=$sessionId")
                if (version != null) add("--version=$version")
            }
        val process =
            ProcessBuilder(args)
                .redirectErrorStream(true)
                .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        // Exit code 0 = direct trigger, 2 = forwarded to WebSocket server (both are success)
        if (exitCode != 0 && exitCode != 2) {
            throw RuntimeException(
                "twitch event trigger $eventType failed (exit=$exitCode): $output",
            )
        }
    }

    /**
     * Fetches a mock OAuth token from the mock-api server.
     *
     * @param clientId the client ID from the mock-api's generated data.
     * @param clientSecret the client secret from the mock-api's generated data.
     * @param httpClient an HttpClient to use for the request.
     * @param mockApiPort the port the mock-api server is running on.
     * @return the access token string.
     */
    suspend fun getMockToken(
        clientId: String,
        clientSecret: String,
        httpClient: HttpClient,
        mockApiPort: Int = 8081,
    ): String {
        val response =
            httpClient.post("http://localhost:$mockApiPort/auth/token") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("client_id=$clientId&client_secret=$clientSecret&grant_type=client_credentials")
            }
        val body = response.bodyAsText()
        val parsed = json.parseToJsonElement(body).jsonObject
        return parsed["access_token"]?.jsonPrimitive?.content
            ?: throw RuntimeException("No access_token in mock-api response: $body")
    }
}
