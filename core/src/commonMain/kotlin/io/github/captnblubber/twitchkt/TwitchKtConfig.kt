package io.github.captnblubber.twitchkt

import io.github.captnblubber.twitchkt.auth.ScopeProvider
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.logging.TwitchKtLogger

@Suppress("DEPRECATION")
data class TwitchKtConfig(
    val clientId: String,
    val tokenProvider: TokenProvider,
    val helixBaseUrl: String = "https://api.twitch.tv/helix",
    val eventSubUrl: String = "wss://eventsub.wss.twitch.tv/ws",
    @Deprecated("IRC is deprecated. See: https://dev.twitch.tv/docs/chat/irc-migration/")
    val ircUrl: String = "wss://irc-ws.chat.twitch.tv",
    @Deprecated("IRC is deprecated. See: https://dev.twitch.tv/docs/chat/irc-migration/")
    val login: String? = null,
    val logger: TwitchKtLogger? = null,
    val scopeProvider: ScopeProvider? = null,
)
