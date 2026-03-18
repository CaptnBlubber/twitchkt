package io.github.captnblubber.twitchkt.eventsub.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import io.github.captnblubber.twitchkt.eventsub.model.AnnouncementNotification
import io.github.captnblubber.twitchkt.eventsub.model.BitsBadgeTierNotification
import io.github.captnblubber.twitchkt.eventsub.model.BitsUseType
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsReward
import io.github.captnblubber.twitchkt.eventsub.model.CharityDonationNotification
import io.github.captnblubber.twitchkt.eventsub.model.ChatBadge
import io.github.captnblubber.twitchkt.eventsub.model.ChatCheer
import io.github.captnblubber.twitchkt.eventsub.model.ChatMessage
import io.github.captnblubber.twitchkt.eventsub.model.ChatMessageType
import io.github.captnblubber.twitchkt.eventsub.model.ChatReply
import io.github.captnblubber.twitchkt.eventsub.model.CommunitySubGiftNotification
import io.github.captnblubber.twitchkt.eventsub.model.ConduitTransport
import io.github.captnblubber.twitchkt.eventsub.model.CurrencyAmount
import io.github.captnblubber.twitchkt.eventsub.model.ExtensionProduct
import io.github.captnblubber.twitchkt.eventsub.model.GiftPaidUpgradeNotification
import io.github.captnblubber.twitchkt.eventsub.model.GlobalCooldownSetting
import io.github.captnblubber.twitchkt.eventsub.model.HypeTrainContribution
import io.github.captnblubber.twitchkt.eventsub.model.MaxPerStreamSetting
import io.github.captnblubber.twitchkt.eventsub.model.ModAutomodTerms
import io.github.captnblubber.twitchkt.eventsub.model.ModBan
import io.github.captnblubber.twitchkt.eventsub.model.ModDelete
import io.github.captnblubber.twitchkt.eventsub.model.ModFollowers
import io.github.captnblubber.twitchkt.eventsub.model.ModRaid
import io.github.captnblubber.twitchkt.eventsub.model.ModSlow
import io.github.captnblubber.twitchkt.eventsub.model.ModTargetUser
import io.github.captnblubber.twitchkt.eventsub.model.ModTimeout
import io.github.captnblubber.twitchkt.eventsub.model.ModWarn
import io.github.captnblubber.twitchkt.eventsub.model.PayItForwardNotification
import io.github.captnblubber.twitchkt.eventsub.model.PollChoice
import io.github.captnblubber.twitchkt.eventsub.model.PowerUp
import io.github.captnblubber.twitchkt.eventsub.model.PredictionOutcome
import io.github.captnblubber.twitchkt.eventsub.model.PrimePaidUpgradeNotification
import io.github.captnblubber.twitchkt.eventsub.model.RaidNotification
import io.github.captnblubber.twitchkt.eventsub.model.ResubNotification
import io.github.captnblubber.twitchkt.eventsub.model.RewardImage
import io.github.captnblubber.twitchkt.eventsub.model.SharedChatParticipant
import io.github.captnblubber.twitchkt.eventsub.model.SubGiftNotification
import io.github.captnblubber.twitchkt.eventsub.model.SubNotification
import io.github.captnblubber.twitchkt.eventsub.model.VotingSettings
import kotlin.time.Instant

@Serializable
internal data class ChannelFollowPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("followed_at") val followedAt: Instant,
)

@Serializable
internal data class ChannelSubscribePayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val tier: String,
    @SerialName("is_gift") val isGift: Boolean,
)

@Serializable
internal data class ChannelSubscriptionGiftPayload(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_login") val userLogin: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val total: Int,
    val tier: String,
    @SerialName("cumulative_total") val cumulativeTotal: Int? = null,
    @SerialName("is_anonymous") val isAnonymous: Boolean,
)

@Serializable
internal data class ChannelSubscriptionMessagePayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val tier: String,
    val message: ChatMessage,
    @SerialName("cumulative_months") val cumulativeMonths: Int,
    @SerialName("streak_months") val streakMonths: Int? = null,
    @SerialName("duration_months") val durationMonths: Int,
)

@Serializable
internal data class ChannelSubscriptionEndPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val tier: String,
    @SerialName("is_gift") val isGift: Boolean,
)

