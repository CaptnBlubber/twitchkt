package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.ExtensionTransaction

/**
 * Twitch Helix Extensions API resource.
 *
 * Provides methods for retrieving extension transactions.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-extension-transactions">Twitch API Reference - Extensions</a>
 */
class ExtensionResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Extension Transactions](https://dev.twitch.tv/docs/api/reference/#get-extension-transactions)
     *
     * Gets an extension's list of transactions. A transaction records the exchange of a currency
     * (for example, Bits) for a digital product.
     *
     * Requires an app access token.
     *
     * @param extensionId the ID of the extension whose list of transactions you want to get.
     * @param ids a list of transaction IDs used to filter the list of transactions. You may
     * specify a maximum of 100 IDs.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the list of transactions.
     */
    suspend fun getTransactions(
        extensionId: String,
        ids: List<String> = emptyList(),
        first: Int = 20,
        after: String? = null,
    ): List<ExtensionTransaction> =
        http
            .get<ExtensionTransaction>(
                "extensions/transactions",
                buildList {
                    add("extension_id" to extensionId)
                    ids.forEach { add("id" to it) }
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                },
            ).data
}
