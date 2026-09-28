package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Modal bottom sheet with managed dismissal lifecycle and constrained readable width.
 *
 * @param visible Authoritative visibility state owned by the caller.
 * @param onDismissRequest Callback invoked when user requests dismissal via back, scrim, or swipe.
 * @param modifier Root layout modifier.
 * @param dismissEnabled Whether user dismissal interactions are allowed.
 * @param sheetMaxWidth Maximum width constraint for readable layout on wider viewports.
 * @param dragHandle Optional custom drag handle composable; defaults to standard Material handle.
 * @param content Sheet body rendered inside [ColumnScope].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun AppModalBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissEnabled: Boolean = true,
    sheetMaxWidth: Dp = Dimens.maxReadableWidth,
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit,
) {
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = false,
            confirmValueChange = { targetValue ->
                if (!dismissEnabled && targetValue == SheetValue.Hidden) {
                    false
                } else {
                    true
                }
            },
        )
    var isComposed by remember { mutableStateOf(visible) }

    if (visible) {
        isComposed = true
    }

    if (isComposed) {
        ModalBottomSheet(
            onDismissRequest = {
                if (dismissEnabled) {
                    currentOnDismiss()
                }
            },
            modifier = modifier,
            sheetState = sheetState,
            sheetMaxWidth = sheetMaxWidth,
            dragHandle = dragHandle,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = content,
        )
    }

    LaunchedEffect(visible) {
        if (!visible && sheetState.isVisible) {
            sheetState.hide()
            isComposed = false
        }
    }
}
