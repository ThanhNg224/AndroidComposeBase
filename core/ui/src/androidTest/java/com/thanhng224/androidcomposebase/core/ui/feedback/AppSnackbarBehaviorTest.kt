package com.thanhng224.androidcomposebase.core.ui.feedback

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.core.text.UiText
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSnackbarBehaviorTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun appendingTailKeepsHeadVisibleAndAcknowledgesEachMatchingIdOnce() {
        val hostState = SnackbarHostState()
        val first = snackbarMessage(id = 1L, text = "First")
        val second = snackbarMessage(id = 2L, text = "Second")
        val messages = mutableStateOf(listOf(first))
        val acknowledgements = mutableListOf<Pair<Long, SnackbarResult>>()

        composeRule.setContent {
            AndroidComposeBaseTheme {
                Box {
                    AppSnackbarEffect(
                        message = messages.value.firstOrNull(),
                        hostState = hostState,
                        onResult = { id, result ->
                            acknowledgements += id to result
                            if (messages.value.firstOrNull()?.id == id) {
                                messages.value = messages.value.drop(1)
                            }
                        },
                    )
                    SnackbarHost(hostState = hostState, modifier = Modifier)
                }
            }
        }

        awaitVisible(hostState, "First")
        val firstData = hostState.currentSnackbarData
        composeRule.runOnIdle { messages.value = listOf(first, second) }
        composeRule.waitForIdle()
        assertSame(firstData, hostState.currentSnackbarData)
        assertEquals("First", hostState.currentSnackbarData?.visuals?.message)
        assertEquals(emptyList<Pair<Long, SnackbarResult>>(), acknowledgements)

        composeRule.runOnIdle { hostState.currentSnackbarData?.dismiss() }
        awaitVisible(hostState, "Second")
        assertEquals(listOf(1L to SnackbarResult.Dismissed), acknowledgements)

        composeRule.runOnIdle { hostState.currentSnackbarData?.dismiss() }
        composeRule.waitUntil { acknowledgements.size == 2 }
        assertEquals(
            listOf(1L to SnackbarResult.Dismissed, 2L to SnackbarResult.Dismissed),
            acknowledgements,
        )
    }

    @Test
    fun actionReportsActionPerformedAndUsesLatestCallback() {
        val hostState = SnackbarHostState()
        val message = snackbarMessage(id = 8L, text = "Undo", actionLabel = "Undo")
        val callback = mutableStateOf<(Long, SnackbarResult) -> Unit>({ _, _ -> })
        val oldResults = mutableListOf<Pair<Long, SnackbarResult>>()
        val latestResults = mutableListOf<Pair<Long, SnackbarResult>>()
        callback.value = { id, result -> oldResults += id to result }

        composeRule.setContent {
            AndroidComposeBaseTheme {
                Box {
                    AppSnackbarEffect(message = message, hostState = hostState, onResult = callback.value)
                    SnackbarHost(hostState = hostState)
                }
            }
        }

        awaitVisible(hostState, "Undo")
        composeRule.runOnIdle { callback.value = { id, result -> latestResults += id to result } }
        composeRule.waitForIdle()
        composeRule.runOnIdle { hostState.currentSnackbarData?.performAction() }
        composeRule.waitUntil { latestResults.isNotEmpty() }

        assertEquals(emptyList<Pair<Long, SnackbarResult>>(), oldResults)
        assertEquals(listOf(8L to SnackbarResult.ActionPerformed), latestResults)
    }

    @Test
    fun disposalCancelsWithoutAcknowledgingAndPendingHeadCanDisplayAgain() {
        val hostState = SnackbarHostState()
        val message = snackbarMessage(id = 12L, text = "Pending")
        val acknowledgements = mutableListOf<Pair<Long, SnackbarResult>>()
        val contentVisible = mutableStateOf(true)

        composeRule.setContent {
            if (contentVisible.value) {
                AndroidComposeBaseTheme {
                    Box {
                        AppSnackbarEffect(
                            message = message,
                            hostState = hostState,
                            onResult = { id, result -> acknowledgements += id to result },
                        )
                        SnackbarHost(hostState = hostState)
                    }
                }
            }
        }
        awaitVisible(hostState, "Pending")

        composeRule.runOnIdle { contentVisible.value = false }
        composeRule.waitForIdle()
        assertEquals(emptyList<Pair<Long, SnackbarResult>>(), acknowledgements)
        assertNull(hostState.currentSnackbarData)

        composeRule.runOnIdle { contentVisible.value = true }
        composeRule.waitForIdle()
        awaitVisible(hostState, "Pending")
        assertNotNull(hostState.currentSnackbarData)
        assertEquals(emptyList<Pair<Long, SnackbarResult>>(), acknowledgements)
    }

    private fun awaitVisible(
        hostState: SnackbarHostState,
        message: String,
    ) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            hostState.currentSnackbarData?.visuals?.message == message
        }
    }

    private fun snackbarMessage(
        id: Long,
        text: String,
        actionLabel: String? = null,
    ): AppSnackbarMessage =
        AppSnackbarMessage(
            id = id,
            text = UiText.DynamicString(text),
            actionLabel = actionLabel?.let { UiText.DynamicString(it) },
            duration = SnackbarDuration.Indefinite,
        )
}
