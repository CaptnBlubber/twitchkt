package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a game or category on Twitch.
 *
 * @property id an ID that identifies the category or game.
 * @property name the category's or game's name.
 * @property boxArtUrl a URL to the category's or game's box art. You must replace the
 * `{width}x{height}` placeholder with the size of image you want.
 * @property igdbId the ID that [IGDB](https://www.igdb.com/) uses to identify this game.
 * If the IGDB ID is not available to Twitch, this field is set to an empty string.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-games">Twitch API Reference - Get Games</a>
 */
@Serializable
data class Game(
    val id: String,
    val name: String,
    @SerialName("box_art_url") val boxArtUrl: String = "",
    @SerialName("igdb_id") val igdbId: String = "",
)
