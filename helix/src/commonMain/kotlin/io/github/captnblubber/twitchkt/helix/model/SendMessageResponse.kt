package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * [Twitch API: Send Chat Message](https://dev.twitch.tv/docs/api/reference/#send-chat-message)
 *
 * @property messageId an ID that uniquely identifies the message that was sent.
 * @property isSent whether the message was sent successfully.
 * @property dropReason the reason the message was dropped; `null` if the message was sent.
 */
@Serializable
data class SendMessageResponse(
    @SerialName("message_id") val messageId: String,
    @SerialName("is_sent") val isSent: Boolean,
    @SerialName("drop_reason") val dropReason: DropReason? = null,
)

/**
 * Reason a chat message was dropped.
 *
 * @property code a machine-readable code for the drop reason (e.g. `automod_held`).
 * @property message a human-readable description of why the message was dropped.
 */
@Serializable
data class DropReason(
    val code: String,
    val message: String,
)
