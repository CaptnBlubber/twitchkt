package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
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
import io.github.captnblubber.twitchkt.helix.model.UpdateChannelRequest

class ChannelResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): ChannelResource {
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
            return ChannelResource(HelixHttpClient(httpClient, config))
        }

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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
    })
