package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.suspicious_user.message](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsuspicious_usermessage)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the suspicious user.
 * @property userLogin the login of the suspicious user.
 * @property userName the display name of the suspicious user.
 * @property lowTrustStatus the low trust status of the user (e.g. `active_monitoring`, `restricted`).
 * @property sharedBanChannelIds list of channel IDs where the user is banned.
 * @property types the types of suspicious activity detected.
 * @property bannedChannelCount the number of channels that have banned this user.
 * @property message the chat message sent by the suspicious user.
 */
data class ChannelSuspiciousUserMessage(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val lowTrustStatus: String,
    val sharedBanChannelIds: List<String>,
    val types: List<String>,
    val bannedChannelCount: Int,
    val message: ChatMessage,
) : TwitchEvent
