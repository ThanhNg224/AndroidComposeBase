package com.thanhng224.androidcomposebase.core.ui.components

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Deterministic visual-review artifacts, not golden-image assertions. */
@RunWith(AndroidJUnit4::class)
class AppFloatingNavBarCaptureTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureLightDarkLargeFontBadgesAndRtl() {
        val preset = mutableStateOf(Preset())
        var labelColor = 0
        rule.setContent {
            val current = preset.value
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(base.density, current.fontScale),
                LocalLayoutDirection provides current.direction,
            ) {
                AndroidComposeBaseTheme(darkTheme = current.dark, dynamicColor = false) {
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
                    Surface(Modifier.requiredWidth(320.dp).testTag("fixture")) {
                        Box(Modifier.padding(16.dp)) {
                            AppFloatingNavBar(
                                (0 until 5).map { index ->
                                    AppNavItem(
                                        "tab-$index",
                                        index == 0,
                                        {},
                                        Icons.Default.Home,
                                        Icons.Default.Home,
                                        if (index == 0) "A very long destination name" else "Tab $index",
                                        badgeCount = if (index == 4) 100 else 0,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = File(requireNotNull(context.getExternalFilesDir(null)), "navigation-captures").apply { mkdirs() }
        for (dark in listOf(false, true)) {
            for (fontScale in listOf(1f, 2f)) {
                for (direction in LayoutDirection.entries) {
                    rule.runOnIdle { preset.value = Preset(dark, fontScale, direction) }
                    rule.waitForIdle()
                    rule
                        .onNode(
                            hasText("99+"),
                            useUnmergedTree = true,
                        ).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                            val layouts = mutableListOf<TextLayoutResult>()
                            check(action(layouts))
                            assertFalse("Badge must not clip at large font", layouts.single().didOverflowWidth)
                        }
                    rule
                        .onNode(hasText("A very long destination name"), useUnmergedTree = true)
                        .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                            val layouts = mutableListOf<TextLayoutResult>()
                            check(action(layouts))
                            val text = layouts.single()
                            check(text.isLineEllipsized(0)) { "Long labels must ellipsize" }
                        }
                    val name = "nav-${if (dark) "dark" else "light"}-font$fontScale-${direction.name}"
                    val bitmap = rule.onNodeWithTag("fixture").captureToImage().asAndroidBitmap()
                    val fixtureBounds = rule.onNodeWithTag("fixture").fetchSemanticsNode().boundsInRoot
                    val labelBounds =
                        rule
                            .onNode(hasText("A very long destination name"), useUnmergedTree = true)
                            .fetchSemanticsNode()
                            .boundsInRoot
                            .translate(-fixtureBounds.left, -fixtureBounds.top)
                    var foregroundPixels = 0
                    for (y in labelBounds.top.toInt() until labelBounds.bottom.toInt()) {
                        for (x in labelBounds.left.toInt() until labelBounds.right.toInt()) {
                            if (bitmap.getPixel(x, y) == labelColor) foregroundPixels++
                        }
                    }
                    assertTrue("Selected label must actually render: $name", foregroundPixels > 0)
                    File(output, "$name.png").outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                }
            }
        }
        File(
            output,
            "metadata.txt",
        ).writeText(
            "viewportDp=320\nfontScales=1.0,2.0\nthemes=light,dark\ndynamicColor=false\ndevice=${android.os.Build.MODEL}\napi=${android.os.Build.VERSION.SDK_INT}\n",
        )
    }

    private data class Preset(
        val dark: Boolean = false,
        val fontScale: Float = 1f,
        val direction: LayoutDirection = LayoutDirection.Ltr,
    )
}
