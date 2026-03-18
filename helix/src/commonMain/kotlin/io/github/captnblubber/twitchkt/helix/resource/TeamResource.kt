package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.ChannelTeam
import io.github.captnblubber.twitchkt.helix.model.Team

/**
 * Twitch Helix Teams API resource.
 *
 * Provides methods for retrieving team information.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-teams">Twitch API Reference - Teams</a>
 */
class TeamResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Channel Teams](https://dev.twitch.tv/docs/api/reference/#get-channel-teams)
     *
     * Gets the list of Twitch teams that the broadcaster is a member of.
     *
     * @param broadcasterId the ID of the broadcaster whose teams you want to get.
     * @return the list of teams that the broadcaster is a member of. Returns an empty list if
     * the broadcaster is not a member of a team.
     */
    suspend fun getChannelTeams(broadcasterId: String): List<ChannelTeam> =
        http
            .get<ChannelTeam>(
                "teams/channel",
                listOf("broadcaster_id" to broadcasterId),
            ).data

    /**
     * [Twitch API: Get Teams](https://dev.twitch.tv/docs/api/reference/#get-teams)
     *
     * Gets information about the specified Twitch team.
     *
     * The [name] and [id] parameters are mutually exclusive; you must specify the team's name
     * or ID but not both.
     *
     * @param name the name of the team to get.
     * @param id the ID of the team to get.
     * @return the team that you requested, or `null` if not found.
     */
    suspend fun getTeam(
        name: String? = null,
        id: String? = null,
    ): Team? =
        http
            .get<Team>(
                "teams",
                buildList {
                    name?.let { add("name" to it) }
                    id?.let { add("id" to it) }
                },
            ).data
            .firstOrNull()
}
