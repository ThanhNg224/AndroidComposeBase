package com.thanhng224.androidcomposebase.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.thanhng224.androidcomposebase.appshell.home.HomeRoute
import com.thanhng224.androidcomposebase.feature.settings.navigation.SettingsRoute
import com.thanhng224.androidcomposebase.sample.demo.navigation.DemoRoute
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {
    @Serializable
    private data object Detail : NavKey

    private fun createNavigator(): AppNavigator =
        AppNavigator(
            startKey = HomeRoute,
            backStacks = listOf(HomeRoute, DemoRoute, SettingsRoute).associateWith { NavBackStack(it) },
        )

    @Test
    fun `starts on the start tab showing only its root`() {
        val navigator = createNavigator()

        assertEquals(HomeRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute), navigator.visibleTopLevelKeys)
        assertEquals(listOf(HomeRoute), navigator.visibleEntries)
        assertTrue(navigator.isOnTopLevelRoot)
    }

    @Test
    fun `navigating to a top-level key switches tab and shows it above the start stack`() {
        val navigator = createNavigator()

        navigator.navigate(SettingsRoute)

        assertEquals(SettingsRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute, SettingsRoute), navigator.visibleTopLevelKeys)
        assertEquals(listOf(HomeRoute, SettingsRoute), navigator.visibleEntries)
        assertTrue(navigator.isOnTopLevelRoot)
    }

    @Test
    fun `pushing a non-top-level key stacks it on the current tab and leaves the tab root`() {
        val navigator = createNavigator()
        navigator.navigate(SettingsRoute)

        navigator.navigate(Detail)

        assertEquals(SettingsRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute, SettingsRoute, Detail), navigator.visibleEntries)
        assertFalse(navigator.isOnTopLevelRoot)
    }

    @Test
    fun `switching tabs keeps each tab's own stack`() {
        val navigator = createNavigator()
        navigator.navigate(SettingsRoute)
        navigator.navigate(Detail)

        navigator.navigate(DemoRoute)

        assertEquals(listOf(HomeRoute, DemoRoute), navigator.visibleEntries)
        assertTrue(navigator.isOnTopLevelRoot)

        navigator.navigate(SettingsRoute)

        assertEquals(listOf(HomeRoute, SettingsRoute, Detail), navigator.visibleEntries)
        assertFalse(navigator.isOnTopLevelRoot)
    }

    @Test
    fun `navigating to the current tab is a no-op`() {
        val navigator = createNavigator()
        navigator.navigate(SettingsRoute)

        navigator.navigate(SettingsRoute)
        navigator.navigate(HomeRoute)
        navigator.navigate(HomeRoute)

        assertEquals(HomeRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute), navigator.visibleEntries)
    }

    @Test
    fun `going back pops the current tab stack first`() {
        val navigator = createNavigator()
        navigator.navigate(SettingsRoute)
        navigator.navigate(Detail)

        assertTrue(navigator.goBack())

        assertEquals(SettingsRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute, SettingsRoute), navigator.visibleEntries)
        assertTrue(navigator.isOnTopLevelRoot)
    }

    @Test
    fun `going back on a non-start tab root returns to the start tab`() {
        val navigator = createNavigator()
        navigator.navigate(SettingsRoute)

        assertTrue(navigator.goBack())

        assertEquals(HomeRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute), navigator.visibleEntries)
    }

    @Test
    fun `going back on the start tab root is left to the system`() {
        val navigator = createNavigator()

        assertFalse(navigator.goBack())

        assertEquals(HomeRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute), navigator.visibleEntries)
    }

    @Test
    fun `going back on the start tab pops its pushed keys before leaving`() {
        val navigator = createNavigator()
        navigator.navigate(Detail)

        assertEquals(listOf(HomeRoute, Detail), navigator.visibleEntries)
        assertTrue(navigator.goBack())
        assertEquals(listOf(HomeRoute), navigator.visibleEntries)
        assertFalse(navigator.goBack())
    }

    @Test
    fun `restores the current tab from the provided state`() {
        val navigator =
            AppNavigator(
                startKey = HomeRoute,
                backStacks = listOf(HomeRoute, SettingsRoute).associateWith { NavBackStack(it) },
                topLevelKeyState = mutableStateOf(SettingsRoute),
            )

        assertEquals(SettingsRoute, navigator.currentTopLevelKey)
        assertEquals(listOf(HomeRoute, SettingsRoute), navigator.visibleEntries)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a start key without a back stack`() {
        AppNavigator(startKey = HomeRoute, backStacks = mapOf(SettingsRoute to NavBackStack(SettingsRoute)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a back stack that does not start with its top-level key`() {
        AppNavigator(
            startKey = HomeRoute,
            backStacks = mapOf(HomeRoute to NavBackStack(HomeRoute), SettingsRoute to NavBackStack(DemoRoute)),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects a restored current tab that is not a top-level key`() {
        AppNavigator(
            startKey = HomeRoute,
            backStacks = mapOf(HomeRoute to NavBackStack(HomeRoute)),
            topLevelKeyState = mutableStateOf(Detail),
        )
    }
}
