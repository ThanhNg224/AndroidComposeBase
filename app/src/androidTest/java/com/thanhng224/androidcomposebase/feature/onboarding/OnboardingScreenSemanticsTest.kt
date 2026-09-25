package com.thanhng224.androidcomposebase.feature.onboarding

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingUiState
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.ui.OnboardingContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun continueActionIsAvailableWithoutExposingAppNavigation() {
        var continued = false
        val context =
            androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .targetContext
        composeRule.setContent {
            MaterialTheme {
                OnboardingContent(
                    state = OnboardingUiState(),
                    onContinue = { continued = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.onboarding_get_started)).performClick()
        composeRule.runOnIdle { assertTrue(continued) }
        composeRule.onAllNodesWithText(context.getString(R.string.navigation_home)).assertCountEquals(0)
    }
}
