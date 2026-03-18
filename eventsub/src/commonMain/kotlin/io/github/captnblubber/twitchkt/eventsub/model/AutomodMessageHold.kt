package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: automod.message.hold](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodmessagehold)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the message sender.
 * @property userLogin the login of the message sender.
 * @property userName the display name of the message sender.
 * @property chatMessageId the ID of the held message.
 * @property message the structured message content.
 * @property category the automod category that flagged the message.
 * @property level the automod level that triggered the hold.
 * @property heldAt RFC3339 timestamp of when the message was held.
 * @property fragments the message fragments extracted from the message.
 */
data class AutomodMessageHold(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val chatMessageId: String,
    val message: ChatMessage,
    val category: String,
    val level: Int,
    val heldAt: Instant,
    val fragments: List<MessageFragment>,
) : TwitchEvent
