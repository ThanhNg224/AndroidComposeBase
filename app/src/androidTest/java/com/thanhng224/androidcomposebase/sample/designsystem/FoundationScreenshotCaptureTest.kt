package com.thanhng224.androidcomposebase.sample.designsystem

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppFloatingNavBar
import com.thanhng224.androidcomposebase.core.ui.components.AppNavItem
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.presentation.FloatingNavigationLayout
import com.thanhng224.androidcomposebase.presentation.MainShell
import com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui.DesignSystemScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Manual visual-review fixture. Run this test to write real gallery screenshots under the
 * app's external files directory; these images are review artifacts, not golden assertions.
 * Pass `-e revision <git-sha>` to record the source revision in the metadata sidecar.
 */
@RunWith(AndroidJUnit4::class)
class FoundationScreenshotCaptureTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureStaticLightAndDarkGallery() {
        val outputDirectory =
            File(
                requireNotNull(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)),
                "foundation-captures",
            ).apply { mkdirs() }
        val revision = InstrumentationRegistry.getArguments().getString("revision") ?: "not-provided"

        listOf(false, true).forEach { darkTheme ->
            setWindowAppearance(darkTheme)
            composeRule.runOnUiThread {
                composeRule.activity.setContent {
                    AndroidComposeBaseTheme(darkTheme = darkTheme, dynamicColor = false) {
                        key(darkTheme) {
                            FloatingNavigationLayout(
                                content = { contentModifier -> DesignSystemScreen(modifier = contentModifier) },
                                navigation = {
                                    AppFloatingNavBar(
                                        items =
                                            listOf(
                                                AppNavItem(
                                                    "home",
                                                    false,
                                                    {},
                                                    Icons.Default.Home,
                                                    Icons.Default.Home,
                                                    stringResource(R.string.navigation_home),
                                                ),
                                                AppNavItem(
                                                    "design",
                                                    true,
                                                    {},
                                                    Icons.Default.CheckCircle,
                                                    Icons.Default.CheckCircle,
                                                    stringResource(R.string.navigation_design),
                                                ),
                                                AppNavItem(
                                                    "settings",
                                                    false,
                                                    {},
                                                    Icons.Default.Settings,
                                                    Icons.Default.Settings,
                                                    stringResource(R.string.navigation_settings),
                                                ),
                                            ),
                                        modifier =
                                            Modifier
                                                .navigationBarsPadding()
                                                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                                                .widthIn(max = Dimens.maxNavBarWidth),
                                    )
                                },
                            )
                        }
                    }
                }
            }
            composeRule.waitForIdle()
            val themeTag = if (darkTheme) "dark" else "light"
            capture(
                directory = outputDirectory,
                name = "foundation-$themeTag-overview",
            )

            val lazyList = composeRule.onNode(hasScrollAction())

            lazyList.performScrollToNode(hasText(composeRule.activity.getString(R.string.design_system_saving_button)))
            composeRule.waitForIdle()
            capture(
                directory = outputDirectory,
                name = "foundation-$themeTag-buttons",
            )

            lazyList.performScrollToNode(hasText(composeRule.activity.getString(R.string.design_system_states)))
            composeRule.waitForIdle()
            capture(
                directory = outputDirectory,
                name = "foundation-$themeTag-states-start",
            )

            lazyList.performScrollToNode(
                hasText(composeRule.activity.getString(R.string.design_system_states_empty_title)),
            )
            composeRule.waitForIdle()
            capture(
                directory = outputDirectory,
                name = "foundation-$themeTag-states-empty",
            )

            lazyList.performScrollToNode(
                hasText(composeRule.activity.getString(R.string.design_system_states_error_title)),
            )
            composeRule.waitForIdle()
            capture(
                directory = outputDirectory,
                name = "foundation-$themeTag-states-error",
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val displayMetrics = context.resources.displayMetrics
        File(outputDirectory, "metadata.txt").writeText(
            listOf(
                "revision=$revision",
                "apk=${context.packageManager.getPackageInfo(context.packageName, 0).versionName}",
                "deviceModel=${Build.MODEL}",
                "emulatorApi=${Build.VERSION.SDK_INT}",
                "viewportPx=${displayMetrics.widthPixels}x${displayMetrics.heightPixels}",
                "densityDpi=${displayMetrics.densityDpi}",
                "fontScale=${context.resources.configuration.fontScale}",
                "dynamicColor=false",
                "themes=static-light,static-dark",
            ).joinToString(separator = "\n", postfix = "\n"),
        )
        android.util.Log.i("FoundationCapture", "Screenshots: ${outputDirectory.absolutePath}")
    }

    @Test
    fun captureRailInSimulatedExpandedConfiguration() {
        val directory = File(requireNotNull(composeRule.activity.getExternalFilesDir(null)), "foundation-captures").apply { mkdirs() }
        listOf(false, true).forEach { darkTheme ->
            setWindowAppearance(darkTheme)
            composeRule.runOnUiThread {
                composeRule.activity.setContent {
                    val expanded =
                        Configuration(LocalConfiguration.current).apply {
                            screenWidthDp = 600
                            screenHeightDp = 800
                        }
                    CompositionLocalProvider(LocalConfiguration provides expanded) {
                        AndroidComposeBaseTheme(darkTheme = darkTheme, dynamicColor = false) {
                            MainShell()
                        }
                    }
                }
            }
            composeRule.waitForIdle()
            capture(directory, "rail-simulated-${if (darkTheme) "dark" else "light"}")
        }
    }

    private fun setWindowAppearance(darkTheme: Boolean) {
        composeRule.runOnUiThread {
            val window = composeRule.activity.window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    private fun capture(
        directory: File,
        name: String,
    ) {
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        FileOutputStream(File(directory, "$name.png")).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write $name.png"
            }
        }
        bitmap.recycle()
    }
}
