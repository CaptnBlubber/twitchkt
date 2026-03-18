package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * [Twitch API: Prediction](https://dev.twitch.tv/docs/api/reference/#get-predictions)
 *
 * @property id the prediction ID.
 * @property broadcasterId the broadcaster's user ID.
 * @property broadcasterName the broadcaster's display name.
 * @property broadcasterLogin the broadcaster's login.
 * @property title the prediction title.
 * @property winningOutcomeId the ID of the winning outcome; `null` if not resolved.
 * @property outcomes the list of prediction outcomes.
 * @property predictionWindow the prediction window in seconds.
 * @property status the prediction status.
 * @property createdAt RFC3339 timestamp of when the prediction was created.
 * @property endedAt RFC3339 timestamp of when the prediction ended; `null` if ongoing.
 * @property lockedAt RFC3339 timestamp of when the prediction was locked; `null` if not locked.
 */
@Serializable
data class Prediction(
    val id: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    val title: String,
    @SerialName("winning_outcome_id") val winningOutcomeId: String? = null,
    val outcomes: List<PredictionOutcome>,
    @SerialName("prediction_window") val predictionWindow: Int,
    val status: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("ended_at") val endedAt: Instant? = null,
    @SerialName("locked_at") val lockedAt: Instant? = null,
)

/**
 * An outcome within a prediction.
 *
 * @property id the outcome ID.
 * @property title the outcome title.
 * @property users the number of unique users who predicted this outcome.
 * @property channelPoints the total channel points used for this outcome.
 * @property topPredictors the users who predicted the most channel points for this outcome.
 * @property color the outcome color (e.g. `BLUE`, `PINK`).
 */
@Serializable
data class PredictionOutcome(
    val id: String,
    val title: String,
    val users: Int = 0,
    @SerialName("channel_points") val channelPoints: Int = 0,
    @SerialName("top_predictors") val topPredictors: List<TopPredictor>? = null,
    val color: String,
)

/**
 * A top predictor within a [PredictionOutcome].
 *
 * @property userId the user's ID.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property channelPointsUsed the number of channel points the user predicted.
 * @property channelPointsWon the number of channel points the user won (0 if prediction lost or unresolved).
 */
@Serializable
data class TopPredictor(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("channel_points_used") val channelPointsUsed: Int,
    @SerialName("channel_points_won") val channelPointsWon: Int = 0,
)
