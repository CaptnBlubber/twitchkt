package io.github.captnblubber.twitchkt.eventsub.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

// ── Chat & Messages ──────────────────────────────────────────────────

@Serializable
enum class FragmentType {
    @SerialName("text")
    TEXT,

    @SerialName("emote")
    EMOTE,

    @SerialName("cheermote")
    CHEERMOTE,

    @SerialName("mention")
    MENTION,
}

@Serializable
enum class ChatMessageType {
    @SerialName("text")
    TEXT,

    @SerialName("channel_points_highlighted")
    CHANNEL_POINTS_HIGHLIGHTED,

    @SerialName("channel_points_sub_only")
    CHANNEL_POINTS_SUB_ONLY,

    @SerialName("user_intro")
    USER_INTRO,

    @SerialName("power_ups_message_effect")
    POWER_UPS_MESSAGE_EFFECT,

    @SerialName("power_ups_gigantified_emote")
    POWER_UPS_GIGANTIFIED_EMOTE,

    OTHER,
}

@Serializable
enum class BitsUseType {
    @SerialName("cheer")
    CHEER,

    @SerialName("power_up")
    POWER_UP,

    @SerialName("combo")
    COMBO,

    OTHER,
}

@Serializable
data class ChatMessage(
    val text: String = "",
    val fragments: List<MessageFragment> = emptyList(),
)

@Serializable
data class MessageFragment(
    val type: FragmentType = FragmentType.TEXT,
    val text: String = "",
    val cheermote: Cheermote? = null,
    val emote: Emote? = null,
    val mention: Mention? = null,
)

@Serializable
data class Cheermote(
    val prefix: String = "",
    val bits: Int = 0,
    val tier: Int = 0,
)

@Serializable
data class Emote(
    val id: String = "",
    @SerialName("emote_set_id") val emoteSetId: String = "",
    @SerialName("owner_id") val ownerId: String? = null,
    val format: List<String> = emptyList(),
)

@Serializable
data class Mention(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
)

@Serializable
data class ChatBadge(
    @SerialName("set_id") val setId: String = "",
    val id: String = "",
    val info: String = "",
)

@Serializable
data class ChatCheer(
    val bits: Int = 0,
)

@Serializable
data class ChatReply(
    @SerialName("parent_message_id") val parentMessageId: String = "",
    @SerialName("parent_message_body") val parentMessageBody: String = "",
    @SerialName("parent_user_id") val parentUserId: String = "",
    @SerialName("parent_user_name") val parentUserName: String = "",
    @SerialName("parent_user_login") val parentUserLogin: String = "",
    @SerialName("thread_message_id") val threadMessageId: String = "",
    @SerialName("thread_user_id") val threadUserId: String = "",
    @SerialName("thread_user_name") val threadUserName: String = "",
    @SerialName("thread_user_login") val threadUserLogin: String = "",
)

// ── Polls ────────────────────────────────────────────────────────────

@Serializable
data class PollChoice(
    val id: String = "",
    val title: String = "",
    @SerialName("bits_votes") val bitsVotes: Int = 0,
    @SerialName("channel_points_votes") val channelPointsVotes: Int = 0,
    val votes: Int = 0,
)

@Serializable
data class VotingSettings(
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    @SerialName("amount_per_vote") val amountPerVote: Int = 0,
)

// ── Predictions ──────────────────────────────────────────────────────

@Serializable
data class PredictionOutcome(
    val id: String = "",
    val title: String = "",
    val color: String = "",
    val users: Int = 0,
    @SerialName("channel_points") val channelPoints: Int = 0,
    @SerialName("top_predictors") val topPredictors: List<TopPredictor>? = null,
)

@Serializable
data class TopPredictor(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("channel_points_won") val channelPointsWon: Int? = null,
    @SerialName("channel_points_used") val channelPointsUsed: Int = 0,
)

// ── Hype Train ───────────────────────────────────────────────────────

@Serializable
data class HypeTrainContribution(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    val type: String = "",
    val total: Int = 0,
)

// ── Channel Points ───────────────────────────────────────────────────

@Serializable
data class ChannelPointsReward(
    val id: String = "",
    val title: String = "",
    val cost: Int = 0,
    val prompt: String = "",
)

@Serializable
data class MaxPerStreamSetting(
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    val value: Int = 0,
)

@Serializable
data class RewardImage(
    @SerialName("url_1x") val url1x: String = "",
    @SerialName("url_2x") val url2x: String = "",
    @SerialName("url_4x") val url4x: String = "",
)

@Serializable
data class GlobalCooldownSetting(
    @SerialName("is_enabled") val isEnabled: Boolean = false,
    val seconds: Int = 0,
)

// ── Charity ──────────────────────────────────────────────────────────

@Serializable
data class CurrencyAmount(
    val value: Int = 0,
    @SerialName("decimal_places") val decimalPlaces: Int = 0,
    val currency: String = "",
)

// ── Chat Notification Sub-Types ──────────────────────────────────────

@Serializable
data class SubNotification(
    @SerialName("sub_tier") val subTier: String = "",
    @SerialName("is_prime") val isPrime: Boolean = false,
    @SerialName("duration_months") val durationMonths: Int = 0,
)

