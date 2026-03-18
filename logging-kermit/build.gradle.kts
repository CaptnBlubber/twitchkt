plugins {
    alias(libs.plugins.twitchkt.multiplatform)
    alias(libs.plugins.twitchkt.publish)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-logging-kermit")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.logging)
            api(libs.kermit)
        }
    }
}
