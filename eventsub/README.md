# twitchkt-eventsub

EventSub WebSocket client for Twitch. Manages a single WebSocket connection with automatic reconnection, keepalive monitoring, and 73 typed event models.

## Setup

```kotlin
val eventSub = TwitchEventSub(httpClient, config, helix.subscriptions)
```

Requires a `TwitchHelix` instance for subscription management (uses `helix.subscriptions` to register EventSub subscriptions via the Helix API).

## Usage

### Connect and Observe

```kotlin
eventSub.subscribe(EventSubSubscriptionType.ChannelChatMessage(broadcasterId, botUserId))
eventSub.subscribe(EventSubSubscriptionType.StreamOnline(broadcasterId))

eventSub.connect(lifecycleScope)

eventSub.connectionState.collect { state ->
    when (state) {
        ConnectionState.CONNECTED -> println("Ready")
        ConnectionState.RECONNECTING -> println("Reconnecting...")
        else -> { }
    }
}

eventSub.events.collect { event ->
    when (event) {
        is ChannelChatMessage -> handleChat(event)
        is HypeTrainBegin -> startHypeAnimation(event)
        is StreamOnline -> notifyStreamStart(event)
        is UnknownEvent -> println("Unhandled type: ${event.subscriptionType}")
        else -> { }
    }
}
```

Subscriptions registered before `connect()` are created when the welcome message arrives. Subscriptions added after connection are registered immediately. On error-recovery reconnects, all subscriptions are re-registered. Server-initiated reconnects carry subscriptions over without re-registration.

The `sessionId` StateFlow remains public for advanced use cases requiring manual subscription management via `helix.subscriptions.createEventSub()`.

### Disconnect

```kotlin
eventSub.disconnect()
```

### Reconnect Behavior

- **Server-initiated**: Twitch sends `session_reconnect` — client connects to the new URL, waits for welcome, then closes the old session. No events dropped.
- **Unexpected disconnects**: Exponential backoff starting at 1 second, capped at 30 seconds.
- **Keepalive timeout**: Tracked per the Twitch-specified interval plus a 5-second buffer.

## Event Types

All 73 EventSub subscription types are supported with typed data classes implementing `TwitchEvent`:

### Core Events

| Event Type | Class | Subscription Type |
|---|---|---|
| Channel follow | `ChannelFollow` | `channel.follow` v2 |
| Channel subscribe | `ChannelSubscribe` | `channel.subscribe` v1 |
| Subscription gift | `ChannelSubscriptionGift` | `channel.subscription.gift` v1 |
| Subscription message (resub) | `ChannelSubscriptionMessage` | `channel.subscription.message` v1 |
| Subscription end | `ChannelSubscriptionEnd` | `channel.subscription.end` v1 |
| Chat message | `ChannelChatMessage` | `channel.chat.message` v1 |
| Chat notification | `ChannelChatNotification` | `channel.chat.notification` v1 |
| Bits use | `ChannelBitsUse` | `channel.bits.use` v1 |
| Raid | `ChannelRaid` | `channel.raid` v1 |
| Channel update | `ChannelUpdate` | `channel.update` v2 |
| Ad break begin | `ChannelAdBreakBegin` | `channel.ad_break.begin` v1 |
| Stream online | `StreamOnline` | `stream.online` v1 |
| Stream offline | `StreamOffline` | `stream.offline` v1 |

### Chat Events

| Event Type | Class | Subscription Type |
|---|---|---|
| Cheer | `ChannelCheer` | `channel.cheer` v1 |
| Chat clear | `ChannelChatClear` | `channel.chat.clear` v1 |
| Clip created | `ChannelClipCreated` | `channel.clip.created` v1 |
| Chat clear user messages | `ChannelChatClearUserMessages` | `channel.chat.clear_user_messages` v1 |
| Chat message delete | `ChannelChatMessageDelete` | `channel.chat.message_delete` v1 |
| Chat settings update | `ChannelChatSettingsUpdate` | `channel.chat_settings.update` v1 |
| Chat user message hold | `ChannelChatUserMessageHold` | `channel.chat.user_message_hold` v1 |
| Chat user message update | `ChannelChatUserMessageUpdate` | `channel.chat.user_message_update` v1 |
| Shared chat begin | `ChannelSharedChatBegin` | `channel.shared_chat.begin` v1 |
| Shared chat update | `ChannelSharedChatUpdate` | `channel.shared_chat.update` v1 |
| Shared chat end | `ChannelSharedChatEnd` | `channel.shared_chat.end` v1 |

### Moderation & Safety

