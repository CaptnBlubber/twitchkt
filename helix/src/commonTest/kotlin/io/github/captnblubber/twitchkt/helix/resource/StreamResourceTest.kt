package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.model.StreamType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList

class StreamResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = StreamResource(createHelixClient(engine))

        val streamJson =
            """
            {
                "data": [
                    {
                        "id": "s-1",
                        "user_id": "456",
                        "user_login": "streamer",
                        "user_name": "Streamer",
                        "game_id": "509658",
                        "game_name": "Just Chatting",
                        "type": "live",
                        "title": "Hello!",
                        "viewer_count": 1000,
                        "started_at": "2024-01-01T00:00:00Z",
                        "language": "en",
                        "thumbnail_url": "https://example.com/thumb.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val streamLastPageJson =
            """
            {
                "data": [
                    {
                        "id": "s-2",
                        "user_id": "789",
                        "user_login": "streamer2",
                        "user_name": "Streamer2",
                        "type": "live",
                        "title": "Gaming",
                        "viewer_count": 500,
                        "language": "de",
                        "thumbnail_url": "https://example.com/thumb2.jpg"
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val streamSecondPageJson =
            """
            {
                "data": [
                    {
                        "id": "s-1",
                        "user_id": "456",
                        "user_login": "streamer",
                        "user_name": "Streamer",
                        "type": "live",
                        "title": "Hello!",
                        "viewer_count": 1000,
                        "language": "en",
                        "thumbnail_url": "https://example.com/thumb.jpg"
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("Streams") {

            When("getAllStreams is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val streams = resource.getAllStreams().toList()

                Then("it should call the streams endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/streams"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should deserialize the stream") {
                    streams.size shouldBe 1
                    streams.first().id shouldBe "s-2"
                    streams.first().title shouldBe "Gaming"
                    streams.first().viewerCount shouldBe 500
                }
            }

            When("getStreams is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getStreams()

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the stream data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "s-1"
                    page.data.first().viewerCount shouldBe 1000
                }

                Then("it should return the next page cursor") {
                    page.cursor.shouldNotBeNull()
                    page.cursor shouldBe "abc123"
                }
            }

            When("getStreams is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getStreams(cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getStreams is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getStreams(pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }

            When("getStreams is called with filter parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getStreams(
                    userIds = listOf("456"),
                    gameIds = listOf("509658"),
                    type = StreamType.LIVE,
                )

                Then("it should pass filter parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "456"
                    request.url.parameters["game_id"] shouldBe "509658"
                    request.url.parameters["type"] shouldBe "live"
                }
            }
        }

        Given("FollowedStreams") {

            When("getAllFollowedStreams is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val streams = resource.getAllFollowedStreams(userId = "123").toList()

                Then("it should call the streams/followed endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/streams/followed"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should deserialize the stream") {
                    streams.size shouldBe 1
                    streams.first().id shouldBe "s-2"
                }
            }

            When("getFollowedStreams is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getFollowedStreams(userId = "123")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the stream data") {
                    page.data.size shouldBe 1
                    page.data.first().id shouldBe "s-1"
                }

                Then("it should return the next page cursor") {
                    page.cursor shouldBe "abc123"
                }
            }

            When("getFollowedStreams is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getFollowedStreams(userId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }
            }

            When("getFollowedStreams is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getFollowedStreams(userId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }
        }

        val markerJson =
            """
            {
                "data": [
                    {
                        "user_id": "123",
                        "user_name": "Streamer",
                        "user_login": "streamer",
                        "videos": []
                    }
                ],
                "pagination": {
                    "cursor": "abc123"
                }
            }
            """.trimIndent()

        val markerLastPageJson =
            """
            {
                "data": [
                    {
                        "user_id": "456",
                        "user_name": "Streamer2",
                        "user_login": "streamer2",
                        "videos": []
                    }
                ],
                "pagination": {}
            }
            """.trimIndent()

        val markerSecondPageJson =
            """
            {
                "data": [
                    {
                        "user_id": "123",
                        "user_name": "Streamer",
                        "user_login": "streamer",
                        "videos": []
                    }
                ],
                "pagination": {
                    "cursor": "def456"
                }
            }
            """.trimIndent()

        Given("StreamMarkers") {

            When("getAllStreamMarkers is called with a userId") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val markers = resource.getAllStreamMarkers(userId = "123").toList()

                Then("it should call the streams/markers endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/streams/markers"
                }

                Then("it should pass the user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                }

                Then("it should deserialize the marker group") {
                    markers.size shouldBe 1
                    markers.first().userId shouldBe "456"
                }
            }

            When("getStreamMarkers is called without a cursor (first page)") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getStreamMarkers(userId = "123")

                Then("it should not include an after cursor parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"].shouldBeNull()
                }

                Then("it should return the marker group data") {
                    page.data.size shouldBe 1
                    page.data.first().userId shouldBe "123"
                }

                Then("it should return the next page cursor") {
                    page.cursor shouldBe "abc123"
                }
            }

            When("getStreamMarkers is called with a cursor") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerSecondPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val page = resource.getStreamMarkers(userId = "123", cursor = "abc123")

                Then("it should forward the cursor as the after parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["after"] shouldBe "abc123"
                }

                Then("it should return the cursor from the response") {
                    page.cursor shouldBe "def456"
                }
            }

            When("getStreamMarkers is called with a pageSize") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getStreamMarkers(userId = "123", pageSize = 50)

                Then("it should pass the pageSize as the first parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["first"] shouldBe "50"
                }
            }

            When("getStreamMarkers is called with a videoId") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getStreamMarkers(videoId = "v-1")

                Then("it should pass the video_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["video_id"] shouldBe "v-1"
                }
            }
        }

        Given("StreamKey") {

            val streamKeyJson =
                """
                {
                    "data": [
                        {
                            "stream_key": "live_123456789_abcdefghijklmnop"
                        }
                    ]
                }
                """.trimIndent()

            When("getStreamKey is called") {
                val engine =
                    MockEngine {
                        respond(
                            content = streamKeyJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val streamKey = resource.getStreamKey(broadcasterId = "123")

                Then("it should call the streams/key endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/streams/key"
                }

                Then("it should use GET method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Get
                }

                Then("it should pass the broadcaster_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["broadcaster_id"] shouldBe "123"
                }

                Then("it should deserialize the stream key") {
                    streamKey.streamKey shouldBe "live_123456789_abcdefghijklmnop"
                }
            }
        }

        Given("CreateStreamMarker") {

            val createdMarkerJson =
                """
                {
                    "data": [
                        {
                            "id": "marker-1",
                            "created_at": "2024-01-01T00:00:00Z",
                            "position_seconds": 300,
                            "description": "highlight"
                        }
                    ]
                }
                """.trimIndent()

            When("createStreamMarker is called with description") {
                val engine =
                    MockEngine {
                        respond(
                            content = createdMarkerJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                val marker = resource.createStreamMarker(userId = "123", description = "highlight")

                Then("it should call the streams/markers endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/streams/markers"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }

                Then("it should deserialize the created marker") {
                    marker.id shouldBe "marker-1"
                    marker.positionSeconds shouldBe 300
                    marker.description shouldBe "highlight"
                }
            }

            When("createStreamMarker is called without description") {
                val engine =
                    MockEngine {
                        respond(
                            content = createdMarkerJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.createStreamMarker(userId = "123")

                Then("it should call the streams/markers endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/streams/markers"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }
            }
        }

        Given("getAllStreamMarkers - with optional params") {

            When("getAllStreamMarkers is called with a videoId") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getAllStreamMarkers(videoId = "v-1").toList()

                Then("it should pass the video_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["video_id"] shouldBe "v-1"
                }

                Then("it should not include a user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe null
                }
            }

            When("getAllStreamMarkers is called with both userId and videoId") {
                val engine =
                    MockEngine {
                        respond(
                            content = markerLastPageJson,
                            status = HttpStatusCode.OK,
                            headers = JSON_HEADERS,
                        )
                    }
                val resource = createResource(engine)
                resource.getAllStreamMarkers(userId = "123", videoId = "v-1").toList()

                Then("it should pass both user_id and video_id parameters") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["user_id"] shouldBe "123"
                    request.url.parameters["video_id"] shouldBe "v-1"
                }
            }
        }
    })
