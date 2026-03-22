package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class GoalResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = GoalResource(createHelixClient(engine))

        Given("getGoals") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "goal-1",
                                            "broadcaster_id": "123",
                                            "broadcaster_name": "Streamer",
                                            "broadcaster_login": "streamer",
                                            "type": "follower",
                                            "description": "Follow goal",
                                            "current_amount": 150,
                                            "target_amount": 500,
                                            "created_at": "2023-01-15T12:00:00Z"
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
                val goals = resource.getGoals(broadcasterId = "123")

                Then("it should call the goals endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/goals"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the goal") {
                    goals.size shouldBe 1
                    goals.first().id shouldBe "goal-1"
                    goals.first().broadcasterId shouldBe "123"
                    goals.first().broadcasterName shouldBe "Streamer"
                    goals.first().broadcasterLogin shouldBe "streamer"
                    goals.first().type shouldBe "follower"
                    goals.first().description shouldBe "Follow goal"
                    goals.first().currentAmount shouldBe 150
                    goals.first().targetAmount shouldBe 500
                    goals.first().createdAt.toString() shouldBe "2023-01-15T12:00:00Z"
                }
            }

            When("the broadcaster has no active goals") {
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
                val goals = resource.getGoals(broadcasterId = "123")

                Then("it should return an empty list") {
                    goals.size shouldBe 0
                }
            }

            When("the broadcaster has multiple goals") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "goal-1",
                                            "broadcaster_id": "123",
                                            "broadcaster_name": "Streamer",
                                            "broadcaster_login": "streamer",
                                            "type": "follower",
                                            "description": "Follow goal",
                                            "current_amount": 150,
                                            "target_amount": 500,
                                            "created_at": "2023-01-15T12:00:00Z"
                                        },
                                        {
                                            "id": "goal-2",
                                            "broadcaster_id": "123",
                                            "broadcaster_name": "Streamer",
                                            "broadcaster_login": "streamer",
                                            "type": "subscription",
                                            "description": "Sub goal",
                                            "current_amount": 50,
                                            "target_amount": 100,
                                            "created_at": "2023-02-01T08:30:00Z"
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
                val goals = resource.getGoals(broadcasterId = "123")

                Then("it should return all goals") {
                    goals.size shouldBe 2
                    goals[0].id shouldBe "goal-1"
                    goals[0].type shouldBe "follower"
                    goals[1].id shouldBe "goal-2"
                    goals[1].type shouldBe "subscription"
                }
            }
        }
    })
