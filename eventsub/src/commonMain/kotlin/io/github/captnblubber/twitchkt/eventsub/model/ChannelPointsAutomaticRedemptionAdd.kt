package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.channel_points_automatic_reward_redemption.add](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_automatic_reward_redemptionadd)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the user who redeemed the automatic reward.
 * @property userLogin the login of the user who redeemed the automatic reward.
 * @property userName the display name of the user who redeemed the automatic reward.
 * @property id the redemption ID.
 * @property reward the automatic reward details.
 * @property message the user message if applicable.
 * @property userInput the user input text if applicable.
 * @property redeemedAt RFC3339 timestamp of when the reward was redeemed.
 */
data class ChannelPointsAutomaticRedemptionAdd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val id: String,
    val reward: ChannelPointsReward,
    val message: ChatMessage?,
    val userInput: String?,
    val redeemedAt: Instant,
) : TwitchEvent
