@file:Suppress("DEPRECATION")

package io.github.captnblubber.twitchkt.irc

import io.github.captnblubber.twitchkt.ConnectionState
import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode

class TwitchIrcTest :
    BehaviorSpec({

        val testConfig =
            TwitchKtConfig(
                clientId = "test-client-id",
                tokenProvider = TokenProvider { "test-token" },
                ircUrl = "wss://localhost/irc",
                login = "testuser",
            )

        fun createIrc(): TwitchIrc {
            val mockEngine =
                MockEngine { _ ->
                    respond("", HttpStatusCode.OK)
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(WebSockets)
                }
            return TwitchIrc(httpClient, testConfig)
        }

        Given("a new TwitchIrc instance") {
            val irc = createIrc()

            When("checking initial state") {

                Then("connectionState should be DISCONNECTED") {
                    irc.connectionState.value shouldBe ConnectionState.DISCONNECTED
                }

                Then("messages replayCache should be empty") {
                    irc.messages.replayCache shouldBe emptyList()
                }
            }

            When("disconnect is called without prior connect") {

                Then("connectionState should remain DISCONNECTED") {
                    irc.disconnect()
                    irc.connectionState.value shouldBe ConnectionState.DISCONNECTED
                }
            }
        }

        Given("join and part before connect") {
            val irc = createIrc()

            When("join is called") {

                Then("no exception is thrown") {
                    irc.join("testchannel")
                }
            }

            When("part is called") {

                Then("no exception is thrown") {
                    irc.part("testchannel")
                }
            }
        }
    })
