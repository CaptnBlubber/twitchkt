@file:Suppress("DEPRECATION")

package io.github.captnblubber.twitchkt.irc.internal

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.github.captnblubber.twitchkt.irc.IrcMessage
import io.github.captnblubber.twitchkt.model.common.SubTier

class IrcParserTest :
    BehaviorSpec({
        val parser = IrcParser()

        Given("a PRIVMSG line") {
            val line =
                "@badge-info=;badges=moderator/1;color=#FF0000;display-name=TestUser;" +
                    "emotes=;id=abc-123;mod=1;room-id=12345;subscriber=0;" +
                    "tmi-sent-ts=1642000000000;turbo=0;user-id=67890;user-type=mod " +
                    ":testuser!testuser@testuser.tmi.twitch.tv PRIVMSG #mychannel :Hello world"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return a PrivMsg with correct fields") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.PrivMsg>()
                    msg.channel shouldBe "mychannel"
                    msg.userId shouldBe "67890"
                    msg.userLogin shouldBe "testuser"
                    msg.displayName shouldBe "TestUser"
                    msg.message shouldBe "Hello world"
                    msg.tags["color"] shouldBe "#FF0000"
                }
            }
        }

        Given("a USERNOTICE with msg-id=sub") {
            val line =
                "@badge-info=subscriber/0;badges=subscriber/0;color=#0000FF;" +
                    "display-name=SubUser;emotes=;flags=;id=sub-123;login=subuser;mod=0;" +
                    "msg-id=sub;msg-param-cumulative-months=1;msg-param-months=0;" +
                    "msg-param-multimonth-duration=1;msg-param-multimonth-tenure=0;" +
                    "msg-param-should-share-streak=0;msg-param-sub-plan-name=Channel\\sSub;" +
                    "msg-param-sub-plan=1000;msg-param-was-gifted=false;room-id=12345;" +
                    "subscriber=1;system-msg=subuser\\ssubscribed\\sat\\sTier\\s1.;" +
                    "tmi-sent-ts=1642000001000;user-id=11111;user-type= " +
                    ":subuser!subuser@subuser.tmi.twitch.tv USERNOTICE #mychannel"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return UserNotice.Sub with correct tier") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.UserNotice.Sub>()
                    msg.channel shouldBe "mychannel"
                    msg.userId shouldBe "11111"
                    msg.userLogin shouldBe "subuser"
                    msg.displayName shouldBe "SubUser"
                    msg.tier shouldBe SubTier.TIER_1
                    msg.isGift shouldBe false
                    msg.systemMessage shouldBe "subuser subscribed at Tier 1."
                }
            }
        }

        Given("a USERNOTICE with msg-id=resub") {
            val line =
                "@badge-info=subscriber/24;badges=subscriber/24;color=#00FF00;" +
                    "display-name=ResubUser;emotes=;flags=;id=resub-456;login=resubuser;" +
                    "mod=0;msg-id=resub;msg-param-cumulative-months=24;" +
                    "msg-param-months=0;msg-param-multimonth-duration=0;" +
                    "msg-param-multimonth-tenure=0;msg-param-should-share-streak=1;" +
                    "msg-param-streak-months=12;msg-param-sub-plan-name=Channel\\sSub;" +
                    "msg-param-sub-plan=2000;msg-param-was-gifted=false;room-id=12345;" +
                    "subscriber=1;system-msg=ResubUser\\ssubscribed\\sat\\sTier\\s2.;" +
                    "tmi-sent-ts=1642000002000;user-id=22222;user-type= " +
                    ":resubuser!resubuser@resubuser.tmi.twitch.tv USERNOTICE #mychannel " +
                    ":Still loving this stream!"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return UserNotice.Resub with cumulative and streak months") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.UserNotice.Resub>()
                    msg.channel shouldBe "mychannel"
                    msg.userId shouldBe "22222"
                    msg.tier shouldBe SubTier.TIER_2
                    msg.cumulativeMonths shouldBe 24
                    msg.streakMonths shouldBe 12
                    msg.userMessage shouldBe "Still loving this stream!"
                }
            }
        }

        Given("a USERNOTICE with msg-id=subgift") {
            val line =
                "@badge-info=subscriber/6;badges=subscriber/6;color=#FF00FF;" +
                    "display-name=Gifter;emotes=;flags=;id=gift-789;login=gifter;mod=0;" +
                    "msg-id=subgift;msg-param-months=1;msg-param-recipient-display-name=Lucky;" +
                    "msg-param-recipient-id=33333;msg-param-recipient-user-name=lucky;" +
                    "msg-param-sub-plan-name=Channel\\sSub;msg-param-sub-plan=3000;" +
                    "room-id=12345;subscriber=1;" +
                    "system-msg=Gifter\\sgifted\\sa\\sTier\\s3\\ssub\\sto\\sLucky!;" +
                    "tmi-sent-ts=1642000003000;user-id=44444;user-type= " +
                    ":gifter!gifter@gifter.tmi.twitch.tv USERNOTICE #mychannel"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return UserNotice.SubGift with recipient") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.UserNotice.SubGift>()
                    msg.channel shouldBe "mychannel"
                    msg.userId shouldBe "44444"
                    msg.tier shouldBe SubTier.TIER_3
                    msg.recipientLogin shouldBe "lucky"
                    msg.recipientDisplayName shouldBe "Lucky"
                }
            }
        }

        Given("a USERNOTICE with msg-id=raid") {
            val line =
                "@badge-info=;badges=;color=#AABBCC;display-name=Raider;emotes=;flags=;" +
                    "id=raid-101;login=raider;mod=0;msg-id=raid;" +
                    "msg-param-displayName=Raider;msg-param-login=raider;" +
                    "msg-param-viewerCount=150;room-id=12345;subscriber=0;" +
                    "system-msg=150\\sraiders\\sfrom\\sRaider\\shave\\sjoined!;" +
                    "tmi-sent-ts=1642000004000;user-id=55555;user-type= " +
                    ":raider!raider@raider.tmi.twitch.tv USERNOTICE #mychannel"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return UserNotice.Raid with viewer count") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.UserNotice.Raid>()
                    msg.channel shouldBe "mychannel"
                    msg.userId shouldBe "55555"
                    msg.displayName shouldBe "Raider"
                    msg.viewerCount shouldBe 150
                }
            }
        }

        Given("a USERNOTICE with msg-id=viewer-milestone and msg-param-category=watch-streak") {
            val line =
                "@badge-info=subscriber/15;badges=subscriber/12;color=#00FF00;" +
                    "display-name=Viewer1;emotes=;flags=;id=def-456;login=viewer1;mod=0;" +
                    "msg-id=viewer-milestone;msg-param-category=watch-streak;" +
                    "msg-param-copoReward=450;msg-param-id=deadbeef;msg-param-value=15;" +
                    "room-id=12345;subscriber=1;" +
                    "system-msg=viewer1\\shas\\swatched\\sfor\\s15\\sconsecutive\\sstreams;" +
                    "tmi-sent-ts=1642000001000;user-id=11111;user-type= " +
                    ":viewer1!viewer1@viewer1.tmi.twitch.tv USERNOTICE #mychannel :Keep it up!"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return UserNotice.WatchStreak with streak months") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.UserNotice.WatchStreak>()
                    msg.channel shouldBe "mychannel"
                    msg.userId shouldBe "11111"
                    msg.userLogin shouldBe "viewer1"
                    msg.displayName shouldBe "Viewer1"
                    msg.streakMonths shouldBe 15
                    msg.userMessage shouldBe "Keep it up!"
                    msg.systemMessage shouldBe "viewer1 has watched for 15 consecutive streams"
                }
            }
        }

        Given("a USERNOTICE with unknown msg-id") {
            val line =
                "@badge-info=;badges=;color=;display-name=Someone;emotes=;flags=;" +
                    "id=unk-999;login=someone;mod=0;msg-id=bitsbadgetier;" +
                    "room-id=12345;subscriber=0;" +
                    "system-msg=Someone\\searned\\sa\\snew\\sBits\\sbadge!;" +
                    "tmi-sent-ts=1642000005000;user-id=66666;user-type= " +
                    ":someone!someone@someone.tmi.twitch.tv USERNOTICE #mychannel"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return UserNotice.Unknown") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.UserNotice.Unknown>()
                    msg.channel shouldBe "mychannel"
                    msg.msgId shouldBe "bitsbadgetier"
                    msg.userId shouldBe "66666"
                }
            }
        }

        Given("a ROOMSTATE line") {
            val line =
                "@emote-only=0;followers-only=-1;r9k=0;room-id=12345;slow=10;subs-only=1 " +
                    ":tmi.twitch.tv ROOMSTATE #mychannel"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return RoomState with room settings") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.RoomState>()
                    msg.channel shouldBe "mychannel"
                    msg.emoteOnly shouldBe false
                    msg.followersOnly shouldBe -1
                    msg.slow shouldBe 10
                    msg.subsOnly shouldBe true
                }
            }
        }

        Given("a CLEARCHAT line with target user") {
            val line =
                "@ban-duration=600;room-id=12345;target-user-id=77777;" +
                    "tmi-sent-ts=1642000006000 " +
                    ":tmi.twitch.tv CLEARCHAT #mychannel :baduser"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return ClearChat with targetUserId and duration") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.ClearChat>()
                    msg.channel shouldBe "mychannel"
                    msg.targetUserId shouldBe "77777"
                    msg.duration shouldBe 600
                }
            }
        }

        Given("a CLEARCHAT line without target (full clear)") {
            val line =
                "@room-id=12345;tmi-sent-ts=1642000007000 " +
                    ":tmi.twitch.tv CLEARCHAT #mychannel"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return ClearChat with null targetUserId") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.ClearChat>()
                    msg.channel shouldBe "mychannel"
                    msg.targetUserId.shouldBeNull()
                    msg.duration.shouldBeNull()
                }
            }
        }

        Given("a CLEARMSG line") {
            val line =
                "@login=baduser;room-id=;target-msg-id=msg-abc-123;" +
                    "tmi-sent-ts=1642000008000 " +
                    ":tmi.twitch.tv CLEARMSG #mychannel :Deleted message text"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return ClearMsg with targetMessageId") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.ClearMsg>()
                    msg.channel shouldBe "mychannel"
                    msg.targetMessageId shouldBe "msg-abc-123"
                    msg.login shouldBe "baduser"
                }
            }
        }

        Given("a NOTICE line") {
            val line =
                "@msg-id=slow_on :tmi.twitch.tv NOTICE #mychannel " +
                    ":This room is now in slow mode. You may send messages every 10 seconds."

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return Notice with msgId and message") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.Notice>()
                    msg.channel shouldBe "mychannel"
                    msg.msgId shouldBe "slow_on"
                    msg.message shouldBe
                        "This room is now in slow mode. You may send messages every 10 seconds."
                }
            }
        }

        Given("a PING line") {
            val line = "PING :tmi.twitch.tv"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return null") {
                    result.shouldBeNull()
                }
            }
        }

        Given("a numeric reply (001)") {
            val line = ":tmi.twitch.tv 001 justinfan12345 :Welcome, GLHF!"

            When("parsing") {
                val result = parser.parse(line)

                Then("it should return null") {
                    result.shouldBeNull()
                }
            }
        }

        Given("a line with escaped tag values") {
            val line =
                "@display-name=Test\\sUser;system-msg=Hello\\s\\:\\sworld\\\\end " +
                    ":tmi.twitch.tv NOTICE #mychannel :Test message"

            When("parsing") {
                val result = parser.parse(line)

                Then("tag values should be properly unescaped") {
                    val msg = result.shouldBeInstanceOf<IrcMessage.Notice>()
                    msg.tags["display-name"] shouldBe "Test User"
                    msg.tags["system-msg"] shouldBe "Hello ; world\\end"
                }
            }
        }
    })
