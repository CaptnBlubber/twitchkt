package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class RaidResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = RaidResource(createHelixClient(engine))

        Given("start") {

            When("called with broadcaster IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "created_at": "2024-01-01T00:00:00Z",
                                            "is_mature": false
                                        }
                                    ],
                                    "pagination": {}
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val raidResponse = resource.start(fromBroadcasterId = "123", toBroadcasterId = "456")

                Then("it should call the raids endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/raids"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the from_broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["from_broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the to_broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["to_broadcaster_id"] shouldBe "456"
                }

                Then("it should deserialize the raid response") {
                    raidResponse.createdAt.toString() shouldBe "2024-01-01T00:00:00Z"
                    raidResponse.isMature shouldBe false
                }
            }
        }

        Given("cancel") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.cancel(broadcasterId = "123")

                Then("it should call the raids endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/raids"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }
            }
        }
    })
