package io.github.captnblubber.twitchkt.eventsub.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class EventSubFrame(
    val metadata: EventSubMetadata,
    val payload: JsonObject,
)
