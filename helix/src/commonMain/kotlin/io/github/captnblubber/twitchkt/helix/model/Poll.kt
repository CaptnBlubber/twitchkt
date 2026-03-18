package io.github.captnblubber.twitchkt.helix.model

import io.github.captnblubber.twitchkt.model.common.PollStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Represents a poll from the Twitch Helix API.
 *
 * @property id an ID that identifies the poll.
 * @property broadcasterId an ID that identifies the broadcaster that created the poll.
 * @property broadcasterName the broadcaster's display name.
 * @property broadcasterLogin the broadcaster's login name.
 * @property title the question that viewers are voting on. For example, What game should I play next? The title may contain a maximum of 60 characters.
 * @property choices a list of choices that viewers can choose from. The list will contain a minimum of two choices and up to a maximum of five choices.
 * @property bitsVotingEnabled not used; will be set to false.
 * @property bitsPerVote not used; will be set to 0.
 * @property channelPointsVotingEnabled a Boolean value that indicates whether viewers may cast additional votes using Channel Points.
 * @property channelPointsPerVote the number of points the viewer must spend to cast one additional vote.
 * @property status the poll's status: `ACTIVE`, `COMPLETED`, `TERMINATED`, `ARCHIVED`, `MODERATED`, or `INVALID`.
 * @property duration the length of time (in seconds) that the poll will run for.
 * @property startedAt the UTC date and time (in RFC3339 format) of when the poll began.
 * @property endedAt the UTC date and time (in RFC3339 format) of when the poll ended. If status is ACTIVE, this field is set to null.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-polls">Twitch API Reference - Get Polls</a>
 */
@Serializable
data class Poll(
    val id: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    val title: String,
    val choices: List<PollChoice>,
    @SerialName("bits_voting_enabled") val bitsVotingEnabled: Boolean = false,
    @SerialName("bits_per_vote") val bitsPerVote: Int = 0,
    @SerialName("channel_points_voting_enabled") val channelPointsVotingEnabled: Boolean = false,
    @SerialName("channel_points_per_vote") val channelPointsPerVote: Int = 0,
    val status: PollStatus,
    val duration: Int,
    @SerialName("started_at") val startedAt: Instant? = null,
    @SerialName("ended_at") val endedAt: Instant? = null,
)

/**
 * A single choice in a [Poll].
 *
 * @property id an ID that identifies this choice.
 * @property title the choice's title. The title may contain a maximum of 25 characters.
 * @property votes the total number of votes cast for this choice.
 * @property channelPointsVotes the number of votes cast using Channel Points.
 * @property bitsVotes not used; will be set to 0.
 */
@Serializable
data class PollChoice(
    val id: String,
    val title: String,
    val votes: Int = 0,
    @SerialName("channel_points_votes") val channelPointsVotes: Int = 0,
    @SerialName("bits_votes") val bitsVotes: Int = 0,
)
