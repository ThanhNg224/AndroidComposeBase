package com.thanhng224.androidcomposebase.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.appshell.home.HomeDestination
import com.thanhng224.androidcomposebase.appshell.home.HomeRoute
import com.thanhng224.androidcomposebase.feature.settings.navigation.SettingsDestination
import com.thanhng224.androidcomposebase.feature.settings.navigation.SettingsRoute
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigatorRestorationTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Serializable
    private data object Detail : NavKey

    @Test
    fun restoresSelectedTabItsStackAndSaveableEntryState() {
        val restoration = StateRestorationTester(rule)
        lateinit var navigator: AppNavigator
        restoration.setContent {
            val current = rememberAppNavigator(HomeRoute, listOf(HomeDestination, SettingsDestination))
            SideEffect { navigator = current }
            val entries =
                rememberAppNavEntries(
                    current,
                    entryProvider {
                        entry<HomeRoute> { Text("Home fixture") }
                        entry<SettingsRoute> {
                            val count = rememberSaveable { mutableIntStateOf(0) }
                            Button(onClick = { count.intValue++ }) { Text("Count ${count.intValue}") }
                        }
                        entry<Detail> { Text("Detail fixture") }
                    },
                )
            NavDisplay(entries, onBack = { current.goBack() })
        }
        rule.runOnIdle { navigator.navigate(SettingsRoute) }
        rule.onNodeWithText("Count 0").performClick()
        rule.runOnIdle { navigator.navigate(Detail) }
        restoration.emulateSavedInstanceStateRestore()
        rule.runOnIdle {
            assertEquals(SettingsRoute, navigator.currentTopLevelKey)
            assertEquals(listOf(SettingsRoute, Detail), navigator.tabStacks.getValue(SettingsRoute))
            navigator.goBack()
        }
        rule.onNodeWithText("Count 1").assertExists()
    }
}