@Serializable
internal data class ChannelChatNotificationPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("chatter_user_id") val chatterUserId: String,
    @SerialName("chatter_user_login") val chatterUserLogin: String,
    @SerialName("chatter_user_name") val chatterUserName: String,
    @SerialName("notice_type") val noticeType: String,
    val message: ChatMessage,
    val sub: SubNotification? = null,
    val resub: ResubNotification? = null,
    @SerialName("sub_gift") val subGift: SubGiftNotification? = null,
    @SerialName("community_sub_gift") val communitySubGift: CommunitySubGiftNotification? = null,
    @SerialName("gift_paid_upgrade") val giftPaidUpgrade: GiftPaidUpgradeNotification? = null,
    @SerialName("prime_paid_upgrade") val primePaidUpgrade: PrimePaidUpgradeNotification? = null,
    @SerialName("pay_it_forward") val payItForward: PayItForwardNotification? = null,
    val raid: RaidNotification? = null,
    val unraid: JsonObject? = null,
    val announcement: AnnouncementNotification? = null,
    @SerialName("bits_badge_tier") val bitsBadgeTier: BitsBadgeTierNotification? = null,
    @SerialName("charity_donation") val charityDonation: CharityDonationNotification? = null,
)

@Serializable
internal data class ChannelBitsUsePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val bits: Int,
    val type: BitsUseType = BitsUseType.OTHER,
    @SerialName("power_up") val powerUp: PowerUp? = null,
    val message: ChatMessage? = null,
)

@Serializable
internal data class ChannelRaidPayload(
    @SerialName("from_broadcaster_user_id") val fromBroadcasterUserId: String,
    @SerialName("from_broadcaster_user_login") val fromBroadcasterUserLogin: String,
    @SerialName("from_broadcaster_user_name") val fromBroadcasterUserName: String,
    @SerialName("to_broadcaster_user_id") val toBroadcasterUserId: String,
    @SerialName("to_broadcaster_user_login") val toBroadcasterUserLogin: String,
    @SerialName("to_broadcaster_user_name") val toBroadcasterUserName: String,
    val viewers: Int,
)

@Serializable
internal data class PollBeginPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    val choices: List<PollChoice>,
    @SerialName("bits_voting") val bitsVoting: VotingSettings,
    @SerialName("channel_points_voting") val channelPointsVoting: VotingSettings,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("ends_at") val endsAt: Instant,
)

@Serializable
internal data class PollProgressPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    val choices: List<PollChoice>,
    @SerialName("bits_voting") val bitsVoting: VotingSettings,
    @SerialName("channel_points_voting") val channelPointsVoting: VotingSettings,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("ends_at") val endsAt: Instant,
)

@Serializable
internal data class PollEndPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    val choices: List<PollChoice>,
    @SerialName("bits_voting") val bitsVoting: VotingSettings,
    @SerialName("channel_points_voting") val channelPointsVoting: VotingSettings,
    val status: String,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("ended_at") val endedAt: Instant,
)

@Serializable
internal data class ChannelPointsRedemptionPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("user_input") val userInput: String,
    val status: String,
    val reward: ChannelPointsReward,
    @SerialName("redeemed_at") val redeemedAt: Instant,
)

@Serializable
internal data class ChannelChatMessagePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("chatter_user_id") val chatterUserId: String,
    @SerialName("chatter_user_login") val chatterUserLogin: String,
    @SerialName("chatter_user_name") val chatterUserName: String,
    @SerialName("message_id") val chatMessageId: String,
    val message: ChatMessage,
    val color: String,
    val badges: List<ChatBadge>,
    @SerialName("message_type") val messageType: ChatMessageType = ChatMessageType.OTHER,
    val cheer: ChatCheer? = null,
    val reply: ChatReply? = null,
    @SerialName("channel_points_custom_reward_id") val channelPointsCustomRewardId: String? = null,
)

@Serializable
internal data class HypeTrainBeginPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val total: Int,
    val progress: Int,
    val goal: Int,
    @SerialName("top_contributions") val topContributions: List<HypeTrainContribution>,
    @SerialName("last_contribution") val lastContribution: HypeTrainContribution? = null,
    val level: Int,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("expires_at") val expiresAt: Instant,
    @SerialName("is_golden_kappa_train") val isGoldenKappaTrain: Boolean = false,
    @SerialName("all_time_high_level") val allTimeHighLevel: Int = 0,
    @SerialName("all_time_high_total") val allTimeHighTotal: Int = 0,
)

