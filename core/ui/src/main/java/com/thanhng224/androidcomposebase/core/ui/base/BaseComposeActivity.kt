package com.thanhng224.androidcomposebase.core.ui.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme

/**
 * Base activity for screens rendered entirely in Jetpack Compose, wrapping [Content] in
 * [AndroidComposeBaseTheme] and enabling edge-to-edge layout.
 */
public abstract class BaseComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AndroidComposeBaseTheme {
                Content()
            }
        }
    }

    /** Subclasses render their Compose UI here. */
    @Composable
    protected abstract fun Content()
}
