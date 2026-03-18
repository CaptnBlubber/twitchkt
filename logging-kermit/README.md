# twitchkt-logging-kermit

`TwitchKtLogger` implementation backed by [Kermit](https://github.com/touchlab/Kermit). Bridges TwitchKt's logging interface to the Kermit logging framework used by the host application.

## Usage

```kotlin
val logger = KermitTwitchKtLogger(tagPrefix = "twitchkt")

val config = TwitchKtConfig(
    clientId = "your_client_id",
    tokenProvider = { myTokenStore.getAccessToken() },
    logger = logger,
)
```

Log messages from TwitchKt modules are forwarded to Kermit with tags prefixed by the configured prefix (e.g. `twitchkt/TwitchEventSub`, `twitchkt/HelixHttpClient`).

## Dependencies

- `twitchkt-logging` (API) — `TwitchKtLogger` interface
- Kermit — logging framework

## Structure

```
logging-kermit/src/commonMain/kotlin/io/github/captnblubber/twitchkt/logging/kermit/
└── KermitTwitchKtLogger.kt
```
