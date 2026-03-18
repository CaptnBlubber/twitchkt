# twitchkt-core

Shared contracts and domain types for the TwitchKt library. Every other TwitchKt module depends on this.

## Contents

### Configuration

- **`TwitchKtConfig`** — Central configuration holding `clientId`, `TokenProvider`, optional `ScopeProvider`, optional `TwitchKtLogger`, and base URL overrides for Helix, EventSub, and IRC endpoints.

### Auth Contracts

| Type | Kind | Purpose |
|---|---|---|
| `TokenProvider` | `fun interface` | `suspend fun token(): String` — supply OAuth tokens lazily (supports rotation) |
| `ScopeProvider` | `fun interface` | `suspend fun scopes(): Set<TwitchScope>` — return granted scopes for proactive validation |
| `TwitchScope` | `enum` | 100+ OAuth scope values with hierarchy (`implies` property) |
| `@RequiresScope` | annotation | Marks API methods with required OAuth scopes |

### Connection

- **`ConnectionState`** — Enum: `DISCONNECTED`, `CONNECTING`, `CONNECTED`, `RECONNECTING`. Used by EventSub, IRC, and any WebSocket client.

### Error Hierarchy

`TwitchApiException` sealed class with typed subclasses:

| Subclass | HTTP Status | Notes |
|---|---|---|
| `Unauthorized` | 401 | Token expired or invalid |
| `Forbidden` | 403 | Insufficient OAuth scope |
| `NotFound` | 404 | Resource does not exist |
| `BadRequest` | 400 | Malformed request |
| `RateLimited` | 429 | `retryAfterMs` contains the wait duration |
| `Conflict` | 409 | Duplicate resource |
| `UnprocessableEntity` | 422 | Validation error |
| `ServerError` | 5xx | `statusCode` contains the raw code |
| `MissingScope` | *(pre-request)* | `missingScopes` lists the required scopes (opt-in via `ScopeProvider`) |
| `EmptyResponse` | *(client-side)* | `endpoint` contains the Helix path that returned no data |

### Shared Enums

| Enum | Values |
|---|---|
| `SubTier` | `Tier1`, `Tier2`, `Tier3`, `Prime` |
| `PollStatus` | Poll lifecycle states |

## Dependencies

- `twitchkt-logging` (API) — logger interface
- `kotlinx-serialization-json` — enum serialization

## Structure

```
core/src/commonMain/kotlin/io/github/captnblubber/twitchkt/
├── TwitchKtConfig.kt
├── ConnectionState.kt
├── auth/
│   ├── TokenProvider.kt
│   ├── ScopeProvider.kt
│   ├── TwitchScope.kt
│   └── RequiresScope.kt
├── error/
│   └── TwitchApiException.kt
└── model/common/
    ├── SubTier.kt
    └── PollStatus.kt
```
