package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: stream.online](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#streamonline)
 *
 * @property id the id of the stream.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's user login.
 * @property broadcasterUserName the broadcaster's user display name.
 * @property type the stream type: `live`, `playlist`, `watch_party`, `premiere`, or `rerun`.
 * @property startedAt the timestamp (RFC3339) at which the stream went online.
 */
data class StreamOnline(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val type: String,
    val startedAt: Instant,
) : TwitchEvent
