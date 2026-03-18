package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.hype_train.end v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelhype_trainend)
 *
 * See [HypeTrainBegin] for how points are calculated from bits and subscriptions.
 *
 * @property id the Hype Train ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property level the final level of the Hype Train.
 * @property total total points contributed to the Hype Train.
 * @property topContributions the contributors with the most points, each with `user_id`, `type`, and `total`.
 * @property startedAt the time when the Hype Train started.
 * @property endedAt the time when the Hype Train ended.
 * @property cooldownEndsAt the time when the Hype Train cooldown ends so the next one can start.
 * @property isGoldenKappaTrain whether the Hype Train was a Golden Kappa Train (v2).
 */
data class HypeTrainEnd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val level: Int,
    val total: Int,
    val topContributions: List<HypeTrainContribution>,
    val startedAt: Instant,
    val endedAt: Instant,
    val cooldownEndsAt: Instant,
    val isGoldenKappaTrain: Boolean = false,
) : TwitchEvent
