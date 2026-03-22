package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

/**
 * Twitch Helix Subscriptions API resource.
 *
 * Provides methods for retrieving broadcaster subscriptions and checking user subscriptions.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-broadcaster-subscriptions">Twitch API Reference - Subscriptions</a>
 */
class SubscriptionResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Broadcaster Subscriptions](https://dev.twitch.tv/docs/api/reference/#get-broadcaster-subscriptions)
     *
     * Paginates through all subscriptions.
     *
     * @param broadcasterId the broadcaster's ID. This ID must match the user ID in the access token.
     * @return a [Flow] emitting each subscription.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS)
    fun getAll(broadcasterId: String): Flow<Subscription> {
        val params = listOf("broadcaster_id" to broadcasterId)
        return http
            .paginate<Subscription>("subscriptions", params)
            .onStart { http.validateScopes(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS) }
    }

    /**
     * [Twitch API: Get Broadcaster Subscriptions](https://dev.twitch.tv/docs/api/reference/#get-broadcaster-subscriptions)
     *
     * Gets a single page of the broadcaster's subscribers. The list is sorted by the date
     * and time each user subscribed (newest first).
     *
     * @param broadcasterId the broadcaster's ID. This ID must match the user ID in the access token.
     * @param userIds filters the list to include only the specified subscribers. You may specify a maximum of 100 user IDs. Do not specify [cursor] when using this filter.
     * @param cursor the cursor used to get the next page of results. Do not specify if you set [userIds].
     * @param pageSize the maximum number of items to return per page (1-100, default 20). Null uses the API default.
     * @return a [Page] of [Subscription] objects.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS)
    suspend fun get(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<Subscription> {
        http.validateScopes(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userIds.forEach { add("user_id" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "subscriptions", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Check User Subscription](https://dev.twitch.tv/docs/api/reference/#check-user-subscription)
     *
     * Checks whether the user subscribes to the broadcaster's channel.
     *
     * @param broadcasterId the ID of a partner or affiliate broadcaster.
     * @param userId the ID of the user that you're checking to see whether they subscribe to
     * the broadcaster. This ID must match the user ID in the access token.
     * @return the subscription information, or `null` if the user does not subscribe (404).
     */
    @RequiresScope(TwitchScope.USER_READ_SUBSCRIPTIONS)
    suspend fun checkUserSubscription(
        broadcasterId: String,
        userId: String,
    ): Subscription? {
        http.validateScopes(TwitchScope.USER_READ_SUBSCRIPTIONS)
        val params =
            listOf(
                "broadcaster_id" to broadcasterId,
                "user_id" to userId,
            )
        return try {
            http.get<Subscription>("subscriptions/user", params).data.firstOrNull()
        } catch (
            @Suppress("SwallowedException") e: TwitchApiException.NotFound,
        ) {
            null
        }
    }
}
