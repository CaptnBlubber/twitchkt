package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class CharityResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = CharityResource(createHelixClient(engine))

        Given("getCampaign") {

            When("called with a broadcaster ID that has an active campaign") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "campaign-123",
                                            "broadcaster_id": "123",
                                            "broadcaster_login": "testbroadcaster",
                                            "broadcaster_name": "TestBroadcaster",
                                            "charity_name": "Example Charity",
                                            "charity_description": "A great charity",
                                            "charity_logo": "https://example.com/logo.png",
                                            "charity_website": "https://example.com",
                                            "current_amount": {
                                                "value": 5000,
                                                "decimal_places": 2,
                                                "currency": "USD"
                                            },
                                            "target_amount": {
                                                "value": 150000,
                                                "decimal_places": 2,
                                                "currency": "USD"
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
                val campaign = resource.getCampaign(broadcasterId = "123")

                Then("it should call the charity/campaigns endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/charity/campaigns"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the campaign") {
                    campaign?.id shouldBe "campaign-123"
                    campaign?.broadcasterId shouldBe "123"
                    campaign?.broadcasterLogin shouldBe "testbroadcaster"
                    campaign?.broadcasterName shouldBe "TestBroadcaster"
                    campaign?.charityName shouldBe "Example Charity"
                    campaign?.charityDescription shouldBe "A great charity"
                    campaign?.charityLogo shouldBe "https://example.com/logo.png"
                    campaign?.charityWebsite shouldBe "https://example.com"
                }

                Then("it should deserialize the current amount") {
                    campaign?.currentAmount?.value shouldBe 5000
                    campaign?.currentAmount?.decimalPlaces shouldBe 2
                    campaign?.currentAmount?.currency shouldBe "USD"
                }

                Then("it should deserialize the target amount") {
                    campaign?.targetAmount?.value shouldBe 150000
                    campaign?.targetAmount?.decimalPlaces shouldBe 2
                    campaign?.targetAmount?.currency shouldBe "USD"
                }
            }

            When("called with a broadcaster ID that has no active campaign") {
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
                val campaign = resource.getCampaign(broadcasterId = "999")

                Then("it should return null") {
                    campaign.shouldBeNull()
                }
            }
        }

        Given("getDonations") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "donation-1",
                                            "campaign_id": "campaign-123",
                                            "user_id": "456",
                                            "user_login": "donor",
                                            "user_name": "Donor",
                                            "amount": {
                                                "value": 1000,
                                                "decimal_places": 2,
                                                "currency": "USD"
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
                val donations = resource.getDonations(broadcasterId = "123")

                Then("it should call the charity/donations endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/charity/donations"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the default first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "20"
                }

                Then("it should deserialize the donation") {
                    donations.size shouldBe 1
                    donations.first().id shouldBe "donation-1"
                    donations.first().campaignId shouldBe "campaign-123"
                    donations.first().userId shouldBe "456"
                    donations.first().userLogin shouldBe "donor"
                    donations.first().userName shouldBe "Donor"
                }

                Then("it should deserialize the donation amount") {
                    donations.first().amount.value shouldBe 1000
                    donations.first().amount.decimalPlaces shouldBe 2
                    donations.first().amount.currency shouldBe "USD"
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
                resource.getDonations(broadcasterId = "123", first = 50, after = "cursor-abc")

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
                resource.getDonations(broadcasterId = "123")

                Then("it should not include an after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }
            }
        }
    })
