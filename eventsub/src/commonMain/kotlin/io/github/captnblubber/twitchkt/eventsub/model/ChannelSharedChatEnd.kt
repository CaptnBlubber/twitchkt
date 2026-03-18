package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.shared_chat.end](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshared_chatend)
 *
 * @property sessionId the unique ID of the shared chat session.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property hostBroadcasterUserId the user ID of the host broadcaster.
 * @property hostBroadcasterUserLogin the login of the host broadcaster.
 * @property hostBroadcasterUserName the display name of the host broadcaster.
 */
data class ChannelSharedChatEnd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val sessionId: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val hostBroadcasterUserId: String,
    val hostBroadcasterUserLogin: String,
    val hostBroadcasterUserName: String,
) : TwitchEvent
