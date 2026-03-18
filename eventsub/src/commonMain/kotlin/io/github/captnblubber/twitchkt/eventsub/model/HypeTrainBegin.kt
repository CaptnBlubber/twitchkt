package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.hype_train.begin v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelhype_trainbegin)
 *
 * Points are earned from viewer contributions:
 * - **Bits**: 1 bit = 1 point
 * - **Tier 1 sub**: 500 points
 * - **Tier 2 sub**: 1,000 points
 * - **Tier 3 sub**: 2,500 points
 *
 * Gift subs contribute at their respective tier's point value per recipient.
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
 * @property level the starting level of the Hype Train.
 * @property startedAt the time when the Hype Train started.
 * @property expiresAt the time when the Hype Train expires; extended when it reaches a new level.
 * @property isGoldenKappaTrain whether the Hype Train is a Golden Kappa Train (v2).
 * @property allTimeHighLevel the all-time highest level reached on this channel (v2).
 * @property allTimeHighTotal the all-time highest total points on this channel (v2).
 */
data class HypeTrainBegin(
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
    val allTimeHighLevel: Int = 0,
    val allTimeHighTotal: Int = 0,
) : TwitchEvent
