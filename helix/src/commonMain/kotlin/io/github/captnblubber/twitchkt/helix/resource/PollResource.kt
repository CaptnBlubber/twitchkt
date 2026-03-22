package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.CreatePollRequest
import io.github.captnblubber.twitchkt.helix.model.Poll
import io.github.captnblubber.twitchkt.helix.model.PollEndStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Polls API resource.
 *
 * Note: This resource returns lists directly rather than [Page]/[Flow] because polls are scoped
 * to a single broadcaster and optionally filtered by specific poll IDs. The result set is
 * inherently small (only one poll can be active at a time, and historical polls are typically
 * fetched by ID), making auto-pagination unnecessary.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-polls">Twitch API Reference - Polls</a>
 */
class PollResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Polls](https://dev.twitch.tv/docs/api/reference/#get-polls)
     *
     * Gets a list of polls that the broadcaster created. Polls are available for 90 days after
     * they're created.
     *
     * @param broadcasterId the ID of the broadcaster that created the polls. This ID must match the user ID in the user access token.
     * @param ids a list of IDs that identify the polls to return. You may specify a maximum of 20 IDs. The endpoint ignores duplicate IDs and those not owned by this broadcaster.
     * @param first the maximum number of items to return per page in the response. The minimum page size is 1 item per page and the maximum is 20 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the list of polls.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_POLLS)
    suspend fun getPolls(
        broadcasterId: String,
        ids: List<String> = emptyList(),
        first: Int = 20,
        after: String? = null,
    ): List<Poll> {
        http.validateScopes(TwitchScope.CHANNEL_READ_POLLS)
        return http
            .get<Poll>(
                "polls",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    ids.forEach { add("id" to it) }
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                },
            ).data
    }

    /**
     * [Twitch API: Create Poll](https://dev.twitch.tv/docs/api/reference/#create-poll)
     *
     * Creates a poll that viewers in the broadcaster's channel can vote on. The poll begins as
     * soon as it's created. You may run only one poll at a time.
     *
     * @param request the poll configuration including broadcaster ID, title, choices, and duration.
     * @return the created poll.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_POLLS)
    suspend fun create(request: CreatePollRequest): Poll {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_POLLS)
        return http.post<Poll>("polls", body = http.encodeBody(request)).requireFirst("polls")
    }

    /**
     * [Twitch API: End Poll](https://dev.twitch.tv/docs/api/reference/#end-poll)
     *
     * Ends an active poll. You have the option to end it or end it and archive it.
     *
     * @param broadcasterId the ID of the broadcaster that's running the poll. This ID must match the user ID in the user access token.
     * @param pollId the ID of the poll to update.
     * @param status the status to set the poll to. Possible case-sensitive values are: `TERMINATED` — Ends the poll before the poll is scheduled to end. The poll remains publicly visible. `ARCHIVED` — Ends the poll before the poll is scheduled to end, and then archives it so it's no longer publicly visible.
     * @return the ended poll with final results.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_POLLS)
    suspend fun end(
        broadcasterId: String,
        pollId: String,
        status: PollEndStatus,
    ): Poll {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_POLLS)
        val request =
            EndPollRequest(
                broadcasterId = broadcasterId,
                id = pollId,
                status = status.name,
            )
        return http.patch<Poll>("polls", body = http.encodeBody(request)).requireFirst("polls")
    }
}

@Serializable
internal data class EndPollRequest(
    @SerialName("broadcaster_id") val broadcasterId: String,
    val id: String,
    val status: String,
)
