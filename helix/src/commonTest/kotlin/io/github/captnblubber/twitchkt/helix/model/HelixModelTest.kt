package io.github.captnblubber.twitchkt.helix.model

import io.github.captnblubber.twitchkt.model.common.PollStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class HelixModelTest :
    BehaviorSpec({

        val json = Json { ignoreUnknownKeys = true }

        Given("a User JSON from Twitch API") {

            When("deserializing a complete user response") {
                val userJson =
                    """
                    {
                        "id": "141981764",
                        "login": "twitchdev",
                        "display_name": "TwitchDev",
                        "type": "",
                        "broadcaster_type": "partner",
                        "description": "Supporting third-party developers building Twitch integrations.",
                        "profile_image_url": "https://static-cdn.jtvnw.net/jtv_user_pictures/8a6381c7-d0c0-4576-b179-38bd5ce1d6af-profile_image-300x300.png",
                        "offline_image_url": "https://static-cdn.jtvnw.net/jtv_user_pictures/3f13ab61-ec78-4fe6-8481-8682cb3b0ac2-channel_offline_image-1920x1080.png",
                        "created_at": "2026-01-15T20:32:28Z"
                    }
                    """.trimIndent()

                val user = json.decodeFromString<User>(userJson)

                Then("it should decode the id") {
                    user.id shouldBe "141981764"
                }

                Then("it should decode the login") {
                    user.login shouldBe "twitchdev"
                }

                Then("it should decode the display name") {
                    user.displayName shouldBe "TwitchDev"
                }

                Then("it should decode the broadcaster type") {
                    user.broadcasterType shouldBe "partner"
                }

                Then("it should decode the profile image URL") {
                    user.profileImageUrl shouldBe "https://static-cdn.jtvnw.net/jtv_user_pictures/8a6381c7-d0c0-4576-b179-38bd5ce1d6af-profile_image-300x300.png"
                }

                Then("it should decode the created at timestamp") {
                    user.createdAt shouldBe Instant.parse("2026-01-15T20:32:28Z")
                }
            }

            When("deserializing a minimal user response") {
                val userJson =
                    """
                    {
                        "id": "12345",
                        "login": "minimaluser",
                        "display_name": "MinimalUser"
                    }
                    """.trimIndent()

                val user = json.decodeFromString<User>(userJson)

                Then("it should decode required fields") {
                    user.id shouldBe "12345"
                    user.login shouldBe "minimaluser"
                    user.displayName shouldBe "MinimalUser"
                }

                Then("it should use defaults for optional fields") {
                    user.type shouldBe ""
                    user.broadcasterType shouldBe ""
                    user.description shouldBe ""
                    user.profileImageUrl shouldBe ""
                    user.offlineImageUrl shouldBe ""
                    user.createdAt.shouldBeNull()
                }
            }
        }

        Given("a Poll JSON from Twitch API") {

            When("deserializing an active poll with channel points voting") {
                val pollJson =
                    """
                    {
                        "id": "ed961efd-8a3f-4cf5-a9d0-e616c590cd2a",
                        "broadcaster_id": "55696719",
                        "broadcaster_name": "TwitchDev",
                        "broadcaster_login": "twitchdev",
                        "title": "Heads or Tails?",
                        "choices": [
                            {
                                "id": "4c123012-1351-4f33-84b7-43856e7a0f47",
                                "title": "Heads",
                                "votes": 0,
                                "channel_points_votes": 0
                            },
                            {
                                "id": "279087e3-54a7-467e-bcd0-c1393fcea4f0",
                                "title": "Tails",
                                "votes": 0,
                                "channel_points_votes": 0
                            }
                        ],
                        "bits_voting_enabled": false,
                        "bits_per_vote": 0,
                        "channel_points_voting_enabled": true,
                        "channel_points_per_vote": 100,
                        "status": "ACTIVE",
                        "duration": 1800,
                        "started_at": "2026-02-10T06:08:33.871278372Z",
                        "ended_at": null
                    }
                    """.trimIndent()

                val poll = json.decodeFromString<Poll>(pollJson)

                Then("it should decode the poll id") {
                    poll.id shouldBe "ed961efd-8a3f-4cf5-a9d0-e616c590cd2a"
                }

                Then("it should decode the title") {
                    poll.title shouldBe "Heads or Tails?"
                }

                Then("it should decode choices") {
                    poll.choices shouldHaveSize 2
                    poll.choices[0].title shouldBe "Heads"
                    poll.choices[1].title shouldBe "Tails"
                }

                Then("it should decode channel points voting settings") {
                    poll.channelPointsVotingEnabled shouldBe true
                    poll.channelPointsPerVote shouldBe 100
                }

                Then("it should decode status as enum") {
                    poll.status shouldBe PollStatus.ACTIVE
                }

                Then("it should decode duration") {
                    poll.duration shouldBe 1800
                }

                Then("it should decode the started at timestamp") {
                    poll.startedAt shouldBe Instant.parse("2026-02-10T06:08:33.871278372Z")
                }

                Then("it should decode null ended_at") {
                    poll.endedAt.shouldBeNull()
                }
            }

            When("deserializing a completed poll") {
                val pollJson =
                    """
                    {
                        "id": "abc-123",
                        "broadcaster_id": "12345",
                        "broadcaster_name": "Streamer",
                        "broadcaster_login": "streamer",
                        "title": "Best game?",
                        "choices": [
                            {
                                "id": "choice-1",
                                "title": "Game A",
                                "votes": 150,
                                "channel_points_votes": 50
                            }
                        ],
                        "status": "COMPLETED",
                        "duration": 300,
                        "started_at": "2026-02-10T06:08:33Z",
                        "ended_at": "2026-02-10T06:13:33Z"
                    }
                    """.trimIndent()

                val poll = json.decodeFromString<Poll>(pollJson)

                Then("it should decode vote counts") {
                    poll.choices[0].votes shouldBe 150
                    poll.choices[0].channelPointsVotes shouldBe 50
                }

                Then("it should decode completed status") {
                    poll.status shouldBe PollStatus.COMPLETED
                }

                Then("it should decode ended_at timestamp") {
                    poll.endedAt shouldBe Instant.parse("2026-02-10T06:13:33Z")
                }
            }
        }

        Given("a Stream JSON from Twitch API") {

            When("deserializing a live stream response") {
                val streamJson =
                    """
                    {
                        "id": "40944725613",
                        "user_id": "67890",
                        "user_login": "streamerlogin",
                        "user_name": "StreamerName",
                        "game_id": "33214",
                        "game_name": "Fortnite",
                        "type": "live",
                        "title": "Hey! Playing Fortnite",
                        "tags": ["English", "FPS"],
                        "viewer_count": 25420,
                        "started_at": "2026-03-01T15:04:21Z",
                        "language": "en",
                        "thumbnail_url": "https://static-cdn.jtvnw.net/previews-ttv/live_user_streamerlogin-{width}x{height}.jpg",
                        "is_mature": false
                    }
                    """.trimIndent()

                val stream = json.decodeFromString<Stream>(streamJson)

                Then("it should decode the stream id") {
                    stream.id shouldBe "40944725613"
                }

                Then("it should decode user info") {
                    stream.userId shouldBe "67890"
                    stream.userLogin shouldBe "streamerlogin"
                    stream.userName shouldBe "StreamerName"
                }

                Then("it should decode game info") {
                    stream.gameId shouldBe "33214"
                    stream.gameName shouldBe "Fortnite"
                }

                Then("it should decode type as live") {
                    stream.type shouldBe "live"
                }

                Then("it should decode the title") {
                    stream.title shouldBe "Hey! Playing Fortnite"
                }

                Then("it should decode tags") {
                    stream.tags.shouldNotBeNull() shouldHaveSize 2
                    stream.tags shouldBe listOf("English", "FPS")
                }

                Then("it should decode viewer count") {
                    stream.viewerCount shouldBe 25420
                }

                Then("it should decode started at timestamp") {
                    stream.startedAt shouldBe Instant.parse("2026-03-01T15:04:21Z")
                }

                Then("it should decode is_mature") {
                    stream.isMature shouldBe false
                }
            }
        }

        Given("a Stream JSON with null tags from Twitch API") {

            When("deserializing a stream where tags is explicitly null") {
                val streamJson =
                    """
                    {
                        "id": "99887766",
                        "user_id": "11111",
                        "user_login": "notags",
                        "user_name": "NoTags",
                        "game_id": "509658",
                        "game_name": "Just Chatting",
                        "type": "live",
                        "title": "Stream with null tags",
                        "tags": null,
                        "viewer_count": 100,
                        "started_at": "2026-03-13T15:00:00Z",
                        "language": "en",
                        "thumbnail_url": "https://example.com/thumb.jpg",
                        "is_mature": false
                    }
                    """.trimIndent()

                val stream = json.decodeFromString<Stream>(streamJson)

                Then("it should decode without crashing") {
                    stream.id shouldBe "99887766"
                }

                Then("tags should be null") {
                    stream.tags.shouldBeNull()
                }
            }

            When("deserializing a stream where tags is missing entirely") {
                val streamJson =
                    """
                    {
                        "id": "55544433",
                        "user_id": "22222",
                        "user_login": "missingtags",
                        "user_name": "MissingTags",
                        "type": "live",
                        "title": "Stream without tags field"
                    }
                    """.trimIndent()

                val stream = json.decodeFromString<Stream>(streamJson)

                Then("tags should default to null") {
                    stream.tags.shouldBeNull()
                }
            }
        }

        Given("a ChannelInformation JSON with null tags from Twitch API") {

            When("deserializing channel info where tags is explicitly null") {
                val channelJson =
                    """
                    {
                        "broadcaster_id": "456",
                        "broadcaster_login": "streamer",
                        "broadcaster_name": "Streamer",
                        "broadcaster_language": "en",
                        "game_name": "Just Chatting",
                        "game_id": "509658",
                        "title": "Channel with null tags",
                        "delay": 0,
                        "tags": null,
                        "content_classification_labels": [],
                        "is_branded_content": false
                    }
                    """.trimIndent()

                val info = json.decodeFromString<ChannelInformation>(channelJson)

                Then("it should decode without crashing") {
                    info.broadcasterId shouldBe "456"
                    info.title shouldBe "Channel with null tags"
                }

                Then("tags should be null") {
                    info.tags.shouldBeNull()
                }
            }
        }

        Given("a SearchedChannel JSON with null tags from Twitch API") {

            When("deserializing a searched channel where tags is explicitly null") {
                val channelJson =
                    """
                    {
                        "broadcaster_language": "en",
                        "broadcaster_login": "nulltags",
                        "display_name": "NullTags",
                        "game_id": "509658",
                        "game_name": "Just Chatting",
                        "id": "33333",
                        "is_live": true,
                        "title": "Search result with null tags",
                        "thumbnail_url": "https://example.com/thumb.jpg",
                        "tags": null
                    }
                    """.trimIndent()

                val channel = json.decodeFromString<SearchedChannel>(channelJson)

                Then("it should decode without crashing") {
                    channel.id shouldBe "33333"
                }

                Then("tags should be null") {
                    channel.tags.shouldBeNull()
                }
            }
        }

        Given("a Subscription JSON from Twitch API") {

            When("deserializing a gift subscription") {
                val subJson =
                    """
                    {
                        "broadcaster_id": "141981764",
                        "broadcaster_login": "twitchdev",
                        "broadcaster_name": "TwitchDev",
                        "gifter_id": "12826",
                        "gifter_login": "twitch",
                        "gifter_name": "Twitch",
                        "is_gift": true,
                        "tier": "1000",
                        "plan_name": "Channel Subscription (twitchdev)",
                        "user_id": "527115020",
                        "user_name": "twitchgaming",
                        "user_login": "twitchgaming"
                    }
                    """.trimIndent()

                val sub = json.decodeFromString<Subscription>(subJson)

                Then("it should decode broadcaster info") {
                    sub.broadcasterId shouldBe "141981764"
                    sub.broadcasterLogin shouldBe "twitchdev"
                    sub.broadcasterName shouldBe "TwitchDev"
                }

                Then("it should decode gifter info") {
                    sub.gifterId shouldBe "12826"
                    sub.gifterLogin shouldBe "twitch"
                    sub.gifterName shouldBe "Twitch"
                }

                Then("it should decode is_gift as true") {
                    sub.isGift shouldBe true
                }

                Then("it should decode the tier") {
                    sub.tier shouldBe "1000"
                }

                Then("it should decode subscriber info") {
                    sub.userId shouldBe "527115020"
                    sub.userName shouldBe "twitchgaming"
                    sub.userLogin shouldBe "twitchgaming"
                }
            }

            When("deserializing a non-gift subscription") {
                val subJson =
                    """
                    {
                        "broadcaster_id": "141981764",
                        "broadcaster_login": "twitchdev",
                        "broadcaster_name": "TwitchDev",
                        "gifter_id": "",
                        "gifter_login": "",
                        "gifter_name": "",
                        "is_gift": false,
                        "tier": "2000",
                        "plan_name": "Channel Subscription (twitchdev): $9.99 Sub",
                        "user_id": "99999",
                        "user_name": "Subscriber",
                        "user_login": "subscriber"
                    }
                    """.trimIndent()

                val sub = json.decodeFromString<Subscription>(subJson)

                Then("it should decode is_gift as false") {
                    sub.isGift shouldBe false
                }

                Then("it should decode the tier") {
                    sub.tier shouldBe "2000"
                }

                Then("it should decode empty gifter fields") {
                    sub.gifterId shouldBe ""
                    sub.gifterLogin shouldBe ""
                    sub.gifterName shouldBe ""
                }
            }
        }

        Given("a CustomReward JSON from Twitch API") {

            When("deserializing a custom reward with all settings") {
                val rewardJson =
                    """
                    {
                        "broadcaster_id": "274637212",
                        "broadcaster_login": "teststreamer",
                        "broadcaster_name": "TestStreamer",
                        "id": "92af127c-7326-4483-a52b-b0da0be61c01",
                        "title": "game reward",
                        "prompt": "reward prompt",
                        "cost": 30000,
                        "image": {
                            "url_1x": "https://cdn.example.com/1x.png",
                            "url_2x": "https://cdn.example.com/2x.png",
                            "url_4x": "https://cdn.example.com/4x.png"
                        },
                        "default_image": {
                            "url_1x": "https://static-cdn.jtvnw.net/custom-reward-images/default-1.png",
                            "url_2x": "https://static-cdn.jtvnw.net/custom-reward-images/default-2.png",
                            "url_4x": "https://static-cdn.jtvnw.net/custom-reward-images/default-4.png"
                        },
                        "background_color": "#00E5CB",
                        "is_enabled": true,
                        "is_user_input_required": true,
                        "max_per_stream_setting": {
                            "is_enabled": true,
                            "max_per_stream": 7
                        },
                        "max_per_user_per_stream_setting": {
                            "is_enabled": true,
                            "max_per_user_per_stream": 3
                        },
                        "global_cooldown_setting": {
                            "is_enabled": true,
                            "global_cooldown_seconds": 60
                        },
                        "is_paused": false,
                        "is_in_stock": true,
                        "should_redemptions_skip_request_queue": false,
                        "redemptions_redeemed_current_stream": 15,
                        "cooldown_expires_at": "2026-03-07T18:15:00.000Z"
                    }
                    """.trimIndent()

                val reward = json.decodeFromString<CustomReward>(rewardJson)

                Then("it should decode broadcaster info") {
                    reward.broadcasterId shouldBe "274637212"
                    reward.broadcasterLogin shouldBe "teststreamer"
                    reward.broadcasterName shouldBe "TestStreamer"
                }

                Then("it should decode reward identity") {
                    reward.id shouldBe "92af127c-7326-4483-a52b-b0da0be61c01"
                    reward.title shouldBe "game reward"
                    reward.prompt shouldBe "reward prompt"
                    reward.cost shouldBe 30000
                }

                Then("it should decode the custom image") {
                    reward.image shouldBe
                        RewardImage(
                            url1x = "https://cdn.example.com/1x.png",
                            url2x = "https://cdn.example.com/2x.png",
                            url4x = "https://cdn.example.com/4x.png",
                        )
                }

                Then("it should decode the default image") {
                    reward.defaultImage shouldBe
                        RewardImage(
                            url1x = "https://static-cdn.jtvnw.net/custom-reward-images/default-1.png",
                            url2x = "https://static-cdn.jtvnw.net/custom-reward-images/default-2.png",
                            url4x = "https://static-cdn.jtvnw.net/custom-reward-images/default-4.png",
                        )
                }

                Then("it should decode styling") {
                    reward.backgroundColor shouldBe "#00E5CB"
                }

                Then("it should decode boolean flags") {
                    reward.isEnabled shouldBe true
                    reward.isUserInputRequired shouldBe true
                    reward.isPaused shouldBe false
                    reward.isInStock shouldBe true
                    reward.shouldRedemptionsSkipRequestQueue shouldBe false
                }

                Then("it should decode max per stream setting") {
                    reward.maxPerStreamSetting.isEnabled shouldBe true
                    reward.maxPerStreamSetting.maxPerStream shouldBe 7
                }

                Then("it should decode max per user per stream setting") {
                    reward.maxPerUserPerStreamSetting.isEnabled shouldBe true
                    reward.maxPerUserPerStreamSetting.maxPerUserPerStream shouldBe 3
                }

                Then("it should decode global cooldown setting") {
                    reward.globalCooldownSetting.isEnabled shouldBe true
                    reward.globalCooldownSetting.globalCooldownSeconds shouldBe 60
                }

                Then("it should decode stream-specific fields") {
                    reward.redemptionsRedeemedCurrentStream shouldBe 15
                    reward.cooldownExpiresAt shouldBe Instant.parse("2026-03-07T18:15:00Z")
                }
            }

            When("deserializing a reward with null optional fields") {
                val rewardJson =
                    """
                    {
                        "broadcaster_id": "274637212",
                        "broadcaster_login": "teststreamer",
                        "broadcaster_name": "TestStreamer",
                        "id": "abc-123",
                        "title": "Simple Reward",
                        "cost": 100,
                        "image": null,
                        "default_image": {
                            "url_1x": "https://static-cdn.jtvnw.net/default-1.png",
                            "url_2x": "https://static-cdn.jtvnw.net/default-2.png",
                            "url_4x": "https://static-cdn.jtvnw.net/default-4.png"
                        },
                        "background_color": "#FF0000",
                        "is_enabled": true,
                        "is_user_input_required": false,
                        "max_per_stream_setting": {
                            "is_enabled": false,
                            "max_per_stream": 0
                        },
                        "max_per_user_per_stream_setting": {
                            "is_enabled": false,
                            "max_per_user_per_stream": 0
                        },
                        "global_cooldown_setting": {
                            "is_enabled": false,
                            "global_cooldown_seconds": 0
                        },
                        "is_paused": false,
                        "is_in_stock": true,
                        "should_redemptions_skip_request_queue": true,
                        "redemptions_redeemed_current_stream": null,
                        "cooldown_expires_at": null
                    }
                    """.trimIndent()

                val reward = json.decodeFromString<CustomReward>(rewardJson)

                Then("it should decode null image") {
                    reward.image.shouldBeNull()
                }

                Then("it should decode null redemptions count") {
                    reward.redemptionsRedeemedCurrentStream.shouldBeNull()
                }

                Then("it should decode null cooldown expiry") {
                    reward.cooldownExpiresAt.shouldBeNull()
                }
            }
        }

        Given("a RawAdSchedule JSON from Twitch API") {

            When("deserializing and converting to AdSchedule") {
                val adJson =
                    """
                    {
                        "next_ad_at": "2026-03-07T23:08:18Z",
                        "last_ad_at": "2026-03-07T22:08:18Z",
                        "duration": 60,
                        "preroll_free_time": 90,
                        "snooze_count": 1,
                        "snooze_refresh_at": "2026-03-07T23:38:18Z"
                    }
                    """.trimIndent()

                val raw = json.decodeFromString<RawAdSchedule>(adJson)
                val adSchedule = raw.toAdSchedule()

                Then("it should convert timestamps to Instant") {
                    adSchedule.nextAdAt shouldBe Instant.parse("2026-03-07T23:08:18Z")
                    adSchedule.lastAdAt shouldBe Instant.parse("2026-03-07T22:08:18Z")
                    adSchedule.snoozeRefreshAt shouldBe Instant.parse("2026-03-07T23:38:18Z")
                }

                Then("it should preserve non-timestamp fields") {
                    adSchedule.duration shouldBe 60
                    adSchedule.prerollFreeTime shouldBe 90
                    adSchedule.snoozeCount shouldBe 1
                }
            }

            When("deserializing with empty optional timestamp strings") {
                val adJson =
                    """
                    {
                        "next_ad_at": "",
                        "last_ad_at": "",
                        "duration": 0,
                        "preroll_free_time": 0,
                        "snooze_count": 0,
                        "snooze_refresh_at": "2026-03-07T23:38:18Z"
                    }
                    """.trimIndent()

                val raw = json.decodeFromString<RawAdSchedule>(adJson)
                val adSchedule = raw.toAdSchedule()

                Then("it should convert empty strings to null for optional fields") {
                    adSchedule.nextAdAt.shouldBeNull()
                    adSchedule.lastAdAt.shouldBeNull()
                }

                Then("it should parse snoozeRefreshAt as non-null") {
                    adSchedule.snoozeRefreshAt shouldBe Instant.parse("2026-03-07T23:38:18Z")
                }
            }
        }

        Given("an EventSubSubscription JSON from Twitch API") {

            When("deserializing a websocket subscription response") {
                val eventSubJson =
                    """
                    {
                        "id": "f1c2a387-161a-49f9-a165-0f21d7a4e1c4",
                        "status": "enabled",
                        "type": "channel.update",
                        "version": "2",
                        "condition": {
                            "broadcaster_user_id": "141981764"
                        },
                        "transport": {
                            "method": "websocket",
                            "session_id": "AgoQHR3s6Mb4T8GFB1l3DlEOS"
                        },
                        "created_at": "2026-01-20T10:11:12.123Z",
                        "cost": 0
                    }
                    """.trimIndent()

                val sub = json.decodeFromString<EventSubSubscription>(eventSubJson)

                Then("it should decode subscription identity") {
                    sub.id shouldBe "f1c2a387-161a-49f9-a165-0f21d7a4e1c4"
                    sub.status shouldBe "enabled"
                    sub.type shouldBe "channel.update"
                    sub.version shouldBe "2"
                }

                Then("it should decode the condition map") {
                    sub.condition shouldBe mapOf("broadcaster_user_id" to "141981764")
                }

                Then("it should decode transport for websocket") {
                    sub.transport.method shouldBe "websocket"
                    sub.transport.sessionId shouldBe "AgoQHR3s6Mb4T8GFB1l3DlEOS"
                    sub.transport.callback.shouldBeNull()
                }

                Then("it should decode metadata") {
                    sub.createdAt shouldBe Instant.parse("2026-01-20T10:11:12.123Z")
                    sub.cost shouldBe 0
                }
            }
        }

        Given("a Follower JSON from Twitch API") {

            When("deserializing a follower response") {
                val followerJson =
                    """
                    {
                        "user_id": "11111",
                        "user_login": "userloginname",
                        "user_name": "UserDisplayName",
                        "followed_at": "2026-02-14T22:22:08Z"
                    }
                    """.trimIndent()

                val follower = json.decodeFromString<Follower>(followerJson)

                Then("it should decode all fields") {
                    follower.userId shouldBe "11111"
                    follower.userLogin shouldBe "userloginname"
                    follower.userName shouldBe "UserDisplayName"
                    follower.followedAt shouldBe Instant.parse("2026-02-14T22:22:08Z")
                }
            }
        }

        Given("a ChatBadge JSON from Twitch API") {

            When("deserializing a chat badge set") {
                val badgeJson =
                    """
                    {
                        "set_id": "subscriber",
                        "versions": [
                            {
                                "id": "0",
                                "image_url_1x": "https://static-cdn.jtvnw.net/badges/v1/badge/1x",
                                "image_url_2x": "https://static-cdn.jtvnw.net/badges/v1/badge/2x",
                                "image_url_4x": "https://static-cdn.jtvnw.net/badges/v1/badge/4x",
                                "title": "Subscriber",
                                "description": "Subscriber"
                            }
                        ]
                    }
                    """.trimIndent()

                val badge = json.decodeFromString<ChatBadge>(badgeJson)

                Then("it should decode the set id") {
                    badge.setId shouldBe "subscriber"
                }

                Then("it should decode badge versions") {
                    badge.versions shouldHaveSize 1
                    badge.versions[0].id shouldBe "0"
                    badge.versions[0].imageUrl1x shouldBe "https://static-cdn.jtvnw.net/badges/v1/badge/1x"
                    badge.versions[0].imageUrl2x shouldBe "https://static-cdn.jtvnw.net/badges/v1/badge/2x"
                    badge.versions[0].imageUrl4x shouldBe "https://static-cdn.jtvnw.net/badges/v1/badge/4x"
                    badge.versions[0].title shouldBe "Subscriber"
                    badge.versions[0].description shouldBe "Subscriber"
                }
            }
        }

        Given("a Clip JSON from Twitch API") {

            When("deserializing a complete clip response") {
                val clipJson =
                    """
                    {
                        "id": "AwkwardHelplessSalamanderSwiftRage-abc123",
                        "url": "https://clips.twitch.tv/AwkwardHelplessSalamanderSwiftRage-abc123",
                        "embed_url": "https://clips.twitch.tv/embed?clip=AwkwardHelplessSalamanderSwiftRage-abc123",
                        "broadcaster_id": "67890",
                        "broadcaster_name": "CoolStreamer",
                        "creator_id": "12345",
                        "creator_name": "ClipMaker",
                        "video_id": "v98765",
                        "game_id": "33214",
                        "language": "en",
                        "title": "Amazing play!",
                        "view_count": 1500,
                        "created_at": "2026-01-15T12:30:00Z",
                        "thumbnail_url": "https://clips-media-assets2.twitch.tv/abc-preview-480x272.jpg",
                        "duration": 30.0,
                        "vod_offset": 3600,
                        "is_featured": true
                    }
                    """.trimIndent()

                val clip = json.decodeFromString<Clip>(clipJson)

                Then("it should decode clip identity") {
                    clip.id shouldBe "AwkwardHelplessSalamanderSwiftRage-abc123"
                    clip.url shouldBe "https://clips.twitch.tv/AwkwardHelplessSalamanderSwiftRage-abc123"
                    clip.embedUrl shouldBe "https://clips.twitch.tv/embed?clip=AwkwardHelplessSalamanderSwiftRage-abc123"
                }

                Then("it should decode broadcaster and creator info") {
                    clip.broadcasterId shouldBe "67890"
                    clip.broadcasterName shouldBe "CoolStreamer"
                    clip.creatorId shouldBe "12345"
                    clip.creatorName shouldBe "ClipMaker"
                }

                Then("it should decode content metadata") {
                    clip.title shouldBe "Amazing play!"
                    clip.gameId shouldBe "33214"
                    clip.language shouldBe "en"
                    clip.viewCount shouldBe 1500
                    clip.duration shouldBe 30.0
                }

                Then("it should decode timestamps and media") {
                    clip.createdAt shouldBe Instant.parse("2026-01-15T12:30:00Z")
                    clip.thumbnailUrl shouldBe "https://clips-media-assets2.twitch.tv/abc-preview-480x272.jpg"
                }

                Then("it should decode optional fields") {
                    clip.videoId shouldBe "v98765"
                    clip.vodOffset shouldBe 3600
                    clip.isFeatured shouldBe true
                }
            }

            When("deserializing a minimal clip response") {
                val clipJson =
                    """
                    {
                        "id": "MinimalClip-xyz",
                        "url": "https://clips.twitch.tv/MinimalClip-xyz",
                        "embed_url": "https://clips.twitch.tv/embed?clip=MinimalClip-xyz",
                        "broadcaster_id": "11111",
                        "broadcaster_name": "Broadcaster",
                        "creator_id": "22222",
                        "creator_name": "Creator",
                        "title": "Short clip",
                        "created_at": "2026-06-01T08:00:00Z",
                        "thumbnail_url": "https://clips-media-assets2.twitch.tv/xyz-preview-480x272.jpg"
                    }
                    """.trimIndent()

                val clip = json.decodeFromString<Clip>(clipJson)

                Then("it should use defaults for optional fields") {
                    clip.videoId shouldBe ""
                    clip.gameId shouldBe ""
                    clip.language shouldBe ""
                    clip.viewCount shouldBe 0
                    clip.duration shouldBe 0.0
                    clip.vodOffset.shouldBeNull()
                    clip.isFeatured shouldBe false
                }
            }
        }

        Given("a ClipDownload JSON from Twitch API") {

            When("deserializing a clip download with both URLs") {
                val downloadJson =
                    """
                    {
                        "clip_id": "AwkwardHelplessSalamanderSwiftRage-abc123",
                        "landscape_download_url": "https://production.assets.clips.twitchcdn.net/abc123/AT-cm%7Cabc123-landscape.mp4",
                        "portrait_download_url": "https://production.assets.clips.twitchcdn.net/abc123/AT-cm%7Cabc123-portrait.mp4"
                    }
                    """.trimIndent()

                val download = json.decodeFromString<ClipDownload>(downloadJson)

                Then("it should decode the clip ID") {
                    download.clipId shouldBe "AwkwardHelplessSalamanderSwiftRage-abc123"
                }

                Then("it should decode the landscape download URL") {
                    download.landscapeDownloadUrl shouldBe "https://production.assets.clips.twitchcdn.net/abc123/AT-cm%7Cabc123-landscape.mp4"
                }

                Then("it should decode the portrait download URL") {
                    download.portraitDownloadUrl shouldBe "https://production.assets.clips.twitchcdn.net/abc123/AT-cm%7Cabc123-portrait.mp4"
                }
            }

            When("deserializing a clip download with null URLs") {
                val downloadJson =
                    """
                    {
                        "clip_id": "MinimalClip-xyz",
                        "landscape_download_url": null,
                        "portrait_download_url": null
                    }
                    """.trimIndent()

                val download = json.decodeFromString<ClipDownload>(downloadJson)

                Then("it should decode null landscape URL") {
                    download.landscapeDownloadUrl.shouldBeNull()
                }

                Then("it should decode null portrait URL") {
                    download.portraitDownloadUrl.shouldBeNull()
                }
            }
        }

        Given("a Game JSON from Twitch API") {

            When("deserializing a game/category response") {
                val gameJson =
                    """
                    {
                        "id": "33214",
                        "name": "Fortnite",
                        "box_art_url": "https://static-cdn.jtvnw.net/ttv-boxart/33214-52x72.jpg",
                        "igdb_id": "1905"
                    }
                    """.trimIndent()

                val game = json.decodeFromString<Game>(gameJson)

                Then("it should decode all fields") {
                    game.id shouldBe "33214"
                    game.name shouldBe "Fortnite"
                    game.boxArtUrl shouldBe "https://static-cdn.jtvnw.net/ttv-boxart/33214-52x72.jpg"
                    game.igdbId shouldBe "1905"
                }
            }
        }
    })
