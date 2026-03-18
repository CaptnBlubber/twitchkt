package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.RaidResponse

class RaidResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Start a Raid](https://dev.twitch.tv/docs/api/reference/#start-a-raid)
     *
     * Raid another channel by sending the broadcaster's viewers to the targeted channel.
     *
     * To determine whether the raid successfully occurred, you must subscribe to the Channel Raid event.
     *
     * **Rate Limit**: The limit is 10 requests within a 10-minute window.
     *
     * @param fromBroadcasterId the ID of the broadcaster that's sending the raiding party. This ID must match the user ID in the user access token.
     * @param toBroadcasterId the ID of the broadcaster to raid.
     * @return the [RaidResponse] with information about the pending raid.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_RAIDS)
    suspend fun start(
        fromBroadcasterId: String,
        toBroadcasterId: String,
    ): RaidResponse {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_RAIDS)
        val params =
            listOf(
                "from_broadcaster_id" to fromBroadcasterId,
                "to_broadcaster_id" to toBroadcasterId,
            )
        return http.post<RaidResponse>("raids", params = params).requireFirst("raids")
    }

    /**
     * [Twitch API: Cancel a Raid](https://dev.twitch.tv/docs/api/reference/#cancel-a-raid)
     *
     * Cancel a pending raid. You can cancel a raid at any point up until the broadcaster clicks
     * Raid Now in the Twitch UX or the 90-second countdown expires.
     *
     * **Rate Limit**: The limit is 10 requests within a 10-minute window.
     *
     * @param broadcasterId the ID of the broadcaster that initiated the raid. This ID must match the user ID in the user access token.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_RAIDS)
    suspend fun cancel(broadcasterId: String) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_RAIDS)
        val params = listOf("broadcaster_id" to broadcasterId)
        http.deleteNoContent("raids", params)
    }
}