@Serializable
internal data class HypeTrainProgressPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val total: Int,
    val progress: Int,
    val goal: Int,
    @SerialName("top_contributions") val topContributions: List<HypeTrainContribution>,
    @SerialName("last_contribution") val lastContribution: HypeTrainContribution? = null,
    val level: Int,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("expires_at") val expiresAt: Instant,
    @SerialName("is_golden_kappa_train") val isGoldenKappaTrain: Boolean = false,
)

@Serializable
internal data class HypeTrainEndPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val level: Int,
    val total: Int,
    @SerialName("top_contributions") val topContributions: List<HypeTrainContribution>,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("ended_at") val endedAt: Instant,
    @SerialName("cooldown_ends_at") val cooldownEndsAt: Instant,
    @SerialName("is_golden_kappa_train") val isGoldenKappaTrain: Boolean = false,
)

@Serializable
internal data class StreamOnlinePayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val type: String,
    @SerialName("started_at") val startedAt: Instant,
)

@Serializable
internal data class StreamOfflinePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
)

@Serializable
internal data class ChannelUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    val language: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("content_classification_labels") val contentClassificationLabels: List<String>,
)

@Serializable
internal data class ChannelAdBreakBeginPayload(
    @SerialName("duration_seconds") val durationSeconds: Int,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("is_automatic") val isAutomatic: Boolean,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("requester_user_id") val requesterUserId: String,
    @SerialName("requester_user_login") val requesterUserLogin: String,
    @SerialName("requester_user_name") val requesterUserName: String,
)

@Serializable
internal data class ChannelBanPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    val reason: String,
    @SerialName("banned_at") val bannedAt: Instant,
    @SerialName("ends_at") val endsAt: Instant? = null,
    @SerialName("is_permanent") val isPermanent: Boolean,
)

@Serializable
internal data class ChannelUnbanPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
)

@Serializable
internal data class ChannelModeratePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    val action: String,
    val followers: ModFollowers? = null,
    val slow: ModSlow? = null,
    val vip: ModTargetUser? = null,
    val unvip: ModTargetUser? = null,
    val mod: ModTargetUser? = null,
    val unmod: ModTargetUser? = null,
    val ban: ModBan? = null,
    val unban: ModTargetUser? = null,
    val timeout: ModTimeout? = null,
    val untimeout: ModTargetUser? = null,
    val raid: ModRaid? = null,
    val unraid: ModTargetUser? = null,
    val delete: ModDelete? = null,
    @SerialName("automod_terms") val automodTerms: ModAutomodTerms? = null,
    val unmute: ModTargetUser? = null,
    val warn: ModWarn? = null,
    val shared: JsonObject? = null,
)

@Serializable
internal data class ChannelModeratorPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
)

@Serializable
internal data class ChannelUnbanRequestCreatePayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val text: String,
    @SerialName("created_at") val createdAt: Instant,
)

@Serializable
internal data class ChannelUnbanRequestResolvePayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("resolution_text") val resolutionText: String? = null,
    val status: String,
)

@Serializable
internal data class ChannelSuspiciousUserMessagePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("low_trust_status") val lowTrustStatus: String,
    @SerialName("shared_ban_channel_ids") val sharedBanChannelIds: List<String>,
    val types: List<String>,
    @SerialName("banned_channel_count") val bannedChannelCount: Int,
    val message: ChatMessage,
)

@Serializable
internal data class ChannelSuspiciousUserUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("low_trust_status") val lowTrustStatus: String,
)

@Serializable
internal data class ChannelWarningAcknowledgePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
)

@Serializable
internal data class ChannelWarningSendPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val reason: String? = null,
    @SerialName("chat_rules_cited") val chatRulesCited: List<String>? = null,
)

@Serializable
internal data class ChannelVipPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
)

@Serializable
internal data class ChannelShieldModeBeginPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("started_at") val startedAt: Instant,
)

@Serializable
internal data class ChannelShieldModeEndPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("ended_at") val endedAt: Instant,
)

@Serializable
internal data class ChannelCheerPayload(
    @SerialName("is_anonymous") val isAnonymous: Boolean,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_login") val userLogin: String? = null,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val message: String,
    val bits: Int,
)

@Serializable
internal data class ChannelChatClearPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
)

