package com.thanhng224.androidcomposebase.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.thanhng224.androidcomposebase.core.ui.R
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Loading placeholder for async screen content. The caller controls its size. When [message]
 * changes it is announced to accessibility services as a polite live region.
 */
@Composable
public fun AppLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier.padding(Dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        if (message != null) {
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

/**
 * Placeholder for a screen or section with no content, with an optional illustrative [icon] and
 * a caller-supplied [action], such as a button to create content. The caller controls its size.
 */
@Composable
public fun AppEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(Dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimens.iconSizeLarge),
            )
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (action != null) {
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
            action()
        }
    }
}

/**
 * Placeholder for a failed screen or section, with an optional [onRetry] action rendered as an
 * [AppPrimaryButton]. [action], when supplied, replaces the retry convenience.
 * This component does not impose a size; the caller owns its available width and height.
 */
@Composable
public fun AppErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(Dimens.spaceLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(Dimens.iconSizeLarge),
        )
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (action != null || onRetry != null) {
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
            if (action != null) {
                action()
            } else {
                onRetry?.let { retry ->
                    AppPrimaryButton(
                        text = stringResource(R.string.core_ui_retry),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = retry,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppLoadingStatePreview() {
    AndroidComposeBaseTheme {
        AppLoadingState(message = "Loading your data…")
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppEmptyStatePreview() {
    AndroidComposeBaseTheme {
        AppEmptyState(
            title = "Nothing here yet",
            message = "Items you add will show up in this list.",
            action = { AppPrimaryButton(text = "Add item", onClick = {}) },
        )
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppErrorStatePreview() {
    AndroidComposeBaseTheme {
        AppErrorState(
            title = "Something went wrong",
            message = "Please check your connection and try again.",
            onRetry = {},
        )
    }
}
