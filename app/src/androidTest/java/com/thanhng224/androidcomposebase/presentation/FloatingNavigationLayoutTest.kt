package com.thanhng224.androidcomposebase.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FloatingNavigationLayoutTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun reservesCompleteBarOnFirstLayoutAndConsumesInsetsWithoutDoublePadding() {
        val visible = mutableStateOf(true)
        val height = mutableStateOf(48.dp)
        val insetHeight = mutableStateOf(24)
        val placements = mutableListOf<Int>()
        val density = rule.activity.resources.displayMetrics.density
        rule.setContent {
            val insets = WindowInsets(bottom = insetHeight.value)
            FloatingNavigationLayout(
                modifier = Modifier.requiredSize(320.dp, 320.dp),
                navigation = {
                    if (visible.value) Box(Modifier.fillMaxWidth().windowInsetsPadding(insets).height(height.value))
                },
                content = { contentModifier ->
                    Box(contentModifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize().windowInsetsPadding(insets)) {
                            Box(Modifier.fillMaxSize().onGloballyPositioned { placements += it.size.height })
                        }
                    }
                },
            )
        }

        fun expected(
            barDp: Int,
            insetPx: Int,
        ) = (320 * density).toInt() - (barDp * density).toInt() - insetPx
        rule.runOnIdle {
            assertTrue(placements.isNotEmpty())
            placements.forEach { assertEquals(expected(48, 24), it) }
            placements.clear()
            height.value = 80.dp
            insetHeight.value = 40
        }
        rule.runOnIdle {
            assertTrue(placements.isNotEmpty())
            placements.forEach { assertEquals(expected(80, 40), it) }
            placements.clear()
            visible.value = false
        }
        rule.runOnIdle {
            assertTrue(placements.isNotEmpty())
            placements.forEach { assertEquals(expected(0, 40), it) }
        }
    }
}
