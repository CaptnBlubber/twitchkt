import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class TwitchKtMultiplatformPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlin.multiplatform")
            apply(plugin = "twitchkt.spotless")

            val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<KotlinMultiplatformExtension> {
                jvm {
                    testRuns.named("test") {
                        executionTask.configure {
                            useJUnitPlatform()
                        }
                    }
                }

                js(IR) {
                    browser {
                        testTask {
                            enabled = false
                        }
                    }
                }

                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    browser {
                        testTask {
                            enabled = false
                        }
                    }
                }

                sourceSets.commonTest.dependencies {
                    implementation(libs.findLibrary("kotest-assertions-core").get())
                    implementation(libs.findLibrary("kotest-framework-engine").get())
                }

                sourceSets.jvmTest.dependencies {
                    implementation(libs.findLibrary("kotest-runner-junit5").get())
                    implementation(libs.findLibrary("mockk").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                }
            }
        }
    }
}
