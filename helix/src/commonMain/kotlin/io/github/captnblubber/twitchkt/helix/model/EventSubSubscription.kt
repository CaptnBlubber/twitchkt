package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * [Twitch API: Create EventSub Subscription](https://dev.twitch.tv/docs/api/reference/#create-eventsub-subscription)
 *
 * @property id an ID that uniquely identifies this subscription.
 * @property status the subscription's status (e.g., `enabled` for WebSockets).
 * @property type the subscription's type (e.g., `channel.follow`).
 * @property version the version number of the subscription type's definition.
 * @property condition the subscription's parameter values, specific to the subscription type.
 * @property transport the transport used for notifications.
 * @property createdAt when the subscription was created.
 * @property cost how much this subscription counts against the application's subscription limit.
 */
@Serializable
data class EventSubSubscription(
    val id: String,
    val status: String,
    val type: String,
    val version: String,
    val condition: Map<String, String> = emptyMap(),
    val transport: EventSubTransport,
    @SerialName("created_at") val createdAt: Instant? = null,
    val cost: Int = 0,
)

/**
 * Transport configuration for an [EventSubSubscription].
 *
 * @property method the transport method: `webhook`, `websocket`, or `conduit`.
 * @property sessionId the WebSocket session ID (websocket only).
 * @property callback the callback URL where events are sent (webhook only).
 * @property secret the secret used to verify webhook event signatures.
 * @property conduitId the conduit ID (conduit only).
 */
@Serializable
data class EventSubTransport(
    val method: String,
    @SerialName("session_id") val sessionId: String? = null,
    val callback: String? = null,
    val secret: String? = null,
    @SerialName("conduit_id") val conduitId: String? = null,
)
