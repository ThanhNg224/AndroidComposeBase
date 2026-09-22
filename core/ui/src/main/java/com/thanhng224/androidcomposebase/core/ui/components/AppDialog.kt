package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Animated dialog container with enter/exit transitions, customizable primary and secondary
 * action buttons, and Material 3 design tokens.
 */
@Composable
public fun AppDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String? = null,
    message: String? = null,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    enter: EnterTransition = fadeIn() + scaleIn(initialScale = 0.9f),
    exit: ExitTransition = fadeOut() + scaleOut(targetScale = 0.95f),
    content: (@Composable () -> Unit)? = null,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false,
        ),
    ) {
        val interactionSource = remember { MutableInteractionSource() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (dismissOnClickOutside) onDismiss()
                    },
                )
                .padding(Dimens.spaceLarge),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = enter,
                exit = exit,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = Dimens.elevationHigh,
                    shadowElevation = Dimens.elevationHigh,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = modifier
                        .fillMaxWidth(0.92f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { /* Intercept tap inside card */ },
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.spaceLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(Dimens.spaceXXLarge),
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                        }

                        if (!title.isNullOrBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                        }

                        if (!message.isNullOrBlank()) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
                        }

                        if (content != null) {
                            content()
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                        }

                        if (primaryActionText != null || secondaryActionText != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (secondaryActionText != null) {
                                    OutlinedButton(
                                        onClick = {
                                            onSecondaryAction?.invoke()
                                            onDismiss()
                                        },
                                        modifier = Modifier.height(Dimens.buttonHeight),
                                        shape = MaterialTheme.shapes.medium,
                                    ) {
                                        Text(text = secondaryActionText)
                                    }
                                }

                                if (primaryActionText != null && secondaryActionText != null) {
                                    Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                                }

                                if (primaryActionText != null) {
                                    Button(
                                        onClick = {
                                            onPrimaryAction?.invoke()
                                            onDismiss()
                                        },
                                        modifier = Modifier.height(Dimens.buttonHeight),
                                        shape = MaterialTheme.shapes.medium,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                        ),
                                    ) {
                                        Text(text = primaryActionText)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
