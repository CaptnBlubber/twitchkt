# Helix Single-Page API Design

**Date:** 2026-03-18
**Scope:** Add `Page<T>` return type and single-page suspend functions to the 5 auto-paginating Helix endpoints; rename their `Flow<T>` counterparts to make "fetch all" explicit.

---

## Overview

Five Helix resource methods currently auto-paginate via `Flow<T>`. Their names (`list`, `getModerators`, `getVIPs`) give no indication that calling them will exhaust every page. This change:

1. Renames those Flow methods with an `All` suffix to make the behaviour explicit.
2. Adds a public `Page<T>` data class that pairs a result list with the next cursor.
3. Adds a single-page suspend counterpart for each renamed method, returning `Page<T>` and accepting an optional `cursor` and optional `pageSize` parameter.
4. Adds a `getPage<T>()` helper to `HelixHttpClient` to support the new suspend functions.

---

## `Page<T>`

New public data class in the `helix` module, package `io.github.captnblubber.twitchkt.helix`:

```kotlin
data class Page<T>(
    val data: List<T>,
    val cursor: String?,
)
```

`cursor` is `null` when there are no further pages. `TwitchResponse<T>` remains `internal` — it is never exposed to callers.

---

## `HelixHttpClient` — new internal helper

```kotlin
suspend inline fun <reified T> getPage(
    endpoint: String,
    params: List<Pair<String, String>> = emptyList(),
    pageSize: Int? = null,
): Page<T> {
    val fullParams = buildList {
        addAll(params)
        pageSize?.let { add("first" to it.toString()) }
    }
    val response = get<T>(endpoint, fullParams)
    return Page(data = response.data, cursor = response.pagination?.cursor)
}
```

`pageSize` is optional — when `null`, the Twitch API uses its own default (typically 20 or 100 depending on the endpoint). Callers pass `pageSize` explicitly when they need a specific page size.

Each resource's single-page function is responsible for threading `cursor` into the params list before calling `getPage`. For example:

```kotlin
suspend fun list(broadcasterId: String, cursor: String? = null, pageSize: Int? = null): Page<Follower> {
    val params = buildList {
        add("broadcaster_id" to broadcasterId)
        cursor?.let { add("after" to it) }
    }
    return http.getPage(endpoint = "channels/followers", params = params, pageSize = pageSize)
}
```

---

## Renames (breaking)

The `All` variants drop the `first` and `after`/`before` cursor params that some Flow methods currently accept — those were forwarded to `paginate()` which already manages page size and cursors internally. `userIds` filter params are kept where applicable.

| Resource | Current name | Renamed to | Param changes |
|---|---|---|---|
| `FollowerResource` | `list()` | `listAll()` | None |
| `SubscriptionResource` | `list()` | `listAll()` | None |
| `ModerationResource` | `getModerators()` | `getAllModerators()` | Drop `first`, `after` |
| `ModerationResource` | `getVIPs()` | `getAllVIPs()` | Drop `first`, `after` |
| `ChatResource` | `getAllChatters()` | *(unchanged)* | None |

These are breaking changes. Acceptable since the library is in `0.1.0-alpha01`.

---

## New single-page suspend functions

Each renamed method gets a suspend counterpart. `cursor` defaults to `null` (first page). `pageSize` defaults to `null` (use Twitch's default for that endpoint). Scope validation is applied identically to the `All` variant.

### `FollowerResource`

```kotlin
suspend fun list(
    broadcasterId: String,
    userId: String? = null,
    cursor: String? = null,
    pageSize: Int? = null,
): Page<Follower>
```

### `SubscriptionResource`

```kotlin
suspend fun list(
    broadcasterId: String,
    cursor: String? = null,
    pageSize: Int? = null,
): Page<Subscription>
```

Note: `SubscriptionResource` already exposes a `get()` method for fine-grained single-page control with `userIds`, `before`, etc. The new `list()` is specifically for cursor-based pagination navigation and intentionally has a minimal signature.

### `ModerationResource`

```kotlin
suspend fun getModerators(
    broadcasterId: String,
    userIds: List<String> = emptyList(),
    cursor: String? = null,
    pageSize: Int? = null,
): Page<ChannelRoleUser>

suspend fun getVIPs(
    broadcasterId: String,
    userIds: List<String> = emptyList(),
    cursor: String? = null,
    pageSize: Int? = null,
): Page<ChannelRoleUser>
```

### `ChatResource`

```kotlin
suspend fun getChatters(
    broadcasterId: String,
    moderatorId: String,
    cursor: String? = null,
    pageSize: Int? = null,
): Page<Chatter>
```

---

## Usage example

```kotlin
var cursor: String? = null
do {
    val page = helix.followers.list(broadcasterId = "123", cursor = cursor)
    process(page.data)
    cursor = page.cursor
} while (cursor != null)
```

---

## Files changed

| File | Change |
|---|---|
| `helix/src/commonMain/.../helix/Page.kt` | New — `Page<T>` data class |
| `helix/src/commonMain/.../helix/internal/HelixHttpClient.kt` | Add `getPage<T>()` helper |
| `helix/src/commonMain/.../helix/resource/FollowerResource.kt` | Rename `list` → `listAll`, add `list` single-page |
| `helix/src/commonMain/.../helix/resource/SubscriptionResource.kt` | Rename `list` → `listAll`, add `list` single-page |
| `helix/src/commonMain/.../helix/resource/ModerationResource.kt` | Rename `getModerators` → `getAllModerators` (drop `first`/`after`), `getVIPs` → `getAllVIPs` (drop `first`/`after`), add single-page counterparts |
| `helix/src/commonMain/.../helix/resource/ChatResource.kt` | Add `getChatters` single-page |
| `helix/src/commonTest/.../helix/resource/*Test.kt` | New/updated tests for all changed resources |
