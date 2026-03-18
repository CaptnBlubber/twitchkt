package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * [Twitch API: Get Custom Reward](https://dev.twitch.tv/docs/api/reference/#get-custom-reward)
 *
 * @property broadcasterId an ID that uniquely identifies the broadcaster.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property id an ID that uniquely identifies this custom reward.
 * @property title the reward's title.
 * @property prompt the prompt shown to the viewer when they redeem the reward.
 * @property cost the cost of the reward in Channel Points.
 * @property image a set of custom images for the reward; `null` if no custom images were uploaded.
 * @property defaultImage a set of default images for the reward.
 * @property backgroundColor the background color of the reward in hex format (e.g., `#FA1ED2`).
 * @property isEnabled whether the reward is enabled; viewers see only enabled rewards.
 * @property isUserInputRequired whether the user must enter information when redeeming the reward.
 * @property maxPerStreamSetting the settings for the maximum number of redemptions per live stream.
 * @property maxPerUserPerStreamSetting the settings for the maximum number of redemptions per user per stream.
 * @property globalCooldownSetting the settings for the cooldown period between redemptions.
 * @property isPaused whether the reward is currently paused.
 * @property isInStock whether the reward is currently in stock.
 * @property shouldRedemptionsSkipRequestQueue whether redemptions are set to FULFILLED automatically rather than entering the request queue.
 * @property redemptionsRedeemedCurrentStream the number of redemptions during the current live stream; `null` if not live.
 * @property cooldownExpiresAt the timestamp (RFC3339) when the cooldown expires; `null` if not on cooldown.
 */
@Serializable
data class CustomReward(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    val id: String,
    val title: String,
    val prompt: String = "",
    val cost: Int,
    val image: RewardImage? = null,
    @SerialName("default_image") val defaultImage: RewardImage? = null,
    @SerialName("background_color") val backgroundColor: String = "",
    @SerialName("is_enabled") val isEnabled: Boolean = true,
    @SerialName("is_user_input_required") val isUserInputRequired: Boolean = false,
    @SerialName("max_per_stream_setting") val maxPerStreamSetting: MaxPerStreamSetting = MaxPerStreamSetting(),
    @SerialName("max_per_user_per_stream_setting") val maxPerUserPerStreamSetting: MaxPerUserPerStreamSetting = MaxPerUserPerStreamSetting(),
    @SerialName("global_cooldown_setting") val globalCooldownSetting: GlobalCooldownSetting = GlobalCooldownSetting(),
    @SerialName("is_paused") val isPaused: Boolean = false,
    @SerialName("is_in_stock") val isInStock: Boolean = true,
    @SerialName("should_redemptions_skip_request_queue") val shouldRedemptionsSkipRequestQueue: Boolean = false,
    @SerialName("redemptions_redeemed_current_stream") val redemptionsRedeemedCurrentStream: Int? = null,
    @SerialName("cooldown_expires_at") val cooldownExpiresAt: Instant? = null,
)

/**
 * Custom or default image for a [CustomReward].
 *
 * @property url1x the URL to the small (28x28) version of the image.
 * @property url2x the URL to the medium (56x56) version of the image.
 * @property url4x the URL to the large (112x112) version of the image.
 */
@Serializable
data class RewardImage(
    @SerialName("url_1x") val url1x: String,
    @SerialName("url_2x") val url2x: String,
    @SerialName("url_4x") val url4x: String,
)

/**
 * Max-per-stream setting for a [CustomReward].
 *
 * @property isEnabled whether the stream limit is enabled.
 * @property maxPerStream the maximum number of redemptions allowed per live stream.
 */
@Serializable
data class MaxPerStreamSetting(
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    @SerialName("max_per_stream") val maxPerStream: Int = 0,
)

/**
 * Max-per-user-per-stream setting for a [CustomReward].
 *
 * @property isEnabled whether the per-user limit is enabled.
 * @property maxPerUserPerStream the maximum number of redemptions per user per stream.
 */
@Serializable
data class MaxPerUserPerStreamSetting(
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    @SerialName("max_per_user_per_stream") val maxPerUserPerStream: Int = 0,
)

/**
 * Global cooldown setting for a [CustomReward].
 *
 * @property isEnabled whether the cooldown is enabled.
 * @property globalCooldownSeconds the cooldown period in seconds.
 */
@Serializable
data class GlobalCooldownSetting(
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    @SerialName("global_cooldown_seconds") val globalCooldownSeconds: Int = 0,
)
