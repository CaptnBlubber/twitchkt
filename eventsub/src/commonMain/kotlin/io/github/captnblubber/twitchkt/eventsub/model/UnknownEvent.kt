package io.github.captnblubber.twitchkt.eventsub.model

import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

data class UnknownEvent(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val rawPayload: JsonObject,
) : TwitchEvent
