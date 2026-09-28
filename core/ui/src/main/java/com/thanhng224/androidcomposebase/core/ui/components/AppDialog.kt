package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Standard dialog enforcing the design system's constrained Surface container policy:
 * centered layout with 24dp horizontal clearance, 560dp maximum width cap, extra-large rounded
 * corners, surfaceContainerHigh background, and flat elevation.
 */
@Composable
public fun AppDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.spaceLarge),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = modifier.widthIn(max = Dimens.maxReadableWidth),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.spaceLarge),
                    content = content,
                )
            }
        }
    }
}
