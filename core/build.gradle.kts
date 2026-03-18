plugins {
    alias(libs.plugins.twitchkt.multiplatform)
    alias(libs.plugins.twitchkt.publish)
    alias(libs.plugins.kotlinSerialization)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.logging)
            implementation(libs.kotlinx.serializationJson)
        }
    }
}
