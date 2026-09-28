package com.thanhng224.androidcomposebase.core.ui.base

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable

/**
 * Base activity for screens rendered entirely in Jetpack Compose, enabling edge-to-edge layout
 * and establishing a single [setContent] call. [Content] owns theme rendering.
 */
public abstract class BaseComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Content()
        }
    }

    /** Subclasses render their Compose UI here. */
    @Composable
    protected abstract fun Content()
}
