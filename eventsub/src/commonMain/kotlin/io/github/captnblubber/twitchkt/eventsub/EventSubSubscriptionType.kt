package io.github.captnblubber.twitchkt.eventsub

import io.github.captnblubber.twitchkt.helix.EventSubSubscriptionType as HelixEventSubSubscriptionType

/**
 * Type-safe representation of an
 * [EventSub subscription type](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/).
 *
 * Each subclass encodes the Twitch `type` string, `version`, and the exact condition fields
 * required for that subscription — eliminating raw strings and untyped maps from the call site.
 *
 * @property type the EventSub subscription type identifier (e.g. `channel.follow`).
 * @property version the schema version for this subscription type.
 */
sealed class EventSubSubscriptionType(
    override val type: String,
    override val version: String,
) : HelixEventSubSubscriptionType {
    /**
     * Serializes the typed condition fields into the `condition` map expected by the Twitch API.
     */
    abstract override fun toCondition(): Map<String, String>

    /**
     * [channel.follow v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelfollow)
     *
     * @property broadcasterUserId the broadcaster user ID to receive follow events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelFollow(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.follow", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.subscribe v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsubscribe)
     *
     * @property broadcasterUserId the broadcaster user ID to receive subscription events for.
     */
    class ChannelSubscribe(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.subscribe", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.subscription.gift v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsubscriptiongift)
     *
     * @property broadcasterUserId the broadcaster user ID to receive gift subscription events for.
     */
    class ChannelSubscriptionGift(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.subscription.gift", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.subscription.message v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsubscriptionmessage)
     *
     * @property broadcasterUserId the broadcaster user ID to receive resubscription message events for.
     */
    class ChannelSubscriptionMessage(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.subscription.message", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.subscription.end v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsubscriptionend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive subscription end events for.
     */
    class ChannelSubscriptionEnd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.subscription.end", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.chat.message v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatmessage)
     *
     * @property broadcasterUserId the broadcaster user ID whose chat to listen to.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatMessage(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.message", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.chat.notification v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatnotification)
     *
     * @property broadcasterUserId the broadcaster user ID whose chat notifications to listen to.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatNotification(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.notification", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.bits.use v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelbitsuse)
     *
     * @property broadcasterUserId the broadcaster user ID to receive bits use events for.
     */
    class ChannelBitsUse(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.bits.use", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.raid v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelraid)
     *
     * Subscribes to raids **into** the specified broadcaster's channel.
     *
     * @property toBroadcasterUserId the broadcaster user ID receiving the raid.
     */
    class ChannelRaid(
        val toBroadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.raid", "1") {
        override fun toCondition() =
            mapOf(
                "to_broadcaster_user_id" to toBroadcasterUserId,
            )
    }

    /**
     * [channel.update v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive channel update events for.
     */
    class ChannelUpdate(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.update", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.channel_points_custom_reward_redemption.add v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_reward_redemptionadd)
     *
     * @property broadcasterUserId the broadcaster user ID to receive redemption events for.
     * @property rewardId optional reward ID to filter events for a specific reward.
     */
    class ChannelPointsRedemptionAdd(
        val broadcasterUserId: String,
        val rewardId: String? = null,
    ) : EventSubSubscriptionType("channel.channel_points_custom_reward_redemption.add", "1") {
        override fun toCondition() =
            buildMap {
                put("broadcaster_user_id", broadcasterUserId)
                if (rewardId != null) put("reward_id", rewardId)
            }
    }

    /**
     * [channel.channel_points_custom_reward_redemption.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_reward_redemptionupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive redemption update events for.
     * @property rewardId optional reward ID to filter events for a specific reward.
     */
    class ChannelPointsRedemptionUpdate(
        val broadcasterUserId: String,
        val rewardId: String? = null,
    ) : EventSubSubscriptionType("channel.channel_points_custom_reward_redemption.update", "1") {
        override fun toCondition() =
            buildMap {
                put("broadcaster_user_id", broadcasterUserId)
                if (rewardId != null) put("reward_id", rewardId)
            }
    }

    /**
     * [channel.poll.begin v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpollbegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive poll begin events for.
     */
    class PollBegin(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.poll.begin", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.poll.progress v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpollprogress)
     *
     * @property broadcasterUserId the broadcaster user ID to receive poll progress events for.
     */
    class PollProgress(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.poll.progress", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.poll.end v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpollend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive poll end events for.
     */
    class PollEnd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.poll.end", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.hype_train.begin v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelhype_trainbegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive hype train begin events for.
     */
    class HypeTrainBegin(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.hype_train.begin", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.hype_train.progress v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelhype_trainprogress)
     *
     * @property broadcasterUserId the broadcaster user ID to receive hype train progress events for.
     */
    class HypeTrainProgress(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.hype_train.progress", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.hype_train.end v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelhype_trainend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive hype train end events for.
     */
    class HypeTrainEnd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.hype_train.end", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.ad_break.begin v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelad_breakbegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive ad break events for.
     */
    class ChannelAdBreakBegin(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.ad_break.begin", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.cheer v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcheer)
     *
     * @property broadcasterUserId the broadcaster user ID to receive cheer events for.
     */
    class ChannelCheer(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.cheer", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.chat.clear v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatclear)
     *
     * @property broadcasterUserId the broadcaster user ID to receive chat clear events for.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatClear(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.clear", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.chat.clear_user_messages v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatclear_user_messages)
     *
     * @property broadcasterUserId the broadcaster user ID to receive clear user messages events for.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatClearUserMessages(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.clear_user_messages", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.chat.message_delete v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatmessage_delete)
     *
     * @property broadcasterUserId the broadcaster user ID to receive message delete events for.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatMessageDelete(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.message_delete", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.chat_settings.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchat_settingsupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive chat settings update events for.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatSettingsUpdate(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat_settings.update", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.chat.user_message_hold v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatuser_message_hold)
     *
     * @property broadcasterUserId the broadcaster user ID to receive user message hold events for.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatUserMessageHold(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.user_message_hold", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.chat.user_message_update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchatuser_message_update)
     *
     * @property broadcasterUserId the broadcaster user ID to receive user message update events for.
     * @property userId the user ID to read chat as (must match the user access token).
     */
    class ChannelChatUserMessageUpdate(
        val broadcasterUserId: String,
        val userId: String,
    ) : EventSubSubscriptionType("channel.chat.user_message_update", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "user_id" to userId,
            )
    }

    /**
     * [channel.shared_chat.begin v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshared_chatbegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shared chat begin events for.
     */
    class ChannelSharedChatBegin(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.shared_chat.begin", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.shared_chat.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshared_chatupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shared chat update events for.
     */
    class ChannelSharedChatUpdate(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.shared_chat.update", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.shared_chat.end v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshared_chatend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shared chat end events for.
     */
    class ChannelSharedChatEnd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.shared_chat.end", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.ban v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelban)
     *
     * @property broadcasterUserId the broadcaster user ID to receive ban events for.
     */
    class ChannelBan(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.ban", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.unban v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelunban)
     *
     * @property broadcasterUserId the broadcaster user ID to receive unban events for.
     */
    class ChannelUnban(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.unban", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.moderate v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelmoderate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive moderation events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelModerate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.moderate", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.moderator.add v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelmoderatoradd)
     *
     * @property broadcasterUserId the broadcaster user ID to receive moderator add events for.
     */
    class ChannelModeratorAdd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.moderator.add", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.moderator.remove v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelmoderatorremove)
     *
     * @property broadcasterUserId the broadcaster user ID to receive moderator remove events for.
     */
    class ChannelModeratorRemove(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.moderator.remove", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.unban_request.create v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelunban_requestcreate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive unban request events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelUnbanRequestCreate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.unban_request.create", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.unban_request.resolve v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelunban_requestresolve)
     *
     * @property broadcasterUserId the broadcaster user ID to receive unban request resolution events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelUnbanRequestResolve(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.unban_request.resolve", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.suspicious_user.message v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsuspicious_usermessage)
     *
     * @property broadcasterUserId the broadcaster user ID to receive suspicious user message events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelSuspiciousUserMessage(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.suspicious_user.message", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.suspicious_user.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelsuspicious_userupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive suspicious user update events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelSuspiciousUserUpdate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.suspicious_user.update", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.warning.acknowledge v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelwarningacknowledge)
     *
     * @property broadcasterUserId the broadcaster user ID to receive warning acknowledgement events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelWarningAcknowledge(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.warning.acknowledge", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.warning.send v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelwarningsend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive warning send events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelWarningSend(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.warning.send", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.vip.add v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelvipadd)
     *
     * @property broadcasterUserId the broadcaster user ID to receive VIP add events for.
     */
    class ChannelVipAdd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.vip.add", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.vip.remove v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelvipremove)
     *
     * @property broadcasterUserId the broadcaster user ID to receive VIP remove events for.
     */
    class ChannelVipRemove(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.vip.remove", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.shield_mode.begin v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshield_modebegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shield mode begin events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelShieldModeBegin(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.shield_mode.begin", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.shield_mode.end v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshield_modeend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shield mode end events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelShieldModeEnd(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.shield_mode.end", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [stream.online v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#streamonline)
     *
     * @property broadcasterUserId the broadcaster user ID to receive stream online events for.
     */
    class StreamOnline(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("stream.online", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [stream.offline v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#streamoffline)
     *
     * @property broadcasterUserId the broadcaster user ID to receive stream offline events for.
     */
    class StreamOffline(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("stream.offline", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    // — Channel Points, Predictions, Goals, Charity —

    /**
     * [channel.channel_points_automatic_reward_redemption.add v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_automatic_reward_redemptionadd)
     *
     * @property broadcasterUserId the broadcaster user ID to receive automatic reward redemption events for.
     */
    class ChannelPointsAutomaticRedemptionAdd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.channel_points_automatic_reward_redemption.add", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.channel_points_custom_reward.add v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_rewardadd)
     *
     * @property broadcasterUserId the broadcaster user ID to receive custom reward add events for.
     */
    class ChannelPointsCustomRewardAdd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.channel_points_custom_reward.add", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.channel_points_custom_reward.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_rewardupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive custom reward update events for.
     */
    class ChannelPointsCustomRewardUpdate(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.channel_points_custom_reward.update", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.channel_points_custom_reward.remove v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelchannel_points_custom_rewardremove)
     *
     * @property broadcasterUserId the broadcaster user ID to receive custom reward remove events for.
     */
    class ChannelPointsCustomRewardRemove(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.channel_points_custom_reward.remove", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.prediction.begin v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpredictionbegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive prediction begin events for.
     */
    class PredictionBegin(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.prediction.begin", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.prediction.progress v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpredictionprogress)
     *
     * @property broadcasterUserId the broadcaster user ID to receive prediction progress events for.
     */
    class PredictionProgress(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.prediction.progress", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.prediction.lock v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpredictionlock)
     *
     * @property broadcasterUserId the broadcaster user ID to receive prediction lock events for.
     */
    class PredictionLock(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.prediction.lock", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.prediction.end v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpredictionend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive prediction end events for.
     */
    class PredictionEnd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.prediction.end", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.goal.begin v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelgoalbegin)
     *
     * @property broadcasterUserId the broadcaster user ID to receive goal begin events for.
     */
    class ChannelGoalBegin(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.goal.begin", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.goal.progress v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelgoalprogress)
     *
     * @property broadcasterUserId the broadcaster user ID to receive goal progress events for.
     */
    class ChannelGoalProgress(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.goal.progress", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.goal.end v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelgoalend)
     *
     * @property broadcasterUserId the broadcaster user ID to receive goal end events for.
     */
    class ChannelGoalEnd(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.goal.end", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.charity_campaign.donate v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcharity_campaigndonate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive charity donation events for.
     */
    class CharityDonate(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.charity_campaign.donate", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.charity_campaign.start v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcharity_campaignstart)
     *
     * @property broadcasterUserId the broadcaster user ID to receive charity campaign start events for.
     */
    class CharityCampaignStart(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.charity_campaign.start", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.charity_campaign.progress v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcharity_campaignprogress)
     *
     * @property broadcasterUserId the broadcaster user ID to receive charity campaign progress events for.
     */
    class CharityCampaignProgress(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.charity_campaign.progress", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    /**
     * [channel.charity_campaign.stop v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelcharity_campaignstop)
     *
     * @property broadcasterUserId the broadcaster user ID to receive charity campaign stop events for.
     */
    class CharityCampaignStop(
        val broadcasterUserId: String,
    ) : EventSubSubscriptionType("channel.charity_campaign.stop", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
            )
    }

    // — Automod, Shoutouts, User, System —

    /**
     * [automod.message.hold v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodmessagehold)
     *
     * @property broadcasterUserId the broadcaster user ID to receive automod message hold events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class AutomodMessageHold(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("automod.message.hold", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [automod.message.update v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodmessageupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive automod message update events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class AutomodMessageUpdate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("automod.message.update", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [automod.settings.update v2](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodsettingsupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive automod settings update events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class AutomodSettingsUpdate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("automod.settings.update", "2") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [automod.terms.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodtermsupdate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive automod terms update events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class AutomodTermsUpdate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("automod.terms.update", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.shoutout.create v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshoutoutcreate)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shoutout create events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelShoutoutCreate(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.shoutout.create", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [channel.shoutout.receive v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelshoutoutreceive)
     *
     * @property broadcasterUserId the broadcaster user ID to receive shoutout receive events for.
     * @property moderatorUserId a user ID that has moderator permissions in the broadcaster's channel.
     */
    class ChannelShoutoutReceive(
        val broadcasterUserId: String,
        val moderatorUserId: String,
    ) : EventSubSubscriptionType("channel.shoutout.receive", "1") {
        override fun toCondition() =
            mapOf(
                "broadcaster_user_id" to broadcasterUserId,
                "moderator_user_id" to moderatorUserId,
            )
    }

    /**
     * [user.authorization.grant v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#userauthorizationgrant)
     *
     * @property clientId the client ID to receive user authorization grant events for.
     */
    class UserAuthorizationGrant(
        val clientId: String,
    ) : EventSubSubscriptionType("user.authorization.grant", "1") {
        override fun toCondition() =
            mapOf(
                "client_id" to clientId,
            )
    }

    /**
     * [user.authorization.revoke v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#userauthorizationrevoke)
     *
     * @property clientId the client ID to receive user authorization revoke events for.
     */
    class UserAuthorizationRevoke(
        val clientId: String,
    ) : EventSubSubscriptionType("user.authorization.revoke", "1") {
        override fun toCondition() =
            mapOf(
                "client_id" to clientId,
            )
    }

    /**
     * [user.update v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#userupdate)
     *
     * @property userId the user ID to receive user update events for.
     */
    class UserUpdate(
        val userId: String,
    ) : EventSubSubscriptionType("user.update", "1") {
        override fun toCondition() =
            mapOf(
                "user_id" to userId,
            )
    }

    /**
     * [extension.bits_transaction.create v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#extensionbits_transactioncreate)
     *
     * @property extensionClientId the extension client ID to receive bits transaction events for.
     */
    class ExtensionBitsTransactionCreate(
        val extensionClientId: String,
    ) : EventSubSubscriptionType("extension.bits_transaction.create", "1") {
        override fun toCondition() =
            mapOf(
                "extension_client_id" to extensionClientId,
            )
    }

    /**
     * [conduit.shard.disabled v1](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#conduitsharddisabled)
     *
     * @property clientId the client ID to receive conduit shard disabled events for.
     */
    class ConduitShardDisabled(
        val clientId: String,
    ) : EventSubSubscriptionType("conduit.shard.disabled", "1") {
        override fun toCondition() =
            mapOf(
                "client_id" to clientId,
            )
    }
}
