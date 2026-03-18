package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an analytics report for an extension.
 *
 * @property extensionId an ID that identifies the extension that the report was generated for.
 * @property url the URL that you use to download the report. The URL is valid for 5 minutes.
 * @property type the type of report.
 * @property dateRange the reporting window's start and end dates, in RFC3339 format.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-extension-analytics">Twitch API Reference - Get Extension Analytics</a>
 */
@Serializable
data class ExtensionAnalyticsReport(
    @SerialName("extension_id") val extensionId: String,
    @SerialName("URL") val url: String,
    val type: String,
    @SerialName("date_range") val dateRange: DateRange,
)

/**
 * Represents a reporting window's start and end dates.
 *
 * @property startedAt the reporting window's start date, in RFC3339 format.
 * @property endedAt the reporting window's end date, in RFC3339 format.
 */
@Serializable
data class DateRange(
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String,
)
