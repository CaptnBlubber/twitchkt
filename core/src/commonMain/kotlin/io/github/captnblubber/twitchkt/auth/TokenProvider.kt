package io.github.captnblubber.twitchkt.auth

fun interface TokenProvider {
    suspend fun token(): String
}
