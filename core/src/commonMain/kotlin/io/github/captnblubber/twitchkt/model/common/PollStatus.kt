package io.github.captnblubber.twitchkt.model.common

import kotlinx.serialization.Serializable

@Serializable
enum class PollStatus {
    ACTIVE,
    COMPLETED,
    TERMINATED,
    ARCHIVED,
}
