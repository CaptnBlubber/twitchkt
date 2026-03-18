package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: channel.poll.begin](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#channelpollbegin)
 *
 * @property id ID of the poll.
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property title question displayed for the poll.
 * @property choices the poll's choices with vote counts.
 * @property bitsVoting Bits voting settings (not used, set to disabled).
 * @property channelPointsVoting Channel Points voting settings.
 * @property startedAt the time the poll started.
 * @property endsAt the time the poll will end.
 */
data class PollBegin(
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
    val startedAt: Instant,
    val endsAt: Instant,
) : TwitchEvent
