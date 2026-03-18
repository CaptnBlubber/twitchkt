package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Game
import io.github.captnblubber.twitchkt.helix.model.SearchedChannel

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
     * Gets the games or categories that match the specified query.
     *
     * To match, the category's name must contain all parts of the query string. For example,
     * if the query string is "just for]", the response includes categories with names that
     * contain "just" and "for" but not necessarily in that order.
     *
     * @param query the URI-encoded search string. For example, encode "#702702" as "%23702702".
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the list of categories that match the query.
     */
    suspend fun categories(
        query: String,
        first: Int = 20,
        after: String? = null,
    ): List<Game> {
        val params =
            buildList {
                add("query" to query)
                add("first" to first.toString())
                after?.let { add("after" to it) }
            }
        return http.get<Game>("search/categories", params).data
    }

    /**
     * [Twitch API: Search Channels](https://dev.twitch.tv/docs/api/reference/#search-channels)
     *
     * Gets the channels that match the specified query and have streamed content within the
     * past 6 months.
     *
     * The fields that the API uses for comparison depends on the value that the [liveOnly]
     * query parameter is set to. If `true`, the API matches on the broadcaster's login name.
     * Otherwise, the API matches on the broadcaster's login name and display name.
     *
     * To match, the beginning of the broadcaster's name must match the query string. The
     * comparison is case insensitive. If the query string is "]]a]b]c", the response includes
     * all names that begin with "abc".
     *
     * @param query the URI-encoded search string. For example, encode "#702702" as "%23702702".
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100. The default is 20.
     * @param liveOnly a Boolean value that determines whether the response includes only
     * channels that are currently streaming live. Set to `true` to get only channels that are
     * streaming live; otherwise, `false` to get live and offline channels. The default is `false`.
     * @param after the cursor used to get the next page of results.
     * @return the list of channels that match the query.
     */
    suspend fun channels(
        query: String,
        first: Int = 20,
        liveOnly: Boolean = false,
        after: String? = null,
    ): List<SearchedChannel> {
        val params =
            buildList {
                add("query" to query)
                add("first" to first.toString())
                add("live_only" to liveOnly.toString())
                after?.let { add("after" to it) }
            }
        return http.get<SearchedChannel>("search/channels", params).data
    }
}
