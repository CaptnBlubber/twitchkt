package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.ChannelEditor
import io.github.captnblubber.twitchkt.helix.model.ChannelInformation
import io.github.captnblubber.twitchkt.helix.model.FollowedChannel
import io.github.captnblubber.twitchkt.helix.model.UpdateChannelRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class ChannelResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Channel Information](https://dev.twitch.tv/docs/api/reference/#get-channel-information)
     *
     * Gets information about one or more channels.
     *
     * @param broadcasterIds the IDs of the broadcasters whose channels you want to get. You may
     * specify a maximum of 100 IDs. The API ignores duplicate IDs and IDs that are not found.
     * @return a list of channel information objects. The list is empty if the specified channels
     * weren't found.
     */
    suspend fun getInformation(broadcasterIds: List<String>): List<ChannelInformation> {
        val params = broadcasterIds.map { "broadcaster_id" to it }
        return http.get<ChannelInformation>("channels", params).data
    }

    /**
     * Convenience overload that gets channel information for a single broadcaster.
     *
     * @see [getInformation]
     */
    suspend fun getInformation(broadcasterId: String): ChannelInformation =
        getInformation(listOf(broadcasterId)).firstOrNull()
            ?: throw TwitchApiException.EmptyResponse("channels")

    /**
     * [Twitch API: Modify Channel Information](https://dev.twitch.tv/docs/api/reference/#modify-channel-information)
     *
     * Updates a channel's properties.
     *
     * @param broadcasterId the ID of the broadcaster whose channel you want to update. This ID must match the user ID in the user access token.
     * @param request the fields to update. All fields are optional, but at least one must be specified.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_BROADCAST)
    suspend fun update(
        broadcasterId: String,
        request: UpdateChannelRequest,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_BROADCAST)
        val params = listOf("broadcaster_id" to broadcasterId)
        http.patchNoContent("channels", body = http.encodeBody(request), params = params)
    }

    /**
     * [Twitch API: Get Channel Editors](https://dev.twitch.tv/docs/api/reference/#get-channel-editors)
     *
     * Gets the broadcaster's list of editors.
     *
     * @param broadcasterId the ID of the broadcaster that owns the channel. This ID must match the user ID in the access token.
     * @return a list of users that are editors for the specified broadcaster. The list is empty if the broadcaster doesn't have editors.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_EDITORS)
    suspend fun getEditors(broadcasterId: String): List<ChannelEditor> {
        http.validateScopes(TwitchScope.CHANNEL_READ_EDITORS)
        return http
            .get<ChannelEditor>(
                "channels/editors",
                listOf("broadcaster_id" to broadcasterId),
            ).data
    }

    /**
     * [Twitch API: Get Followed Channels](https://dev.twitch.tv/docs/api/reference/#get-followed-channels)
     *
     * Gets all broadcasters that the specified user follows.
     * Automatically paginates through all results.
     *
     * @param userId a user's ID. Returns the list of broadcasters that this user follows. This ID must match the user ID in the user OAuth token.
     * @param broadcasterId a broadcaster's ID. Use this parameter to see whether the user follows this broadcaster.
     * @return a [Flow] of [FollowedChannel] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_FOLLOWS)
    fun getAllFollowedChannels(
        userId: String,
        broadcasterId: String? = null,
    ): Flow<FollowedChannel> {
        val params =
            buildList {
                add("user_id" to userId)
                broadcasterId?.let { add("broadcaster_id" to it) }
            }
        return http
            .paginate<FollowedChannel>("channels/followed", params)
            .onStart { http.validateScopes(TwitchScope.USER_READ_FOLLOWS) }
    }

    /**
     * [Twitch API: Get Followed Channels](https://dev.twitch.tv/docs/api/reference/#get-followed-channels)
     *
     * Gets a single page of broadcasters that the specified user follows.
     *
     * @param userId a user's ID. Returns the list of broadcasters that this user follows. This ID must match the user ID in the user OAuth token.
     * @param broadcasterId a broadcaster's ID. Use this parameter to see whether the user follows this broadcaster.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [FollowedChannel] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_FOLLOWS)
    suspend fun getFollowedChannels(
        userId: String,
        broadcasterId: String? = null,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<FollowedChannel> {
        http.validateScopes(TwitchScope.USER_READ_FOLLOWS)
        val params =
            buildList {
                add("user_id" to userId)
                broadcasterId?.let { add("broadcaster_id" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "channels/followed", params = params, pageSize = pageSize)
    }
}
