package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a charity campaign that a broadcaster is running.
 *
 * @property id an ID that identifies the charity campaign.
 * @property broadcasterId an ID that identifies the broadcaster that's running the campaign.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property charityName the charity's name.
 * @property charityDescription a description of the charity.
 * @property charityLogo a URL to an image of the charity's logo. The image's type is PNG and its size is 100px X 100px.
 * @property charityWebsite a URL to the charity's website.
 * @property currentAmount the current amount of donations that the campaign has received.
 * @property targetAmount the campaign's fundraising goal. This field is `null` if the broadcaster
 * has not defined a fundraising goal.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-charity-campaign">Twitch API Reference - Get Charity Campaign</a>
 */
@Serializable
data class CharityCampaign(
    val id: String,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("charity_name") val charityName: String,
    @SerialName("charity_description") val charityDescription: String,
    @SerialName("charity_logo") val charityLogo: String,
    @SerialName("charity_website") val charityWebsite: String,
    @SerialName("current_amount") val currentAmount: CharityAmount,
    @SerialName("target_amount") val targetAmount: CharityAmount? = null,
)

/**
 * Represents a donation to a charity campaign.
 *
 * @property id an ID that identifies the donation. The ID is unique across campaigns.
 * @property campaignId an ID that identifies the charity campaign that the donation applies to.
 * @property userId an ID that identifies a user that donated money to the campaign.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property amount an object that contains the amount of money that the user donated.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-charity-campaign-donations">Twitch API Reference - Get Charity Campaign Donations</a>
 */
@Serializable
data class CharityDonation(
    val id: String,
    @SerialName("campaign_id") val campaignId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val amount: CharityAmount,
)

/**
 * Represents a monetary amount in a charity context.
 *
 * @property value the monetary amount. The amount is specified in the currency's minor unit.
 * For example, the minor units for USD is cents, so if the amount is $5.50 USD, value is set to 550.
 * @property decimalPlaces the number of decimal places used by the currency. For example, USD uses
 * two decimal places. Use this number to translate value from minor units to major units by using the
 * formula: `value / 10^decimalPlaces`.
 * @property currency the ISO-4217 three-letter currency code that identifies the type of currency in value.
 */
@Serializable
data class CharityAmount(
    val value: Int,
    @SerialName("decimal_places") val decimalPlaces: Int,
    val currency: String,
)
