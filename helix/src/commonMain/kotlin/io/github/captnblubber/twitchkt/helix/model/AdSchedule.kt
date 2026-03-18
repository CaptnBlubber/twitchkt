package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlin.time.Instant

/**
 * [Twitch API: Get Ad Schedule](https://dev.twitch.tv/docs/api/reference/#get-ad-schedule)
 *
 * @property nextAdAt when the broadcaster's next scheduled ad will run, or `null` if none.
 * @property lastAdAt when the broadcaster's last ad break ran, or `null` if none.
 * @property duration the length in seconds of the scheduled upcoming ad break.
 * @property prerollFreeTime the amount of pre-roll free time remaining for the channel in seconds.
 * @property snoozeCount the number of snoozes available for the broadcaster.
 * @property snoozeRefreshAt when the broadcaster will gain an additional snooze, or `null` if none.
 */
data class AdSchedule(
    val nextAdAt: Instant? = null,
    val lastAdAt: Instant? = null,
    val duration: Int = 0,
    val prerollFreeTime: Int = 0,
    val snoozeCount: Int = 0,
    val snoozeRefreshAt: Instant? = null,
)

@Serializable
internal data class RawAdSchedule(
    @SerialName("next_ad_at") val nextAdAt: JsonElement = JsonPrimitive(""),
    @SerialName("last_ad_at") val lastAdAt: JsonElement = JsonPrimitive(""),
    val duration: Int = 0,
    @SerialName("preroll_free_time") val prerollFreeTime: Int = 0,
    @SerialName("snooze_count") val snoozeCount: Int = 0,
    @SerialName("snooze_refresh_at") val snoozeRefreshAt: JsonElement = JsonPrimitive(""),
) {
    fun toAdSchedule(): AdSchedule =
        AdSchedule(
            nextAdAt = nextAdAt.toInstantOrNull(),
            lastAdAt = lastAdAt.toInstantOrNull(),
            duration = duration,
            prerollFreeTime = prerollFreeTime,
            snoozeCount = snoozeCount,
            snoozeRefreshAt = snoozeRefreshAt.toInstantOrNull(),
        )
}

private fun JsonElement.toInstantOrNull(): Instant? {
    val primitive = this as? JsonPrimitive ?: return null
    val content = primitive.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
    return content.toLongOrNull()?.let { Instant.fromEpochSeconds(it) } ?: Instant.parse(content)
}
