package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a moderator or VIP from the Twitch Helix API.
 * Used by Get Moderators and Get VIPs endpoints which return `user_id`/`user_login`/`user_name`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-moderators">Twitch API Reference - Get Moderators</a>
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-vips">Twitch API Reference - Get VIPs</a>
 */
@Serializable
data class ChannelRoleUser(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
)

/**
 * Represents a banned user from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-banned-users">Twitch API Reference - Get Banned Users</a>
 */
@Serializable
data class BannedUser(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("created_at") val createdAt: Instant,
    val reason: String,
    @SerialName("moderator_id") val moderatorId: String,
    @SerialName("moderator_login") val moderatorLogin: String,
    @SerialName("moderator_name") val moderatorName: String,
)

/**
 * Represents a blocked term from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-blocked-terms">Twitch API Reference - Get Blocked Terms</a>
 */
@Serializable
data class BlockedTerm(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("moderator_id") val moderatorId: String,
    val id: String,
    val text: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("expires_at") val expiresAt: Instant? = null,
)

/**
 * Represents shield mode status from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-shield-mode-status">Twitch API Reference - Get Shield Mode Status</a>
 */
@Serializable
data class ShieldModeStatus(
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("moderator_id") val moderatorId: String,
    @SerialName("moderator_login") val moderatorLogin: String,
    @SerialName("moderator_name") val moderatorName: String,
    @SerialName("last_activated_at") val lastActivatedAt: Instant,
)

/**
 * Represents an unban request from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-unban-requests">Twitch API Reference - Get Unban Requests</a>
 */
@Serializable
data class UnbanRequestResponse(
    val id: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("moderator_id") val moderatorId: String? = null,
    @SerialName("moderator_login") val moderatorLogin: String? = null,
    @SerialName("moderator_name") val moderatorName: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val text: String,
    val status: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("resolved_at") val resolvedAt: Instant? = null,
    @SerialName("resolution_text") val resolutionText: String? = null,
)
