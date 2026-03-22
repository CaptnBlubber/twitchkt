@file:Suppress("DEPRECATION")

package io.github.captnblubber.twitchkt.irc

import io.github.captnblubber.twitchkt.ConnectionState
import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.error.TwitchApiException
import io.github.captnblubber.twitchkt.irc.internal.IrcParser
import io.github.captnblubber.twitchkt.logging.LogLevel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Read-only Twitch IRC client with automatic reconnection.
 *
 * Twitch IRC is deprecated in favor of EventSub. This client exists solely because EventSub
 * does not yet cover all event types (e.g. watch streaks / viewer milestones).
 * Once Twitch adds full EventSub coverage, this class will be removed.
 *
 * See: [Twitch IRC Migration Guide](https://dev.twitch.tv/docs/chat/irc-migration/)
 *
 * Usage:
 * ```kotlin
 * @Suppress("DEPRECATION")
 * val irc = TwitchIrc(httpClient, config)
 * irc.join("channelname")
 * irc.connect(scope)
 * irc.messages.collect { msg -> /* ... */ }
 * ```
 */
@Deprecated(
    "Twitch IRC is deprecated. Prefer EventSub for real-time events. " +
        "IRC remains only for event types not yet available via EventSub (e.g. watch streaks). " +
        "See: https://dev.twitch.tv/docs/chat/irc-migration/",
)
class TwitchIrc(
    private val httpClient: HttpClient,
    private val config: TwitchKtConfig,
) {
    private val parser = IrcParser()
    private val channelsMutex = Mutex()
    private val channels = mutableSetOf<String>()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableSharedFlow<IrcMessage>(extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val messages: SharedFlow<IrcMessage> = _messages.asSharedFlow()

    private val connectionMutex = Mutex()
    private var connectionJob: Job? = null
    private val activeSession = MutableStateFlow<WebSocketSession?>(null)

    suspend fun join(channel: String) {
        val normalized = channel.lowercase().removePrefix("#")
        val session =
            channelsMutex.withLock {
                channels.add(normalized)
                activeSession.value
            }
        if (session != null) {
            try {
                sendCommand(session, "JOIN #$normalized")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Failed to send JOIN for $normalized: ${e.message}")
            }
        }
    }

    suspend fun part(channel: String) {
        val normalized = channel.lowercase().removePrefix("#")
        val session =
            channelsMutex.withLock {
                channels.remove(normalized)
                activeSession.value
            }
        if (session != null) {
            try {
                sendCommand(session, "PART #$normalized")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Failed to send PART for $normalized: ${e.message}")
            }
        }
    }

    suspend fun connect(scope: CoroutineScope) {
        connectionMutex.withLock {
            connectionJob?.cancel()
            connectionJob =
                scope.launch {
                    connectWithReconnect()
                }
        }
    }

    suspend fun disconnect() {
        connectionMutex.withLock {
            connectionJob?.cancel()
            connectionJob = null
            activeSession.value = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private suspend fun connectWithReconnect() {
        var attempt = 0

        while (true) {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                log(LogLevel.INFO, "Connecting to IRC at ${config.ircUrl}")
                runWebSocketSession()
            } catch (e: CancellationException) {
                throw e
            } catch (e: TwitchApiException.Unauthorized) {
                log(LogLevel.ERROR, "Authentication failed, not reconnecting: ${e.message}")
                _connectionState.value = ConnectionState.DISCONNECTED
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "IRC connection error: ${e.message}")
            }

            activeSession.value = null

            val backoffMs = computeBackoff(attempt)
            _connectionState.value = ConnectionState.RECONNECTING
            log(LogLevel.INFO, "Reconnecting in ${backoffMs}ms (attempt ${attempt + 1})")
            delay(backoffMs)
            attempt++
        }
    }

    private suspend fun runWebSocketSession() {
        httpClient.webSocket(config.ircUrl) {
            activeSession.value = this
            authenticate(this)
            joinChannels(this)
            processFrames(this)
        }
    }

    private suspend fun authenticate(session: WebSocketSession) {
        sendCommand(session, "CAP REQ :twitch.tv/tags twitch.tv/commands")
        val token = config.tokenProvider.token()
        sendCommand(session, "PASS oauth:$token")
        val login = config.login ?: ANONYMOUS_LOGIN
        sendCommand(session, "NICK $login")
    }

    private suspend fun joinChannels(session: WebSocketSession) {
        val current = channelsMutex.withLock { channels.toSet() }
        for (channel in current) {
            sendCommand(session, "JOIN #$channel")
        }
    }

    private suspend fun processFrames(session: WebSocketSession) {
        var pingTimeoutJob: Job? = null

        suspend fun resetPingTimeout(scope: CoroutineScope) {
            pingTimeoutJob?.cancel()
            pingTimeoutJob =
                scope.launch {
                    delay(PING_TIMEOUT_MS)
                    log(LogLevel.WARN, "PING timeout exceeded, closing connection")
                    session.close()
                }
        }

        resetPingTimeout(session)

        for (frame in session.incoming) {
            if (frame !is Frame.Text) continue
            val text = frame.readText()

            for (line in text.lines()) {
                if (line.isBlank()) continue

                if (line.contains("NOTICE") && line.contains("Login authentication failed")) {
                    log(LogLevel.ERROR, "IRC authentication failed — check your token")
                    throw TwitchApiException.Unauthorized("IRC login authentication failed")
                }

                if (line.startsWith("PING")) {
                    sendCommand(session, "PONG :tmi.twitch.tv")
                    resetPingTimeout(session)
                    continue
                }

                val message =
                    try {
                        parser.parse(line)
                    } catch (e: Exception) {
                        log(LogLevel.ERROR, "Failed to parse IRC message: ${e.message}")
                        null
                    }

                if (message != null) {
                    resetPingTimeout(session)
                    _messages.emit(message)
                }
            }
        }
    }

    private suspend fun sendCommand(
        session: WebSocketSession,
        command: String,
    ) {
        session.send(Frame.Text(command))
    }

    private fun computeBackoff(attempt: Int): Long = Companion.computeBackoff(attempt)

    private fun log(
        level: LogLevel,
        message: String,
    ) {
        config.logger?.log(level, LOG_TAG) { message }
    }

    internal companion object {
        private const val LOG_TAG = "TwitchIrc"
        internal const val BASE_BACKOFF_MS = 1000L
        internal const val MAX_BACKOFF_MS = 30_000L
        internal const val JITTER_FACTOR = 0.2
        private const val PING_TIMEOUT_MS = 360_000L
        private const val ANONYMOUS_LOGIN = "justinfan12345"

        internal fun computeBackoff(attempt: Int): Long {
            val baseDelayMs = min(BASE_BACKOFF_MS * 2.0.pow(attempt), MAX_BACKOFF_MS.toDouble()).toLong()
            val jitter = (baseDelayMs * JITTER_FACTOR * (Random.nextDouble() * 2 - 1)).toLong()
            return baseDelayMs + jitter
        }
    }
}
