package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Status values for ending a prediction via the Helix API.
 */
@Serializable
enum class PredictionEndStatus {
    @SerialName("RESOLVED")
    RESOLVED,

    @SerialName("CANCELED")
    CANCELED,

    @SerialName("LOCKED")
    LOCKED,
}
