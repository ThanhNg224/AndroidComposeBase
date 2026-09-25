package com.thanhng224.androidcomposebase.sample

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.thanhng224.androidcomposebase.navigation.TopLevelDestination
import com.thanhng224.androidcomposebase.sample.demo.navigation.DemoDestination
import com.thanhng224.androidcomposebase.sample.demo.navigation.demoEntry
import com.thanhng224.androidcomposebase.sample.designsystem.navigation.DesignSystemDestination
import com.thanhng224.androidcomposebase.sample.designsystem.navigation.designSystemEntry

/**
 * The only sample file the app shell references. `scripts/init_project.py --clean-samples` deletes
 * `sample/` together with the `MainShell` references to it.
 */
val sampleTopLevelDestinations: List<TopLevelDestination> = listOf(DemoDestination, DesignSystemDestination)

fun EntryProviderScope<NavKey>.sampleEntries() {
    demoEntry()
    designSystemEntry()
}
