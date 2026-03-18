package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Start Commercial](https://dev.twitch.tv/docs/api/reference/#start-commercial)
 *
 * @property length the length of the commercial in seconds.
 * @property message a message describing the result.
 * @property retryAfter the number of seconds before the broadcaster can run the next commercial.
 */
@Serializable
data class Commercial(
    val length: Int,
    val message: String = "",
    @SerialName("retry_after") val retryAfter: Int = 0,
)
