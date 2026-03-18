package io.github.captnblubber.twitchkt.eventsub.model

import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.chat.notification](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatnotification)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property chatterUserId the user ID of the user in the notification.
 * @property chatterUserLogin the user login of the user in the notification.
 * @property chatterUserName the user display name of the user in the notification.
 * @property noticeType the type of notice: `sub`, `resub`, `sub_gift`, `community_sub_gift`, `raid`, `announcement`, etc.
 * @property message the notification message text and fragments.
 * @property sub information about the sub event. Null if [noticeType] is not `sub`.
 * @property resub information about the resub event. Null if [noticeType] is not `resub`.
 * @property subGift information about the gift sub event. Null if [noticeType] is not `sub_gift`.
 * @property communitySubGift information about the community gift sub event. Null if [noticeType] is not `community_sub_gift`.
 * @property giftPaidUpgrade information about the gift paid upgrade event. Null if [noticeType] is not `gift_paid_upgrade`.
 * @property primePaidUpgrade information about the Prime paid upgrade event. Null if [noticeType] is not `prime_paid_upgrade`.
 * @property payItForward information about the pay-it-forward event. Null if [noticeType] is not `pay_it_forward`.
 * @property raid information about the raid event. Null if [noticeType] is not `raid`.
 * @property unraid information about the unraid event. Null if [noticeType] is not `unraid`.
 * @property announcement information about the announcement event. Null if [noticeType] is not `announcement`.
 * @property bitsBadgeTier information about the Bits badge tier event. Null if [noticeType] is not `bits_badge_tier`.
 * @property charityDonation information about the charity donation event. Null if [noticeType] is not `charity_donation`.
 */
data class ChannelChatNotification(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val chatterUserId: String,
    val chatterUserLogin: String,
    val chatterUserName: String,
    val noticeType: String,
    val message: ChatMessage,
    val sub: SubNotification?,
    val resub: ResubNotification?,
    val subGift: SubGiftNotification?,
    val communitySubGift: CommunitySubGiftNotification?,
    val giftPaidUpgrade: GiftPaidUpgradeNotification?,
    val primePaidUpgrade: PrimePaidUpgradeNotification?,
    val payItForward: PayItForwardNotification?,
    val raid: RaidNotification?,
    val unraid: JsonObject?,
    val announcement: AnnouncementNotification?,
    val bitsBadgeTier: BitsBadgeTierNotification?,
    val charityDonation: CharityDonationNotification?,
) : TwitchEvent
