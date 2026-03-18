package io.github.captnblubber.twitchkt.helix.resource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.EventSubSubscriptionType
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.EventSubSubscription
import io.github.captnblubber.twitchkt.helix.model.Subscription

/**
 * Twitch Helix Subscriptions and EventSub API resource.
 *
 * Provides methods for retrieving broadcaster subscriptions, checking user subscriptions,
 * and managing EventSub subscriptions.
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
    fun list(broadcasterId: String): Flow<Subscription> {
        val params = listOf("broadcaster_id" to broadcasterId)
        return http
            .paginate<Subscription>("subscriptions", params)
            .onStart { http.validateScopes(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS) }
    }

    /**
     * [Twitch API: Get Broadcaster Subscriptions](https://dev.twitch.tv/docs/api/reference/#get-broadcaster-subscriptions)
     *
     * Gets a list of users that subscribe to the specified broadcaster. Use this method for
     * filtered queries or when you need a single page of results.
     *
     * @param broadcasterId the broadcaster's ID. This ID must match the user ID in the access token.
     * @param userIds filters the list to include only the specified subscribers. You may specify
     * a maximum of 100 subscriber IDs. Do not specify [after] or [before] if you set this parameter.
     * @param first the maximum number of items to return per page (1-100, default 20).
     * @param after the cursor used to get the next page of results. Do not specify if you set [userIds].
     * @param before the cursor used to get the previous page of results. Do not specify if you set [userIds].
     * @return the list of subscriptions.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS)
    suspend fun get(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
        first: Int = 20,
        after: String? = null,
        before: String? = null,
    ): List<Subscription> {
        http.validateScopes(TwitchScope.CHANNEL_READ_SUBSCRIPTIONS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userIds.forEach { add("user_id" to it) }
                add("first" to first.toString())
                after?.let { add("after" to it) }
                before?.let { add("before" to it) }
            }
        return http.get<Subscription>("subscriptions", params).data
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
            @Suppress("SwallowedException") e: io.github.captnblubber.twitchkt.error.TwitchApiException.NotFound,
        ) {
            null
        }
    }

    /**
     * [Twitch API: Create EventSub Subscription](https://dev.twitch.tv/docs/api/reference/#create-eventsub-subscription)
     *
     * @param subscription the type-safe subscription definition containing type, version, and condition fields.
     * @param sessionId the WebSocket session ID from the welcome message.
     * @return the created EventSub subscription.
     */
    suspend fun createEventSub(
        subscription: EventSubSubscriptionType,
        sessionId: String,
    ): EventSubSubscription {
        val request =
            CreateEventSubRequest(
                type = subscription.type,
                version = subscription.version,
                condition = subscription.toCondition(),
                transport = EventSubTransportRequest(sessionId = sessionId),
            )
        return http.post<EventSubSubscription>("eventsub/subscriptions", body = http.encodeBody(request)).requireFirst("eventsub/subscriptions")
    }
}

@Serializable
internal data class CreateEventSubRequest(
    val type: String,
    val version: String,
    val condition: Map<String, String>,
    val transport: EventSubTransportRequest,
)

@Serializable
internal data class EventSubTransportRequest(
    val method: String = "websocket",
    @SerialName("session_id") val sessionId: String,
)
