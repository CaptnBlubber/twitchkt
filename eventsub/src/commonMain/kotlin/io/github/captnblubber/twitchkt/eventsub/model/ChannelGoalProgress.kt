package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.goal.progress](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelgoalprogress)
 *
 * @property id the goal ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property type the goal type.
 * @property description the goal description.
 * @property currentAmount the current progress toward the goal.
 * @property targetAmount the target amount for the goal.
 * @property startedAt RFC3339 timestamp of when the goal started.
 */
data class ChannelGoalProgress(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val type: String,
    val description: String,
    val currentAmount: Int,
    val targetAmount: Int,
    val startedAt: Instant,
) : TwitchEvent
