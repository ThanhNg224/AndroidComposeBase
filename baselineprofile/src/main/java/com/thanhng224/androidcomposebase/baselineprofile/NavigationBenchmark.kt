package com.thanhng224.androidcomposebase.baselineprofile

import android.os.SystemClock
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMacrobenchmarkApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

/** Warm tab transitions only. Compilation is set externally; app data is never reset here. */
@OptIn(ExperimentalMacrobenchmarkApi::class)
@RunWith(AndroidJUnit4::class)
class NavigationBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun homeSettings() = tabSwitches("Settings|Cài đặt", "Appearance|Giao diện")

    @Test
    fun homeDesign() = tabSwitches("Design|Thiết kế", "Color roles|Vai trò màu sắc")

    private fun tabSwitches(tab: String, heading: String) {
        benchmarkRule.measureRepeated(
            packageName = "com.thanhng224.androidcomposebase",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Ignore(),
            iterations = 10,
            setupBlock = {
                startActivityAndWait()
                device.findObject(By.text(Pattern.compile("Get Started|Bắt đầu")))?.click()
                check(device.wait(Until.hasObject(By.text(Pattern.compile("Welcome to your new app|Chào mừng đến với ứng dụng mới"))), 15_000))
                switchTab(tab, heading)
                switchTab("Home|Trang chủ", "Welcome to your new app|Chào mừng đến với ứng dụng mới")
            },
        ) {
            repeat(2) {
                switchTab(tab, heading)
                switchTab("Home|Trang chủ", "Welcome to your new app|Chào mừng đến với ứng dụng mới")
            }
        }
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.switchTab(tab: String, heading: String) {
        val selector = By.desc(Pattern.compile(tab))
        check(device.wait(Until.hasObject(selector), 5_000)) { "Missing tab: $tab" }
        checkNotNull(device.findObject(selector)).click()
        check(device.wait(Until.hasObject(By.text(Pattern.compile(heading))), 5_000)) { "Missing destination: $heading" }
        device.waitForIdle()
        // Accessibility becomes idle before spring rendering settles. The fixed settling window is
        // constant across runs, and idle time adds no frames to FrameTimingMetric's distribution.
        SystemClock.sleep(700)
    }
}
