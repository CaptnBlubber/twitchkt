package io.github.captnblubber.twitchkt.auth

/**
 * Twitch OAuth2 scopes.
 *
 * Twitch uses a hierarchical scope model where `manage` scopes implicitly grant the
 * corresponding `read` permission. For example, a token with `channel:manage:polls`
 * satisfies endpoints that require `channel:read:polls`. This hierarchy is encoded via
 * [implies] — the set of scopes that this scope implicitly grants.
 *
 * See: [Twitch OAuth Scopes](https://dev.twitch.tv/docs/authentication/scopes/)
 */
enum class TwitchScope(
    val value: String,
) {
    // Analytics
    ANALYTICS_READ_EXTENSIONS("analytics:read:extensions"),
    ANALYTICS_READ_GAMES("analytics:read:games"),

    // Bits
    BITS_READ("bits:read"),

    // Channel
    CHANNEL_BOT("channel:bot"),
    CHANNEL_MANAGE_ADS("channel:manage:ads"),
    CHANNEL_READ_ADS("channel:read:ads"),
    CHANNEL_MANAGE_BROADCAST("channel:manage:broadcast"),
    CHANNEL_READ_CHARITY("channel:read:charity"),
    CHANNEL_EDIT_COMMERCIAL("channel:edit:commercial"),
    CHANNEL_READ_EDITORS("channel:read:editors"),
    CHANNEL_MANAGE_EXTENSIONS("channel:manage:extensions"),
    CHANNEL_READ_GOALS("channel:read:goals"),
    CHANNEL_READ_GUEST_STAR("channel:read:guest_star"),
    CHANNEL_MANAGE_GUEST_STAR("channel:manage:guest_star"),
    CHANNEL_READ_HYPE_TRAIN("channel:read:hype_train"),
    CHANNEL_MANAGE_MODERATORS("channel:manage:moderators"),
    CHANNEL_READ_POLLS("channel:read:polls"),
    CHANNEL_MANAGE_POLLS("channel:manage:polls"),
    CHANNEL_READ_PREDICTIONS("channel:read:predictions"),
    CHANNEL_MANAGE_PREDICTIONS("channel:manage:predictions"),
    CHANNEL_MANAGE_RAIDS("channel:manage:raids"),
    CHANNEL_READ_REDEMPTIONS("channel:read:redemptions"),
    CHANNEL_MANAGE_REDEMPTIONS("channel:manage:redemptions"),
    CHANNEL_MANAGE_SCHEDULE("channel:manage:schedule"),
    CHANNEL_READ_STREAM_KEY("channel:read:stream_key"),
    CHANNEL_READ_SUBSCRIPTIONS("channel:read:subscriptions"),
    CHANNEL_MANAGE_VIDEOS("channel:manage:videos"),
    CHANNEL_READ_VIPS("channel:read:vips"),
    CHANNEL_MANAGE_VIPS("channel:manage:vips"),
    CHANNEL_MODERATE("channel:moderate"),

    // Chat
    CHAT_EDIT("chat:edit"),
    CHAT_READ("chat:read"),

    // Clips
    CLIPS_EDIT("clips:edit"),
    CHANNEL_MANAGE_CLIPS("channel:manage:clips"),
    EDITOR_MANAGE_CLIPS("editor:manage:clips"),

    // Moderation
    MODERATION_READ("moderation:read"),
    MODERATOR_MANAGE_ANNOUNCEMENTS("moderator:manage:announcements"),
    MODERATOR_MANAGE_AUTOMOD("moderator:manage:automod"),
    MODERATOR_READ_AUTOMOD_SETTINGS("moderator:read:automod_settings"),
    MODERATOR_MANAGE_AUTOMOD_SETTINGS("moderator:manage:automod_settings"),
    MODERATOR_MANAGE_BANNED_USERS("moderator:manage:banned_users"),
    MODERATOR_READ_BLOCKED_TERMS("moderator:read:blocked_terms"),
    MODERATOR_MANAGE_BLOCKED_TERMS("moderator:manage:blocked_terms"),
    MODERATOR_MANAGE_CHAT_MESSAGES("moderator:manage:chat_messages"),
    MODERATOR_READ_CHAT_SETTINGS("moderator:read:chat_settings"),
    MODERATOR_MANAGE_CHAT_SETTINGS("moderator:manage:chat_settings"),
    MODERATOR_READ_CHATTERS("moderator:read:chatters"),
    MODERATOR_READ_FOLLOWERS("moderator:read:followers"),
    MODERATOR_READ_GUEST_STAR("moderator:read:guest_star"),
    MODERATOR_MANAGE_GUEST_STAR("moderator:manage:guest_star"),
    MODERATOR_READ_SHIELD_MODE("moderator:read:shield_mode"),
    MODERATOR_MANAGE_SHIELD_MODE("moderator:manage:shield_mode"),
    MODERATOR_READ_SHOUTOUTS("moderator:read:shoutouts"),
    MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts"),
    MODERATOR_READ_UNBAN_REQUESTS("moderator:read:unban_requests"),
    MODERATOR_MANAGE_UNBAN_REQUESTS("moderator:manage:unban_requests"),
    MODERATOR_READ_WARNINGS("moderator:read:warnings"),
    MODERATOR_MANAGE_WARNINGS("moderator:manage:warnings"),
    MODERATOR_MANAGE_SUSPICIOUS_USERS("moderator:manage:suspicious_users"),

    // User
    USER_BOT("user:bot"),
    USER_EDIT("user:edit"),
    USER_EDIT_BROADCAST("user:edit:broadcast"),
    USER_READ_BLOCKED_USERS("user:read:blocked_users"),
    USER_MANAGE_BLOCKED_USERS("user:manage:blocked_users"),
    USER_READ_BROADCAST("user:read:broadcast"),
    USER_READ_CHAT("user:read:chat"),
    USER_MANAGE_CHAT_COLOR("user:manage:chat_color"),
    USER_READ_EMAIL("user:read:email"),
    USER_READ_EMOTES("user:read:emotes"),
    USER_READ_FOLLOWS("user:read:follows"),
    USER_READ_MODERATED_CHANNELS("user:read:moderated_channels"),
    USER_READ_SUBSCRIPTIONS("user:read:subscriptions"),
    USER_READ_WHISPERS("user:read:whispers"),
    USER_MANAGE_WHISPERS("user:manage:whispers"),
    USER_WRITE_CHAT("user:write:chat"),
    ;

    /**
     * Scopes that this scope implicitly grants. A `manage` scope implies the corresponding
     * `read` scope. For example, `channel:manage:polls` implies `channel:read:polls`.
     *
     * Special case: `channel:manage:moderators` implies `moderation:read` as documented
     * by the Twitch API.
     */
    val implies: Set<TwitchScope> by lazy { SCOPE_HIERARCHY[this] ?: emptySet() }

    companion object {
        fun fromValue(value: String): TwitchScope? = entries.find { it.value == value }

        /**
         * Checks whether the [granted] scopes satisfy the [required] scope, taking into
         * account the scope hierarchy (manage implies read).
         */
        fun isSatisfied(
            required: TwitchScope,
            granted: Set<TwitchScope>,
        ): Boolean = required in granted || granted.any { required in it.implies }

        private val SCOPE_HIERARCHY: Map<TwitchScope, Set<TwitchScope>> =
            mapOf(
                // channel:manage:* implies channel:read:*
                CHANNEL_MANAGE_ADS to setOf(CHANNEL_READ_ADS),
                CHANNEL_MANAGE_GUEST_STAR to setOf(CHANNEL_READ_GUEST_STAR),
                CHANNEL_MANAGE_POLLS to setOf(CHANNEL_READ_POLLS),
                CHANNEL_MANAGE_PREDICTIONS to setOf(CHANNEL_READ_PREDICTIONS),
                CHANNEL_MANAGE_REDEMPTIONS to setOf(CHANNEL_READ_REDEMPTIONS),
                CHANNEL_MANAGE_VIPS to setOf(CHANNEL_READ_VIPS),
                // channel:manage:moderators implies moderation:read (Twitch special case)
                CHANNEL_MANAGE_MODERATORS to setOf(MODERATION_READ),
                // moderator:manage:* implies moderator:read:*
                MODERATOR_MANAGE_AUTOMOD_SETTINGS to setOf(MODERATOR_READ_AUTOMOD_SETTINGS),
                MODERATOR_MANAGE_BLOCKED_TERMS to setOf(MODERATOR_READ_BLOCKED_TERMS),
                MODERATOR_MANAGE_CHAT_SETTINGS to setOf(MODERATOR_READ_CHAT_SETTINGS),
                MODERATOR_MANAGE_GUEST_STAR to setOf(MODERATOR_READ_GUEST_STAR),
                MODERATOR_MANAGE_SHIELD_MODE to setOf(MODERATOR_READ_SHIELD_MODE),
                MODERATOR_MANAGE_SHOUTOUTS to setOf(MODERATOR_READ_SHOUTOUTS),
                MODERATOR_MANAGE_UNBAN_REQUESTS to setOf(MODERATOR_READ_UNBAN_REQUESTS),
                MODERATOR_MANAGE_WARNINGS to setOf(MODERATOR_READ_WARNINGS),
                // user:manage:* implies user:read:*
                USER_MANAGE_BLOCKED_USERS to setOf(USER_READ_BLOCKED_USERS),
                USER_MANAGE_WHISPERS to setOf(USER_READ_WHISPERS),
            )
    }
}
