package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.ChannelEmote
import io.github.captnblubber.twitchkt.helix.model.ChatBadge
import io.github.captnblubber.twitchkt.helix.model.ChatColorEntry
import io.github.captnblubber.twitchkt.helix.model.ChatSettings
import io.github.captnblubber.twitchkt.helix.model.Chatter
import io.github.captnblubber.twitchkt.helix.model.SendChatMessageRequest
import io.github.captnblubber.twitchkt.helix.model.SendMessageResponse
import io.github.captnblubber.twitchkt.helix.model.SharedChatSession
import io.github.captnblubber.twitchkt.helix.model.UserEmote
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Chat API resource.
 *
 * Provides methods for managing chat messages, emotes, badges, settings, colors,
 * announcements, shared chat sessions, and user emotes.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#chat">Twitch API Reference - Chat</a>
 */
class ChatResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Send Chat Message](https://dev.twitch.tv/docs/api/reference/#send-chat-message)
     *
     * Sends a message to the broadcaster's chat room. To send a chat message, your app may use
     * an app access token or user access token.
     *
     * **Rate Limits**: The user sending the chat message is limited to sending 20 chat messages
     * per 30 seconds.
     *
     * @param request the message payload including broadcaster ID, sender ID, and message text.
     * @return the response indicating whether the message was sent successfully.
     */
    @RequiresScope(TwitchScope.USER_WRITE_CHAT)
    suspend fun sendMessage(request: SendChatMessageRequest): SendMessageResponse {
        http.validateScopes(TwitchScope.USER_WRITE_CHAT)
        return http.post<SendMessageResponse>("chat/messages", body = http.encodeBody(request)).requireFirst("chat/messages")
    }

    /**
     * [Twitch API: Get Global Chat Badges](https://dev.twitch.tv/docs/api/reference/#get-global-chat-badges)
     *
     * Gets Twitch's list of chat badges, which users may use in any channel's chat room.
     * For a list of custom badges for a specific channel, call [getChannelBadges].
     *
     * @return the list of chat badges. The list is sorted in ascending order by `set_id`,
     * and within a set, the list is sorted in ascending order by the badge version `id`.
     */
    suspend fun getGlobalBadges(): List<ChatBadge> = http.get<ChatBadge>("chat/badges/global").data

    /**
     * [Twitch API: Get Channel Chat Badges](https://dev.twitch.tv/docs/api/reference/#get-channel-chat-badges)
     *
     * Gets the broadcaster's list of custom chat badges. The list is empty if the broadcaster
     * hasn't created custom chat badges. For a list of global badges, call [getGlobalBadges].
     *
     * @param broadcasterId the ID of the broadcaster whose chat badges you want to get.
     * @return the list of chat badges. The list is sorted in ascending order by `set_id`,
     * and within a set, the list is sorted in ascending order by the badge version `id`.
     */
    suspend fun getChannelBadges(broadcasterId: String): List<ChatBadge> {
        val params = listOf("broadcaster_id" to broadcasterId)
        return http.get<ChatBadge>("chat/badges", params).data
    }

    /**
     * [Twitch API: Get Chatters](https://dev.twitch.tv/docs/api/reference/#get-chatters)
     *
     * Gets the list of users that are connected to the broadcaster's chat session.
     *
     * **NOTE:** There is a delay between when users join and leave a chat and when the list
     * is updated accordingly.
     *
     * To determine whether a user is a moderator or VIP, use the [getChannelBadges] endpoint.
     * You can also check the user's badges in chat messages.
     *
     * @param broadcasterId the ID of the broadcaster whose list of chatters you want to get.
     * @param moderatorId the ID of the broadcaster or one of the broadcaster's moderators.
     * This ID must match the user ID in the user access token.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 1000. The default is 100.
     * @param after the cursor used to get the next page of results. The Pagination object in
     * the response contains the cursor's value.
     * @return the list of users connected to the broadcaster's chat room.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_CHATTERS)
    suspend fun getChatters(
        broadcasterId: String,
        moderatorId: String,
        first: Int = 100,
        after: String? = null,
    ): List<Chatter> {
        http.validateScopes(TwitchScope.MODERATOR_READ_CHATTERS)
        return http
            .get<Chatter>(
                "chat/chatters",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    add("moderator_id" to moderatorId)
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                },
            ).data
    }

    /**
     * Returns a paginated [Flow] of all users in the broadcaster's chat room.
     * Automatically follows pagination cursors until all chatters have been fetched.
     *
     * @param broadcasterId the ID of the broadcaster whose chatters to get.
     * @param moderatorId the ID of the broadcaster or a moderator. Must match the token user.
     * @param pageSize items per page (max 1000).
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_CHATTERS)
    fun getAllChatters(
        broadcasterId: String,
        moderatorId: String,
        pageSize: Int = 1000,
    ): Flow<Chatter> =
        http.paginate<Chatter>(
            endpoint = "chat/chatters",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                ),
            pageSize = pageSize,
        )

    /**
     * [Twitch API: Get Channel Emotes](https://dev.twitch.tv/docs/api/reference/#get-channel-emotes)
     *
     * Gets the broadcaster's list of custom emotes. Broadcasters create these custom emotes
     * for users who subscribe to or follow the channel or cheer Bits in the channel's chat window.
     *
     * For information about the custom emotes, see
     * [subscriber emotes](https://help.twitch.tv/s/article/subscriber-emote-guide),
     * [Bits tier emotes](https://help.twitch.tv/s/article/custom-bit-badges-guide), and
     * [follower emotes](https://blog.twitch.tv/en/2021/06/04/kicking-off-10-years-with-our-community/).
     *
     * **NOTE:** With the exception of custom combos, the emote images are not returned at a 3.0 scale.
     *
     * @param broadcasterId an ID that identifies the broadcaster whose emotes you want to get.
     * @return the list of emotes that the specified broadcaster created. If the broadcaster
     * hasn't created custom emotes, the list is empty.
     */
    suspend fun getChannelEmotes(broadcasterId: String): List<ChannelEmote> =
        http
            .get<ChannelEmote>(
                "chat/emotes",
                listOf("broadcaster_id" to broadcasterId),
            ).data

    /**
     * [Twitch API: Get Global Emotes](https://dev.twitch.tv/docs/api/reference/#get-global-emotes)
     *
     * Gets the list of [global emotes](https://www.twitch.tv/creatorcamp/en/paths/getting-started-on-twitch/emotes/).
     * Global emotes are Twitch-created emotes that users can use in any Twitch chat.
     *
     * @return the list of global emotes.
     */
    suspend fun getGlobalEmotes(): List<ChannelEmote> = http.get<ChannelEmote>("chat/emotes/global").data

    /**
     * [Twitch API: Get Emote Sets](https://dev.twitch.tv/docs/api/reference/#get-emote-sets)
     *
     * Gets emotes for one or more specified emote sets.
     *
     * An emote set groups emotes that have a similar context. For example, Twitch places all
     * the subscriber emotes that a broadcaster uploads for their channel in the same emote set.
     *
     * @param emoteSetIds the IDs of the emote sets to get. You may specify a maximum of 25 IDs.
     * The response contains only those IDs that are valid.
     * @return the list of emotes found in the specified emote sets.
     */
    suspend fun getEmoteSets(emoteSetIds: List<String>): List<ChannelEmote> =
        http
            .get<ChannelEmote>(
                "chat/emotes/set",
                emoteSetIds.map { "emote_set_id" to it },
            ).data

    /**
     * [Twitch API: Get Chat Settings](https://dev.twitch.tv/docs/api/reference/#get-chat-settings)
     *
     * Gets the broadcaster's chat settings.
     *
     * To include the `non_moderator_chat_delay` and `non_moderator_chat_delay_duration` settings
     * in the response, you must specify a user access token that includes the
     * `moderator:read:chat_settings` scope. The [moderatorId] must match the user ID in the access token.
     *
     * @param broadcasterId the ID of the broadcaster whose chat settings you want to get.
     * @param moderatorId the ID of the broadcaster or one of the broadcaster's moderators.
     * Required only if you want to include the `non_moderator_chat_delay` and
     * `non_moderator_chat_delay_duration` settings in the response.
     * @return the chat settings for the specified broadcaster's channel.
     */
    suspend fun getSettings(
        broadcasterId: String,
        moderatorId: String? = null,
    ): ChatSettings =
        http
            .get<ChatSettings>(
                "chat/settings",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    moderatorId?.let { add("moderator_id" to it) }
                },
            ).requireFirst("chat/settings")

    /**
     * [Twitch API: Update Chat Settings](https://dev.twitch.tv/docs/api/reference/#update-chat-settings)
     *
     * Updates the broadcaster's chat settings. All fields are optional; specify only those
     * fields that you want to update.
     *
     * @param broadcasterId the ID of the broadcaster whose chat settings you want to update.
     * @param moderatorId the ID of a user that has permission to moderate the broadcaster's
     * chat room, or the broadcaster's ID if they're making the update. This ID must match
     * the user ID in the user access token.
     * @param slowMode a Boolean value that determines whether chat messages must wait before
     * being sent. Set to `true` to require users to wait; set to `false` to remove the wait time.
     * @param slowModeWaitTime the amount of time, in seconds, that users must wait between
     * sending messages (3–120). Set only if [slowMode] is `true`.
     * @param followerMode a Boolean value that determines whether the broadcaster restricts the
     * chat room to followers only. Set to `true` to restrict to followers only; set to `false`
     * to remove the restriction.
     * @param followerModeDuration the length of time, in minutes, that users must follow the
     * broadcaster before being able to participate in the chat room (0–129600). Set only if
     * [followerMode] is `true`. 0 means no minimum.
     * @param subscriberMode a Boolean value that determines whether only users that subscribe
     * to the broadcaster's channel may talk in the chat room.
     * @param emoteMode a Boolean value that determines whether chat messages must contain only
     * emotes.
     * @param uniqueChatMode a Boolean value that determines whether the broadcaster requires
     * users to post only unique messages in the chat room (formerly R9K).
     * @param nonModeratorChatDelay a Boolean value that determines whether the broadcaster adds
     * a short delay before chat messages appear in the chat room. This gives chat moderators
     * and bots a chance to remove them before viewers can see the message.
     * @param nonModeratorChatDelayDuration the amount of time, in seconds, that messages are
     * delayed before appearing in chat. Possible values are: 2, 4, and 6. Set only if
     * [nonModeratorChatDelay] is `true`.
     * @return the updated chat settings.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_CHAT_SETTINGS)
    suspend fun updateSettings(
        broadcasterId: String,
        moderatorId: String,
        slowMode: Boolean? = null,
        slowModeWaitTime: Int? = null,
        followerMode: Boolean? = null,
        followerModeDuration: Int? = null,
        subscriberMode: Boolean? = null,
        emoteMode: Boolean? = null,
        uniqueChatMode: Boolean? = null,
        nonModeratorChatDelay: Boolean? = null,
        nonModeratorChatDelayDuration: Int? = null,
    ): ChatSettings {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_CHAT_SETTINGS)
        return http
            .patch<ChatSettings>(
                "chat/settings",
                params =
                    listOf(
                        "broadcaster_id" to broadcasterId,
                        "moderator_id" to moderatorId,
                    ),
                body =
                    http.encodeBody(
                        UpdateChatSettingsRequest(
                            slowMode = slowMode,
                            slowModeWaitTime = slowModeWaitTime,
                            followerMode = followerMode,
                            followerModeDuration = followerModeDuration,
                            subscriberMode = subscriberMode,
                            emoteMode = emoteMode,
                            uniqueChatMode = uniqueChatMode,
                            nonModeratorChatDelay = nonModeratorChatDelay,
                            nonModeratorChatDelayDuration = nonModeratorChatDelayDuration,
                        ),
                    ),
            ).requireFirst("chat/settings")
    }

    /**
     * [Twitch API: Send Chat Announcement](https://dev.twitch.tv/docs/api/reference/#send-chat-announcement)
     *
     * Sends an announcement to the broadcaster's chat room.
     *
     * @param broadcasterId the ID of the broadcaster that owns the chat room to send the
     * announcement to.
     * @param moderatorId the ID of a user who has permission to moderate the broadcaster's
     * chat room, or the broadcaster's ID if they're sending the announcement. This ID must
     * match the user ID in the user access token.
     * @param message the announcement to make in the broadcaster's chat room. Announcements
     * are limited to a maximum of 500 characters; announcements longer than 500 characters
     * are truncated.
     * @param color the color used to highlight the announcement. Possible case-sensitive values
     * are: `blue`, `green`, `orange`, `purple`, `primary` (default). If color is set to
     * `primary` or is not set, the channel's accent color is used to highlight the announcement.
     * @param sourceOnly determines if the chat announcement is sent only to the source channel
     * during a shared chat session. This parameter can only be set when utilizing an App Access
     * Token. Defaults to `false`.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_ANNOUNCEMENTS)
    suspend fun sendAnnouncement(
        broadcasterId: String,
        moderatorId: String,
        message: String,
        color: String = "primary",
        sourceOnly: Boolean? = null,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_ANNOUNCEMENTS)
        http.postNoContent(
            "chat/announcements",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                ),
            body =
                http.encodeBody(
                    AnnouncementRequest(
                        message = message,
                        color = color,
                        sourceOnly = sourceOnly,
                    ),
                ),
        )
    }

    /**
     * [Twitch API: Get User Chat Color](https://dev.twitch.tv/docs/api/reference/#get-user-chat-color)
     *
     * Gets the color used for the user's name in chat.
     *
     * @param userIds the IDs of the users whose username color you want to get. To specify more
     * than one user, include the parameter for each user to get. You may specify a maximum of
     * 100 IDs. The API ignores duplicate IDs and IDs that weren't found.
     * @return the list of users and the color code that's used for their name.
     */
    suspend fun getUserColor(userIds: List<String>): List<ChatColorEntry> =
        http
            .get<ChatColorEntry>(
                "chat/color",
                userIds.map { "user_id" to it },
            ).data

    /**
     * [Twitch API: Update User Chat Color](https://dev.twitch.tv/docs/api/reference/#update-user-chat-color)
     *
     * Updates the color used for the user's name in chat.
     *
     * @param userId the ID of the user whose chat color you want to update. This ID must match
     * the user ID in the access token.
     * @param color the color to use for the user's name in chat. All users may specify one of
     * the following named color values: `blue`, `blue_violet`, `cadet_blue`, `chocolate`,
     * `coral`, `dodger_blue`, `firebrick`, `golden_rod`, `green`, `hot_pink`, `orange_red`,
     * `red`, `sea_green`, `spring_green`, `yellow_green`. Turbo and Prime users may also
     * specify a Hex color code (for example, `#9146FF`). If you use a Hex color code, remember
     * to URL-encode the `#` as `%23`.
     */
    @RequiresScope(TwitchScope.USER_MANAGE_CHAT_COLOR)
    suspend fun updateUserColor(
        userId: String,
        color: String,
    ) {
        http.validateScopes(TwitchScope.USER_MANAGE_CHAT_COLOR)
        http.putNoContent(
            "chat/color",
            params =
                listOf(
                    "user_id" to userId,
                    "color" to color,
                ),
        )
    }

    /**
     * [Twitch API: Get Shared Chat Session](https://dev.twitch.tv/docs/api/reference/#get-shared-chat-session)
     *
     * Retrieves the active shared chat session for a channel.
     *
     * **NOTE:** This endpoint is only available for channels that are in an active shared chat
     * session. If the channel is not in a shared chat session, the response will be a 404 error.
     *
     * @param broadcasterId the User ID of the channel broadcaster.
     * @return the active [SharedChatSession] for the specified channel.
     */
    suspend fun getSharedChatSession(broadcasterId: String): SharedChatSession =
        http
            .get<SharedChatSession>(
                "shared_chat/session",
                listOf("broadcaster_id" to broadcasterId),
            ).requireFirst("shared_chat/session")

    /**
     * [Twitch API: Get User Emotes](https://dev.twitch.tv/docs/api/reference/#get-user-emotes)
     *
     * Retrieves emotes available to the user across all channels. The user token in the request
     * identifies the user.
     *
     * @param userId the ID of the user. This ID must match the user ID in the user access token.
     * @param after the cursor used to get the next page of results. The Pagination object in
     * the response contains the cursor's value.
     * @param broadcasterId the User ID of a broadcaster you wish to get follower emotes of.
     * Using this query parameter will guarantee inclusion of the broadcaster's follower emotes
     * in the response body. If the user is not following this broadcaster, follower emotes will
     * not appear in the response body.
     * @return the list of emotes the user has access to.
     */
    @RequiresScope(TwitchScope.USER_READ_EMOTES)
    suspend fun getUserEmotes(
        userId: String,
        after: String? = null,
        broadcasterId: String? = null,
    ): List<UserEmote> {
        http.validateScopes(TwitchScope.USER_READ_EMOTES)
        return http
            .get<UserEmote>(
                "chat/emotes/user",
                buildList {
                    add("user_id" to userId)
                    after?.let { add("after" to it) }
                    broadcasterId?.let { add("broadcaster_id" to it) }
                },
            ).data
    }
}

@Serializable
internal data class UpdateChatSettingsRequest(
    @SerialName("slow_mode") val slowMode: Boolean? = null,
    @SerialName("slow_mode_wait_time") val slowModeWaitTime: Int? = null,
    @SerialName("follower_mode") val followerMode: Boolean? = null,
    @SerialName("follower_mode_duration") val followerModeDuration: Int? = null,
    @SerialName("subscriber_mode") val subscriberMode: Boolean? = null,
    @SerialName("emote_mode") val emoteMode: Boolean? = null,
    @SerialName("unique_chat_mode") val uniqueChatMode: Boolean? = null,
    @SerialName("non_moderator_chat_delay") val nonModeratorChatDelay: Boolean? = null,
    @SerialName("non_moderator_chat_delay_duration") val nonModeratorChatDelayDuration: Int? = null,
)

@Serializable
internal data class AnnouncementRequest(
    val message: String,
    val color: String = "primary",
    @SerialName("source_only") val sourceOnly: Boolean? = null,
)
