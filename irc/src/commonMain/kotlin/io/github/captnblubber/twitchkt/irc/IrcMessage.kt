@file:Suppress("DEPRECATION")

package io.github.captnblubber.twitchkt.irc

import io.github.captnblubber.twitchkt.model.common.SubTier

/**
 * Sealed hierarchy of parsed Twitch IRC messages.
 *
 * Twitch IRC is deprecated in favor of EventSub. This model exists solely because EventSub
 * does not yet cover all event types (e.g. watch streaks / viewer milestones).
 * Once Twitch adds full EventSub coverage, this module will be removed.
 *
 * See: [Twitch IRC Migration Guide](https://dev.twitch.tv/docs/chat/irc-migration/)
 */
@Deprecated(
    "Twitch IRC is deprecated. Prefer EventSub for real-time events. " +
        "IRC remains only for event types not yet available via EventSub (e.g. watch streaks). " +
        "See: https://dev.twitch.tv/docs/chat/irc-migration/",
)
sealed interface IrcMessage {
    /** Channel name without the `#` prefix. */
    val channel: String

    /** Raw IRCv3 tags for direct access when typed fields are insufficient. */
    val tags: Map<String, String>

    /** A standard chat message (PRIVMSG). */
    data class PrivMsg(
        override val channel: String,
        override val tags: Map<String, String>,
        val userId: String,
        val userLogin: String,
        val displayName: String,
        val message: String,
    ) : IrcMessage

    /**
     * A USERNOTICE message dispatched by Twitch for subscriptions, raids, watch streaks, and
     * other channel events. Subclasses represent specific `msg-id` values.
     */
    sealed class UserNotice(
        override val channel: String,
        override val tags: Map<String, String>,
        val userId: String,
        val userLogin: String,
        val displayName: String,
        val systemMessage: String,
    ) : IrcMessage {
        /** Viewer milestone: watch streak (`msg-id=viewer-milestone`, `msg-param-category=watch-streak`). */
        class WatchStreak(
            channel: String,
            tags: Map<String, String>,
            userId: String,
            userLogin: String,
            displayName: String,
            systemMessage: String,
            val streakMonths: Int,
            val userMessage: String?,
        ) : UserNotice(channel, tags, userId, userLogin, displayName, systemMessage)

        /** New subscription (`msg-id=sub`). */
        class Sub(
            channel: String,
            tags: Map<String, String>,
            userId: String,
            userLogin: String,
            displayName: String,
            systemMessage: String,
            val tier: SubTier,
            val isGift: Boolean,
        ) : UserNotice(channel, tags, userId, userLogin, displayName, systemMessage)

        /** Resubscription (`msg-id=resub`). */
        class Resub(
            channel: String,
            tags: Map<String, String>,
            userId: String,
            userLogin: String,
            displayName: String,
            systemMessage: String,
            val tier: SubTier,
            val cumulativeMonths: Int,
            val streakMonths: Int?,
            val userMessage: String?,
        ) : UserNotice(channel, tags, userId, userLogin, displayName, systemMessage)

        /** Gifted subscription (`msg-id=subgift`). */
        class SubGift(
            channel: String,
            tags: Map<String, String>,
            userId: String,
            userLogin: String,
            displayName: String,
            systemMessage: String,
            val tier: SubTier,
            val recipientLogin: String,
            val recipientDisplayName: String,
        ) : UserNotice(channel, tags, userId, userLogin, displayName, systemMessage)

        /** Incoming raid (`msg-id=raid`). */
        class Raid(
            channel: String,
            tags: Map<String, String>,
            userId: String,
            userLogin: String,
            displayName: String,
            systemMessage: String,
            val viewerCount: Int,
        ) : UserNotice(channel, tags, userId, userLogin, displayName, systemMessage)

        /** Catch-all for unrecognized `msg-id` values. */
        class Unknown(
            channel: String,
            tags: Map<String, String>,
            userId: String,
            userLogin: String,
            displayName: String,
            systemMessage: String,
            val msgId: String,
        ) : UserNotice(channel, tags, userId, userLogin, displayName, systemMessage)
    }

    /** Room state change (ROOMSTATE). Fields are nullable because Twitch sends partial updates. */
    data class RoomState(
        override val channel: String,
        override val tags: Map<String, String>,
        val emoteOnly: Boolean?,
        val followersOnly: Int?,
        val slow: Int?,
        val subsOnly: Boolean?,
    ) : IrcMessage

    /** Chat clear or user ban/timeout (CLEARCHAT). */
    data class ClearChat(
        override val channel: String,
        override val tags: Map<String, String>,
        val targetUserId: String?,
        val duration: Int?,
    ) : IrcMessage

    /** Single message deletion (CLEARMSG). */
    data class ClearMsg(
        override val channel: String,
        override val tags: Map<String, String>,
        val targetMessageId: String,
        val login: String,
    ) : IrcMessage

    /** Server notice (NOTICE). */
    data class Notice(
        override val channel: String,
        override val tags: Map<String, String>,
        val msgId: String?,
        val message: String,
    ) : IrcMessage
}
