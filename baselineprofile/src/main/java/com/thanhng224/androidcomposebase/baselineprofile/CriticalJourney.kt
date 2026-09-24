package com.thanhng224.androidcomposebase.baselineprofile

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

// Profile collection runs against a non-minified, uncompiled build, and CI collects on an
// emulator that is slower again than a physical device. Waits return as soon as the node appears,
// so a generous ceiling costs nothing on fast hardware and prevents flakes on slow hardware.
private const val WAIT_TIMEOUT_MS = 15_000L

/**
 * The one critical journey both [BaselineProfileGenerator] (profile collection) and
 * [StartupBenchmark] (measured None-vs-Partial comparison) exercise, so the profile actually
 * covers the code paths the benchmark measures. Every step waits for a resource this starter app
 * itself owns before moving on, rather than a fixed delay.
 */
internal object CriticalJourney {
    private fun textSelector(vararg labels: String): BySelector =
        By.text(Pattern.compile(labels.joinToString("|") { Pattern.quote(it) }))

    /**
     * `findObject(...)?.click()` silently does nothing when the node is missing, which then
     * surfaces as a misleading failure on the *next* `check`. Fail at the actual missing step.
     */
    private fun UiDevice.clickOrFail(
        selector: BySelector,
        what: String,
    ) {
        val node = checkNotNull(findObject(selector)) { "Could not find $what" }
        node.click()
    }

    fun execute(
        device: UiDevice,
        packageName: String,
    ) {
        // Handle onboarding if displayed on first launch
        val getStarted = device.findObject(textSelector("Get Started", "Bắt đầu"))
        if (getStarted != null) {
            getStarted.click()
        }

        // Wait for Home screen
        check(device.wait(Until.hasObject(textSelector("Home", "Trang chủ")), WAIT_TIMEOUT_MS)) {
            "Home did not appear"
        }

        // Navigate to the offline weather example
        device.clickOrFail(textSelector("Demo"), "the Demo tab")
        check(
            device.wait(
                Until.hasObject(textSelector("Current conditions", "Thời tiết hiện tại")),
                WAIT_TIMEOUT_MS,
            ),
        ) {
            "Demo tab never showed weather card"
        }
        device.clickOrFail(textSelector("Refresh weather", "Làm mới thời tiết"), "the weather refresh button")

        // Navigate to the UI Kit
        device.clickOrFail(textSelector("Design", "Thiết kế"), "the Design tab")
        check(
            device.wait(Until.hasObject(textSelector("Color roles", "Vai trò màu sắc")), WAIT_TIMEOUT_MS),
        ) {
            "Design system tab never rendered"
        }

        // Navigate to Settings
        device.clickOrFail(textSelector("Settings", "Cài đặt"), "the Settings tab")
        check(device.wait(Until.hasObject(textSelector("Appearance", "Giao diện")), WAIT_TIMEOUT_MS)) {
            "Settings tab never rendered"
        }

        // Return to Home
        device.clickOrFail(textSelector("Home", "Trang chủ"), "the Home tab")
        check(
            device.wait(
                Until.hasObject(textSelector("Welcome to your new app", "Chào mừng đến với ứng dụng mới")),
                WAIT_TIMEOUT_MS,
            ),
        ) {
            "Never returned to Home"
        }
    }
}
