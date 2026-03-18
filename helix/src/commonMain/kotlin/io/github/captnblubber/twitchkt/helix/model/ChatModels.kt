package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user in a broadcaster's chat.
 *
 * @property userId the ID of a user that's connected to the broadcaster's chat room.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-chatters">Twitch API Reference - Get Chatters</a>
 */
@Serializable
data class Chatter(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
)

/**
 * Represents an emote from the Twitch Helix API.
 *
 * Used by Get Channel Emotes, Get Global Emotes, and Get Emote Sets endpoints.
 *
 * @property id an ID that uniquely identifies this emote.
 * @property name the name of the emote. This is the name that viewers type in the chat window to get the emote to appear.
 * @property images the image URLs for the emote. These always provide a static, non-animated emote image with a light background.
 * @property tier the subscriber tier at which the emote is unlocked. Empty string if [emoteType] is not `subscriptions`.
 * @property emoteType the type of emote: `bitstier`, `follower`, or `subscriptions`.
 * @property emoteSetId an ID that identifies the emote set that the emote belongs to.
 * @property ownerId the ID of the broadcaster who owns the emote. Present in Get Emote Sets responses.
 * @property format the formats that the emote is available in: `animated` and/or `static`.
 * @property scale the sizes that the emote is available in: `1.0`, `2.0`, and/or `3.0`.
 * @property themeMode the background themes that the emote is available in: `dark` and/or `light`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-channel-emotes">Twitch API Reference - Get Channel Emotes</a>
 */
@Serializable
data class ChannelEmote(
    val id: String,
    val name: String,
    val images: EmoteImages,
    val tier: String = "",
    @SerialName("emote_type") val emoteType: String = "",
    @SerialName("emote_set_id") val emoteSetId: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    val format: List<String> = emptyList(),
    val scale: List<String> = emptyList(),
    @SerialName("theme_mode") val themeMode: List<String> = emptyList(),
)

/**
 * Represents image URLs for an emote.
 */
@Serializable
data class EmoteImages(
    @SerialName("url_1x") val url1x: String,
    @SerialName("url_2x") val url2x: String,
    @SerialName("url_4x") val url4x: String,
)

/**
 * Represents chat settings for a broadcaster's channel.
 *
 * @property broadcasterId the ID of the broadcaster specified in the request.
 * @property slowMode whether the broadcaster limits how often users in the chat room are allowed to send messages.
 * @property slowModeWaitTime the amount of time, in seconds, that users must wait between sending messages. `null` if [slowMode] is `false`.
 * @property followerMode whether the broadcaster restricts the chat room to followers only.
 * @property followerModeDuration the length of time, in minutes, that users must follow the broadcaster before being able to participate in the chat room. `null` if [followerMode] is `false`.
 * @property subscriberMode whether only users that subscribe to the broadcaster's channel may talk in the chat room.
 * @property emoteMode whether chat messages must contain only emotes.
 * @property uniqueChatMode whether the broadcaster requires users to post only unique messages in the chat room.
 * @property nonModeratorChatDelay whether the broadcaster adds a short delay before chat messages appear in the chat room. Only included when the request specifies a moderator access token.
 * @property nonModeratorChatDelayDuration the amount of time, in seconds, that messages are delayed before appearing in chat. `null` if [nonModeratorChatDelay] is `false`. Only included when the request specifies a moderator access token.
 * @property moderatorId the moderator's ID. Only included when the request specifies a user access token with the `moderator:read:chat_settings` scope.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-chat-settings">Twitch API Reference - Get Chat Settings</a>
 */
@Serializable
data class ChatSettings(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("slow_mode") val slowMode: Boolean,
    @SerialName("slow_mode_wait_time") val slowModeWaitTime: Int? = null,
    @SerialName("follower_mode") val followerMode: Boolean,
    @SerialName("follower_mode_duration") val followerModeDuration: Int? = null,
    @SerialName("subscriber_mode") val subscriberMode: Boolean,
    @SerialName("emote_mode") val emoteMode: Boolean,
    @SerialName("unique_chat_mode") val uniqueChatMode: Boolean,
    @SerialName("non_moderator_chat_delay") val nonModeratorChatDelay: Boolean = false,
    @SerialName("non_moderator_chat_delay_duration") val nonModeratorChatDelayDuration: Int? = null,
    @SerialName("moderator_id") val moderatorId: String? = null,
)

/**
 * Represents a user's chat name color.
 *
 * @property userId an ID that uniquely identifies the user.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property color the Hex color code that the user uses in chat for their name. If the user
 * hasn't specified a color in their settings, the string is empty.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-user-chat-color">Twitch API Reference - Get User Chat Color</a>
 */
@Serializable
data class ChatColorEntry(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val color: String,
)
