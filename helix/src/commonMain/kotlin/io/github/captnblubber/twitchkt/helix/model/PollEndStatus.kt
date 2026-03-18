package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.Serializable

@Serializable
enum class PollEndStatus {
    TERMINATED,
    ARCHIVED,
}
