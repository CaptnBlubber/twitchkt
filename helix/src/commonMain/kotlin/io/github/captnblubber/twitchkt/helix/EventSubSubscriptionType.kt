package io.github.captnblubber.twitchkt.helix

/**
 * Contract for an EventSub subscription type that can be registered via the Helix API.
 *
 * Concrete implementations live in the `twitchkt-eventsub` module. This interface is defined
 * here so that [TwitchHelix] can accept subscription registrations without depending on the
 * full EventSub module.
 *
 * @property type the EventSub subscription type identifier (e.g. `channel.follow`).
 * @property version the schema version for this subscription type.
 */
interface EventSubSubscriptionType {
    val type: String
    val version: String

    /**
     * Serializes the typed condition fields into the `condition` map expected by the Twitch API.
     */
    fun toCondition(): Map<String, String>
}
