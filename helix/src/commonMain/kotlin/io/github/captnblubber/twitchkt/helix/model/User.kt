package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a Twitch user from the Helix API.
 *
 * @property id an ID that identifies the user.
 * @property login the user's login name.
 * @property displayName the user's display name.
 * @property type the type of user. Possible values are: `admin`, `global_mod`, `staff`, `""` (normal user).
 * @property broadcasterType the type of broadcaster. Possible values are: `affiliate`, `partner`, `""` (normal broadcaster).
 * @property description the user's description of their channel.
 * @property profileImageUrl a URL to the user's profile image.
 * @property offlineImageUrl a URL to the user's offline image.
 * @property viewCount **DEPRECATED** The number of times the user's channel has been viewed. Any data in this field is not valid and should not be used.
 * @property email the user's verified email address. The object includes this field only if the user access token includes the `user:read:email` scope.
 * @property createdAt the UTC date and time that the user's account was created. The timestamp is in RFC3339 format.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-users">Twitch API Reference - Get Users</a>
 */
@Serializable
data class User(
    val id: String,
    val login: String,
    @SerialName("display_name") val displayName: String,
    val type: String = "",
    @SerialName("broadcaster_type") val broadcasterType: String = "",
    val description: String = "",
    @SerialName("profile_image_url") val profileImageUrl: String = "",
    @SerialName("offline_image_url") val offlineImageUrl: String = "",
    @Deprecated("This field has been deprecated by Twitch. Any data in this field is not valid.")
    @SerialName("view_count") val viewCount: Int = 0,
    val email: String? = null,
    @SerialName("created_at") val createdAt: Instant? = null,
)
