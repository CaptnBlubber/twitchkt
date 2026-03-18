# twitchkt-helix

Twitch Helix REST API client with typed suspend functions, `Flow`-based pagination, and proactive scope validation. Covers 25 resource groups.

## Setup

```kotlin
val config = TwitchKtConfig(
    clientId = "your_client_id",
    tokenProvider = { myTokenStore.getAccessToken() },
)

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) { json() }
}

val helix = TwitchHelix(httpClient, config)
```

## Usage

`TwitchHelix` exposes resource groups as properties. All methods are `suspend` functions (or return `Flow` for paginated endpoints).

### Users

```kotlin
val users = helix.users.getUsers(logins = listOf("captnblubber"))
val user = users.first()
println("${user.displayName} — ${user.id}")
```

### Channels

```kotlin
val info = helix.channels.getInformation(broadcasterId = "123456")

helix.channels.update(
    broadcasterId = "123456",
    request = UpdateChannelRequest(title = "New stream title", gameId = "509658"),
)
```

### Streams

```kotlin
val live = helix.streams.getStreams(userLogins = listOf("captnblubber"))
```

### Chat

```kotlin
helix.chat.sendMessage(SendChatMessageRequest(broadcasterId = "123456", senderId = "789", message = "Hello!"))
val globalBadges = helix.chat.getGlobalBadges()
val channelBadges = helix.chat.getChannelBadges(broadcasterId = "123456")
```

### Followers

```kotlin
helix.followers.list(broadcasterId = "123456").collect { follower ->
    println(follower.userLogin)
}
val count = helix.followers.getTotal(broadcasterId = "123456")
```

### Subscriptions

```kotlin
helix.subscriptions.list(broadcasterId = "123456").collect { sub ->
    println("${sub.userLogin} — tier ${sub.tier}")
}
```

### Polls

```kotlin
val poll = helix.polls.create(
    CreatePollRequest(
        broadcasterId = "123456",
        title = "Favourite game?",
        choices = listOf(CreatePollChoice("Elden Ring"), CreatePollChoice("Hades II")),
        duration = 60,
    )
)
helix.polls.end(broadcasterId = "123456", pollId = poll.id, status = PollEndStatus.TERMINATED)
```

### Channel Point Rewards

```kotlin
val rewards = helix.rewards.list(broadcasterId = "123456")
val reward = helix.rewards.create(broadcasterId = "123456", request = CreateRewardRequest(title = "Hydrate!", cost = 500))
helix.rewards.updateRedemptionStatus(broadcasterId, rewardId, redemptionId, RedemptionStatus.FULFILLED)
```

### Ads

```kotlin
val schedule = helix.ads.getSchedule(broadcasterId = "123456")
helix.ads.snoozeNextAd(broadcasterId = "123456")
helix.ads.startCommercial(broadcasterId = "123456", duration = 30)
```

### Moderation

```kotlin
helix.moderation.getModerators(broadcasterId = "123456").collect { mod -> println(mod.userLogin) }
helix.moderation.getVIPs(broadcasterId = "123456").collect { vip -> println(vip.userLogin) }
helix.moderation.sendShoutout(fromId = "123456", toId = "789", moderatorId = "123456")
```

### Search

```kotlin
val games = helix.search.categories(query = "Minecraft", first = 5)
```

## Resource Reference

