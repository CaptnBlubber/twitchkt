package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a suspicious user status on a broadcaster's channel.
 *
 * @property userId the ID of the user being given or having the suspicious status removed.
 * @property broadcasterId the user ID of the broadcaster indicating in which channel the status is being applied.
 * @property moderatorId the user ID of the moderator who applied or modified the last status.
 * @property updatedAt the timestamp of the last time this user's status was updated.
 * @property status the type of suspicious status. Possible values are: `ACTIVE_MONITORING`, `RESTRICTED`, `NO_TREATMENT`.
 * @property types an array of strings representing the type(s) of suspicious user this is. Possible values are: `MANUALLY_ADDED`, `DETECTED_BAN_EVADER`, `DETECTED_SUS_CHATTER`, `BANNED_IN_SHARED_CHANNEL`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#add-suspicious-status-to-chat-user">Twitch API Reference - Add Suspicious Status to Chat User</a>
 */
@Serializable
data class SuspiciousUserStatus(
    @SerialName("user_id") val userId: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("moderator_id") val moderatorId: String,
    @SerialName("updated_at") val updatedAt: String,
    val status: String,
    val types: List<String>,
)
