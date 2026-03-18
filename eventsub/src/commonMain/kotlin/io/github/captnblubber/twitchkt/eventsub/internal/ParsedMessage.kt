package io.github.captnblubber.twitchkt.eventsub.internal

import io.github.captnblubber.twitchkt.eventsub.model.TwitchEvent
import io.github.captnblubber.twitchkt.eventsub.protocol.SessionPayload

internal sealed interface ParsedMessage {
    data class Welcome(
        val session: SessionPayload,
    ) : ParsedMessage

    data object Keepalive : ParsedMessage

    data class Reconnect(
        val session: SessionPayload,
    ) : ParsedMessage

    data class Notification(
        val event: TwitchEvent,
    ) : ParsedMessage

    data class Revocation(
        val subscriptionType: String,
        val status: String,
    ) : ParsedMessage
}
