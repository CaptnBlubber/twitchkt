# twitchkt-auth

OAuth2 Authorization Code flow for Twitch. Handles authorization URL generation, code exchange, token refresh, and token validation.

## Setup

```kotlin
val auth = TwitchAuth(httpClient, clientId = "your_client_id", clientSecret = "your_client_secret")
```

Requires an `HttpClient` with JSON content negotiation installed.

## Usage

### Authorization URL

Generate the URL to redirect users to Twitch for login:

```kotlin
val url = auth.authorizationUrl(
    scopes = setOf(TwitchScope.CHAT_READ, TwitchScope.CHANNEL_READ_SUBSCRIPTIONS),
    redirectUri = "http://localhost:8080/callback",
    state = "optional-csrf-state",
)
```

### Code Exchange

After the user authorizes, Twitch redirects with a `code` parameter:

```kotlin
val tokens = auth.exchangeCode(code = "abc123", redirectUri = "http://localhost:8080/callback")
// tokens.accessToken, tokens.refreshToken, tokens.expiresIn
```

### Token Refresh

```kotlin
val newTokens = auth.refresh(refreshToken = tokens.refreshToken)
```

### Token Validation

```kotlin
val info = auth.validate(accessToken = tokens.accessToken)
// info.userId, info.login, info.scopes, info.expiresIn (seconds remaining)
```

## Models

| Type | Fields |
|---|---|
| `TokenResponse` | `accessToken`, `refreshToken`, `expiresIn`, `scopes`, `tokenType` |
| `ValidationResponse` | `clientId`, `login`, `userId`, `scopes`, `expiresIn` |

