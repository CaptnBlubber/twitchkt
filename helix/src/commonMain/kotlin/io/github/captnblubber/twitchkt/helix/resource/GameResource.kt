package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Game

/**
 * Twitch Helix Games API resource.
 *
 * Provides methods for retrieving game/category information.
 *
 * Note: This resource returns lists directly rather than [Page]/[Flow] because [getGames] looks
 * up specific games by ID or name (bounded to 100, no cursor pagination), and [getTopGames]
 * supports both `before` and `after` cursors for bidirectional navigation, which does not fit the
 * forward-only auto-pagination model.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-games">Twitch API Reference - Games</a>
 */
class GameResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Games](https://dev.twitch.tv/docs/api/reference/#get-games)
     *
     * Gets information about specified categories or games.
     *
     * You may get up to 100 categories or games by specifying their ID or name. You may specify
     * all IDs, all names, or a combination of IDs and names. If you specify a combination of IDs
     * and names, the total number of IDs and names must not exceed 100.
     *
     * @param ids the IDs of the categories or games to get. You may specify a maximum of 100 IDs.
     * The endpoint ignores duplicate and invalid IDs or IDs that weren't found.
     * @param names the names of the categories or games to get. The name must exactly match the
     * category's or game's title. You may specify a maximum of 100 names. The endpoint ignores
     * duplicate names and names that weren't found.
     * @param igdbIds the [IGDB](https://www.igdb.com/) IDs of the games to get. You may specify
     * a maximum of 100 IDs. The endpoint ignores duplicate and invalid IDs or IDs that weren't found.
     * @return the list of categories and games. The list is empty if the specified categories and
     * games weren't found.
     */
    suspend fun getGames(
        ids: List<String> = emptyList(),
        names: List<String> = emptyList(),
        igdbIds: List<String> = emptyList(),
    ): List<Game> =
        http
            .get<Game>(
                "games",
                buildList {
                    ids.forEach { add("id" to it) }
                    names.forEach { add("name" to it) }
                    igdbIds.forEach { add("igdb_id" to it) }
                },
            ).data

    /**
     * [Twitch API: Get Top Games](https://dev.twitch.tv/docs/api/reference/#get-top-games)
     *
     * Gets information about all broadcasts on Twitch.
     *
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @param before the cursor used to get the previous page of results.
     * @return the list of broadcasts sorted by the number of viewers, with the most popular first.
     */
    suspend fun getTopGames(
        first: Int = 20,
        after: String? = null,
        before: String? = null,
    ): List<Game> =
        http
            .get<Game>(
                "games/top",
                buildList {
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                    before?.let { add("before" to it) }
                },
            ).data
}
