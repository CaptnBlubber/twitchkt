package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.AutoModCheckResult
import io.github.captnblubber.twitchkt.helix.model.AutoModSettings
import io.github.captnblubber.twitchkt.helix.model.BannedUser
import io.github.captnblubber.twitchkt.helix.model.BlockedTerm
import io.github.captnblubber.twitchkt.helix.model.ChannelRoleUser
import io.github.captnblubber.twitchkt.helix.model.ModeratedChannel
import io.github.captnblubber.twitchkt.helix.model.ShieldModeStatus
import io.github.captnblubber.twitchkt.helix.model.SuspiciousUserStatus
import io.github.captnblubber.twitchkt.helix.model.UnbanRequestResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ModerationResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Check AutoMod Status](https://dev.twitch.tv/docs/api/reference/#check-automod-status)
     *
     * Checks whether AutoMod would flag the specified message for review.
     *
     * @param broadcasterId the ID of the broadcaster whose AutoMod settings and list of blocked terms are used to check the message. This ID must match the user ID in the access token.
     * @param messages the list of messages to check. Each entry contains a caller-defined `msgId` and the `msgText` to check. The list must contain at least one message and may contain up to a maximum of 100 messages.
     * @return the list of messages and whether Twitch would approve them for chat.
     */
    @RequiresScope(TwitchScope.MODERATION_READ)
    suspend fun checkAutoModStatus(
        broadcasterId: String,
        messages: List<AutoModCheckMessage>,
    ): List<AutoModCheckResult> {
        http.validateScopes(TwitchScope.MODERATION_READ)
        return http
            .post<AutoModCheckResult>(
                "moderation/enforcements/status",
                params = listOf("broadcaster_id" to broadcasterId),
                body = http.encodeBody(AutoModCheckRequest(data = messages)),
            ).data
    }

    /**
     * [Twitch API: Manage Held AutoMod Messages](https://dev.twitch.tv/docs/api/reference/#manage-held-automod-messages)
     *
     * Allow or deny the message that AutoMod flagged for review.
     *
     * @param userId the moderator who is approving or denying the held message. This ID must match the user ID in the access token.
     * @param msgId the ID of the message to allow or deny.
     * @param action the action to take for the message. Possible values are: `ALLOW`, `DENY`.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_AUTOMOD)
    suspend fun manageHeldAutoModMessage(
        userId: String,
        msgId: String,
        action: String,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_AUTOMOD)
        http.postNoContent(
            "moderation/automod/message",
            body =
                http.encodeBody(
                    ManageAutoModMessageRequest(
                        userId = userId,
                        msgId = msgId,
                        action = action,
                    ),
                ),
        )
    }

    /**
     * [Twitch API: Get AutoMod Settings](https://dev.twitch.tv/docs/api/reference/#get-automod-settings)
     *
     * Gets the broadcaster's AutoMod settings. The settings are used to automatically block
     * inappropriate or harassing messages from appearing in the broadcaster's chat room.
     *
     * @param broadcasterId the ID of the broadcaster whose AutoMod settings you want to get.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @return the AutoMod settings for the channel.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_AUTOMOD_SETTINGS)
    suspend fun getAutoModSettings(
        broadcasterId: String,
        moderatorId: String,
    ): AutoModSettings {
        http.validateScopes(TwitchScope.MODERATOR_READ_AUTOMOD_SETTINGS)
        return http
            .get<AutoModSettings>(
                "moderation/automod/settings",
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                ),
            ).requireFirst("moderation/automod/settings")
    }

    /**
     * [Twitch API: Update AutoMod Settings](https://dev.twitch.tv/docs/api/reference/#update-automod-settings)
     *
     * Updates the broadcaster's AutoMod settings. The settings are used to automatically block
     * inappropriate or harassing messages from appearing in the broadcaster's chat room.
     *
     * Because PUT is an overwrite operation, you must include all the fields that you want set
     * after the operation completes. You may set either [overallLevel] or the individual settings
     * like [aggression], but not both.
     *
     * @param broadcasterId the ID of the broadcaster whose AutoMod settings you want to update.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param request the AutoMod settings to apply.
     * @return the updated AutoMod settings.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_AUTOMOD_SETTINGS)
    suspend fun updateAutoModSettings(
        broadcasterId: String,
        moderatorId: String,
        request: UpdateAutoModSettingsRequest,
    ): AutoModSettings {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_AUTOMOD_SETTINGS)
        return http
            .put<AutoModSettings>(
                "moderation/automod/settings",
                params =
                    listOf(
                        "broadcaster_id" to broadcasterId,
                        "moderator_id" to moderatorId,
                    ),
                body = http.encodeBody(request),
            ).requireFirst("moderation/automod/settings")
    }

    /**
     * [Twitch API: Get Banned Users](https://dev.twitch.tv/docs/api/reference/#get-banned-users)
     *
     * Gets all users that the broadcaster banned or put in a timeout.
     *
     * @param broadcasterId the ID of the broadcaster whose list of banned users you want to get. This ID must match the user ID in the access token.
     * @param userIds a list of user IDs used to filter the results. You may specify a maximum of 100 IDs. The returned list includes only those users that were banned or put in a timeout.
     * @param first the maximum number of items to return per page in the response. The minimum page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @param before the cursor used to get the previous page of results.
     * @return list of [BannedUser] objects.
     */
    @RequiresScope(TwitchScope.MODERATION_READ)
    suspend fun getBanned(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
        first: Int = 20,
        after: String? = null,
        before: String? = null,
    ): List<BannedUser> {
        http.validateScopes(TwitchScope.MODERATION_READ)
        return http
            .get<BannedUser>(
                "moderation/banned",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    userIds.forEach { add("user_id" to it) }
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                    before?.let { add("before" to it) }
                },
            ).data
    }

    /**
     * [Twitch API: Ban User](https://dev.twitch.tv/docs/api/reference/#ban-user)
     *
     * Bans a user from participating in the specified broadcaster's chat room or puts them in a timeout.
     *
     * @param broadcasterId the ID of the broadcaster whose chat room the user is being banned from.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param userId the ID of the user to ban or put in a timeout.
     * @param reason the reason you're banning the user or putting them in a timeout. The text is user defined and is limited to a maximum of 500 characters.
     * @param duration to ban a user indefinitely, don't include this field. To put a user in a timeout, specify the timeout period, in seconds. The minimum timeout is 1 second and the maximum is 1,209,600 seconds (2 weeks).
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_BANNED_USERS)
    suspend fun ban(
        broadcasterId: String,
        moderatorId: String,
        userId: String,
        reason: String = "",
        duration: Int? = null,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_BANNED_USERS)
        http.postNoContent(
            "moderation/bans",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                ),
            body =
                http.encodeBody(
                    BanUserRequest(
                        data = BanUserData(userId = userId, reason = reason, duration = duration),
                    ),
                ),
        )
    }

    /**
     * [Twitch API: Unban User](https://dev.twitch.tv/docs/api/reference/#unban-user)
     *
     * Removes the ban or timeout that was placed on the specified user.
     *
     * @param broadcasterId the ID of the broadcaster whose chat room the user is banned from chatting in.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param userId the ID of the user to remove the ban or timeout from.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_BANNED_USERS)
    suspend fun unban(
        broadcasterId: String,
        moderatorId: String,
        userId: String,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_BANNED_USERS)
        http.deleteNoContent(
            "moderation/bans",
            listOf(
                "broadcaster_id" to broadcasterId,
                "moderator_id" to moderatorId,
                "user_id" to userId,
            ),
        )
    }

    /**
     * [Twitch API: Get Unban Requests](https://dev.twitch.tv/docs/api/reference/#get-unban-requests)
     *
     * Gets a list of unban requests for a broadcaster's channel.
     *
     * @param broadcasterId the ID of the broadcaster whose channel is receiving unban requests.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's unban requests. This ID must match the user ID in the user access token.
     * @param status filter by a status: `pending`, `approved`, `denied`, `acknowledged`, `canceled`.
     * @param userId the ID used to filter what unban requests are returned.
     * @param after cursor used to get the next page of results.
     * @param first the maximum number of items to return per page in response.
     * @return list of [UnbanRequestResponse] objects.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_UNBAN_REQUESTS)
    suspend fun getUnbanRequests(
        broadcasterId: String,
        moderatorId: String,
        status: String = "pending",
        userId: String? = null,
        after: String? = null,
        first: Int = 20,
    ): List<UnbanRequestResponse> {
        http.validateScopes(TwitchScope.MODERATOR_READ_UNBAN_REQUESTS)
        return http
            .get<UnbanRequestResponse>(
                "moderation/unban_requests",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    add("moderator_id" to moderatorId)
                    add("status" to status)
                    userId?.let { add("user_id" to it) }
                    after?.let { add("after" to it) }
                    add("first" to first.toString())
                },
            ).data
    }

    /**
     * [Twitch API: Resolve Unban Requests](https://dev.twitch.tv/docs/api/reference/#resolve-unban-requests)
     *
     * Resolves an unban request by approving or denying it.
     *
     * @param broadcasterId the ID of the broadcaster whose channel is approving or denying the unban request.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's unban requests. This ID must match the user ID in the user access token.
     * @param unbanRequestId the ID of the unban request to resolve.
     * @param status resolution status: `approved` or `denied`.
     * @param resolutionText message supplied by the unban request resolver. The message is limited to a maximum of 500 characters.
     * @return the resolved [UnbanRequestResponse].
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_UNBAN_REQUESTS)
    suspend fun resolveUnbanRequest(
        broadcasterId: String,
        moderatorId: String,
        unbanRequestId: String,
        status: String,
        resolutionText: String? = null,
    ): UnbanRequestResponse {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_UNBAN_REQUESTS)
        return http
            .patch<UnbanRequestResponse>(
                "moderation/unban_requests",
                params =
                    buildList {
                        add("broadcaster_id" to broadcasterId)
                        add("moderator_id" to moderatorId)
                        add("unban_request_id" to unbanRequestId)
                        add("status" to status)
                        resolutionText?.let { add("resolution_text" to it) }
                    },
            ).requireFirst("moderation/unban_requests")
    }

    /**
     * [Twitch API: Get Blocked Terms](https://dev.twitch.tv/docs/api/reference/#get-blocked-terms)
     *
     * Gets the broadcaster's list of non-private, blocked words or phrases. These are the terms
     * that the broadcaster or moderator added manually or that were denied by AutoMod.
     *
     * @param broadcasterId the ID of the broadcaster whose blocked terms you're getting.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param first the maximum number of items to return per page in the response. The minimum page size is 1 item per page and the maximum is 100 items per page. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return list of [BlockedTerm] objects.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_BLOCKED_TERMS)
    suspend fun getBlockedTerms(
        broadcasterId: String,
        moderatorId: String,
        first: Int = 20,
        after: String? = null,
    ): List<BlockedTerm> {
        http.validateScopes(TwitchScope.MODERATOR_READ_BLOCKED_TERMS)
        return http
            .get<BlockedTerm>(
                "moderation/blocked_terms",
                buildList {
                    add("broadcaster_id" to broadcasterId)
                    add("moderator_id" to moderatorId)
                    add("first" to first.toString())
                    after?.let { add("after" to it) }
                },
            ).data
    }

    /**
     * [Twitch API: Add Blocked Term](https://dev.twitch.tv/docs/api/reference/#add-blocked-term)
     *
     * Adds a word or phrase to the broadcaster's list of blocked terms. These are the terms that
     * the broadcaster doesn't want used in their chat room.
     *
     * @param broadcasterId the ID of the broadcaster that owns the list of blocked terms.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param text the word or phrase to block from being used in the broadcaster's chat room. The term must contain a minimum of 2 characters and may contain up to a maximum of 500 characters. Terms may include a wildcard character (*).
     * @return the created [BlockedTerm].
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_BLOCKED_TERMS)
    suspend fun addBlockedTerm(
        broadcasterId: String,
        moderatorId: String,
        text: String,
    ): BlockedTerm {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_BLOCKED_TERMS)
        return http
            .post<BlockedTerm>(
                "moderation/blocked_terms",
                params =
                    listOf(
                        "broadcaster_id" to broadcasterId,
                        "moderator_id" to moderatorId,
                    ),
                body = http.encodeBody(AddBlockedTermRequest(text = text)),
            ).requireFirst("moderation/blocked_terms")
    }

    /**
     * [Twitch API: Remove Blocked Term](https://dev.twitch.tv/docs/api/reference/#remove-blocked-term)
     *
     * Removes the word or phrase from the broadcaster's list of blocked terms.
     *
     * @param broadcasterId the ID of the broadcaster that owns the list of blocked terms.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param id the ID of the blocked term to remove from the broadcaster's list of blocked terms.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_BLOCKED_TERMS)
    suspend fun removeBlockedTerm(
        broadcasterId: String,
        moderatorId: String,
        id: String,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_BLOCKED_TERMS)
        http.deleteNoContent(
            "moderation/blocked_terms",
            listOf(
                "broadcaster_id" to broadcasterId,
                "moderator_id" to moderatorId,
                "id" to id,
            ),
        )
    }

    /**
     * [Twitch API: Delete Chat Messages](https://dev.twitch.tv/docs/api/reference/#delete-chat-messages)
     *
     * Removes a single chat message or all chat messages from the broadcaster's chat room.
     *
     * @param broadcasterId the ID of the broadcaster that owns the chat room to remove messages from.
     * @param moderatorId the ID of the broadcaster or a user that has permission to moderate the broadcaster's chat room. This ID must match the user ID in the user access token.
     * @param messageId the ID of the message to remove. If not specified, the request removes all messages in the broadcaster's chat room.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_CHAT_MESSAGES)
    suspend fun deleteMessage(
        broadcasterId: String,
        moderatorId: String,
        messageId: String? = null,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_CHAT_MESSAGES)
        http.deleteNoContent(
            "moderation/chat",
            buildList {
                add("broadcaster_id" to broadcasterId)
                add("moderator_id" to moderatorId)
                messageId?.let { add("message_id" to it) }
            },
        )
    }

    /**
     * [Twitch API: Get Moderated Channels](https://dev.twitch.tv/docs/api/reference/#get-moderated-channels)
     *
     * Gets a list of channels that the specified user has moderator privileges in.
     *
     * @param userId a user's ID. Returns the list of channels that this user has moderator privileges in. This ID must match the user ID in the user OAuth token.
     * @param after the cursor used to get the next page of results.
     * @param first the maximum number of items to return per page in the response. Minimum page size is 1 item per page and the maximum is 100. The default is 20.
     * @return list of [ModeratedChannel] objects.
     */
    @RequiresScope(TwitchScope.USER_READ_MODERATED_CHANNELS)
    suspend fun getModeratedChannels(
        userId: String,
        after: String? = null,
        first: Int = 20,
    ): List<ModeratedChannel> {
        http.validateScopes(TwitchScope.USER_READ_MODERATED_CHANNELS)
        return http
            .get<ModeratedChannel>(
                "moderation/channels",
                buildList {
                    add("user_id" to userId)
                    after?.let { add("after" to it) }
                    add("first" to first.toString())
                },
            ).data
    }

    /**
     * [Twitch API: Get Moderators](https://dev.twitch.tv/docs/api/reference/#get-moderators)
     *
     * Gets all users allowed to moderate the broadcaster's chat room.
     *
     * @param broadcasterId the ID of the broadcaster whose list of moderators you want to get. This ID must match the user ID in the access token.
     * @param userIds a list of user IDs used to filter the results. You may specify a maximum of 100 IDs.
     * @return a [Flow] emitting each moderator.
     */
    @RequiresScope(TwitchScope.MODERATION_READ)
    fun getAllModerators(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
    ): Flow<ChannelRoleUser> {
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userIds.forEach { add("user_id" to it) }
            }
        return http
            .paginate<ChannelRoleUser>("moderation/moderators", params)
            .onStart { http.validateScopes(TwitchScope.MODERATION_READ) }
    }

    /**
     * [Twitch API: Get Moderators](https://dev.twitch.tv/docs/api/reference/#get-moderators)
     *
     * Fetches a single page of moderators for the broadcaster's chat room.
     *
     * @param broadcasterId the ID of the broadcaster whose list of moderators you want to get. This ID must match the user ID in the access token.
     * @param userIds a list of user IDs used to filter the results. You may specify a maximum of 100 IDs.
     * @param cursor the cursor used to get the next page of results. Pass `null` to get the first page.
     * @param pageSize the maximum number of items to return (1–100). `null` uses the API default (20).
     * @return a [Page] containing the moderators on this page and the cursor for the next page.
     */
    @RequiresScope(TwitchScope.MODERATION_READ)
    suspend fun getModerators(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<ChannelRoleUser> {
        http.validateScopes(TwitchScope.MODERATION_READ)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userIds.forEach { add("user_id" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "moderation/moderators", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Add Channel Moderator](https://dev.twitch.tv/docs/api/reference/#add-channel-moderator)
     *
     * Adds a moderator to the broadcaster's chat room.
     *
     * **Rate Limits**: The broadcaster may add a maximum of 10 moderators within a 10-second window.
     *
     * @param broadcasterId the ID of the broadcaster that owns the chat room. This ID must match the user ID in the access token.
     * @param userId the ID of the user to add as a moderator in the broadcaster's chat room.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_MODERATORS)
    suspend fun addModerator(
        broadcasterId: String,
        userId: String,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_MODERATORS)
        http.postNoContent(
            "moderation/moderators",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "user_id" to userId,
                ),
        )
    }

    /**
     * [Twitch API: Remove Channel Moderator](https://dev.twitch.tv/docs/api/reference/#remove-channel-moderator)
     *
     * Removes a moderator from the broadcaster's chat room.
     *
     * **Rate Limits**: The broadcaster may remove a maximum of 10 moderators within a 10-second window.
     *
     * @param broadcasterId the ID of the broadcaster that owns the chat room. This ID must match the user ID in the access token.
     * @param userId the ID of the user to remove as a moderator from the broadcaster's chat room.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_MODERATORS)
    suspend fun removeModerator(
        broadcasterId: String,
        userId: String,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_MODERATORS)
        http.deleteNoContent(
            "moderation/moderators",
            listOf(
                "broadcaster_id" to broadcasterId,
                "user_id" to userId,
            ),
        )
    }

    /**
     * [Twitch API: Get VIPs](https://dev.twitch.tv/docs/api/reference/#get-vips)
     *
     * Gets a list of the broadcaster's VIPs.
     *
     * @param broadcasterId the ID of the broadcaster whose list of VIPs you want to get. This ID must match the user ID in the access token.
     * @param userIds filters the list for specific VIPs. You may specify a maximum of 100 IDs. Ignores the ID of those users in the list that aren't VIPs.
     * @return a [Flow] emitting each VIP.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_VIPS)
    fun getAllVIPs(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
    ): Flow<ChannelRoleUser> {
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userIds.forEach { add("user_id" to it) }
            }
        return http
            .paginate<ChannelRoleUser>("channels/vips", params)
            .onStart { http.validateScopes(TwitchScope.CHANNEL_READ_VIPS) }
    }

    /**
     * [Twitch API: Get VIPs](https://dev.twitch.tv/docs/api/reference/#get-vips)
     *
     * Fetches a single page of VIPs for the broadcaster's channel.
     *
     * @param broadcasterId the ID of the broadcaster whose list of VIPs you want to get. This ID must match the user ID in the access token.
     * @param userIds filters the list for specific VIPs. You may specify a maximum of 100 IDs.
     * @param cursor the cursor used to get the next page of results. Pass `null` to get the first page.
     * @param pageSize the maximum number of items to return (1–100). `null` uses the API default (20).
     * @return a [Page] containing the VIPs on this page and the cursor for the next page.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_VIPS)
    suspend fun getVIPs(
        broadcasterId: String,
        userIds: List<String> = emptyList(),
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<ChannelRoleUser> {
        http.validateScopes(TwitchScope.CHANNEL_READ_VIPS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                userIds.forEach { add("user_id" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "channels/vips", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Add Channel VIP](https://dev.twitch.tv/docs/api/reference/#add-channel-vip)
     *
     * Adds the specified user as a VIP in the broadcaster's channel.
     *
     * **Rate Limits**: The broadcaster may add a maximum of 10 VIPs within a 10-second window.
     *
     * @param broadcasterId the ID of the broadcaster that's adding the user as a VIP. This ID must match the user ID in the access token.
     * @param userId the ID of the user to give VIP status to.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_VIPS)
    suspend fun addVip(
        broadcasterId: String,
        userId: String,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_VIPS)
        http.postNoContent(
            "channels/vips",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "user_id" to userId,
                ),
        )
    }

    /**
     * [Twitch API: Remove Channel VIP](https://dev.twitch.tv/docs/api/reference/#remove-channel-vip)
     *
     * Removes the specified user as a VIP in the broadcaster's channel.
     *
     * **Rate Limits**: The broadcaster may remove a maximum of 10 VIPs within a 10-second window.
     *
     * @param broadcasterId the ID of the broadcaster who owns the channel where the user has VIP status.
     * @param userId the ID of the user to remove VIP status from.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_VIPS)
    suspend fun removeVip(
        broadcasterId: String,
        userId: String,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_VIPS)
        http.deleteNoContent(
            "channels/vips",
            listOf(
                "broadcaster_id" to broadcasterId,
                "user_id" to userId,
            ),
        )
    }

    /**
     * [Twitch API: Get Shield Mode Status](https://dev.twitch.tv/docs/api/reference/#get-shield-mode-status)
     *
     * Gets the broadcaster's Shield Mode activation status.
     *
     * @param broadcasterId the ID of the broadcaster whose Shield Mode activation status you want to get.
     * @param moderatorId the ID of the broadcaster or a user that is one of the broadcaster's moderators. This ID must match the user ID in the access token.
     * @return the [ShieldModeStatus] for the channel.
     */
    @RequiresScope(TwitchScope.MODERATOR_READ_SHIELD_MODE)
    suspend fun getShieldMode(
        broadcasterId: String,
        moderatorId: String,
    ): ShieldModeStatus {
        http.validateScopes(TwitchScope.MODERATOR_READ_SHIELD_MODE)
        return http
            .get<ShieldModeStatus>(
                "moderation/shield_mode",
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                ),
            ).requireFirst("moderation/shield_mode")
    }

    /**
     * [Twitch API: Update Shield Mode Status](https://dev.twitch.tv/docs/api/reference/#update-shield-mode-status)
     *
     * Activates or deactivates the broadcaster's Shield Mode.
     *
     * @param broadcasterId the ID of the broadcaster whose Shield Mode you want to activate or deactivate.
     * @param moderatorId the ID of the broadcaster or a user that is one of the broadcaster's moderators. This ID must match the user ID in the access token.
     * @param isActive a Boolean value that determines whether to activate Shield Mode. Set to true to activate Shield Mode; otherwise, false to deactivate Shield Mode.
     * @return the updated [ShieldModeStatus].
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_SHIELD_MODE)
    suspend fun updateShieldMode(
        broadcasterId: String,
        moderatorId: String,
        isActive: Boolean,
    ): ShieldModeStatus {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_SHIELD_MODE)
        return http
            .put<ShieldModeStatus>(
                "moderation/shield_mode",
                params =
                    listOf(
                        "broadcaster_id" to broadcasterId,
                        "moderator_id" to moderatorId,
                    ),
                body = http.encodeBody(UpdateShieldModeRequest(isActive = isActive)),
            ).requireFirst("moderation/shield_mode")
    }

    /**
     * [Twitch API: Warn Chat User](https://dev.twitch.tv/docs/api/reference/#warn-chat-user)
     *
     * Warns a user in the specified broadcaster's chat room, preventing them from chat interaction
     * until the warning is acknowledged. New warnings can be issued to a user when they already
     * have a warning in the channel (new warning will replace old warning).
     *
     * @param broadcasterId the ID of the channel in which the warning will take effect.
     * @param moderatorId the ID of the twitch user who requested the warning.
     * @param userId the ID of the twitch user to be warned.
     * @param reason a custom reason for the warning. Max 500 chars.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_WARNINGS)
    suspend fun warn(
        broadcasterId: String,
        moderatorId: String,
        userId: String,
        reason: String,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_WARNINGS)
        http.postNoContent(
            "moderation/warnings",
            params =
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                ),
            body =
                http.encodeBody(
                    WarnUserRequest(
                        data = WarnUserData(userId = userId, reason = reason),
                    ),
                ),
        )
    }

    /**
     * [Twitch API: Send a Shoutout](https://dev.twitch.tv/docs/api/reference/#send-a-shoutout)
     *
     * Sends a Shoutout to the specified broadcaster. Typically, you send Shoutouts when you or one
     * of your moderators notice another broadcaster in your chat, the other broadcaster is
     * streaming, and you want to alert your viewers about the other broadcaster.
     *
     * **Rate Limits**: The broadcaster may send a Shoutout once every 2 minutes. They may send the
     * same broadcaster a Shoutout once every 60 minutes.
     *
     * @param fromId the ID of the broadcaster that's sending the Shoutout.
     * @param toId the ID of the broadcaster that's receiving the Shoutout.
     * @param moderatorId the ID of the broadcaster or a user that is one of the broadcaster's moderators. This ID must match the user ID in the access token.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_SHOUTOUTS)
    suspend fun sendShoutout(
        fromId: String,
        toId: String,
        moderatorId: String,
    ) {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_SHOUTOUTS)
        val params =
            listOf(
                "from_broadcaster_id" to fromId,
                "to_broadcaster_id" to toId,
                "moderator_id" to moderatorId,
            )
        http.postNoContent("chat/shoutouts", params = params)
    }

    /**
     * [Twitch API: Add Suspicious Status to Chat User](https://dev.twitch.tv/docs/api/reference/#add-suspicious-status-to-chat-user)
     *
     * Adds a suspicious user status to a chatter on the broadcaster's channel.
     *
     * @param broadcasterId the user ID of the broadcaster, indicating the channel where the status is being applied.
     * @param moderatorId the user ID of the moderator who is applying the status.
     * @param userId the ID of the user being given the suspicious status.
     * @param status the type of suspicious status. Possible values are: `ACTIVE_MONITORING`, `RESTRICTED`.
     * @return the [SuspiciousUserStatus] result.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_SUSPICIOUS_USERS)
    suspend fun addSuspiciousStatus(
        broadcasterId: String,
        moderatorId: String,
        userId: String,
        status: String,
    ): SuspiciousUserStatus {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_SUSPICIOUS_USERS)
        return http
            .post<SuspiciousUserStatus>(
                "moderation/suspicious_users",
                params =
                    listOf(
                        "broadcaster_id" to broadcasterId,
                        "moderator_id" to moderatorId,
                    ),
                body =
                    http.encodeBody(
                        SuspiciousUserRequest(userId = userId, status = status),
                    ),
            ).requireFirst("moderation/suspicious_users")
    }

    /**
     * [Twitch API: Remove Suspicious Status From Chat User](https://dev.twitch.tv/docs/api/reference/#remove-suspicious-status-from-chat-user)
     *
     * Remove a suspicious user status from a chatter on broadcaster's channel.
     *
     * @param broadcasterId the user ID of the broadcaster, indicating the channel where the status is being removed.
     * @param moderatorId the user ID of the moderator who is removing the status.
     * @param userId the ID of the user having the suspicious status removed.
     * @return the [SuspiciousUserStatus] result.
     */
    @RequiresScope(TwitchScope.MODERATOR_MANAGE_SUSPICIOUS_USERS)
    suspend fun removeSuspiciousStatus(
        broadcasterId: String,
        moderatorId: String,
        userId: String,
    ): SuspiciousUserStatus {
        http.validateScopes(TwitchScope.MODERATOR_MANAGE_SUSPICIOUS_USERS)
        return http
            .delete<SuspiciousUserStatus>(
                "moderation/suspicious_users",
                listOf(
                    "broadcaster_id" to broadcasterId,
                    "moderator_id" to moderatorId,
                    "user_id" to userId,
                ),
            ).requireFirst("moderation/suspicious_users")
    }
}

