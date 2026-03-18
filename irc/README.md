# twitchkt-irc (Deprecated)

> **Twitch IRC is deprecated.** This client exists solely because EventSub does not yet cover all event types — notably viewer milestones (watch streaks). Once Twitch adds full EventSub coverage, this module will be removed. See: [Twitch IRC Migration Guide](https://dev.twitch.tv/docs/chat/irc-migration/)

All types are annotated `@Deprecated` so IDEs flag every usage site.

## Usage

```kotlin
@Suppress("DEPRECATION")
val irc = TwitchIrc(httpClient, config)
irc.join("channelname")
irc.connect(scope)

irc.messages.collect { msg ->
    when (msg) {
        is IrcMessage.UserNotice.WatchStreak ->
            println("${msg.displayName} watched ${msg.streakMonths} streams!")
        is IrcMessage.UserNotice.Sub ->
            println("${msg.displayName} subscribed at ${msg.tier}")
        is IrcMessage.RoomState ->
            println("Slow mode: ${msg.slow}")
        else -> { }
    }
}
```

## Why IRC?

For chat messages and sending, use EventSub (`channel.chat.message`) and Helix (`chat.sendMessage()`). IRC is only needed for event types Twitch has not yet migrated to EventSub. The primary use case is **watch streaks** (`viewer-milestone` with `watch-streak` category), which have no EventSub equivalent as of March 2026.

## Message Types

| IRC Command | Model | Key Fields |
|---|---|---|
| `PRIVMSG` | `IrcMessage.PrivMsg` | userId, userLogin, displayName, message |
| `USERNOTICE` (sub) | `IrcMessage.UserNotice.Sub` | tier, isGift |
| `USERNOTICE` (resub) | `IrcMessage.UserNotice.Resub` | tier, cumulativeMonths, streakMonths |
| `USERNOTICE` (subgift) | `IrcMessage.UserNotice.SubGift` | tier, recipientLogin, recipientDisplayName |
| `USERNOTICE` (raid) | `IrcMessage.UserNotice.Raid` | viewerCount |
| `USERNOTICE` (viewer-milestone) | `IrcMessage.UserNotice.WatchStreak` | streakMonths, userMessage |
| `USERNOTICE` (other) | `IrcMessage.UserNotice.Unknown` | msgId |
| `ROOMSTATE` | `IrcMessage.RoomState` | emoteOnly, followersOnly, slow, subsOnly |
| `CLEARCHAT` | `IrcMessage.ClearChat` | targetUserId, duration |
| `CLEARMSG` | `IrcMessage.ClearMsg` | targetMessageId, login |
| `NOTICE` | `IrcMessage.Notice` | msgId, message |

## Dependencies

- `twitchkt-core` (API) — config, connection state
- Ktor client (WebSocket)
- `kotlinx-coroutines-core`

## Structure

```
irc/src/
├── commonMain/kotlin/io/github/captnblubber/twitchkt/irc/
│   ├── TwitchIrc.kt
│   ├── IrcMessage.kt
│   └── internal/
│       └── IrcParser.kt
└── commonTest/kotlin/io/github/captnblubber/twitchkt/irc/
    ├── TwitchIrcTest.kt
    └── internal/
        └── IrcParserTest.kt
```
