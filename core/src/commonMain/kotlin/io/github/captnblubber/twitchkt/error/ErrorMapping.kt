package io.github.captnblubber.twitchkt.error

/**
 * Maps an HTTP status code and response body to the appropriate [TwitchApiException].
 *
 * @param statusCode the HTTP status code from the Twitch API response.
 * @param body the raw response body text.
 * @param retryAfterMs for 429 responses, the number of milliseconds until the rate limit resets.
 *   Pass `null` when the retry-after information is not available.
 */
fun mapTwitchApiError(
    statusCode: Int,
    body: String,
    retryAfterMs: Long? = null,
): TwitchApiException =
    when (statusCode) {
        400 -> TwitchApiException.BadRequest(body)
        401 -> TwitchApiException.Unauthorized(body)
        403 -> TwitchApiException.Forbidden(body)
        404 -> TwitchApiException.NotFound(body)
        409 -> TwitchApiException.Conflict(body)
        422 -> TwitchApiException.UnprocessableEntity(body)
        429 -> TwitchApiException.RateLimited(retryAfterMs ?: 0L, body)
        else -> TwitchApiException.ServerError(statusCode, body)
    }
