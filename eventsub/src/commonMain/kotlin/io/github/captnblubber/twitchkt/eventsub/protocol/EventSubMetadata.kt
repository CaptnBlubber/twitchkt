package io.github.captnblubber.twitchkt.eventsub.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventSubMetadata(
    @SerialName("message_id") val messageId: String,
    @SerialName("message_type") val messageType: String,
    @SerialName("message_timestamp") val messageTimestamp: String,
    @SerialName("subscription_type") val subscriptionType: String? = null,
    @SerialName("subscription_version") val subscriptionVersion: String? = null,
)
