package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a stream schedule from the Twitch Helix API.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-stream-schedule">Twitch API Reference - Get Channel Stream Schedule</a>
 */
@Serializable
data class StreamSchedule(
    val segments: List<ScheduleSegment>? = null,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    val vacation: ScheduleVacation? = null,
)

/**
 * Represents a segment in a stream schedule.
 */
@Serializable
data class ScheduleSegment(
    val id: String,
    @SerialName("start_time") val startTime: Instant,
    @SerialName("end_time") val endTime: Instant,
    val title: String,
    @SerialName("canceled_until") val canceledUntil: Instant? = null,
    val category: ScheduleCategory? = null,
    @SerialName("is_recurring") val isRecurring: Boolean,
)

/**
 * Represents a category associated with a schedule segment.
 */
@Serializable
data class ScheduleCategory(
    val id: String,
    val name: String,
)

/**
 * Represents a vacation period in a stream schedule.
 */
@Serializable
data class ScheduleVacation(
    @SerialName("start_time") val startTime: Instant,
    @SerialName("end_time") val endTime: Instant,
)
