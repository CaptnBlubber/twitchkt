package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Response from starting a raid.
 *
 * @property createdAt the UTC date and time, in RFC3339 format, of when the raid was requested.
 * @property isMature **DEPRECATED** This field is deprecated and returns only false.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#start-a-raid">Twitch API Reference - Start a Raid</a>
 */
@Serializable
data class RaidResponse(
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("is_mature") val isMature: Boolean = false,
)
