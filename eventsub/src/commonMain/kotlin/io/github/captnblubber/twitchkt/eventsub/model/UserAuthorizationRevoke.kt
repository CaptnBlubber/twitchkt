package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: user.authorization.revoke](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#userauthorizationrevoke)
 *
 * @property clientId the client ID of the application that had authorization revoked.
 * @property userId the user ID of the user who revoked authorization; `null` if unavailable.
 * @property userLogin the login of the user who revoked authorization; `null` if unavailable.
 * @property userName the display name of the user who revoked authorization; `null` if unavailable.
 */
data class UserAuthorizationRevoke(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val clientId: String,
    val userId: String?,
    val userLogin: String?,
    val userName: String?,
) : TwitchEvent
