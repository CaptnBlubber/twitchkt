package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StreamType(
    val value: String,
) {
    @SerialName("all")
    ALL("all"),

    @SerialName("live")
    LIVE("live"),
}

@Serializable
enum class VideoPeriod(
    val value: String,
) {
    @SerialName("all")
    ALL("all"),

    @SerialName("day")
    DAY("day"),

    @SerialName("week")
    WEEK("week"),

    @SerialName("month")
    MONTH("month"),
}

@Serializable
enum class VideoSort(
    val value: String,
) {
    @SerialName("time")
    TIME("time"),

    @SerialName("trending")
    TRENDING("trending"),

    @SerialName("views")
    VIEWS("views"),
}

@Serializable
enum class VideoType(
    val value: String,
) {
    @SerialName("all")
    ALL("all"),

    @SerialName("upload")
    UPLOAD("upload"),

    @SerialName("archive")
    ARCHIVE("archive"),

    @SerialName("highlight")
    HIGHLIGHT("highlight"),
}

@Serializable
enum class RedemptionSort(
    val value: String,
) {
    @SerialName("OLDEST")
    OLDEST("OLDEST"),

    @SerialName("NEWEST")
    NEWEST("NEWEST"),
}

@Serializable
enum class AutoModAction(
    val value: String,
) {
    @SerialName("ALLOW")
    ALLOW("ALLOW"),

    @SerialName("DENY")
    DENY("DENY"),
}

@Serializable
enum class UnbanRequestStatus(
    val value: String,
) {
    @SerialName("pending")
    PENDING("pending"),

    @SerialName("approved")
    APPROVED("approved"),

    @SerialName("denied")
    DENIED("denied"),

    @SerialName("acknowledged")
    ACKNOWLEDGED("acknowledged"),

    @SerialName("canceled")
    CANCELED("canceled"),
}

@Serializable
enum class AnnouncementColor(
    val value: String,
) {
    @SerialName("blue")
    BLUE("blue"),

    @SerialName("green")
    GREEN("green"),

    @SerialName("orange")
    ORANGE("orange"),

    @SerialName("purple")
    PURPLE("purple"),

    @SerialName("primary")
    PRIMARY("primary"),
}

@Serializable
enum class BlockSourceContext(
    val value: String,
) {
    @SerialName("chat")
    CHAT("chat"),

    @SerialName("whisper")
    WHISPER("whisper"),
}

@Serializable
enum class BlockReason(
    val value: String,
) {
    @SerialName("spam")
    SPAM("spam"),

    @SerialName("harassment")
    HARASSMENT("harassment"),

    @SerialName("other")
    OTHER("other"),
}

@Serializable
enum class BitsLeaderboardPeriod(
    val value: String,
) {
    @SerialName("day")
    DAY("day"),

    @SerialName("week")
    WEEK("week"),

    @SerialName("month")
    MONTH("month"),

    @SerialName("year")
    YEAR("year"),

    @SerialName("all")
    ALL("all"),
}
