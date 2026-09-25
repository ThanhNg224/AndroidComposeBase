package com.thanhng224.androidcomposebase.core.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.thanhng224.androidcomposebase.core.text.UiText

/**
 * Resolves a [UiText] instance into a localized [String] in Composable scope.
 */
@Suppress("SpreadOperator")
@Composable
public fun UiText.asString(): String =
    when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(resId, *formatArgs.toTypedArray())
    }
