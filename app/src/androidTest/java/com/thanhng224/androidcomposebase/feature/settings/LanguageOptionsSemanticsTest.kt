package com.thanhng224.androidcomposebase.feature.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.feature.settings.presentation.ui.LanguageOptions
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageOptionsSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun languageRowsExposeSelectionAndWholeRowAction() {
        var selectedLanguage: AppLanguage? = null
        composeRule.setContent {
            MaterialTheme {
                LanguageOptions(
                    languages = AppLanguage.BUILT_IN,
                    selectedLanguage = AppLanguage.ENGLISH,
                    onLanguageSelected = { selectedLanguage = it },
                )
            }
        }

        composeRule.onAllNodesWithText("English").assertCountEquals(1)
        composeRule
            .onNodeWithText("English")
            .assertIsSelected()
            .assertHasClickAction()
        composeRule.onNodeWithText("Tiếng Việt").performClick()
        composeRule.runOnIdle { assertEquals(AppLanguage.VIETNAMESE, selectedLanguage) }
    }
}
