@file:Suppress("DEPRECATION")

package io.github.captnblubber.twitchkt.irc.internal

import io.github.captnblubber.twitchkt.irc.IrcMessage
import io.github.captnblubber.twitchkt.model.common.SubTier

/**
 * Pure parser converting raw Twitch IRC lines into typed [IrcMessage] instances.
 *
 * Returns `null` for lines that don't map to a modeled message type (PING, numeric replies,
 * CAP ACK, etc.) — those are handled at the connection layer.
 */
internal class IrcParser {
    fun parse(line: String): IrcMessage? {
        val raw = tokenize(line)
        val channel = raw.channel?.removePrefix("#") ?: return null

        return when (raw.command) {
            "PRIVMSG" -> parsePrivMsg(raw, channel)
            "USERNOTICE" -> parseUserNotice(raw, channel)
            "ROOMSTATE" -> parseRoomState(raw, channel)
            "CLEARCHAT" -> parseClearChat(raw, channel)
            "CLEARMSG" -> parseClearMsg(raw, channel)
            "NOTICE" -> parseNotice(raw, channel)
            else -> null
        }
    }

    private fun tokenize(line: String): RawIrcLine {
        var remaining = line
        val tags =
            if (remaining.startsWith("@")) {
                val spaceIdx = remaining.indexOf(' ')
                val tagBlock = remaining.substring(1, spaceIdx)
                remaining = remaining.substring(spaceIdx + 1)
                parseTags(tagBlock)
            } else {
                emptyMap()
            }

        val prefix =
            if (remaining.startsWith(":")) {
                val spaceIdx = remaining.indexOf(' ')
                val p = remaining.substring(1, spaceIdx)
                remaining = remaining.substring(spaceIdx + 1)
                p
            } else {
                null
            }

        val trailingIdx = remaining.indexOf(" :")
        val trailing =
            if (trailingIdx != -1) {
                val t = remaining.substring(trailingIdx + 2)
                remaining = remaining.substring(0, trailingIdx)
                t
            } else {
                null
            }

        val parts = remaining.split(' ').filter { it.isNotEmpty() }
        val command = parts.firstOrNull() ?: ""
        val params = parts.drop(1)
        val channel = params.firstOrNull { it.startsWith("#") }

        return RawIrcLine(tags, prefix, command, channel, params, trailing)
    }

    private fun parseTags(block: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (pair in block.split(';')) {
            val eqIdx = pair.indexOf('=')
            if (eqIdx == -1) {
                map[pair] = ""
            } else {
                map[pair.substring(0, eqIdx)] = unescapeTagValue(pair.substring(eqIdx + 1))
            }
        }
        return map
    }

