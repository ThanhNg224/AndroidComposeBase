package com.thanhng224.androidcomposebase.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Standard content container: a Material 3 card on the `surfaceContainerLow` tonal role with no
 * drop shadow, so it stays distinct from the background in dark theme, where shadows are
 * invisible. It applies no inner padding; callers pad [content] (typically [Dimens.spaceMedium]
 * or [Dimens.spaceLarge]).
 *
 * Pass [onClick] to make the whole card one clickable target (Material 3 `Card(onClick = ...)`).
 */
@Composable
public fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.large
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    val elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationNone)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            content = content,
        )
    }
}

@Preview(name = "AppCard light", showBackground = true)
@Preview(name = "AppCard dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppCardPreview() {
    AndroidComposeBaseTheme {
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Static card",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(Dimens.spaceMedium),
                )
            }
            AppCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                Text(
                    text = "Clickable card",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(Dimens.spaceMedium),
                )
            }
        }
    }
}
