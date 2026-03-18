package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.CharityCampaign
import io.github.captnblubber.twitchkt.helix.model.CharityDonation

/**
 * Twitch Helix Charity API resource.
 *
 * Provides methods for retrieving charity campaign information and donations.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-charity-campaign">Twitch API Reference - Charity</a>
 */
class CharityResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Charity Campaign](https://dev.twitch.tv/docs/api/reference/#get-charity-campaign)
     *
     * Gets information about the charity campaign that a broadcaster is running. For example,
     * the campaign's fundraising goal and the current amount of donations.
     *
     * @param broadcasterId the ID of the broadcaster that's currently running a charity campaign.
     * This ID must match the user ID in the access token.
     * @return the charity campaign that the broadcaster is currently running, or `null` if the
     * broadcaster is not running a charity campaign.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_CHARITY)
    suspend fun getCampaign(broadcasterId: String): CharityCampaign? {
        http.validateScopes(TwitchScope.CHANNEL_READ_CHARITY)
        val params = listOf("broadcaster_id" to broadcasterId)
        return http.get<CharityCampaign>("charity/campaigns", params).data.firstOrNull()
    }

    /**
     * [Twitch API: Get Charity Campaign Donations](https://dev.twitch.tv/docs/api/reference/#get-charity-campaign-donations)
     *
     * Gets the list of donations that users have made to the broadcaster's active charity campaign.
     *
     * @param broadcasterId the ID of the broadcaster that's currently running a charity campaign.
     * This ID must match the user ID in the access token.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the list of donations that users have made to the broadcaster's charity campaign.
     * The list is empty if the broadcaster is not currently running a charity campaign.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_CHARITY)
    suspend fun getDonations(
        broadcasterId: String,
        first: Int = 20,
        after: String? = null,
    ): List<CharityDonation> {
        http.validateScopes(TwitchScope.CHANNEL_READ_CHARITY)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                add("first" to first.toString())
                after?.let { add("after" to it) }
            }
        return http.get<CharityDonation>("charity/donations", params).data
    }
}
