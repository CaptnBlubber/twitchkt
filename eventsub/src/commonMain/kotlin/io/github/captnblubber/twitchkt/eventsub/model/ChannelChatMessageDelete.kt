package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat.message_delete](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatmessage_delete)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property targetUserId the user ID of the user whose message was deleted.
 * @property targetUserLogin the login of the user whose message was deleted.
 * @property targetUserName the display name of the user whose message was deleted.
 * @property targetMessageId the ID of the deleted message.
 */
data class ChannelChatMessageDelete(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val targetUserId: String,
    val targetUserLogin: String,
    val targetUserName: String,
    val targetMessageId: String,
) : TwitchEvent
