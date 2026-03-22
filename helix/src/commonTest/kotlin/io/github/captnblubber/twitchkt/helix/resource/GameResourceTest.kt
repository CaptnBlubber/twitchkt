package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class GameResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = GameResource(createHelixClient(engine))

        Given("getGames") {

            When("called with game IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "33214",
                                            "name": "Fortnite",
                                            "box_art_url": "https://static-cdn.jtvnw.net/ttv-boxart/33214-{width}x{height}.jpg",
                                            "igdb_id": "1905"
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
                val games = resource.getGames(ids = listOf("33214"))

                Then("it should call the games endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/games"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["id"] shouldBe "33214"
                }

                Then("it should deserialize the game") {
                    games.size shouldBe 1
                    games.first().id shouldBe "33214"
                    games.first().name shouldBe "Fortnite"
                    games.first().boxArtUrl shouldBe "https://static-cdn.jtvnw.net/ttv-boxart/33214-{width}x{height}.jpg"
                    games.first().igdbId shouldBe "1905"
                }
            }

            When("called with game names") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "33214",
                                            "name": "Fortnite",
                                            "box_art_url": "https://static-cdn.jtvnw.net/ttv-boxart/33214-{width}x{height}.jpg",
                                            "igdb_id": "1905"
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
                resource.getGames(names = listOf("Fortnite"))

                Then("it should pass the name parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["name"] shouldBe "Fortnite"
                }
            }

            When("called with IGDB IDs") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "33214",
                                            "name": "Fortnite",
                                            "box_art_url": "",
                                            "igdb_id": "1905"
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
                resource.getGames(igdbIds = listOf("1905"))

                Then("it should pass the igdb_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["igdb_id"] shouldBe "1905"
                }
            }

            When("called with no results") {
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
                val games = resource.getGames(ids = listOf("999999"))

                Then("it should return an empty list") {
                    games.size shouldBe 0
                }
            }
        }

        Given("getTopGames") {

            When("called with default parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content =
                                """
                                {
                                    "data": [
                                        {
                                            "id": "509658",
                                            "name": "Just Chatting",
                                            "box_art_url": "https://static-cdn.jtvnw.net/ttv-boxart/509658-{width}x{height}.jpg",
                                            "igdb_id": ""
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
                val games = resource.getTopGames()

                Then("it should call the games/top endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/games/top"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the default first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "20"
                }

                Then("it should not include cursor parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                    request.url.parameters["before"].shouldBeNull()
                }

                Then("it should deserialize the game") {
                    games.size shouldBe 1
                    games.first().id shouldBe "509658"
                    games.first().name shouldBe "Just Chatting"
                }
            }

            When("called with a custom page size and after cursor") {
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
                resource.getTopGames(first = 50, after = "abc123")

                Then("it should pass the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }

                Then("it should pass the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }
            }

            When("called with a before cursor") {
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
                resource.getTopGames(before = "xyz789")

                Then("it should pass the before parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["before"] shouldBe "xyz789"
                }
            }
        }
    })
