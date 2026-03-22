package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.model.CreatePollChoice
import io.github.captnblubber.twitchkt.helix.model.CreatePollRequest
import io.github.captnblubber.twitchkt.helix.model.PollEndStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class PollResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = PollResource(createHelixClient(engine))

        val pollJson =
            """
            {
                "data": [
                    {
                        "id": "poll-1",
                        "broadcaster_id": "123",
                        "broadcaster_name": "Streamer",
                        "broadcaster_login": "streamer",
                        "title": "What game next?",
                        "choices": [
                            {
                                "id": "choice-1",
                                "title": "Minecraft",
                                "votes": 10,
                                "channel_points_votes": 5,
                                "bits_votes": 0
                            },
                            {
                                "id": "choice-2",
                                "title": "Fortnite",
                                "votes": 20,
                                "channel_points_votes": 8,
                                "bits_votes": 0
                            }
                        ],
                        "bits_voting_enabled": false,
                        "bits_per_vote": 0,
                        "channel_points_voting_enabled": true,
                        "channel_points_per_vote": 100,
                        "status": "ACTIVE",
                        "duration": 300,
                        "started_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        Given("getPolls") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = pollJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val polls = resource.getPolls(broadcasterId = "123")

                Then("it should call the polls endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/polls"
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

                Then("it should deserialize the poll") {
                    polls.size shouldBe 1
                    polls.first().id shouldBe "poll-1"
                    polls.first().broadcasterId shouldBe "123"
                    polls.first().title shouldBe "What game next?"
                    polls.first().status.name shouldBe "ACTIVE"
                    polls.first().duration shouldBe 300
                }

                Then("it should deserialize the choices") {
                    polls.first().choices.size shouldBe 2
                    polls.first().choices[0].id shouldBe "choice-1"
                    polls.first().choices[0].title shouldBe "Minecraft"
                    polls.first().choices[0].votes shouldBe 10
                    polls.first().choices[1].id shouldBe "choice-2"
                    polls.first().choices[1].title shouldBe "Fortnite"
                    polls.first().choices[1].votes shouldBe 20
                }
            }

            When("called with poll IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = pollJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getPolls(broadcasterId = "123", ids = listOf("poll-1", "poll-2"))

                Then("it should pass the id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("poll-1", "poll-2")
                }
            }

            When("called with a custom first value") {
                val engine =
                    MockEngine {
                        respond(
                            content = pollJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getPolls(broadcasterId = "123", first = 5)

                Then("it should pass the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "5"
                }
            }

            When("called with an after cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = pollJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getPolls(broadcasterId = "123", after = "cursor123")

                Then("it should pass the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "cursor123"
                }
            }
        }

        Given("create") {

            When("called with a poll request") {
                val engine =
                    MockEngine {
                        respond(
                            content = pollJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val request =
                    CreatePollRequest(
                        broadcasterId = "123",
                        title = "What game next?",
                        choices = listOf(CreatePollChoice("Minecraft"), CreatePollChoice("Fortnite")),
                        duration = 300,
                    )
                val poll = resource.create(request)

                Then("it should call the polls endpoint") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.encodedPath shouldBe "/helix/polls"
                }

                Then("it should use POST method") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the created poll") {
                    poll.id shouldBe "poll-1"
                    poll.title shouldBe "What game next?"
                    poll.broadcasterId shouldBe "123"
                }
            }
        }

        Given("end") {

            When("called with TERMINATED status") {
                val terminatedPollJson =
                    """
                    {
                        "data": [
                            {
                                "id": "poll-1",
                                "broadcaster_id": "123",
                                "broadcaster_name": "Streamer",
                                "broadcaster_login": "streamer",
                                "title": "What game next?",
                                "choices": [
                                    {
                                        "id": "choice-1",
                                        "title": "Minecraft",
                                        "votes": 10,
                                        "channel_points_votes": 5,
                                        "bits_votes": 0
                                    }
                                ],
                                "bits_voting_enabled": false,
                                "bits_per_vote": 0,
                                "channel_points_voting_enabled": false,
                                "channel_points_per_vote": 0,
                                "status": "TERMINATED",
                                "duration": 300,
                                "started_at": "2024-01-01T00:00:00Z",
                                "ended_at": "2024-01-01T00:03:00Z"
                            }
                        ],
                        "pagination": {}
                    }
                    """.trimIndent()
                val engine =
                    MockEngine {
                        respond(
                            content = terminatedPollJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val poll = resource.end(broadcasterId = "123", pollId = "poll-1", status = PollEndStatus.TERMINATED)

                Then("it should call the polls endpoint") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.url.encodedPath shouldBe "/helix/polls"
                }

                Then("it should use PATCH method") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.method shouldBe HttpMethod.Patch
                }

                Then("it should set Content-Type to application/json") {
                    val httpRequest = engine.requestHistory.first()
                    httpRequest.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the ended poll") {
                    poll.id shouldBe "poll-1"
                    poll.status.name shouldBe "TERMINATED"
                    poll.endedAt.toString() shouldBe "2024-01-01T00:03:00Z"
                }
            }
        }
    })
