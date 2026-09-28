package com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.text.UiText
import com.thanhng224.androidcomposebase.core.ui.components.AppCard
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.feedback.AppSnackbarEffect
import com.thanhng224.androidcomposebase.core.ui.feedback.AppSnackbarMessage
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

@Composable
fun FeedbackSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val hostState = remember { SnackbarHostState() }
    var currentMessage by remember { mutableStateOf<AppSnackbarMessage?>(null) }
    var messageCounter by remember { mutableStateOf(1L) }

    AppSnackbarEffect(
        message = currentMessage,
        hostState = hostState,
        onResult = { id, _ ->
            if (currentMessage?.id == id) {
                currentMessage = null
            }
        },
    )

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
            Text(
                text = stringResource(R.string.design_system_feedback_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
            ) {
                AppPrimaryButton(
                    text = stringResource(R.string.design_system_show_snackbar),
                    onClick = {
                        val id = messageCounter++
                        currentMessage =
                            AppSnackbarMessage(
                                id = id,
                                text = UiText.StringResource(R.string.design_system_snackbar_message),
                                actionLabel = UiText.StringResource(R.string.action_cancel),
                            )
                    },
                    modifier = Modifier.weight(1f),
                )
                val toastMessage = stringResource(R.string.design_system_toast_message)
                AppOutlinedButton(
                    text = stringResource(R.string.design_system_show_toast),
                    onClick = {
                        Toast
                            .makeText(
                                context.applicationContext,
                                toastMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            SnackbarHost(hostState = hostState, modifier = Modifier.fillMaxWidth().padding(top = Dimens.spaceSmall))
        }
    }
}
