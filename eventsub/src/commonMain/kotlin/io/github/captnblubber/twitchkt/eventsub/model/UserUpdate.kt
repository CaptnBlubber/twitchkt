package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: user.update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#userupdate)
 *
 * @property userId the user ID of the updated user.
 * @property userLogin the login of the updated user.
 * @property userName the display name of the updated user.
 * @property email the user's email address; `null` if not authorized to view.
 * @property emailVerified whether the user's email has been verified.
 * @property description the user's profile description.
 */
data class UserUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val email: String?,
    val emailVerified: Boolean,
    val description: String,
) : TwitchEvent
