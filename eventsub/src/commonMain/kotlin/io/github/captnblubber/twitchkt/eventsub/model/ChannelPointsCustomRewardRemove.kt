package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.channel_points_custom_reward.remove](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_rewardremove)
 *
 * Fired when a custom channel points reward is removed. Uses the same structure as
 * [ChannelPointsCustomRewardAdd].
 */
data class ChannelPointsCustomRewardRemove(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val isEnabled: Boolean,
    val isPaused: Boolean,
    val isInStock: Boolean,
    val title: String,
    val cost: Int,
    val prompt: String,
    val isUserInputRequired: Boolean,
    val shouldRedemptionsSkipRequestQueue: Boolean,
    val maxPerStream: MaxPerStreamSetting,
    val maxPerUserPerStream: MaxPerStreamSetting,
    val backgroundColor: String,
    val image: RewardImage?,
    val defaultImage: RewardImage,
    val globalCooldown: GlobalCooldownSetting,
    val cooldownExpiresAt: Instant?,
    val redemptionsRedeemedCurrentStream: Int?,
) : TwitchEvent
