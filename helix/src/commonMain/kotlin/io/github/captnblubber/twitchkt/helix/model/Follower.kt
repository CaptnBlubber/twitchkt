package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * [Twitch API: Get Channel Followers](https://dev.twitch.tv/docs/api/reference/#get-channel-followers)
 *
 * @property userId an ID that uniquely identifies the user that's following the broadcaster.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property followedAt when the user started following the broadcaster.
 */
@Serializable
data class Follower(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("followed_at") val followedAt: Instant,
)
