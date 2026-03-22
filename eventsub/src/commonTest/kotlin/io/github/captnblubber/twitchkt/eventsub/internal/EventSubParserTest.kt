package io.github.captnblubber.twitchkt.eventsub.internal

import io.github.captnblubber.twitchkt.eventsub.model.ChannelFollow
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscribe
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionGift
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUpdate
import io.github.captnblubber.twitchkt.eventsub.model.StreamOnline
import io.github.captnblubber.twitchkt.eventsub.model.UnknownEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Instant

class EventSubParserTest :
    BehaviorSpec({

        val parser = EventSubParser()

        Given("a session_welcome message") {

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

            When("parsing the message") {
                val result = parser.parse(welcomeJson)

                Then("it should return a Welcome message") {
                    result.shouldBeInstanceOf<ParsedMessage.Welcome>()
                }

                Then("it should contain the correct session id") {
                    val welcome = result as ParsedMessage.Welcome
                    welcome.session.id shouldBe "AQoQILE98gtqShGmLD7AM6yJThAB"
                }

                Then("it should contain the correct session status") {
                    val welcome = result as ParsedMessage.Welcome
                    welcome.session.status shouldBe "connected"
                }

                Then("it should contain the keepalive timeout") {
                    val welcome = result as ParsedMessage.Welcome
                    welcome.session.keepaliveTimeoutSeconds shouldBe 10
                }

                Then("the reconnect URL should be null") {
                    val welcome = result as ParsedMessage.Welcome
                    welcome.session.reconnectUrl.shouldBeNull()
                }
            }
        }

        Given("a session_keepalive message") {

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

            When("parsing the message") {
                val result = parser.parse(keepaliveJson)

                Then("it should return Keepalive") {
                    result.shouldBeInstanceOf<ParsedMessage.Keepalive>()
                }
            }
        }

        Given("a session_reconnect message") {

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

            When("parsing the message") {
                val result = parser.parse(reconnectJson)

                Then("it should return a Reconnect message") {
                    result.shouldBeInstanceOf<ParsedMessage.Reconnect>()
                }

                Then("it should contain the reconnect URL") {
                    val reconnect = result as ParsedMessage.Reconnect
                    reconnect.session.reconnectUrl shouldBe
                        "wss://eventsub.wss.twitch.tv?token=reconnect-token-123"
                }

                Then("it should have reconnecting status") {
                    val reconnect = result as ParsedMessage.Reconnect
                    reconnect.session.status shouldBe "reconnecting"
                }

                Then("the keepalive timeout should be null") {
                    val reconnect = result as ParsedMessage.Reconnect
                    reconnect.session.keepaliveTimeoutSeconds.shouldBeNull()
                }
            }
        }

        Given("a channel.follow notification") {

            val followJson =
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

            When("parsing the message") {
                val result = parser.parse(followJson)

                Then("it should return a Notification") {
                    result.shouldBeInstanceOf<ParsedMessage.Notification>()
                }

                Then("the event should be a ChannelFollow") {
                    val notification = result as ParsedMessage.Notification
                    notification.event.shouldBeInstanceOf<ChannelFollow>()
                }

                Then("it should map all follow fields correctly") {
                    val follow = (result as ParsedMessage.Notification).event as ChannelFollow
                    follow.subscriptionType shouldBe "channel.follow"
                    follow.messageId shouldBe "befa7b53-d79d-478f-86b9-120f112b044e"
                    follow.timestamp shouldBe Instant.parse("2022-11-16T10:11:12.464757833Z")
                    follow.userId shouldBe "1234"
                    follow.userLogin shouldBe "cool_user"
                    follow.userName shouldBe "Cool_User"
                    follow.broadcasterUserId shouldBe "1337"
                    follow.broadcasterUserLogin shouldBe "cooler_user"
                    follow.broadcasterUserName shouldBe "Cooler_User"
                    follow.followedAt shouldBe Instant.parse("2023-07-15T18:16:11.17106713Z")
                }
            }
        }

        Given("a channel.subscribe notification") {

            val subscribeJson =
                """
                {
                    "metadata": {
                        "message_id": "sub-msg-001",
                        "message_type": "notification",
                        "message_timestamp": "2023-01-10T15:00:00.000Z",
                        "subscription_type": "channel.subscribe",
                        "subscription_version": "1"
                    },
                    "payload": {
                        "subscription": {
                            "id": "sub-id-001",
                            "status": "enabled",
                            "type": "channel.subscribe",
                            "version": "1",
                            "condition": {
                                "broadcaster_user_id": "1337"
                            },
                            "transport": {
                                "method": "websocket",
                                "session_id": "session-001"
                            },
                            "created_at": "2023-01-01T00:00:00.000Z",
                            "cost": 0
                        },
                        "event": {
                            "user_id": "5678",
                            "user_login": "sub_user",
                            "user_name": "Sub_User",
                            "broadcaster_user_id": "1337",
                            "broadcaster_user_login": "streamer",
                            "broadcaster_user_name": "Streamer",
                            "tier": "1000",
                            "is_gift": false
                        }
                    }
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(subscribeJson)

                Then("the event should be a ChannelSubscribe") {
                    val notification = result as ParsedMessage.Notification
                    notification.event.shouldBeInstanceOf<ChannelSubscribe>()
                }

                Then("it should map all subscribe fields correctly") {
                    val sub = (result as ParsedMessage.Notification).event as ChannelSubscribe
                    sub.subscriptionType shouldBe "channel.subscribe"
                    sub.messageId shouldBe "sub-msg-001"
                    sub.userId shouldBe "5678"
                    sub.userLogin shouldBe "sub_user"
                    sub.userName shouldBe "Sub_User"
                    sub.tier shouldBe "1000"
                    sub.isGift shouldBe false
                }
            }
        }

        Given("a channel.subscription.gift notification") {

            val giftJson =
                """
                {
                    "metadata": {
                        "message_id": "gift-msg-001",
                        "message_type": "notification",
                        "message_timestamp": "2023-02-14T12:00:00.000Z",
                        "subscription_type": "channel.subscription.gift",
                        "subscription_version": "1"
                    },
                    "payload": {
                        "subscription": {
                            "id": "gift-sub-001",
                            "status": "enabled",
                            "type": "channel.subscription.gift",
                            "version": "1",
                            "condition": {
                                "broadcaster_user_id": "1337"
                            },
                            "transport": {
                                "method": "websocket",
                                "session_id": "session-001"
                            },
                            "created_at": "2023-01-01T00:00:00.000Z",
                            "cost": 0
                        },
                        "event": {
                            "user_id": "9999",
                            "user_login": "generous_user",
                            "user_name": "Generous_User",
                            "broadcaster_user_id": "1337",
                            "broadcaster_user_login": "streamer",
                            "broadcaster_user_name": "Streamer",
                            "total": 5,
                            "tier": "1000",
                            "cumulative_total": 25,
                            "is_anonymous": false
                        }
                    }
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(giftJson)

                Then("the event should be a ChannelSubscriptionGift") {
                    val notification = result as ParsedMessage.Notification
                    notification.event.shouldBeInstanceOf<ChannelSubscriptionGift>()
                }

                Then("it should map gift fields correctly") {
                    val gift = (result as ParsedMessage.Notification).event as ChannelSubscriptionGift
                    gift.total shouldBe 5
                    gift.tier shouldBe "1000"
                    gift.cumulativeTotal shouldBe 25
                    gift.isAnonymous shouldBe false
                    gift.userId shouldBe "9999"
                }
            }
        }

        Given("a stream.online notification") {

            val streamOnlineJson =
                """
                {
                    "metadata": {
                        "message_id": "stream-msg-001",
                        "message_type": "notification",
                        "message_timestamp": "2023-03-20T18:00:00.000Z",
                        "subscription_type": "stream.online",
                        "subscription_version": "1"
                    },
                    "payload": {
                        "subscription": {
                            "id": "stream-sub-001",
                            "status": "enabled",
                            "type": "stream.online",
                            "version": "1",
                            "condition": {
                                "broadcaster_user_id": "1337"
                            },
                            "transport": {
                                "method": "websocket",
                                "session_id": "session-001"
                            },
                            "created_at": "2023-01-01T00:00:00.000Z",
                            "cost": 0
                        },
                        "event": {
                            "id": "9001",
                            "broadcaster_user_id": "1337",
                            "broadcaster_user_login": "cool_user",
                            "broadcaster_user_name": "Cool_User",
                            "type": "live",
                            "started_at": "2023-03-20T18:00:00.000Z"
                        }
                    }
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(streamOnlineJson)

                Then("the event should be a StreamOnline") {
                    val notification = result as ParsedMessage.Notification
                    notification.event.shouldBeInstanceOf<StreamOnline>()
                }

                Then("it should map all stream online fields correctly") {
                    val online = (result as ParsedMessage.Notification).event as StreamOnline
                    online.subscriptionType shouldBe "stream.online"
                    online.messageId shouldBe "stream-msg-001"
                    online.id shouldBe "9001"
                    online.broadcasterUserId shouldBe "1337"
                    online.broadcasterUserLogin shouldBe "cool_user"
                    online.broadcasterUserName shouldBe "Cool_User"
                    online.type shouldBe "live"
                    online.startedAt shouldBe Instant.parse("2023-03-20T18:00:00.000Z")
                }
            }
        }

        Given("a channel.update notification") {

            val updateJson =
                """
                {
                    "metadata": {
                        "message_id": "update-msg-001",
                        "message_type": "notification",
                        "message_timestamp": "2023-04-01T12:00:00.000Z",
                        "subscription_type": "channel.update",
                        "subscription_version": "2"
                    },
                    "payload": {
                        "subscription": {
                            "id": "update-sub-001",
                            "status": "enabled",
                            "type": "channel.update",
                            "version": "2",
                            "condition": {
                                "broadcaster_user_id": "1337"
                            },
                            "transport": {
                                "method": "websocket",
                                "session_id": "session-001"
                            },
                            "created_at": "2023-01-01T00:00:00.000Z",
                            "cost": 0
                        },
                        "event": {
                            "broadcaster_user_id": "1337",
                            "broadcaster_user_login": "cool_user",
                            "broadcaster_user_name": "Cool_User",
                            "title": "Playing Elden Ring!",
                            "language": "en",
                            "category_id": "512953",
                            "category_name": "Elden Ring",
                            "content_classification_labels": ["MatureGame"]
                        }
                    }
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(updateJson)

                Then("the event should be a ChannelUpdate") {
                    val notification = result as ParsedMessage.Notification
                    notification.event.shouldBeInstanceOf<ChannelUpdate>()
                }

                Then("it should map all channel update fields correctly") {
                    val update = (result as ParsedMessage.Notification).event as ChannelUpdate
                    update.title shouldBe "Playing Elden Ring!"
                    update.language shouldBe "en"
                    update.categoryId shouldBe "512953"
                    update.categoryName shouldBe "Elden Ring"
                    update.contentClassificationLabels shouldBe listOf("MatureGame")
                }
            }
        }

        Given("a revocation message") {

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

            When("parsing the message") {
                val result = parser.parse(revocationJson)

                Then("it should return a Revocation message") {
                    result.shouldBeInstanceOf<ParsedMessage.Revocation>()
                }

                Then("it should contain the correct subscription type") {
                    val revocation = result as ParsedMessage.Revocation
                    revocation.subscriptionType shouldBe "channel.follow"
                }

                Then("it should contain the revocation status") {
                    val revocation = result as ParsedMessage.Revocation
                    revocation.status shouldBe "authorization_revoked"
                }
            }
        }

        Given("a notification with an unknown subscription type") {

            val unknownJson =
                """
                {
                    "metadata": {
                        "message_id": "unknown-msg-001",
                        "message_type": "notification",
                        "message_timestamp": "2023-05-01T12:00:00.000Z",
                        "subscription_type": "channel.some_future_event",
                        "subscription_version": "1"
                    },
                    "payload": {
                        "subscription": {
                            "id": "unknown-sub-001",
                            "status": "enabled",
                            "type": "channel.some_future_event",
                            "version": "1",
                            "condition": {
                                "broadcaster_user_id": "1337"
                            },
                            "transport": {
                                "method": "websocket",
                                "session_id": "session-001"
                            },
                            "created_at": "2023-01-01T00:00:00.000Z",
                            "cost": 0
                        },
                        "event": {
                            "some_field": "some_value",
                            "another_field": 42
                        }
                    }
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(unknownJson)

                Then("it should return a Notification") {
                    result.shouldBeInstanceOf<ParsedMessage.Notification>()
                }

                Then("the event should be an UnknownEvent") {
                    val notification = result as ParsedMessage.Notification
                    notification.event.shouldBeInstanceOf<UnknownEvent>()
                }

                Then("the unknown event should contain the subscription type") {
                    val unknown = (result as ParsedMessage.Notification).event as UnknownEvent
                    unknown.subscriptionType shouldBe "channel.some_future_event"
                }

                Then("the unknown event should preserve the raw payload") {
                    val unknown = (result as ParsedMessage.Notification).event as UnknownEvent
                    unknown.rawPayload.shouldNotBeNull()
                    unknown.rawPayload.containsKey("some_field") shouldBe true
                }

                Then("the unknown event should have the correct message id") {
                    val unknown = (result as ParsedMessage.Notification).event as UnknownEvent
                    unknown.messageId shouldBe "unknown-msg-001"
                }
            }
        }

        // --- parser error paths ---

        Given("an empty string input") {

            When("parsing the empty string") {

                Then("it should throw an exception") {
                    shouldThrow<Exception> {
                        parser.parse("")
                    }
                }
            }
        }

        Given("invalid JSON input") {

            When("parsing non-JSON text") {

                Then("it should throw an exception") {
                    shouldThrow<Exception> {
                        parser.parse("not json at all")
                    }
                }
            }
        }

        Given("valid JSON but missing message_type") {

            When("parsing JSON without required metadata fields") {

                Then("it should throw an exception") {
                    shouldThrow<Exception> {
                        parser.parse("""{"metadata":{},"payload":{}}""")
                    }
                }
            }
        }

        Given("a message with an unknown message_type value") {

            val unknownMessageTypeJson =
                """
                {
                    "metadata": {
                        "message_type": "unknown_type",
                        "message_id": "1",
                        "message_timestamp": "2024-01-01T00:00:00Z"
                    },
                    "payload": {}
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(unknownMessageTypeJson)

                Then("it should return a Notification with an UnknownEvent") {
                    val notification = result.shouldBeInstanceOf<ParsedMessage.Notification>()
                    notification.event.shouldBeInstanceOf<UnknownEvent>()
                }
            }
        }

        Given("an unknown message type") {

            val unknownTypeJson =
                """
                {
                    "metadata": {
                        "message_id": "future-msg-001",
                        "message_type": "some_future_message_type",
                        "message_timestamp": "2023-06-01T12:00:00.000Z"
                    },
                    "payload": {
                        "data": "something"
                    }
                }
                """.trimIndent()

            When("parsing the message") {
                val result = parser.parse(unknownTypeJson)

                Then("it should return a Notification with an UnknownEvent") {
                    val notification = result.shouldBeInstanceOf<ParsedMessage.Notification>()
                    notification.event.shouldBeInstanceOf<UnknownEvent>()
                }
            }
        }
    })
