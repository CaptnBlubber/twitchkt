package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.model.BitsLeaderboardPeriod
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class BitsResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = BitsResource(createHelixClient(engine))

        Given("getLeaderboard") {

            When("called with default parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "cheerer",
                                            "user_name": "Cheerer",
                                            "rank": 1,
                                            "score": 12543
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
                val entries = resource.getLeaderboard()

                Then("it should call the bits/leaderboard endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/bits/leaderboard"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the default count parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["count"] shouldBe "10"
                }

                Then("it should pass the default period parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["period"] shouldBe "all"
                }

                Then("it should not include optional parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["started_at"].shouldBeNull()
                    request.url.parameters["user_id"].shouldBeNull()
                }

                Then("it should deserialize the leaderboard entry") {
                    entries.size shouldBe 1
                    entries.first().userId shouldBe "456"
                    entries.first().userLogin shouldBe "cheerer"
                    entries.first().userName shouldBe "Cheerer"
                    entries.first().rank shouldBe 1
                    entries.first().score shouldBe 12543
                }
            }

            When("called with a specific period") {
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
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getLeaderboard(period = BitsLeaderboardPeriod.WEEK)

                Then("it should pass the period value as a query parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["period"] shouldBe "week"
                }
            }

            When("called with all optional parameters") {
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
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getLeaderboard(
                    count = 5,
                    period = BitsLeaderboardPeriod.MONTH,
                    startedAt = "2023-06-01T00:00:00Z",
                    userId = "789",
                )

                Then("it should pass all parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["count"] shouldBe "5"
                    request.url.parameters["period"] shouldBe "month"
                    request.url.parameters["started_at"] shouldBe "2023-06-01T00:00:00Z"
                    request.url.parameters["user_id"] shouldBe "789"
                }
            }
        }

        Given("getCheermotes") {

            When("called without a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "prefix": "Cheer",
                                            "tiers": [
                                                {
                                                    "min_bits": 1,
                                                    "id": "1",
                                                    "color": "#979797",
                                                    "images": {
                                                        "dark": {
                                                            "animated": {
                                                                "1": "https://d3aqoihi2n8q8.cloudfront.net/actions/cheer/dark/animated/1/1.gif"
                                                            },
                                                            "static": {
                                                                "1": "https://d3aqoihi2n8q8.cloudfront.net/actions/cheer/dark/static/1/1.png"
                                                            }
                                                        }
                                                    },
                                                    "can_cheer": true,
                                                    "show_in_bits_card": true
                                                }
                                            ],
                                            "type": "global_first_party",
                                            "order": 1,
                                            "last_updated": "2018-05-22T00:06:04Z",
                                            "is_charitable": false
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
                val cheermotes = resource.getCheermotes()

                Then("it should call the bits/cheermotes endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/bits/cheermotes"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should not include the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"].shouldBeNull()
                }

                Then("it should deserialize the cheermote") {
                    cheermotes.size shouldBe 1
                    cheermotes.first().prefix shouldBe "Cheer"
                    cheermotes.first().type shouldBe "global_first_party"
                    cheermotes.first().order shouldBe 1
                    cheermotes.first().isCharitable shouldBe false
                }

                Then("it should deserialize the cheermote tier") {
                    val tier = cheermotes.first().tiers.first()
                    tier.minBits shouldBe 1
                    tier.id shouldBe "1"
                    tier.color shouldBe "#979797"
                    tier.canCheer shouldBe true
                    tier.showInBitsCard shouldBe true
                }
            }

            When("called with a broadcaster ID") {
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
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getCheermotes(broadcasterId = "123")

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }
            }
        }
    })
