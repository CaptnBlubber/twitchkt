package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.model.ExtensionAnalyticsReport

/**
 * Twitch Helix Analytics API resource.
 *
 * Provides methods for retrieving analytics reports for extensions.
 *
 * Note: This resource returns lists directly rather than [Page]/[Flow] because when an
 * [extensionId][getExtensionAnalytics] is specified the response contains a single report, and
 * without one the result set is bounded by the number of extensions the user owns (typically
 * small). The cursor is also ignored when filtering by extension ID, making auto-pagination
 * a poor fit.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-extension-analytics">Twitch API Reference - Analytics</a>
 */
class AnalyticsResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Extension Analytics](https://dev.twitch.tv/docs/api/reference/#get-extension-analytics)
     *
     * Gets an analytics report for one or more extensions. The response contains the URLs used
     * to download the reports (CSV files).
     *
     * @param extensionId the extension's client ID. If specified, the response contains a report
     * for the specified extension. If not specified, the response includes a report for each
     * extension that the authenticated user owns.
     * @param type the type of analytics report to get. Possible values are: `overview_v2`.
     * @param startedAt the reporting window's start date, in RFC3339 format. Set the time portion
     * to zeroes (for example, 2021-10-22T00:00:00Z). The start date must be on or after January 31,
     * 2018. If you specify a start date, you must specify an end date.
     * @param endedAt the reporting window's end date, in RFC3339 format. Set the time portion to
     * zeroes (for example, 2021-10-27T00:00:00Z). The report is inclusive of the end date. Specify
     * an end date only if you provide a start date. Because it can take up to two days for the data
     * to be available, you must specify an end date that's earlier than today minus one to two days.
     * @param first the maximum number of report URLs to return per page in the response. The minimum
     * page size is 1 URL per page and the maximum is 100 URLs per page. The default is 20.
     * @param after the cursor used to get the next page of results. This parameter is ignored if
     * the [extensionId] parameter is set.
     * @return the list of analytics reports.
     */
    @RequiresScope(TwitchScope.ANALYTICS_READ_EXTENSIONS)
    suspend fun getExtensionAnalytics(
        extensionId: String? = null,
        type: String? = null,
        startedAt: String? = null,
        endedAt: String? = null,
        first: Int = 20,
        after: String? = null,
    ): List<ExtensionAnalyticsReport> {
        http.validateScopes(TwitchScope.ANALYTICS_READ_EXTENSIONS)
        val params =
            buildList {
                extensionId?.let { add("extension_id" to it) }
                type?.let { add("type" to it) }
                startedAt?.let { add("started_at" to it) }
                endedAt?.let { add("ended_at" to it) }
                add("first" to first.toString())
                after?.let { add("after" to it) }
            }
        return http.get<ExtensionAnalyticsReport>("analytics/extensions", params).data
    }
}
