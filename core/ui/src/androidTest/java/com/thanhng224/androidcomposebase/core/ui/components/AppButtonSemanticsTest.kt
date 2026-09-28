package com.thanhng224.androidcomposebase.core.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.core.ui.R
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppButtonSemanticsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadingButtonKeepsItsLabelAndExposesLoadingStateWhileBlockingClicks() {
        var clickCount = 0
        composeRule.setContent {
            AndroidComposeBaseTheme(dynamicColor = false) {
                AppPrimaryButton(text = "Save changes", isLoading = true, onClick = { clickCount++ })
            }
        }

        val button = composeRule.onNodeWithText("Save changes")
        val expectedLoadingDescription = composeRule.activity.getString(R.string.core_ui_loading)
        button
            .assertTextEquals("Save changes")
            .assertIsNotEnabled()
            .assert(
                SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expectedLoadingDescription),
            ).performClick()

        composeRule.runOnIdle { assertEquals(0, clickCount) }
    }

    @Test
    fun enabledButtonKeepsItsClickAction() {
        var clicked = false
        composeRule.setContent {
            AndroidComposeBaseTheme(dynamicColor = false) {
                AppPrimaryButton(text = "Continue", onClick = { clicked = true })
            }
        }

        composeRule.onNodeWithText("Continue").assertHasClickAction().performClick()

        composeRule.runOnIdle { assertEquals(true, clicked) }
    }
}