| Property | Resource | Key Methods | Required Scopes |
|---|---|---|---|
| `helix.users` | Users | `getUsers(ids, logins)` | None |
| `helix.channels` | Channels | `getInformation`, `update`, `getEditors`, `getFollowedChannels` | `channel:manage:broadcast` (write) |
| `helix.streams` | Streams | `getStreams(userIds, userLogins, gameIds)` | None |
| `helix.chat` | Chat | `sendMessage`, `getChatters`, `getChannelEmotes`, `getGlobalEmotes`, `getEmoteSets`, `getSettings`, `updateSettings`, `sendAnnouncement`, `getUserColor`, `updateUserColor`, `getGlobalBadges`, `getChannelBadges` | Varies per method |
| `helix.followers` | Followers | `list` (Flow), `getTotal` | `moderator:read:followers` |
| `helix.subscriptions` | Subscriptions | `list` (Flow), `createEventSub` | `channel:read:subscriptions` (list) |
| `helix.polls` | Polls | `create`, `end` | `channel:manage:polls` |
| `helix.predictions` | Predictions | `list`, `create`, `end` | `channel:manage:predictions` |
| `helix.ads` | Ads | `getSchedule`, `startCommercial`, `snoozeNextAd` | `channel:read:ads` / `channel:manage:ads` |
| `helix.rewards` | Channel Points | `list`, `create`, `update`, `updateRedemptionStatus` | `channel:read:redemptions` / `channel:manage:redemptions` |
| `helix.moderation` | Moderation | `getModerators` (Flow), `getVIPs` (Flow), `sendShoutout`, `ban`, `unban`, `getBanned`, `getBlockedTerms`, `addBlockedTerm`, `removeBlockedTerm`, `deleteMessage`, `addModerator`, `removeModerator`, `addVip`, `removeVip`, `getShieldMode`, `updateShieldMode`, `warn`, `getUnbanRequests`, `resolveUnbanRequest` | Varies per method |
| `helix.raids` | Raids | `start`, `cancel` | `channel:manage:raids` |
| `helix.clips` | Clips | `create`, `get` | `clips:edit` (create) |
| `helix.videos` | Videos | `get`, `delete` | `channel:manage:videos` (delete) |
| `helix.schedule` | Schedule | `getSchedule`, `createSegment`, `updateSegment`, `deleteSegment` | `channel:manage:schedule` (write) |
| `helix.goals` | Goals | `getGoals` | `channel:read:goals` |
| `helix.bits` | Bits | `getLeaderboard`, `getCheermotes` | `bits:read` (leaderboard) |
| `helix.games` | Games | `getGames`, `getTopGames` | None |
| `helix.teams` | Teams | `getChannelTeams`, `getTeam` | None |
| `helix.hypeTrain` | Hype Train | `getEvents` | `channel:read:hype_train` |
| `helix.whispers` | Whispers | `send` | `user:manage:whispers` |
| `helix.search` | Search | `categories(query)`, `channels(query)` | None |

Paginated methods return `Flow<T>` and fetch all pages automatically using cursor-based pagination.

## Scope Validation

By default, TwitchKt relies on the Twitch API to reject requests missing required scopes (HTTP 403). Opt in to **proactive validation** by supplying a `ScopeProvider`:

```kotlin
val config = TwitchKtConfig(
    clientId = "your_client_id",
    tokenProvider = { myTokenStore.getAccessToken() },
    scopeProvider = { myTokenStore.getGrantedScopes() },
)
```

When a method annotated with `@RequiresScope` is called, scopes are checked locally before the request is sent. Missing scopes throw `TwitchApiException.MissingScope` with no network request.

## Error Handling

Helix methods throw `TwitchApiException` on non-2xx responses. See the [core module](../core/) for the full error hierarchy.

```kotlin
try {
    helix.channels.update(broadcasterId, request)
} catch (e: TwitchApiException.RateLimited) {
    delay(e.retryAfterMs)
} catch (e: TwitchApiException.Forbidden) {
    // missing OAuth scope
}
```

## Integration Testing

End-to-end tests run against Twitch CLI mock servers. They are excluded from normal test runs.

**Prerequisites:** `twitch` CLI and `sqlite3` on `$PATH`, ports 8080 and 8081 free.

Start the mock API server, then run:

```bash
./gradlew :helix:jvmTest -DintegrationTest=true
```

| Test Class | Scenarios |
|---|---|
| `HelixIntegrationTest` | `getStreams()` against the mock Helix API with real authentication |

