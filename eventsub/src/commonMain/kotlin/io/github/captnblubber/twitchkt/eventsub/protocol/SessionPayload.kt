package io.github.captnblubber.twitchkt.eventsub.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class SessionPayload(
    val id: String,
    val status: String,
    @SerialName("connected_at") val connectedAt: Instant? = null,
    @SerialName("keepalive_timeout_seconds") val keepaliveTimeoutSeconds: Int? = null,
    @SerialName("reconnect_url") val reconnectUrl: String? = null,
)
