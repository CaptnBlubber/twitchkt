package io.github.captnblubber.twitchkt.eventsub.internal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import io.github.captnblubber.twitchkt.eventsub.model.AutomodMessageHold
import io.github.captnblubber.twitchkt.eventsub.model.AutomodMessageUpdate
import io.github.captnblubber.twitchkt.eventsub.model.AutomodSettingsUpdate
import io.github.captnblubber.twitchkt.eventsub.model.AutomodTermsUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelAdBreakBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelBan
import io.github.captnblubber.twitchkt.eventsub.model.ChannelBitsUse
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatClear
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatClearUserMessages
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatMessage
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatMessageDelete
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatNotification
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatSettingsUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatUserMessageHold
import io.github.captnblubber.twitchkt.eventsub.model.ChannelChatUserMessageUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelCheer
import io.github.captnblubber.twitchkt.eventsub.model.ChannelFollow
import io.github.captnblubber.twitchkt.eventsub.model.ChannelGoalBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelGoalEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelGoalProgress
import io.github.captnblubber.twitchkt.eventsub.model.ChannelModerate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelModeratorAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelModeratorRemove
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsAutomaticRedemptionAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsCustomRewardAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsCustomRewardRemove
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsCustomRewardUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsRedemptionAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsRedemptionUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelRaid
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSharedChatBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSharedChatEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSharedChatUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShieldModeBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShieldModeEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShoutoutCreate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShoutoutReceive
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscribe
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionGift
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionMessage
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSuspiciousUserMessage
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSuspiciousUserUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUnban
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUnbanRequestCreate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUnbanRequestResolve
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelVipAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelVipRemove
import io.github.captnblubber.twitchkt.eventsub.model.ChannelWarningAcknowledge
import io.github.captnblubber.twitchkt.eventsub.model.ChannelWarningSend
import io.github.captnblubber.twitchkt.eventsub.model.CharityCampaignProgress
import io.github.captnblubber.twitchkt.eventsub.model.CharityCampaignStart
import io.github.captnblubber.twitchkt.eventsub.model.CharityCampaignStop
import io.github.captnblubber.twitchkt.eventsub.model.CharityDonate
import io.github.captnblubber.twitchkt.eventsub.model.ConduitShardDisabled
import io.github.captnblubber.twitchkt.eventsub.model.ExtensionBitsTransactionCreate
import io.github.captnblubber.twitchkt.eventsub.model.HypeTrainBegin
import io.github.captnblubber.twitchkt.eventsub.model.HypeTrainEnd
import io.github.captnblubber.twitchkt.eventsub.model.HypeTrainProgress
import io.github.captnblubber.twitchkt.eventsub.model.PollBegin
import io.github.captnblubber.twitchkt.eventsub.model.PollEnd
import io.github.captnblubber.twitchkt.eventsub.model.PollProgress
import io.github.captnblubber.twitchkt.eventsub.model.PredictionBegin
import io.github.captnblubber.twitchkt.eventsub.model.PredictionEnd
import io.github.captnblubber.twitchkt.eventsub.model.PredictionLock
import io.github.captnblubber.twitchkt.eventsub.model.PredictionProgress
import io.github.captnblubber.twitchkt.eventsub.model.StreamOffline
import io.github.captnblubber.twitchkt.eventsub.model.StreamOnline
import io.github.captnblubber.twitchkt.eventsub.model.TwitchEvent
import io.github.captnblubber.twitchkt.eventsub.model.UnknownEvent
import io.github.captnblubber.twitchkt.eventsub.model.UserAuthorizationGrant
import io.github.captnblubber.twitchkt.eventsub.model.UserAuthorizationRevoke
import io.github.captnblubber.twitchkt.eventsub.model.UserUpdate
import io.github.captnblubber.twitchkt.eventsub.protocol.EventSubFrame
import io.github.captnblubber.twitchkt.eventsub.protocol.EventSubMetadata
import io.github.captnblubber.twitchkt.eventsub.protocol.SessionPayload
import kotlin.time.Instant

