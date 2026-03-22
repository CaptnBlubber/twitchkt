package io.github.captnblubber.twitchkt.error

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class ErrorMappingTest :
    BehaviorSpec({

        Given("mapTwitchApiError") {

            When("called with status code 400") {
                val result = mapTwitchApiError(400, "bad request body")

                Then("it should return BadRequest") {
                    result.shouldBeInstanceOf<TwitchApiException.BadRequest>()
                    result.message shouldBe "bad request body"
                }
            }

            When("called with status code 401") {
                val result = mapTwitchApiError(401, "unauthorized body")

                Then("it should return Unauthorized") {
                    result.shouldBeInstanceOf<TwitchApiException.Unauthorized>()
                    result.message shouldBe "unauthorized body"
                }
            }

            When("called with status code 403") {
                val result = mapTwitchApiError(403, "forbidden body")

                Then("it should return Forbidden") {
                    result.shouldBeInstanceOf<TwitchApiException.Forbidden>()
                    result.message shouldBe "forbidden body"
                }
            }

            When("called with status code 404") {
                val result = mapTwitchApiError(404, "not found body")

                Then("it should return NotFound") {
                    result.shouldBeInstanceOf<TwitchApiException.NotFound>()
                    result.message shouldBe "not found body"
                }
            }

            When("called with status code 409") {
                val result = mapTwitchApiError(409, "conflict body")

                Then("it should return Conflict") {
                    result.shouldBeInstanceOf<TwitchApiException.Conflict>()
                    result.message shouldBe "conflict body"
                }
            }

            When("called with status code 422") {
                val result = mapTwitchApiError(422, "unprocessable body")

                Then("it should return UnprocessableEntity") {
                    result.shouldBeInstanceOf<TwitchApiException.UnprocessableEntity>()
                    result.message shouldBe "unprocessable body"
                }
            }

            When("called with status code 429") {
                val result = mapTwitchApiError(429, "rate limited body", retryAfterMs = 3000L)

                Then("it should return RateLimited with retryAfterMs") {
                    val rateLimited = result.shouldBeInstanceOf<TwitchApiException.RateLimited>()
                    rateLimited.message shouldBe "rate limited body"
                    rateLimited.retryAfterMs shouldBe 3000L
                }
            }

            When("called with status code 429 without retryAfterMs") {
                val result = mapTwitchApiError(429, "rate limited body")

                Then("it should default retryAfterMs to 0") {
                    val rateLimited = result.shouldBeInstanceOf<TwitchApiException.RateLimited>()
                    rateLimited.retryAfterMs shouldBe 0L
                }
            }

            When("called with status code 500") {
                val result = mapTwitchApiError(500, "server error body")

                Then("it should return ServerError") {
                    val serverError = result.shouldBeInstanceOf<TwitchApiException.ServerError>()
                    serverError.message shouldBe "server error body"
                    serverError.statusCode shouldBe 500
                }
            }

            When("called with an unknown status code") {
                val result = mapTwitchApiError(503, "service unavailable")

                Then("it should return ServerError with that status code") {
                    val serverError = result.shouldBeInstanceOf<TwitchApiException.ServerError>()
                    serverError.message shouldBe "service unavailable"
                    serverError.statusCode shouldBe 503
                }
            }
        }
    })
