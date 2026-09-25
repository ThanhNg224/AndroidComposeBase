package com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppEmptyState
import com.thanhng224.androidcomposebase.core.ui.components.AppErrorState
import com.thanhng224.androidcomposebase.core.ui.components.AppLoadingState
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.components.AppSecondaryButton
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

@Composable
public fun DesignSystemScreen(modifier: Modifier = Modifier) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppCenterTopBar(title = stringResource(R.string.design_system_title))
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 800.dp).fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = Dimens.spaceLarge,
                        end = Dimens.spaceLarge,
                        top = Dimens.spaceMedium,
                        bottom = Dimens.spaceLarge,
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.design_system_color_roles),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() },
                    )
                }

                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                    ) {
                        ColorChip(name = stringResource(R.string.design_system_primary), color = MaterialTheme.colorScheme.primary)
                        ColorChip(name = stringResource(R.string.design_system_secondary), color = MaterialTheme.colorScheme.secondary)
                        ColorChip(name = stringResource(R.string.design_system_tertiary), color = MaterialTheme.colorScheme.tertiary)
                        ColorChip(name = stringResource(R.string.design_system_surface), color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.design_system_buttons),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimens.spaceMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
                        ) {
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_primary_button),
                                icon = Icons.Default.CheckCircle,
                                onClick = {},
                            )
                            AppSecondaryButton(
                                text = stringResource(R.string.design_system_secondary_button),
                                onClick = {},
                            )
                            AppOutlinedButton(
                                text = stringResource(R.string.design_system_outlined_button),
                                onClick = {},
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.design_system_states),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                    ) {
                        AppLoadingState(message = stringResource(R.string.design_system_states_loading_message))
                    }
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                    ) {
                        AppEmptyState(
                            title = stringResource(R.string.design_system_states_empty_title),
                            message = stringResource(R.string.design_system_states_empty_message),
                            action = {
                                AppOutlinedButton(
                                    text = stringResource(R.string.design_system_states_empty_action),
                                    onClick = {},
                                )
                            },
                        )
                    }
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                    ) {
                        AppErrorState(
                            title = stringResource(R.string.design_system_states_error_title),
                            message = stringResource(R.string.design_system_states_error_message),
                            onRetry = {},
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.design_system_dialog_sample),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
                            Text(
                                text = stringResource(R.string.design_system_dialog_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_open_dialog),
                                onClick = { showDialog = true },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.design_system_dialog_title)) },
            text = {
                Text(
                    text = stringResource(R.string.design_system_dialog_body),
                    modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.design_system_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun ColorChip(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(color),
        )
        Spacer(modifier = Modifier.height(Dimens.spaceXXSmall))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
