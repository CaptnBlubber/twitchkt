package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.EventSubSubscriptionType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class EventSubResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = EventSubResource(createHelixClient(engine))

        Given("create") {

            When("called with a subscription type and session ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "sub-123",
                                            "status": "enabled",
                                            "type": "channel.follow",
                                            "version": "2",
                                            "condition": {
                                                "broadcaster_user_id": "456",
                                                "moderator_user_id": "789"
                                            },
                                            "transport": {
                                                "method": "websocket",
                                                "session_id": "session-abc"
                                            },
                                            "created_at": "2023-04-11T10:11:12.123Z",
                                            "cost": 0
                                        }
                                    ],
                                    "pagination": {}
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }

                val subscriptionType =
                    object : EventSubSubscriptionType {
                        override val type = "channel.follow"
                        override val version = "2"

                        override fun toCondition() =
                            mapOf(
                                "broadcaster_user_id" to "456",
                                "moderator_user_id" to "789",
                            )
                    }

                val resource = createResource(engine)
                val subscription = resource.create(subscriptionType, sessionId = "session-abc")

                Then("it should call the eventsub/subscriptions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/eventsub/subscriptions"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the subscription") {
                    subscription.id shouldBe "sub-123"
                    subscription.status shouldBe "enabled"
                    subscription.type shouldBe "channel.follow"
                    subscription.version shouldBe "2"
                }

                Then("it should deserialize the condition") {
                    subscription.condition["broadcaster_user_id"] shouldBe "456"
                    subscription.condition["moderator_user_id"] shouldBe "789"
                }

                Then("it should deserialize the transport") {
                    subscription.transport.method shouldBe "websocket"
                    subscription.transport.sessionId shouldBe "session-abc"
                }

                Then("it should deserialize the cost") {
                    subscription.cost shouldBe 0
                }
            }
        }
    })
