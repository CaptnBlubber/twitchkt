package io.github.captnblubber.twitchkt.helix.internal

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.ScopeProvider
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TestItem(
    val id: String,
    val name: String,
)

@Serializable
data class TestRequestBody(
    val title: String,
)

class HelixHttpClientTest :
    BehaviorSpec({

        coroutineTestScope = true

        val testToken = "test-token-123"
        val testClientId = "test-client-id"
        val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

        fun createConfig(baseUrl: String = "https://api.twitch.tv/helix") =
            TwitchKtConfig(
                clientId = testClientId,
                tokenProvider = TokenProvider { testToken },
                helixBaseUrl = baseUrl,
            )

        fun createClient(
            engine: MockEngine,
            baseUrl: String = "https://api.twitch.tv/helix",
        ): HelixHttpClient {
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
            return HelixHttpClient(httpClient, createConfig(baseUrl))
        }

        Given("a GET request") {

            When("the request is made") {
                val engine =
                    MockEngine { request ->
                        respond(
                            content = """{"data":[{"id":"1","name":"test"}]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                val result = client.get<TestItem>("users")

                Then("it should add the Authorization header") {
                    val request = engine.requestHistory.first()
                    request.headers["Authorization"] shouldBe "Bearer $testToken"
                }

                Then("it should add the Client-Id header") {
                    val request = engine.requestHistory.first()
                    request.headers["Client-Id"] shouldBe testClientId
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should deserialize the response") {
                    result.data shouldHaveSize 1
                    result.data.first().id shouldBe "1"
                    result.data.first().name shouldBe "test"
                }
            }

            When("query parameters are provided") {
                val engine =
                    MockEngine { request ->
                        respond(
                            content = """{"data":[{"id":"1","name":"test"}]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                client.get<TestItem>(
                    "users",
                    params = listOf("id" to "123", "id" to "456", "login" to "testuser"),
                )

                Then("it should append all query parameters including repeated keys") {
                    val request = engine.requestHistory.first()
                    val url = request.url
                    url.parameters.getAll("id") shouldBe listOf("123", "456")
                    url.parameters["login"] shouldBe "testuser"
                }
            }

            When("the endpoint is called with base URL") {
                val engine =
                    MockEngine { request ->
                        respond(
                            content = """{"data":[]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine, "https://api.twitch.tv/helix")
                client.get<TestItem>("channels")

                Then("it should construct the full URL correctly") {
                    val request = engine.requestHistory.first()
                    request.url.toString() shouldBe "https://api.twitch.tv/helix/channels"
                }
            }
        }

        Given("a POST request") {

            When("a JSON body is provided") {
                val engine =
                    MockEngine { request ->
                        respond(
                            content = """{"data":[{"id":"new","name":"created"}]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                val result =
                    client.post<TestItem>(
                        "polls",
                        body = client.encodeBody(TestRequestBody(title = "Test Poll")),
                    )

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the response") {
                    result.data.first().id shouldBe "new"
                    result.data.first().name shouldBe "created"
                }
            }
        }

        Given("a POST request with no content response") {

            When("the server responds with 204") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val client = createClient(engine)

                Then("postNoContent should return successfully") {
                    client.postNoContent("chat/announcements")
                }
            }
        }

        Given("a PATCH request") {

            When("a JSON body is provided") {
                val engine =
                    MockEngine { request ->
                        respond(
                            content = """{"data":[{"id":"1","name":"updated"}]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                val result =
                    client.patch<TestItem>(
                        "channels",
                        body = client.encodeBody(TestRequestBody(title = "New Title")),
                    )

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should deserialize the response") {
                    result.data.first().name shouldBe "updated"
                }
            }
        }

        Given("a DELETE request") {

            When("the request is made") {
                val engine =
                    MockEngine { request ->
                        respond(
                            content = """{"data":[]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                client.delete<TestItem>("predictions", params = listOf("id" to "abc"))

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should include query parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "abc"
                }
            }

            When("the server responds with 204 for deleteNoContent") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val client = createClient(engine)

                Then("deleteNoContent should return successfully") {
                    client.deleteNoContent("schedule/segment", params = listOf("id" to "123"))
                }
            }
        }

        Given("HTTP error responses") {

            data class ErrorCase(
                val statusCode: HttpStatusCode,
                val expectedType: String,
            )

            val errorCases =
                listOf(
                    ErrorCase(HttpStatusCode.BadRequest, "BadRequest"),
                    ErrorCase(HttpStatusCode.Unauthorized, "Unauthorized"),
                    ErrorCase(HttpStatusCode.Forbidden, "Forbidden"),
                    ErrorCase(HttpStatusCode.NotFound, "NotFound"),
                    ErrorCase(HttpStatusCode.Conflict, "Conflict"),
                    ErrorCase(HttpStatusCode.UnprocessableEntity, "UnprocessableEntity"),
                )

            errorCases.forEach { (statusCode, expectedType) ->
                When("the server responds with ${statusCode.value} ($expectedType)") {
                    val errorBody = """{"error":"$expectedType","message":"test error"}"""
                    val engine =
                        MockEngine {
                            respond(
                                content = errorBody,
                                status = statusCode,
                                headers = jsonHeaders,
                            )
                        }
                    val client = createClient(engine)

                    Then("it should throw TwitchApiException.$expectedType") {
                        val exception =
                            shouldThrow<TwitchApiException> {
                                client.get<TestItem>("test")
                            }
                        when (expectedType) {
                            "BadRequest" -> exception.shouldBeInstanceOf<TwitchApiException.BadRequest>()
                            "Unauthorized" -> exception.shouldBeInstanceOf<TwitchApiException.Unauthorized>()
                            "Forbidden" -> exception.shouldBeInstanceOf<TwitchApiException.Forbidden>()
                            "NotFound" -> exception.shouldBeInstanceOf<TwitchApiException.NotFound>()
                            "Conflict" -> exception.shouldBeInstanceOf<TwitchApiException.Conflict>()
                            "UnprocessableEntity" -> exception.shouldBeInstanceOf<TwitchApiException.UnprocessableEntity>()
                        }
                        exception.message shouldBe errorBody
                    }
                }
            }

            When("the server responds with 429 (RateLimited)") {
                val errorBody = """{"error":"Too Many Requests","message":"rate limited"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.TooManyRequests,
                            headers =
                                headersOf(
                                    HttpHeaders.ContentType to listOf("application/json"),
                                    "Ratelimit-Reset" to listOf("9999999999"),
                                ),
                        )
                    }
                val client = createClient(engine)

                Then("it should throw TwitchApiException.RateLimited with retryAfterMs") {
                    val exception =
                        shouldThrow<TwitchApiException.RateLimited> {
                            client.get<TestItem>("test")
                        }
                    exception.message shouldBe errorBody
                    (exception.retryAfterMs > 0) shouldBe true
                }
            }

            When("the server responds with 500 (ServerError)") {
                val errorBody = """{"error":"Internal Server Error"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.InternalServerError,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw TwitchApiException.ServerError with status code") {
                    val exception =
                        shouldThrow<TwitchApiException.ServerError> {
                            client.get<TestItem>("test")
                        }
                    exception.statusCode shouldBe 500
                    exception.message shouldBe errorBody
                }
            }

            When("the server responds with 503") {
                val errorBody = """{"error":"Service Unavailable"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.ServiceUnavailable,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw TwitchApiException.ServerError with the actual status code") {
                    val exception =
                        shouldThrow<TwitchApiException.ServerError> {
                            client.get<TestItem>("test")
                        }
                    exception.statusCode shouldBe 503
                }
            }
        }

        Given("a successful response with pagination and total") {

            When("the response includes pagination and total fields") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [{"id":"1","name":"first"},{"id":"2","name":"second"}],
                                    "pagination": {"cursor": "abc123"},
                                    "total": 50
                                }
                                """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                val result = client.get<TestItem>("followers")

                Then("it should deserialize all data items") {
                    result.data shouldHaveSize 2
                    result.data[0].id shouldBe "1"
                    result.data[1].id shouldBe "2"
                }

                Then("it should deserialize the pagination cursor") {
                    result.pagination?.cursor shouldBe "abc123"
                }

                Then("it should deserialize the total count") {
                    result.total shouldBe 50
                }
            }
        }

        Given("pagination via paginate()") {

            When("multiple pages of results exist") {
                var requestCount = 0
                val engine =
                    MockEngine { request ->
                        requestCount++
                        val cursor = request.url.parameters["after"]
                        when (cursor) {
                            null -> {
                                respond(
                                    content =
                                        """
                                        {
                                            "data": [{"id":"1","name":"first"},{"id":"2","name":"second"}],
                                            "pagination": {"cursor": "page2"}
                                        }
                                        """.trimIndent(),
                                    status = HttpStatusCode.OK,
                                    headers = jsonHeaders,
                                )
                            }

                            "page2" -> {
                                respond(
                                    content =
                                        """
                                        {
                                            "data": [{"id":"3","name":"third"}],
                                            "pagination": {"cursor": "page3"}
                                        }
                                        """.trimIndent(),
                                    status = HttpStatusCode.OK,
                                    headers = jsonHeaders,
                                )
                            }

                            "page3" -> {
                                respond(
                                    content =
                                        """
                                        {
                                            "data": [{"id":"4","name":"fourth"}],
                                            "pagination": {}
                                        }
                                        """.trimIndent(),
                                    status = HttpStatusCode.OK,
                                    headers = jsonHeaders,
                                )
                            }

                            else -> {
                                error("Unexpected cursor: $cursor")
                            }
                        }
                    }
                val client = createClient(engine)
                val items =
                    client
                        .paginate<TestItem>(
                            "followers",
                            params = listOf("broadcaster_id" to "12345"),
                            pageSize = 2,
                        ).toList()

                Then("it should collect all items across all pages") {
                    items shouldHaveSize 4
                    items.map { it.id } shouldBe listOf("1", "2", "3", "4")
                }

                Then("it should make requests until pagination cursor is null") {
                    requestCount shouldBe 3
                }

                Then("each request should include the first parameter") {
                    engine.requestHistory.forEach { request ->
                        request.url.parameters["first"] shouldBe "2"
                    }
                }

                Then("each request should include the original params") {
                    engine.requestHistory.forEach { request ->
                        request.url.parameters["broadcaster_id"] shouldBe "12345"
                    }
                }

                Then("subsequent requests should include the after cursor") {
                    engine.requestHistory[0].url.parameters["after"] shouldBe null
                    engine.requestHistory[1].url.parameters["after"] shouldBe "page2"
                    engine.requestHistory[2].url.parameters["after"] shouldBe "page3"
                }
            }

            When("only one page of results exists") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data":[{"id":"1","name":"only"}]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                val items = client.paginate<TestItem>("streams").toList()

                Then("it should return just the single page of results") {
                    items shouldHaveSize 1
                    items.first().name shouldBe "only"
                }

                Then("it should only make one request") {
                    engine.requestHistory shouldHaveSize 1
                }
            }
        }

        Given("response with unknown JSON fields") {

            When("the response contains extra fields not in the model") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data":[{"id":"1","name":"test","extra_field":"ignored"}],"unknown":true}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)
                val result = client.get<TestItem>("users")

                Then("it should deserialize successfully ignoring unknown keys") {
                    result.data shouldHaveSize 1
                    result.data.first().id shouldBe "1"
                }
            }
        }

        Given("error responses on POST") {

            When("POST returns 400") {
                val errorBody = """{"error":"BadRequest","message":"invalid body"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.BadRequest,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw BadRequest") {
                    val exception =
                        shouldThrow<TwitchApiException.BadRequest> {
                            client.post<TestItem>(
                                "polls",
                                body = client.encodeBody(TestRequestBody(title = "Test")),
                            )
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on PATCH") {

            When("PATCH returns 403") {
                val errorBody = """{"error":"Forbidden","message":"not allowed"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.Forbidden,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw Forbidden") {
                    val exception =
                        shouldThrow<TwitchApiException.Forbidden> {
                            client.patch<TestItem>(
                                "channels",
                                body = client.encodeBody(TestRequestBody(title = "New Title")),
                            )
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on DELETE") {

            When("DELETE returns 404") {
                val errorBody = """{"error":"NotFound","message":"resource not found"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.NotFound,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw NotFound") {
                    val exception =
                        shouldThrow<TwitchApiException.NotFound> {
                            client.delete<TestItem>("predictions", params = listOf("id" to "abc"))
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on PUT") {

            When("PUT returns 401") {
                val errorBody = """{"error":"Unauthorized","message":"invalid token"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.Unauthorized,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw Unauthorized") {
                    val exception =
                        shouldThrow<TwitchApiException.Unauthorized> {
                            client.put<TestItem>(
                                "channels",
                                body = client.encodeBody(TestRequestBody(title = "Test")),
                            )
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on postNoContent") {

            When("postNoContent returns 400") {
                val errorBody = """{"error":"BadRequest","message":"invalid request"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.BadRequest,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw BadRequest") {
                    val exception =
                        shouldThrow<TwitchApiException.BadRequest> {
                            client.postNoContent("chat/announcements")
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on patchNoContent") {

            When("patchNoContent returns 403") {
                val errorBody = """{"error":"Forbidden","message":"forbidden"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.Forbidden,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw Forbidden") {
                    val exception =
                        shouldThrow<TwitchApiException.Forbidden> {
                            client.patchNoContent("channels")
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on deleteNoContent") {

            When("deleteNoContent returns 401") {
                val errorBody = """{"error":"Unauthorized","message":"bad token"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.Unauthorized,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw Unauthorized") {
                    val exception =
                        shouldThrow<TwitchApiException.Unauthorized> {
                            client.deleteNoContent("schedule/segment", params = listOf("id" to "123"))
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("error responses on putNoContent") {

            When("putNoContent returns 422") {
                val errorBody = """{"error":"UnprocessableEntity","message":"invalid entity"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.UnprocessableEntity,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw UnprocessableEntity") {
                    val exception =
                        shouldThrow<TwitchApiException.UnprocessableEntity> {
                            client.putNoContent("channels")
                        }
                    exception.message shouldBe errorBody
                }
            }
        }

        Given("RateLimited without Ratelimit-Reset header") {

            When("429 response has no Ratelimit-Reset header") {
                val errorBody = """{"error":"Too Many Requests","message":"rate limited"}"""
                val engine =
                    MockEngine {
                        respond(
                            content = errorBody,
                            status = HttpStatusCode.TooManyRequests,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should have retryAfterMs of 0") {
                    val exception =
                        shouldThrow<TwitchApiException.RateLimited> {
                            client.get<TestItem>("test")
                        }
                    exception.retryAfterMs shouldBe 0
                }
            }
        }

        Given("pagination error on second page") {

            When("first page succeeds but second page returns 500") {
                val engine =
                    MockEngine { request ->
                        val cursor = request.url.parameters["after"]
                        when (cursor) {
                            null -> {
                                respond(
                                    content =
                                        """
                                        {
                                            "data": [{"id":"1","name":"first"}],
                                            "pagination": {"cursor": "page2"}
                                        }
                                        """.trimIndent(),
                                    status = HttpStatusCode.OK,
                                    headers = jsonHeaders,
                                )
                            }

                            else -> {
                                respond(
                                    content = """{"error":"Internal Server Error"}""",
                                    status = HttpStatusCode.InternalServerError,
                                    headers = jsonHeaders,
                                )
                            }
                        }
                    }
                val client = createClient(engine)

                Then("it should throw ServerError when collecting the flow") {
                    val exception =
                        shouldThrow<TwitchApiException.ServerError> {
                            client.paginate<TestItem>("followers", pageSize = 1).toList()
                        }
                    exception.statusCode shouldBe 500
                }
            }
        }

        Given("getPage with invalid pageSize") {

            When("pageSize is zero") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data":[]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw IllegalArgumentException") {
                    shouldThrow<IllegalArgumentException> {
                        client.getPage<TestItem>("followers", pageSize = 0)
                    }
                }
            }

            When("pageSize is negative") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data":[]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should throw IllegalArgumentException") {
                    shouldThrow<IllegalArgumentException> {
                        client.getPage<TestItem>("followers", pageSize = -5)
                    }
                }
            }
        }

        Given("validateAnyScope") {

            fun createClientWithScopes(scopes: Set<TwitchScope>): HelixHttpClient {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data":[]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
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
                        scopeProvider = ScopeProvider { scopes },
                    )
                return HelixHttpClient(httpClient, config)
            }

            When("at least one scope matches") {
                val client =
                    createClientWithScopes(
                        setOf(TwitchScope.CHANNEL_READ_POLLS, TwitchScope.BITS_READ),
                    )

                Then("it should not throw") {
                    shouldNotThrowAny {
                        client.validateAnyScope(
                            TwitchScope.CHANNEL_MANAGE_POLLS,
                            TwitchScope.CHANNEL_READ_POLLS,
                        )
                    }
                }
            }

            When("no scopes match") {
                val client =
                    createClientWithScopes(
                        setOf(TwitchScope.BITS_READ),
                    )

                Then("it should throw MissingScope") {
                    shouldThrow<TwitchApiException.MissingScope> {
                        client.validateAnyScope(
                            TwitchScope.CHANNEL_READ_POLLS,
                            TwitchScope.CHANNEL_MANAGE_POLLS,
                        )
                    }
                }
            }

            When("no ScopeProvider is configured") {
                val engine =
                    MockEngine {
                        respond(
                            content = """{"data":[]}""",
                            status = HttpStatusCode.OK,
                            headers = jsonHeaders,
                        )
                    }
                val client = createClient(engine)

                Then("it should be a no-op") {
                    shouldNotThrowAny {
                        client.validateAnyScope(
                            TwitchScope.CHANNEL_READ_POLLS,
                            TwitchScope.CHANNEL_MANAGE_POLLS,
                        )
                    }
                }
            }
        }
    })
