package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
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
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json

class VideoResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): VideoResource {
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
            return VideoResource(HelixHttpClient(httpClient, config))
        }

        val videoJson =
            """
            {
                "data": [
                    {
                        "id": "v-1",
                        "user_id": "456",
                        "user_login": "streamer",
                        "user_name": "Streamer",
                        "title": "My Stream",
                        "created_at": "2024-01-01T00:00:00Z",
                        "published_at": "2024-01-01T00:00:00Z",
                        "url": "https://www.twitch.tv/videos/v-1",
                        "thumbnail_url": "https://example.com/thumb.jpg",
                        "view_count": 100,
                        "language": "en",
                        "type": "archive",
                        "duration": "3h21m"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val videoLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "v-2",
                        "user_id": "456",
                        "user_login": "streamer",
                        "user_name": "Streamer",
                        "title": "Another Stream",
                        "created_at": "2024-02-01T00:00:00Z",
                        "published_at": "2024-02-01T00:00:00Z",
                        "url": "https://www.twitch.tv/videos/v-2",
                        "thumbnail_url": "https://example.com/thumb2.jpg"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val videoSecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "v-1",
                        "user_id": "456",
                        "user_login": "streamer",
                        "user_name": "Streamer",
                        "title": "My Stream",
                        "created_at": "2024-01-01T00:00:00Z",
                        "published_at": "2024-01-01T00:00:00Z",
                        "url": "https://www.twitch.tv/videos/v-1",
                        "thumbnail_url": "https://example.com/thumb.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("getAllVideos") {

            When("called with a userId") {
                val engine =
                    MockEngine {
                        respond(
                            content = videoLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val videos = resource.getAllVideos(userId = "456").toList()

                Then("it should call the videos endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/videos"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }

                Then("it should deserialize the video") {
                    videos.size shouldBe 1
                    videos.first().id shouldBe "v-2"
                    videos.first().title shouldBe "Another Stream"
                }
            }
        }

        Given("get") {

            When("called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = videoJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(userId = "456")

                Then("it should call the videos endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/videos"
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the video data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "v-1"
                    page.data.first().title shouldBe "My Stream"
                    page.data.first().viewCount shouldBe 100
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
                            content = videoSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(userId = "456", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = videoLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(userId = "456")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = videoJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.get(userId = "456", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }

            When("called with filter parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = videoJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.get(
                    userId = "456",
                    language = "en",
                    period = "week",
                    sort = "views",
                    type = "archive",
                )

                Then("it should pass all filter parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["language"] shouldBe "en"
                    request.url.parameters["period"] shouldBe "week"
                    request.url.parameters["sort"] shouldBe "views"
                    request.url.parameters["type"] shouldBe "archive"
                }
            }
        }
    })
