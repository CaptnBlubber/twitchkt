// TwitchKt Sample: Helix Pagination (Manual)
//
// Demonstrates manual pagination using Page<T>. Fetches channels matching a query
// one page at a time, advancing the cursor explicitly.
//
// Setup:
//   1. Copy samples/local.properties.example to samples/local.properties
//   2. Fill in twitch.token and twitch.clientId
//
// Run:
//   ./gradlew :samples:helix-pagination:run

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
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

    var cursor: String? = null
    val maxPages = 3

    for (pageNumber in 1..maxPages) {
        val page = helix.search.channels(query = "development", pageSize = 5, cursor = cursor)

        println("--- Page $pageNumber ---")
        for (channel in page.data) {
            println("  ${channel.displayName} — ${channel.gameName} (live: ${channel.isLive})")
        }

        cursor = page.cursor
        if (cursor == null) {
            println("No more pages.")
            break
        }
    }

    httpClient.close()
}
