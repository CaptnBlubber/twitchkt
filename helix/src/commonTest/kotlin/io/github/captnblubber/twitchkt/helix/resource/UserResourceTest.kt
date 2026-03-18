package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
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

class UserResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): UserResource {
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
            return UserResource(HelixHttpClient(httpClient, config))
        }

        Given("getUsers") {

            When("called with user IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "123",
                                            "login": "testuser",
                                            "display_name": "TestUser",
                                            "type": "",
                                            "broadcaster_type": "affiliate",
                                            "description": "A test user",
                                            "profile_image_url": "https://example.com/img.png",
                                            "offline_image_url": "",
                                            "created_at": "2020-01-01T00:00:00Z"
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val users = resource.getUsers(ids = listOf("123"))

                Then("it should call the users endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/users"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "123"
                }

                Then("it should deserialize the user") {
                    users shouldHaveSize 1
                    users.first().id shouldBe "123"
                    users.first().login shouldBe "testuser"
                    users.first().displayName shouldBe "TestUser"
                    users.first().broadcasterType shouldBe "affiliate"
                }
            }

            When("called with multiple IDs and logins") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {"id": "1", "login": "user1", "display_name": "User1"},
                                        {"id": "2", "login": "user2", "display_name": "User2"}
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val users = resource.getUsers(ids = listOf("1", "2"), logins = listOf("user3"))

                Then("it should pass all ID parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("1", "2")
                }

                Then("it should pass the login parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["login"] shouldBe "user3"
                }

                Then("it should return all users") {
                    users shouldHaveSize 2
                }
            }

            When("called with no parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {"id": "self", "login": "myself", "display_name": "Myself"}
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                val users = resource.getUsers()

                Then("it should not include any query parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters
                        .names()
                        .filter { it == "id" || it == "login" } shouldHaveSize 0
                }

                Then("it should return the token owner user") {
                    users shouldHaveSize 1
                    users.first().login shouldBe "myself"
                }
            }
        }
    })
