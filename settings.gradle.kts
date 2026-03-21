pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "twitchkt"

include(":logging")
include(":core")
include(":auth")
include(":helix")
include(":eventsub")
include(":irc")
include(":logging-kermit")
include(":bom")

include(":samples:helix-read")
include(":samples:helix-pagination")
include(":samples:helix-pagination-flow")
include(":samples:helix-write")
include(":samples:eventsub")
