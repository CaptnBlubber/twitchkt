package io.github.captnblubber.twitchkt.helix.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an extension that a user has installed.
 *
 * @property id the ID of the extension.
 * @property version the version of the extension.
 * @property name the extension's name.
 * @property canActivate a Boolean value that determines whether the extension is configured and can be activated.
 * @property type a list that contains the extension type. Possible values are: `component`, `mobile`, `overlay`, `panel`.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-user-extensions">Twitch API Reference - Get User Extensions</a>
 */
@Serializable
data class UserExtension(
    val id: String,
    val version: String,
    val name: String,
    @SerialName("can_activate") val canActivate: Boolean,
    val type: List<String>,
)

/**
 * Represents an active extension in a specific slot.
 *
 * @property active a Boolean value that determines whether the extension is active.
 * @property id the ID of the extension. Is `null` if the extension is not active.
 * @property version the version of the extension. Is `null` if the extension is not active.
 * @property name the name of the extension. Is `null` if the extension is not active.
 * @property x the x-coordinate of the extension. Only set for component extensions. Is `null` otherwise.
 * @property y the y-coordinate of the extension. Only set for component extensions. Is `null` otherwise.
 */
@Serializable
data class ActiveExtensionSlot(
    val active: Boolean,
    val id: String? = null,
    val version: String? = null,
    val name: String? = null,
    val x: Int? = null,
    val y: Int? = null,
)

/**
 * Represents a user's active extensions, organized by type.
 *
 * Each type (panel, overlay, component) is a map from slot number (as a string key like "1", "2", "3")
 * to the extension configuration in that slot.
 *
 * @property panel the panel extensions. Panels are displayed below the video player. A maximum of 3 panels are supported.
 * @property overlay the overlay extensions. Overlays are displayed on top of the video player. A maximum of 1 overlay is supported.
 * @property component the component extensions. Components are displayed within the video player. A maximum of 2 components are supported.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-user-active-extensions">Twitch API Reference - Get User Active Extensions</a>
 */
@Serializable
data class ActiveExtensions(
    val panel: Map<String, ActiveExtensionSlot> = emptyMap(),
    val overlay: Map<String, ActiveExtensionSlot> = emptyMap(),
    val component: Map<String, ActiveExtensionSlot> = emptyMap(),
)
