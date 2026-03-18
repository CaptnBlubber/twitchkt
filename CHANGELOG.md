# Changelog

All notable changes to TwitchKt will be documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)

## [Unreleased]

## [0.1.0-alpha01] - 2026-03-18

### Added
- Initial release with typed clients for Twitch OAuth2, Helix REST API, EventSub WebSocket, and IRC
- Kotlin Multiplatform support (JVM, JS, WasmJS)
- `Flow`-based cursor pagination for all paginated Helix endpoints
- EventSub WebSocket with automatic reconnect and keepalive management
- Pluggable `TokenProvider` and `TwitchKtLogger` interfaces
- `@RequiresScope` annotations and opt-in `ScopeProvider` for proactive scope validation
- Typed `TwitchApiException` hierarchy with rate-limit retry-after support
- Twitch CLI-compatible URL overrides for local mock server testing
- BOM artifact (`twitchkt-bom`) for version alignment
- Kermit logging bridge (`twitchkt-logging-kermit`)

[Unreleased]: https://github.com/CaptnBlubber/twitchkt/compare/v0.1.0-alpha01...HEAD
[0.1.0-alpha01]: https://github.com/CaptnBlubber/twitchkt/releases/tag/v0.1.0-alpha01
