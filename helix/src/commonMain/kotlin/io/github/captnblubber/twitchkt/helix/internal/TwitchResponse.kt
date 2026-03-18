package io.github.captnblubber.twitchkt.helix.internal

import kotlinx.serialization.Serializable
import io.github.captnblubber.twitchkt.error.TwitchApiException

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
