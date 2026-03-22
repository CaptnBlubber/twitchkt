package io.github.captnblubber.twitchkt.eventsub.integration

import io.github.captnblubber.twitchkt.ConnectionState
import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.auth.TokenProvider
import io.github.captnblubber.twitchkt.eventsub.EventSubSubscriptionType
import io.github.captnblubber.twitchkt.eventsub.TwitchEventSub
import io.github.captnblubber.twitchkt.eventsub.model.ChannelAdBreakBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelBan
import io.github.captnblubber.twitchkt.eventsub.model.ChannelCheer
import io.github.captnblubber.twitchkt.eventsub.model.ChannelFollow
import io.github.captnblubber.twitchkt.eventsub.model.ChannelGoalBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelGoalEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelGoalProgress
import io.github.captnblubber.twitchkt.eventsub.model.ChannelModeratorAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelModeratorRemove
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsCustomRewardAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsCustomRewardRemove
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsCustomRewardUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsRedemptionAdd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelPointsRedemptionUpdate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelRaid
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShieldModeBegin
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShieldModeEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShoutoutCreate
import io.github.captnblubber.twitchkt.eventsub.model.ChannelShoutoutReceive
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscribe
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionEnd
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionGift
import io.github.captnblubber.twitchkt.eventsub.model.ChannelSubscriptionMessage
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUnban
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUnbanRequestResolve
import io.github.captnblubber.twitchkt.eventsub.model.ChannelUpdate
import io.github.captnblubber.twitchkt.eventsub.model.CharityCampaignProgress
import io.github.captnblubber.twitchkt.eventsub.model.CharityCampaignStart
import io.github.captnblubber.twitchkt.eventsub.model.CharityCampaignStop
import io.github.captnblubber.twitchkt.eventsub.model.CharityDonate
import io.github.captnblubber.twitchkt.eventsub.model.PollBegin
import io.github.captnblubber.twitchkt.eventsub.model.PollEnd
import io.github.captnblubber.twitchkt.eventsub.model.PollProgress
import io.github.captnblubber.twitchkt.eventsub.model.PredictionBegin
import io.github.captnblubber.twitchkt.eventsub.model.PredictionEnd
import io.github.captnblubber.twitchkt.eventsub.model.PredictionLock
import io.github.captnblubber.twitchkt.eventsub.model.PredictionProgress
import io.github.captnblubber.twitchkt.eventsub.model.StreamOffline
import io.github.captnblubber.twitchkt.eventsub.model.StreamOnline
import io.github.captnblubber.twitchkt.eventsub.model.TwitchEvent
import io.github.captnblubber.twitchkt.eventsub.model.UserUpdate
import io.github.captnblubber.twitchkt.helix.TwitchHelix
import io.github.captnblubber.twitchkt.logging.TwitchKtLogger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class EventSubIntegrationTest :
    FunSpec({

        val enabled = isIntegrationEnabled()

        val testLogger =
            TwitchKtLogger { level, tag, message ->
                println("[ktwitch/$tag] $level: ${message()}")
            }

        fun createClient(): HttpClient =
            HttpClient(CIO) {
                install(WebSockets)
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }

        val config =
            TwitchKtConfig(
                clientId = "integration-test-client",
                tokenProvider = TokenProvider { "mock-token" },
                helixBaseUrl = "http://localhost:8080",
                eventSubUrl = "ws://localhost:8080/ws",
                logger = testLogger,
            )

        fun createEventSub(httpClient: HttpClient): TwitchEventSub {
            val helix = TwitchHelix(httpClient, config)
            return TwitchEventSub(httpClient, config, helix.eventSub)
        }

        suspend fun subscribeAndVerify(
            subscription: EventSubSubscriptionType,
            expectedType: KClass<out TwitchEvent>,
            triggerVersion: String? = null,
        ) {
            val httpClient = createClient()
            val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            val eventSub = createEventSub(httpClient)
            try {
                eventSub.connect(scope)
                withTimeout(5_000) {
                    eventSub.sessionId.filterNotNull().first()
                }

                eventSub.subscribe(subscription)

                val eventDeferred = CompletableDeferred<TwitchEvent>()
                scope.launch {
                    eventDeferred.complete(eventSub.events.first())
                }

                TwitchCliProcess.triggerEvent(
                    subscription.type,
                    eventSub.sessionId.value!!,
                    triggerVersion,
                )

                val event =
                    withTimeout(5_000) {
                        eventDeferred.await()
                    }
                event::class shouldBe expectedType
            } finally {
                eventSub.disconnect()
                scope.cancel()
                httpClient.close()
            }
        }

        test("connect sets connectionState to CONNECTED and populates sessionId").config(
            enabledIf = { enabled },
        ) {
            val httpClient = createClient()
            val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            val eventSub = createEventSub(httpClient)
            try {
                eventSub.connect(scope)

                withTimeout(5_000) {
                    eventSub.connectionState.first { it == ConnectionState.CONNECTED }
                }
                eventSub.connectionState.value shouldBe ConnectionState.CONNECTED
                eventSub.sessionId.value.shouldNotBeBlank()
            } finally {
                eventSub.disconnect()
                scope.cancel()
                httpClient.close()
            }
        }

        test("disconnect after connecting returns to DISCONNECTED").config(
            enabledIf = { enabled },
        ) {
            val httpClient = createClient()
            val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
            val eventSub = createEventSub(httpClient)
            try {
                eventSub.connect(scope)
                withTimeout(5_000) {
                    eventSub.connectionState.first { it == ConnectionState.CONNECTED }
                }

                eventSub.disconnect()

                eventSub.connectionState.value shouldBe ConnectionState.DISCONNECTED
            } finally {
                eventSub.disconnect()
                scope.cancel()
                httpClient.close()
            }
        }

        val broadcaster = "1"
        val moderator = "1"

        // All Twitch CLI-supported EventSub event types mapped to their expected domain models.
        // Each test connects, subscribes via ktwitch's EventSubResource, triggers, and verifies.
        @Suppress("LongMethod")
        listOf(
            // Subscriptions & Gifts
            EventSubSubscriptionType.ChannelSubscribe(broadcaster) to ChannelSubscribe::class,
            EventSubSubscriptionType.ChannelSubscriptionGift(broadcaster) to ChannelSubscriptionGift::class,
            EventSubSubscriptionType.ChannelSubscriptionMessage(broadcaster) to ChannelSubscriptionMessage::class,
            EventSubSubscriptionType.ChannelSubscriptionEnd(broadcaster) to ChannelSubscriptionEnd::class,
            // Ad Break
            EventSubSubscriptionType.ChannelAdBreakBegin(broadcaster) to ChannelAdBreakBegin::class,
            // Moderation
            EventSubSubscriptionType.ChannelBan(broadcaster) to ChannelBan::class,
            EventSubSubscriptionType.ChannelUnban(broadcaster) to ChannelUnban::class,
            EventSubSubscriptionType.ChannelModeratorAdd(broadcaster) to ChannelModeratorAdd::class,
            EventSubSubscriptionType.ChannelModeratorRemove(broadcaster) to ChannelModeratorRemove::class,
            // Bits & Cheers
            EventSubSubscriptionType.ChannelCheer(broadcaster) to ChannelCheer::class,
            // Points & Rewards
            EventSubSubscriptionType.ChannelPointsCustomRewardAdd(broadcaster) to ChannelPointsCustomRewardAdd::class,
            EventSubSubscriptionType.ChannelPointsCustomRewardUpdate(broadcaster) to ChannelPointsCustomRewardUpdate::class,
            EventSubSubscriptionType.ChannelPointsCustomRewardRemove(broadcaster) to ChannelPointsCustomRewardRemove::class,
            EventSubSubscriptionType.ChannelPointsRedemptionAdd(broadcaster) to ChannelPointsRedemptionAdd::class,
            EventSubSubscriptionType.ChannelPointsRedemptionUpdate(broadcaster) to ChannelPointsRedemptionUpdate::class,
            // Polls
            EventSubSubscriptionType.PollBegin(broadcaster) to PollBegin::class,
            EventSubSubscriptionType.PollProgress(broadcaster) to PollProgress::class,
            EventSubSubscriptionType.PollEnd(broadcaster) to PollEnd::class,
            // Predictions
            EventSubSubscriptionType.PredictionBegin(broadcaster) to PredictionBegin::class,
            EventSubSubscriptionType.PredictionProgress(broadcaster) to PredictionProgress::class,
            EventSubSubscriptionType.PredictionLock(broadcaster) to PredictionLock::class,
            EventSubSubscriptionType.PredictionEnd(broadcaster) to PredictionEnd::class,
            // Hype Train — ktwitch uses v2 but Twitch CLI mock only supports v1, tested separately below
            // Goals
            EventSubSubscriptionType.ChannelGoalBegin(broadcaster) to ChannelGoalBegin::class,
            EventSubSubscriptionType.ChannelGoalProgress(broadcaster) to ChannelGoalProgress::class,
            EventSubSubscriptionType.ChannelGoalEnd(broadcaster) to ChannelGoalEnd::class,
            // Charity
            EventSubSubscriptionType.CharityDonate(broadcaster) to CharityDonate::class,
            EventSubSubscriptionType.CharityCampaignStart(broadcaster) to CharityCampaignStart::class,
            EventSubSubscriptionType.CharityCampaignProgress(broadcaster) to CharityCampaignProgress::class,
            EventSubSubscriptionType.CharityCampaignStop(broadcaster) to CharityCampaignStop::class,
            // Stream
            EventSubSubscriptionType.StreamOnline(broadcaster) to StreamOnline::class,
            EventSubSubscriptionType.StreamOffline(broadcaster) to StreamOffline::class,
        ).forEach { (subscription, expectedType) ->
            test("trigger ${subscription.type} delivers ${expectedType.simpleName}").config(
                enabledIf = { enabled },
            ) {
                subscribeAndVerify(subscription, expectedType)
            }
        }

        // Events requiring broadcaster + moderator condition
        listOf(
            EventSubSubscriptionType.ChannelFollow(broadcaster, moderator) to ChannelFollow::class,
            EventSubSubscriptionType.ChannelShieldModeBegin(broadcaster, moderator) to ChannelShieldModeBegin::class,
            EventSubSubscriptionType.ChannelShieldModeEnd(broadcaster, moderator) to ChannelShieldModeEnd::class,
            EventSubSubscriptionType.ChannelShoutoutCreate(broadcaster, moderator) to ChannelShoutoutCreate::class,
            EventSubSubscriptionType.ChannelShoutoutReceive(broadcaster, moderator) to ChannelShoutoutReceive::class,
            // channel.unban_request.create omitted — CLI mock payload missing required 'id' field
            EventSubSubscriptionType.ChannelUnbanRequestResolve(broadcaster, moderator) to ChannelUnbanRequestResolve::class,
        ).forEach { (subscription, expectedType) ->
            test("trigger ${subscription.type} delivers ${expectedType.simpleName}").config(
                enabledIf = { enabled },
            ) {
                subscribeAndVerify(subscription, expectedType)
            }
        }

        // Events with non-standard conditions or trigger flags
        test("trigger channel.update delivers ChannelUpdate").config(
            enabledIf = { enabled },
        ) {
            // channel.update has multiple versions — CLI requires explicit --version
            subscribeAndVerify(
                EventSubSubscriptionType.ChannelUpdate(broadcaster),
                ChannelUpdate::class,
                triggerVersion = "2",
            )
        }

        test("trigger channel.raid delivers ChannelRaid").config(
            enabledIf = { enabled },
        ) {
            subscribeAndVerify(
                EventSubSubscriptionType.ChannelRaid(toBroadcasterUserId = broadcaster),
                ChannelRaid::class,
            )
        }

        test("trigger user.update delivers UserUpdate").config(
            enabledIf = { enabled },
        ) {
            subscribeAndVerify(
                EventSubSubscriptionType.UserUpdate(userId = broadcaster),
                UserUpdate::class,
            )
        }

        // Types not testable via Twitch CLI mock server:
        // - channel.hype_train.begin/progress/end — ktwitch uses v2, mock server only supports v1
        // - user.authorization.grant/revoke (client_id condition not supported)
        // - extension.bits_transaction.create (extension_client_id condition not supported)
        // - drop.entitlement.grant (batch payload type not supported)
    })
