package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList
import kotlin.time.Instant

class ClipResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = ClipResource(createHelixClient(engine))

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
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(broadcasterId = "456")

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
                    page.data shouldHaveSize 1
                    page.data[0].id shouldBe "RandomClip123"
                    page.data[0].title shouldBe "Great moment"
                    page.data[0].creatorName shouldBe "Clipper"
                    page.data[0].viewCount shouldBe 500
                    page.data[0].duration shouldBe 25.5
                }
            }

            When("called with startedAt and endedAt") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data": []}""",
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
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
                            headers = JSON_HEADERS,
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
                            headers = JSON_HEADERS,
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

        val clipPaginationJson =
            """
            {
                "data": [
                    {
                        "id": "clip-1",
                        "url": "https://clips.twitch.tv/clip-1",
                        "embed_url": "https://clips.twitch.tv/embed?clip=clip-1",
                        "broadcaster_id": "456",
                        "broadcaster_name": "Streamer",
                        "creator_id": "789",
                        "creator_name": "Clipper",
                        "title": "Nice clip",
                        "view_count": 100,
                        "created_at": "2024-01-01T00:00:00Z",
                        "thumbnail_url": "https://example.com/thumb.jpg",
                        "duration": 30.0
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val clipLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "clip-2",
                        "url": "https://clips.twitch.tv/clip-2",
                        "embed_url": "https://clips.twitch.tv/embed?clip=clip-2",
                        "broadcaster_id": "456",
                        "broadcaster_name": "Streamer",
                        "creator_id": "111",
                        "creator_name": "OtherClipper",
                        "title": "Another clip",
                        "view_count": 50,
                        "created_at": "2024-02-01T00:00:00Z",
                        "thumbnail_url": "https://example.com/thumb2.jpg",
                        "duration": 15.0
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val clipSecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "clip-1",
                        "url": "https://clips.twitch.tv/clip-1",
                        "embed_url": "https://clips.twitch.tv/embed?clip=clip-1",
                        "broadcaster_id": "456",
                        "broadcaster_name": "Streamer",
                        "creator_id": "789",
                        "creator_name": "Clipper",
                        "title": "Nice clip",
                        "view_count": 100,
                        "created_at": "2024-01-01T00:00:00Z",
                        "thumbnail_url": "https://example.com/thumb.jpg",
                        "duration": 30.0
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("getAllClips") {

            When("called with a broadcasterId") {
                val engine =
                    MockEngine {
                        respond(
                            content = clipLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val clips = resource.getAllClips(broadcasterId = "456").toList()

                Then("it should call the clips endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/clips"
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }

                Then("it should deserialize the clip") {
                    clips.size shouldBe 1
                    clips.first().id shouldBe "clip-2"
                    clips.first().title shouldBe "Another clip"
                }
            }
        }

        Given("get - pagination") {

            When("called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = clipPaginationJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(broadcasterId = "456")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the clip data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "clip-1"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = clipSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(broadcasterId = "456", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = clipPaginationJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.get(broadcasterId = "456", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }

            When("called with isFeatured filter") {
                val engine =
                    MockEngine {
                        respond(
                            content = clipPaginationJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.get(broadcasterId = "456", isFeatured = true)

                Then("it should pass the is_featured parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["is_featured"] shouldBe "true"
                }
            }
        }
    })
