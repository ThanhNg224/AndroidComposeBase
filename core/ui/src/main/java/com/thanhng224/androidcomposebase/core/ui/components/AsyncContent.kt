package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

public sealed interface AsyncState<out T> {
    public data object Idle : AsyncState<Nothing>
    public data object Loading : AsyncState<Nothing>
    public data class Success<T>(val data: T) : AsyncState<T>
    public data class Error(val message: String, val throwable: Throwable? = null) : AsyncState<Nothing>
}

/**
 * Renders loading, error, and content states cleanly with animated crossfades.
 */
@Composable
public fun <T> AsyncContent(
    state: AsyncState<T>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    loadingContent: @Composable () -> Unit = { DefaultLoadingView() },
    errorContent: @Composable (AsyncState.Error) -> Unit = { error ->
        DefaultErrorView(
            message = error.message,
            onRetry = onRetry,
        )
    },
    successContent: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "async_content_state",
        modifier = modifier,
    ) { target ->
        when (target) {
            is AsyncState.Idle -> Box(Modifier.fillMaxSize())
            is AsyncState.Loading -> loadingContent()
            is AsyncState.Error -> errorContent(target)
            is AsyncState.Success -> successContent(target.data)
        }
    }
}

@Composable
public fun DefaultLoadingView(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.padding(Dimens.spaceXLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.spaceXXLarge),
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
public fun DefaultErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(Dimens.spaceLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimens.spaceXXXLarge),
            )
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
            Text(
                text = "Oops, something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.height(Dimens.buttonHeight),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(text = "Try Again")
                }
            }
        }
    }
}
