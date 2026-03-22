package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.github.captnblubber.twitchkt.helix.model.SendChatMessageRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.toList

class ChatResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = ChatResource(createHelixClient(engine))

        Given("getChatters") {

            When("called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "chatter",
                                            "user_name": "Chatter"
                                        }
                                    ],
                                    "pagination": {
                                        "cursor": "abc123"
                                    }
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getChatters(broadcasterId = "123", moderatorId = "456")

                Then("it should call the chat/chatters endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/chatters"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should deserialize the chatter") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                    page.data.first().userLogin shouldBe "chatter"
                    page.data.first().userName shouldBe "Chatter"
                }

                Then("it should return a non-null cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("called with a cursor (subsequent page)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "chatter",
                                            "user_name": "Chatter"
                                        }
                                    ],
                                    "pagination": {
                                        "cursor": "def456"
                                    }
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getChatters(broadcasterId = "123", moderatorId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the response cursor") {
                    page.cursor shouldBe "def456"
                }
            }

            When("the response has no pagination cursor (last page)") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "789",
                                            "user_login": "lastchatter",
                                            "user_name": "LastChatter"
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
                val page = resource.getChatters(broadcasterId = "123", moderatorId = "456")

                Then("it should return the chatter data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "789"
                }

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("called with a pageSize") {
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
                resource.getChatters(broadcasterId = "123", moderatorId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        Given("getAllChatters") {

            When("called with broadcaster and moderator IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "456",
                                            "user_login": "chatter",
                                            "user_name": "Chatter"
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
                val chatters = resource.getAllChatters(broadcasterId = "123", moderatorId = "456").toList()

                Then("it should call the chat/chatters endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/chatters"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should deserialize the chatter") {
                    chatters.size shouldBe 1
                    chatters.first().userId shouldBe "456"
                    chatters.first().userLogin shouldBe "chatter"
                    chatters.first().userName shouldBe "Chatter"
                }
            }
        }

        val userEmoteJson =
            """
            {
                "data": [
                    {
                        "id": "emote-1",
                        "name": "HeyGuys",
                        "emote_type": "globals",
                        "emote_set_id": "0",
                        "owner_id": "456",
                        "format": ["static"],
                        "scale": ["1.0", "2.0"],
                        "theme_mode": ["dark", "light"]
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val userEmoteLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "emote-2",
                        "name": "LUL",
                        "emote_type": "subscriptions",
                        "emote_set_id": "100",
                        "owner_id": "789",
                        "format": ["static", "animated"],
                        "scale": ["1.0"],
                        "theme_mode": ["dark"]
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val userEmoteSecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "emote-1",
                        "name": "HeyGuys",
                        "emote_type": "globals",
                        "emote_set_id": "0",
                        "owner_id": "456",
                        "format": ["static"],
                        "scale": ["1.0", "2.0"],
                        "theme_mode": ["dark", "light"]
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("UserEmotes") {

            When("getAllUserEmotes is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val emotes = resource.getAllUserEmotes(userId = "123").toList()

                Then("it should call the chat/emotes/user endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/emotes/user"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should deserialize the emote") {
                    emotes.size shouldBe 1
                    emotes.first().id shouldBe "emote-2"
                    emotes.first().name shouldBe "LUL"
                    emotes.first().emoteType shouldBe "subscriptions"
                }
            }

            When("getAllUserEmotes is called with a broadcasterId") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getAllUserEmotes(userId = "123", broadcasterId = "456").toList()

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "456"
                }
            }

            When("getUserEmotes is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUserEmotes(userId = "123")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the emote data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "emote-1"
                    page.data.first().name shouldBe "HeyGuys"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getUserEmotes is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUserEmotes(userId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getUserEmotes is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = userEmoteLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUserEmotes(userId = "123")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }
        }

        Given("getChatters error paths") {

            When("the API returns 403 Forbidden") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Forbidden","message":"Access denied"}""",
                            status = HttpStatusCode.Forbidden,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw Forbidden") {
                    shouldThrow<TwitchApiException.Forbidden> {
                        resource.getChatters(broadcasterId = "123", moderatorId = "456")
                    }
                }
            }
        }

        Given("sendMessage error paths") {

            When("the API returns 401 Unauthorized") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Unauthorized","message":"Invalid token"}""",
                            status = HttpStatusCode.Unauthorized,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw Unauthorized") {
                    shouldThrow<TwitchApiException.Unauthorized> {
                        resource.sendMessage(
                            SendChatMessageRequest(
                                broadcasterId = "123",
                                senderId = "456",
                                message = "Hello",
                            ),
                        )
                    }
                }
            }
        }

        Given("getSettings error paths") {

            When("the API returns empty data") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"data": []}""",
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw EmptyResponse") {
                    shouldThrow<TwitchApiException.EmptyResponse> {
                        resource.getSettings(broadcasterId = "123")
                    }
                }
            }
        }

        Given("sendMessage") {

            When("called with a valid request") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "message_id": "msg-123",
                                            "is_sent": true
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val response =
                    resource.sendMessage(
                        SendChatMessageRequest(
                            broadcasterId = "123",
                            senderId = "456",
                            message = "Hello World",
                        ),
                    )

                Then("it should call the chat/messages endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/messages"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the response") {
                    response.messageId shouldBe "msg-123"
                    response.isSent shouldBe true
                    response.dropReason shouldBe null
                }
            }
        }

        Given("getGlobalBadges") {

            When("called") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "set_id": "vip",
                                            "versions": [
                                                {
                                                    "id": "1",
                                                    "image_url_1x": "https://example.com/1x.png",
                                                    "image_url_2x": "https://example.com/2x.png",
                                                    "image_url_4x": "https://example.com/4x.png",
                                                    "title": "VIP",
                                                    "description": "VIP badge"
                                                }
                                            ]
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val badges = resource.getGlobalBadges()

                Then("it should call the chat/badges/global endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/badges/global"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should deserialize the badges") {
                    badges.size shouldBe 1
                    badges.first().setId shouldBe "vip"
                    badges.first().versions.size shouldBe 1
                    badges
                        .first()
                        .versions
                        .first()
                        .id shouldBe "1"
                    badges
                        .first()
                        .versions
                        .first()
                        .title shouldBe "VIP"
                }
            }
        }

        Given("getChannelBadges") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "set_id": "subscriber",
                                            "versions": [
                                                {
                                                    "id": "0",
                                                    "image_url_1x": "https://example.com/sub1x.png",
                                                    "image_url_2x": "https://example.com/sub2x.png",
                                                    "image_url_4x": "https://example.com/sub4x.png"
                                                }
                                            ]
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val badges = resource.getChannelBadges(broadcasterId = "123")

                Then("it should call the chat/badges endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/badges"
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the badges") {
                    badges.size shouldBe 1
                    badges.first().setId shouldBe "subscriber"
                }
            }
        }

        Given("getChannelEmotes") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "emote-1",
                                            "name": "streamerHype",
                                            "images": {
                                                "url_1x": "https://example.com/1x.png",
                                                "url_2x": "https://example.com/2x.png",
                                                "url_4x": "https://example.com/4x.png"
                                            },
                                            "tier": "1000",
                                            "emote_type": "subscriptions",
                                            "emote_set_id": "100",
                                            "format": ["static"],
                                            "scale": ["1.0", "2.0"],
                                            "theme_mode": ["dark", "light"]
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val emotes = resource.getChannelEmotes(broadcasterId = "123")

                Then("it should call the chat/emotes endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/emotes"
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the emotes") {
                    emotes.size shouldBe 1
                    emotes.first().id shouldBe "emote-1"
                    emotes.first().name shouldBe "streamerHype"
                    emotes.first().tier shouldBe "1000"
                    emotes.first().emoteType shouldBe "subscriptions"
                }
            }
        }

        Given("getGlobalEmotes") {

            When("called") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "global-1",
                                            "name": "Kappa",
                                            "images": {
                                                "url_1x": "https://example.com/kappa1x.png",
                                                "url_2x": "https://example.com/kappa2x.png",
                                                "url_4x": "https://example.com/kappa4x.png"
                                            },
                                            "format": ["static", "animated"],
                                            "scale": ["1.0", "2.0", "3.0"],
                                            "theme_mode": ["dark", "light"]
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val emotes = resource.getGlobalEmotes()

                Then("it should call the chat/emotes/global endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/emotes/global"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should deserialize the emotes") {
                    emotes.size shouldBe 1
                    emotes.first().id shouldBe "global-1"
                    emotes.first().name shouldBe "Kappa"
                }
            }
        }

        Given("getEmoteSets") {

            When("called with emote set IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "emote-set-1",
                                            "name": "SetEmote",
                                            "images": {
                                                "url_1x": "https://example.com/set1x.png",
                                                "url_2x": "https://example.com/set2x.png",
                                                "url_4x": "https://example.com/set4x.png"
                                            },
                                            "emote_set_id": "300",
                                            "owner_id": "789"
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val emotes = resource.getEmoteSets(emoteSetIds = listOf("300", "301"))

                Then("it should call the chat/emotes/set endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/emotes/set"
                }

                Then("it should pass multiple emote_set_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("emote_set_id") shouldBe listOf("300", "301")
                }

                Then("it should deserialize the emotes") {
                    emotes.size shouldBe 1
                    emotes.first().id shouldBe "emote-set-1"
                    emotes.first().emoteSetId shouldBe "300"
                    emotes.first().ownerId shouldBe "789"
                }
            }
        }

        Given("getSettings") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "123",
                                            "slow_mode": false,
                                            "slow_mode_wait_time": null,
                                            "follower_mode": true,
                                            "follower_mode_duration": 10,
                                            "subscriber_mode": false,
                                            "emote_mode": false,
                                            "unique_chat_mode": false
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val settings = resource.getSettings(broadcasterId = "123")

                Then("it should call the chat/settings endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/settings"
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should not include moderator_id when not provided") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"].shouldBeNull()
                }

                Then("it should deserialize the settings") {
                    settings.broadcasterId shouldBe "123"
                    settings.slowMode shouldBe false
                    settings.followerMode shouldBe true
                    settings.followerModeDuration shouldBe 10
                    settings.subscriberMode shouldBe false
                    settings.emoteMode shouldBe false
                    settings.uniqueChatMode shouldBe false
                }
            }

            When("called with a moderator ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "123",
                                            "slow_mode": false,
                                            "follower_mode": false,
                                            "subscriber_mode": false,
                                            "emote_mode": false,
                                            "unique_chat_mode": false,
                                            "non_moderator_chat_delay": true,
                                            "non_moderator_chat_delay_duration": 4,
                                            "moderator_id": "456"
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val settings = resource.getSettings(broadcasterId = "123", moderatorId = "456")

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should deserialize moderator-only fields") {
                    settings.nonModeratorChatDelay shouldBe true
                    settings.nonModeratorChatDelayDuration shouldBe 4
                    settings.moderatorId shouldBe "456"
                }
            }
        }

        Given("updateSettings") {

            When("called with settings to update") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "123",
                                            "slow_mode": true,
                                            "slow_mode_wait_time": 10,
                                            "follower_mode": false,
                                            "subscriber_mode": false,
                                            "emote_mode": false,
                                            "unique_chat_mode": false
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val settings =
                    resource.updateSettings(
                        broadcasterId = "123",
                        moderatorId = "456",
                        slowMode = true,
                        slowModeWaitTime = 10,
                    )

                Then("it should call the chat/settings endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/settings"
                }

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the updated settings") {
                    settings.broadcasterId shouldBe "123"
                    settings.slowMode shouldBe true
                    settings.slowModeWaitTime shouldBe 10
                }
            }
        }

        Given("updateSettings - with additional optional params") {

            When("called with all optional settings") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "broadcaster_id": "123",
                                            "slow_mode": false,
                                            "follower_mode": true,
                                            "follower_mode_duration": 30,
                                            "subscriber_mode": true,
                                            "emote_mode": true,
                                            "unique_chat_mode": true,
                                            "non_moderator_chat_delay": true,
                                            "non_moderator_chat_delay_duration": 4
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val settings =
                    resource.updateSettings(
                        broadcasterId = "123",
                        moderatorId = "456",
                        followerMode = true,
                        followerModeDuration = 30,
                        subscriberMode = true,
                        emoteMode = true,
                        uniqueChatMode = true,
                        nonModeratorChatDelay = true,
                        nonModeratorChatDelayDuration = 4,
                    )

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the updated settings") {
                    settings.followerMode shouldBe true
                    settings.subscriberMode shouldBe true
                    settings.emoteMode shouldBe true
                    settings.uniqueChatMode shouldBe true
                    settings.nonModeratorChatDelay shouldBe true
                    settings.nonModeratorChatDelayDuration shouldBe 4
                }
            }
        }

        Given("sendAnnouncement") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.sendAnnouncement(
                    broadcasterId = "123",
                    moderatorId = "456",
                    message = "Big news!",
                )

                Then("it should call the chat/announcements endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/announcements"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "456"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        Given("getUserColor") {

            When("called with user IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "user_id": "123",
                                            "user_login": "user1",
                                            "user_name": "User1",
                                            "color": "#9146FF"
                                        },
                                        {
                                            "user_id": "456",
                                            "user_login": "user2",
                                            "user_name": "User2",
                                            "color": ""
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val colors = resource.getUserColor(userIds = listOf("123", "456"))

                Then("it should call the chat/color endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/color"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass multiple user_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("user_id") shouldBe listOf("123", "456")
                }

                Then("it should deserialize the color entries") {
                    colors.size shouldBe 2
                    colors[0].userId shouldBe "123"
                    colors[0].color shouldBe "#9146FF"
                    colors[1].userId shouldBe "456"
                    colors[1].color shouldBe ""
                }
            }
        }

        Given("updateUserColor") {

            When("called with user ID and color") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.updateUserColor(userId = "123", color = "blue")

                Then("it should call the chat/color endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/color"
                }

                Then("it should use PUT method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Put
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should pass the color parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["color"] shouldBe "blue"
                }
            }
        }

        Given("getSharedChatSession") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "session_id": "session-1",
                                            "host_broadcaster_id": "123",
                                            "participants": [
                                                {"broadcaster_id": "123"},
                                                {"broadcaster_id": "456"}
                                            ],
                                            "created_at": "2024-01-01T00:00:00Z",
                                            "updated_at": "2024-01-01T01:00:00Z"
                                        }
                                    ]
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val session = resource.getSharedChatSession(broadcasterId = "123")

                Then("it should call the shared_chat/session endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/shared_chat/session"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the session") {
                    session.sessionId shouldBe "session-1"
                    session.hostBroadcasterId shouldBe "123"
                    session.participants.size shouldBe 2
                    session.participants[0].broadcasterId shouldBe "123"
                    session.participants[1].broadcasterId shouldBe "456"
                }
            }
        }

        Given("getAllChatters error paths") {

            When("the API returns 429 Rate Limited") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """{"error":"Too Many Requests","message":"Rate limit exceeded"}""",
                            status = HttpStatusCode.TooManyRequests,
                            headers =
                                headersOf(
                                    HttpHeaders.ContentType to listOf("application/json"),
                                    "Ratelimit-Reset" to listOf("9999999999"),
                                ),
                        )
                    }
                val resource = createResource(engine)

                Then("it should throw RateLimited when collecting the flow") {
                    shouldThrow<TwitchApiException.RateLimited> {
                        resource.getAllChatters(broadcasterId = "123", moderatorId = "456").toList()
                    }
                }
            }
        }
    })
