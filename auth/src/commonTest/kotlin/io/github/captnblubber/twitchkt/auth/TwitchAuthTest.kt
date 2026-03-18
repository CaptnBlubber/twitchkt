package io.github.captnblubber.twitchkt.auth

import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.decodeURLPart
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TwitchAuthTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testClientId = "test-client-id"
        val testClientSecret = "test-client-secret"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createAuth(engine: MockEngine): TwitchAuth {
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
            return TwitchAuth(
                httpClient = httpClient,
                clientId = testClientId,
                clientSecret = testClientSecret,
            )
        }

        val noOpEngine = MockEngine { respond("", HttpStatusCode.OK) }

        // --- authorizationUrl ---

        Given("scopes and a redirect URI") {
            val auth = createAuth(noOpEngine)
            val scopes = setOf(TwitchScope.CHAT_READ, TwitchScope.CHANNEL_READ_SUBSCRIPTIONS)
            val redirectUri = "http://localhost:3000/callback"

            When("generating the authorization URL") {
                val url = auth.authorizationUrl(scopes = scopes, redirectUri = redirectUri)

                Then("it should start with the authorize endpoint") {
                    url shouldStartWith "https://id.twitch.tv/oauth2/authorize"
                }

                Then("it should contain the client_id") {
                    url shouldContain "client_id=$testClientId"
                }

                Then("it should contain the URL-encoded redirect_uri") {
                    url shouldContain "redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback"
                }

                Then("it should contain response_type=code") {
                    url shouldContain "response_type=code"
                }

                Then("it should contain both scopes") {
                    val scopeParam = url.substringAfter("scope=").substringBefore("&")
                    val decodedScopes = scopeParam.decodeURLPart()
                    decodedScopes.split(" ") shouldContainExactlyInAnyOrder
                        listOf("chat:read", "channel:read:subscriptions")
                }
            }
        }

        Given("a state parameter") {
            val auth = createAuth(noOpEngine)

            When("generating the authorization URL with state") {
                val url =
                    auth.authorizationUrl(
                        scopes = setOf(TwitchScope.CHAT_READ),
                        redirectUri = "http://localhost:3000/callback",
                        state = "random-csrf-token",
                    )

                Then("it should contain the state parameter") {
                    url shouldContain "state=random-csrf-token"
                }
            }
        }

        Given("no state parameter") {
            val auth = createAuth(noOpEngine)

            When("generating the authorization URL without state") {
                val url =
                    auth.authorizationUrl(
                        scopes = setOf(TwitchScope.CHAT_READ),
                        redirectUri = "http://localhost:3000/callback",
                    )

                Then("it should not contain state in the URL") {
                    url shouldNotContain "state="
                }
            }
        }

        // --- exchangeCode ---

        Given("a valid authorization code") {

            When("exchanging the code for tokens") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{
                            "access_token": "access123",
                            "refresh_token": "refresh456",
                            "expires_in": 14400,
                            "scope": ["chat:read", "channel:read:subscriptions"],
                            "token_type": "bearer"
                        }""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val auth = createAuth(engine)
                val result = auth.exchangeCode("auth-code-789", "http://localhost:3000/callback")

                Then("it should POST to the token endpoint") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                    request.url.toString() shouldContain "/oauth2/token"
                }

                Then("it should send a form-encoded body") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldContain "x-www-form-urlencoded"
                }

                Then("it should return the token response") {
                    result.accessToken shouldBe "access123"
                    result.refreshToken shouldBe "refresh456"
                    result.expiresIn shouldBe 14400
                    result.scopes shouldBe listOf("chat:read", "channel:read:subscriptions")
                    result.tokenType shouldBe "bearer"
                }
            }
        }

        Given("an invalid authorization code") {

            When("the server rejects the code") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"status":400,"message":"Invalid authorization code"}""",
                            status = HttpStatusCode.BadRequest,
                            headers = jsonHeaders,
                        )
                    }
                val auth = createAuth(engine)

                Then("it should throw TwitchApiException.BadRequest") {
                    shouldThrow<TwitchApiException.BadRequest> {
                        auth.exchangeCode("bad-code", "http://localhost:3000/callback")
                    }
                }
            }
        }

        // --- refresh ---

        Given("a valid refresh token") {

            When("refreshing the token") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{
                            "access_token": "new-access-token",
                            "refresh_token": "new-refresh-token",
                            "expires_in": 14400,
                            "scope": ["chat:read"],
                            "token_type": "bearer"
                        }""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val auth = createAuth(engine)
                val result = auth.refresh("old-refresh-token")

                Then("it should POST to the token endpoint") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                    request.url.toString() shouldContain "/oauth2/token"
                }

                Then("it should send a form-encoded body") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldContain "x-www-form-urlencoded"
                }

                Then("it should return the new token response") {
                    result.accessToken shouldBe "new-access-token"
                    result.refreshToken shouldBe "new-refresh-token"
                    result.expiresIn shouldBe 14400
                }
            }
        }

        Given("an invalid refresh token") {

            When("the server rejects the refresh") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"status":401,"message":"Invalid refresh token"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = jsonHeaders,
                        )
                    }
                val auth = createAuth(engine)

                Then("it should throw TwitchApiException.Unauthorized") {
                    shouldThrow<TwitchApiException.Unauthorized> {
                        auth.refresh("expired-token")
                    }
                }
            }
        }

        // --- validate ---

        Given("a valid access token") {

            When("validating the token") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{
                            "client_id": "$testClientId",
                            "login": "testuser",
                            "user_id": "12345",
                            "scopes": ["chat:read", "channel:read:subscriptions"],
                            "expires_in": 7200
                        }""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val auth = createAuth(engine)
                val result = auth.validate("valid-access-token")

                Then("it should send a GET request to the validate endpoint") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                    request.url.toString() shouldContain "/oauth2/validate"
                }

                Then("it should use the OAuth authorization header") {
                    val request = engine.requestHistory.first()
                    request.headers["Authorization"] shouldBe "OAuth valid-access-token"
                }

                Then("it should return the validation response") {
                    result.clientId shouldBe testClientId
                    result.login shouldBe "testuser"
                    result.userId shouldBe "12345"
                    result.scopes shouldBe listOf("chat:read", "channel:read:subscriptions")
                    result.expiresIn shouldBe 7200
                }
            }
        }

        Given("an expired access token") {

            When("the server rejects validation") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"status":401,"message":"invalid access token"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = jsonHeaders,
                        )
                    }
                val auth = createAuth(engine)

                Then("it should throw TwitchApiException.Unauthorized") {
                    shouldThrow<TwitchApiException.Unauthorized> {
                        auth.validate("expired-token")
                    }
                }
            }
        }
    })
