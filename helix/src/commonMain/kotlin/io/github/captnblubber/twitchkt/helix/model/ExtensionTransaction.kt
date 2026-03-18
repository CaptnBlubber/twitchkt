package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Represents an extension transaction — a record of the exchange of a currency
 * (for example, Bits) for a digital product.
 *
 * @property id an ID that identifies the transaction.
 * @property timestamp the UTC date and time (in RFC3339 format) of the transaction.
 * @property broadcasterId the ID of the broadcaster that owns the channel where the transaction occurred.
 * @property broadcasterLogin the broadcaster's login name.
 * @property broadcasterName the broadcaster's display name.
 * @property userId the ID of the user that purchased the digital product.
 * @property userLogin the user's login name.
 * @property userName the user's display name.
 * @property productType the type of transaction. Possible values are: `BITS_IN_EXTENSION`.
 * @property productData contains details about the digital product.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-extension-transactions">Twitch API Reference - Get Extension Transactions</a>
 */
@OptIn(ExperimentalTime::class)
@Serializable
data class ExtensionTransaction(
    val id: String,
    val timestamp: Instant,
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("broadcaster_login") val broadcasterLogin: String,
    @SerialName("broadcaster_name") val broadcasterName: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_login") val userLogin: String,
    @SerialName("user_name") val userName: String,
    @SerialName("product_type") val productType: String,
    @SerialName("product_data") val productData: ExtensionProductData,
)

/**
 * Contains details about an extension's digital product.
 *
 * @property sku an ID that identifies the digital product.
 * @property domain set to `twitch.ext.` + the extension's ID.
 * @property cost contains details about the digital product's cost.
 * @property inDevelopment a Boolean value that determines whether the product is in development.
 * Is `true` if the digital product is in development and cannot be exchanged.
 * @property displayName the name of the digital product.
 * @property expiration this field is always empty since you may purchase only unexpired products.
 * @property broadcast a Boolean value that determines whether the data was broadcast to all
 * instances of the extension.
 */
@Serializable
data class ExtensionProductData(
    val sku: String,
    val domain: String,
    val cost: ExtensionProductCost,
    val inDevelopment: Boolean,
    val displayName: String,
    val expiration: String = "",
    val broadcast: Boolean,
)

/**
 * Contains details about a digital product's cost.
 *
 * @property amount the amount exchanged for the digital product.
 * @property type the type of currency exchanged. Possible values are: `bits`.
 */
@Serializable
data class ExtensionProductCost(
    val amount: Int,
    val type: String,
)