@Serializable
internal data class ChannelChatClearUserMessagesPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("target_user_id") val targetUserId: String,
    @SerialName("target_user_login") val targetUserLogin: String,
    @SerialName("target_user_name") val targetUserName: String,
)

@Serializable
internal data class ChannelChatMessageDeletePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("target_user_id") val targetUserId: String,
    @SerialName("target_user_login") val targetUserLogin: String,
    @SerialName("target_user_name") val targetUserName: String,
    @SerialName("message_id") val targetMessageId: String,
)

@Serializable
internal data class ChannelChatSettingsUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("emote_mode") val emoteMode: Boolean,
    @SerialName("follower_mode") val followerMode: Boolean,
    @SerialName("follower_mode_duration_minutes") val followerModeDurationMinutes: Int? = null,
    @SerialName("slow_mode") val slowMode: Boolean,
    @SerialName("slow_mode_wait_time_seconds") val slowModeWaitTimeSeconds: Int? = null,
    @SerialName("subscriber_mode") val subscriberMode: Boolean,
    @SerialName("unique_chat_mode") val uniqueChatMode: Boolean,
)

@Serializable
internal data class ChannelChatUserMessageHoldPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("message_id") val chatMessageId: String,
    val message: ChatMessage,
)

@Serializable
internal data class ChannelChatUserMessageUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val status: String,
    @SerialName("message_id") val chatMessageId: String,
    val message: ChatMessage,
)

@Serializable
internal data class ChannelSharedChatPayload(
    @SerialName("session_id") val sessionId: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("host_broadcaster_user_id") val hostBroadcasterUserId: String,
    @SerialName("host_broadcaster_user_login") val hostBroadcasterUserLogin: String,
    @SerialName("host_broadcaster_user_name") val hostBroadcasterUserName: String,
    val participants: List<SharedChatParticipant> = emptyList(),
)

@Serializable
internal data class SubscriptionPayload(
    val id: String,
    val status: String,
    val type: String,
)

// — Channel Points, Predictions, Goals, Charity —

@Serializable
internal data class ChannelPointsAutomaticRedemptionAddPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val id: String,
    val reward: ChannelPointsReward,
    val message: ChatMessage? = null,
    @SerialName("user_input") val userInput: String? = null,
    @SerialName("redeemed_at") val redeemedAt: Instant,
)

@Serializable
internal data class ChannelPointsCustomRewardPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("is_enabled") val isEnabled: Boolean,
    @SerialName("is_paused") val isPaused: Boolean,
    @SerialName("is_in_stock") val isInStock: Boolean,
    val title: String,
    val cost: Int,
    val prompt: String,
    @SerialName("is_user_input_required") val isUserInputRequired: Boolean,
    @SerialName("should_redemptions_skip_request_queue") val shouldRedemptionsSkipRequestQueue: Boolean,
    @SerialName("max_per_stream") val maxPerStream: MaxPerStreamSetting,
    @SerialName("max_per_user_per_stream") val maxPerUserPerStream: MaxPerStreamSetting,
    @SerialName("background_color") val backgroundColor: String,
    val image: RewardImage? = null,
    @SerialName("default_image") val defaultImage: RewardImage,
    @SerialName("global_cooldown") val globalCooldown: GlobalCooldownSetting,
    @SerialName("cooldown_expires_at") val cooldownExpiresAt: Instant? = null,
    @SerialName("redemptions_redeemed_current_stream") val redemptionsRedeemedCurrentStream: Int? = null,
)

@Serializable
internal data class PredictionBeginPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    val outcomes: List<PredictionOutcome>,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("locks_at") val locksAt: Instant,
)

@Serializable
internal data class PredictionLockPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    val outcomes: List<PredictionOutcome>,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("locked_at") val lockedAt: Instant,
)

@Serializable
internal data class PredictionEndPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val title: String,
    @SerialName("winning_outcome_id") val winningOutcomeId: String? = null,
    val outcomes: List<PredictionOutcome>,
    val status: String,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("ended_at") val endedAt: Instant,
)

@Serializable
internal data class ChannelGoalPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val type: String,
    val description: String,
    @SerialName("current_amount") val currentAmount: Int,
    @SerialName("target_amount") val targetAmount: Int,
    @SerialName("started_at") val startedAt: Instant,
)

