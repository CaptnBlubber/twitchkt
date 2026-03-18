package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.hype_train.progress v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelhype_trainprogress)
 *
 * See [HypeTrainBegin] for how points are calculated from bits and subscriptions.
 *
 * @property id the Hype Train ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property total total points contributed to the Hype Train.
 * @property progress the number of points contributed at the current level.
 * @property goal the number of points required to reach the next level.
 * @property topContributions the contributors with the most points, each with `user_id`, `type`, and `total`.
 * @property lastContribution the most recent contribution with `user_id`, `type`, and `total`.
 * @property level the current level of the Hype Train.
 * @property startedAt the time when the Hype Train started.
 * @property expiresAt the time when the Hype Train expires; extended when it reaches a new level.
 * @property isGoldenKappaTrain whether the Hype Train is a Golden Kappa Train (v2).
 */
data class HypeTrainProgress(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val total: Int,
    val progress: Int,
    val goal: Int,
    val topContributions: List<HypeTrainContribution>,
    val lastContribution: HypeTrainContribution?,
    val level: Int,
    val startedAt: Instant,
    val expiresAt: Instant,
    val isGoldenKappaTrain: Boolean = false,
) : TwitchEvent
