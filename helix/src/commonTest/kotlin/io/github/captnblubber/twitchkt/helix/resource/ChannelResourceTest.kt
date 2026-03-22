package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.ScopeProvider
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.UpdateChannelRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json

class ChannelResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = ChannelResource(createHelixClient(engine))

        Given("getInformation") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "456",
                                            "broadcaster_login": "streamer",
                                            "broadcaster_name": "Streamer",
                                            "broadcaster_language": "en",
                                            "game_name": "Just Chatting",
                                            "game_id": "509658",
                                            "title": "Hello World!",
                                            "delay": 0,
                                            "tags": ["English", "Fun"],
                                            "content_classification_labels": [],
                                            "is_branded_content": false
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val info = resource.getInformation("456")

                Then("it should call the channels endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }

                Then("it should deserialize channel information") {
                    info.broadcasterId shouldBe "456"
                    info.broadcasterLogin shouldBe "streamer"
                    info.gameName shouldBe "Just Chatting"
                    info.gameId shouldBe "509658"
                    info.title shouldBe "Hello World!"
                    info.tags shouldBe listOf("English", "Fun")
                }
            }

            When("the API returns null tags") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "789",
                                            "broadcaster_login": "nulltags",
                                            "broadcaster_name": "NullTags",
                                            "broadcaster_language": "en",
                                            "game_name": "Just Chatting",
                                            "game_id": "509658",
                                            "title": "Stream with null tags",
                                            "delay": 0,
                                            "tags": null,
                                            "content_classification_labels": [],
                                            "is_branded_content": false
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val info = resource.getInformation("789")

                Then("it should deserialize without crashing") {
                    info.broadcasterId shouldBe "789"
                    info.title shouldBe "Stream with null tags"
                }

                Then("tags should be null") {
                    info.tags shouldBe null
                }
            }
        }

        Given("update") {

            When("called with a title update") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                val request = UpdateChannelRequest(title = "New Title")
                resource.update("456", request)

                Then("it should call the channels endpoint") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.encodedPath shouldBe "/helix/channels"
                }

                Then("it should use PATCH method") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.method shouldBe HttpMethod.Patch
                }

                Then("it should pass the broadcaster_id parameter") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.parameters["broadcaster_id"] shouldBe "456"
                }

                Then("it should set Content-Type to application/json") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.body.contentType?.toString() shouldBe "application/json"
                }
            }

            When("called with multiple fields") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                val request =
                    UpdateChannelRequest(
                        title = "Updated",
                        gameId = "12345",
                        tags = listOf("Tag1", "Tag2"),
                    )
                resource.update("789", request)

                Then("it should pass the broadcaster_id parameter") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.parameters["broadcaster_id"] shouldBe "789"
                }

                Then("it should complete without error for 204 response") {
                    engine.requestHistory shouldBe engine.requestHistory
                }
            }
        }

        val followedChannelJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "456",
                        "broadcaster_login": "followed",
                        "broadcaster_name": "Followed",
                        "followed_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val followedChannelLastPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "789",
                        "broadcaster_login": "lastfollowed",
                        "broadcaster_name": "LastFollowed",
                        "followed_at": "2024-02-01T00:00:00Z"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val followedChannelSecondPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "456",
                        "broadcaster_login": "followed",
                        "broadcaster_name": "Followed",
                        "followed_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("FollowedChannels") {

            When("getAllFollowedChannels is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = followedChannelLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val channels = resource.getAllFollowedChannels(userId = "123").toList()

                Then("it should call the channels/followed endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/followed"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should deserialize the followed channel") {
                    channels.size shouldBe 1
                    channels.first().broadcasterId shouldBe "789"
                    channels.first().broadcasterLogin shouldBe "lastfollowed"
                }
            }

            When("getFollowedChannels is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = followedChannelJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getFollowedChannels(userId = "123")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the followed channel data") {
                    page.data.size shouldBe 1
                    page.data.first().broadcasterId shouldBe "456"
                    page.data.first().broadcasterName shouldBe "Followed"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getFollowedChannels is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = followedChannelSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getFollowedChannels(userId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getFollowedChannels is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = followedChannelLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getFollowedChannels(userId = "123")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getFollowedChannels is called with a broadcasterId filter") {
                val engine =
                    MockEngine {
                        respond(
                            content = followedChannelJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getFollowedChannels(userId = "123", broadcasterId = "456")

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }
            }

            When("getFollowedChannels is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = followedChannelJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getFollowedChannels(userId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        Given("getInformation error paths") {

            When("the API returns an empty data array") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"data": []}""",
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw EmptyResponse") {
                    shouldThrow<TwitchApiException.EmptyResponse> {
                        resource.getInformation("456")
                    }
                }
            }

            When("the API returns 401 Unauthorized") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Unauthorized","message":"Invalid token"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw Unauthorized") {
                    shouldThrow<TwitchApiException.Unauthorized> {
                        resource.getInformation("456")
                    }
                }
            }
        }

        Given("update error paths") {

            When("the API returns 400 Bad Request") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Bad Request","message":"Invalid request"}""",
                            status = HttpStatusCode.BadRequest,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw BadRequest") {
                    shouldThrow<TwitchApiException.BadRequest> {
                        resource.update("456", UpdateChannelRequest(title = "New Title"))
                    }
                }
            }

            When("the scope validation fails") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val httpClient =
                    HttpClient(engine) {
                        install(ContentNegotiation) {
                            json(Json { ignoreUnknownKeys = true })
                        }
                    }
                val config =
                    TwitchKtConfig(
                        clientId = TEST_CLIENT_ID,
                        tokenProvider = TokenProvider { TEST_TOKEN },
                        scopeProvider = ScopeProvider { setOf(TwitchScope.BITS_READ) },
                    )
                val resource = ChannelResource(HelixHttpClient(httpClient, config))

                Then("it should throw MissingScope") {
                    shouldThrow<TwitchApiException.MissingScope> {
                        resource.update("456", UpdateChannelRequest(title = "New Title"))
                    }
                }
            }
        }

        Given("getFollowedChannels error paths") {

            When("the API returns 401 Unauthorized") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Unauthorized","message":"Invalid token"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw Unauthorized") {
                    shouldThrow<TwitchApiException.Unauthorized> {
                        resource.getFollowedChannels(userId = "123")
                    }
                }
            }
        }

        Given("getAllFollowedChannels error paths") {

            When("the API returns 500 on the first page") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Internal Server Error","message":"Something went wrong"}""",
                            status = HttpStatusCode.InternalServerError,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw ServerError when collecting the flow") {
                    shouldThrow<TwitchApiException.ServerError> {
                        resource.getAllFollowedChannels(userId = "123").toList()
                    }
                }
            }
        }
    })
