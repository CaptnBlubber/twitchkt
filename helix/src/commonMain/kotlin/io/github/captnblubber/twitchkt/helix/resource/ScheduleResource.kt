package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.StreamSchedule
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Schedule API resource.
 *
 * Provides methods for managing and retrieving channel stream schedules.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-stream-schedule">Twitch API Reference - Schedule</a>
 */
class ScheduleResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Channel Stream Schedule](https://dev.twitch.tv/docs/api/reference/#get-channel-stream-schedule)
     *
     * Gets the broadcaster's streaming schedule. You can get the entire schedule or specific
     * segments of the schedule.
     *
     * @param broadcasterId the ID of the broadcaster that owns the streaming schedule you want to get.
     * @param ids the ID of the scheduled segment to return. You may specify a maximum of 100 IDs.
     * @param startTime the UTC date and time that identifies when in the broadcaster's schedule to start returning segments. If not specified, the request returns segments starting after the current UTC date and time. Specify the date and time in RFC3339 format.
     * @param first the maximum number of items to return per page in the response. The minimum page size is 1 item per page and the maximum is 25 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the [StreamSchedule] for the channel.
     */
    suspend fun getSchedule(
        broadcasterId: String,
        ids: List<String> = emptyList(),
        startTime: String? = null,
        first: Int = 20,
        after: String? = null,
    ): StreamSchedule =
        http
            .get<StreamSchedule>(
                "schedule",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    ids.forEach { add("id" to it) }
                    startTime?.let { add("start_time" to it) }
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                },
            ).requireFirst("schedule")

    /**
     * [Twitch API: Update Channel Stream Schedule](https://dev.twitch.tv/docs/api/reference/#update-channel-stream-schedule)
     *
     * Updates the broadcaster's schedule settings, such as scheduling a vacation.
     *
     * @param broadcasterId the ID of the broadcaster whose schedule settings you want to update. The ID must match the user ID in the user access token.
     * @param isVacationEnabled a Boolean value that indicates whether the broadcaster has scheduled a vacation. Set to true to enable Vacation Mode and add vacation dates, or false to cancel a previously scheduled vacation.
     * @param vacationStartTime the UTC date and time of when the broadcaster's vacation starts. Specify the date and time in RFC3339 format. Required if [isVacationEnabled] is true.
     * @param vacationEndTime the UTC date and time of when the broadcaster's vacation ends. Specify the date and time in RFC3339 format. Required if [isVacationEnabled] is true.
     * @param timezone the time zone that the broadcaster broadcasts from. Specify the time zone using IANA time zone database format. Required if [isVacationEnabled] is true.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
    suspend fun updateSettings(
        broadcasterId: String,
        isVacationEnabled: Boolean? = null,
        vacationStartTime: String? = null,
        vacationEndTime: String? = null,
        timezone: String? = null,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
        http.patchNoContent(
            "schedule/settings",
            params =
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    isVacationEnabled?.let { add("is_vacation_enabled" to it.toString()) }
                    vacationStartTime?.let { add("vacation_start_time" to it) }
                    vacationEndTime?.let { add("vacation_end_time" to it) }
                    timezone?.let { add("timezone" to it) }
                },
        )
    }

    /**
     * [Twitch API: Create Channel Stream Schedule Segment](https://dev.twitch.tv/docs/api/reference/#create-channel-stream-schedule-segment)
     *
     * Adds a single or recurring broadcast to the broadcaster's streaming schedule.
     *
     * @param broadcasterId the ID of the broadcaster that owns the schedule to add the broadcast segment to. This ID must match the user ID in the user access token.
     * @param startTime the date and time that the broadcast segment starts. Specify the date and time in RFC3339 format.
     * @param timezone the time zone where the broadcast takes place. Specify the time zone using IANA time zone database format.
     * @param duration the length of time, in minutes, that the broadcast is scheduled to run. The duration must be in the range 30 through 1380 (23 hours).
     * @param isRecurring a Boolean value that determines whether the broadcast recurs weekly. Only partners and affiliates may add non-recurring broadcasts.
     * @param categoryId the ID of the category that best represents the broadcast's content.
     * @param title the broadcast's title. The title may contain a maximum of 140 characters.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
    suspend fun createSegment(
        broadcasterId: String,
        startTime: String,
        timezone: String,
        duration: Int = 240,
        isRecurring: Boolean = false,
        categoryId: String? = null,
        title: String? = null,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
        http.postNoContent(
            "schedule/segment",
            params = listOf("broadcaster_id" to broadcasterId),
            body =
                http.encodeBody(
                    CreateScheduleSegmentRequest(
                        startTime = startTime,
                        timezone = timezone,
                        duration = duration.toString(),
                        isRecurring = isRecurring,
                        categoryId = categoryId,
                        title = title,
                    ),
                ),
        )
    }

    /**
     * [Twitch API: Update Channel Stream Schedule Segment](https://dev.twitch.tv/docs/api/reference/#update-channel-stream-schedule-segment)
     *
     * Updates a scheduled broadcast segment. For recurring segments, updating a segment's title,
     * category, duration, and timezone, changes all segments in the recurring schedule, not just
     * the specified segment.
     *
     * @param broadcasterId the ID of the broadcaster who owns the broadcast segment to update. This ID must match the user ID in the user access token.
     * @param segmentId the ID of the broadcast segment to update.
     * @param startTime the date and time that the broadcast segment starts. Specify the date and time in RFC3339 format. Only partners and affiliates may update a broadcast's start time and only for non-recurring segments.
     * @param timezone the time zone where the broadcast takes place. Specify the time zone using IANA time zone database format.
     * @param duration the length of time, in minutes, that the broadcast is scheduled to run. The duration must be in the range 30 through 1380 (23 hours).
     * @param isCanceled a Boolean value that indicates whether the broadcast is canceled. Set to true to cancel the segment.
     * @param categoryId the ID of the category that best represents the broadcast's content.
     * @param title the broadcast's title. The title may contain a maximum of 140 characters.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
    suspend fun updateSegment(
        broadcasterId: String,
        segmentId: String,
        startTime: String? = null,
        timezone: String? = null,
        duration: Int? = null,
        isCanceled: Boolean? = null,
        categoryId: String? = null,
        title: String? = null,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
        http.patchNoContent(
            "schedule/segment",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "id" to segmentId,
                ),
            body =
                http.encodeBody(
                    UpdateScheduleSegmentRequest(
                        startTime = startTime,
                        timezone = timezone,
                        duration = duration?.toString(),
                        isCanceled = isCanceled,
                        categoryId = categoryId,
                        title = title,
                    ),
                ),
        )
    }

    /**
     * [Twitch API: Delete Channel Stream Schedule Segment](https://dev.twitch.tv/docs/api/reference/#delete-channel-stream-schedule-segment)
     *
     * Removes a broadcast segment from the broadcaster's streaming schedule.
     *
     * **NOTE**: For recurring segments, removing a segment removes all segments in the recurring schedule.
     *
     * @param broadcasterId the ID of the broadcaster that owns the streaming schedule. This ID must match the user ID in the user access token.
     * @param segmentId the ID of the broadcast segment to remove.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
    suspend fun deleteSegment(
        broadcasterId: String,
        segmentId: String,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_SCHEDULE)
        http.deleteNoContent(
            "schedule/segment",
            listOf(
                "broadcaster_id" to broadcasterId,
                "id" to segmentId,
            ),
        )
    }
}

@Serializable
internal data class CreateScheduleSegmentRequest(
    @SerialName("start_time") val startTime: String,
    val timezone: String,
    val duration: String,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    @SerialName("category_id") val categoryId: String? = null,
    val title: String? = null,
)

@Serializable
internal data class UpdateScheduleSegmentRequest(
    @SerialName("start_time") val startTime: String? = null,
    val timezone: String? = null,
    val duration: String? = null,
    @SerialName("is_canceled") val isCanceled: Boolean? = null,
    @SerialName("category_id") val categoryId: String? = null,
    val title: String? = null,
)
