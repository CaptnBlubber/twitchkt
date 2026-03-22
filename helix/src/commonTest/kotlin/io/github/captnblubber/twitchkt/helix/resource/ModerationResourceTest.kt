package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.model.AutoModAction
import io.github.captnblubber.twitchkt.helix.model.UnbanRequestStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList

class ModerationResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = ModerationResource(createHelixClient(engine))

        val singleItemJson =
            """
            {
                "data": [
                    {
                        "user_id": "456",
                        "user_login": "moderator",
                        "user_name": "Moderator"
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
                        "user_id": "789",
                        "user_login": "lastmod",
                        "user_name": "LastMod"
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
                        "user_id": "456",
                        "user_login": "moderator",
                        "user_name": "Moderator"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("Moderators") {

            When("getAllModerators is called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = lastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val moderators = resource.getAllModerators(broadcasterId = "123").toList()

                Then("it should call the moderation/moderators endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/moderators"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the moderator") {
                    moderators.size shouldBe 1
                    moderators.first().userId shouldBe "789"
                    moderators.first().userLogin shouldBe "lastmod"
                    moderators.first().userName shouldBe "LastMod"
                }
            }

            When("getModerators is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = singleItemJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getModerators(broadcasterId = "123")

                Then("it should call the moderation/moderators endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/moderators"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the moderator data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                    page.data.first().userName shouldBe "Moderator"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getModerators is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = secondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getModerators(broadcasterId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getModerators is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = lastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getModerators(broadcasterId = "123")

                Then("it should return the data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "789"
                }

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getModerators is called with userIds filter") {
                val engine =
                    MockEngine {
                        respond(
                            content = singleItemJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getModerators(broadcasterId = "123", userIds = listOf("456", "789"))

                Then("it should pass the user_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("user_id") shouldBe listOf("456", "789")
                }
            }

            When("getModerators is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = singleItemJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getModerators(broadcasterId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        val unbanRequestJson =
            """
            {
                "data": [
                    {
                        "id": "req-1",
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "user_id": "456",
                        "user_login": "requester",
                        "user_name": "Requester",
                        "text": "please unban me",
                        "status": "pending",
                        "created_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val unbanRequestLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "req-2",
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "user_id": "789",
                        "user_login": "anotheruser",
                        "user_name": "AnotherUser",
                        "text": "sorry",
                        "status": "pending",
                        "created_at": "2024-02-01T00:00:00Z"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val unbanRequestSecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "req-1",
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "user_id": "456",
                        "user_login": "requester",
                        "user_name": "Requester",
                        "text": "please unban me",
                        "status": "pending",
                        "created_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("UnbanRequests") {

            When("getAllUnbanRequests is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = unbanRequestLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val requests = resource.getAllUnbanRequests(broadcasterId = "123", moderatorId = "100").toList()

                Then("it should call the moderation/unban_requests endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/unban_requests"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass broadcaster_id and moderator_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should pass the default status parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["status"] shouldBe "pending"
                }

                Then("it should deserialize the unban request") {
                    requests.size shouldBe 1
                    requests.first().id shouldBe "req-2"
                    requests.first().userId shouldBe "789"
                    requests.first().text shouldBe "sorry"
                }
            }

            When("getUnbanRequests is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = unbanRequestJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUnbanRequests(broadcasterId = "123", moderatorId = "100")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the unban request data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "req-1"
                    page.data.first().text shouldBe "please unban me"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getUnbanRequests is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = unbanRequestSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page =
                    resource.getUnbanRequests(
                        broadcasterId = "123",
                        moderatorId = "100",
                        cursor = "abc123",
                    )

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getUnbanRequests is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = unbanRequestLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getUnbanRequests(broadcasterId = "123", moderatorId = "100")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getUnbanRequests is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = unbanRequestJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getUnbanRequests(
                    broadcasterId = "123",
                    moderatorId = "100",
                    pageSize = 50,
                )

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }

            When("getUnbanRequests is called with a custom status") {
                val engine =
                    MockEngine {
                        respond(
                            content = unbanRequestJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getUnbanRequests(
                    broadcasterId = "123",
                    moderatorId = "100",
                    status = UnbanRequestStatus.APPROVED,
                )

                Then("it should pass the status parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["status"] shouldBe "approved"
                }
            }
        }

        val blockedTermJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "moderator_id": "100",
                        "id": "term-1",
                        "text": "badword",
                        "created_at": "2024-01-01T00:00:00Z",
                        "updated_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val blockedTermLastPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "moderator_id": "100",
                        "id": "term-2",
                        "text": "anotherbad",
                        "created_at": "2024-02-01T00:00:00Z",
                        "updated_at": "2024-02-01T00:00:00Z"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val blockedTermSecondPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "moderator_id": "100",
                        "id": "term-1",
                        "text": "badword",
                        "created_at": "2024-01-01T00:00:00Z",
                        "updated_at": "2024-01-01T00:00:00Z"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("BlockedTerms") {

            When("getAllBlockedTerms is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = blockedTermLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val terms = resource.getAllBlockedTerms(broadcasterId = "123", moderatorId = "100").toList()

                Then("it should call the moderation/blocked_terms endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/blocked_terms"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass broadcaster_id and moderator_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should deserialize the blocked term") {
                    terms.size shouldBe 1
                    terms.first().id shouldBe "term-2"
                    terms.first().text shouldBe "anotherbad"
                }
            }

            When("getBlockedTerms is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = blockedTermJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getBlockedTerms(broadcasterId = "123", moderatorId = "100")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the blocked term data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "term-1"
                    page.data.first().text shouldBe "badword"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getBlockedTerms is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = blockedTermSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getBlockedTerms(broadcasterId = "123", moderatorId = "100", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getBlockedTerms is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = blockedTermLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getBlockedTerms(broadcasterId = "123", moderatorId = "100")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getBlockedTerms is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = blockedTermJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getBlockedTerms(broadcasterId = "123", moderatorId = "100", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        val bannedUserJson =
            """
            {
                "data": [
                    {
                        "user_id": "456",
                        "user_login": "banneduser",
                        "user_name": "BannedUser",
                        "expires_at": "",
                        "created_at": "2024-01-01T00:00:00Z",
                        "reason": "spam",
                        "moderator_id": "100",
                        "moderator_login": "mod",
                        "moderator_name": "Mod"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val bannedUserLastPageJson =
            """
            {
                "data": [
                    {
                        "user_id": "789",
                        "user_login": "anotherbanned",
                        "user_name": "AnotherBanned",
                        "expires_at": "",
                        "created_at": "2024-02-01T00:00:00Z",
                        "reason": "harassment",
                        "moderator_id": "100",
                        "moderator_login": "mod",
                        "moderator_name": "Mod"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val bannedUserSecondPageJson =
            """
            {
                "data": [
                    {
                        "user_id": "456",
                        "user_login": "banneduser",
                        "user_name": "BannedUser",
                        "expires_at": "",
                        "created_at": "2024-01-01T00:00:00Z",
                        "reason": "spam",
                        "moderator_id": "100",
                        "moderator_login": "mod",
                        "moderator_name": "Mod"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("Banned") {

            When("getAllBanned is called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = bannedUserLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val banned = resource.getAllBanned(broadcasterId = "123").toList()

                Then("it should call the moderation/banned endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/banned"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the banned user") {
                    banned.size shouldBe 1
                    banned.first().userId shouldBe "789"
                    banned.first().userLogin shouldBe "anotherbanned"
                    banned.first().reason shouldBe "harassment"
                }
            }

            When("getBanned is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = bannedUserJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getBanned(broadcasterId = "123")

                Then("it should call the moderation/banned endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/banned"
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the banned user data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                    page.data.first().reason shouldBe "spam"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getBanned is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = bannedUserSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getBanned(broadcasterId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getBanned is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = bannedUserLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getBanned(broadcasterId = "123")

                Then("it should return the data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "789"
                }

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getBanned is called with userIds filter") {
                val engine =
                    MockEngine {
                        respond(
                            content = bannedUserJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getBanned(broadcasterId = "123", userIds = listOf("456", "789"))

                Then("it should pass the user_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("user_id") shouldBe listOf("456", "789")
                }
            }

            When("getBanned is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = bannedUserJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getBanned(broadcasterId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        val moderatedChannelJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "456",
                        "broadcaster_login": "channelone",
                        "broadcaster_name": "ChannelOne"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val moderatedChannelLastPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "789",
                        "broadcaster_login": "channeltwo",
                        "broadcaster_name": "ChannelTwo"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val moderatedChannelSecondPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "456",
                        "broadcaster_login": "channelone",
                        "broadcaster_name": "ChannelOne"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("ModeratedChannels") {

            When("getAllModeratedChannels is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = moderatedChannelLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val channels = resource.getAllModeratedChannels(userId = "123").toList()

                Then("it should call the moderation/channels endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/channels"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should deserialize the moderated channel") {
                    channels.size shouldBe 1
                    channels.first().broadcasterId shouldBe "789"
                    channels.first().broadcasterLogin shouldBe "channeltwo"
                }
            }

            When("getModeratedChannels is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = moderatedChannelJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getModeratedChannels(userId = "123")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the moderated channel data") {
                    page.data.size shouldBe 1
                    page.data.first().broadcasterId shouldBe "456"
                    page.data.first().broadcasterName shouldBe "ChannelOne"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getModeratedChannels is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = moderatedChannelSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getModeratedChannels(userId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getModeratedChannels is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = moderatedChannelLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getModeratedChannels(userId = "123")

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getModeratedChannels is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = moderatedChannelJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getModeratedChannels(userId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        Given("VIPs") {

            When("getAllVIPs is called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = lastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val vips = resource.getAllVIPs(broadcasterId = "123").toList()

                Then("it should call the channels/vips endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/vips"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the VIP") {
                    vips.size shouldBe 1
                    vips.first().userId shouldBe "789"
                    vips.first().userLogin shouldBe "lastmod"
                    vips.first().userName shouldBe "LastMod"
                }
            }

            When("getVIPs is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = singleItemJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getVIPs(broadcasterId = "123")

                Then("it should call the channels/vips endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/vips"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the VIP data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "456"
                    page.data.first().userName shouldBe "Moderator"
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getVIPs is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = secondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getVIPs(broadcasterId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getVIPs is called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = lastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getVIPs(broadcasterId = "123")

                Then("it should return the data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "789"
                }

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("getVIPs is called with userIds filter") {
                val engine =
                    MockEngine {
                        respond(
                            content = singleItemJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getVIPs(broadcasterId = "123", userIds = listOf("456", "789"))

                Then("it should pass the user_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("user_id") shouldBe listOf("456", "789")
                }
            }

            When("getVIPs is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = singleItemJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getVIPs(broadcasterId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        Given("checkAutoModStatus") {

            val autoModCheckResponseJson =
                """
                {
                    "data": [
                        {
                            "msg_id": "msg-1",
                            "is_permitted": true
                        },
                        {
                            "msg_id": "msg-2",
                            "is_permitted": false
                        }
                    ]
                }
                """.trimIndent()

            When("called with messages to check") {
                val engine =
                    MockEngine {
                        respond(
                            content = autoModCheckResponseJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val results =
                    resource.checkAutoModStatus(
                        broadcasterId = "123",
                        messages =
                            listOf(
                                AutoModCheckMessage(msgId = "msg-1", msgText = "hello"),
                                AutoModCheckMessage(msgId = "msg-2", msgText = "bad word"),
                            ),
                    )

                Then("it should call the moderation/enforcements/status endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/enforcements/status"
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

                Then("it should deserialize the results") {
                    results.size shouldBe 2
                    results[0].msgId shouldBe "msg-1"
                    results[0].isPermitted shouldBe true
                    results[1].msgId shouldBe "msg-2"
                    results[1].isPermitted shouldBe false
                }
            }
        }

        Given("manageHeldAutoModMessage") {

            When("called with ALLOW action") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.manageHeldAutoModMessage(
                    userId = "100",
                    msgId = "msg-1",
                    action = AutoModAction.ALLOW,
                )

                Then("it should call the moderation/automod/message endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/automod/message"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        val autoModSettingsJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "moderator_id": "100",
                        "overall_level": null,
                        "disability": 1,
                        "aggression": 2,
                        "sexuality_sex_or_gender": 3,
                        "misogyny": 1,
                        "bullying": 2,
                        "swearing": 0,
                        "race_ethnicity_or_religion": 3,
                        "sex_based_terms": 1
                    }
                ]
            }
            """.trimIndent()

        Given("getAutoModSettings") {

            When("called with broadcaster and moderator IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = autoModSettingsJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val settings = resource.getAutoModSettings(broadcasterId = "123", moderatorId = "100")

                Then("it should call the moderation/automod/settings endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/automod/settings"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should deserialize the settings") {
                    settings.broadcasterId shouldBe "123"
                    settings.moderatorId shouldBe "100"
                    settings.overallLevel.shouldBeNull()
                    settings.disability shouldBe 1
                    settings.aggression shouldBe 2
                    settings.sexualitySexOrGender shouldBe 3
                    settings.misogyny shouldBe 1
                    settings.bullying shouldBe 2
                    settings.swearing shouldBe 0
                    settings.raceEthnicityOrReligion shouldBe 3
                    settings.sexBasedTerms shouldBe 1
                }
            }
        }

        Given("updateAutoModSettings") {

            When("called with updated settings") {
                val engine =
                    MockEngine {
                        respond(
                            content = autoModSettingsJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val settings =
                    resource.updateAutoModSettings(
                        broadcasterId = "123",
                        moderatorId = "100",
                        request = UpdateAutoModSettingsRequest(aggression = 2, bullying = 2),
                    )

                Then("it should call the moderation/automod/settings endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/automod/settings"
                }

                Then("it should use PUT method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Put
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the returned settings") {
                    settings.broadcasterId shouldBe "123"
                    settings.aggression shouldBe 2
                }
            }
        }

        Given("ban") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.ban(
                    broadcasterId = "123",
                    moderatorId = "100",
                    userId = "456",
                    reason = "spam",
                )

                Then("it should call the moderation/bans endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/bans"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        Given("ban - with duration (timed ban)") {

            When("called with a duration parameter") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.ban(
                    broadcasterId = "123",
                    moderatorId = "100",
                    userId = "456",
                    reason = "timeout",
                    duration = 600,
                )

                Then("it should call the moderation/bans endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/bans"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        Given("unban") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.unban(
                    broadcasterId = "123",
                    moderatorId = "100",
                    userId = "456",
                )

                Then("it should call the moderation/bans endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/bans"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }
            }
        }

        Given("resolveUnbanRequest") {

            val resolvedUnbanRequestJson =
                """
                {
                    "data": [
                        {
                            "id": "req-1",
                            "broadcaster_id": "123",
                            "broadcaster_login": "streamer",
                            "broadcaster_name": "Streamer",
                            "moderator_id": "100",
                            "moderator_login": "mod",
                            "moderator_name": "Mod",
                            "user_id": "456",
                            "user_login": "requester",
                            "user_name": "Requester",
                            "text": "please unban me",
                            "status": "approved",
                            "created_at": "2024-01-01T00:00:00Z",
                            "resolved_at": "2024-01-02T00:00:00Z",
                            "resolution_text": "ok"
                        }
                    ]
                }
                """.trimIndent()

            When("called with approval") {
                val engine =
                    MockEngine {
                        respond(
                            content = resolvedUnbanRequestJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val result =
                    resource.resolveUnbanRequest(
                        broadcasterId = "123",
                        moderatorId = "100",
                        unbanRequestId = "req-1",
                        status = UnbanRequestStatus.APPROVED,
                        resolutionText = "ok",
                    )

                Then("it should call the moderation/unban_requests endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/unban_requests"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should pass the unban_request_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["unban_request_id"] shouldBe "req-1"
                }

                Then("it should pass the status parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["status"] shouldBe "approved"
                }

                Then("it should pass the resolution_text parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["resolution_text"] shouldBe "ok"
                }

                Then("it should deserialize the resolved unban request") {
                    result.id shouldBe "req-1"
                    result.status shouldBe "approved"
                    result.resolutionText shouldBe "ok"
                }
            }

            When("called without resolutionText") {
                val engine =
                    MockEngine {
                        respond(
                            content = resolvedUnbanRequestJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.resolveUnbanRequest(
                    broadcasterId = "123",
                    moderatorId = "100",
                    unbanRequestId = "req-1",
                    status = UnbanRequestStatus.DENIED,
                )

                Then("it should not include a resolution_text parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["resolution_text"] shouldBe null
                }

                Then("it should pass the status parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["status"] shouldBe "denied"
                }
            }
        }

        Given("addBlockedTerm") {

            val addBlockedTermResponseJson =
                """
                {
                    "data": [
                        {
                            "broadcaster_id": "123",
                            "moderator_id": "100",
                            "id": "term-new",
                            "text": "newbadword",
                            "created_at": "2024-03-01T00:00:00Z",
                            "updated_at": "2024-03-01T00:00:00Z"
                        }
                    ]
                }
                """.trimIndent()

            When("called with a term to add") {
                val engine =
                    MockEngine {
                        respond(
                            content = addBlockedTermResponseJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val term =
                    resource.addBlockedTerm(
                        broadcasterId = "123",
                        moderatorId = "100",
                        text = "newbadword",
                    )

                Then("it should call the moderation/blocked_terms endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/blocked_terms"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the created blocked term") {
                    term.id shouldBe "term-new"
                    term.text shouldBe "newbadword"
                    term.broadcasterId shouldBe "123"
                }
            }
        }

        Given("removeBlockedTerm") {

            When("called with a term ID to remove") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.removeBlockedTerm(
                    broadcasterId = "123",
                    moderatorId = "100",
                    id = "term-1",
                )

                Then("it should call the moderation/blocked_terms endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/blocked_terms"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "term-1"
                }
            }
        }

        Given("deleteMessage") {

            When("called with a message ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.deleteMessage(
                    broadcasterId = "123",
                    moderatorId = "100",
                    messageId = "msg-1",
                )

                Then("it should call the moderation/chat endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/chat"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should pass the message_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["message_id"] shouldBe "msg-1"
                }
            }

            When("called without a message ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.deleteMessage(
                    broadcasterId = "123",
                    moderatorId = "100",
                )

                Then("it should not include a message_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["message_id"].shouldBeNull()
                }
            }
        }

        Given("addModerator") {

            When("called with broadcaster and user IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.addModerator(broadcasterId = "123", userId = "456")

                Then("it should call the moderation/moderators endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/moderators"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }
            }
        }

        Given("removeModerator") {

            When("called with broadcaster and user IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.removeModerator(broadcasterId = "123", userId = "456")

                Then("it should call the moderation/moderators endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/moderators"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }
            }
        }

        Given("addVip") {

            When("called with broadcaster and user IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.addVip(broadcasterId = "123", userId = "456")

                Then("it should call the channels/vips endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/vips"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }
            }
        }

        Given("removeVip") {

            When("called with broadcaster and user IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.removeVip(broadcasterId = "123", userId = "456")

                Then("it should call the channels/vips endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channels/vips"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }
            }
        }

        val shieldModeJson =
            """
            {
                "data": [
                    {
                        "is_active": true,
                        "moderator_id": "100",
                        "moderator_login": "mod",
                        "moderator_name": "Mod",
                        "last_activated_at": "2024-01-01T00:00:00Z"
                    }
                ]
            }
            """.trimIndent()

        Given("getShieldMode") {

            When("called with broadcaster and moderator IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = shieldModeJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val status = resource.getShieldMode(broadcasterId = "123", moderatorId = "100")

                Then("it should call the moderation/shield_mode endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/shield_mode"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should deserialize the shield mode status") {
                    status.isActive shouldBe true
                    status.moderatorId shouldBe "100"
                    status.moderatorLogin shouldBe "mod"
                    status.moderatorName shouldBe "Mod"
                }
            }
        }

        Given("updateShieldMode") {

            When("called to activate shield mode") {
                val engine =
                    MockEngine {
                        respond(
                            content = shieldModeJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val status =
                    resource.updateShieldMode(
                        broadcasterId = "123",
                        moderatorId = "100",
                        isActive = true,
                    )

                Then("it should call the moderation/shield_mode endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/shield_mode"
                }

                Then("it should use PUT method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Put
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the updated shield mode status") {
                    status.isActive shouldBe true
                    status.moderatorId shouldBe "100"
                }
            }
        }

        Given("warn") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.warn(
                    broadcasterId = "123",
                    moderatorId = "100",
                    userId = "456",
                    reason = "Please follow the rules",
                )

                Then("it should call the moderation/warnings endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/warnings"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }

        Given("sendShoutout") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.sendShoutout(
                    fromId = "123",
                    toId = "456",
                    moderatorId = "100",
                )

                Then("it should call the chat/shoutouts endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/chat/shoutouts"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the from_broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["from_broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the to_broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["to_broadcaster_id"] shouldBe "456"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }
            }
        }

        val suspiciousUserJson =
            """
            {
                "data": [
                    {
                        "user_id": "456",
                        "broadcaster_id": "123",
                        "moderator_id": "100",
                        "updated_at": "2024-01-01T00:00:00Z",
                        "status": "RESTRICTED",
                        "types": ["MANUALLY_ADDED"]
                    }
                ]
            }
            """.trimIndent()

        Given("addSuspiciousStatus") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = suspiciousUserJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val result =
                    resource.addSuspiciousStatus(
                        broadcasterId = "123",
                        moderatorId = "100",
                        userId = "456",
                        status = "RESTRICTED",
                    )

                Then("it should call the moderation/suspicious_users endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/suspicious_users"
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
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the suspicious user status") {
                    result.userId shouldBe "456"
                    result.broadcasterId shouldBe "123"
                    result.status shouldBe "RESTRICTED"
                    result.types shouldBe listOf("MANUALLY_ADDED")
                }
            }
        }

        Given("removeSuspiciousStatus") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = suspiciousUserJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val result =
                    resource.removeSuspiciousStatus(
                        broadcasterId = "123",
                        moderatorId = "100",
                        userId = "456",
                    )

                Then("it should call the moderation/suspicious_users endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/moderation/suspicious_users"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the moderator_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["moderator_id"] shouldBe "100"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                }

                Then("it should deserialize the suspicious user status") {
                    result.userId shouldBe "456"
                    result.status shouldBe "RESTRICTED"
                }
            }
        }
    })
