package com.thanhng224.androidcomposebase.baselineprofile

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

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
        val getStarted = device.findObject(By.text("Get Started"))
        if (getStarted != null) {
            getStarted.click()
        }

        // Wait for Home screen
        device.wait(Until.hasObject(By.text("Home")), WAIT_TIMEOUT_MS)

        // Navigate to Demo
        device.clickOrFail(By.text("Demo"), "the Demo tab")
        check(device.wait(Until.hasObject(By.textContains("Ho Chi Minh City")), WAIT_TIMEOUT_MS)) {
            "Demo tab never showed weather card"
        }
        val refreshBtn = device.findObject(By.text("Refresh weather"))
        refreshBtn?.click()

        // Navigate to UI Kit (Design System)
        device.clickOrFail(By.text("Design"), "the Design tab")
        check(device.wait(Until.hasObject(By.text("Color Palette")), WAIT_TIMEOUT_MS)) {
            "Design system tab never rendered"
        }

        // Navigate to Settings
        device.clickOrFail(By.text("Settings"), "the Settings tab")
        check(device.wait(Until.hasObject(By.text("Appearance")), WAIT_TIMEOUT_MS)) {
            "Settings tab never rendered"
        }

        // Return to Home
        device.clickOrFail(By.text("Home"), "the Home tab")
        check(device.wait(Until.hasObject(By.text("Android Compose Base")), WAIT_TIMEOUT_MS)) {
            "Never returned to Home"
        }
    }
}
