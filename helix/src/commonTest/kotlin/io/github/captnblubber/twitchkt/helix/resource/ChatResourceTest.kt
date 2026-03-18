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

        val userEmoteJson =
            """
            {
                "data": [
                    {
                        "id": "emote-1",
                        "name": "HeyGuys",
                        "emote_type": "globals",
                        "emote_set_id": "0",
                        "owner_id": "456",
                        "format": ["static"],
                        "scale": ["1.0", "2.0"],
                        "theme_mode": ["dark", "light"]
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val userEmoteLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "emote-2",
                        "name": "LUL",
                        "emote_type": "subscriptions",
                        "emote_set_id": "100",
                        "owner_id": "789",
                        "format": ["static", "animated"],
                        "scale": ["1.0"],
                        "theme_mode": ["dark"]
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val userEmoteSecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "emote-1",
                        "name": "HeyGuys",
                        "emote_type": "globals",
                        "emote_set_id": "0",
                        "owner_id": "456",
                        "format": ["static"],
                        "scale": ["1.0", "2.0"],
                        "theme_mode": ["dark", "light"]
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("UserEmotes") {

            When("getAllUserEmotes is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val emotes = resource.getAllUserEmotes(userId = "123").toList()

                Then("it should call the chat/emotes/user endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/emotes/user"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should deserialize the emote") {
                    emotes.size shouldBe 1
                    emotes.first().id shouldBe "emote-2"
                    emotes.first().name shouldBe "LUL"
                    emotes.first().emoteType shouldBe "subscriptions"
                }
            }

            When("getAllUserEmotes is called with a broadcasterId") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.getAllUserEmotes(userId = "123", broadcasterId = "456").toList()

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }
            }

            When("getUserEmotes is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUserEmotes(userId = "123")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the emote data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "emote-1"
                    page.data.first().name shouldBe "HeyGuys"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getUserEmotes is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUserEmotes(userId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getUserEmotes is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUserEmotes(userId = "123")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }
        }
    })
