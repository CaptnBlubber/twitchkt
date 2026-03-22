// TwitchKt Sample: EventSub WebSocket
//
// Demonstrates connecting to Twitch EventSub via WebSocket and receiving real-time
// chat message events. Runs until cancelled (Ctrl+C).
//
// Setup:
//   1. Copy samples/local.properties.example to samples/local.properties
//   2. Fill in twitch.token, twitch.clientId, twitch.broadcasterId, and twitch.userId
//
// Run:
//   ./gradlew :samples:eventsub:run

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.eventsub.EventSubSubscriptionType
import io.github.captnblubber.twitchkt.eventsub.TwitchEventSub
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatMessage
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Properties

suspend fun main(): Unit = coroutineScope {
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
        install(WebSockets)
    }

    val config = TwitchKtConfig(
        clientId = clientId,
        tokenProvider = TokenProvider { token },
    )

    val helix = TwitchHelix(httpClient, config)
    val eventSub = TwitchEventSub(httpClient, config, helix.eventSub)

    eventSub.subscribe(
        EventSubSubscriptionType.ChannelChatMessage(
            broadcasterUserId = broadcasterId,
            userId = userId,
        ),
    )

    eventSub.connect(this)

    println("Listening for chat messages... (Ctrl+C to stop)")

    eventSub.events.collect { event ->
        if (event is ChannelChatMessage) {
            println("[${event.chatterUserName}] ${event.message.text}")
        }
    }
}
