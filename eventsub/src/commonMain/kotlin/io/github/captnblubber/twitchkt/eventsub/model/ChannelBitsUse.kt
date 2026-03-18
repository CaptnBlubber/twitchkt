package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.bits.use](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelbitsuse)
 *
 * @property broadcasterUserId the user ID of the channel where the Bits were used.
 * @property broadcasterUserLogin the login of the channel where the Bits were used.
 * @property broadcasterUserName the display name of the channel where the Bits were used.
 * @property userId the user ID of the user who used Bits.
 * @property userLogin the login name of the user who used Bits.
 * @property userName the display name of the user who used Bits.
 * @property bits the number of Bits used.
 * @property type the type of Bits use.
 * @property powerUp Power-up details if [type] is [BitsUseType.POWER_UP]; `null` otherwise.
 * @property message the user message with text and fragments.
 */
data class ChannelBitsUse(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val bits: Int,
    val type: BitsUseType,
    val powerUp: PowerUp?,
    val message: ChatMessage?,
) : TwitchEvent
