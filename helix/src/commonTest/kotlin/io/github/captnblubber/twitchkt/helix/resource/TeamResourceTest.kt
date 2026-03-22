package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class TeamResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = TeamResource(createHelixClient(engine))

        Given("getChannelTeams") {

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
                                            "broadcaster_login": "teststreamer",
                                            "broadcaster_name": "TestStreamer",
                                            "background_image_url": "https://example.com/bg.png",
                                            "banner": "https://example.com/banner.png",
                                            "created_at": "2020-01-01T00:00:00Z",
                                            "updated_at": "2023-01-01T00:00:00Z",
                                            "info": "A cool team",
                                            "thumbnail_url": "https://example.com/thumb.png",
                                            "team_display_name": "Cool Team",
                                            "team_name": "coolteam",
                                            "id": "team-1"
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
                val teams = resource.getChannelTeams(broadcasterId = "123")

                Then("it should call the teams/channel endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/teams/channel"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the team") {
                    teams.size shouldBe 1
                    teams.first().broadcasterId shouldBe "123"
                    teams.first().broadcasterLogin shouldBe "teststreamer"
                    teams.first().broadcasterName shouldBe "TestStreamer"
                    teams.first().teamDisplayName shouldBe "Cool Team"
                    teams.first().teamName shouldBe "coolteam"
                    teams.first().id shouldBe "team-1"
                    teams.first().info shouldBe "A cool team"
                    teams.first().thumbnailUrl shouldBe "https://example.com/thumb.png"
                }
            }

            When("the broadcaster is not a member of any team") {
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
                val teams = resource.getChannelTeams(broadcasterId = "999")

                Then("it should return an empty list") {
                    teams.size shouldBe 0
                }
            }
        }

        Given("getTeam") {

            When("called with a team name") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "users": [
                                                {
                                                    "user_id": "456",
                                                    "user_login": "member1",
                                                    "user_name": "Member1"
                                                }
                                            ],
                                            "background_image_url": "https://example.com/bg.png",
                                            "banner": "https://example.com/banner.png",
                                            "created_at": "2020-01-01T00:00:00Z",
                                            "updated_at": "2023-01-01T00:00:00Z",
                                            "info": "Team description",
                                            "thumbnail_url": "https://example.com/thumb.png",
                                            "team_name": "coolteam",
                                            "team_display_name": "Cool Team",
                                            "id": "team-1"
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
                val team = resource.getTeam(name = "coolteam")

                Then("it should call the teams endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/teams"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the name parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["name"] shouldBe "coolteam"
                }

                Then("it should deserialize the team") {
                    team!!.teamName shouldBe "coolteam"
                    team.teamDisplayName shouldBe "Cool Team"
                    team.id shouldBe "team-1"
                    team.info shouldBe "Team description"
                }

                Then("it should deserialize the team members") {
                    team!!.users.size shouldBe 1
                    team.users.first().userId shouldBe "456"
                    team.users.first().userLogin shouldBe "member1"
                    team.users.first().userName shouldBe "Member1"
                }
            }

            When("called with a team ID") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "users": [],
                                            "background_image_url": null,
                                            "banner": null,
                                            "created_at": "2020-01-01T00:00:00Z",
                                            "updated_at": "2023-01-01T00:00:00Z",
                                            "info": "Team info",
                                            "thumbnail_url": "https://example.com/thumb.png",
                                            "team_name": "otherteam",
                                            "team_display_name": "Other Team",
                                            "id": "team-2"
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
                resource.getTeam(id = "team-2")

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "team-2"
                }
            }

            When("the team is not found") {
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
                val team = resource.getTeam(name = "nonexistent")

                Then("it should return null") {
                    team.shouldBeNull()
                }
            }
        }
    })
