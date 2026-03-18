package io.github.captnblubber.twitchkt.helix.internal

import io.github.captnblubber.twitchkt.error.TwitchApiException
import kotlinx.serialization.Serializable

@Serializable
internal data class TwitchResponse<T>(
    val data: List<T>,
    val pagination: Pagination? = null,
    val total: Int? = null,
)

internal fun <T> TwitchResponse<T>.requireFirst(endpoint: String): T = data.firstOrNull() ?: throw TwitchApiException.EmptyResponse(endpoint)

@Serializable
internal data class Pagination(
    val cursor: String? = null,
)
