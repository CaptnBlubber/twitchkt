package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Send Chat Message](https://dev.twitch.tv/docs/api/reference/#send-chat-message)
 *
 * @property broadcasterId the ID of the broadcaster whose chat room the message will be sent to.
 * @property senderId the ID of the user sending the message. This ID must match the user ID in the user access token.
 * @property message the message to send. The message is limited to a maximum of 500 characters. Chat messages can also include emoticons. To include emoticons, use the name of the emote. The names are case sensitive.
 * @property replyParentMessageId the ID of the chat message being replied to.
 * @property forSourceOnly determines if the chat message is sent only to the source channel (defined by [broadcasterId]) during a shared chat session. This parameter can only be set when utilizing an App Access Token. Defaults to `false` for App Access Tokens.
 */
@Serializable
data class SendChatMessageRequest(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("sender_id") val senderId: String,
    val message: String,
    @SerialName("reply_parent_message_id") val replyParentMessageId: String? = null,
    @SerialName("for_source_only") val forSourceOnly: Boolean? = null,
)
