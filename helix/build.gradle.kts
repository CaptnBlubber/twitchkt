plugins {
    alias(libs.plugins.twitchkt.multiplatform)
    alias(libs.plugins.twitchkt.publish)
    alias(libs.plugins.kotlinSerialization)
}

mavenPublishing {
    coordinates(artifactId = "twitchkt-helix")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJson)
            implementation(libs.kotlinx.serializationJson)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.clientMock)
        }
        jvmTest.dependencies {
            implementation(libs.ktor.clientCIO)
        }
    }
}

tasks.named<Test>("jvmTest") {
    val isIntegration = System.getProperty("integrationTest")?.toBoolean() ?: false
    if (!isIntegration) {
        exclude("**/integration/**")
    }
    systemProperty("integrationTest", System.getProperty("integrationTest") ?: "false")
    systemProperty("mockClientId", System.getProperty("mockClientId") ?: "")
    systemProperty("mockClientSecret", System.getProperty("mockClientSecret") ?: "")
}
