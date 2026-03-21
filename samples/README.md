# TwitchKt Samples

Runnable JVM sample applications demonstrating core TwitchKt usage patterns.

## Setup

1. Copy the credential template:
   ```bash
   cp samples/local.properties.example samples/local.properties
   ```

2. Fill in your Twitch credentials in `samples/local.properties`:
   ```properties
   twitch.token=YOUR_TOKEN_HERE
   twitch.clientId=YOUR_CLIENT_ID_HERE

   # Required for helix-write and eventsub samples:
   twitch.broadcasterId=YOUR_BROADCASTER_ID_HERE
   twitch.userId=YOUR_USER_ID_HERE
   ```

### Getting a Token

For quick testing, you can generate a token at [twitchtokengenerator.com](https://twitchtokengenerator.com/).

> **Note:** We do not own or operate twitchtokengenerator.com. Treat tokens as secrets and never commit them. For production applications, set up your own OAuth flow with a registered Twitch application.

## Samples

| Sample | Run Command | Description |
|--------|-------------|-------------|
| **helix-read** | `./gradlew :samples:helix-read:run` | Look up a user by login name — simplest possible Helix call |
| **helix-pagination** | `./gradlew :samples:helix-pagination:run` | Manual pagination with `Page<T>`, advancing the cursor explicitly |
| **helix-pagination-flow** | `./gradlew :samples:helix-pagination-flow:run` | Auto-paginating `Flow` API — same data as above, much simpler code |
| **helix-write** | `./gradlew :samples:helix-write:run` | Send a chat message (requires `broadcasterId` and `userId`) |
| **eventsub** | `./gradlew :samples:eventsub:run` | Connect to EventSub WebSocket and print real-time chat messages |
