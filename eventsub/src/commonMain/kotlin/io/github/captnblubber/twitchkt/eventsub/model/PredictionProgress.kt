package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.prediction.progress](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpredictionprogress)
 *
 * @property id the prediction ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property title the prediction title.
 * @property outcomes the list of prediction outcomes with current point totals.
 * @property startedAt RFC3339 timestamp of when the prediction started.
 * @property locksAt RFC3339 timestamp of when the prediction locks.
 */
data class PredictionProgress(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val title: String,
    val outcomes: List<PredictionOutcome>,
    val startedAt: Instant,
    val locksAt: Instant,
) : TwitchEvent
