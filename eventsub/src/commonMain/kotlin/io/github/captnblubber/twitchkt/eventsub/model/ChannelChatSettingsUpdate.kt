package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat_settings.update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchat_settingsupdate)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property emoteMode whether emote-only mode is enabled.
 * @property followerMode whether follower-only mode is enabled.
 * @property followerModeDurationMinutes the follower mode duration in minutes; `null` if disabled.
 * @property slowMode whether slow mode is enabled.
 * @property slowModeWaitTimeSeconds the slow mode wait time in seconds; `null` if disabled.
 * @property subscriberMode whether subscriber-only mode is enabled.
 * @property uniqueChatMode whether unique chat mode is enabled.
 */
data class ChannelChatSettingsUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val emoteMode: Boolean,
    val followerMode: Boolean,
    val followerModeDurationMinutes: Int?,
    val slowMode: Boolean,
    val slowModeWaitTimeSeconds: Int?,
    val subscriberMode: Boolean,
    val uniqueChatMode: Boolean,
) : TwitchEvent
