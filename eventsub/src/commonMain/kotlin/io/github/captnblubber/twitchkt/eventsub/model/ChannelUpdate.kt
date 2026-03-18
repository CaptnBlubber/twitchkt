package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelupdate)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's user login.
 * @property broadcasterUserName the broadcaster's user display name.
 * @property title the channel's stream title.
 * @property language the channel's broadcast language.
 * @property categoryId the channel's category ID.
 * @property categoryName the category name.
 * @property contentClassificationLabels content classification label IDs currently applied on the channel.
 */
data class ChannelUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val title: String,
    val language: String,
    val categoryId: String,
    val categoryName: String,
    val contentClassificationLabels: List<String>,
) : TwitchEvent
