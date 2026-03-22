package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class AnalyticsResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = AnalyticsResource(createHelixClient(engine))

        Given("getExtensionAnalytics") {

            When("called with default parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "extension_id": "ext-abc123",
                                            "URL": "https://twitch.tv/analytics/ext-abc123.csv",
                                            "type": "overview_v2",
                                            "date_range": {
                                                "started_at": "2023-01-01T00:00:00Z",
                                                "ended_at": "2023-01-31T00:00:00Z"
                                            }
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
                val reports = resource.getExtensionAnalytics()

                Then("it should call the analytics/extensions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/analytics/extensions"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the default first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "20"
                }

                Then("it should not include optional parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["extension_id"].shouldBeNull()
                    request.url.parameters["type"].shouldBeNull()
                    request.url.parameters["started_at"].shouldBeNull()
                    request.url.parameters["ended_at"].shouldBeNull()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should deserialize the analytics report") {
                    reports.size shouldBe 1
                    reports.first().extensionId shouldBe "ext-abc123"
                    reports.first().url shouldBe "https://twitch.tv/analytics/ext-abc123.csv"
                    reports.first().type shouldBe "overview_v2"
                    reports.first().dateRange.startedAt shouldBe "2023-01-01T00:00:00Z"
                    reports.first().dateRange.endedAt shouldBe "2023-01-31T00:00:00Z"
                }
            }

            When("called with an extension ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "extension_id": "ext-specific",
                                            "URL": "https://twitch.tv/analytics/ext-specific.csv",
                                            "type": "overview_v2",
                                            "date_range": {
                                                "started_at": "2023-01-01T00:00:00Z",
                                                "ended_at": "2023-01-31T00:00:00Z"
                                            }
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
                resource.getExtensionAnalytics(extensionId = "ext-specific")

                Then("it should pass the extension_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["extension_id"] shouldBe "ext-specific"
                }
            }

            When("called with all optional parameters") {
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
                resource.getExtensionAnalytics(
                    extensionId = "ext-123",
                    type = "overview_v2",
                    startedAt = "2023-01-01T00:00:00Z",
                    endedAt = "2023-01-31T00:00:00Z",
                    first = 50,
                    after = "cursor-abc",
                )

                Then("it should pass all parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["extension_id"] shouldBe "ext-123"
                    request.url.parameters["type"] shouldBe "overview_v2"
                    request.url.parameters["started_at"] shouldBe "2023-01-01T00:00:00Z"
                    request.url.parameters["ended_at"] shouldBe "2023-01-31T00:00:00Z"
                    request.url.parameters["first"] shouldBe "50"
                    request.url.parameters["after"] shouldBe "cursor-abc"
                }
            }
        }
    })
