package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.ActiveExtensions
import io.github.captnblubber.twitchkt.helix.model.BlockedUser
import io.github.captnblubber.twitchkt.helix.model.User
import io.github.captnblubber.twitchkt.helix.model.UserExtension
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Users API resource.
 *
 * Provides methods for getting and updating user information, managing block lists,
 * and working with user extensions.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-users">Twitch API Reference - Users</a>
 */
class UserResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Users](https://dev.twitch.tv/docs/api/reference/#get-users)
     *
     * Gets information about one or more users.
     *
     * You may look up users using their user ID, login name, or both but the total number of
     * entries must not exceed 100.
     *
     * If you don't specify IDs or login names, the request returns information about the user
     * in the access token if you specify a user access token.
     *
     * To include the user's verified email address in the response, you must use a user access
     * token that includes the `user:read:email` scope.
     *
     * @param ids the IDs of the users to get. You may specify a maximum of 100 IDs.
     * @param logins the login names of the users to get. You may specify a maximum of 100 login names.
     * @return the list of matching users.
     */
    suspend fun getUsers(
        ids: List<String> = emptyList(),
        logins: List<String> = emptyList(),
    ): List<User> {
        val params =
            buildList {
                ids.forEach { add("id" to it) }
                logins.forEach { add("login" to it) }
            }
        return http.get<User>("users", params).data
    }

    /**
     * [Twitch API: Update User](https://dev.twitch.tv/docs/api/reference/#update-user)
     *
     * Updates the specified user's information. The user ID in the OAuth token identifies the
     * user whose information you want to update.
     *
     * To include the user's verified email address in the response, the user access token must
     * also include the `user:read:email` scope.
     *
     * @param description the string to update the channel's description to. The description is
     * limited to a maximum of 300 characters. To remove the description, specify this parameter
     * but don't set its value (e.g., pass an empty string).
     * @return the updated user.
     */
    @RequiresScope(TwitchScope.USER_EDIT)
    suspend fun updateUser(description: String? = null): User {
        http.validateScopes(TwitchScope.USER_EDIT)
        val params =
            buildList {
                description?.let { add("description" to it) }
            }
        return http.put<User>("users", params = params).requireFirst("users")
    }

    /**
     * [Twitch API: Get User Block List](https://dev.twitch.tv/docs/api/reference/#get-user-block-list)
     *
     * Gets the list of users that the broadcaster has blocked.
     *
     * @param broadcasterId the ID of the broadcaster whose block list you want to get. This ID
     * must match the user ID in the access token.
     * @param first the maximum number of items to return per page in the response. The minimum
     * page size is 1 item per page and the maximum is 100. The default is 20.
     * @param after the cursor used to get the next page of results.
     * @return the list of blocked users.
     */
    @RequiresScope(TwitchScope.USER_READ_BLOCKED_USERS, TwitchScope.USER_MANAGE_BLOCKED_USERS)
    suspend fun getBlockList(
        broadcasterId: String,
        first: Int = 20,
        after: String? = null,
    ): List<BlockedUser> {
        http.validateAnyScope(TwitchScope.USER_READ_BLOCKED_USERS, TwitchScope.USER_MANAGE_BLOCKED_USERS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                add("first" to first.toString())
                after?.let { add("after" to it) }
            }
        return http.get<BlockedUser>("users/blocks", params).data
    }

    /**
     * [Twitch API: Block User](https://dev.twitch.tv/docs/api/reference/#block-user)
     *
     * Blocks the specified user from interacting with or being followed by the broadcaster.
     * The user ID in the OAuth token identifies the broadcaster who is blocking the user.
     *
     * @param targetUserId the ID of the user to block.
     * @param sourceContext the location where the harassment took place that is causing the
     * broadcaster to block the user. Possible values are: `chat`, `whisper`.
     * @param reason the reason the broadcaster is blocking the user. Possible values are:
     * `harassment`, `spam`, `other`.
     */
    @RequiresScope(TwitchScope.USER_MANAGE_BLOCKED_USERS)
    suspend fun blockUser(
        targetUserId: String,
        sourceContext: String? = null,
        reason: String? = null,
    ) {
        http.validateScopes(TwitchScope.USER_MANAGE_BLOCKED_USERS)
        val params =
            buildList {
                add("target_user_id" to targetUserId)
                sourceContext?.let { add("source_context" to it) }
                reason?.let { add("reason" to it) }
            }
        http.putNoContent("users/blocks", params = params)
    }

    /**
     * [Twitch API: Unblock User](https://dev.twitch.tv/docs/api/reference/#unblock-user)
     *
     * Removes the user from the broadcaster's block list. The user ID in the OAuth token
     * identifies the broadcaster who is removing the block.
     *
     * @param targetUserId the ID of the user to unblock.
     */
    @RequiresScope(TwitchScope.USER_MANAGE_BLOCKED_USERS)
    suspend fun unblockUser(targetUserId: String) {
        http.validateScopes(TwitchScope.USER_MANAGE_BLOCKED_USERS)
        val params = listOf("target_user_id" to targetUserId)
        http.deleteNoContent("users/blocks", params)
    }

    /**
     * [Twitch API: Get User Extensions](https://dev.twitch.tv/docs/api/reference/#get-user-extensions)
     *
     * Gets a list of all extensions (both active and inactive) that the broadcaster has installed.
     * The user ID in the access token identifies the broadcaster.
     *
     * @return the list of extensions that the user has installed.
     */
    @RequiresScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.USER_EDIT_BROADCAST)
    suspend fun getExtensions(): List<UserExtension> {
        http.validateAnyScope(TwitchScope.USER_READ_BROADCAST, TwitchScope.USER_EDIT_BROADCAST)
        return http.get<UserExtension>("users/extensions/list").data
    }

    /**
     * [Twitch API: Get User Active Extensions](https://dev.twitch.tv/docs/api/reference/#get-user-active-extensions)
     *
     * Gets the active extensions that the broadcaster has installed for each configuration
     * slot (panel, overlay, or component). If the `user_id` query parameter is not provided,
     * the user ID in the access token identifies the broadcaster.
     *
     * @param userId the ID of the broadcaster whose active extensions you want to get. This
     * parameter is optional. If not specified, the user ID from the access token is used.
     * @return the active extensions organized by type (panel, overlay, component).
     */
    suspend fun getActiveExtensions(userId: String? = null): ActiveExtensions {
        val params =
            buildList {
                userId?.let { add("user_id" to it) }
            }
        return http.getTyped<ActiveExtensionsResponse>("users/extensions", params).data
    }

    /**
     * [Twitch API: Update User Extensions](https://dev.twitch.tv/docs/api/reference/#update-user-extensions)
     *
     * Updates the active extensions that the broadcaster has installed for each configuration
     * slot (panel, overlay, or component).
     *
     * @param extensions the extensions to activate. Specifies which extensions to activate and
     * in which slots. Slots that are not specified are disabled.
     * @return the updated active extensions.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_EXTENSIONS, TwitchScope.USER_EDIT_BROADCAST)
    suspend fun updateActiveExtensions(extensions: ActiveExtensions): ActiveExtensions {
        http.validateAnyScope(TwitchScope.CHANNEL_MANAGE_EXTENSIONS, TwitchScope.USER_EDIT_BROADCAST)
        return http
            .putTyped<ActiveExtensionsResponse>(
                "users/extensions",
                body = http.encodeBody(ActiveExtensionsResponse(extensions)),
            ).data
    }
}

@Serializable
internal data class ActiveExtensionsResponse(
    val data: ActiveExtensions,
)
