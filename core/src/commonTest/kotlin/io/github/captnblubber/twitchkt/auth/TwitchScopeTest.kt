package io.github.captnblubber.twitchkt.auth

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TwitchScopeTest :
    BehaviorSpec({

        // --- fromValue ---

        Given("a valid scope string") {
            When("calling fromValue with 'chat:read'") {
                val result = TwitchScope.fromValue("chat:read")

                Then("it should return CHAT_READ") {
                    result shouldBe TwitchScope.CHAT_READ
                }
            }

            When("calling fromValue with 'channel:manage:polls'") {
                val result = TwitchScope.fromValue("channel:manage:polls")

                Then("it should return CHANNEL_MANAGE_POLLS") {
                    result shouldBe TwitchScope.CHANNEL_MANAGE_POLLS
                }
            }
        }

        Given("an unknown scope string") {
            When("calling fromValue with 'not:a:scope'") {
                val result = TwitchScope.fromValue("not:a:scope")

                Then("it should return null") {
                    result.shouldBeNull()
                }
            }
        }

        // --- isSatisfied ---

        Given("a required scope that is directly in the granted set") {
            When("checking isSatisfied") {
                val result =
                    TwitchScope.isSatisfied(
                        required = TwitchScope.CHAT_READ,
                        granted = setOf(TwitchScope.CHAT_READ, TwitchScope.BITS_READ),
                    )

                Then("it should return true") {
                    result shouldBe true
                }
            }
        }

        Given("a manage scope that implies the required read scope") {
            When("checking isSatisfied for channel:read:polls with channel:manage:polls granted") {
                val result =
                    TwitchScope.isSatisfied(
                        required = TwitchScope.CHANNEL_READ_POLLS,
                        granted = setOf(TwitchScope.CHANNEL_MANAGE_POLLS),
                    )

                Then("it should return true because manage implies read") {
                    result shouldBe true
                }
            }
        }

        Given("a granted set that does not satisfy the required scope") {
            When("checking isSatisfied for channel:manage:polls with only channel:read:polls granted") {
                val result =
                    TwitchScope.isSatisfied(
                        required = TwitchScope.CHANNEL_MANAGE_POLLS,
                        granted = setOf(TwitchScope.CHANNEL_READ_POLLS),
                    )

                Then("it should return false because read does not imply manage") {
                    result shouldBe false
                }
            }
        }

        Given("an empty granted set") {
            When("checking isSatisfied") {
                val result =
                    TwitchScope.isSatisfied(
                        required = TwitchScope.CHAT_READ,
                        granted = emptySet(),
                    )

                Then("it should return false") {
                    result shouldBe false
                }
            }
        }

        // --- implies ---

        Given("a manage scope with hierarchy") {
            When("accessing implies on CHANNEL_MANAGE_POLLS") {
                val implied = TwitchScope.CHANNEL_MANAGE_POLLS.implies

                Then("it should contain CHANNEL_READ_POLLS") {
                    implied shouldContainExactly setOf(TwitchScope.CHANNEL_READ_POLLS)
                }
            }
        }

        Given("the special case CHANNEL_MANAGE_MODERATORS") {
            When("accessing implies") {
                val implied = TwitchScope.CHANNEL_MANAGE_MODERATORS.implies

                Then("it should contain MODERATION_READ") {
                    implied shouldContainExactly setOf(TwitchScope.MODERATION_READ)
                }
            }
        }

        Given("a scope without hierarchy") {
            When("accessing implies on CHAT_READ") {
                val implied = TwitchScope.CHAT_READ.implies

                Then("it should return an empty set") {
                    implied.shouldBeEmpty()
                }
            }
        }
    })
