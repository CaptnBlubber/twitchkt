# Changelog

All notable changes to TwitchKt will be documented here.
Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)

## [Unreleased]

## [1.0.0] - 2026-05-10

### Changed
- Promoted to stable: alpha testing surfaced no major issues, public API is now considered stable under semantic versioning
- Bumped Kotlin to 2.3.21
- Bumped Ktor to 3.4.3
- Bumped kotlinx-coroutines to 1.11.0
- Bumped kotlinx-serialization to 1.11.0
- Bumped KSP to 2.3.7
- Bumped Dokka to 2.2.0
- Bumped Kover to 0.9.8
- Bumped Gradle wrapper to 9.5.0

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

[Unreleased]: https://github.com/CaptnBlubber/twitchkt/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/CaptnBlubber/twitchkt/releases/tag/v1.0.0
[0.1.0-alpha01]: https://github.com/CaptnBlubber/twitchkt/releases/tag/v0.1.0-alpha01
