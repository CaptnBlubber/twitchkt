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
            apply(plugin = "com.google.devtools.ksp")
            apply(plugin = "io.kotest")
            apply(plugin = "org.jetbrains.dokka")
            apply(plugin = "org.jetbrains.kotlinx.kover")
            apply(plugin = "twitchkt.spotless")

            val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<KotlinMultiplatformExtension> {
                // JVM
                jvm {
                    testRuns.named("test") {
                        executionTask.configure {
                            useJUnitPlatform()
                        }
                    }
                }

                // JS — nested test styles (BehaviorSpec) are not supported on JS
                js(IR) {
                    nodejs {
                        testTask {
                            enabled = false
                        }
                    }
                }

                // Wasm
                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    nodejs()
                }

                // iOS
                iosX64()
                iosArm64()
                iosSimulatorArm64()

                // macOS
                macosX64()
                macosArm64()

                // tvOS
                tvosX64()
                tvosArm64()
                tvosSimulatorArm64()

                // watchOS
                watchosX64()
                watchosArm32()
                watchosArm64()
                watchosSimulatorArm64()

                // Linux
                linuxX64()
                linuxArm64()

                // Windows
                mingwX64()

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
