package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList

class SubscriptionResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = SubscriptionResource(createHelixClient(engine))

        val firstPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "gifter_id": "",
                        "gifter_login": "",
                        "gifter_name": "",
                        "is_gift": false,
                        "plan_name": "Tier 1",
                        "tier": "1000",
                        "user_id": "456",
                        "user_login": "subscriber",
                        "user_name": "Subscriber"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val lastPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "gifter_id": "",
                        "gifter_login": "",
                        "gifter_name": "",
                        "is_gift": false,
                        "plan_name": "Tier 1",
                        "tier": "1000",
                        "user_id": "789",
                        "user_login": "lastsub",
                        "user_name": "LastSub"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val secondPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "gifter_id": "",
                        "gifter_login": "",
                        "gifter_name": "",
                        "is_gift": false,
                        "plan_name": "Tier 1",
                        "tier": "1000",
                        "user_id": "456",
                        "user_login": "subscriber",
                        "user_name": "Subscriber"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("getAll") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = lastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val subscriptions = resource.getAll(broadcasterId = "123").toList()

                Then("it should call the subscriptions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/subscriptions"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the subscriber") {
                    subscriptions.size shouldBe 1
                    subscriptions.first().userId shouldBe "789"
                    subscriptions.first().userLogin shouldBe "lastsub"
                }
            }
        }

        Given("get") {

            When("called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = firstPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(broadcasterId = "123")

                Then("it should call the subscriptions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/subscriptions"
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the subscription data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = secondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(broadcasterId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = lastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.get(broadcasterId = "123")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("called with userIds filter") {
                val engine =
                    MockEngine {
                        respond(
                            content = firstPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.get(broadcasterId = "123", userIds = listOf("456", "789"))

                Then("it should pass the user_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("user_id") shouldBe listOf("456", "789")
                }
            }

            When("called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = firstPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.get(broadcasterId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        Given("checkUserSubscription") {

            When("the user is subscribed") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "123",
                                            "broadcaster_login": "streamer",
                                            "broadcaster_name": "Streamer",
                                            "gifter_id": "",
                                            "gifter_login": "",
                                            "gifter_name": "",
                                            "is_gift": false,
                                            "plan_name": "Tier 1",
                                            "tier": "1000",
                                            "user_id": "456",
                                            "user_login": "subscriber",
                                            "user_name": "Subscriber"
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val subscription = resource.checkUserSubscription(broadcasterId = "123", userId = "456")

                Then("it should call the subscriptions/user endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/subscriptions/user"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }

                Then("it should return the subscription") {
                    subscription.shouldNotBeNull()
                    subscription.userId shouldBe "456"
                    subscription.tier shouldBe "1000"
                }
            }

            When("the user is not subscribed (404)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Not Found","message":"user not found"}""",
                            status = HttpStatusCode.NotFound,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val subscription = resource.checkUserSubscription(broadcasterId = "123", userId = "456")

                Then("it should return null") {
                    subscription.shouldBeNull()
                }
            }
        }
    })
