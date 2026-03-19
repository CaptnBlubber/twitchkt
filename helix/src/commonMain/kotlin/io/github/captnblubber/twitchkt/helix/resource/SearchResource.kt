package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Game
import io.github.captnblubber.twitchkt.helix.model.SearchedChannel
import kotlinx.coroutines.flow.Flow

/**
 * Twitch Helix Search API resource.
 *
 * Provides methods for searching categories and channels.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#search-categories">Twitch API Reference - Search</a>
 */
class SearchResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Search Categories](https://dev.twitch.tv/docs/api/reference/#search-categories)
     *
     * Gets all categories that match the specified query.
     * Automatically paginates through all results.
     *
     * @param query the URI-encoded search string.
     * @return a [Flow] of [Game] objects.
     */
    fun getAllCategories(query: String): Flow<Game> {
        val params = listOf("query" to query)
        return http.paginate<Game>("search/categories", params)
    }

    /**
     * [Twitch API: Search Categories](https://dev.twitch.tv/docs/api/reference/#search-categories)
     *
     * Gets a single page of categories that match the specified query.
     *
     * @param query the URI-encoded search string.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [Game] objects.
     */
    suspend fun categories(
        query: String,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Game> {
        val params =
            buildList {
                add("query" to query)
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "search/categories", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Search Channels](https://dev.twitch.tv/docs/api/reference/#search-channels)
     *
     * Gets all channels that match the specified query.
     * Automatically paginates through all results.
     *
     * @param query the URI-encoded search string.
     * @param liveOnly a Boolean value that determines whether the response includes only channels that are currently streaming live. Default: `false`.
     * @return a [Flow] of [SearchedChannel] objects.
     */
    fun getAllChannels(
        query: String,
        liveOnly: Boolean = false,
    ): Flow<SearchedChannel> {
        val params =
            listOf(
                "query" to query,
                "live_only" to liveOnly.toString(),
            )
        return http.paginate<SearchedChannel>("search/channels", params)
    }

    /**
     * [Twitch API: Search Channels](https://dev.twitch.tv/docs/api/reference/#search-channels)
     *
     * Gets a single page of channels that match the specified query.
     *
     * @param query the URI-encoded search string.
     * @param liveOnly a Boolean value that determines whether the response includes only channels that are currently streaming live. Default: `false`.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [SearchedChannel] objects.
     */
    suspend fun channels(
        query: String,
        liveOnly: Boolean = false,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<SearchedChannel> {
        val params =
            buildList {
                add("query" to query)
                add("live_only" to liveOnly.toString())
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "search/channels", params = params, pageSize = pageSize)
    }
}
