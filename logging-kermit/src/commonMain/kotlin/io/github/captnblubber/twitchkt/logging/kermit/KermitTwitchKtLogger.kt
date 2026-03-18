package io.github.captnblubber.twitchkt.logging.kermit

import co.touchlab.kermit.Logger
import io.github.captnblubber.twitchkt.logging.LogLevel
import io.github.captnblubber.twitchkt.logging.TwitchKtLogger

class KermitTwitchKtLogger(
    private val tagPrefix: String = "twitchkt",
) : TwitchKtLogger {
    override fun log(
        level: LogLevel,
        tag: String,
        message: () -> String,
    ) {
        val fullTag = "$tagPrefix/$tag"
        when (level) {
            LogLevel.DEBUG -> Logger.d(tag = fullTag) { message() }
            LogLevel.INFO -> Logger.i(tag = fullTag) { message() }
            LogLevel.WARN -> Logger.w(tag = fullTag) { message() }
            LogLevel.ERROR -> Logger.e(tag = fullTag) { message() }
        }
    }
}
