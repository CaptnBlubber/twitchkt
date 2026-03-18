package io.github.captnblubber.twitchkt.model.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SubTier {
    @SerialName("1000")
    TIER_1,

    @SerialName("2000")
    TIER_2,

    @SerialName("3000")
    TIER_3,

    @SerialName("Prime")
    PRIME,
}
