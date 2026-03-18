# Contributing to TwitchKt

Thanks for your interest in contributing! This document covers everything you need to get started.

## Prerequisites

- JDK 21+ (note: JDK 25+ breaks Dokka — use JDK 21 for generating documentation locally)
- Git

## Building

```bash
./gradlew build
```

## Running Tests

```bash
# All JVM unit tests
./gradlew jvmTest --rerun --stacktrace

# Single module
./gradlew :helix:jvmTest --rerun --stacktrace
```

## Integration Tests

Integration tests run against a Twitch CLI mock server and are excluded from the normal test run.

**Prerequisites:** Install the [Twitch CLI](https://dev.twitch.tv/docs/cli/) and ensure `twitch` is on `$PATH`. Ports 8080 and 8081 must be free.

Start the mock API server in a separate terminal before running:

```bash
twitch mock-api start
```

Then run the integration tests for the desired module:

```bash
./gradlew :helix:jvmTest -DintegrationTest=true
./gradlew :eventsub:jvmTest -DintegrationTest=true
```

## Code Style

The project uses [Spotless](https://github.com/diffplug/spotless) with [ktlint](https://github.com/pinterest/ktlint). Before submitting a PR, format your code:

```bash
./gradlew spotlessApply
```

The CI pipeline runs `spotlessCheck` and will fail if formatting is off.

## Generating Documentation

API docs are generated with Dokka. Dokka requires JDK 21 or older — JDK 25+ will break the task:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew dokkaGeneratePublicationHtml
```

## Submitting a PR

1. Fork the repository and create a branch from `main`.
2. Keep changes focused — one logical change per PR.
3. Add or update tests for any changed behaviour.
4. Run `./gradlew spotlessApply` before pushing.
5. Write clear commit messages describing *what* and *why*.
6. Open a PR against `main` — the template will guide you through the description.

## Reporting Bugs / Requesting Features

Use the GitHub issue templates. Please search existing issues before opening a new one.
