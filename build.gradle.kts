plugins {
    alias(libs.plugins.twitchkt.spotless) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

dependencies {
    subprojects.forEach { subproject ->
        if (subproject.path != ":bom" && !subproject.path.startsWith(":samples")) {
            kover(subproject)
        }
    }
}

kover {
    reports {
        filters {
            excludes {
                // Data models — pure serialization containers with no logic
                packages(
                    "io.github.captnblubber.twitchkt.helix.model",
                    "io.github.captnblubber.twitchkt.eventsub.model",
                    "io.github.captnblubber.twitchkt.eventsub.protocol",
                    "io.github.captnblubber.twitchkt.eventsub.internal",
                    "io.github.captnblubber.twitchkt.model.common",
                )
                // Internal response wrapper
                classes("io.github.captnblubber.twitchkt.helix.internal.TwitchResponse*")
                // IRC message sealed hierarchy and internal parser models
                classes("io.github.captnblubber.twitchkt.irc.IrcMessage*")
                packages("io.github.captnblubber.twitchkt.irc.internal")
                // Auth response DTOs
                classes(
                    "io.github.captnblubber.twitchkt.auth.TokenResponse",
                    "io.github.captnblubber.twitchkt.auth.ValidationResponse",
                )
            }
        }
    }
}
