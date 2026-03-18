# twitchkt-bom

Bill of Materials (BOM) for the TwitchKt library. Aligns versions of all TwitchKt modules so consumers only need to declare the BOM dependency once.

## Usage

```kotlin
dependencies {
    implementation(platform("io.github.captnblubber:twitchkt-bom:0.1.0-alpha01"))
    implementation("io.github.captnblubber:twitchkt-helix")    // version managed by BOM
    implementation("io.github.captnblubber:twitchkt-eventsub")  // version managed by BOM
}
```

## Included Modules

| Module | Artifact |
|---|---|
| `twitchkt-logging` | `io.github.captnblubber:twitchkt-logging` |
| `twitchkt-core` | `io.github.captnblubber:twitchkt-core` |
| `twitchkt-auth` | `io.github.captnblubber:twitchkt-auth` |
| `twitchkt-helix` | `io.github.captnblubber:twitchkt-helix` |
| `twitchkt-eventsub` | `io.github.captnblubber:twitchkt-eventsub` |
| `twitchkt-irc` | `io.github.captnblubber:twitchkt-irc` |
| `twitchkt-logging-kermit` | `io.github.captnblubber:twitchkt-logging-kermit` |

