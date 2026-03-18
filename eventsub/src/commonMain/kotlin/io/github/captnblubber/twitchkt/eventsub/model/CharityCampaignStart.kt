package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.charity_campaign.start](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcharity_campaignstart)
 *
 * @property id the charity campaign ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property charityName the name of the charity.
 * @property charityDescription the description of the charity.
 * @property charityLogo the URL of the charity logo.
 * @property charityWebsite the website URL of the charity.
 * @property currentAmount the current raised amount (value, decimal_places, currency).
 * @property targetAmount the target amount (value, decimal_places, currency).
 * @property startedAt RFC3339 timestamp of when the campaign started.
 */
data class CharityCampaignStart(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val charityName: String,
    val charityDescription: String,
    val charityLogo: String,
    val charityWebsite: String,
    val currentAmount: CurrencyAmount,
    val targetAmount: CurrencyAmount,
    val startedAt: Instant,
) : TwitchEvent
