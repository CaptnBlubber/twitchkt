package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import kotlin.time.Instant

class ClipResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): ClipResource {
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
            val config =
                TwitchKtConfig(
                    clientId = testClientId,
                    tokenProvider = TokenProvider { testToken },
                )
            return ClipResource(HelixHttpClient(httpClient, config))
        }

        Given("get") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "RandomClip123",
                                            "url": "https://clips.twitch.tv/RandomClip123",
                                            "embed_url": "https://clips.twitch.tv/embed?clip=RandomClip123",
                                            "broadcaster_id": "456",
                                            "broadcaster_name": "Streamer",
                                            "creator_id": "789",
                                            "creator_name": "Clipper",
                                            "title": "Great moment",
                                            "view_count": 500,
                                            "created_at": "2024-03-15T10:00:00Z",
                                            "thumbnail_url": "https://clips-media-assets2.twitch.tv/clip-preview-480x272.jpg",
                                            "duration": 25.5
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val clips = resource.get(broadcasterId = "456")

                Then("it should call the clips endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/clips"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }

                Then("it should deserialize the clip") {
                    clips shouldHaveSize 1
                    clips[0].id shouldBe "RandomClip123"
                    clips[0].title shouldBe "Great moment"
                    clips[0].creatorName shouldBe "Clipper"
                    clips[0].viewCount shouldBe 500
                    clips[0].duration shouldBe 25.5
                }
            }

            When("called with startedAt and endedAt") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data": []}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val start = Instant.parse("2024-01-01T00:00:00Z")
                val end = Instant.parse("2024-01-02T00:00:00Z")
                resource.get(broadcasterId = "456", startedAt = start, endedAt = end)

                Then("it should pass started_at parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["started_at"] shouldBe "2024-01-01T00:00:00Z"
                }

                Then("it should pass ended_at parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["ended_at"] shouldBe "2024-01-02T00:00:00Z"
                }
            }

            When("called with specific clip IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data": []}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.get(ids = listOf("clip1", "clip2"))

                Then("it should pass multiple id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("clip1", "clip2")
                }
            }
        }

        Given("getDownloadUrls") {

            When("called with editor, broadcaster, and clip IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "clip_id": "clip1",
                                            "landscape_download_url": "https://production.assets.clips.twitchcdn.net/clip1/landscape.mp4",
                                            "portrait_download_url": "https://production.assets.clips.twitchcdn.net/clip1/portrait.mp4"
                                        },
                                        {
                                            "clip_id": "clip2",
                                            "landscape_download_url": "https://production.assets.clips.twitchcdn.net/clip2/landscape.mp4",
                                            "portrait_download_url": null
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val downloads =
                    resource.getDownloadUrls(
                        editorId = "editor123",
                        broadcasterId = "456",
                        clipIds = listOf("clip1", "clip2"),
                    )

                Then("it should call the clips/downloads endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/clips/downloads"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass editor_id and broadcaster_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["editor_id"] shouldBe "editor123"
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }

                Then("it should pass clip_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("clip_id") shouldBe listOf("clip1", "clip2")
                }

                Then("it should deserialize download entries") {
                    downloads shouldHaveSize 2
                    downloads[0].clipId shouldBe "clip1"
                    downloads[0].landscapeDownloadUrl shouldBe "https://production.assets.clips.twitchcdn.net/clip1/landscape.mp4"
                    downloads[0].portraitDownloadUrl shouldBe "https://production.assets.clips.twitchcdn.net/clip1/portrait.mp4"
                    downloads[1].clipId shouldBe "clip2"
                    downloads[1].portraitDownloadUrl shouldBe null
                }
            }
        }
    })
