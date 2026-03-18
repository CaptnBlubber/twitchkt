package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.charity_campaign.donate](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcharity_campaigndonate)
 *
 * @property id the donation ID.
 * @property campaignId the charity campaign ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the donor.
 * @property userLogin the login of the donor.
 * @property userName the display name of the donor.
 * @property charityName the name of the charity.
 * @property charityDescription the description of the charity.
 * @property charityLogo the URL of the charity logo.
 * @property charityWebsite the website URL of the charity.
 * @property amount the donation amount (value, decimal_places, currency).
 */
data class CharityDonate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val campaignId: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val charityName: String,
    val charityDescription: String,
    val charityLogo: String,
    val charityWebsite: String,
    val amount: CurrencyAmount,
) : TwitchEvent
