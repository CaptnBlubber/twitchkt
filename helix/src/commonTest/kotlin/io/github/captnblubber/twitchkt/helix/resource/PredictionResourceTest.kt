package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.model.PredictionEndStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class PredictionResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = PredictionResource(createHelixClient(engine))

        val predictionJson =
            """
            {
                "data": [
                    {
                        "id": "pred-1",
                        "broadcaster_id": "123",
                        "broadcaster_name": "Streamer",
                        "broadcaster_login": "streamer",
                        "title": "Will I win?",
                        "winning_outcome_id": null,
                        "outcomes": [
                            {
                                "id": "outcome-1",
                                "title": "Yes",
                                "users": 10,
                                "channel_points": 5000,
                                "top_predictors": [
                                    {
                                        "user_id": "user-1",
                                        "user_login": "viewer1",
                                        "user_name": "Viewer1",
                                        "channel_points_used": 1000,
                                        "channel_points_won": 0
                                    }
                                ],
                                "color": "BLUE"
                            },
                            {
                                "id": "outcome-2",
                                "title": "No",
                                "users": 5,
                                "channel_points": 2000,
                                "top_predictors": null,
                                "color": "PINK"
                            }
                        ],
                        "prediction_window": 120,
                        "status": "ACTIVE",
                        "created_at": "2024-01-01T00:00:00Z",
                        "ended_at": null,
                        "locked_at": null
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        Given("list") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = predictionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val predictions = resource.list(broadcasterId = "123")

                Then("it should call the predictions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/predictions"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the first parameter with default value") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "20"
                }

                Then("it should deserialize the prediction") {
                    predictions.size shouldBe 1
                    predictions.first().id shouldBe "pred-1"
                    predictions.first().broadcasterId shouldBe "123"
                    predictions.first().title shouldBe "Will I win?"
                    predictions.first().status shouldBe "ACTIVE"
                    predictions.first().predictionWindow shouldBe 120
                }

                Then("it should deserialize the outcomes") {
                    predictions.first().outcomes.size shouldBe 2
                    predictions.first().outcomes[0].id shouldBe "outcome-1"
                    predictions.first().outcomes[0].title shouldBe "Yes"
                    predictions.first().outcomes[0].users shouldBe 10
                    predictions.first().outcomes[0].channelPoints shouldBe 5000
                    predictions.first().outcomes[0].color shouldBe "BLUE"
                    predictions.first().outcomes[1].id shouldBe "outcome-2"
                    predictions.first().outcomes[1].title shouldBe "No"
                    predictions.first().outcomes[1].color shouldBe "PINK"
                }

                Then("it should deserialize top predictors") {
                    val topPredictors = predictions.first().outcomes[0].topPredictors
                    topPredictors?.size shouldBe 1
                    topPredictors?.first()?.userId shouldBe "user-1"
                    topPredictors?.first()?.userLogin shouldBe "viewer1"
                    topPredictors?.first()?.channelPointsUsed shouldBe 1000
                }

                Then("winning outcome should be null for active prediction") {
                    predictions.first().winningOutcomeId.shouldBeNull()
                }

                Then("ended_at should be null for active prediction") {
                    predictions.first().endedAt.shouldBeNull()
                }
            }

            When("called with prediction IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = predictionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.list(broadcasterId = "123", ids = listOf("pred-1", "pred-2"))

                Then("it should pass the id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("pred-1", "pred-2")
                }
            }

            When("called with a custom first value") {
                val engine =
                    MockEngine {
                        respond(
                            content = predictionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.list(broadcasterId = "123", first = 10)

                Then("it should pass the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "10"
                }
            }

            When("called with an after cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = predictionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.list(broadcasterId = "123", after = "cursor456")

                Then("it should pass the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "cursor456"
                }
            }
        }

        Given("create") {

            When("called with prediction parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = predictionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val prediction =
                    resource.create(
                        broadcasterId = "123",
                        title = "Will I win?",
                        outcomes = listOf("Yes", "No"),
                        predictionWindow = 120,
                    )

                Then("it should call the predictions endpoint") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.encodedPath shouldBe "/helix/predictions"
                }

                Then("it should use POST method") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the created prediction") {
                    prediction.id shouldBe "pred-1"
                    prediction.title shouldBe "Will I win?"
                    prediction.broadcasterId shouldBe "123"
                }
            }
        }

        Given("end") {

            When("called with RESOLVED status and winning outcome") {
                val resolvedJson =
                    """
                    {
                        "data": [
                            {
                                "id": "pred-1",
                                "broadcaster_id": "123",
                                "broadcaster_name": "Streamer",
                                "broadcaster_login": "streamer",
                                "title": "Will I win?",
                                "winning_outcome_id": "outcome-1",
                                "outcomes": [
                                    {
                                        "id": "outcome-1",
                                        "title": "Yes",
                                        "users": 10,
                                        "channel_points": 5000,
                                        "color": "BLUE"
                                    },
                                    {
                                        "id": "outcome-2",
                                        "title": "No",
                                        "users": 5,
                                        "channel_points": 2000,
                                        "color": "PINK"
                                    }
                                ],
                                "prediction_window": 120,
                                "status": "RESOLVED",
                                "created_at": "2024-01-01T00:00:00Z",
                                "ended_at": "2024-01-01T00:02:00Z",
                                "locked_at": "2024-01-01T00:01:00Z"
                            }
                        ],
                        "pagination": {}
                    }
                    """.trimIndent()
                val engine =
                    MockEngine {
                        respond(
                            content = resolvedJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val prediction =
                    resource.end(
                        broadcasterId = "123",
                        predictionId = "pred-1",
                        status = PredictionEndStatus.RESOLVED,
                        winningOutcomeId = "outcome-1",
                    )

                Then("it should call the predictions endpoint") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.encodedPath shouldBe "/helix/predictions"
                }

                Then("it should use PATCH method") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.method shouldBe HttpMethod.Patch
                }

                Then("it should set Content-Type to application/json") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the resolved prediction") {
                    prediction.id shouldBe "pred-1"
                    prediction.status shouldBe "RESOLVED"
                    prediction.winningOutcomeId shouldBe "outcome-1"
                    prediction.endedAt.toString() shouldBe "2024-01-01T00:02:00Z"
                }
            }

            When("called with CANCELED status") {
                val canceledJson =
                    """
                    {
                        "data": [
                            {
                                "id": "pred-1",
                                "broadcaster_id": "123",
                                "broadcaster_name": "Streamer",
                                "broadcaster_login": "streamer",
                                "title": "Will I win?",
                                "winning_outcome_id": null,
                                "outcomes": [
                                    {
                                        "id": "outcome-1",
                                        "title": "Yes",
                                        "users": 0,
                                        "channel_points": 0,
                                        "color": "BLUE"
                                    }
                                ],
                                "prediction_window": 120,
                                "status": "CANCELED",
                                "created_at": "2024-01-01T00:00:00Z",
                                "ended_at": "2024-01-01T00:01:00Z"
                            }
                        ],
                        "pagination": {}
                    }
                    """.trimIndent()
                val engine =
                    MockEngine {
                        respond(
                            content = canceledJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val prediction =
                    resource.end(
                        broadcasterId = "123",
                        predictionId = "pred-1",
                        status = PredictionEndStatus.CANCELED,
                    )

                Then("it should deserialize the canceled prediction") {
                    prediction.id shouldBe "pred-1"
                    prediction.status shouldBe "CANCELED"
                    prediction.winningOutcomeId.shouldBeNull()
                }
            }
        }
    })
