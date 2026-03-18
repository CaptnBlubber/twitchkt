package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.prediction.end](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpredictionend)
 *
 * @property id the prediction ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property title the prediction title.
 * @property winningOutcomeId the ID of the winning outcome; `null` if canceled.
 * @property outcomes the list of prediction outcomes with final totals.
 * @property status the end status: `resolved` or `canceled`.
 * @property startedAt RFC3339 timestamp of when the prediction started.
 * @property endedAt RFC3339 timestamp of when the prediction ended.
 */
data class PredictionEnd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val title: String,
    val winningOutcomeId: String?,
    val outcomes: List<PredictionOutcome>,
    val status: String,
    val startedAt: Instant,
    val endedAt: Instant,
) : TwitchEvent
