package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a channel result from the Twitch Helix Search API.
 *
 * @property broadcasterLanguage the ISO 639-1 two-letter language code of the language used by
 * the broadcaster. For example, `en` for English. If the broadcaster uses a language not in the
 * list of [supported stream languages](https://help.twitch.tv/customer/portal/articles/2931082-languages-on-twitch#streamlang),
 * the value is `other`.
 * @property broadcasterLogin the broadcaster's login name.
 * @property displayName the broadcaster's display name.
 * @property gameId the ID of the game that the broadcaster is playing or last played.
 * @property gameName the name of the game that the broadcaster is playing or last played.
 * @property id an ID that uniquely identifies the channel (this is the broadcaster's ID).
 * @property isLive a Boolean value that determines whether the broadcaster is streaming live.
 * Is `true` if the broadcaster is streaming live; otherwise, `false`.
 * @property title the stream's title. Is an empty string if the broadcaster didn't set it.
 * @property startedAt the UTC date and time of when the broadcaster started streaming. The
 * string is empty if the broadcaster is not streaming live.
 * @property thumbnailUrl a URL to a thumbnail of the broadcaster's profile image.
 * @property tags the tags applied to the channel.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#search-channels">Twitch API Reference - Search Channels</a>
 */
@Serializable
data class SearchedChannel(
    @SerialName("broadcaster_language") val broadcasterLanguage: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("game_id") val gameId: String,
    @SerialName("game_name") val gameName: String,
    val id: String,
    @SerialName("is_live") val isLive: Boolean,
    val title: String,
    @SerialName("started_at") val startedAt: Instant? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
    val tags: List<String>? = null,
)
