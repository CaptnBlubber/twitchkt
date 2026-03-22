# twitchkt-eventsub

EventSub WebSocket client for Twitch. Manages a single WebSocket connection with automatic reconnection, keepalive monitoring, and 73 typed event models.

## Setup

```kotlin
val eventSub = TwitchEventSub(httpClient, config, helix.eventSub)
```

Requires a `TwitchHelix` instance for subscription management (uses `helix.eventSub` to register EventSub subscriptions via the Helix API).

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

The `sessionId` StateFlow remains public for advanced use cases requiring manual subscription management via `helix.eventSub.create()`.

### Disconnect

```kotlin
eventSub.disconnect()
```

### Reconnect Behavior

- **Server-initiated**: Twitch sends `session_reconnect` — client connects to the new URL, waits for welcome, then closes the old session. No events dropped.
- **Unexpected disconnects**: Exponential backoff starting at 1 second, capped at 30 seconds.
- **Keepalive timeout**: Tracked per the Twitch-specified interval plus a 5-second buffer.

## Event Types

73 subscription types are supported, all arriving as typed `TwitchEvent` subclasses on the `events` flow. Categories include:

- **Core**: follow, subscribe, resub, gift sub, raid, channel update, ad break, stream online/offline
- **Chat**: chat message, notification, cheer, chat clear, message delete, settings update, shared chat
- **Moderation**: ban, unban, moderate, moderator add/remove, unban requests, suspicious users, warnings, VIP add/remove, shield mode, shoutout
- **Automod**: message hold/update, settings update, terms update
- **Channel Points**: reward add/update/remove, redemption add/update, automatic redemption
- **Polls & Predictions**: poll begin/progress/end, prediction begin/progress/lock/end
- **Hype Train & Goals**: hype train begin/progress/end, goal begin/progress/end
- **Charity**: donate, campaign start/progress/stop
- **User & System**: authorization grant/revoke, user update, extension bits transaction, conduit shard disabled

Unknown or future event types arrive as `UnknownEvent` preserving the raw payload — the parser never crashes on new types.

For full field documentation on each event type, see the [API documentation](https://captnblubber.github.io/twitchkt/).

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

