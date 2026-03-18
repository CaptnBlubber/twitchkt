package io.github.captnblubber.twitchkt.logging

fun interface TwitchKtLogger {
    fun log(
        level: LogLevel,
        tag: String,
        message: () -> String,
    )
}
