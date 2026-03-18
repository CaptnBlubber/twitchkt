package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Represents a hype train event from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-hype-train-events">Twitch API Reference - Get Hype Train Events</a>
 */
@Serializable
data class HypeTrainEvent(
    val id: String,
    @SerialName("event_type") val eventType: String,
    @SerialName("event_timestamp") val eventTimestamp: String,
    val version: String,
    @SerialName("event_data") val eventData: JsonObject,
)