internal class EventSubParser(
    private val json: Json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        },
) {
    fun parse(rawText: String): ParsedMessage {
        val frame = json.decodeFromString<EventSubFrame>(rawText)
        return route(frame)
    }

    private fun route(frame: EventSubFrame): ParsedMessage {
        val meta = frame.metadata
        val payload = frame.payload

        return when (meta.messageType) {
            "session_welcome" -> parseWelcome(payload)
            "session_keepalive" -> ParsedMessage.Keepalive
            "session_reconnect" -> parseReconnect(payload)
            "notification" -> parseNotification(meta, payload)
            "revocation" -> parseRevocation(meta, payload)
            else -> parseUnknown(meta, payload)
        }
    }

    private fun parseWelcome(payload: JsonObject): ParsedMessage.Welcome {
        val sessionJson =
            payload["session"]?.jsonObject
                ?: error("Missing 'session' field in welcome payload")
        val session = json.decodeFromJsonElement<SessionPayload>(sessionJson)
        return ParsedMessage.Welcome(session)
    }

    private fun parseReconnect(payload: JsonObject): ParsedMessage.Reconnect {
        val sessionJson =
            payload["session"]?.jsonObject
                ?: error("Missing 'session' field in reconnect payload")
        val session = json.decodeFromJsonElement<SessionPayload>(sessionJson)
        return ParsedMessage.Reconnect(session)
    }

    private fun parseNotification(
        meta: EventSubMetadata,
        payload: JsonObject,
    ): ParsedMessage.Notification {
        val eventJson =
            payload["event"]?.jsonObject
                ?: return ParsedMessage.Notification(unknownEvent(meta, payload))

        val event = parseEvent(meta, eventJson)
        return ParsedMessage.Notification(event)
    }

    private fun parseRevocation(
        meta: EventSubMetadata,
        payload: JsonObject,
    ): ParsedMessage.Revocation {
        val subscription =
            json.decodeFromJsonElement<SubscriptionPayload>(
                payload["subscription"]?.jsonObject
                    ?: error("Missing 'subscription' field in revocation payload"),
            )
        return ParsedMessage.Revocation(
            subscriptionType = meta.subscriptionType ?: subscription.type,
            status = subscription.status,
        )
    }

    private fun parseUnknown(
        meta: EventSubMetadata,
        payload: JsonObject,
    ): ParsedMessage.Notification = ParsedMessage.Notification(unknownEvent(meta, payload))

    private fun parseEvent(
        meta: EventSubMetadata,
        eventJson: JsonObject,
    ): TwitchEvent {
        val subType = meta.subscriptionType ?: ""
        val messageId = meta.messageId
        val timestamp = Instant.parse(meta.messageTimestamp)

        return when (subType) {
            "channel.follow" -> {
                parseChannelFollow(messageId, timestamp, subType, eventJson)
            }

            "channel.subscribe" -> {
                parseChannelSubscribe(messageId, timestamp, subType, eventJson)
            }

            "channel.subscription.gift" -> {
                parseChannelSubscriptionGift(messageId, timestamp, subType, eventJson)
            }

            "channel.subscription.message" -> {
                parseChannelSubscriptionMessage(messageId, timestamp, subType, eventJson)
            }

            "channel.subscription.end" -> {
                parseChannelSubscriptionEnd(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.notification" -> {
                parseChannelChatNotification(messageId, timestamp, subType, eventJson)
            }

            "channel.bits.use" -> {
                parseChannelBitsUse(messageId, timestamp, subType, eventJson)
            }

            "channel.raid" -> {
                parseChannelRaid(messageId, timestamp, subType, eventJson)
            }

            "channel.poll.begin" -> {
                parsePollBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.poll.progress" -> {
                parsePollProgress(messageId, timestamp, subType, eventJson)
            }

            "channel.poll.end" -> {
                parsePollEnd(messageId, timestamp, subType, eventJson)
            }

            "channel.channel_points_custom_reward_redemption.add" -> {
                parseChannelPointsRedemptionAdd(messageId, timestamp, subType, eventJson)
            }

            "channel.channel_points_custom_reward_redemption.update" -> {
                parseChannelPointsRedemptionUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.message" -> {
                parseChannelChatMessage(messageId, timestamp, subType, eventJson)
            }

            "channel.hype_train.begin" -> {
                parseHypeTrainBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.hype_train.progress" -> {
                parseHypeTrainProgress(messageId, timestamp, subType, eventJson)
            }

            "channel.hype_train.end" -> {
                parseHypeTrainEnd(messageId, timestamp, subType, eventJson)
            }

            "stream.online" -> {
                parseStreamOnline(messageId, timestamp, subType, eventJson)
            }

            "stream.offline" -> {
                parseStreamOffline(messageId, timestamp, subType, eventJson)
            }

            "channel.update" -> {
                parseChannelUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.ad_break.begin" -> {
                parseChannelAdBreakBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.ban" -> {
                parseChannelBan(messageId, timestamp, subType, eventJson)
            }

            "channel.unban" -> {
                parseChannelUnban(messageId, timestamp, subType, eventJson)
            }

            "channel.moderate" -> {
                parseChannelModerate(messageId, timestamp, subType, eventJson)
            }

            "channel.moderator.add" -> {
                parseChannelModeratorAdd(messageId, timestamp, subType, eventJson)
            }

            "channel.moderator.remove" -> {
                parseChannelModeratorRemove(messageId, timestamp, subType, eventJson)
            }

            "channel.unban_request.create" -> {
                parseChannelUnbanRequestCreate(messageId, timestamp, subType, eventJson)
            }

            "channel.unban_request.resolve" -> {
                parseChannelUnbanRequestResolve(messageId, timestamp, subType, eventJson)
            }

            "channel.suspicious_user.message" -> {
                parseChannelSuspiciousUserMessage(messageId, timestamp, subType, eventJson)
            }

            "channel.suspicious_user.update" -> {
                parseChannelSuspiciousUserUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.warning.acknowledge" -> {
                parseChannelWarningAcknowledge(messageId, timestamp, subType, eventJson)
            }

            "channel.warning.send" -> {
                parseChannelWarningSend(messageId, timestamp, subType, eventJson)
            }

            "channel.vip.add" -> {
                parseChannelVipAdd(messageId, timestamp, subType, eventJson)
            }

            "channel.vip.remove" -> {
                parseChannelVipRemove(messageId, timestamp, subType, eventJson)
            }

            "channel.shield_mode.begin" -> {
                parseChannelShieldModeBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.shield_mode.end" -> {
                parseChannelShieldModeEnd(messageId, timestamp, subType, eventJson)
            }

            "channel.cheer" -> {
                parseChannelCheer(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.clear" -> {
                parseChannelChatClear(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.clear_user_messages" -> {
                parseChannelChatClearUserMessages(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.message_delete" -> {
                parseChannelChatMessageDelete(messageId, timestamp, subType, eventJson)
            }

            "channel.chat_settings.update" -> {
                parseChannelChatSettingsUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.user_message_hold" -> {
                parseChannelChatUserMessageHold(messageId, timestamp, subType, eventJson)
            }

            "channel.chat.user_message_update" -> {
                parseChannelChatUserMessageUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.shared_chat.begin" -> {
                parseChannelSharedChatBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.shared_chat.update" -> {
                parseChannelSharedChatUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.shared_chat.end" -> {
                parseChannelSharedChatEnd(messageId, timestamp, subType, eventJson)
            }

            // — Channel Points, Predictions, Goals, Charity —

            "channel.channel_points_automatic_reward_redemption.add" -> {
                parseChannelPointsAutomaticRedemptionAdd(messageId, timestamp, subType, eventJson)
            }

            "channel.channel_points_custom_reward.add" -> {
                parseChannelPointsCustomRewardAdd(messageId, timestamp, subType, eventJson)
            }

            "channel.channel_points_custom_reward.update" -> {
                parseChannelPointsCustomRewardUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.channel_points_custom_reward.remove" -> {
                parseChannelPointsCustomRewardRemove(messageId, timestamp, subType, eventJson)
            }

            "channel.prediction.begin" -> {
                parsePredictionBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.prediction.progress" -> {
                parsePredictionProgress(messageId, timestamp, subType, eventJson)
            }

            "channel.prediction.lock" -> {
                parsePredictionLock(messageId, timestamp, subType, eventJson)
            }

            "channel.prediction.end" -> {
                parsePredictionEnd(messageId, timestamp, subType, eventJson)
            }

            "channel.goal.begin" -> {
                parseChannelGoalBegin(messageId, timestamp, subType, eventJson)
            }

            "channel.goal.progress" -> {
                parseChannelGoalProgress(messageId, timestamp, subType, eventJson)
            }

            "channel.goal.end" -> {
                parseChannelGoalEnd(messageId, timestamp, subType, eventJson)
            }

            "channel.charity_campaign.donate" -> {
                parseCharityDonate(messageId, timestamp, subType, eventJson)
            }

            "channel.charity_campaign.start" -> {
                parseCharityCampaignStart(messageId, timestamp, subType, eventJson)
            }

            "channel.charity_campaign.progress" -> {
                parseCharityCampaignProgress(messageId, timestamp, subType, eventJson)
            }

            "channel.charity_campaign.stop" -> {
                parseCharityCampaignStop(messageId, timestamp, subType, eventJson)
            }

            // — Automod, Shoutouts, User, System —

            "automod.message.hold" -> {
                parseAutomodMessageHold(messageId, timestamp, subType, eventJson)
            }

            "automod.message.update" -> {
                parseAutomodMessageUpdate(messageId, timestamp, subType, eventJson)
            }

            "automod.settings.update" -> {
                parseAutomodSettingsUpdate(messageId, timestamp, subType, eventJson)
            }

            "automod.terms.update" -> {
                parseAutomodTermsUpdate(messageId, timestamp, subType, eventJson)
            }

            "channel.shoutout.create" -> {
                parseChannelShoutoutCreate(messageId, timestamp, subType, eventJson)
            }

            "channel.shoutout.receive" -> {
                parseChannelShoutoutReceive(messageId, timestamp, subType, eventJson)
            }

            "user.authorization.grant" -> {
                parseUserAuthorizationGrant(messageId, timestamp, subType, eventJson)
            }

            "user.authorization.revoke" -> {
                parseUserAuthorizationRevoke(messageId, timestamp, subType, eventJson)
            }

            "user.update" -> {
                parseUserUpdate(messageId, timestamp, subType, eventJson)
            }

            "extension.bits_transaction.create" -> {
                parseExtensionBitsTransactionCreate(messageId, timestamp, subType, eventJson)
            }

            "conduit.shard.disabled" -> {
                parseConduitShardDisabled(messageId, timestamp, subType, eventJson)
            }

            else -> {
                unknownEvent(meta, eventJson)
            }
        }
    }

    private fun parseChannelFollow(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelFollow {
        val p = json.decodeFromJsonElement<ChannelFollowPayload>(eventJson)
        return ChannelFollow(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            followedAt = p.followedAt,
        )
    }

    private fun parseChannelSubscribe(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSubscribe {
        val p = json.decodeFromJsonElement<ChannelSubscribePayload>(eventJson)
        return ChannelSubscribe(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            tier = p.tier,
            isGift = p.isGift,
        )
    }

    private fun parseChannelSubscriptionGift(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSubscriptionGift {
        val p = json.decodeFromJsonElement<ChannelSubscriptionGiftPayload>(eventJson)
        return ChannelSubscriptionGift(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            total = p.total,
            tier = p.tier,
            cumulativeTotal = p.cumulativeTotal,
            isAnonymous = p.isAnonymous,
        )
    }

    private fun parseChannelSubscriptionMessage(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSubscriptionMessage {
        val p = json.decodeFromJsonElement<ChannelSubscriptionMessagePayload>(eventJson)
        return ChannelSubscriptionMessage(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            tier = p.tier,
            message = p.message,
            cumulativeMonths = p.cumulativeMonths,
            streakMonths = p.streakMonths,
            durationMonths = p.durationMonths,
        )
    }

    private fun parseChannelSubscriptionEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSubscriptionEnd {
        val p = json.decodeFromJsonElement<ChannelSubscriptionEndPayload>(eventJson)
        return ChannelSubscriptionEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            tier = p.tier,
            isGift = p.isGift,
        )
    }

    private fun parseChannelChatNotification(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatNotification {
        val p = json.decodeFromJsonElement<ChannelChatNotificationPayload>(eventJson)
        return ChannelChatNotification(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            chatterUserId = p.chatterUserId,
            chatterUserLogin = p.chatterUserLogin,
            chatterUserName = p.chatterUserName,
            noticeType = p.noticeType,
            message = p.message,
            sub = p.sub,
            resub = p.resub,
            subGift = p.subGift,
            communitySubGift = p.communitySubGift,
            giftPaidUpgrade = p.giftPaidUpgrade,
            primePaidUpgrade = p.primePaidUpgrade,
            payItForward = p.payItForward,
            raid = p.raid,
            unraid = p.unraid,
            announcement = p.announcement,
            bitsBadgeTier = p.bitsBadgeTier,
            charityDonation = p.charityDonation,
        )
    }

    private fun parseChannelBitsUse(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelBitsUse {
        val p = json.decodeFromJsonElement<ChannelBitsUsePayload>(eventJson)
        return ChannelBitsUse(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            bits = p.bits,
            type = p.type,
            powerUp = p.powerUp,
            message = p.message,
        )
    }

    private fun parseChannelRaid(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelRaid {
        val p = json.decodeFromJsonElement<ChannelRaidPayload>(eventJson)
        return ChannelRaid(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            fromBroadcasterUserId = p.fromBroadcasterUserId,
            fromBroadcasterUserLogin = p.fromBroadcasterUserLogin,
            fromBroadcasterUserName = p.fromBroadcasterUserName,
            toBroadcasterUserId = p.toBroadcasterUserId,
            toBroadcasterUserLogin = p.toBroadcasterUserLogin,
            toBroadcasterUserName = p.toBroadcasterUserName,
            viewers = p.viewers,
        )
    }

    private fun parsePollBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PollBegin {
        val p = json.decodeFromJsonElement<PollBeginPayload>(eventJson)
        return PollBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            choices = p.choices,
            bitsVoting = p.bitsVoting,
            channelPointsVoting = p.channelPointsVoting,
            startedAt = p.startedAt,
            endsAt = p.endsAt,
        )
    }

    private fun parsePollProgress(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PollProgress {
        val p = json.decodeFromJsonElement<PollProgressPayload>(eventJson)
        return PollProgress(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            choices = p.choices,
            bitsVoting = p.bitsVoting,
            channelPointsVoting = p.channelPointsVoting,
            startedAt = p.startedAt,
            endsAt = p.endsAt,
        )
    }

    private fun parsePollEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PollEnd {
        val p = json.decodeFromJsonElement<PollEndPayload>(eventJson)
        return PollEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            choices = p.choices,
            bitsVoting = p.bitsVoting,
            channelPointsVoting = p.channelPointsVoting,
            status = p.status,
            startedAt = p.startedAt,
            endedAt = p.endedAt,
        )
    }

    private fun parseChannelPointsRedemptionAdd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelPointsRedemptionAdd {
        val p = json.decodeFromJsonElement<ChannelPointsRedemptionPayload>(eventJson)
        return ChannelPointsRedemptionAdd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            userInput = p.userInput,
            status = p.status,
            reward = p.reward,
            redeemedAt = p.redeemedAt,
        )
    }

    private fun parseChannelPointsRedemptionUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelPointsRedemptionUpdate {
        val p = json.decodeFromJsonElement<ChannelPointsRedemptionPayload>(eventJson)
        return ChannelPointsRedemptionUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            userInput = p.userInput,
            status = p.status,
            reward = p.reward,
            redeemedAt = p.redeemedAt,
        )
    }

    private fun parseChannelChatMessage(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatMessage {
        val p = json.decodeFromJsonElement<ChannelChatMessagePayload>(eventJson)
        return ChannelChatMessage(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            chatterUserId = p.chatterUserId,
            chatterUserLogin = p.chatterUserLogin,
            chatterUserName = p.chatterUserName,
            chatMessageId = p.chatMessageId,
            message = p.message,
            color = p.color,
            badges = p.badges,
            messageType = p.messageType,
            cheer = p.cheer,
            reply = p.reply,
            channelPointsCustomRewardId = p.channelPointsCustomRewardId,
        )
    }

    private fun parseHypeTrainBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): HypeTrainBegin {
        val p = json.decodeFromJsonElement<HypeTrainBeginPayload>(eventJson)
        return HypeTrainBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            total = p.total,
            progress = p.progress,
            goal = p.goal,
            topContributions = p.topContributions,
            lastContribution = p.lastContribution,
            level = p.level,
            startedAt = p.startedAt,
            expiresAt = p.expiresAt,
            isGoldenKappaTrain = p.isGoldenKappaTrain,
            allTimeHighLevel = p.allTimeHighLevel,
            allTimeHighTotal = p.allTimeHighTotal,
        )
    }

    private fun parseHypeTrainProgress(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): HypeTrainProgress {
        val p = json.decodeFromJsonElement<HypeTrainProgressPayload>(eventJson)
        return HypeTrainProgress(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            total = p.total,
            progress = p.progress,
            goal = p.goal,
            topContributions = p.topContributions,
            lastContribution = p.lastContribution,
            level = p.level,
            startedAt = p.startedAt,
            expiresAt = p.expiresAt,
            isGoldenKappaTrain = p.isGoldenKappaTrain,
        )
    }

    private fun parseHypeTrainEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): HypeTrainEnd {
        val p = json.decodeFromJsonElement<HypeTrainEndPayload>(eventJson)
        return HypeTrainEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            level = p.level,
            total = p.total,
            topContributions = p.topContributions,
            startedAt = p.startedAt,
            endedAt = p.endedAt,
            cooldownEndsAt = p.cooldownEndsAt,
            isGoldenKappaTrain = p.isGoldenKappaTrain,
        )
    }

    private fun parseStreamOnline(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): StreamOnline {
        val p = json.decodeFromJsonElement<StreamOnlinePayload>(eventJson)
        return StreamOnline(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            type = p.type,
            startedAt = p.startedAt,
        )
    }

    private fun parseStreamOffline(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): StreamOffline {
        val p = json.decodeFromJsonElement<StreamOfflinePayload>(eventJson)
        return StreamOffline(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
        )
    }

    private fun parseChannelUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelUpdate {
        val p = json.decodeFromJsonElement<ChannelUpdatePayload>(eventJson)
        return ChannelUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            language = p.language,
            categoryId = p.categoryId,
            categoryName = p.categoryName,
            contentClassificationLabels = p.contentClassificationLabels,
        )
    }

    private fun parseChannelAdBreakBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelAdBreakBegin {
        val p = json.decodeFromJsonElement<ChannelAdBreakBeginPayload>(eventJson)
        return ChannelAdBreakBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            durationSeconds = p.durationSeconds,
            startedAt = p.startedAt,
            isAutomatic = p.isAutomatic,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            requesterUserId = p.requesterUserId,
            requesterUserLogin = p.requesterUserLogin,
            requesterUserName = p.requesterUserName,
        )
    }

    private fun parseChannelCheer(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelCheer {
        val p = json.decodeFromJsonElement<ChannelCheerPayload>(eventJson)
        return ChannelCheer(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            isAnonymous = p.isAnonymous,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            message = p.message,
            bits = p.bits,
        )
    }

    private fun parseChannelChatClear(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatClear {
        val p = json.decodeFromJsonElement<ChannelChatClearPayload>(eventJson)
        return ChannelChatClear(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
        )
    }

    private fun parseChannelChatClearUserMessages(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatClearUserMessages {
        val p = json.decodeFromJsonElement<ChannelChatClearUserMessagesPayload>(eventJson)
        return ChannelChatClearUserMessages(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            targetUserId = p.targetUserId,
            targetUserLogin = p.targetUserLogin,
            targetUserName = p.targetUserName,
        )
    }

    private fun parseChannelChatMessageDelete(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatMessageDelete {
        val p = json.decodeFromJsonElement<ChannelChatMessageDeletePayload>(eventJson)
        return ChannelChatMessageDelete(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            targetUserId = p.targetUserId,
            targetUserLogin = p.targetUserLogin,
            targetUserName = p.targetUserName,
            targetMessageId = p.targetMessageId,
        )
    }

    private fun parseChannelChatSettingsUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatSettingsUpdate {
        val p = json.decodeFromJsonElement<ChannelChatSettingsUpdatePayload>(eventJson)
        return ChannelChatSettingsUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            emoteMode = p.emoteMode,
            followerMode = p.followerMode,
            followerModeDurationMinutes = p.followerModeDurationMinutes,
            slowMode = p.slowMode,
            slowModeWaitTimeSeconds = p.slowModeWaitTimeSeconds,
            subscriberMode = p.subscriberMode,
            uniqueChatMode = p.uniqueChatMode,
        )
    }

    private fun parseChannelChatUserMessageHold(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatUserMessageHold {
        val p = json.decodeFromJsonElement<ChannelChatUserMessageHoldPayload>(eventJson)
        return ChannelChatUserMessageHold(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            chatMessageId = p.chatMessageId,
            message = p.message,
        )
    }

    private fun parseChannelChatUserMessageUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelChatUserMessageUpdate {
        val p = json.decodeFromJsonElement<ChannelChatUserMessageUpdatePayload>(eventJson)
        return ChannelChatUserMessageUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            status = p.status,
            chatMessageId = p.chatMessageId,
            message = p.message,
        )
    }

    private fun parseChannelSharedChatBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSharedChatBegin {
        val p = json.decodeFromJsonElement<ChannelSharedChatPayload>(eventJson)
        return ChannelSharedChatBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            sessionId = p.sessionId,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            hostBroadcasterUserId = p.hostBroadcasterUserId,
            hostBroadcasterUserLogin = p.hostBroadcasterUserLogin,
            hostBroadcasterUserName = p.hostBroadcasterUserName,
            participants = p.participants,
        )
    }

    private fun parseChannelSharedChatUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSharedChatUpdate {
        val p = json.decodeFromJsonElement<ChannelSharedChatPayload>(eventJson)
        return ChannelSharedChatUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            sessionId = p.sessionId,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            hostBroadcasterUserId = p.hostBroadcasterUserId,
            hostBroadcasterUserLogin = p.hostBroadcasterUserLogin,
            hostBroadcasterUserName = p.hostBroadcasterUserName,
            participants = p.participants,
        )
    }

    private fun parseChannelSharedChatEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSharedChatEnd {
        val p = json.decodeFromJsonElement<ChannelSharedChatPayload>(eventJson)
        return ChannelSharedChatEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            sessionId = p.sessionId,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            hostBroadcasterUserId = p.hostBroadcasterUserId,
            hostBroadcasterUserLogin = p.hostBroadcasterUserLogin,
            hostBroadcasterUserName = p.hostBroadcasterUserName,
        )
    }

    private fun parseChannelBan(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelBan {
        val p = json.decodeFromJsonElement<ChannelBanPayload>(eventJson)
        return ChannelBan(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            reason = p.reason,
            bannedAt = p.bannedAt,
            endsAt = p.endsAt,
            isPermanent = p.isPermanent,
        )
    }

    private fun parseChannelUnban(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelUnban {
        val p = json.decodeFromJsonElement<ChannelUnbanPayload>(eventJson)
        return ChannelUnban(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
        )
    }

    private fun parseChannelModerate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelModerate {
        val p = json.decodeFromJsonElement<ChannelModeratePayload>(eventJson)
        return ChannelModerate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            action = p.action,
            followers = p.followers,
            slow = p.slow,
            vip = p.vip,
            unvip = p.unvip,
            mod = p.mod,
            unmod = p.unmod,
            ban = p.ban,
            unban = p.unban,
            timeout = p.timeout,
            untimeout = p.untimeout,
            raid = p.raid,
            unraid = p.unraid,
            delete = p.delete,
            automodTerms = p.automodTerms,
            unmute = p.unmute,
            warn = p.warn,
            shared = p.shared,
        )
    }

    private fun parseChannelModeratorAdd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelModeratorAdd {
        val p = json.decodeFromJsonElement<ChannelModeratorPayload>(eventJson)
        return ChannelModeratorAdd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
        )
    }

    private fun parseChannelModeratorRemove(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelModeratorRemove {
        val p = json.decodeFromJsonElement<ChannelModeratorPayload>(eventJson)
        return ChannelModeratorRemove(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
        )
    }

    private fun parseChannelUnbanRequestCreate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelUnbanRequestCreate {
        val p = json.decodeFromJsonElement<ChannelUnbanRequestCreatePayload>(eventJson)
        return ChannelUnbanRequestCreate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            text = p.text,
            createdAt = p.createdAt,
        )
    }

    private fun parseChannelUnbanRequestResolve(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelUnbanRequestResolve {
        val p = json.decodeFromJsonElement<ChannelUnbanRequestResolvePayload>(eventJson)
        return ChannelUnbanRequestResolve(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            resolutionText = p.resolutionText,
            status = p.status,
        )
    }

    private fun parseChannelSuspiciousUserMessage(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSuspiciousUserMessage {
        val p = json.decodeFromJsonElement<ChannelSuspiciousUserMessagePayload>(eventJson)
        return ChannelSuspiciousUserMessage(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            lowTrustStatus = p.lowTrustStatus,
            sharedBanChannelIds = p.sharedBanChannelIds,
            types = p.types,
            bannedChannelCount = p.bannedChannelCount,
            message = p.message,
        )
    }

    private fun parseChannelSuspiciousUserUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelSuspiciousUserUpdate {
        val p = json.decodeFromJsonElement<ChannelSuspiciousUserUpdatePayload>(eventJson)
        return ChannelSuspiciousUserUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            lowTrustStatus = p.lowTrustStatus,
        )
    }

    private fun parseChannelWarningAcknowledge(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelWarningAcknowledge {
        val p = json.decodeFromJsonElement<ChannelWarningAcknowledgePayload>(eventJson)
        return ChannelWarningAcknowledge(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
        )
    }

    private fun parseChannelWarningSend(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelWarningSend {
        val p = json.decodeFromJsonElement<ChannelWarningSendPayload>(eventJson)
        return ChannelWarningSend(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            reason = p.reason,
            chatRulesCited = p.chatRulesCited,
        )
    }

    private fun parseChannelVipAdd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelVipAdd {
        val p = json.decodeFromJsonElement<ChannelVipPayload>(eventJson)
        return ChannelVipAdd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
        )
    }

    private fun parseChannelVipRemove(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelVipRemove {
        val p = json.decodeFromJsonElement<ChannelVipPayload>(eventJson)
        return ChannelVipRemove(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
        )
    }

    private fun parseChannelShieldModeBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelShieldModeBegin {
        val p = json.decodeFromJsonElement<ChannelShieldModeBeginPayload>(eventJson)
        return ChannelShieldModeBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            startedAt = p.startedAt,
        )
    }

    private fun parseChannelShieldModeEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelShieldModeEnd {
        val p = json.decodeFromJsonElement<ChannelShieldModeEndPayload>(eventJson)
        return ChannelShieldModeEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            endedAt = p.endedAt,
        )
    }

    // — Channel Points, Predictions, Goals, Charity —

    private fun parseChannelPointsAutomaticRedemptionAdd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelPointsAutomaticRedemptionAdd {
        val p = json.decodeFromJsonElement<ChannelPointsAutomaticRedemptionAddPayload>(eventJson)
        return ChannelPointsAutomaticRedemptionAdd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            id = p.id,
            reward = p.reward,
            message = p.message,
            userInput = p.userInput,
            redeemedAt = p.redeemedAt,
        )
    }

    private fun parseChannelPointsCustomRewardAdd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelPointsCustomRewardAdd {
        val p = json.decodeFromJsonElement<ChannelPointsCustomRewardPayload>(eventJson)
        return ChannelPointsCustomRewardAdd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            isEnabled = p.isEnabled,
            isPaused = p.isPaused,
            isInStock = p.isInStock,
            title = p.title,
            cost = p.cost,
            prompt = p.prompt,
            isUserInputRequired = p.isUserInputRequired,
            shouldRedemptionsSkipRequestQueue = p.shouldRedemptionsSkipRequestQueue,
            maxPerStream = p.maxPerStream,
            maxPerUserPerStream = p.maxPerUserPerStream,
            backgroundColor = p.backgroundColor,
            image = p.image,
            defaultImage = p.defaultImage,
            globalCooldown = p.globalCooldown,
            cooldownExpiresAt = p.cooldownExpiresAt,
            redemptionsRedeemedCurrentStream = p.redemptionsRedeemedCurrentStream,
        )
    }

    private fun parseChannelPointsCustomRewardUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelPointsCustomRewardUpdate {
        val p = json.decodeFromJsonElement<ChannelPointsCustomRewardPayload>(eventJson)
        return ChannelPointsCustomRewardUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            isEnabled = p.isEnabled,
            isPaused = p.isPaused,
            isInStock = p.isInStock,
            title = p.title,
            cost = p.cost,
            prompt = p.prompt,
            isUserInputRequired = p.isUserInputRequired,
            shouldRedemptionsSkipRequestQueue = p.shouldRedemptionsSkipRequestQueue,
            maxPerStream = p.maxPerStream,
            maxPerUserPerStream = p.maxPerUserPerStream,
            backgroundColor = p.backgroundColor,
            image = p.image,
            defaultImage = p.defaultImage,
            globalCooldown = p.globalCooldown,
            cooldownExpiresAt = p.cooldownExpiresAt,
            redemptionsRedeemedCurrentStream = p.redemptionsRedeemedCurrentStream,
        )
    }

    private fun parseChannelPointsCustomRewardRemove(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelPointsCustomRewardRemove {
        val p = json.decodeFromJsonElement<ChannelPointsCustomRewardPayload>(eventJson)
        return ChannelPointsCustomRewardRemove(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            isEnabled = p.isEnabled,
            isPaused = p.isPaused,
            isInStock = p.isInStock,
            title = p.title,
            cost = p.cost,
            prompt = p.prompt,
            isUserInputRequired = p.isUserInputRequired,
            shouldRedemptionsSkipRequestQueue = p.shouldRedemptionsSkipRequestQueue,
            maxPerStream = p.maxPerStream,
            maxPerUserPerStream = p.maxPerUserPerStream,
            backgroundColor = p.backgroundColor,
            image = p.image,
            defaultImage = p.defaultImage,
            globalCooldown = p.globalCooldown,
            cooldownExpiresAt = p.cooldownExpiresAt,
            redemptionsRedeemedCurrentStream = p.redemptionsRedeemedCurrentStream,
        )
    }

    private fun parsePredictionBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PredictionBegin {
        val p = json.decodeFromJsonElement<PredictionBeginPayload>(eventJson)
        return PredictionBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            outcomes = p.outcomes,
            startedAt = p.startedAt,
            locksAt = p.locksAt,
        )
    }

    private fun parsePredictionProgress(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PredictionProgress {
        val p = json.decodeFromJsonElement<PredictionBeginPayload>(eventJson)
        return PredictionProgress(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            outcomes = p.outcomes,
            startedAt = p.startedAt,
            locksAt = p.locksAt,
        )
    }

    private fun parsePredictionLock(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PredictionLock {
        val p = json.decodeFromJsonElement<PredictionLockPayload>(eventJson)
        return PredictionLock(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            outcomes = p.outcomes,
            startedAt = p.startedAt,
            lockedAt = p.lockedAt,
        )
    }

    private fun parsePredictionEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): PredictionEnd {
        val p = json.decodeFromJsonElement<PredictionEndPayload>(eventJson)
        return PredictionEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            title = p.title,
            winningOutcomeId = p.winningOutcomeId,
            outcomes = p.outcomes,
            status = p.status,
            startedAt = p.startedAt,
            endedAt = p.endedAt,
        )
    }

    private fun parseChannelGoalBegin(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelGoalBegin {
        val p = json.decodeFromJsonElement<ChannelGoalPayload>(eventJson)
        return ChannelGoalBegin(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            type = p.type,
            description = p.description,
            currentAmount = p.currentAmount,
            targetAmount = p.targetAmount,
            startedAt = p.startedAt,
        )
    }

    private fun parseChannelGoalProgress(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelGoalProgress {
        val p = json.decodeFromJsonElement<ChannelGoalPayload>(eventJson)
        return ChannelGoalProgress(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            type = p.type,
            description = p.description,
            currentAmount = p.currentAmount,
            targetAmount = p.targetAmount,
            startedAt = p.startedAt,
        )
    }

    private fun parseChannelGoalEnd(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelGoalEnd {
        val p = json.decodeFromJsonElement<ChannelGoalEndPayload>(eventJson)
        return ChannelGoalEnd(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            type = p.type,
            description = p.description,
            isAchieved = p.isAchieved,
            currentAmount = p.currentAmount,
            targetAmount = p.targetAmount,
            startedAt = p.startedAt,
            endedAt = p.endedAt,
        )
    }

    private fun parseCharityDonate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): CharityDonate {
        val p = json.decodeFromJsonElement<CharityDonatePayload>(eventJson)
        return CharityDonate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            campaignId = p.campaignId,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            charityName = p.charityName,
            charityDescription = p.charityDescription,
            charityLogo = p.charityLogo,
            charityWebsite = p.charityWebsite,
            amount = p.amount,
        )
    }

    private fun parseCharityCampaignStart(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): CharityCampaignStart {
        val p = json.decodeFromJsonElement<CharityCampaignPayload>(eventJson)
        return CharityCampaignStart(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            charityName = p.charityName,
            charityDescription = p.charityDescription,
            charityLogo = p.charityLogo,
            charityWebsite = p.charityWebsite,
            currentAmount = p.currentAmount,
            targetAmount = p.targetAmount,
            startedAt = p.startedAt ?: timestamp,
        )
    }

    private fun parseCharityCampaignProgress(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): CharityCampaignProgress {
        val p = json.decodeFromJsonElement<CharityCampaignPayload>(eventJson)
        return CharityCampaignProgress(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            charityName = p.charityName,
            charityDescription = p.charityDescription,
            charityLogo = p.charityLogo,
            charityWebsite = p.charityWebsite,
            currentAmount = p.currentAmount,
            targetAmount = p.targetAmount,
        )
    }

    private fun parseCharityCampaignStop(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): CharityCampaignStop {
        val p = json.decodeFromJsonElement<CharityCampaignPayload>(eventJson)
        return CharityCampaignStop(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            charityName = p.charityName,
            charityDescription = p.charityDescription,
            charityLogo = p.charityLogo,
            charityWebsite = p.charityWebsite,
            currentAmount = p.currentAmount,
            targetAmount = p.targetAmount,
            stoppedAt = p.stoppedAt ?: timestamp,
        )
    }

    // — Automod, Shoutouts, User, System —

    private fun parseAutomodMessageHold(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): AutomodMessageHold {
        val p = json.decodeFromJsonElement<AutomodMessageHoldPayload>(eventJson)
        return AutomodMessageHold(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            chatMessageId = p.chatMessageId,
            message = p.message,
            category = p.category,
            level = p.level,
            heldAt = p.heldAt,
            fragments = p.message.fragments,
        )
    }

    private fun parseAutomodMessageUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): AutomodMessageUpdate {
        val p = json.decodeFromJsonElement<AutomodMessageUpdatePayload>(eventJson)
        return AutomodMessageUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            chatMessageId = p.chatMessageId,
            message = p.message,
            category = p.category,
            level = p.level,
            status = p.status,
            heldAt = p.heldAt,
            fragments = p.message.fragments,
        )
    }

    private fun parseAutomodSettingsUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): AutomodSettingsUpdate {
        val p = json.decodeFromJsonElement<AutomodSettingsUpdatePayload>(eventJson)
        return AutomodSettingsUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            overallLevel = p.overallLevel,
            disability = p.disability,
            aggression = p.aggression,
            sexualitySexOrGender = p.sexualitySexOrGender,
            misogyny = p.misogyny,
            bullying = p.bullying,
            swearing = p.swearing,
            raceEthnicityOrReligion = p.raceEthnicityOrReligion,
            sexBasedTerms = p.sexBasedTerms,
        )
    }

    private fun parseAutomodTermsUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): AutomodTermsUpdate {
        val p = json.decodeFromJsonElement<AutomodTermsUpdatePayload>(eventJson)
        return AutomodTermsUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            action = p.action,
            fromAutomod = p.fromAutomod,
            terms = p.terms,
        )
    }

    private fun parseChannelShoutoutCreate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelShoutoutCreate {
        val p = json.decodeFromJsonElement<ChannelShoutoutCreatePayload>(eventJson)
        return ChannelShoutoutCreate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            moderatorUserId = p.moderatorUserId,
            moderatorUserLogin = p.moderatorUserLogin,
            moderatorUserName = p.moderatorUserName,
            toBroadcasterUserId = p.toBroadcasterUserId,
            toBroadcasterUserLogin = p.toBroadcasterUserLogin,
            toBroadcasterUserName = p.toBroadcasterUserName,
            viewerCount = p.viewerCount,
            startedAt = p.startedAt,
            cooldownEndsAt = p.cooldownEndsAt,
            targetCooldownEndsAt = p.targetCooldownEndsAt,
        )
    }

    private fun parseChannelShoutoutReceive(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ChannelShoutoutReceive {
        val p = json.decodeFromJsonElement<ChannelShoutoutReceivePayload>(eventJson)
        return ChannelShoutoutReceive(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            fromBroadcasterUserId = p.fromBroadcasterUserId,
            fromBroadcasterUserLogin = p.fromBroadcasterUserLogin,
            fromBroadcasterUserName = p.fromBroadcasterUserName,
            viewerCount = p.viewerCount,
            startedAt = p.startedAt,
        )
    }

    private fun parseUserAuthorizationGrant(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): UserAuthorizationGrant {
        val p = json.decodeFromJsonElement<UserAuthorizationPayload>(eventJson)
        return UserAuthorizationGrant(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            clientId = p.clientId,
            userId = p.userId ?: "",
            userLogin = p.userLogin ?: "",
            userName = p.userName ?: "",
        )
    }

    private fun parseUserAuthorizationRevoke(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): UserAuthorizationRevoke {
        val p = json.decodeFromJsonElement<UserAuthorizationPayload>(eventJson)
        return UserAuthorizationRevoke(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            clientId = p.clientId,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
        )
    }

    private fun parseUserUpdate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): UserUpdate {
        val p = json.decodeFromJsonElement<UserUpdatePayload>(eventJson)
        return UserUpdate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            email = p.email,
            emailVerified = p.emailVerified,
            description = p.description,
        )
    }

    private fun parseExtensionBitsTransactionCreate(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ExtensionBitsTransactionCreate {
        val p = json.decodeFromJsonElement<ExtensionBitsTransactionCreatePayload>(eventJson)
        return ExtensionBitsTransactionCreate(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            extensionClientId = p.extensionClientId,
            id = p.id,
            broadcasterUserId = p.broadcasterUserId,
            broadcasterUserLogin = p.broadcasterUserLogin,
            broadcasterUserName = p.broadcasterUserName,
            userId = p.userId,
            userLogin = p.userLogin,
            userName = p.userName,
            product = p.product,
        )
    }

    private fun parseConduitShardDisabled(
        messageId: String,
        timestamp: Instant,
        subType: String,
        eventJson: JsonObject,
    ): ConduitShardDisabled {
        val p = json.decodeFromJsonElement<ConduitShardDisabledPayload>(eventJson)
        return ConduitShardDisabled(
            subscriptionType = subType,
            messageId = messageId,
            timestamp = timestamp,
            conduitId = p.conduitId,
            shardId = p.shardId,
            status = p.status,
            transport = p.transport,
        )
    }

    private fun unknownEvent(
        meta: EventSubMetadata,
        rawPayload: JsonObject,
    ): UnknownEvent =
        UnknownEvent(
            subscriptionType = meta.subscriptionType ?: "unknown",
            messageId = meta.messageId,
            timestamp = Instant.parse(meta.messageTimestamp),
            rawPayload = rawPayload,
        )
}
