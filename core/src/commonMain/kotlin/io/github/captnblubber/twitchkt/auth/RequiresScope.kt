package io.github.captnblubber.twitchkt.auth

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class RequiresScope(
    vararg val scopes: TwitchScope,
)