/**
 * A message to check against AutoMod.
 *
 * @property msgId a caller-defined ID used to correlate this message with the same message in the response.
 * @property msgText the message to check.
 */
@Serializable
data class AutoModCheckMessage(
    @SerialName("msg_id") val msgId: String,
    @SerialName("msg_text") val msgText: String,
)

@Serializable
internal data class AutoModCheckRequest(
    val data: List<AutoModCheckMessage>,
)

@Serializable
internal data class ManageAutoModMessageRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("msg_id") val msgId: String,
    val action: String,
)

@Serializable
data class UpdateAutoModSettingsRequest(
    val aggression: Int? = null,
    val bullying: Int? = null,
    val disability: Int? = null,
    val misogyny: Int? = null,
    @SerialName("overall_level") val overallLevel: Int? = null,
    @SerialName("race_ethnicity_or_religion") val raceEthnicityOrReligion: Int? = null,
    @SerialName("sex_based_terms") val sexBasedTerms: Int? = null,
    @SerialName("sexuality_sex_or_gender") val sexualitySexOrGender: Int? = null,
    val swearing: Int? = null,
)

@Serializable
internal data class SuspiciousUserRequest(
    @SerialName("user_id") val userId: String,
    val status: String,
)

@Serializable
internal data class BanUserData(
    @SerialName("user_id") val userId: String,
    val reason: String = "",
    val duration: Int? = null,
)

@Serializable
internal data class BanUserRequest(
    val data: BanUserData,
)

@Serializable
internal data class AddBlockedTermRequest(
    val text: String,
)

@Serializable
internal data class UpdateShieldModeRequest(
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
internal data class WarnUserData(
    @SerialName("user_id") val userId: String,
    val reason: String,
)

@Serializable
internal data class WarnUserRequest(
    val data: WarnUserData,
)
