package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * Base interface for all EventSub event types.
 *
 * @property subscriptionType The EventSub subscription type string (e.g. `channel.follow`).
 * @property messageId Unique ID for this EventSub message.
 * @property timestamp The [Instant] when the EventSub notification was sent, parsed from ISO-8601.
 */
sealed interface TwitchEvent {
    val subscriptionType: String
    val messageId: String
    val timestamp: Instant
}
