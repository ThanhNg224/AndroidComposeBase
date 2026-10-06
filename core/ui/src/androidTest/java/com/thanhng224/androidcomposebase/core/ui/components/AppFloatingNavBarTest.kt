package com.thanhng224.androidcomposebase.core.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.core.ui.R
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFloatingNavBarTest {
    private val motionScale =
        object : MotionDurationScale {
            override var scaleFactor = 1f
        }

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>(effectContext = motionScale)

    private val tabRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    @Test
    fun oneNamedNodePerTabAndExactlyOneCallbackAndHapticOnChange() {
        val selected = mutableIntStateOf(0)
        var clicks = 0
        var haptics = 0
        val haptic =
            object : HapticFeedback {
                override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                    haptics++
                }
            }
        rule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides haptic) {
                AndroidComposeBaseTheme(dynamicColor = false) {
                    AppFloatingNavBar(
                        items(3, selected.intValue) {
                            selected.intValue = it
                            clicks++
                        },
                    )
                }
            }
        }
        rule.onAllNodes(tabRole).assertCountEquals(3)
        rule.onAllNodesWithContentDescription(label(1)).assertCountEquals(1)
        rule.onAllNodes(hasText(label(1))).assertCountEquals(0)
        rule.onNodeWithContentDescription(label(0)).performClick()
        assertEquals(0, clicks)
        assertEquals(0, haptics)
        rule
            .onNodeWithContentDescription(label(1))
            .performClick()
            .assertIsSelected()
            .performClick()
        assertEquals(1, clicks)
        assertEquals(1, haptics)
    }

    @Test
    fun badgeCountAndCallerNameAreExposedOnceWithoutBadgeClickTargets() {
        val count = mutableIntStateOf(0)
        rule.setContent {
            AndroidComposeBaseTheme(dynamicColor = false) {
                AppFloatingNavBar(
                    items(3, 0).mapIndexed { index, item ->
                        if (index == 0) item.copy(badgeCount = count.intValue, contentDescription = "Inbox") else item
                    },
                )
            }
        }
        listOf(0, 1, 99, 100).forEach { value ->
            rule.runOnIdle { count.intValue = value }
            val name =
                if (value > 0) {
                    "Inbox, " + rule.activity.resources.getQuantityString(R.plurals.core_ui_nav_badge_count, value, value)
                } else {
                    "Inbox"
                }
            rule.onAllNodesWithContentDescription(name, useUnmergedTree = true).assertCountEquals(1)
            rule.onAllNodes(tabRole).assertCountEquals(3)
            // The platform selected announcement is preserved rather than replaced by the badge.
            rule.onNodeWithContentDescription(name).assertIsSelected()
            rule.onNodeWithContentDescription(name).assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription))
        }
    }

    @Test
    fun narrowLargeFontAndRtlKeepEveryTabReachableDuringInterruptedAnimations() {
        val config = mutableStateOf(NavConfig())
        val selected = mutableIntStateOf(0)
        var clickCount = 0
        rule.setContent {
            val current = config.value
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(baseDensity.density, current.fontScale),
                LocalLayoutDirection provides current.direction,
            ) {
                AndroidComposeBaseTheme(dynamicColor = false) {
                    Box(Modifier.requiredWidth(current.width.dp).padding(horizontal = 16.dp)) {
                        AppFloatingNavBar(
                            items(current.count, selected.intValue) {
                                selected.intValue = it
                                clickCount++
                            },
                        )
                    }
                }
            }
        }
        val density = rule.activity.resources.displayMetrics.density
        for (width in listOf(320, 360, 412)) {
            for (count in 3..5) {
                for (fontScale in listOf(1f, 1.5f, 2f)) {
                    for (direction in LayoutDirection.entries) {
                        rule.runOnIdle {
                            config.value = NavConfig(width, count, fontScale, direction)
                            selected.intValue = 0
                        }
                        rule.waitForIdle()
                        assertBounds(count, width, density)
                        rule.mainClock.autoAdvance = false
                        rule.onNodeWithContentDescription(label(count - 1)).performClick()
                        rule.mainClock.advanceTimeBy(80)
                        assertBounds(count, width, density)
                        rule.onNodeWithContentDescription(label(1)).performClick()
                        rule.mainClock.advanceTimeBy(80)
                        assertBounds(count, width, density)
                        rule.onNodeWithContentDescription(label(0)).performClick()
                        rule.mainClock.advanceTimeBy(1_500)
                        rule.mainClock.autoAdvance = true
                        rule.onNodeWithContentDescription(label(0)).assertIsSelected()
                    }
                }
            }
        }
        assertEquals(54 * 3, clickCount)
    }

    @Test
    fun widthBelowMinimumTouchTargetsShrinksEvenlyWithoutCrashing() {
        rule.setContent {
            AndroidComposeBaseTheme(dynamicColor = false) {
                Box(Modifier.requiredWidth(160.dp)) { AppFloatingNavBar(items(5, 0)) }
            }
        }
        val bounds =
            (0 until 5)
                .map { rule.onNodeWithContentDescription(label(it)).fetchSemanticsNode().boundsInRoot }
                .sortedBy { it.left }
        bounds.zipWithNext().forEach { (left, right) -> assertTrue("Overlapping tabs: $left / $right", left.right <= right.left + 1) }
        assertTrue(bounds.all { it.width > 0f })
    }

    @Test
    fun keyboardActivationAndZeroMotionScaleEndOnTheLatestSelection() {
        val selected = mutableIntStateOf(0)
        var clicks = 0
        lateinit var inputModeManager: InputModeManager
        rule.setContent {
            inputModeManager = LocalInputModeManager.current
            AndroidComposeBaseTheme(dynamicColor = false) {
                AppFloatingNavBar(
                    items(3, selected.intValue) {
                        selected.intValue = it
                        clicks++
                    },
                )
            }
        }
        val second = rule.onNodeWithContentDescription(label(1))
        rule.runOnIdle { assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard)) }
        rule.onNodeWithContentDescription(label(0)).performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        rule.onNodeWithContentDescription(label(0)).assertIsFocused().performKeyInput { pressKey(Key.Tab) }
        second.assertIsFocused().performKeyInput { pressKey(Key.Enter) }.assertIsSelected()
        assertEquals(1, clicks)
        rule.runOnIdle {
            motionScale.scaleFactor = 0f
            selected.intValue = 2
        }
        rule.mainClock.advanceTimeByFrame()
        rule.onNodeWithContentDescription(label(2)).assertIsSelected()
        assertBounds(3, 412, rule.activity.resources.displayMetrics.density)
    }

    private fun assertBounds(
        count: Int,
        width: Int,
        density: Float,
    ) {
        val bounds =
            (0 until count)
                .map { index ->
                    val node = rule.onNodeWithContentDescription(label(index)).fetchSemanticsNode()
                    Rect(node.positionInRoot, Size(node.size.width.toFloat(), node.size.height.toFloat()))
                }.sortedBy { it.left }
        bounds.forEach {
            assertTrue("Touch width: $it", it.width >= 48 * density - 1)
            assertTrue("Touch height: $it", it.height >= 48 * density - 1)
        }
        bounds.zipWithNext().forEach { (left, right) -> assertTrue("Overlapping tabs: $left / $right", left.right <= right.left + 1) }
        assertTrue("Tabs exceed viewport", bounds.last().right - bounds.first().left <= width * density)
    }

    private data class NavConfig(
        val width: Int = 320,
        val count: Int = 3,
        val fontScale: Float = 1f,
        val direction: LayoutDirection = LayoutDirection.Ltr,
    )

    private fun label(index: Int) = "Tab $index with a very long translated destination name"

    private fun items(
        count: Int,
        selected: Int,
        onClick: (Int) -> Unit = {},
    ) = (0 until count).map { index ->
        AppNavItem("tab-$index", index == selected, { onClick(index) }, Icons.Default.Home, Icons.Default.Home, label(index))
    }
}
