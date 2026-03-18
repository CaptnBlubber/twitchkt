package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Whispers API resource.
 *
 * Provides methods for sending whisper messages.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#send-whisper">Twitch API Reference - Whispers</a>
 */
class WhisperResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Send Whisper](https://dev.twitch.tv/docs/api/reference/#send-whisper)
     *
     * Sends a whisper message to the specified user.
     *
     * **NOTE:** The user sending the whisper must have a verified phone number.
     *
     * **NOTE:** The API may silently drop whispers that it suspects of violating Twitch policies.
     * (The API does not indicate that it dropped the whisper; it returns a 204 status code as if
     * it succeeded.)
     *
     * **Rate Limits:** You may whisper to a maximum of 40 unique recipients per day. Within the
     * per day limit, you may whisper a maximum of 3 whispers per second and a maximum of 100
     * whispers per minute.
     *
     * @param fromUserId the ID of the user sending the whisper. This user must have a verified
     * phone number. This ID must match the user ID in the user access token.
     * @param toUserId the ID of the user to receive the whisper.
     * @param message the whisper message to send. The message must not be empty. The maximum
     * message lengths are: 500 characters if the recipient hasn't whispered you before, 10,000
     * characters if the recipient has whispered you before. Messages that exceed the maximum
     * length are truncated.
     */
    @RequiresScope(TwitchScope.USER_MANAGE_WHISPERS)
    suspend fun send(
        fromUserId: String,
        toUserId: String,
        message: String,
    ) {
        http.validateScopes(TwitchScope.USER_MANAGE_WHISPERS)
        http.postNoContent(
            "whispers",
            params =
                listOf(
                    "from_user_id" to fromUserId,
                    "to_user_id" to toUserId,
                ),
            body = http.encodeBody(SendWhisperRequest(message = message)),
        )
    }
}

@Serializable
internal data class SendWhisperRequest(
    val message: String,
)
