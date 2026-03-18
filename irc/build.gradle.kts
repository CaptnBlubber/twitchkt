plugins {
    alias(libs.plugins.twitchkt.multiplatform)
    alias(libs.plugins.twitchkt.publish)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-irc")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientWebSockets)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.clientMock)
        }
    }
}
