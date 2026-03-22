package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class ExtensionResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = ExtensionResource(createHelixClient(engine))

        Given("getTransactions") {

            When("called with an extension ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "tx-123",
                                            "timestamp": "2023-04-11T10:11:12.123Z",
                                            "broadcaster_id": "456",
                                            "broadcaster_login": "testbroadcaster",
                                            "broadcaster_name": "TestBroadcaster",
                                            "user_id": "789",
                                            "user_login": "buyer",
                                            "user_name": "Buyer",
                                            "product_type": "BITS_IN_EXTENSION",
                                            "product_data": {
                                                "sku": "sku-001",
                                                "domain": "twitch.ext.ext-123",
                                                "cost": {
                                                    "amount": 100,
                                                    "type": "bits"
                                                },
                                                "inDevelopment": false,
                                                "displayName": "Test Product",
                                                "expiration": "",
                                                "broadcast": false
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
                val transactions = resource.getTransactions(extensionId = "ext-123")

                Then("it should call the extensions/transactions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/extensions/transactions"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the extension_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["extension_id"] shouldBe "ext-123"
                }

                Then("it should pass the default first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "20"
                }

                Then("it should deserialize the transaction") {
                    transactions.size shouldBe 1
                    transactions.first().id shouldBe "tx-123"
                    transactions.first().broadcasterId shouldBe "456"
                    transactions.first().broadcasterLogin shouldBe "testbroadcaster"
                    transactions.first().broadcasterName shouldBe "TestBroadcaster"
                    transactions.first().userId shouldBe "789"
                    transactions.first().userLogin shouldBe "buyer"
                    transactions.first().userName shouldBe "Buyer"
                    transactions.first().productType shouldBe "BITS_IN_EXTENSION"
                }

                Then("it should deserialize the product data") {
                    val product = transactions.first().productData
                    product.sku shouldBe "sku-001"
                    product.domain shouldBe "twitch.ext.ext-123"
                    product.cost.amount shouldBe 100
                    product.cost.type shouldBe "bits"
                    product.inDevelopment shouldBe false
                    product.displayName shouldBe "Test Product"
                    product.broadcast shouldBe false
                }
            }

            When("called with transaction IDs filter") {
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
                resource.getTransactions(extensionId = "ext-123", ids = listOf("tx-1", "tx-2"))

                Then("it should pass the id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("tx-1", "tx-2")
                }
            }

            When("called with a custom first and after cursor") {
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
                resource.getTransactions(extensionId = "ext-123", first = 50, after = "cursor-abc")

                Then("it should pass the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }

                Then("it should pass the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "cursor-abc"
                }
            }

            When("called without an after cursor") {
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
                resource.getTransactions(extensionId = "ext-123")

                Then("it should not include an after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }
            }
        }
    })
