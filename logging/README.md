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

All TwitchKt modules accept a logger via `TwitchKtConfig.logger`. If no logger is provided, logging is disabled.

For a ready-made implementation backed by Kermit, see [`twitchkt-logging-kermit`](../logging-kermit/).

## Structure

```
logging/src/commonMain/kotlin/io/github/captnblubber/twitchkt/logging/
├── LogLevel.kt
└── TwitchKtLogger.kt
```
