package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Update Custom Reward](https://dev.twitch.tv/docs/api/reference/#update-custom-reward)
 *
 * @property title the custom reward's title; max 45 characters, must be unique among the broadcaster's rewards.
 * @property cost the cost of the reward in Channel Points; minimum 1.
 * @property prompt the prompt shown to the viewer when they redeem the reward; max 200 characters.
 * @property isEnabled whether the reward is enabled; viewers see only enabled rewards.
 * @property backgroundColor the background color of the reward in hex format (e.g., `#FA1ED2`).
 * @property isUserInputRequired whether the user must enter information when redeeming the reward.
 * @property isMaxPerStreamEnabled whether to limit the maximum number of redemptions per live stream.
 * @property maxPerStream the maximum number of redemptions allowed per live stream.
 * @property isMaxPerUserPerStreamEnabled whether to limit the maximum number of redemptions per user per stream.
 * @property maxPerUserPerStream the maximum number of redemptions allowed per user per stream.
 * @property isGlobalCooldownEnabled whether to apply a cooldown period between redemptions.
 * @property globalCooldownSeconds the cooldown period in seconds.
 * @property shouldRedemptionsSkipRequestQueue whether redemptions are automatically set to FULFILLED rather than entering the request queue.
 * @property isPaused whether the reward is currently paused; viewers cannot redeem paused rewards.
 */
@Serializable
data class UpdateRewardRequest(
    val title: String? = null,
    val cost: Int? = null,
    val prompt: String? = null,
    @SerialName("is_enabled") val isEnabled: Boolean? = null,
    @SerialName("background_color") val backgroundColor: String? = null,
    @SerialName("is_user_input_required") val isUserInputRequired: Boolean? = null,
    @SerialName("is_max_per_stream_enabled") val isMaxPerStreamEnabled: Boolean? = null,
    @SerialName("max_per_stream") val maxPerStream: Int? = null,
    @SerialName("is_max_per_user_per_stream_enabled") val isMaxPerUserPerStreamEnabled: Boolean? = null,
    @SerialName("max_per_user_per_stream") val maxPerUserPerStream: Int? = null,
    @SerialName("is_global_cooldown_enabled") val isGlobalCooldownEnabled: Boolean? = null,
    @SerialName("global_cooldown_seconds") val globalCooldownSeconds: Int? = null,
    @SerialName("should_redemptions_skip_request_queue") val shouldRedemptionsSkipRequestQueue: Boolean? = null,
    @SerialName("is_paused") val isPaused: Boolean? = null,
)
