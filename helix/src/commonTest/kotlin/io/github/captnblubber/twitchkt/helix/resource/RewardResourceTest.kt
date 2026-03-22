package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.model.RedemptionSort
import io.github.captnblubber.twitchkt.helix.model.RedemptionStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList

class RewardResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = RewardResource(createHelixClient(engine))

        val redemptionJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "id": "red-1",
                        "user_id": "456",
                        "user_login": "viewer",
                        "user_name": "Viewer",
                        "user_input": "hello",
                        "status": "UNFULFILLED",
                        "redeemed_at": "2024-01-01T00:00:00Z",
                        "reward": {
                            "id": "reward-1",
                            "title": "VIP",
                            "prompt": "Become a VIP!",
                            "cost": 5000
                        }
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val redemptionLastPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "id": "red-2",
                        "user_id": "789",
                        "user_login": "viewer2",
                        "user_name": "Viewer2",
                        "status": "FULFILLED",
                        "redeemed_at": "2024-02-01T00:00:00Z",
                        "reward": {
                            "id": "reward-1",
                            "title": "VIP",
                            "cost": 5000
                        }
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val redemptionSecondPageJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "id": "red-1",
                        "user_id": "456",
                        "user_login": "viewer",
                        "user_name": "Viewer",
                        "status": "UNFULFILLED",
                        "redeemed_at": "2024-01-01T00:00:00Z",
                        "reward": {
                            "id": "reward-1",
                            "title": "VIP",
                            "cost": 5000
                        }
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        val customRewardJson =
            """
            {
                "data": [
                    {
                        "broadcaster_id": "123",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "id": "reward-1",
                        "title": "VIP",
                        "prompt": "Become a VIP!",
                        "cost": 5000,
                        "image": null,
                        "default_image": {
                            "url_1x": "https://example.com/1x.png",
                            "url_2x": "https://example.com/2x.png",
                            "url_4x": "https://example.com/4x.png"
                        },
                        "background_color": "#FA1ED2",
                        "is_enabled": true,
                        "is_user_input_required": true,
                        "max_per_stream_setting": {
                            "is_enabled": false,
                            "max_per_stream": 0
                        },
                        "max_per_user_per_stream_setting": {
                            "is_enabled": false,
                            "max_per_user_per_stream": 0
                        },
                        "global_cooldown_setting": {
                            "is_enabled": false,
                            "global_cooldown_seconds": 0
                        },
                        "is_paused": false,
                        "is_in_stock": true,
                        "should_redemptions_skip_request_queue": false
                    }
                ]
            }
            """.trimIndent()

        Given("list") {

            When("called with a broadcaster ID") {
                val engine =
                    MockEngine {
                        respond(
                            content = customRewardJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val rewards = resource.list(broadcasterId = "123")

                Then("it should call the channel_points/custom_rewards endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channel_points/custom_rewards"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the reward") {
                    rewards.size shouldBe 1
                    rewards.first().id shouldBe "reward-1"
                    rewards.first().title shouldBe "VIP"
                    rewards.first().cost shouldBe 5000
                    rewards.first().isUserInputRequired shouldBe true
                    rewards.first().backgroundColor shouldBe "#FA1ED2"
                }
            }

            When("called with onlyManageableRewards") {
                val engine =
                    MockEngine {
                        respond(
                            content = customRewardJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.list(broadcasterId = "123", onlyManageableRewards = true)

                Then("it should pass only_manageable_rewards parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["only_manageable_rewards"] shouldBe "true"
                }
            }

            When("called with specific IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = customRewardJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.list(broadcasterId = "123", ids = listOf("reward-1", "reward-2"))

                Then("it should pass multiple id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("reward-1", "reward-2")
                }
            }
        }

        Given("create") {

            When("called with a reward request") {
                val engine =
                    MockEngine {
                        respond(
                            content = customRewardJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val reward =
                    resource.create(
                        broadcasterId = "123",
                        request =
                            io.github.captnblubber.twitchkt.helix.model.CreateRewardRequest(
                                title = "VIP",
                                cost = 5000,
                            ),
                    )

                Then("it should call the channel_points/custom_rewards endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channel_points/custom_rewards"
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

                Then("it should deserialize the created reward") {
                    reward.id shouldBe "reward-1"
                    reward.title shouldBe "VIP"
                    reward.cost shouldBe 5000
                }
            }
        }

        Given("update") {

            When("called with an update request") {
                val engine =
                    MockEngine {
                        respond(
                            content = customRewardJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val reward =
                    resource.update(
                        broadcasterId = "123",
                        rewardId = "reward-1",
                        request =
                            io.github.captnblubber.twitchkt.helix.model.UpdateRewardRequest(
                                title = "VIP",
                                cost = 5000,
                            ),
                    )

                Then("it should call the channel_points/custom_rewards endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channel_points/custom_rewards"
                }

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "reward-1"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the updated reward") {
                    reward.id shouldBe "reward-1"
                    reward.title shouldBe "VIP"
                }
            }
        }

        Given("delete") {

            When("called with broadcaster and reward IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.delete(broadcasterId = "123", id = "reward-1")

                Then("it should call the channel_points/custom_rewards endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channel_points/custom_rewards"
                }

                Then("it should use DELETE method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Delete
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "reward-1"
                }
            }
        }

        Given("updateRedemptionStatus") {

            When("called with redemption IDs and status") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val redemptions =
                    resource.updateRedemptionStatus(
                        broadcasterId = "123",
                        rewardId = "reward-1",
                        redemptionIds = listOf("red-1", "red-2"),
                        status = RedemptionStatus.FULFILLED,
                    )

                Then("it should call the channel_points/custom_rewards/redemptions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channel_points/custom_rewards/redemptions"
                }

                Then("it should use PATCH method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Patch
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should pass the reward_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["reward_id"] shouldBe "reward-1"
                }

                Then("it should pass multiple id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("red-1", "red-2")
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the redemptions") {
                    redemptions.size shouldBe 1
                    redemptions.first().id shouldBe "red-2"
                    redemptions.first().status shouldBe "FULFILLED"
                }
            }

            When("called with single redemption ID convenience overload") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.updateRedemptionStatus(
                    broadcasterId = "123",
                    rewardId = "reward-1",
                    redemptionId = "red-1",
                    status = RedemptionStatus.CANCELED,
                )

                Then("it should pass the single id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters.getAll("id") shouldBe listOf("red-1")
                }
            }
        }

        Given("getAllRedemptions") {

            When("called with broadcasterId and rewardId") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val redemptions =
                    resource
                        .getAllRedemptions(
                            broadcasterId = "123",
                            rewardId = "reward-1",
                            status = RedemptionStatus.FULFILLED,
                        ).toList()

                Then("it should call the channel_points/custom_rewards/redemptions endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/channel_points/custom_rewards/redemptions"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass required parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                    request.url.parameters["reward_id"] shouldBe "reward-1"
                    request.url.parameters["status"] shouldBe "FULFILLED"
                }

                Then("it should deserialize the redemption") {
                    redemptions.size shouldBe 1
                    redemptions.first().id shouldBe "red-2"
                    redemptions.first().userId shouldBe "789"
                    redemptions.first().status shouldBe "FULFILLED"
                }
            }
        }

        Given("getRedemptions") {

            When("called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page =
                    resource.getRedemptions(
                        broadcasterId = "123",
                        rewardId = "reward-1",
                        status = RedemptionStatus.UNFULFILLED,
                    )

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the redemption data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "red-1"
                    page.data.first().userInput shouldBe "hello"
                    page.data
                        .first()
                        .reward.title shouldBe "VIP"
                    page.data
                        .first()
                        .reward.cost shouldBe 5000
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page =
                    resource.getRedemptions(
                        broadcasterId = "123",
                        rewardId = "reward-1",
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

            When("called and there is no next page") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page =
                    resource.getRedemptions(
                        broadcasterId = "123",
                        rewardId = "reward-1",
                    )

                Then("cursor should be null") {
                    page.cursor.shouldBeNull()
                }
            }

            When("called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getRedemptions(
                    broadcasterId = "123",
                    rewardId = "reward-1",
                    pageSize = 25,
                )

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "25"
                }
            }

            When("called with sort parameter") {
                val engine =
                    MockEngine {
                        respond(
                            content = redemptionJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getRedemptions(
                    broadcasterId = "123",
                    rewardId = "reward-1",
                    sort = RedemptionSort.NEWEST,
                )

                Then("it should pass the sort parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["sort"] shouldBe "NEWEST"
                }
            }
        }
    })
