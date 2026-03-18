package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.channel_points_custom_reward.add](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_rewardadd)
 *
 * @property id the reward ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property isEnabled whether the reward is enabled.
 * @property isPaused whether the reward is paused.
 * @property isInStock whether the reward is in stock.
 * @property title the reward title.
 * @property cost the reward cost in channel points.
 * @property prompt the reward prompt.
 * @property isUserInputRequired whether user input is required.
 * @property shouldRedemptionsSkipRequestQueue whether redemptions skip the request queue.
 * @property maxPerStream max per stream settings.
 * @property maxPerUserPerStream max per user per stream settings.
 * @property backgroundColor the reward background color.
 * @property image the reward image URLs.
 * @property defaultImage the default image URLs.
 * @property globalCooldown global cooldown settings.
 * @property cooldownExpiresAt RFC3339 timestamp of when the cooldown expires; `null` if not on cooldown.
 * @property redemptionsRedeemedCurrentStream number of redemptions in the current stream; `null` if not live.
 */
data class ChannelPointsCustomRewardAdd(
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
