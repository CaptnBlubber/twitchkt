package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.EventSubSubscriptionType
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.EventSubSubscription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Twitch Helix EventSub API resource.
 *
 * Provides methods for managing EventSub subscriptions via the Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#create-eventsub-subscription">Twitch API Reference - EventSub</a>
 */
class EventSubResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Create EventSub Subscription](https://dev.twitch.tv/docs/api/reference/#create-eventsub-subscription)
     *
     * @param subscription the type-safe subscription definition containing type, version, and condition fields.
     * @param sessionId the WebSocket session ID from the welcome message.
     * @return the created EventSub subscription.
     */
    suspend fun create(
        subscription: EventSubSubscriptionType,
        sessionId: String,
    ): EventSubSubscription {
        val request =
            CreateEventSubRequest(
                type = subscription.type,
                version = subscription.version,
                condition = subscription.toCondition(),
                transport = EventSubTransportRequest(sessionId = sessionId),
            )
        return http.post<EventSubSubscription>("eventsub/subscriptions", body = http.encodeBody(request)).requireFirst("eventsub/subscriptions")
    }
}

@Serializable
internal data class CreateEventSubRequest(
    val type: String,
    val version: String,
    val condition: Map<String, String>,
    val transport: EventSubTransportRequest,
)

@Serializable
internal data class EventSubTransportRequest(
    val method: String = "websocket",
    @SerialName("session_id") val sessionId: String,
)
