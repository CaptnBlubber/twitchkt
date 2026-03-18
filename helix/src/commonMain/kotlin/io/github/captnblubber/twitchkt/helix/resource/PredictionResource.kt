package io.github.captnblubber.twitchkt.helix.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.Prediction
import io.github.captnblubber.twitchkt.helix.model.PredictionEndStatus

class PredictionResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Predictions](https://dev.twitch.tv/docs/api/reference/#get-predictions)
     *
     * Gets a list of Channel Points Predictions that the broadcaster created.
     *
     * @param broadcasterId the ID of the broadcaster whose predictions you want to get. This ID must match the user ID in the user access token.
     * @param ids the ID of the prediction to get. You may specify a maximum of 25 IDs. The endpoint ignores duplicate IDs and those not owned by the broadcaster.
     * @param first the maximum number of items to return per page in the response. The minimum page size is 1 item per page and the maximum is 25 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the list of predictions.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_PREDICTIONS)
    suspend fun list(
        broadcasterId: String,
        ids: List<String> = emptyList(),
        first: Int = 20,
        after: String? = null,
    ): List<Prediction> {
        http.validateScopes(TwitchScope.CHANNEL_READ_PREDICTIONS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                ids.forEach { add("id" to it) }
                add("first" to first.toString())
                after?.let { add("after" to it) }
            }
        return http.get<Prediction>("predictions", params).data
    }

    /**
     * [Twitch API: Create Prediction](https://dev.twitch.tv/docs/api/reference/#create-prediction)
     *
     * Creates a Channel Points Prediction. With a Channel Points Prediction, the broadcaster
     * poses a question and viewers try to predict the outcome. The prediction runs as soon as
     * it's created. The broadcaster may run only one prediction at a time.
     *
     * @param broadcasterId the ID of the broadcaster that's running the prediction. This ID must match the user ID in the user access token.
     * @param title the question that the broadcaster is asking. For example, Will I finish this entire pizza? The title is limited to a maximum of 45 characters.
     * @param outcomes the list of possible outcomes that the viewers may choose from. The list must contain a minimum of 2 choices and up to a maximum of 10 choices.
     * @param predictionWindow the length of time (in seconds) that the prediction will run for. The minimum is 30 seconds and the maximum is 1800 seconds (30 minutes).
     * @return the created prediction.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_PREDICTIONS)
    suspend fun create(
        broadcasterId: String,
        title: String,
        outcomes: List<String>,
        predictionWindow: Int,
    ): Prediction {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_PREDICTIONS)
        val request =
            CreatePredictionRequest(
                broadcasterId = broadcasterId,
                title = title,
                outcomes = outcomes.map { CreatePredictionOutcome(it) },
                predictionWindow = predictionWindow,
            )
        return http.post<Prediction>("predictions", body = http.encodeBody(request)).requireFirst("predictions")
    }

    /**
     * [Twitch API: End Prediction](https://dev.twitch.tv/docs/api/reference/#end-prediction)
     *
     * Locks, resolves, or cancels a Channel Points Prediction.
     *
     * @param broadcasterId the ID of the broadcaster that's running the prediction. This ID must match the user ID in the user access token.
     * @param predictionId the ID of the prediction to update.
     * @param status the status to set the prediction to. Possible case-sensitive values are: `RESOLVED`, `CANCELED`, `LOCKED`. The broadcaster can update an active prediction to LOCKED, RESOLVED, or CANCELED; and update a locked prediction to RESOLVED or CANCELED.
     * @param winningOutcomeId the ID of the winning outcome. You must set this parameter if you set status to RESOLVED.
     * @return the ended prediction.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_PREDICTIONS)
    suspend fun end(
        broadcasterId: String,
        predictionId: String,
        status: PredictionEndStatus,
        winningOutcomeId: String? = null,
    ): Prediction {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_PREDICTIONS)
        val request =
            EndPredictionRequest(
                broadcasterId = broadcasterId,
                id = predictionId,
                status = status,
                winningOutcomeId = winningOutcomeId,
            )
        return http.patch<Prediction>("predictions", body = http.encodeBody(request)).requireFirst("predictions")
    }
}

@Serializable
internal data class CreatePredictionRequest(
    @SerialName("broadcaster_id") val broadcasterId: String,
    val title: String,
    val outcomes: List<CreatePredictionOutcome>,
    @SerialName("prediction_window") val predictionWindow: Int,
)

@Serializable
internal data class CreatePredictionOutcome(
    val title: String,
)

@Serializable
internal data class EndPredictionRequest(
    @SerialName("broadcaster_id") val broadcasterId: String,
    val id: String,
    val status: PredictionEndStatus,
    @SerialName("winning_outcome_id") val winningOutcomeId: String? = null,
)
