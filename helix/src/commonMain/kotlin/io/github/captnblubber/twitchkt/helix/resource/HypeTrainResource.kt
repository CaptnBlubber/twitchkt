package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.HypeTrainEvent
import io.github.captnblubber.twitchkt.helix.model.HypeTrainStatus

/**
 * Twitch Helix Hype Train API resource.
 *
 * Provides methods for retrieving Hype Train events and status.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-hype-train-events">Twitch API Reference - Hype Train</a>
 */
class HypeTrainResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Hype Train Events](https://dev.twitch.tv/docs/api/reference/#get-hype-train-events)
     *
     * Gets the Hype Train events for the specified broadcaster.
     *
     * @param broadcasterId the ID of the broadcaster whose hype train events to get.
     * @param first maximum number of items to return (1-100, default 1).
     * @return list of [HypeTrainEvent] objects.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_HYPE_TRAIN)
    suspend fun getEvents(
        broadcasterId: String,
        first: Int = 1,
    ): List<HypeTrainEvent> {
        http.validateScopes(TwitchScope.CHANNEL_READ_HYPE_TRAIN)
        return http
            .get<HypeTrainEvent>(
                "hypetrain/events",
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "first" to first.toString(),
                ),
            ).data
    }

    /**
     * [Twitch API: Get Hype Train Status](https://dev.twitch.tv/docs/api/reference/#get-hype-train-status)
     *
     * Gets the status of a Hype Train for the specified broadcaster, including the current
     * active Hype Train (if any) and all-time high records.
     *
     * @param broadcasterId the user ID of the channel broadcaster. Must match the user ID
     * in the access token.
     * @return the Hype Train status for the channel.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_HYPE_TRAIN)
    suspend fun getStatus(broadcasterId: String): HypeTrainStatus {
        http.validateScopes(TwitchScope.CHANNEL_READ_HYPE_TRAIN)
        return http
            .get<HypeTrainStatus>(
                "hypetrain/status",
                listOf("broadcaster_id" to broadcasterId),
            ).requireFirst("hypetrain/status")
    }
}
