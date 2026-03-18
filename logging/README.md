# twitchkt-logging

Minimal logging abstraction for the TwitchKt library. Defines the `TwitchKtLogger` interface and `LogLevel` enum with zero external dependencies.

## API

### TwitchKtLogger

```kotlin
fun interface TwitchKtLogger {
    fun log(level: LogLevel, tag: String, message: () -> String)
}
```

A single-method interface with lazy message evaluation. Implementations bridge to the host application's logging framework (e.g. Kermit, SLF4J, `println`).

### LogLevel

```kotlin
enum class LogLevel { DEBUG, INFO, WARN, ERROR }
```

## Usage

Pass a `TwitchKtLogger` to `TwitchKtConfig`. All TwitchKt modules will use it for internal logging. If omitted, logging is silently disabled.

### Custom implementation

Implement the interface to bridge to any logging framework:

```kotlin
// SLF4J example
val logger = TwitchKtLogger { level, tag, message ->
    val slf4j = LoggerFactory.getLogger(tag)
    when (level) {
        LogLevel.DEBUG -> slf4j.debug(message())
        LogLevel.INFO  -> slf4j.info(message())
        LogLevel.WARN  -> slf4j.warn(message())
        LogLevel.ERROR -> slf4j.error(message())
    }
}

val config = TwitchKtConfig(
    clientId = "your_client_id",
    tokenProvider = { myTokenStore.getAccessToken() },
    logger = logger,
)
```

For a ready-made Kermit implementation, see [`twitchkt-logging-kermit`](../logging-kermit/).
