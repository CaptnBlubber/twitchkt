package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf

const val TEST_TOKEN = "test-token"
const val TEST_CLIENT_ID = "test-client-id"
val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")

internal fun createHelixClient(engine: MockEngine): HelixHttpClient {
    val httpClient = HttpClient(engine)
    val config =
        TwitchKtConfig(
            clientId = TEST_CLIENT_ID,
            tokenProvider = TokenProvider { TEST_TOKEN },
        )
    return HelixHttpClient(httpClient, config)
}
