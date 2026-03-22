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
                // Data model packages — pure serialization containers with no logic
                packages(
                    "io.github.captnblubber.twitchkt.helix.model",
                    "io.github.captnblubber.twitchkt.eventsub.model",
                    "io.github.captnblubber.twitchkt.eventsub.protocol",
                    "io.github.captnblubber.twitchkt.eventsub.internal",
                    "io.github.captnblubber.twitchkt.model.common",
                    "io.github.captnblubber.twitchkt.irc.internal",
                )
                // EventSub subscription type definitions — sealed class with 60+ data class entries
                classes("io.github.captnblubber.twitchkt.eventsub.EventSubSubscriptionType*")
                // WebSocket connection managers — tested via integration tests
                classes("io.github.captnblubber.twitchkt.eventsub.TwitchEventSub*")
                classes("io.github.captnblubber.twitchkt.irc.TwitchIrc*")
                // IRC message sealed hierarchy
                classes("io.github.captnblubber.twitchkt.irc.IrcMessage*")
            }
        }
    }
}