@Serializable
data class ResubNotification(
    @SerialName("cumulative_months") val cumulativeMonths: Int = 0,
    @SerialName("duration_months") val durationMonths: Int = 0,
    @SerialName("streak_months") val streakMonths: Int = 0,
    @SerialName("sub_tier") val subTier: String = "",
    @SerialName("is_prime") val isPrime: Boolean = false,
    @SerialName("is_gift") val isGift: Boolean = false,
    @SerialName("gifter_is_anonymous") val gifterIsAnonymous: Boolean? = null,
    @SerialName("gifter_user_id") val gifterUserId: String? = null,
    @SerialName("gifter_user_name") val gifterUserName: String? = null,
    @SerialName("gifter_user_login") val gifterUserLogin: String? = null,
)

@Serializable
data class SubGiftNotification(
    @SerialName("duration_months") val durationMonths: Int = 0,
    @SerialName("cumulative_total") val cumulativeTotal: Int? = null,
    @SerialName("recipient_user_id") val recipientUserId: String = "",
    @SerialName("recipient_user_name") val recipientUserName: String = "",
    @SerialName("recipient_user_login") val recipientUserLogin: String = "",
    @SerialName("sub_tier") val subTier: String = "",
    @SerialName("community_gift_id") val communityGiftId: String? = null,
)

@Serializable
data class CommunitySubGiftNotification(
    val id: String = "",
    val total: Int = 0,
    @SerialName("sub_tier") val subTier: String = "",
    @SerialName("cumulative_total") val cumulativeTotal: Int? = null,
)

@Serializable
data class GiftPaidUpgradeNotification(
    @SerialName("gifter_is_anonymous") val gifterIsAnonymous: Boolean = false,
    @SerialName("gifter_user_id") val gifterUserId: String? = null,
    @SerialName("gifter_user_name") val gifterUserName: String? = null,
    @SerialName("gifter_user_login") val gifterUserLogin: String? = null,
)

@Serializable
data class PrimePaidUpgradeNotification(
    @SerialName("sub_tier") val subTier: String = "",
)

@Serializable
data class PayItForwardNotification(
    @SerialName("gifter_is_anonymous") val gifterIsAnonymous: Boolean = false,
    @SerialName("gifter_user_id") val gifterUserId: String? = null,
    @SerialName("gifter_user_name") val gifterUserName: String? = null,
    @SerialName("gifter_user_login") val gifterUserLogin: String? = null,
)

@Serializable
data class RaidNotification(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("viewer_count") val viewerCount: Int = 0,
)

@Serializable
data class AnnouncementNotification(
    val color: String = "",
)

@Serializable
data class BitsBadgeTierNotification(
    val tier: Int = 0,
)

@Serializable
data class CharityDonationNotification(
    @SerialName("charity_name") val charityName: String = "",
    val amount: CurrencyAmount = CurrencyAmount(),
)

// ── Moderation ───────────────────────────────────────────────────────

@Serializable
data class ModFollowers(
    @SerialName("follow_duration_minutes") val followDurationMinutes: Int = 0,
)

@Serializable
data class ModSlow(
    @SerialName("wait_time_seconds") val waitTimeSeconds: Int = 0,
)

@Serializable
data class ModTargetUser(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
)

@Serializable
data class ModBan(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    val reason: String = "",
)

@Serializable
data class ModTimeout(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    val reason: String = "",
    @SerialName("expires_at") val expiresAt: Instant? = null,
)

@Serializable
data class ModRaid(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("viewer_count") val viewerCount: Int = 0,
)

@Serializable
data class ModDelete(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("message_id") val messageId: String = "",
    @SerialName("message_body") val messageBody: String = "",
)

@Serializable
data class ModAutomodTerms(
    val action: String = "",
    val list: String = "",
    val terms: List<String> = emptyList(),
    @SerialName("from_automod") val fromAutomod: Boolean = false,
)

@Serializable
data class ModWarn(
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_login") val userLogin: String = "",
    @SerialName("user_name") val userName: String = "",
    val reason: String? = null,
    @SerialName("chat_rules_cited") val chatRulesCited: List<String>? = null,
)

// ── Bits ─────────────────────────────────────────────────────────────

@Serializable
data class PowerUp(
    val type: String = "",
    val emote: PowerUpEmote? = null,
)

@Serializable
data class PowerUpEmote(
    val id: String = "",
    @SerialName("set_id") val setId: String = "",
)

// ── Shared Chat ──────────────────────────────────────────────────────

@Serializable
data class SharedChatParticipant(
    @SerialName("broadcaster_user_id") val broadcasterUserId: String = "",
    @SerialName("broadcaster_user_login") val broadcasterUserLogin: String = "",
    @SerialName("broadcaster_user_name") val broadcasterUserName: String = "",
)

// ── Extensions ───────────────────────────────────────────────────────

@Serializable
data class ExtensionProduct(
    val name: String = "",
    val sku: String = "",
    val bits: Int = 0,
    @SerialName("in_development") val inDevelopment: Boolean = false,
)

// ── Conduit ──────────────────────────────────────────────────────────

@Serializable
data class ConduitTransport(
    val method: String = "",
    val callback: String? = null,
    @SerialName("session_id") val sessionId: String? = null,
    @SerialName("connected_at") val connectedAt: Instant? = null,
    @SerialName("disconnected_at") val disconnectedAt: Instant? = null,
)
