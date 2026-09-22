package com.thanhng224.androidcomposebase.core.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme

/**
 * Sets [content] on this [ComposeView], wrapped in [AndroidComposeBaseTheme] and configured with
 * [ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed] for XML embedding.
 */
public fun ComposeView.setThemedContent(content: @Composable () -> Unit) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        AndroidComposeBaseTheme(content)
    }
}
