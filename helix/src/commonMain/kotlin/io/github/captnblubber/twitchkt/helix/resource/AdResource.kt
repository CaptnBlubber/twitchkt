package io.github.captnblubber.twitchkt.helix.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.AdSchedule
import io.github.captnblubber.twitchkt.helix.model.Commercial
import io.github.captnblubber.twitchkt.helix.model.RawAdSchedule

class AdResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Ad Schedule](https://dev.twitch.tv/docs/api/reference/#get-ad-schedule)
     *
     * Returns ad schedule related information, including snooze, when the last ad was run,
     * when the next ad is scheduled, and if the channel is currently in pre-roll free time.
     *
     * @param broadcasterId the ID of the broadcaster. Must match the user ID in the auth token.
     * @return the ad schedule for the specified broadcaster.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_ADS)
    suspend fun getSchedule(broadcasterId: String): AdSchedule {
        http.validateScopes(TwitchScope.CHANNEL_READ_ADS)
        val params = listOf("broadcaster_id" to broadcasterId)
        return http
            .get<RawAdSchedule>("channels/ads", params)
            .requireFirst("channels/ads")
            .toAdSchedule()
    }

    /**
     * [Twitch API: Start Commercial](https://dev.twitch.tv/docs/api/reference/#start-commercial)
     *
     * Starts a commercial on the specified channel. Only partners and affiliates may run
     * commercials and they must be streaming live at the time.
     *
     * @param broadcasterId the ID of the partner or affiliate broadcaster that wants to run the commercial. This ID must match the user ID found in the OAuth token.
     * @param duration the length of the commercial to run, in seconds. Twitch tries to serve a commercial that's the requested length, but it may be shorter or longer. The maximum length you should request is 180 seconds.
     * @return the commercial status including the actual length served, a message, and the retry cooldown.
     */
    @RequiresScope(TwitchScope.CHANNEL_EDIT_COMMERCIAL)
    suspend fun startCommercial(
        broadcasterId: String,
        duration: Int,
    ): Commercial {
        http.validateScopes(TwitchScope.CHANNEL_EDIT_COMMERCIAL)
        val request =
            StartCommercialRequest(
                broadcasterId = broadcasterId,
                length = duration,
            )
        return http.post<Commercial>("channels/commercial", body = http.encodeBody(request)).requireFirst("channels/commercial")
    }

    /**
     * [Twitch API: Snooze Next Ad](https://dev.twitch.tv/docs/api/reference/#snooze-next-ad)
     *
     * If available, pushes back the timestamp of the upcoming automatic mid-roll ad by 5 minutes.
     * This endpoint duplicates the snooze functionality in the creator dashboard's Ads Manager.
     *
     * @param broadcasterId the ID of the broadcaster. Must match the user ID in the auth token.
     * @return the updated snooze count, snooze refresh time, and next ad time.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_ADS)
    suspend fun snoozeNextAd(broadcasterId: String): AdSchedule {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_ADS)
        val params = listOf("broadcaster_id" to broadcasterId)
        return http
            .post<RawAdSchedule>("channels/ads/schedule/snooze", params = params)
            .requireFirst("channels/ads/schedule/snooze")
            .toAdSchedule()
    }
}

@Serializable
internal data class StartCommercialRequest(
    @SerialName("broadcaster_id") val broadcasterId: String,
    val length: Int,
)
