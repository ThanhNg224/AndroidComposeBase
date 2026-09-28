@file:Suppress("MatchingDeclarationName")

package com.thanhng224.androidcomposebase.core.ui.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import com.thanhng224.androidcomposebase.core.text.UiText
import com.thanhng224.androidcomposebase.core.ui.text.asString

/**
 * Immutable presentation data for one snackbar display attempt.
 *
 * Keep the content associated with an [id] stable for that attempt. Callers own message queues and
 * decide what each [SnackbarResult] means.
 */
public data class AppSnackbarMessage(
    val id: Long,
    val text: UiText,
    val actionLabel: UiText? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val withDismissAction: Boolean = false,
)

/**
 * Presents the caller's current queue head through [hostState] and reports its result once.
 *
 * The caller should place a Material [androidx.compose.material3.SnackbarHost] with the same
 * [hostState] in its Scaffold. This effect does not add insets or own a host. Changing a queue
 * tail leaves the current ID unchanged, so it does not restart the active snackbar. Leaving
 * composition cancels presentation without acknowledging the head; a still-pending message may be
 * shown again when the caller re-enters composition.
 */
@Composable
public fun AppSnackbarEffect(
    message: AppSnackbarMessage?,
    hostState: SnackbarHostState,
    onResult: (Long, SnackbarResult) -> Unit,
) {
    val currentOnResult = rememberUpdatedState(onResult)
    val text = message?.text?.asString()
    val actionLabel = message?.actionLabel?.asString()

    LaunchedEffect(hostState, message?.id) {
        val currentMessage = message ?: return@LaunchedEffect
        val result =
            hostState.showSnackbar(
                message = checkNotNull(text),
                actionLabel = actionLabel,
                withDismissAction = currentMessage.withDismissAction,
                duration = currentMessage.duration,
            )
        currentOnResult.value(currentMessage.id, result)
    }
}
