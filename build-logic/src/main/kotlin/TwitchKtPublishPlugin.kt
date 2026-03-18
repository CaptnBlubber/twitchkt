import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class TwitchKtPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.vanniktech.maven.publish")

            extensions.configure<MavenPublishBaseExtension> {
                publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
                signAllPublications()

                pom {
                    name.set(provider { "twitchkt-${target.name}" })
                    description.set("Kotlin Multiplatform library for the Twitch API")
                    url.set("https://github.com/CaptnBlubber/twitchkt")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("CaptnBlubber")
                            name.set("Angelo Rüggeberg")
                            url.set("https://github.com/CaptnBlubber")
                        }
                    }

                    scm {
                        url.set("https://github.com/CaptnBlubber/twitchkt")
                        connection.set("scm:git:git://github.com/CaptnBlubber/twitchkt.git")
                        developerConnection.set("scm:git:ssh://git@github.com/CaptnBlubber/twitchkt.git")
                    }
                }
            }
        }
    }
}
