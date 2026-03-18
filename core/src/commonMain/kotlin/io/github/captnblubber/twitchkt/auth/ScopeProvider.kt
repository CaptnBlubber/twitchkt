package io.github.captnblubber.twitchkt.auth

fun interface ScopeProvider {
    suspend fun scopes(): Set<TwitchScope>
}
