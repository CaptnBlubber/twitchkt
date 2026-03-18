package io.github.captnblubber.twitchkt.eventsub.protocol

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant

class EventSubMessageTest :
    BehaviorSpec({

        val json = Json { ignoreUnknownKeys = true }

        Given("a session_welcome message from Twitch EventSub WebSocket") {

            When("deserializing a welcome frame") {
                val welcomeJson =
                    """
                    {
                        "metadata": {
                            "message_id": "96a3f3b5-5dec-4eed-908e-e11ee657416c",
                            "message_type": "session_welcome",
                            "message_timestamp": "2023-07-19T14:56:51.634234626Z"
                        },
                        "payload": {
                            "session": {
                                "id": "AQoQILE98gtqShGmLD7AM6yJThAB",
                                "status": "connected",
                                "keepalive_timeout_seconds": 10,
                                "reconnect_url": null,
                                "connected_at": "2023-07-19T14:56:51.616329898Z"
                            }
                        }
                    }
                    """.trimIndent()

                val frame = json.decodeFromString<EventSubFrame>(welcomeJson)

                Then("it should decode metadata fields") {
                    frame.metadata.messageId shouldBe "96a3f3b5-5dec-4eed-908e-e11ee657416c"
                    frame.metadata.messageType shouldBe "session_welcome"
                    frame.metadata.messageTimestamp shouldBe "2023-07-19T14:56:51.634234626Z"
                }

                Then("metadata should not have subscription fields") {
                    frame.metadata.subscriptionType.shouldBeNull()
                    frame.metadata.subscriptionVersion.shouldBeNull()
                }

                Then("it should contain a session object in the payload") {
                    val sessionJson = frame.payload["session"]
                    sessionJson.shouldNotBeNull()
                }

                Then("it should decode the session payload") {
                    val sessionObj = frame.payload["session"].shouldNotBeNull().jsonObject
                    val session = json.decodeFromString<SessionPayload>(sessionObj.toString())

                    session.id shouldBe "AQoQILE98gtqShGmLD7AM6yJThAB"
                    session.status shouldBe "connected"
                    session.keepaliveTimeoutSeconds shouldBe 10
                    session.reconnectUrl.shouldBeNull()
                    session.connectedAt shouldBe Instant.parse("2023-07-19T14:56:51.616329898Z")
                }
            }
        }

        Given("a session_keepalive message from Twitch EventSub WebSocket") {

            When("deserializing a keepalive frame") {
                val keepaliveJson =
                    """
                    {
                        "metadata": {
                            "message_id": "84c1e79a-2a4b-4c13-ba0b-4312293e9308",
                            "message_type": "session_keepalive",
                            "message_timestamp": "2023-07-19T10:11:12.634234626Z"
                        },
                        "payload": {}
                    }
                    """.trimIndent()

                val frame = json.decodeFromString<EventSubFrame>(keepaliveJson)

                Then("it should decode metadata fields") {
                    frame.metadata.messageId shouldBe "84c1e79a-2a4b-4c13-ba0b-4312293e9308"
                    frame.metadata.messageType shouldBe "session_keepalive"
                    frame.metadata.messageTimestamp shouldBe "2023-07-19T10:11:12.634234626Z"
                }

                Then("the payload should be empty") {
                    frame.payload.isEmpty() shouldBe true
                }

                Then("metadata should not have subscription fields") {
                    frame.metadata.subscriptionType.shouldBeNull()
                    frame.metadata.subscriptionVersion.shouldBeNull()
                }
            }
        }

        Given("a notification message from Twitch EventSub WebSocket") {

            When("deserializing a channel.follow notification frame") {
                val notificationJson =
                    """
                    {
                        "metadata": {
                            "message_id": "befa7b53-d79d-478f-86b9-120f112b044e",
                            "message_type": "notification",
                            "message_timestamp": "2022-11-16T10:11:12.464757833Z",
                            "subscription_type": "channel.follow",
                            "subscription_version": "2"
                        },
                        "payload": {
                            "subscription": {
                                "id": "f1c2a387-161a-49f9-a165-0f21d7a4e1c4",
                                "status": "enabled",
                                "type": "channel.follow",
                                "version": "2",
                                "condition": {
                                    "broadcaster_user_id": "1337",
                                    "moderator_user_id": "1337"
                                },
                                "transport": {
                                    "method": "websocket",
                                    "session_id": "AQoQexAWVYKSTIu4ec_2VAxyuhAB"
                                },
                                "created_at": "2023-04-11T10:11:12.123Z",
                                "cost": 0
                            },
                            "event": {
                                "user_id": "1234",
                                "user_login": "cool_user",
                                "user_name": "Cool_User",
                                "broadcaster_user_id": "1337",
                                "broadcaster_user_login": "cooler_user",
                                "broadcaster_user_name": "Cooler_User",
                                "followed_at": "2023-07-15T18:16:11.17106713Z"
                            }
                        }
                    }
                    """.trimIndent()

                val frame = json.decodeFromString<EventSubFrame>(notificationJson)

                Then("it should decode metadata with subscription info") {
                    frame.metadata.messageType shouldBe "notification"
                    frame.metadata.subscriptionType shouldBe "channel.follow"
                    frame.metadata.subscriptionVersion shouldBe "2"
                }

                Then("it should contain a subscription object in the payload") {
                    val subscription = frame.payload["subscription"]
                    subscription.shouldNotBeNull()
                }

                Then("it should contain an event object in the payload") {
                    val event = frame.payload["event"]
                    event.shouldNotBeNull()
                }

                Then("the event payload should contain follow-specific fields") {
                    val event = frame.payload["event"].shouldNotBeNull().jsonObject
                    event["user_id"].shouldNotBeNull().jsonPrimitive.content shouldBe "1234"
                    event["user_login"].shouldNotBeNull().jsonPrimitive.content shouldBe "cool_user"
                    event["followed_at"].shouldNotBeNull().jsonPrimitive.content shouldBe "2023-07-15T18:16:11.17106713Z"
                }
            }
        }

        Given("a session_reconnect message from Twitch EventSub WebSocket") {

            When("deserializing a reconnect frame") {
                val reconnectJson =
                    """
                    {
                        "metadata": {
                            "message_id": "84c1e79a-2a4b-4c13-ba0b-4312293e9308",
                            "message_type": "session_reconnect",
                            "message_timestamp": "2022-11-18T09:10:11.634234626Z"
                        },
                        "payload": {
                            "session": {
                                "id": "AQoQexAWVYKSTIu4ec_2VAxyuhAB",
                                "status": "reconnecting",
                                "keepalive_timeout_seconds": null,
                                "reconnect_url": "wss://eventsub.wss.twitch.tv?token=reconnect-token-123",
                                "connected_at": "2022-11-16T10:11:12.634234626Z"
                            }
                        }
                    }
                    """.trimIndent()

                val frame = json.decodeFromString<EventSubFrame>(reconnectJson)

                Then("it should decode metadata as session_reconnect") {
                    frame.metadata.messageType shouldBe "session_reconnect"
                }

                Then("it should decode the session payload with reconnect URL") {
                    val sessionObj = frame.payload["session"].shouldNotBeNull().jsonObject
                    val session = json.decodeFromString<SessionPayload>(sessionObj.toString())

                    session.id shouldBe "AQoQexAWVYKSTIu4ec_2VAxyuhAB"
                    session.status shouldBe "reconnecting"
                    session.keepaliveTimeoutSeconds.shouldBeNull()
                    session.reconnectUrl shouldBe "wss://eventsub.wss.twitch.tv?token=reconnect-token-123"
                    session.connectedAt shouldBe Instant.parse("2022-11-16T10:11:12.634234626Z")
                }
            }
        }

        Given("a revocation message from Twitch EventSub WebSocket") {

            When("deserializing a revocation frame") {
                val revocationJson =
                    """
                    {
                        "metadata": {
                            "message_id": "abc-123-def-456",
                            "message_type": "revocation",
                            "message_timestamp": "2022-11-16T10:11:12.464757833Z",
                            "subscription_type": "channel.follow",
                            "subscription_version": "2"
                        },
                        "payload": {
                            "subscription": {
                                "id": "f1c2a387-161a-49f9-a165-0f21d7a4e1c4",
                                "status": "authorization_revoked",
                                "type": "channel.follow",
                                "version": "2",
                                "condition": {
                                    "broadcaster_user_id": "1337",
                                    "moderator_user_id": "1337"
                                },
                                "transport": {
                                    "method": "websocket",
                                    "session_id": "AQoQexAWVYKSTIu4ec_2VAxyuhAB"
                                },
                                "created_at": "2023-04-11T10:11:12.123Z",
                                "cost": 0
                            }
                        }
                    }
                    """.trimIndent()

                val frame = json.decodeFromString<EventSubFrame>(revocationJson)

                Then("it should decode metadata as revocation") {
                    frame.metadata.messageType shouldBe "revocation"
                    frame.metadata.subscriptionType shouldBe "channel.follow"
                    frame.metadata.subscriptionVersion shouldBe "2"
                }

                Then("it should contain a subscription object in the payload") {
                    val subscription = frame.payload["subscription"]
                    subscription.shouldNotBeNull()
                    val subObj = subscription.jsonObject
                    subObj["status"].shouldNotBeNull().jsonPrimitive.content shouldBe "authorization_revoked"
                }

                Then("it should not contain an event object") {
                    frame.payload["event"].shouldBeNull()
                }
            }
        }
    })