@Serializable
internal data class ChannelGoalEndPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    val type: String,
    val description: String,
    @SerialName("is_achieved") val isAchieved: Boolean,
    @SerialName("current_amount") val currentAmount: Int,
    @SerialName("target_amount") val targetAmount: Int,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("ended_at") val endedAt: Instant,
)

@Serializable
internal data class CharityDonatePayload(
    val id: String,
    @SerialName("campaign_id") val campaignId: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("charity_name") val charityName: String,
    @SerialName("charity_description") val charityDescription: String,
    @SerialName("charity_logo") val charityLogo: String,
    @SerialName("charity_website") val charityWebsite: String,
    val amount: CurrencyAmount,
)

@Serializable
internal data class CharityCampaignPayload(
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("charity_name") val charityName: String,
    @SerialName("charity_description") val charityDescription: String,
    @SerialName("charity_logo") val charityLogo: String,
    @SerialName("charity_website") val charityWebsite: String,
    @SerialName("current_amount") val currentAmount: CurrencyAmount,
    @SerialName("target_amount") val targetAmount: CurrencyAmount,
    @SerialName("started_at") val startedAt: Instant? = null,
    @SerialName("stopped_at") val stoppedAt: Instant? = null,
)

// — Automod, Shoutouts, User, System —

@Serializable
internal data class ChannelShoutoutCreatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("to_broadcaster_user_id") val toBroadcasterUserId: String,
    @SerialName("to_broadcaster_user_login") val toBroadcasterUserLogin: String,
    @SerialName("to_broadcaster_user_name") val toBroadcasterUserName: String,
    @SerialName("viewer_count") val viewerCount: Int,
    @SerialName("started_at") val startedAt: Instant,
    @SerialName("cooldown_ends_at") val cooldownEndsAt: Instant,
    @SerialName("target_cooldown_ends_at") val targetCooldownEndsAt: Instant,
)

@Serializable
internal data class ChannelShoutoutReceivePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("from_broadcaster_user_id") val fromBroadcasterUserId: String,
    @SerialName("from_broadcaster_user_login") val fromBroadcasterUserLogin: String,
    @SerialName("from_broadcaster_user_name") val fromBroadcasterUserName: String,
    @SerialName("viewer_count") val viewerCount: Int,
    @SerialName("started_at") val startedAt: Instant,
)

@Serializable
internal data class UserAuthorizationPayload(
    @SerialName("client_id") val clientId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_login") val userLogin: String? = null,
    @SerialName("user_name") val userName: String? = null,
)

@Serializable
internal data class UserUpdatePayload(
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val email: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean,
    val description: String,
)

@Serializable
internal data class ExtensionBitsTransactionCreatePayload(
    @SerialName("extension_client_id") val extensionClientId: String,
    val id: String,
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    val product: ExtensionProduct,
)

@Serializable
internal data class AutomodMessageHoldPayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("message_id") val chatMessageId: String,
    val message: ChatMessage,
    val category: String,
    val level: Int,
    @SerialName("held_at") val heldAt: Instant,
)

@Serializable
internal data class AutomodMessageUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("message_id") val chatMessageId: String,
    val message: ChatMessage,
    val category: String,
    val level: Int,
    val status: String,
    @SerialName("held_at") val heldAt: Instant,
)

@Serializable
internal data class AutomodSettingsUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    @SerialName("overall_level") val overallLevel: Int? = null,
    val disability: Int,
    val aggression: Int,
    @SerialName("sexuality_sex_or_gender") val sexualitySexOrGender: Int,
    val misogyny: Int,
    val bullying: Int,
    val swearing: Int,
    @SerialName("race_ethnicity_or_religion") val raceEthnicityOrReligion: Int,
    @SerialName("sex_based_terms") val sexBasedTerms: Int,
)

@Serializable
internal data class AutomodTermsUpdatePayload(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String,
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name") val broadcasterUserName: String,
    @SerialName("moderator_user_id") val moderatorUserId: String,
    @SerialName("moderator_user_login") val moderatorUserLogin: String,
    @SerialName("moderator_user_name") val moderatorUserName: String,
    val action: String,
    @SerialName("from_automod") val fromAutomod: Boolean,
    val terms: List<String>,
)

@Serializable
internal data class ConduitShardDisabledPayload(
    @SerialName("conduit_id") val conduitId: String,
    @SerialName("shard_id") val shardId: String,
    val status: String,
    val transport: ConduitTransport,
)
