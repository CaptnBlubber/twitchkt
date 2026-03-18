package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents an active shared chat session.
 *
 * @property sessionId the unique identifier for the shared chat session.
 * @property hostBroadcasterId the User ID of the host channel.
 * @property participants the list of participants in the shared chat session.
 * @property createdAt the UTC date and time of when the shared chat session began.
 * @property updatedAt the UTC date and time of when the shared chat session was last updated.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-shared-chat-session">Twitch API Reference - Get Shared Chat Session</a>
 */
@Serializable
data class SharedChatSession(
    @SerialName("session_id") val sessionId: String,
    @SerialName("host_broadcaster_id") val hostBroadcasterId: String,
    val participants: List<SharedChatParticipant>,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
)

/**
 * Represents a participant in a shared chat session.
 *
 * @property broadcasterId the User ID of the participant channel.
 */
@Serializable
data class SharedChatParticipant(
    @SerialName("broadcaster_id") val broadcasterId: String,
)
