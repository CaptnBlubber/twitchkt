package io.github.captnblubber.twitchkt.error

import io.github.captnblubber.twitchkt.auth.TwitchScope

sealed class TwitchApiException(
    override val message: String,
) : Exception(message) {
    class Unauthorized(
        override val message: String,
    ) : TwitchApiException(message)

    class Forbidden(
        override val message: String,
    ) : TwitchApiException(message)

    class NotFound(
        override val message: String,
    ) : TwitchApiException(message)

    class BadRequest(
        override val message: String,
    ) : TwitchApiException(message)

    class RateLimited(
        val retryAfterMs: Long,
        override val message: String,
    ) : TwitchApiException(message)

    class Conflict(
        override val message: String,
    ) : TwitchApiException(message)

    class UnprocessableEntity(
        override val message: String,
    ) : TwitchApiException(message)

    class ServerError(
        val statusCode: Int,
        override val message: String,
    ) : TwitchApiException(message)

    class MissingScope(
        val missingScopes: List<TwitchScope>,
    ) : TwitchApiException(
            "Missing required scope(s): ${missingScopes.joinToString { it.value }}",
        )

    data class EmptyResponse(
        val endpoint: String,
    ) : TwitchApiException(
            "Twitch API returned an empty data array for endpoint: $endpoint",
        )
}
