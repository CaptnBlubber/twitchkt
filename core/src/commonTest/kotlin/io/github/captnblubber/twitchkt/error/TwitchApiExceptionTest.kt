package io.github.captnblubber.twitchkt.error

import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

class TwitchApiExceptionTest :
    BehaviorSpec({

        // --- Exception messages ---

        Given("each exception subclass") {
            Then("Unauthorized should have the correct message") {
                val ex = TwitchApiException.Unauthorized("token expired")
                ex.message shouldBe "token expired"
            }

            Then("Forbidden should have the correct message") {
                val ex = TwitchApiException.Forbidden("access denied")
                ex.message shouldBe "access denied"
            }

            Then("NotFound should have the correct message") {
                val ex = TwitchApiException.NotFound("resource not found")
                ex.message shouldBe "resource not found"
            }

            Then("BadRequest should have the correct message") {
                val ex = TwitchApiException.BadRequest("invalid parameter")
                ex.message shouldBe "invalid parameter"
            }

            Then("Conflict should have the correct message") {
                val ex = TwitchApiException.Conflict("resource conflict")
                ex.message shouldBe "resource conflict"
            }

            Then("UnprocessableEntity should have the correct message") {
                val ex = TwitchApiException.UnprocessableEntity("validation failed")
                ex.message shouldBe "validation failed"
            }

            Then("ServerError should have the correct message and status code") {
                val ex = TwitchApiException.ServerError(503, "service unavailable")
                ex.message shouldBe "service unavailable"
                ex.statusCode shouldBe 503
            }
        }

        // --- MissingScope ---

        Given("a MissingScope exception") {
            When("created with a single scope") {
                val ex = TwitchApiException.MissingScope(listOf(TwitchScope.CHAT_READ))

                Then("it should format the message with the scope value") {
                    ex.message shouldContain "chat:read"
                    ex.message shouldContain "Missing required scope(s)"
                }
            }

            When("created with multiple scopes") {
                val ex =
                    TwitchApiException.MissingScope(
                        listOf(TwitchScope.CHAT_READ, TwitchScope.BITS_READ),
                    )

                Then("it should include all scope values in the message") {
                    ex.message shouldContain "chat:read"
                    ex.message shouldContain "bits:read"
                }
            }

            Then("it should expose the missing scopes list") {
                val scopes = listOf(TwitchScope.CHANNEL_MANAGE_POLLS)
                val ex = TwitchApiException.MissingScope(scopes)
                ex.missingScopes shouldBe scopes
            }
        }

        // --- EmptyResponse ---

        Given("an EmptyResponse exception") {
            When("created with an endpoint") {
                val ex = TwitchApiException.EmptyResponse("/helix/users")

                Then("it should format the message with the endpoint") {
                    ex.message shouldContain "/helix/users"
                    ex.message shouldContain "empty data array"
                }
            }

            Then("it should expose the endpoint") {
                val ex = TwitchApiException.EmptyResponse("/helix/streams")
                ex.endpoint shouldBe "/helix/streams"
            }
        }

        // --- RateLimited ---

        Given("a RateLimited exception") {
            When("created with retryAfterMs") {
                val ex =
                    TwitchApiException.RateLimited(
                        retryAfterMs = 5000L,
                        message = "rate limited",
                    )

                Then("it should preserve the retryAfterMs value") {
                    ex.retryAfterMs shouldBe 5000L
                }

                Then("it should have the correct message") {
                    ex.message shouldBe "rate limited"
                }
            }
        }

        // --- cause parameter ---

        Given("an exception with a cause") {
            val rootCause = RuntimeException("network failure")

            When("created with a cause") {
                val ex = TwitchApiException.Unauthorized("token expired", rootCause)

                Then("the cause should be preserved in the exception chain") {
                    ex.cause shouldBe rootCause
                }
            }

            Then("Forbidden preserves cause") {
                val ex = TwitchApiException.Forbidden("denied", rootCause)
                ex.cause shouldBe rootCause
            }

            Then("BadRequest preserves cause") {
                val ex = TwitchApiException.BadRequest("bad", rootCause)
                ex.cause shouldBe rootCause
            }

            Then("ServerError preserves cause") {
                val ex = TwitchApiException.ServerError(500, "error", rootCause)
                ex.cause shouldBe rootCause
            }
        }

        // --- sealed class hierarchy ---

        Given("all exception subclasses") {
            Then("they should be instances of TwitchApiException") {
                TwitchApiException.Unauthorized("x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.Forbidden("x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.NotFound("x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.BadRequest("x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.RateLimited(0L, "x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.Conflict("x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.UnprocessableEntity("x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.ServerError(500, "x").shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.MissingScope(emptyList()).shouldBeInstanceOf<TwitchApiException>()
                TwitchApiException.EmptyResponse("x").shouldBeInstanceOf<TwitchApiException>()
            }
        }
    })
