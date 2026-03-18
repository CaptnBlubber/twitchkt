package io.github.captnblubber.twitchkt.helix.integration

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotBeBlank
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class HelixIntegrationTest :
    FunSpec({

        val enabled = isIntegrationEnabled()

        test("getStreams returns live streams from mock-api").config(
            enabledIf = { enabled },
        ) {
            val httpClient =
                HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
            try {
                val clientId =
                    System.getProperty("mockClientId")
                        ?: error("mockClientId system property not set")
                val clientSecret =
                    System.getProperty("mockClientSecret")
                        ?: error("mockClientSecret system property not set")

                val mockToken = TwitchCliProcess.getMockToken(clientId, clientSecret, httpClient)

                val config =
                    TwitchKtConfig(
                        clientId = clientId,
                        tokenProvider = TokenProvider { mockToken },
                        helixBaseUrl = "http://localhost:8081/mock",
                    )

                val helix = TwitchHelix(httpClient, config)

                val streams = helix.streams.getStreams()
                streams.shouldNotBeEmpty()
                val stream = streams.first()
                stream.id.shouldNotBeBlank()
                stream.userId.shouldNotBeBlank()
                stream.userLogin.shouldNotBeBlank()
            } finally {
                httpClient.close()
            }
        }
    })
