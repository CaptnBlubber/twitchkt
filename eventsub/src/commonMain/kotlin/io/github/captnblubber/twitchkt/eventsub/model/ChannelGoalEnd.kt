package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.goal.end](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelgoalend)
 *
 * @property id the goal ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property type the goal type.
 * @property description the goal description.
 * @property isAchieved whether the goal was achieved.
 * @property currentAmount the final progress amount.
 * @property targetAmount the target amount for the goal.
 * @property startedAt RFC3339 timestamp of when the goal started.
 * @property endedAt RFC3339 timestamp of when the goal ended.
 */
data class ChannelGoalEnd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val type: String,
    val description: String,
    val isAchieved: Boolean,
    val currentAmount: Int,
    val targetAmount: Int,
    val startedAt: Instant,
    val endedAt: Instant,
) : TwitchEvent
