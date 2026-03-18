package io.github.captnblubber.twitchkt.eventsub.model

import kotlin.time.Instant

/**
 * [Twitch EventSub: automod.settings.update](https://dev.twitch.tv/docs/eventsub/eventsub-subscription-types/#automodsettingsupdate)
 *
 * @property broadcasterUserId the broadcaster's user ID.
 * @property broadcasterUserLogin the broadcaster's login.
 * @property broadcasterUserName the broadcaster's display name.
 * @property moderatorUserId the user ID of the moderator who changed the settings.
 * @property moderatorUserLogin the login of the moderator who changed the settings.
 * @property moderatorUserName the display name of the moderator who changed the settings.
 * @property overallLevel the overall automod level (0-4), or `null` if individual levels are set.
 * @property disability the automod filter level for disability.
 * @property aggression the automod filter level for aggression.
 * @property sexualitySexOrGender the automod filter level for sexuality, sex, or gender.
 * @property misogyny the automod filter level for misogyny.
 * @property bullying the automod filter level for bullying.
 * @property swearing the automod filter level for swearing.
 * @property raceEthnicityOrReligion the automod filter level for race, ethnicity, or religion.
 * @property sexBasedTerms the automod filter level for sex-based terms.
 */
data class AutomodSettingsUpdate(
    override val subscriptionType: String,
    override val messageId: String,
    override val timestamp: Instant,
    val broadcasterUserId: String,
    val broadcasterUserLogin: String,
    val broadcasterUserName: String,
    val moderatorUserId: String,
    val moderatorUserLogin: String,
    val moderatorUserName: String,
    val overallLevel: Int?,
    val disability: Int,
    val aggression: Int,
    val sexualitySexOrGender: Int,
    val misogyny: Int,
    val bullying: Int,
    val swearing: Int,
    val raceEthnicityOrReligion: Int,
    val sexBasedTerms: Int,
) : TwitchEvent
