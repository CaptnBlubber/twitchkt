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

class SearchResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): SearchResource {
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
            return SearchResource(HelixHttpClient(httpClient, config))
        }

        val categoryJson =
            """
            {
                "data": [
                    {
                        "id": "509658",
                        "name": "Just Chatting",
                        "box_art_url": "https://example.com/boxart.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val categoryLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "33214",
                        "name": "Fortnite",
                        "box_art_url": "https://example.com/fortnite.jpg"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val categorySecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "509658",
                        "name": "Just Chatting",
                        "box_art_url": "https://example.com/boxart.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("Categories") {

            When("getAllCategories is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = categoryLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val categories = resource.getAllCategories(query = "fort").toList()

                Then("it should call the search/categories endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/search/categories"
                }

                Then("it should pass the query parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["query"] shouldBe "fort"
                }

                Then("it should deserialize the category") {
                    categories.size shouldBe 1
                    categories.first().id shouldBe "33214"
                    categories.first().name shouldBe "Fortnite"
                }
            }

            When("categories is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = categoryJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.categories(query = "just")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the category data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "509658"
                    page.data.first().name shouldBe "Just Chatting"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("categories is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = categorySecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.categories(query = "just", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("categories is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = categoryJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.categories(query = "just", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        val channelJson =
            """
            {
                "data": [
                    {
                        "broadcaster_language": "en",
                        "broadcaster_login": "streamer",
                        "display_name": "Streamer",
                        "game_id": "509658",
                        "game_name": "Just Chatting",
                        "id": "456",
                        "is_live": true,
                        "title": "Hello!",
                        "started_at": "2024-01-01T00:00:00Z",
                        "thumbnail_url": "https://example.com/thumb.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val channelLastPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_language": "de",
                        "broadcaster_login": "streamer2",
                        "display_name": "Streamer2",
                        "game_id": "33214",
                        "game_name": "Fortnite",
                        "id": "789",
                        "is_live": false,
                        "title": "Offline",
                        "thumbnail_url": "https://example.com/thumb2.jpg"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val channelSecondPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_language": "en",
                        "broadcaster_login": "streamer",
                        "display_name": "Streamer",
                        "game_id": "509658",
                        "game_name": "Just Chatting",
                        "id": "456",
                        "is_live": true,
                        "title": "Hello!",
                        "started_at": "2024-01-01T00:00:00Z",
                        "thumbnail_url": "https://example.com/thumb.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("Channels") {

            When("getAllChannels is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = channelLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val channels = resource.getAllChannels(query = "streamer").toList()

                Then("it should call the search/channels endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/search/channels"
                }

                Then("it should pass the query parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["query"] shouldBe "streamer"
                }

                Then("it should deserialize the channel") {
                    channels.size shouldBe 1
                    channels.first().id shouldBe "789"
                    channels.first().displayName shouldBe "Streamer2"
                }
            }

            When("channels is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = channelJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.channels(query = "streamer")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the channel data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "456"
                    page.data.first().displayName shouldBe "Streamer"
                    page.data.first().isLive shouldBe true
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("channels is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = channelSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.channels(query = "streamer", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("channels is called with liveOnly true") {
                val engine =
                    MockEngine {
                        respond(
                            content = channelJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.channels(query = "streamer", liveOnly = true)

                Then("it should pass the live_only parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["live_only"] shouldBe "true"
                }
            }

            When("channels is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = channelJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.channels(query = "streamer", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }
    })
