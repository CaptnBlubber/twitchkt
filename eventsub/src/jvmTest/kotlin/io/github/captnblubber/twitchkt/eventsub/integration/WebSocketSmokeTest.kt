package io.github.captnblubber.twitchkt.eventsub.integration

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.withTimeout

class WebSocketSmokeTest :
    FunSpec({

        val enabled = isIntegrationEnabled()

        test("CIO WebSocket client can connect and receive welcome message").config(
            enabledIf = { enabled },
        ) {
            val httpClient =
                HttpClient(CIO) {
                    install(WebSockets)
                }
            try {
                httpClient.webSocket("ws://localhost:8080/ws") {
                    val frame =
                        withTimeout(5_000) {
                            incoming.receive()
                        }
                    val text = (frame as Frame.Text).readText()
                    text shouldContain "session_welcome"
                }
            } finally {
                httpClient.close()
            }
        }
    })
