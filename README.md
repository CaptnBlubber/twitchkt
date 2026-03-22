# TwitchKt

[![CI](https://github.com/CaptnBlubber/twitchkt/actions/workflows/ci.yml/badge.svg)](https://github.com/CaptnBlubber/twitchkt/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.captnblubber/twitchkt-core)](https://central.sonatype.com/namespace/io.github.captnblubber)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.20-blue.svg?logo=kotlin)](https://kotlinlang.org)
![Coverage](https://img.shields.io/badge/coverage-99%25-brightgreen)
[![License](https://img.shields.io/badge/license-Apache%202.0-green.svg)](LICENSE)

Kotlin Multiplatform Twitch API library. Provides typed, coroutine-native clients for Twitch OAuth2, Helix REST API, EventSub WebSocket, and IRC.

## Features

- Kotlin Multiplatform (JVM, JS, Wasm targets via `commonMain`)
- Typed suspend functions for all Helix endpoints
- `Flow`-based cursor pagination — emit all pages without manual cursor management
- EventSub WebSocket with automatic reconnect and keepalive management
- OAuth2 Authorization Code flow — authorization URL, code exchange, token refresh, validation
- Pluggable `TokenProvider` — supply tokens lazily at call time (supports rotation)
- Pluggable `TwitchKtLogger` — bridge to any logging framework
- Opt-in `ScopeProvider` for proactive scope validation before requests
- `@RequiresScope` annotations on methods that need specific OAuth scopes
- Typed `TwitchApiException` hierarchy with rate-limit retry-after support
- Twitch CLI-compatible URL overrides for local mock server testing

## Modules

| Module | Purpose |
|---|---|
| [`twitchkt-logging`](logging/) | `TwitchKtLogger` interface, `LogLevel` enum. Zero dependencies. |
| [`twitchkt-core`](core/) | Config (`TwitchKtConfig`), auth contracts (`TokenProvider`, `ScopeProvider`, `TwitchScope`), error hierarchy, shared enums |
| [`twitchkt-auth`](auth/) | OAuth2 flows — authorization URL, token exchange, refresh, validation |
| [`twitchkt-helix`](helix/) | Twitch Helix REST API client with 25 typed resource groups and pagination |
| [`twitchkt-eventsub`](eventsub/) | EventSub WebSocket client with 73 typed event models, reconnection logic |
| [`twitchkt-irc`](irc/) | Deprecated IRC client (retained for watch streaks only) |
| [`twitchkt-logging-kermit`](logging-kermit/) | `TwitchKtLogger` implementation backed by Kermit |
| [`twitchkt-bom`](bom/) | BOM/platform artifact for version alignment |

## Getting Started

TwitchKt uses [Ktor](https://ktor.io) for all HTTP and WebSocket communication. You provide the `HttpClient` so you stay in control of the engine, timeouts, and plugins — TwitchKt does not force a specific setup on you.

### 1. Add dependencies

If you only need specific parts of the library, declare them directly with an explicit version:

```kotlin
dependencies {
    val twitchKtVersion = "0.1.0-alpha01"
    implementation("io.github.captnblubber:twitchkt-helix:$twitchKtVersion")      // Helix REST API
    implementation("io.github.captnblubber:twitchkt-eventsub:$twitchKtVersion")   // EventSub WebSocket
    implementation("io.github.captnblubber:twitchkt-auth:$twitchKtVersion")       // OAuth2 flows

    // Ktor — pick an engine for your platform
    val ktorVersion = "3.3.3"
    implementation("io.ktor:ktor-client-cio:$ktorVersion")                         // JVM
    // implementation("io.ktor:ktor-client-js:$ktorVersion")                       // JS/Wasm

    // Ktor plugins required by TwitchKt
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")                  // EventSub only
}
```

Alternatively, use the BOM to align all module versions and omit version numbers on individual declarations:

```kotlin
dependencies {
    // BOM — manages all twitchkt module versions
    implementation(platform("io.github.captnblubber:twitchkt-bom:0.1.0-alpha01"))

    // TwitchKt modules (no version needed with BOM)
    implementation("io.github.captnblubber:twitchkt-helix")
    implementation("io.github.captnblubber:twitchkt-eventsub")
    implementation("io.github.captnblubber:twitchkt-auth")

    // Optional: Kermit logging bridge
    implementation("io.github.captnblubber:twitchkt-logging-kermit")

    // Ktor (still needs an explicit version)
    val ktorVersion = "3.3.3"
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")                  // EventSub only
}
```

### 2. Create an HttpClient

TwitchKt requires `ContentNegotiation` with JSON. If you use EventSub, also install `WebSockets`:

```kotlin
val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets)
}
```

### 3. Configure TwitchKt

```kotlin
val config = TwitchKtConfig(
    clientId = "your_client_id",
    tokenProvider = { myTokenStore.getAccessToken() },
)
```

The `tokenProvider` lambda is called on every request, so you can rotate or refresh tokens transparently without rebuilding the client.

## Quick Start

### Helix REST API

```kotlin
val helix = TwitchHelix(httpClient, config)
```

Fetch a user, check if a stream is live, or update channel info — all as typed suspend functions:

```kotlin
// Look up a user by login name
val users = helix.users.getUsers(logins = listOf("captnblubber"))

// Check if a channel is currently live
val page = helix.streams.getStreams(userLogins = listOf("captnblubber"))
val streams = page.data

// Update the stream title (requires channel:manage:broadcast scope)
helix.channels.update(
    broadcasterId = "123456",
    request = UpdateChannelRequest(title = "New stream title"),
)
```

Paginated endpoints offer two access patterns — an auto-paginating `Flow` and a single-page `Page<T>`:

```kotlin
// Option 1: Auto-paginate with Flow — fetches all pages as you collect
helix.followers.listAll(broadcasterId = "123456").collect { follower ->
    println(follower.userLogin)
}

// Option 2: Single page with manual cursor control
val page = helix.followers.list(broadcasterId = "123456", pageSize = 50)
page.data.forEach { println(it.userLogin) }

// Fetch the next page using the cursor
val nextPage = helix.followers.list(broadcasterId = "123456", cursor = page.cursor)
```

### EventSub WebSocket

EventSub delivers real-time Twitch events over a managed WebSocket connection. Subscribe to the events you care about, connect, then collect from the `events` flow:

```kotlin
val eventSub = TwitchEventSub(httpClient, config, helix.eventSub)

// Register subscriptions before connecting
eventSub.subscribe(EventSubSubscriptionType.ChannelFollow(broadcasterId, moderatorId))
eventSub.subscribe(EventSubSubscriptionType.StreamOnline(broadcasterId))

// Connect — manages keepalives and reconnects automatically
eventSub.connect(coroutineScope)

// All incoming events arrive on this flow as typed sealed classes
eventSub.events.collect { event ->
    when (event) {
        is ChannelFollow -> println("New follower: ${event.userName}")
        is ChannelSubscribe -> println("New sub: ${event.userName} tier ${event.tier}")
        is StreamOnline -> println("Stream started!")
        else -> { }
    }
}
```

### Authentication

If you need to handle the OAuth2 flow yourself rather than providing a static token:

```kotlin
val auth = TwitchAuth(httpClient, clientId = "your_client_id", clientSecret = "your_client_secret")

// Build the URL to redirect users to for authorization
val url = auth.authorizationUrl(
    scopes = setOf(TwitchScope.CHAT_READ, TwitchScope.CHANNEL_READ_SUBSCRIPTIONS),
    redirectUri = "http://localhost:8080/callback",
)

// Exchange the code Twitch returns for an access + refresh token pair
val tokens = auth.exchangeCode(code = "abc123", redirectUri = "http://localhost:8080/callback")

// Refresh when the access token expires
val newTokens = auth.refresh(refreshToken = tokens.refreshToken)
```

### Error Handling

All Twitch API errors are thrown as typed `TwitchApiException` subclasses:

```kotlin
try {
    helix.channels.update(broadcasterId, request)
} catch (e: TwitchApiException.RateLimited) {
    delay(e.retryAfterMs)
} catch (e: TwitchApiException.Forbidden) {
    // Missing OAuth scope — check @RequiresScope on the method
}
```

## Integration Tests

Integration tests run against Twitch CLI mock servers and are excluded from the normal test run.

**Prerequisites:** Install the [Twitch CLI](https://dev.twitch.tv/docs/cli/) and ensure `twitch` is on `$PATH`. Ports 8080 and 8081 must be free.

Start the mock API server in a separate terminal:

```bash
twitch mock-api start
```

Then run the integration tests for the desired module:

```bash
# Helix integration tests
./gradlew :helix:jvmTest -DintegrationTest=true

# EventSub integration tests
./gradlew :eventsub:jvmTest -DintegrationTest=true
```

See the [helix](helix/) and [eventsub](eventsub/) module READMEs for details on what each suite covers.

## Module Documentation

Each module has its own README with deeper documentation on its API, models, and usage patterns:

| Module | Documentation |
|---|---|
| `twitchkt-logging` | [logging/README.md](logging/README.md) |
| `twitchkt-core` | [core/README.md](core/README.md) |
| `twitchkt-auth` | [auth/README.md](auth/README.md) |
| `twitchkt-helix` | [helix/README.md](helix/README.md) |
| `twitchkt-eventsub` | [eventsub/README.md](eventsub/README.md) |
| `twitchkt-irc` | [irc/README.md](irc/README.md) |
| `twitchkt-logging-kermit` | [logging-kermit/README.md](logging-kermit/README.md) |
| `twitchkt-bom` | [bom/README.md](bom/README.md) |

## Support

If you find TwitchKt useful, the best way to support the project is to drop a follow on the Twitch channel where it was built — [twitch.tv/captnblubber](https://twitch.tv/captnblubber). If you have Amazon Prime, a free Prime sub goes a long way too.

## License

```
Copyright 2026 Angelo Rüggeberg

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
