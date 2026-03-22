package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.BitsLeaderEntry
import io.github.captnblubber.twitchkt.helix.model.BitsLeaderboardPeriod
import io.github.captnblubber.twitchkt.helix.model.Cheermote

/**
 * Twitch Helix Bits API resource.
 *
 * Provides methods for retrieving Bits leaderboards and Cheermotes.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-bits-leaderboard">Twitch API Reference - Bits</a>
 */
class BitsResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Bits Leaderboard](https://dev.twitch.tv/docs/api/reference/#get-bits-leaderboard)
     *
     * Gets the Bits leaderboard for the authenticated broadcaster.
     *
     * @param count the number of results to return. The minimum count is 1 and the maximum is 100.
     * The default is 10.
     * @param period the time period over which data is aggregated (uses the PST time zone). Possible
     * values are: `day`, `week`, `month`, `year`, `all`. The default is `all`.
     * @param startedAt the start date, in RFC3339 format, used for determining the aggregation period.
     * Specify this parameter only if you specify the [period] parameter. The start date is ignored if
     * [period] is `all`.
     * @param userId an ID that identifies a user that cheered bits in the channel. If [count] is
     * greater than 1, the response may include users ranked above and below the specified user.
     * To get the leaderboard's top leaders, don't specify a user ID.
     * @return a list of leaderboard leaders, returned in rank order by how much they've cheered.
     * The list is empty if nobody has cheered bits.
     */
    @RequiresScope(TwitchScope.BITS_READ)
    suspend fun getLeaderboard(
        count: Int = 10,
        period: BitsLeaderboardPeriod = BitsLeaderboardPeriod.ALL,
        startedAt: String? = null,
        userId: String? = null,
    ): List<BitsLeaderEntry> {
        http.validateScopes(TwitchScope.BITS_READ)
        return http
            .get<BitsLeaderEntry>(
                "bits/leaderboard",
                buildList {
                    add("count" to count.toString())
                    add("period" to period.value)
                    startedAt?.let { add("started_at" to it) }
                    userId?.let { add("user_id" to it) }
                },
            ).data
    }

    /**
     * [Twitch API: Get Cheermotes](https://dev.twitch.tv/docs/api/reference/#get-cheermotes)
     *
     * Gets a list of Cheermotes that users can use to cheer Bits in any Bits-enabled channel's
     * chat room. Cheermotes are animated emotes that viewers can assign Bits to.
     *
     * @param broadcasterId the ID of the broadcaster whose custom Cheermotes you want to get.
     * Specify the broadcaster's ID if you want to include the broadcaster's Cheermotes in the
     * response (not all broadcasters upload Cheermotes). If not specified, the response contains
     * only global Cheermotes. If the broadcaster uploaded Cheermotes, the `type` field in the
     * response is set to `channel_custom`.
     * @return the list of Cheermotes, in ascending order by the `order` field's value.
     */
    suspend fun getCheermotes(broadcasterId: String? = null): List<Cheermote> =
        http
            .get<Cheermote>(
                "bits/cheermotes",
                buildList {
                    broadcasterId?.let { add("broadcaster_id" to it) }
                },
            ).data
}
