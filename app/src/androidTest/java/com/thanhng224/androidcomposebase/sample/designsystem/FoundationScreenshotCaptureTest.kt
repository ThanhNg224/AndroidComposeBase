package com.thanhng224.androidcomposebase.sample.designsystem

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppFloatingNavBar
import com.thanhng224.androidcomposebase.core.ui.components.AppNavItem
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
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
                            val density = LocalDensity.current
                            var barHeight by remember { mutableStateOf(0.dp) }
                            Box(modifier = Modifier.fillMaxSize()) {
                                DesignSystemScreen(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(bottom = barHeight)
                                            .consumeWindowInsets(PaddingValues(bottom = barHeight)),
                                )
                                AppFloatingNavBar(
                                    items =
                                        listOf(
                                            AppNavItem(
                                                false,
                                                {},
                                                Icons.Default.Home,
                                                Icons.Default.Home,
                                                stringResource(R.string.navigation_home),
                                            ),
                                            AppNavItem(
                                                true,
                                                {},
                                                Icons.Default.CheckCircle,
                                                Icons.Default.CheckCircle,
                                                stringResource(R.string.navigation_design),
                                            ),
                                            AppNavItem(
                                                false,
                                                {},
                                                Icons.Default.Settings,
                                                Icons.Default.Settings,
                                                stringResource(R.string.navigation_settings),
                                            ),
                                        ),
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .onSizeChanged { barHeight = with(density) { it.height.toDp() } }
                                            .navigationBarsPadding()
                                            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                                            .widthIn(max = Dimens.maxNavBarWidth),
                                )
                            }
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
        val bitmap =
            requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()) {
                "UiAutomation did not return a screenshot"
            }
        FileOutputStream(File(directory, "$name.png")).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write $name.png"
            }
        }
        bitmap.recycle()
    }
}
