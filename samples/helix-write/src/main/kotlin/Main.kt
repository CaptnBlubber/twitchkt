// TwitchKt Sample: Helix Write
//
// Demonstrates a mutating Helix API call — sending a chat message.
//
// Setup:
//   1. Copy samples/local.properties.example to samples/local.properties
//   2. Fill in twitch.token, twitch.clientId, twitch.broadcasterId, and twitch.userId
//
// Run:
//   ./gradlew :samples:helix-write:run

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.github.captnblubber.twitchkt.helix.model.SendChatMessageRequest
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
    val broadcasterId = props.getProperty("twitch.broadcasterId")
    val userId = props.getProperty("twitch.userId")

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

    val response = helix.chat.sendMessage(
        SendChatMessageRequest(
            broadcasterId = broadcasterId,
            senderId = userId,
            message = "Hello from TwitchKt!",
        ),
    )

    if (response.isSent) {
        println("Message sent! ID: ${response.messageId}")
    } else {
        println("Message dropped: ${response.dropReason?.message}")
    }

    httpClient.close()
}
