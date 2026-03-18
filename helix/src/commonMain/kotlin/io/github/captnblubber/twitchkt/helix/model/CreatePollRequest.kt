package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Create Poll](https://dev.twitch.tv/docs/api/reference/#create-poll)
 *
 * @property broadcasterId the ID of the broadcaster that's running the poll.
 * @property title the question that viewers are voting on; max 60 characters.
 * @property choices the choices that viewers can vote on; must contain 2 to 5 choices.
 * @property duration how long the poll runs, in seconds; min 15, max 1800.
 * @property channelPointsVotingEnabled whether to enable Channel Points voting.
 * @property channelPointsPerVote the number of Channel Points required per additional vote; min 1, max 1,000,000.
 */
@Serializable
data class CreatePollRequest(
    @SerialName("broadcaster_id") val broadcasterId: String,
    val title: String,
    val choices: List<CreatePollChoice>,
    val duration: Int,
    @SerialName("channel_points_voting_enabled") val channelPointsVotingEnabled: Boolean = false,
    @SerialName("channel_points_per_vote") val channelPointsPerVote: Int = 0,
)

/**
 * A choice for a [CreatePollRequest].
 *
 * @property title the text for the choice; max 25 characters.
 */
@Serializable
data class CreatePollChoice(
    val title: String,
)
