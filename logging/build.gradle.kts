plugins {
    alias(libs.plugins.twitchkt.multiplatform)
    alias(libs.plugins.twitchkt.publish)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-logging")
}
