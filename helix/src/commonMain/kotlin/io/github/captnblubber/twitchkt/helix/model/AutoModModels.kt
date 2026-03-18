package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The result of checking a message against AutoMod.
 *
 * @property msgId the caller-defined ID passed in the request.
 * @property isPermitted a Boolean value that indicates whether Twitch would approve the message for chat or hold it for moderator review or block it from chat. Is true if Twitch would approve the message; otherwise, false if Twitch would hold the message for moderator review or block it from chat.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#check-automod-status">Twitch API Reference - Check AutoMod Status</a>
 */
@Serializable
data class AutoModCheckResult(
    @SerialName("msg_id") val msgId: String,
    @SerialName("is_permitted") val isPermitted: Boolean,
)

/**
 * The broadcaster's AutoMod settings.
 *
 * @property broadcasterId the broadcaster's ID.
 * @property moderatorId the moderator's ID.
 * @property overallLevel the default AutoMod level for the broadcaster. This field is null if the broadcaster has set one or more of the individual settings.
 * @property disability the Automod level for discrimination against disability.
 * @property aggression the Automod level for hostility involving aggression.
 * @property sexualitySexOrGender the AutoMod level for discrimination based on sexuality, sex, or gender.
 * @property misogyny the Automod level for discrimination against women.
 * @property bullying the Automod level for hostility involving name calling or insults.
 * @property swearing the Automod level for profanity.
 * @property raceEthnicityOrReligion the Automod level for racial discrimination.
 * @property sexBasedTerms the Automod level for sexual content.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-automod-settings">Twitch API Reference - Get AutoMod Settings</a>
 */
@Serializable
data class AutoModSettings(
    @SerialName("broadcaster_id") val broadcasterId: String,
    @SerialName("moderator_id") val moderatorId: String,
    @SerialName("overall_level") val overallLevel: Int? = null,
    val disability: Int,
    val aggression: Int,
    @SerialName("sexuality_sex_or_gender") val sexualitySexOrGender: Int,
    val misogyny: Int,
    val bullying: Int,
    val swearing: Int,
    @SerialName("race_ethnicity_or_religion") val raceEthnicityOrReligion: Int,
    @SerialName("sex_based_terms") val sexBasedTerms: Int,
)
