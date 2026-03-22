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
// Auto-paginate all live streams for a game
helix.streams.getAllStreams(gameIds = listOf("509658")).collect { stream ->
    println("${stream.userName} — ${stream.viewerCount} viewers")
}

// Or fetch a single page with manual cursor control
val page = helix.streams.getStreams(userLogins = listOf("captnblubber"))
println(page.data.firstOrNull()?.title)
println("Next cursor: ${page.cursor}")
```

### Chat

```kotlin
helix.chat.sendMessage(SendChatMessageRequest(broadcasterId = "123456", senderId = "789", message = "Hello!"))
val globalBadges = helix.chat.getGlobalBadges()
val channelBadges = helix.chat.getChannelBadges(broadcasterId = "123456")
```

### Followers

```kotlin
// Auto-paginate all followers
helix.followers.listAll(broadcasterId = "123456").collect { follower ->
    println(follower.userLogin)
}

// Or fetch a single page
val page = helix.followers.list(broadcasterId = "123456")
println("${page.data.size} followers, next cursor: ${page.cursor}")

val count = helix.followers.getTotal(broadcasterId = "123456")
```

### Subscriptions

```kotlin
// Auto-paginate all subscriptions
helix.subscriptions.getAll(broadcasterId = "123456").collect { sub ->
    println("${sub.userLogin} — tier ${sub.tier}")
}

// Or fetch a single page
val page = helix.subscriptions.get(broadcasterId = "123456", pageSize = 50)
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
// Auto-paginate all moderators
helix.moderation.getAllModerators(broadcasterId = "123456").collect { mod -> println(mod.userLogin) }

// Single page of banned users with manual cursor control
val bannedPage = helix.moderation.getBanned(broadcasterId = "123456", pageSize = 50)
bannedPage.data.forEach { println("${it.userName} — ${it.reason}") }
val nextPage = helix.moderation.getBanned(broadcasterId = "123456", cursor = bannedPage.cursor)

helix.moderation.sendShoutout(fromId = "123456", toId = "789", moderatorId = "123456")
```

### Search

```kotlin
// Auto-paginate all matching categories
helix.search.getAllCategories(query = "Minecraft").collect { game ->
    println(game.name)
}

// Or fetch a single page
val page = helix.search.categories(query = "Minecraft", pageSize = 5)
```

## Resource Reference

| Property | Resource | Key Methods | Required Scopes |
|---|---|---|---|
| `helix.users` | Users | `getUsers(ids, logins)`, `getBlockList` / `getAllBlockedUsers` (Flow) | `user:read:blocked_users` (block list) |
| `helix.channels` | Channels | `getInformation`, `update`, `getEditors`, `getFollowedChannels` / `getAllFollowedChannels` (Flow) | `channel:manage:broadcast` (write) |
| `helix.streams` | Streams | `getStreams` / `getAllStreams` (Flow), `getFollowedStreams` / `getAllFollowedStreams` (Flow), `getStreamMarkers` / `getAllStreamMarkers` (Flow) | Varies per method |
| `helix.chat` | Chat | `sendMessage`, `getChatters` / `getAllChatters` (Flow), `getUserEmotes` / `getAllUserEmotes` (Flow), `getChannelEmotes`, `getGlobalEmotes`, `getEmoteSets`, `getSettings`, `updateSettings`, `sendAnnouncement`, `getUserColor`, `updateUserColor`, `getGlobalBadges`, `getChannelBadges` | Varies per method |
| `helix.followers` | Followers | `list` / `listAll` (Flow), `getTotal` | `moderator:read:followers` |
| `helix.subscriptions` | Subscriptions | `get`, `getAll` (Flow), `checkUserSubscription` | `channel:read:subscriptions` / `user:read:subscriptions` |
| `helix.eventSub` | EventSub | `create` | — |
| `helix.polls` | Polls | `create`, `end` | `channel:manage:polls` |
| `helix.predictions` | Predictions | `list`, `create`, `end` | `channel:manage:predictions` |
| `helix.ads` | Ads | `getSchedule`, `startCommercial`, `snoozeNextAd` | `channel:read:ads` / `channel:manage:ads` |
| `helix.rewards` | Channel Points | `list`, `create`, `update`, `getRedemptions` / `getAllRedemptions` (Flow), `updateRedemptionStatus` | `channel:read:redemptions` / `channel:manage:redemptions` |
| `helix.moderation` | Moderation | `getModerators` / `getAllModerators` (Flow), `getVIPs` / `getAllVIPs` (Flow), `getBanned` / `getAllBanned` (Flow), `getBlockedTerms` / `getAllBlockedTerms` (Flow), `getModeratedChannels` / `getAllModeratedChannels` (Flow), `getUnbanRequests` / `getAllUnbanRequests` (Flow), `sendShoutout`, `ban`, `unban`, `addBlockedTerm`, `removeBlockedTerm`, `deleteMessage`, `addModerator`, `removeModerator`, `addVip`, `removeVip`, `getShieldMode`, `updateShieldMode`, `warn`, `resolveUnbanRequest` | Varies per method |
| `helix.raids` | Raids | `start`, `cancel` | `channel:manage:raids` |
| `helix.clips` | Clips | `create`, `get` / `getAllClips` (Flow) | `clips:edit` (create) |
| `helix.videos` | Videos | `get` / `getAllVideos` (Flow), `delete` | `channel:manage:videos` (delete) |
| `helix.schedule` | Schedule | `getSchedule`, `createSegment`, `updateSegment`, `deleteSegment` | `channel:manage:schedule` (write) |
| `helix.goals` | Goals | `getGoals` | `channel:read:goals` |
| `helix.bits` | Bits | `getLeaderboard`, `getCheermotes` | `bits:read` (leaderboard) |
| `helix.games` | Games | `getGames`, `getTopGames` | None |
| `helix.teams` | Teams | `getChannelTeams`, `getTeam` | None |
| `helix.hypeTrain` | Hype Train | `getEvents` | `channel:read:hype_train` |
| `helix.whispers` | Whispers | `send` | `user:manage:whispers` |
| `helix.search` | Search | `categories` / `getAllCategories` (Flow), `channels` / `getAllChannels` (Flow) | None |

Paginated endpoints offer two access patterns:
- **`getAllXxx()`** returns `Flow<T>` — fetches all pages automatically as you collect.
- **`getXxx()`** returns `Page<T>` — fetches a single page; use `page.cursor` to request the next page manually.

For full method signatures, parameters, and response models, see the [API documentation](https://captnblubber.github.io/twitchkt/).

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

