package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: extension.bits_transaction.create](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#extensionbits_transactioncreate)
 *
 * @property extensionClientId the client ID of the extension.
 * @property id the transaction ID.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property userId the user ID of the user who made the transaction.
 * @property userLogin the login of the user who made the transaction.
 * @property userName the display name of the user who made the transaction.
 * @property product the product details as a JSON object.
 */
data class ExtensionBitsTransactionCreate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val extensionClientId: String,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val userId: String,
    val userLogin: String,
    val userName: String,
    val product: ExtensionProduct,
) : TwitchEvent
