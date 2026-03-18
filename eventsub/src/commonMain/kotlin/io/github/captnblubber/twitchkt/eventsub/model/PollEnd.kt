package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.poll.end](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpollend)
 *
 * @property id ID of the poll.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property title question displayed for the poll.
 * @property choices the poll's choices with final vote totals.
 * @property bitsVoting Bits voting settings (not used, set to disabled).
 * @property channelPointsVoting Channel Points voting settings.
 * @property status the poll's final status: `completed`, `archived`, or `terminated`.
 * @property startedAt the time the poll started.
 * @property endedAt the time the poll ended.
 */
data class PollEnd(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val id: String,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val title: String,
    val choices: List<PollChoice>,
    val bitsVoting: VotingSettings,
    val channelPointsVoting: VotingSettings,
    val status: String,
    val startedAt: Instant,
    val endedAt: Instant,
) : TwitchEvent
