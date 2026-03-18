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

class FollowerResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): FollowerResource {
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
            return FollowerResource(HelixHttpClient(httpClient, config))
        }

        Given("listAll") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "follower",
                                            "user_name": "Follower",
                                            "followed_at": "2022-05-24T22:22:08Z"
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
                val followers = resource.listAll(broadcasterId = "123").toList()

                Then("it should call the channels/followers endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/followers"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the follower") {
                    followers.size shouldBe 1
                    followers.first().userId shouldBe "456"
                    followers.first().userLogin shouldBe "follower"
                    followers.first().userName shouldBe "Follower"
                }
            }

            When("called with a userId filter") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "follower",
                                            "user_name": "Follower",
                                            "followed_at": "2022-05-24T22:22:08Z"
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
                resource.listAll(broadcasterId = "123", userId = "456").toList()

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }
            }
        }

        Given("list") {

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
                                            "user_login": "follower",
                                            "user_name": "Follower",
                                            "followed_at": "2022-05-24T22:22:08Z"
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
                val page = resource.list(broadcasterId = "123")

                Then("it should call the channels/followers endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/followers"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the follower data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                    page.data.first().userLogin shouldBe "follower"
                    page.data.first().userName shouldBe "Follower"
                    page.data.first().followedAt.toString() shouldBe "2022-05-24T22:22:08Z"
                }

                Then("it should return the next page cursor") {
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
                                            "user_login": "follower",
                                            "user_name": "Follower",
                                            "followed_at": "2022-05-24T22:22:08Z"
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
                val page = resource.list(broadcasterId = "123", cursor = "abc123")

                Then("it should pass the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the next cursor from the response") {
                    page.cursor shouldBe "def456"
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
                resource.list(broadcasterId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
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
                                            "user_login": "lastfollower",
                                            "user_name": "LastFollower",
                                            "followed_at": "2022-05-24T22:22:08Z"
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
                val page = resource.list(broadcasterId = "123")

                Then("it should return the follower data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "789"
                }

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }
        }
    })
