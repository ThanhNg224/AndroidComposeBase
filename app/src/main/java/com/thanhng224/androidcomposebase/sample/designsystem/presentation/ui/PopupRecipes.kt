package com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppCard
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopupAndMenuSection(modifier: Modifier = Modifier) {
    var menuExpanded by remember { mutableStateOf(false) }
    var popupVisible by remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState()

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
            Text(
                text = stringResource(R.string.design_system_menus_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    AppOutlinedButton(
                        text = stringResource(R.string.design_system_open_menu),
                        onClick = { menuExpanded = true },
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.design_system_menu_item_action)) },
                            onClick = { menuExpanded = false },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimens.iconSizeSmall),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.design_system_menu_item_disabled)) },
                            onClick = {},
                            enabled = false,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(R.string.design_system_tooltip_content))
                        }
                    },
                    state = tooltipState,
                ) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.design_system_tooltip_title),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.spaceMedium))

                Box {
                    IconButton(onClick = { popupVisible = !popupVisible }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.design_system_popup_toggle),
                        )
                    }
                    if (popupVisible) {
                        Popup(
                            alignment = Alignment.TopStart,
                            onDismissRequest = { popupVisible = false },
                            properties =
                                PopupProperties(
                                    focusable = true,
                                    dismissOnBackPress = true,
                                    dismissOnClickOutside = true,
                                ),
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shadowElevation = Dimens.elevationMedium,
                                modifier = Modifier.padding(Dimens.spaceSmall),
                            ) {
                                Text(
                                    text = stringResource(R.string.design_system_popup_body),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(Dimens.spaceSmall),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
