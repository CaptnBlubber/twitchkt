package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: automod.terms.update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodtermsupdate)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who updated the terms.
 * @property moderatorUserLogin the login of the moderator who updated the terms.
 * @property moderatorUserName the display name of the moderator who updated the terms.
 * @property action the action taken ("add_permitted", "remove_permitted", "add_blocked", "remove_blocked").
 * @property fromAutomod whether the update originated from automod.
 * @property terms the list of terms that were updated.
 */
data class AutomodTermsUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val action: String,
    val fromAutomod: Boolean,
    val terms: List<String>,
) : TwitchEvent
