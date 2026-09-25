package com.thanhng224.androidcomposebase.core.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * [AppErrorState]'s retry action is a behavior and accessibility contract -- it must expose a
 * clickable node the user can find by its label, and invoking it must call [AppErrorState]'s
 * `onRetry` lambda. This is not a layout/styling assertion, so it belongs in this suite.
 */
@RunWith(AndroidJUnit4::class)
class AppStatesSemanticsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun errorStateRetryActionIsClickableAndInvokesCallback() {
        var retried = false
        composeRule.setContent {
            AndroidComposeBaseTheme {
                AppErrorState(
                    title = "Something went wrong",
                    onRetry = { retried = true },
                )
            }
        }

        composeRule
            .onNodeWithText("Retry")
            .assertHasClickAction()
            .performClick()

        assertTrue(retried)
    }
}
