package com.thanhng224.androidcomposebase.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavEntriesLifecycleTest {
    @get:Rule
    val composeRule = createComposeRule()

    private data object TabA : NavKey

    private data object TabB : NavKey

    private data object Detail : NavKey

    private class TrackedViewModel : ViewModel() {
        var cleared = false
            private set

        override fun onCleared() {
            cleared = true
        }
    }

    @Test
    fun entryViewModelSurvivesTabSwitchesAndIsClearedWhenItsEntryIsPopped() {
        val navigator = AppNavigator(startKey = TabA, backStacks = listOf(TabA, TabB).associateWith { NavBackStack(it) })
        val tabBViewModels = mutableListOf<TrackedViewModel>()
        val detailViewModels = mutableListOf<TrackedViewModel>()
        composeRule.setContent {
            val entries =
                rememberAppNavEntries(
                    navigator = navigator,
                    entryProvider =
                        entryProvider {
                            entry<TabA> { Text("Tab A") }
                            entry<TabB> { TrackedScreen("Tab B", tabBViewModels) }
                            entry<Detail> { TrackedScreen("Detail", detailViewModels) }
                        },
                )
            NavDisplay(entries = entries, onBack = { navigator.goBack() })
        }

        composeRule.runOnIdle { navigator.navigate(TabB) }
        composeRule.onNodeWithText("Tab B").assertExists()
        composeRule.runOnIdle { navigator.navigate(Detail) }
        composeRule.onNodeWithText("Detail").assertExists()

        // Switch away: the Detail entry leaves the display but stays on Tab B's stack.
        composeRule.runOnIdle { navigator.navigate(TabA) }
        composeRule.onNodeWithText("Tab A").assertExists()
        composeRule.onNodeWithText("Detail").assertDoesNotExist()
        composeRule.runOnIdle {
            assertFalse("hidden tab entry must keep its ViewModel", detailViewModels.single().cleared)
            assertFalse(tabBViewModels.single().cleared)
        }

        // Switch back: the same ViewModel instance is reused.
        composeRule.runOnIdle { navigator.navigate(TabB) }
        composeRule.onNodeWithText("Detail").assertExists()
        composeRule.runOnIdle { assertEquals(1, detailViewModels.size) }

        // Pop Detail off Tab B: its ViewModel is cleared, Tab B's root ViewModel is not.
        val detailViewModel = detailViewModels.single()
        composeRule.runOnIdle { assertTrue(navigator.goBack()) }
        composeRule.onNodeWithText("Tab B").assertExists()
        composeRule.waitUntil { detailViewModel.cleared }
        composeRule.runOnIdle {
            assertSame(tabBViewModels.first(), tabBViewModels.single())
            assertFalse(tabBViewModels.single().cleared)
        }
    }

    @Composable
    private fun TrackedScreen(
        label: String,
        instances: MutableList<TrackedViewModel>,
    ) {
        val viewModel = viewModel { TrackedViewModel() }
        SideEffect { if (instances.none { it === viewModel }) instances += viewModel }
        Text(label)
    }
}
