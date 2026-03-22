package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class HypeTrainResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = HypeTrainResource(createHelixClient(engine))

        Given("getEvents") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "event-1",
                                            "event_type": "hypetrain.progression",
                                            "event_timestamp": "2023-07-15T18:30:00Z",
                                            "version": "1.0",
                                            "event_data": {
                                                "broadcaster_id": "123",
                                                "level": 2,
                                                "total": 500
                                            }
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
                val events = resource.getEvents(broadcasterId = "123")

                Then("it should call the hypetrain/events endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/hypetrain/events"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the default first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "1"
                }

                Then("it should deserialize the event") {
                    events.size shouldBe 1
                    events.first().id shouldBe "event-1"
                    events.first().eventType shouldBe "hypetrain.progression"
                    events.first().eventTimestamp shouldBe "2023-07-15T18:30:00Z"
                    events.first().version shouldBe "1.0"
                    events.first().eventData.shouldNotBeNull()
                }
            }

            When("called with a custom first parameter") {
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
                resource.getEvents(broadcasterId = "123", first = 5)

                Then("it should pass the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "5"
                }
            }

            When("there are no events") {
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
                val events = resource.getEvents(broadcasterId = "123")

                Then("it should return an empty list") {
                    events.size shouldBe 0
                }
            }
        }

        Given("getStatus") {

            When("called with an active hype train") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "current": {
                                                "id": "train-1",
                                                "broadcaster_user_id": "123",
                                                "broadcaster_user_login": "streamer",
                                                "broadcaster_user_name": "Streamer",
                                                "level": 3,
                                                "total": 1500,
                                                "progress": 300,
                                                "goal": 500,
                                                "top_contributions": [
                                                    {
                                                        "user_id": "456",
                                                        "user_login": "contributor",
                                                        "user_name": "Contributor",
                                                        "type": "bits",
                                                        "total": 500
                                                    }
                                                ],
                                                "started_at": "2023-07-15T18:00:00Z",
                                                "expires_at": "2023-07-15T18:30:00Z",
                                                "type": "regular",
                                                "is_shared_train": false
                                            },
                                            "all_time_high": {
                                                "level": 5,
                                                "total": 5000,
                                                "achieved_at": "2023-06-01T20:00:00Z"
                                            }
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
                val status = resource.getStatus(broadcasterId = "123")

                Then("it should call the hypetrain/status endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/hypetrain/status"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the current hype train") {
                    val current = status.current
                    current.shouldNotBeNull()
                    current.id shouldBe "train-1"
                    current.broadcasterUserId shouldBe "123"
                    current.broadcasterUserLogin shouldBe "streamer"
                    current.broadcasterUserName shouldBe "Streamer"
                    current.level shouldBe 3
                    current.total shouldBe 1500
                    current.progress shouldBe 300
                    current.goal shouldBe 500
                    current.type shouldBe "regular"
                    current.isSharedTrain shouldBe false
                }

                Then("it should deserialize the top contributions") {
                    val contributions = status.current!!.topContributions
                    contributions.size shouldBe 1
                    contributions.first().userId shouldBe "456"
                    contributions.first().userLogin shouldBe "contributor"
                    contributions.first().userName shouldBe "Contributor"
                    contributions.first().type shouldBe "bits"
                    contributions.first().total shouldBe 500
                }

                Then("it should deserialize the all-time high record") {
                    val record = status.allTimeHigh
                    record.shouldNotBeNull()
                    record.level shouldBe 5
                    record.total shouldBe 5000
                    record.achievedAt.toString() shouldBe "2023-06-01T20:00:00Z"
                }
            }

            When("called with no active hype train") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "all_time_high": {
                                                "level": 2,
                                                "total": 800,
                                                "achieved_at": "2023-03-10T15:00:00Z"
                                            }
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
                val status = resource.getStatus(broadcasterId = "123")

                Then("current should be null") {
                    status.current.shouldBeNull()
                }

                Then("it should still have the all-time high") {
                    status.allTimeHigh.shouldNotBeNull()
                    status.allTimeHigh.shouldNotBeNull()
                    status.allTimeHigh.level shouldBe 2
                }
            }
        }
    })
