package io.github.captnblubber.twitchkt.helix.resource

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

class WhisperResourceTest :
    BehaviorSpec({

        coroutineTestScope = true

        fun createResource(engine: MockEngine) = WhisperResource(createHelixClient(engine))

        Given("send") {

            When("called with required parameters") {
                val engine =
                    MockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.NoContent,
                        )
                    }
                val resource = createResource(engine)
                resource.send(
                    fromUserId = "123",
                    toUserId = "456",
                    message = "Hello there!",
                )

                Then("it should call the whispers endpoint") {
                    val request = engine.requestHistory.first()
                    request.url.encodedPath shouldBe "/helix/whispers"
                }

                Then("it should use POST method") {
                    val request = engine.requestHistory.first()
                    request.method shouldBe HttpMethod.Post
                }

                Then("it should pass the from_user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["from_user_id"] shouldBe "123"
                }

                Then("it should pass the to_user_id parameter") {
                    val request = engine.requestHistory.first()
                    request.url.parameters["to_user_id"] shouldBe "456"
                }

                Then("it should set Content-Type to application/json") {
                    val request = engine.requestHistory.first()
                    request.body.contentType?.toString() shouldBe "application/json"
                }
            }
        }
    })
