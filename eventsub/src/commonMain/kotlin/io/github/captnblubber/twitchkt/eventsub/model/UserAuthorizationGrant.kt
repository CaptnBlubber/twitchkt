package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: user.authorization.grant](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#userauthorizationgrant)
 *
 * @property clientId the client ID of the application that was granted authorization.
 * @property userId the user ID of the user who granted authorization.
 * @property userLogin the login of the user who granted authorization.
 * @property userName the display name of the user who granted authorization.
 */
data class UserAuthorizationGrant(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val clientId: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
) : TwitchEvent
