package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: stream.offline](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#streamoffline)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's user login.
 * @property broadcasterUserName the broadcaster's user display name.
 */
data class StreamOffline(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
) : TwitchEvent
