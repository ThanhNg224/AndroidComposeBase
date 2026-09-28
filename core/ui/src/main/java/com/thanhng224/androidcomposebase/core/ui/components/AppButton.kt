package com.thanhng224.androidcomposebase.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.core.ui.R
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Primary action button with a caller-owned width and at least a 48dp touch target.
 * [icon] is an optional composable slot placed before [text]. While [isLoading], the label stays
 * visible beside a progress indicator, the button is disabled, and its localized loading state is
 * exposed to accessibility services.
 */
@Composable
public fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
) {
    val loadingDescription = stringResource(R.string.core_ui_loading)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = MaterialTheme.shapes.medium,
        modifier =
            modifier
                .semantics { if (isLoading) stateDescription = loadingDescription }
                .defaultMinSize(minWidth = Dimens.minTouchTarget, minHeight = Dimens.minTouchTarget),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.iconSizeSmall),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        } else if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Tonal secondary action button with caller-owned width and an optional leading [icon] slot. */
@Composable
public fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier =
            modifier
                .defaultMinSize(minWidth = Dimens.minTouchTarget, minHeight = Dimens.minTouchTarget),
        colors =
            ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/** Outlined low-emphasis action with caller-owned width and an optional leading [icon] slot. */
@Composable
public fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier =
            modifier
                .defaultMinSize(minWidth = Dimens.minTouchTarget, minHeight = Dimens.minTouchTarget),
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppButtonsPreview() {
    AndroidComposeBaseTheme {
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
        ) {
            AppPrimaryButton(
                text = "Primary",
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(Dimens.iconSizeSmall)) },
                onClick = {},
            )
            AppSecondaryButton(text = "Secondary", onClick = {})
            AppOutlinedButton(text = "Outlined", onClick = {})
            AppPrimaryButton(text = "Loading", isLoading = true, onClick = {})
        }
    }
}
