package io.github.captnblubber.twitchkt.helix.resource

import io.github.captnblubber.twitchkt.auth.RequiresScope
import io.github.captnblubber.twitchkt.auth.TwitchScope
import io.github.captnblubber.twitchkt.helix.Page
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.internal.requireFirst
import io.github.captnblubber.twitchkt.helix.model.CreateRewardRequest
import io.github.captnblubber.twitchkt.helix.model.CustomReward
import io.github.captnblubber.twitchkt.helix.model.RedemptionStatus
import io.github.captnblubber.twitchkt.helix.model.RewardRedemption
import io.github.captnblubber.twitchkt.helix.model.UpdateRewardRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable

/**
 * Twitch Helix Channel Points API resource.
 *
 * Provides methods for managing custom rewards and redemptions.
 *
 * @see <a href="https://dev.twitch.tv/docs/api/reference/#get-custom-reward">Twitch API Reference - Channel Points</a>
 */
class RewardResource internal constructor(
    private val http: HelixHttpClient,
) {
    /**
     * [Twitch API: Get Custom Reward](https://dev.twitch.tv/docs/api/reference/#get-custom-reward)
     *
     * Gets a list of custom rewards that the specified broadcaster created. A channel may offer
     * a maximum of 50 rewards, which includes both enabled and disabled rewards.
     *
     * @param broadcasterId the ID of the broadcaster whose custom rewards you want to get.
     * This ID must match the user ID found in the OAuth token.
     * @param ids a list of IDs to filter the rewards by. You may specify a maximum of 50 IDs.
     * @param onlyManageableRewards a Boolean value that determines whether the response contains
     * only the custom rewards that the app may manage (the app is identified by the ID in the
     * Client-Id header). Set to `true` to get only the custom rewards that the app may manage.
     * The default is `false`.
     * @return the list of custom rewards, in ascending order by id.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_REDEMPTIONS, TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun list(
        broadcasterId: String,
        ids: List<String> = emptyList(),
        onlyManageableRewards: Boolean = false,
    ): List<CustomReward> {
        http.validateAnyScope(TwitchScope.CHANNEL_READ_REDEMPTIONS, TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                ids.forEach { add("id" to it) }
                if (onlyManageableRewards) add("only_manageable_rewards" to "true")
            }
        return http.get<CustomReward>("channel_points/custom_rewards", params).data
    }

    /**
     * [Twitch API: Create Custom Rewards](https://dev.twitch.tv/docs/api/reference/#create-custom-rewards)
     *
     * Creates a Custom Reward in the broadcaster's channel. The maximum number of custom rewards
     * per channel is 50, which includes both enabled and disabled rewards.
     *
     * @param broadcasterId the ID of the broadcaster to add the custom reward to. This ID must
     * match the user ID found in the OAuth token.
     * @param request the reward configuration.
     * @return the created custom reward.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun create(
        broadcasterId: String,
        request: CreateRewardRequest,
    ): CustomReward {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
        val params = listOf("broadcaster_id" to broadcasterId)
        return http.post<CustomReward>("channel_points/custom_rewards", body = http.encodeBody(request), params = params).requireFirst("channel_points/custom_rewards")
    }

    /**
     * [Twitch API: Update Custom Reward](https://dev.twitch.tv/docs/api/reference/#update-custom-reward)
     *
     * Updates a custom reward. The app used to create the reward is the only app that may update
     * the reward.
     *
     * @param broadcasterId the ID of the broadcaster that's updating the reward. This ID must
     * match the user ID found in the OAuth token.
     * @param rewardId the ID of the reward to update.
     * @param request the fields to update. The body should contain only the fields you're updating.
     * @return the updated custom reward.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun update(
        broadcasterId: String,
        rewardId: String,
        request: UpdateRewardRequest,
    ): CustomReward {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
        val params = listOf("broadcaster_id" to broadcasterId, "id" to rewardId)
        return http.patch<CustomReward>("channel_points/custom_rewards", body = http.encodeBody(request), params = params).requireFirst("channel_points/custom_rewards")
    }

    /**
     * [Twitch API: Delete Custom Reward](https://dev.twitch.tv/docs/api/reference/#delete-custom-reward)
     *
     * Deletes a custom reward that the broadcaster created. The app used to create the reward is
     * the only app that may delete it. If the reward's redemption status is UNFULFILLED at the time
     * the reward is deleted, its redemption status is marked as FULFILLED.
     *
     * @param broadcasterId the ID of the broadcaster that created the custom reward. This ID must
     * match the user ID found in the OAuth token.
     * @param id the ID of the custom reward to delete.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun delete(
        broadcasterId: String,
        id: String,
    ) {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
        val params = listOf("broadcaster_id" to broadcasterId, "id" to id)
        http.deleteNoContent("channel_points/custom_rewards", params)
    }

    /**
     * [Twitch API: Get Custom Reward Redemption](https://dev.twitch.tv/docs/api/reference/#get-custom-reward-redemption)
     *
     * Gets all redemptions for the specified reward.
     * Automatically paginates through all results.
     *
     * @param broadcasterId the ID of the broadcaster that owns the custom reward. This ID must match the user ID found in the OAuth token.
     * @param rewardId the ID that identifies the custom reward whose redemptions you want to get.
     * @param status filters the list by redemption status. Possible values: `CANCELED`, `FULFILLED`, `UNFULFILLED`.
     * @param ids a list of IDs to filter the redemptions by (max 50).
     * @param sort the order to sort redemptions by. Possible values: `OLDEST`, `NEWEST`. Default: `OLDEST`.
     * @return a [Flow] of [RewardRedemption] objects.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_REDEMPTIONS, TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    fun getAllRedemptions(
        broadcasterId: String,
        rewardId: String,
        status: String? = null,
        ids: List<String> = emptyList(),
        sort: String? = null,
    ): Flow<RewardRedemption> {
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                add("reward_id" to rewardId)
                status?.let { add("status" to it) }
                ids.forEach { add("id" to it) }
                sort?.let { add("sort" to it) }
            }
        return http
            .paginate<RewardRedemption>("channel_points/custom_rewards/redemptions", params)
            .onStart { http.validateAnyScope(TwitchScope.CHANNEL_READ_REDEMPTIONS, TwitchScope.CHANNEL_MANAGE_REDEMPTIONS) }
    }

    /**
     * [Twitch API: Get Custom Reward Redemption](https://dev.twitch.tv/docs/api/reference/#get-custom-reward-redemption)
     *
     * Gets a single page of redemptions for the specified reward.
     *
     * @param broadcasterId the ID of the broadcaster that owns the custom reward. This ID must match the user ID found in the OAuth token.
     * @param rewardId the ID that identifies the custom reward whose redemptions you want to get.
     * @param status filters the list by redemption status. Possible values: `CANCELED`, `FULFILLED`, `UNFULFILLED`.
     * @param ids a list of IDs to filter the redemptions by (max 50).
     * @param sort the order to sort redemptions by. Possible values: `OLDEST`, `NEWEST`. Default: `OLDEST`.
     * @param cursor the cursor used to get the next page of results.
     * @param pageSize the maximum number of redemptions to return per page (1-50, default 20). Null uses the API default.
     * @return a [Page] of [RewardRedemption] objects.
     */
    @RequiresScope(TwitchScope.CHANNEL_READ_REDEMPTIONS, TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun getRedemptions(
        broadcasterId: String,
        rewardId: String,
        status: String? = null,
        ids: List<String> = emptyList(),
        sort: String? = null,
        cursor: String? = null,
        pageSize: Int? = null,
    ): Page<RewardRedemption> {
        http.validateAnyScope(TwitchScope.CHANNEL_READ_REDEMPTIONS, TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
        val params =
            buildList {
                add("broadcaster_id" to broadcasterId)
                add("reward_id" to rewardId)
                status?.let { add("status" to it) }
                ids.forEach { add("id" to it) }
                sort?.let { add("sort" to it) }
                cursor?.let { add("after" to it) }
            }
        return http.getPage(endpoint = "channel_points/custom_rewards/redemptions", params = params, pageSize = pageSize)
    }

    /**
     * [Twitch API: Update Redemption Status](https://dev.twitch.tv/docs/api/reference/#update-redemption-status)
     *
     * Updates a redemption's status. You may update a redemption only if its status is UNFULFILLED.
     * The app used to create the reward is the only app that may update the redemption.
     *
     * @param broadcasterId the ID of the broadcaster that's updating the redemption. This ID must
     * match the user ID in the user access token.
     * @param rewardId the ID that identifies the reward that's been redeemed.
     * @param redemptionIds a list of IDs that identify the redemptions to update. You may specify
     * a maximum of 50 IDs.
     * @param status the status to set the redemption to. Possible values are: `CANCELED`, `FULFILLED`.
     * Setting the status to CANCELED refunds the user's channel points.
     * @return the list of updated redemptions.
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun updateRedemptionStatus(
        broadcasterId: String,
        rewardId: String,
        redemptionIds: List<String>,
        status: RedemptionStatus,
    ): List<RewardRedemption> {
        http.validateScopes(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
        val params =
            buildList {
                redemptionIds.forEach { add("id" to it) }
                add("broadcaster_id" to broadcasterId)
                add("reward_id" to rewardId)
            }
        val body = http.encodeBody(UpdateRedemptionStatusRequest(status = status))
        return http.patch<RewardRedemption>("channel_points/custom_rewards/redemptions", body = body, params = params).data
    }

    /**
     * Convenience overload that updates a single redemption's status.
     *
     * @see [updateRedemptionStatus]
     */
    @RequiresScope(TwitchScope.CHANNEL_MANAGE_REDEMPTIONS)
    suspend fun updateRedemptionStatus(
        broadcasterId: String,
        rewardId: String,
        redemptionId: String,
        status: RedemptionStatus,
    ): List<RewardRedemption> =
        updateRedemptionStatus(
            broadcasterId = broadcasterId,
            rewardId = rewardId,
            redemptionIds = listOf(redemptionId),
            status = status,
        )
}

@Serializable
internal data class UpdateRedemptionStatusRequest(
    val status: RedemptionStatus,
)
