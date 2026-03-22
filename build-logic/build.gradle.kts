plugins {
    `kotlin-dsl`
}

group = "io.github.captnblubber.buildlogic"

dependencies {
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.spotless)
    implementation(libs.plugin.vanniktech.publish)
    implementation(libs.plugin.dokka)
    implementation(libs.plugin.ksp)
    implementation(libs.plugin.kotest)
    implementation(libs.plugin.kover)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("multiplatform") {
            id = "twitchkt.multiplatform"
            implementationClass = "TwitchKtMultiplatformPlugin"
        }
        register("publish") {
            id = "twitchkt.publish"
            implementationClass = "TwitchKtPublishPlugin"
        }
        register("spotless") {
            id = "twitchkt.spotless"
            implementationClass = "TwitchKtSpotlessPlugin"
        }
    }
}
