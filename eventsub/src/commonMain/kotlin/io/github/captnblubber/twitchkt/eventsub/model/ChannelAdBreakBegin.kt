package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.ad_break.begin](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelad_breakbegin)
 *
 * @property durationSeconds length in seconds of the mid-roll ad break requested.
 * @property startedAt the UTC timestamp (RFC3339) of when the ad break began.
 * @property isAutomatic whether the ad was automatically scheduled via Ads Manager.
 * @property broadcasterUserId the broadcaster's user ID for the channel the ad was run on.
 * @property broadcasterUserLogin the broadcaster's login for the channel the ad was run on.
 * @property broadcasterUserName the broadcaster's display name for the channel the ad was run on.
 * @property requesterUserId the ID of the user that requested the ad. For automatic ads, this is the broadcaster's ID.
 * @property requesterUserLogin the login of the user that requested the ad.
 * @property requesterUserName the display name of the user that requested the ad.
 */
data class ChannelAdBreakBegin(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val durationSeconds: Int,
    val startedAt: Instant,
    val isAutomatic: Boolean,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val requesterUserId: String,
    val requesterUserLogin: String,
    val requesterUserName: String,
) : TwitchEvent
