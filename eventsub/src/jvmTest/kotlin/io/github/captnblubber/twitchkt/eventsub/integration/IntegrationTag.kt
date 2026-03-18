package io.github.captnblubber.twitchkt.eventsub.integration

/**
 * Returns `true` when `-DintegrationTest=true` is passed to the JVM,
 * indicating the Twitch CLI mock servers are running.
 */
fun isIntegrationEnabled(): Boolean = System.getProperty("integrationTest")?.toBoolean() ?: false
