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

class ChatResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): ChatResource {
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
            return ChatResource(HelixHttpClient(httpClient, config))
        }

        Given("getChatters") {

            When("called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "chatter",
                                            "user_name": "Chatter"
                                        }
                                    ],
                                    "pagination": {
                                        "cursor": "abc123"
                                    }
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getChatters(broadcasterId = "123", moderatorId = "456")

                Then("it should call the chat/chatters endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/chatters"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should deserialize the chatter") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                    page.data.first().userLogin shouldBe "chatter"
                    page.data.first().userName shouldBe "Chatter"
                }

                Then("it should return a non-null cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("called with a cursor (subsequent page)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "chatter",
                                            "user_name": "Chatter"
                                        }
                                    ],
                                    "pagination": {
                                        "cursor": "def456"
                                    }
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getChatters(broadcasterId = "123", moderatorId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the response cursor") {
                    page.cursor shouldBe "def456"
                }
            }

            When("the response has no pagination cursor (last page)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "789",
                                            "user_login": "lastchatter",
                                            "user_name": "LastChatter"
                                        }
                                    ],
                                    "pagination": {}
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getChatters(broadcasterId = "123", moderatorId = "456")

                Then("it should return the chatter data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "789"
                }

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [],
                                    "pagination": {}
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.getChatters(broadcasterId = "123", moderatorId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        Given("getAllChatters") {

            When("called with broadcaster and moderator IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "chatter",
                                            "user_name": "Chatter"
                                        }
                                    ],
                                    "pagination": {}
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val chatters = resource.getAllChatters(broadcasterId = "123", moderatorId = "456").toList()

                Then("it should call the chat/chatters endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/chatters"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should deserialize the chatter") {
                    chatters.size shouldBe 1
                    chatters.first().userId shouldBe "456"
                    chatters.first().userLogin shouldBe "chatter"
                    chatters.first().userName shouldBe "Chatter"
                }
            }
        }
    })
