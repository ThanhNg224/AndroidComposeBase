package com.thanhng224.androidcomposebase.core.compose

import androidx.activity.ComponentActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thanhng224.androidcomposebase.core.ui.base.setThemedContent
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.thanhng224.androidcomposebase.core.R as CoreR

/**
 * The risk in this module is not its ~89 lines of glue but the module boundary: the theme bridge
 * reads `:core`'s `core_color_*` resources across an AAR boundary, and `setThemedContent` has to
 * compose inside a plain XML [ComposeView]. Both fail at runtime, not at compile time.
 */
@RunWith(AndroidJUnit4::class)
class ComposeInteropSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun explicitThemeChoiceUsesMatchingStaticPalette() {
        var lightPrimary: Color? = null
        var darkPrimary: Color? = null
        composeRule.setContent {
            AndroidComposeBaseTheme(darkTheme = false, dynamicColor = false) {
                lightPrimary = MaterialTheme.colorScheme.primary
            }
            AndroidComposeBaseTheme(darkTheme = true, dynamicColor = false) {
                darkPrimary = MaterialTheme.colorScheme.primary
            }
        }
        composeRule.waitForIdle()

        val expectedLight = Color(ContextCompat.getColor(composeRule.activity, CoreR.color.core_color_primary_light))
        val expectedDark = Color(ContextCompat.getColor(composeRule.activity, CoreR.color.core_color_primary_dark))
        assertEquals(expectedLight, lightPrimary)
        assertEquals(expectedDark, darkPrimary)
    }

    @Test
    fun explicitThemeOverridesTakePrecedenceAndReachMaterialTheme() {
        val overrideColorScheme = lightColorScheme(primary = Color.Magenta)
        val overrideTypography = Typography(bodyLarge = TextStyle(fontSize = 21.sp))
        val overrideShapes = Shapes(small = RoundedCornerShape(13.dp))
        var actualColorScheme: ColorScheme? = null
        var actualTypography: Typography? = null
        var actualShapes: Shapes? = null

        composeRule.setContent {
            AndroidComposeBaseTheme(
                darkTheme = false,
                dynamicColor = true,
                colorScheme = overrideColorScheme,
                typography = overrideTypography,
                shapes = overrideShapes,
            ) {
                actualColorScheme = MaterialTheme.colorScheme
                actualTypography = MaterialTheme.typography
                actualShapes = MaterialTheme.shapes
            }
        }
        composeRule.waitForIdle()

        assertEquals(Color.Magenta, actualColorScheme?.primary)
        assertEquals(21.sp, actualTypography?.bodyLarge?.fontSize)
        assertEquals(overrideShapes.small, actualShapes?.small)
    }

    @Test
    fun setThemedContentComposesInsideAComposeView() {
        composeRule.activity.runOnUiThread {
            val composeView = ComposeView(composeRule.activity)
            composeRule.activity.setContentView(composeView)
            composeView.setThemedContent {
                Text(text = "smoke", modifier = Modifier.testTag("smoke_content"))
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("smoke_content").assertExists()
    }

    @Test
    fun setThemedContentPropagatesThemeOverrides() {
        val overrideColorScheme = lightColorScheme(primary = Color.Cyan)
        val overrideTypography = Typography(bodyLarge = TextStyle(fontSize = 23.sp))
        val overrideShapes = Shapes(small = RoundedCornerShape(11.dp))
        var actualPrimary: Color? = null
        var actualBodyLargeSize = 0.sp
        var actualSmallShape: Shape? = null

        composeRule.activity.runOnUiThread {
            val composeView = ComposeView(composeRule.activity)
            composeRule.activity.setContentView(composeView)
            composeView.setThemedContent(
                darkTheme = false,
                dynamicColor = true,
                colorScheme = overrideColorScheme,
                typography = overrideTypography,
                shapes = overrideShapes,
            ) {
                actualPrimary = MaterialTheme.colorScheme.primary
                actualBodyLargeSize = MaterialTheme.typography.bodyLarge.fontSize
                actualSmallShape = MaterialTheme.shapes.small
                Text(text = "override", modifier = Modifier.testTag("override_content"))
            }
        }
        composeRule.waitForIdle()

        assertEquals(Color.Cyan, actualPrimary)
        assertEquals(23.sp, actualBodyLargeSize)
        assertEquals(overrideShapes.small, actualSmallShape)
        composeRule.onNodeWithTag("override_content").assertExists()
    }
}
