// TwitchKt Sample: Helix Pagination (Flow)
//
// Demonstrates automatic pagination using the getAllXxx() Flow API. Compare with
// the helix-pagination sample — same data, much simpler code.
//
// Setup:
//   1. Copy samples/local.properties.example to samples/local.properties
//   2. Fill in twitch.token and twitch.clientId
//
// Run:
//   ./gradlew :samples:helix-pagination-flow:run

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.take
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

suspend fun main() {
    val props = Properties().apply {
        load(File("samples/local.properties").reader())
    }
    val token = props.getProperty("twitch.token")
    val clientId = props.getProperty("twitch.clientId")

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    val config = TwitchKtConfig(
        clientId = clientId,
        tokenProvider = TokenProvider { token },
    )

    val helix = TwitchHelix(httpClient, config)

    helix.search.getAllChannels(query = "development")
        .take(15)
        .collect { channel ->
            println("${channel.displayName} — ${channel.gameName} (live: ${channel.isLive})")
        }

    httpClient.close()
}
