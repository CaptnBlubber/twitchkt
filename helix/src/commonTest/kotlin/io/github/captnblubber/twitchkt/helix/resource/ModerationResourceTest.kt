package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json

class ModerationResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createResource(engine: MockEngine): ModerationResource {
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
            val config =
                TwitchKtConfig(
                    clientId = testClientId,
                    tokenProvider = TokenProvider { testToken },
                )
            return ModerationResource(HelixHttpClient(httpClient, config))
        }

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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
                        )
                    }
                val resource = createResource(engine)
                resource.getUnbanRequests(
                    broadcasterId = "123",
                    moderatorId = "100",
                    status = "approved",
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
                            headers = jsonHeaders,
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
    })