    private fun unescapeTagValue(value: String): String {
        if ('\\' !in value) return value
        val sb = StringBuilder(value.length)
        var i = 0
        while (i < value.length) {
            if (value[i] == '\\' && i + 1 < value.length) {
                when (value[i + 1]) {
                    ':' -> sb.append(';')
                    's' -> sb.append(' ')
                    '\\' -> sb.append('\\')
                    'r' -> sb.append('\r')
                    'n' -> sb.append('\n')
                    else -> sb.append(value[i + 1])
                }
                i += 2
            } else {
                sb.append(value[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun parsePrivMsg(
        raw: RawIrcLine,
        channel: String,
    ): IrcMessage.PrivMsg {
        val login = raw.prefix?.substringBefore('!') ?: ""
        return IrcMessage.PrivMsg(
            channel = channel,
            tags = raw.tags,
            userId = raw.tags["user-id"] ?: "",
            userLogin = login,
            displayName = raw.tags["display-name"] ?: login,
            message = raw.trailing ?: "",
        )
    }

    private fun parseUserNotice(
        raw: RawIrcLine,
        channel: String,
    ): IrcMessage.UserNotice {
        val login = raw.tags["login"] ?: raw.prefix?.substringBefore('!') ?: ""
        val userId = raw.tags["user-id"] ?: ""
        val displayName = raw.tags["display-name"] ?: login
        val systemMessage = raw.tags["system-msg"] ?: ""
        val msgId = raw.tags["msg-id"] ?: ""

        return when (msgId) {
            "sub" -> {
                IrcMessage.UserNotice.Sub(
                    channel = channel,
                    tags = raw.tags,
                    userId = userId,
                    userLogin = login,
                    displayName = displayName,
                    systemMessage = systemMessage,
                    tier = parseSubTier(raw.tags["msg-param-sub-plan"]),
                    isGift = raw.tags["msg-param-was-gifted"] == "true",
                )
            }

            "resub" -> {
                IrcMessage.UserNotice.Resub(
                    channel = channel,
                    tags = raw.tags,
                    userId = userId,
                    userLogin = login,
                    displayName = displayName,
                    systemMessage = systemMessage,
                    tier = parseSubTier(raw.tags["msg-param-sub-plan"]),
                    cumulativeMonths = raw.tags["msg-param-cumulative-months"]?.toIntOrNull() ?: 0,
                    streakMonths = raw.tags["msg-param-streak-months"]?.toIntOrNull(),
                    userMessage = raw.trailing,
                )
            }

            "subgift" -> {
                IrcMessage.UserNotice.SubGift(
                    channel = channel,
                    tags = raw.tags,
                    userId = userId,
                    userLogin = login,
                    displayName = displayName,
                    systemMessage = systemMessage,
                    tier = parseSubTier(raw.tags["msg-param-sub-plan"]),
                    recipientLogin = raw.tags["msg-param-recipient-user-name"] ?: "",
                    recipientDisplayName = raw.tags["msg-param-recipient-display-name"] ?: "",
                )
            }

            "raid" -> {
                IrcMessage.UserNotice.Raid(
                    channel = channel,
                    tags = raw.tags,
                    userId = userId,
                    userLogin = login,
                    displayName = displayName,
                    systemMessage = systemMessage,
                    viewerCount = raw.tags["msg-param-viewerCount"]?.toIntOrNull() ?: 0,
                )
            }

            "viewer-milestone" -> {
                if (raw.tags["msg-param-category"] == "watch-streak") {
                    IrcMessage.UserNotice.WatchStreak(
                        channel = channel,
                        tags = raw.tags,
                        userId = userId,
                        userLogin = login,
                        displayName = displayName,
                        systemMessage = systemMessage,
                        streakMonths = raw.tags["msg-param-value"]?.toIntOrNull() ?: 0,
                        userMessage = raw.trailing,
                    )
                } else {
                    IrcMessage.UserNotice.Unknown(
                        channel = channel,
                        tags = raw.tags,
                        userId = userId,
                        userLogin = login,
                        displayName = displayName,
                        systemMessage = systemMessage,
                        msgId = msgId,
                    )
                }
            }

            else -> {
                IrcMessage.UserNotice.Unknown(
                    channel = channel,
                    tags = raw.tags,
                    userId = userId,
                    userLogin = login,
                    displayName = displayName,
                    systemMessage = systemMessage,
                    msgId = msgId,
                )
            }
        }
    }

    private fun parseRoomState(
        raw: RawIrcLine,
        channel: String,
    ): IrcMessage.RoomState =
        IrcMessage.RoomState(
            channel = channel,
            tags = raw.tags,
            emoteOnly = raw.tags["emote-only"]?.let { it == "1" },
            followersOnly = raw.tags["followers-only"]?.toIntOrNull(),
            slow = raw.tags["slow"]?.toIntOrNull(),
            subsOnly = raw.tags["subs-only"]?.let { it == "1" },
        )

    private fun parseClearChat(
        raw: RawIrcLine,
        channel: String,
    ): IrcMessage.ClearChat =
        IrcMessage.ClearChat(
            channel = channel,
            tags = raw.tags,
            targetUserId = raw.tags["target-user-id"],
            duration = raw.tags["ban-duration"]?.toIntOrNull(),
        )

    private fun parseClearMsg(
        raw: RawIrcLine,
        channel: String,
    ): IrcMessage.ClearMsg =
        IrcMessage.ClearMsg(
            channel = channel,
            tags = raw.tags,
            targetMessageId = raw.tags["target-msg-id"] ?: "",
            login = raw.tags["login"] ?: "",
        )

    private fun parseNotice(
        raw: RawIrcLine,
        channel: String,
    ): IrcMessage.Notice =
        IrcMessage.Notice(
            channel = channel,
            tags = raw.tags,
            msgId = raw.tags["msg-id"],
            message = raw.trailing ?: "",
        )

    private fun parseSubTier(plan: String?): SubTier =
        when (plan) {
            "2000" -> SubTier.TIER_2
            "3000" -> SubTier.TIER_3
            "Prime" -> SubTier.PRIME
            else -> SubTier.TIER_1
        }
}

private data class RawIrcLine(
    val tags: Map<String, String>,
    val prefix: String?,
    val command: String,
    val channel: String?,
    val params: List<String>,
    val trailing: String?,
)
