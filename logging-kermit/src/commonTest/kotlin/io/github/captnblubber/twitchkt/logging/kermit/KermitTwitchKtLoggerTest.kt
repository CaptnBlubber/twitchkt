package io.github.captnblubber.twitchkt.logging.kermit

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.github.captnblubber.twitchkt.logging.LogLevel
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class KermitTwitchKtLoggerTest :
    BehaviorSpec({

        data class LogEntry(
            val severity: Severity,
            val tag: String,
            val message: String,
        )

        fun setupCapture(): MutableList<LogEntry> {
            val captured = mutableListOf<LogEntry>()
            Logger.setLogWriters(
                object : LogWriter() {
                    override fun log(
                        severity: Severity,
                        message: String,
                        tag: String,
                        throwable: Throwable?,
                    ) {
                        captured.add(LogEntry(severity, tag, message))
                    }
                },
            )
            Logger.setMinSeverity(Severity.Verbose)
            return captured
        }

        Given("KermitTwitchKtLogger") {

            When("log is called with VERBOSE level") {
                val captured = setupCapture()
                val logger = KermitTwitchKtLogger()
                logger.log(LogLevel.VERBOSE, "test") { "verbose message" }

                Then("it should delegate to Kermit with Verbose severity") {
                    captured.size shouldBe 1
                    captured.first().severity shouldBe Severity.Verbose
                    captured.first().message shouldBe "verbose message"
                }

                Then("it should use the prefixed tag") {
                    captured.first().tag shouldBe "twitchkt/test"
                }
            }

            When("log is called with DEBUG level") {
                val captured = setupCapture()
                val logger = KermitTwitchKtLogger()
                logger.log(LogLevel.DEBUG, "debug-tag") { "debug message" }

                Then("it should delegate to Kermit with Debug severity") {
                    captured.size shouldBe 1
                    captured.first().severity shouldBe Severity.Debug
                    captured.first().message shouldBe "debug message"
                    captured.first().tag shouldBe "twitchkt/debug-tag"
                }
            }

            When("log is called with INFO level") {
                val captured = setupCapture()
                val logger = KermitTwitchKtLogger()
                logger.log(LogLevel.INFO, "info-tag") { "info message" }

                Then("it should delegate to Kermit with Info severity") {
                    captured.size shouldBe 1
                    captured.first().severity shouldBe Severity.Info
                    captured.first().message shouldBe "info message"
                    captured.first().tag shouldBe "twitchkt/info-tag"
                }
            }

            When("log is called with WARN level") {
                val captured = setupCapture()
                val logger = KermitTwitchKtLogger()
                logger.log(LogLevel.WARN, "warn-tag") { "warn message" }

                Then("it should delegate to Kermit with Warn severity") {
                    captured.size shouldBe 1
                    captured.first().severity shouldBe Severity.Warn
                    captured.first().message shouldBe "warn message"
                    captured.first().tag shouldBe "twitchkt/warn-tag"
                }
            }

            When("log is called with ERROR level") {
                val captured = setupCapture()
                val logger = KermitTwitchKtLogger()
                logger.log(LogLevel.ERROR, "error-tag") { "error message" }

                Then("it should delegate to Kermit with Error severity") {
                    captured.size shouldBe 1
                    captured.first().severity shouldBe Severity.Error
                    captured.first().message shouldBe "error message"
                    captured.first().tag shouldBe "twitchkt/error-tag"
                }
            }

            When("a custom tag prefix is used") {
                val captured = setupCapture()
                val logger = KermitTwitchKtLogger(tagPrefix = "custom")
                logger.log(LogLevel.INFO, "mytag") { "custom prefix" }

                Then("it should use the custom prefix in the tag") {
                    captured.first().tag shouldBe "custom/mytag"
                }
            }
        }
    })
