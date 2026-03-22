package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class AdResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = AdResource(createHelixClient(engine))

        Given("getSchedule") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "next_ad_at": 1704067200,
                                            "last_ad_at": 1704063600,
                                            "duration": 60,
                                            "preroll_free_time": 300,
                                            "snooze_count": 3,
                                            "snooze_refresh_at": 1704070800
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val schedule = resource.getSchedule(broadcasterId = "123")

                Then("it should call the channels/ads endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/ads"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the ad schedule") {
                    schedule.duration shouldBe 60
                    schedule.prerollFreeTime shouldBe 300
                    schedule.snoozeCount shouldBe 3
                }
            }
        }

        Given("startCommercial") {

            When("called with a broadcaster ID and duration") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "length": 60,
                                            "message": "",
                                            "retry_after": 480
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val commercial = resource.startCommercial(broadcasterId = "123", duration = 60)

                Then("it should call the channels/commercial endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/commercial"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the commercial response") {
                    commercial.length shouldBe 60
                    commercial.message shouldBe ""
                    commercial.retryAfter shouldBe 480
                }
            }
        }

        Given("snoozeNextAd") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "next_ad_at": 1704067500,
                                            "last_ad_at": 1704063600,
                                            "duration": 60,
                                            "preroll_free_time": 300,
                                            "snooze_count": 2,
                                            "snooze_refresh_at": 1704071100
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val schedule = resource.snoozeNextAd(broadcasterId = "456")

                Then("it should call the channels/ads/schedule/snooze endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/ads/schedule/snooze"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }

                Then("it should deserialize the updated ad schedule") {
                    schedule.duration shouldBe 60
                    schedule.prerollFreeTime shouldBe 300
                    schedule.snoozeCount shouldBe 2
                }
            }
        }
    })
