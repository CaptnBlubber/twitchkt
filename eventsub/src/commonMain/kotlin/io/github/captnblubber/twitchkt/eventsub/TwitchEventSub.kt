package io.github.captnblubber.twitchkt.eventsub

import io.github.captnblubber.twitchkt.ConnectionState
import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.eventsub.internal.EventSubParser
import io.github.captnblubber.twitchkt.eventsub.internal.ParsedMessage
import io.github.captnblubber.twitchkt.eventsub.model.TwitchEvent
import io.github.captnblubber.twitchkt.helix.resource.SubscriptionResource
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

class TwitchEventSub(
    private val httpClient: HttpClient,
    private val config: TwitchKtConfig,
    private val subscriptionResource: SubscriptionResource,
) {
    private val parser = EventSubParser()
    private val subscriptionsMutex = Mutex()
    private val subscriptions = mutableListOf<EventSubSubscriptionType>()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<TwitchEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<TwitchEvent> = _events.asSharedFlow()

    private val _sessionId = MutableStateFlow<String?>(null)
    val sessionId: StateFlow<String?> = _sessionId.asStateFlow()

    private val connectionMutex = Mutex()
    private var connectionJob: Job? = null

    suspend fun connect(scope: CoroutineScope) {
        connectionMutex.withLock {
            connectionJob?.cancel()
            connectionJob =
                scope.launch {
                    connectWithReconnect(config.eventSubUrl)
                }
        }
    }

    suspend fun disconnect() {
        connectionMutex.withLock {
            connectionJob?.cancel()
            connectionJob = null
            _connectionState.value = ConnectionState.DISCONNECTED
            _sessionId.value = null
        }
    }

    /**
     * Registers a subscription to be created on the current EventSub session.
     *
     * If the WebSocket is already connected, the subscription is registered immediately.
     * Otherwise it is stored and registered automatically when the next welcome message
     * arrives. Subscriptions are re-registered on error-recovery reconnects but **not**
     * on server-initiated reconnects (Twitch carries those over).
     */
    suspend fun subscribe(subscription: EventSubSubscriptionType) {
        val currentSessionId =
            subscriptionsMutex.withLock {
                subscriptions.add(subscription)
                _sessionId.value
            }
        if (currentSessionId != null) {
            try {
                subscriptionResource.createEventSub(subscription, currentSessionId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Failed to register subscription ${subscription.type}: ${e.message}")
            }
        }
    }

    private suspend fun registerPendingSubscriptions(sessionId: String) {
        val pending = subscriptionsMutex.withLock { subscriptions.toList() }
        for (subscription in pending) {
            try {
                subscriptionResource.createEventSub(subscription, sessionId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "Failed to register subscription ${subscription.type}: ${e.message}")
            }
        }
    }

    private suspend fun connectWithReconnect(initialUrl: String) {
        var url = initialUrl
        var attempt = 0

        while (true) {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                log(LogLevel.INFO, "Connecting to EventSub at $url")
                runWebSocketSession(url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(LogLevel.ERROR, "EventSub connection error: ${e.message}")
            }

            url = config.eventSubUrl
            _sessionId.value = null

            val backoffMs = computeBackoff(attempt)
            _connectionState.value = ConnectionState.RECONNECTING
            log(LogLevel.INFO, "Reconnecting in ${backoffMs}ms (attempt ${attempt + 1})")
            delay(backoffMs)
            attempt++
        }
    }

    private suspend fun runWebSocketSession(url: String) {
        httpClient.webSocket(url) {
            processFrames(this)
        }
    }

    private suspend fun processFrames(session: WebSocketSession) {
        var keepaliveTimeoutMs = DEFAULT_KEEPALIVE_TIMEOUT_MS
        var keepaliveJob: Job? = null

        suspend fun resetKeepaliveTimer(scope: CoroutineScope) {
            keepaliveJob?.cancel()
            keepaliveJob =
                scope.launch {
                    delay(keepaliveTimeoutMs)
                    log(LogLevel.WARN, "Keepalive timeout exceeded, closing connection")
                    session.close()
                }
        }

        resetKeepaliveTimer(session)

        for (frame in session.incoming) {
            if (frame !is Frame.Text) continue
            val text = frame.readText()
            val message =
                try {
                    parser.parse(text)
                } catch (e: Exception) {
                    log(LogLevel.ERROR, "Failed to parse EventSub message: ${e.message}")
                    continue
                }

            when (message) {
                is ParsedMessage.Welcome -> {
                    _sessionId.value = message.session.id
                    _connectionState.value = ConnectionState.CONNECTED
                    keepaliveTimeoutMs =
                        computeKeepaliveTimeout(message.session.keepaliveTimeoutSeconds)
                    resetKeepaliveTimer(session)
                    log(LogLevel.INFO, "EventSub connected, session=${message.session.id}")
                    session.launch { registerPendingSubscriptions(message.session.id) }
                }

                is ParsedMessage.Keepalive -> {
                    resetKeepaliveTimer(session)
                }

                is ParsedMessage.Notification -> {
                    resetKeepaliveTimer(session)
                    _events.emit(message.event)
                }

                is ParsedMessage.Reconnect -> {
                    resetKeepaliveTimer(session)
                    handleReconnect(message, session)
                    return
                }

                is ParsedMessage.Revocation -> {
                    resetKeepaliveTimer(session)
                    log(
                        LogLevel.WARN,
                        "Subscription revoked: type=${message.subscriptionType}, " +
                            "status=${message.status}",
                    )
                }
            }
        }
    }

    private suspend fun handleReconnect(
        message: ParsedMessage.Reconnect,
        oldSession: WebSocketSession,
    ) {
        val reconnectUrl = message.session.reconnectUrl ?: return
        log(LogLevel.INFO, "Received reconnect, connecting to $reconnectUrl")
        _connectionState.value = ConnectionState.RECONNECTING

        try {
            httpClient.webSocket(reconnectUrl) {
                // Wait for welcome on new connection before closing old
                for (frame in incoming) {
                    if (frame !is Frame.Text) continue
                    val text = frame.readText()
                    val parsed =
                        try {
                            parser.parse(text)
                        } catch (e: Exception) {
                            log(LogLevel.ERROR, "Failed to parse EventSub message: ${e.message}")
                            continue
                        }

                    if (parsed is ParsedMessage.Welcome) {
                        _sessionId.value = parsed.session.id
                        _connectionState.value = ConnectionState.CONNECTED
                        log(LogLevel.INFO, "Reconnected, new session=${parsed.session.id}")
                        try {
                            oldSession.close()
                        } catch (_: Exception) {
                            // Old session may already be closed
                        }
                        // Continue processing on the new connection
                        processFrames(this@webSocket)
                        return@webSocket
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log(LogLevel.ERROR, "Reconnect to new URL failed: ${e.message}")
            throw e
        }
    }

    private fun computeKeepaliveTimeout(timeoutSeconds: Int?): Long {
        val baseTimeout = (timeoutSeconds ?: DEFAULT_KEEPALIVE_SECONDS).toLong()
        return (baseTimeout + KEEPALIVE_BUFFER_SECONDS) * MILLIS_PER_SECOND
    }

    private fun computeBackoff(attempt: Int): Long = Companion.computeBackoff(attempt)

    private fun log(
        level: LogLevel,
        message: String,
    ) {
        config.logger?.log(level, LOG_TAG) { message }
    }

    internal companion object {
        private const val LOG_TAG = "TwitchEventSub"
        private const val DEFAULT_KEEPALIVE_SECONDS = 10
        private const val KEEPALIVE_BUFFER_SECONDS = 5
        private const val MILLIS_PER_SECOND = 1000L
        internal const val BASE_BACKOFF_MS = 1000L
        internal const val MAX_BACKOFF_MS = 30_000L
        internal const val JITTER_FACTOR = 0.2
        private const val DEFAULT_KEEPALIVE_TIMEOUT_MS = 15_000L

        internal fun computeBackoff(attempt: Int): Long {
            val baseDelayMs = min(BASE_BACKOFF_MS * 2.0.pow(attempt), MAX_BACKOFF_MS.toDouble()).toLong()
            val jitter = (baseDelayMs * JITTER_FACTOR * (Random.nextDouble() * 2 - 1)).toLong()
            return baseDelayMs + jitter
        }
    }
}
