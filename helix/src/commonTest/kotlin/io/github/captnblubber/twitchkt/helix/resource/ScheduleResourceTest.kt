package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class ScheduleResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = ScheduleResource(createHelixClient(engine))

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
                                            "segments": [
                                                {
                                                    "id": "seg-1",
                                                    "start_time": "2023-04-11T20:00:00Z",
                                                    "end_time": "2023-04-11T23:00:00Z",
                                                    "title": "Stream Title",
                                                    "is_recurring": true,
                                                    "category": {
                                                        "id": "509658",
                                                        "name": "Just Chatting"
                                                    }
                                                }
                                            ],
                                            "broadcaster_id": "123",
                                            "broadcaster_name": "TestStreamer",
                                            "broadcaster_login": "teststreamer",
                                            "vacation": null
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
                val schedule = resource.getSchedule(broadcasterId = "123")

                Then("it should call the schedule endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/schedule"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the schedule") {
                    schedule.broadcasterId shouldBe "123"
                    schedule.broadcasterName shouldBe "TestStreamer"
                    schedule.broadcasterLogin shouldBe "teststreamer"
                }

                Then("it should deserialize the segments") {
                    schedule.segments!!.size shouldBe 1
                    schedule.segments!!.first().id shouldBe "seg-1"
                    schedule.segments!!.first().title shouldBe "Stream Title"
                    schedule.segments!!.first().isRecurring shouldBe true
                    schedule.segments!!
                        .first()
                        .category!!
                        .id shouldBe "509658"
                    schedule.segments!!
                        .first()
                        .category!!
                        .name shouldBe "Just Chatting"
                }
            }

            When("called with optional parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "segments": null,
                                            "broadcaster_id": "123",
                                            "broadcaster_name": "TestStreamer",
                                            "broadcaster_login": "teststreamer",
                                            "vacation": null
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
                resource.getSchedule(
                    broadcasterId = "123",
                    ids = listOf("seg-1", "seg-2"),
                    startTime = "2023-04-11T00:00:00Z",
                    first = 10,
                    after = "cursor123",
                )

                Then("it should pass the id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("seg-1", "seg-2")
                }

                Then("it should pass the start_time parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["start_time"] shouldBe "2023-04-11T00:00:00Z"
                }

                Then("it should pass the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "10"
                }

                Then("it should pass the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "cursor123"
                }
            }
        }

        Given("updateSettings") {

            When("called with vacation parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.updateSettings(
                    broadcasterId = "123",
                    isVacationEnabled = true,
                    vacationStartTime = "2023-06-01T00:00:00Z",
                    vacationEndTime = "2023-06-15T00:00:00Z",
                    timezone = "America/New_York",
                )

                Then("it should call the schedule/settings endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/schedule/settings"
                }

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the is_vacation_enabled parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["is_vacation_enabled"] shouldBe "true"
                }

                Then("it should pass the vacation_start_time parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["vacation_start_time"] shouldBe "2023-06-01T00:00:00Z"
                }

                Then("it should pass the timezone parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["timezone"] shouldBe "America/New_York"
                }
            }
        }

        Given("createSegment") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.createSegment(
                    broadcasterId = "123",
                    startTime = "2023-04-15T20:00:00Z",
                    timezone = "America/New_York",
                )

                Then("it should call the schedule/segment endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/schedule/segment"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        Given("updateSegment") {

            When("called with a segment ID and title") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.updateSegment(
                    broadcasterId = "123",
                    segmentId = "seg-1",
                    title = "Updated Title",
                )

                Then("it should call the schedule/segment endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/schedule/segment"
                }

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "seg-1"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        Given("deleteSegment") {

            When("called with broadcaster ID and segment ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.deleteSegment(
                    broadcasterId = "123",
                    segmentId = "seg-1",
                )

                Then("it should call the schedule/segment endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/schedule/segment"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "seg-1"
                }
            }
        }
    })
