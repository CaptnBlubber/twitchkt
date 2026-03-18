package io.github.captnblubber.twitchkt.helix.internal

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.ScopeProvider
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ScopeValidationTest :
    BehaviorSpec({
        coroutineTestScope = true

        Given("a HelixHttpClient with a ScopeProvider") {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"data":[]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }

            When("the token has the required scope") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.CHANNEL_MANAGE_POLLS, TwitchScope.BITS_READ)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("validateScopes does not throw") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.CHANNEL_MANAGE_POLLS)
                    }
                }
            }

            When("the token is missing the required scope") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.BITS_READ)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("validateScopes throws MissingScope with the missing scope") {
                    val exception =
                        shouldThrow<TwitchApiException.MissingScope> {
                            http.validateScopes(TwitchScope.CHANNEL_MANAGE_POLLS)
                        }
                    exception.missingScopes shouldContainExactly listOf(TwitchScope.CHANNEL_MANAGE_POLLS)
                    exception.message shouldContain "channel:manage:polls"
                }
            }

            When("multiple scopes are required and some are missing") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.BITS_READ)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("validateScopes throws with all missing scopes listed") {
                    val exception =
                        shouldThrow<TwitchApiException.MissingScope> {
                            http.validateScopes(
                                TwitchScope.CHANNEL_MANAGE_POLLS,
                                TwitchScope.CHANNEL_READ_SUBSCRIPTIONS,
                            )
                        }
                    exception.missingScopes shouldBe
                        listOf(
                            TwitchScope.CHANNEL_MANAGE_POLLS,
                            TwitchScope.CHANNEL_READ_SUBSCRIPTIONS,
                        )
                }
            }

            When("no ScopeProvider is configured") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider = null,
                    )
                val http = HelixHttpClient(client, config)

                Then("validateScopes is a no-op") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.CHANNEL_MANAGE_POLLS)
                    }
                }
            }
        }

        Given("scope hierarchy") {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"data":[]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                }
            val client =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
                }

            When("the token has channel:manage:redemptions") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("it satisfies channel:read:redemptions") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.CHANNEL_READ_REDEMPTIONS)
                    }
                }

                Then("it satisfies channel:manage:redemptions itself") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
                    }
                }
            }

            When("the token has channel:manage:moderators") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.CHANNEL_MANAGE_MODERATORS)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("it satisfies moderation:read") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.MODERATION_READ)
                    }
                }
            }

            When("the token has channel:manage:polls") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.CHANNEL_MANAGE_POLLS)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("it satisfies channel:read:polls") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.CHANNEL_READ_POLLS)
                    }
                }

                Then("it does NOT satisfy an unrelated scope like bits:read") {
                    shouldThrow<TwitchApiException.MissingScope> {
                        http.validateScopes(TwitchScope.BITS_READ)
                    }
                }
            }

            When("the token has only channel:read:redemptions (not manage)") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.CHANNEL_READ_REDEMPTIONS)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("it does NOT satisfy channel:manage:redemptions") {
                    shouldThrow<TwitchApiException.MissingScope> {
                        http.validateScopes(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
                    }
                }
            }

            When("the token has moderator:manage:shield_mode") {
                val config =
                    TwitchKtConfig(
                        clientId = "test-client",
                        tokenProvider = { "test-token" },
                        scopeProvider =
                            ScopeProvider {
                                setOf(TwitchScope.MODERATOR_MANAGE_SHIELD_MODE)
                            },
                    )
                val http = HelixHttpClient(client, config)

                Then("it satisfies moderator:read:shield_mode") {
                    shouldNotThrowAny {
                        http.validateScopes(TwitchScope.MODERATOR_READ_SHIELD_MODE)
                    }
                }
            }
        }
    })
