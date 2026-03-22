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
