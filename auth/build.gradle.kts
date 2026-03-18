plugins {
    alias(libs.plugins.twitchkt.multiplatform)
    alias(libs.plugins.twitchkt.publish)
    alias(libs.plugins.kotlinSerialization)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-auth")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.kotlinx.serializationJson)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.clientMock)
        }
    }
}