| Event Type | Class | Subscription Type |
|---|---|---|
| Ban | `ChannelBan` | `channel.ban` v1 |
| Unban | `ChannelUnban` | `channel.unban` v1 |
| Moderate | `ChannelModerate` | `channel.moderate` v2 |
| Moderator add | `ChannelModeratorAdd` | `channel.moderator.add` v1 |
| Moderator remove | `ChannelModeratorRemove` | `channel.moderator.remove` v1 |
| Unban request create | `ChannelUnbanRequestCreate` | `channel.unban_request.create` v1 |
| Unban request resolve | `ChannelUnbanRequestResolve` | `channel.unban_request.resolve` v1 |
| Suspicious user message | `ChannelSuspiciousUserMessage` | `channel.suspicious_user.message` v1 |
| Suspicious user update | `ChannelSuspiciousUserUpdate` | `channel.suspicious_user.update` v1 |
| Warning acknowledge | `ChannelWarningAcknowledge` | `channel.warning.acknowledge` v1 |
| Warning send | `ChannelWarningSend` | `channel.warning.send` v1 |
| VIP add | `ChannelVipAdd` | `channel.vip.add` v1 |
| VIP remove | `ChannelVipRemove` | `channel.vip.remove` v1 |
| Shield mode begin | `ChannelShieldModeBegin` | `channel.shield_mode.begin` v1 |
| Shield mode end | `ChannelShieldModeEnd` | `channel.shield_mode.end` v1 |
| Shoutout create | `ChannelShoutoutCreate` | `channel.shoutout.create` v1 |
| Shoutout receive | `ChannelShoutoutReceive` | `channel.shoutout.receive` v1 |

### Automod

| Event Type | Class | Subscription Type |
|---|---|---|
| Automod message hold | `AutomodMessageHold` | `automod.message.hold` v2 |
| Automod message update | `AutomodMessageUpdate` | `automod.message.update` v2 |
| Automod settings update | `AutomodSettingsUpdate` | `automod.settings.update` v2 |
| Automod terms update | `AutomodTermsUpdate` | `automod.terms.update` v1 |

### Channel Points & Rewards

| Event Type | Class | Subscription Type |
|---|---|---|
| Points auto reward redemption | `ChannelPointsAutomaticRedemptionAdd` | `channel.channel_points_automatic_reward_redemption.add` v2 |
| Points redemption add | `ChannelPointsRedemptionAdd` | `channel.channel_points_custom_reward_redemption.add` v1 |
| Points redemption update | `ChannelPointsRedemptionUpdate` | `channel.channel_points_custom_reward_redemption.update` v1 |
| Custom reward add | `ChannelPointsCustomRewardAdd` | `channel.channel_points_custom_reward.add` v1 |
| Custom reward update | `ChannelPointsCustomRewardUpdate` | `channel.channel_points_custom_reward.update` v1 |
| Custom reward remove | `ChannelPointsCustomRewardRemove` | `channel.channel_points_custom_reward.remove` v1 |

### Polls & Predictions

| Event Type | Class | Subscription Type |
|---|---|---|
| Poll begin | `PollBegin` | `channel.poll.begin` v1 |
| Poll progress | `PollProgress` | `channel.poll.progress` v1 |
| Poll end | `PollEnd` | `channel.poll.end` v1 |
| Prediction begin | `PredictionBegin` | `channel.prediction.begin` v1 |
| Prediction progress | `PredictionProgress` | `channel.prediction.progress` v1 |
| Prediction lock | `PredictionLock` | `channel.prediction.lock` v1 |
| Prediction end | `PredictionEnd` | `channel.prediction.end` v1 |

### Hype Train & Goals

| Event Type | Class | Subscription Type |
|---|---|---|
| Hype train begin | `HypeTrainBegin` | `channel.hype_train.begin` v1 |
| Hype train progress | `HypeTrainProgress` | `channel.hype_train.progress` v1 |
| Hype train end | `HypeTrainEnd` | `channel.hype_train.end` v1 |
| Goal begin | `ChannelGoalBegin` | `channel.goal.begin` v1 |
| Goal progress | `ChannelGoalProgress` | `channel.goal.progress` v1 |
| Goal end | `ChannelGoalEnd` | `channel.goal.end` v1 |

### Charity

| Event Type | Class | Subscription Type |
|---|---|---|
| Charity donate | `CharityDonate` | `channel.charity_campaign.donate` v1 |
| Charity campaign start | `CharityCampaignStart` | `channel.charity_campaign.start` v1 |
| Charity campaign progress | `CharityCampaignProgress` | `channel.charity_campaign.progress` v1 |
| Charity campaign stop | `CharityCampaignStop` | `channel.charity_campaign.stop` v1 |

### User & System

| Event Type | Class | Subscription Type |
|---|---|---|
| User authorization grant | `UserAuthorizationGrant` | `user.authorization.grant` v1 |
| User authorization revoke | `UserAuthorizationRevoke` | `user.authorization.revoke` v1 |
| User update | `UserUpdate` | `user.update` v1 |
| Extension bits transaction | `ExtensionBitsTransactionCreate` | `extension.bits_transaction.create` v1 |
| Conduit shard disabled | `ConduitShardDisabled` | `conduit.shard.disabled` v1 |
| Unknown / future types | `UnknownEvent` | *(any unrecognised type)* |

`UnknownEvent` preserves the raw `JsonObject` payload so future event types do not crash the parser.

## Integration Testing

End-to-end tests run against Twitch CLI mock servers. Excluded from normal test runs.

**Prerequisites:** `twitch` CLI and `sqlite3` on `$PATH`, ports 8080 and 8081 free.

Start the mock API server, then run:

```bash
./gradlew :eventsub:jvmTest -DintegrationTest=true
```

| Test Class | Scenarios |
|---|---|
| `WebSocketSmokeTest` | Raw CIO WebSocket connects and receives welcome message |
| `EventSubIntegrationTest` | Connect/disconnect lifecycle, 43 data-driven event type tests |

Guard mechanism: Excluded via Gradle `exclude("**/integration/**")` and Kotest `enabledIf` checking the `integrationTest` system property.

