package io.github.captnblubber.twitchkt.eventsub.model

import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.moderate](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelmoderate)
 *
 * A moderation action was performed in a channel. The [action] field discriminates the type
 * and the corresponding detail object (e.g. [ban], [timeout], [unban]) will be non-null.
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who performed the action.
 * @property moderatorUserLogin the login of the moderator who performed the action.
 * @property moderatorUserName the display name of the moderator who performed the action.
 * @property action the moderation action that was performed (e.g. `ban`, `timeout`, `delete`, `clear`).
 * @property followers details if [action] is `followers`.
 * @property slow details if [action] is `slow`.
 * @property vip details if [action] is `vip` or `unvip`.
 * @property unvip details if [action] is `unvip`.
 * @property mod details if [action] is `mod` or `unmod`.
 * @property unmod details if [action] is `unmod`.
 * @property ban details if [action] is `ban`.
 * @property unban details if [action] is `unban`.
 * @property timeout details if [action] is `timeout`.
 * @property untimeout details if [action] is `untimeout`.
 * @property raid details if [action] is `raid`.
 * @property unraid details if [action] is `unraid`.
 * @property delete details if [action] is `delete`.
 * @property automodTerms details if [action] is `add_blocked_term` or `delete_blocked_term`.
 * @property unmute details if [action] is `approve_unban_request` or `deny_unban_request`.
 * @property warn details if [action] is `warn`.
 * @property shared details if [action] is applicable to shared chat.
 */
data class ChannelModerate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val action: String,
    val followers: ModFollowers? = null,
    val slow: ModSlow? = null,
    val vip: ModTargetUser? = null,
    val unvip: ModTargetUser? = null,
    val mod: ModTargetUser? = null,
    val unmod: ModTargetUser? = null,
    val ban: ModBan? = null,
    val unban: ModTargetUser? = null,
    val timeout: ModTimeout? = null,
    val untimeout: ModTargetUser? = null,
    val raid: ModRaid? = null,
    val unraid: ModTargetUser? = null,
    val delete: ModDelete? = null,
    val automodTerms: ModAutomodTerms? = null,
    val unmute: ModTargetUser? = null,
    val warn: ModWarn? = null,
    val shared: JsonObject? = null,
) : TwitchEvent
