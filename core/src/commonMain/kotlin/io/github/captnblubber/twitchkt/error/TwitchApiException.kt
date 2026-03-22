package io.github.captnblubber.twitchkt.error

import io.github.captnblubber.twitchkt.auth.TwitchScope

sealed class TwitchApiException(
    override val message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class Unauthorized(
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class Forbidden(
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class NotFound(
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class BadRequest(
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class RateLimited(
        val retryAfterMs: Long,
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class Conflict(
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class UnprocessableEntity(
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class ServerError(
        val statusCode: Int,
        override val message: String,
        cause: Throwable? = null,
    ) : TwitchApiException(message, cause)

    class MissingScope(
        val missingScopes: List<TwitchScope>,
    ) : TwitchApiException(
            "Missing required scope(s): ${missingScopes.joinToString { it.value }}",
        )

    class EmptyResponse(
        val endpoint: String,
    ) : TwitchApiException(
            "Twitch API returned an empty data array for endpoint: $endpoint",
        )
}
