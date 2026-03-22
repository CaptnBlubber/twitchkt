package io.github.captnblubber.twitchkt.helix

import io.github.captnblubber.twitchkt.TwitchKtConfig
import io.github.captnblubber.twitchkt.helix.internal.HelixHttpClient
import io.github.captnblubber.twitchkt.helix.resource.AdResource
import io.github.captnblubber.twitchkt.helix.resource.AnalyticsResource
import io.github.captnblubber.twitchkt.helix.resource.BitsResource
import io.github.captnblubber.twitchkt.helix.resource.ChannelResource
import io.github.captnblubber.twitchkt.helix.resource.CharityResource
import io.github.captnblubber.twitchkt.helix.resource.ChatResource
import io.github.captnblubber.twitchkt.helix.resource.ClipResource
import io.github.captnblubber.twitchkt.helix.resource.EventSubResource
import io.github.captnblubber.twitchkt.helix.resource.ExtensionResource
import io.github.captnblubber.twitchkt.helix.resource.FollowerResource
import io.github.captnblubber.twitchkt.helix.resource.GameResource
import io.github.captnblubber.twitchkt.helix.resource.GoalResource
import io.github.captnblubber.twitchkt.helix.resource.HypeTrainResource
import io.github.captnblubber.twitchkt.helix.resource.ModerationResource
import io.github.captnblubber.twitchkt.helix.resource.PollResource
import io.github.captnblubber.twitchkt.helix.resource.PredictionResource
import io.github.captnblubber.twitchkt.helix.resource.RaidResource
import io.github.captnblubber.twitchkt.helix.resource.RewardResource
import io.github.captnblubber.twitchkt.helix.resource.ScheduleResource
import io.github.captnblubber.twitchkt.helix.resource.SearchResource
import io.github.captnblubber.twitchkt.helix.resource.StreamResource
import io.github.captnblubber.twitchkt.helix.resource.SubscriptionResource
import io.github.captnblubber.twitchkt.helix.resource.TeamResource
import io.github.captnblubber.twitchkt.helix.resource.UserResource
import io.github.captnblubber.twitchkt.helix.resource.VideoResource
import io.github.captnblubber.twitchkt.helix.resource.WhisperResource
import io.ktor.client.HttpClient

class TwitchHelix(
    httpClient: HttpClient,
    config: TwitchKtConfig,
) {
    private val http = HelixHttpClient(httpClient, config)

    val users = UserResource(http)
    val channels = ChannelResource(http)
    val streams = StreamResource(http)
    val chat = ChatResource(http)
    val subscriptions = SubscriptionResource(http)
    val eventSub = EventSubResource(http)
    val followers = FollowerResource(http)
    val polls = PollResource(http)
    val ads = AdResource(http)
    val rewards = RewardResource(http)
    val moderation = ModerationResource(http)
    val search = SearchResource(http)
    val predictions = PredictionResource(http)
    val raids = RaidResource(http)
    val clips = ClipResource(http)
    val videos = VideoResource(http)
    val bits = BitsResource(http)
    val games = GameResource(http)
    val goals = GoalResource(http)
    val hypeTrain = HypeTrainResource(http)
    val schedule = ScheduleResource(http)
    val teams = TeamResource(http)
    val whispers = WhisperResource(http)
    val extensions = ExtensionResource(http)
    val analytics = AnalyticsResource(http)
    val charity = CharityResource(http)
}
