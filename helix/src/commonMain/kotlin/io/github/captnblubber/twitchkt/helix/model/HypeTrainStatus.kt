package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents the Hype Train status for a channel.
 *
 * @property current an object describing the current Hype Train. `null` if a Hype Train is not active.
 * @property allTimeHigh an object with information about the channel's Hype Train records.
 * `null` if a Hype Train has not occurred.
 * @property sharedAllTimeHigh an object with information about the channel's shared Hype Train records.
 * `null` if a shared Hype Train has not occurred.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-hype-train-status">Twitch API Reference - Get Hype Train Status</a>
 */
@Serializable
data class HypeTrainStatus(
    val current: HypeTrainCurrent? = null,
    @SerialName("all_time_high") val allTimeHigh: HypeTrainRecord? = null,
    @SerialName("shared_all_time_high") val sharedAllTimeHigh: HypeTrainRecord? = null,
)

/**
 * Represents a currently active Hype Train.
 *
 * @property id the Hype Train ID.
 * @property broadcasterUserId the broadcaster ID.
 * @property broadcasterUserLogin the broadcaster login.
 * @property broadcasterUserName the broadcaster display name.
 * @property level the current level of the Hype Train.
 * @property total total points contributed to the Hype Train.
 * @property progress the number of points contributed to the Hype Train at the current level.
 * @property goal the number of points required to reach the next level.
 * @property topContributions the contributors with the most points contributed.
 * @property sharedTrainParticipants a list containing the broadcasters participating in the shared
 * Hype Train. `null` if the Hype Train is not shared.
 * @property startedAt the time when the Hype Train started.
 * @property expiresAt the time when the Hype Train expires. The expiration is extended when the
 * Hype Train reaches a new level.
 * @property type the type of the Hype Train. Possible values are: `treasure`, `golden_kappa`, `regular`.
 * @property isSharedTrain indicates if the Hype Train is shared. When `true`,
 * [sharedTrainParticipants] will contain the list of broadcasters the train is shared with.
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class HypeTrainCurrent(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val level: Int,
    val total: Int,
    val progress: Int,
    val goal: Int,
    @SerialName("top_contributions") val topContributions: List<HypeTrainContribution> = emptyList(),
    @SerialName("shared_train_participants") val sharedTrainParticipants: List<SharedTrainParticipant>? = null,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("expires_at") val expiresAt: Instant,
    val type: String,
    @SerialName("is_shared_train") val isSharedTrain: Boolean = false,
)

/**
 * Represents a contribution to a Hype Train.
 *
 * @property userId the ID of the user that made the contribution.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property type the contribution method used. Possible values are: `bits`, `subscription`, `other`.
 * @property total the total number of points contributed for the type.
 */
@Serializable
data class HypeTrainContribution(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val type: String,
    val total: Int,
)

/**
 * Represents a broadcaster participating in a shared Hype Train.
 *
 * @property broadcasterUserId the broadcaster ID.
 * @property broadcasterUserLogin the broadcaster login.
 * @property broadcasterUserName the broadcaster display name.
 */
@Serializable
data class SharedTrainParticipant(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
)

/**
 * Represents a Hype Train all-time high record.
 *
 * @property level the level of the record Hype Train.
 * @property total total points contributed to the record Hype Train.
 * @property achievedAt the time when the record was achieved.
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class HypeTrainRecord(
    val level: Int,
    val total: Int,
    @SerialName("achieved_at") val achievedAt: Instant,
)
