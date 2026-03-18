plugins {
    `java-platform`
    alias(libs.plugins.twitchkt.publish)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-bom")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(projects.logging)
        api(projects.core)
        api(projects.auth)
        api(projects.helix)
        api(projects.eventsub)
        api(projects.irc)
        api(projects.loggingKermit)
    }
}
