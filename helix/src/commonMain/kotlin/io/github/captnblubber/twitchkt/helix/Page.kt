package io.github.captnblubber.twitchkt.helix

/**
 * A single page of results from a paginated Twitch Helix API endpoint.
 *
 * @param data the items on this page.
 * @param cursor the cursor to pass to the next call to retrieve the next page,
 * or `null` if this is the last page.
 */
data class Page<T>(
    val data: List<T>,
    val cursor: String?,
)
