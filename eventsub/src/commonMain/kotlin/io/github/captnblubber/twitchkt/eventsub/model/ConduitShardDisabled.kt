package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: conduit.shard.disabled](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#conduitsharddisabled)
 *
 * @property conduitId the ID of the conduit.
 * @property shardId the ID of the disabled shard.
 * @property status the status of the shard.
 * @property transport the transport details as a JSON object.
 */
data class ConduitShardDisabled(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val conduitId: String,
    val shardId: String,
    val status: String,
    val transport: ConduitTransport,
) : TwitchEvent
