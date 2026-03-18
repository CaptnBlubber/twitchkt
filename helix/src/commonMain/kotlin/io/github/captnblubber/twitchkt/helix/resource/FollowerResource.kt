package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Follower
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

/**
 * Twitch Helix Channel Followers API resource.
 *
 * Provides methods for retrieving channel follower information.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-followers">Twitch API Reference - Channel Followers</a>
 */
class FollowerResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Channel Followers](https://dev.twitch.tv/docs/api/reference/#get-channel-followers)
     *
     * Paginates through all followers in descending order by follow date.
     *
     * This endpoint will return specific follower information only if the user access token includes
     * the `moderator:read:followers` scope and the user ID in the access token matches the
     * broadcaster_id or is a moderator for the specified broadcaster.
     *
     * @param broadcasterId the broadcaster's ID. Returns the list of users that follow this broadcaster.
     * @param userId a user's ID. Use this parameter to see whether the user follows this broadcaster.
     * If specified, the response contains this user if they follow the broadcaster.
     * @return a [Flow] emitting each follower.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_FOLLOWERS)
    fun listAll(
        broadcasterId: String,
        userId: String? = null,
    ): Flow<Follower> {
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userId?.let { add("user_id" to it) }
            }
        return http
            .paginate<Follower>("channels/followers", params)
            .onStart { http.validateScopes(TwitchScope.MODERATOR_READ_FOLLOWERS) }
    }

    /**
     * [Twitch API: Get Channel Followers](https://dev.twitch.tv/docs/api/reference/#get-channel-followers)
     *
     * Fetches a single page of followers in descending order by follow date.
     *
     * This endpoint will return specific follower information only if the user access token includes
     * the `moderator:read:followers` scope and the user ID in the access token matches the
     * broadcaster_id or is a moderator for the specified broadcaster.
     *
     * @param broadcasterId the broadcaster's ID.
     * @param userId a user's ID to filter the results to a specific follower.
     * @param cursor the cursor used to get the next page of results. Pass `null` to get the first page.
     * @param pageSize the maximum number of items to return (1–100). `null` uses the API default (20).
     * @return a [Page] containing the followers on this page and the cursor for the next page.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_FOLLOWERS)
    suspend fun list(
        broadcasterId: String,
        userId: String? = null,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Follower> {
        http.validateScopes(TwitchScope.MODERATOR_READ_FOLLOWERS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userId?.let { add("user_id" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "channels/followers", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Get Channel Followers](https://dev.twitch.tv/docs/api/reference/#get-channel-followers)
     *
     * Returns only the total follower count without fetching individual records.
     *
     * @param broadcasterId the ID of the broadcaster whose follower count to get.
     * @return the total number of followers.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_FOLLOWERS)
    suspend fun getTotal(broadcasterId: String): Int {
        http.validateScopes(TwitchScope.MODERATOR_READ_FOLLOWERS)
        val params = listOf("broadcaster_id" to broadcasterId, "first" to "1")
        return http.get<Follower>("channels/followers", params).total ?: 0
    }
}
