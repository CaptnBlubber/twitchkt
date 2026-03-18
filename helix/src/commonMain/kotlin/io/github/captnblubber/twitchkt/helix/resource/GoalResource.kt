package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.CreatorGoal

/**
 * Twitch Helix Goals API resource.
 *
 * Provides methods for retrieving creator goals.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-creator-goals">Twitch API Reference - Goals</a>
 */
class GoalResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Creator Goals](https://dev.twitch.tv/docs/api/reference/#get-creator-goals)
     *
     * Gets the broadcaster's list of active goals. Use this endpoint to get the current progress
     * of each goal.
     *
     * Instead of polling for the progress of a goal, consider subscribing to receive notifications
     * when a goal makes progress using the `channel.goal.progress` subscription type.
     *
     * @param broadcasterId the ID of the broadcaster that created the goals. This ID must match
     * the user ID in the user access token.
     * @return the list of goals. The list is empty if the broadcaster hasn't created goals.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_GOALS)
    suspend fun getGoals(broadcasterId: String): List<CreatorGoal> {
        http.validateScopes(TwitchScope.CHANNEL_READ_GOALS)
        return http
            .get<CreatorGoal>(
                "goals",
                listOf("broadcaster_id" to broadcasterId),
            ).data
    }
}
