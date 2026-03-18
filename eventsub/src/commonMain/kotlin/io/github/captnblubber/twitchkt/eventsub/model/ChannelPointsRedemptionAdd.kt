package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.channel_points_custom_reward_redemption.add](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_reward_redemptionadd)
 *
 * @property id the redemption identifier.
 * @property broadcasterUserId the ID of the channel where the reward was redeemed.
 * @property broadcasterUserLogin the login of the channel where the reward was redeemed.
 * @property broadcasterUserName the display name of the channel where the reward was redeemed.
 * @property userId the ID of the user who redeemed the reward.
 * @property userLogin the login of the user who redeemed the reward.
 * @property userName the display name of the user who redeemed the reward.
 * @property userInput the user input provided when redeeming the reward. Empty string if not required.
 * @property status the redemption status. For `add` events, this is always `unfulfilled`.
 * @property reward the reward details including `id`, `title`, `cost`, and `prompt`.
 * @property redeemedAt RFC3339 timestamp of when the reward was redeemed.
 */
data class ChannelPointsRedemptionAdd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val userInput: String,
    val status: String,
    val reward: ChannelPointsReward,
    val redeemedAt: Instant,
) : TwitchEvent
