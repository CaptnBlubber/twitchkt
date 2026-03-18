# twitchkt-bom

Bill of Materials (BOM) for the TwitchKt library. Aligns versions of all TwitchKt modules so consumers only need to declare the BOM dependency once.

## Usage

```kotlin
dependencies {
    implementation(platform("io.github.captnblubber:twitchkt-bom:VERSION"))
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

## Implementation Note

Uses Gradle's `java-platform` plugin which produces Maven BOM metadata (`<dependencyManagement>` in the POM). Despite the `java-` prefix in the plugin name, this is the standard Gradle mechanism for creating BOMs and works with all Gradle consumers including Kotlin Multiplatform projects. Major KMP libraries (Ktor, Koin) use the same approach.
