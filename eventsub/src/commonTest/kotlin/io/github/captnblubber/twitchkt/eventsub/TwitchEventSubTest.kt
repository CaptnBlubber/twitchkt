package io.github.captnblubber.twitchkt.eventsub

import io.github.captnblubber.twitchkt.ConnectionState
import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode

class TwitchEventSubTest :
    BehaviorSpec({

        val testConfig =
            TwitchKtConfig(
                clientId = "test-client-id",
                tokenProvider = TokenProvider { "test-token" },
                eventSubUrl = "wss://localhost/ws",
            )

        fun createEventSub(httpClient: HttpClient): TwitchEventSub {
            val helix = TwitchHelix(httpClient, testConfig)
            return TwitchEventSub(httpClient, testConfig, helix.eventSub)
        }

        Given("ConnectionState enum") {

            When("listing all values") {
                val values = ConnectionState.entries

                Then("it should have exactly four states") {
                    values.map { it.name } shouldContainExactly
                        listOf(
                            "DISCONNECTED",
                            "CONNECTING",
                            "CONNECTED",
                            "RECONNECTING",
                        )
                }
            }
        }

        Given("a new TwitchEventSub instance") {
            val mockEngine =
                MockEngine { _ ->
                    respond("", HttpStatusCode.OK)
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(WebSockets)
                }
            val eventSub = createEventSub(httpClient)

            When("checking initial state") {

                Then("connectionState should be DISCONNECTED") {
                    eventSub.connectionState.value shouldBe ConnectionState.DISCONNECTED
                }

                Then("sessionId should be null") {
                    eventSub.sessionId.value.shouldBeNull()
                }
            }

            When("disconnect is called without prior connect") {

                Then("connectionState should remain DISCONNECTED") {
                    eventSub.disconnect()
                    eventSub.connectionState.value shouldBe ConnectionState.DISCONNECTED
                }

                Then("sessionId should remain null") {
                    eventSub.disconnect()
                    eventSub.sessionId.value.shouldBeNull()
                }
            }
        }

        Given("a TwitchEventSub after disconnect") {
            val mockEngine =
                MockEngine { _ ->
                    respond("", HttpStatusCode.OK)
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(WebSockets)
                }
            val eventSub = createEventSub(httpClient)

            When("disconnect is called") {
                eventSub.disconnect()

                Then("connectionState should be DISCONNECTED") {
                    eventSub.connectionState.value shouldBe ConnectionState.DISCONNECTED
                }

                Then("sessionId should be cleared") {
                    eventSub.sessionId.value.shouldBeNull()
                }
            }
        }

        Given("the events SharedFlow") {
            val mockEngine =
                MockEngine { _ ->
                    respond("", HttpStatusCode.OK)
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(WebSockets)
                }
            val eventSub = createEventSub(httpClient)

            When("no events have been emitted") {

                Then("replayCache should be empty") {
                    eventSub.events.replayCache shouldBe emptyList()
                }
            }
        }

        Given("computeBackoff") {

            When("attempt is 0") {

                Then("backoff should be ~1000ms with jitter") {
                    repeat(50) {
                        val backoff = TwitchEventSub.computeBackoff(0)
                        backoff shouldBeGreaterThanOrEqual 800L // 1000 - 20%
                        backoff shouldBeLessThanOrEqual 1200L // 1000 + 20%
                    }
                }
            }

            When("attempt is 3") {

                Then("backoff should be ~8000ms with jitter") {
                    repeat(50) {
                        val backoff = TwitchEventSub.computeBackoff(3)
                        backoff shouldBeGreaterThanOrEqual 6400L // 8000 - 20%
                        backoff shouldBeLessThanOrEqual 9600L // 8000 + 20%
                    }
                }
            }

            When("attempt is very large") {

                Then("backoff should be capped at MAX_BACKOFF_MS with jitter") {
                    repeat(50) {
                        val backoff = TwitchEventSub.computeBackoff(100)
                        backoff shouldBeGreaterThanOrEqual 24_000L // 30000 - 20%
                        backoff shouldBeLessThanOrEqual 36_000L // 30000 + 20%
                    }
                }
            }

            When("called many times") {

                Then("backoff values should not all be identical (jitter is applied)") {
                    val values = (1..20).map { TwitchEventSub.computeBackoff(2) }.toSet()
                    // With 20 samples and jitter, we should get more than 1 distinct value
                    (values.size > 1) shouldBe true
                }
            }
        }
    })
