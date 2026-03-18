package io.github.captnblubber.twitchkt.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidationResponse(
    @SerialName("client_id") val clientId: String,
    val login: String,
    @SerialName("user_id") val userId: String,
    val scopes: List<String>,
    @SerialName("expires_in") val expiresIn: Int,
)
