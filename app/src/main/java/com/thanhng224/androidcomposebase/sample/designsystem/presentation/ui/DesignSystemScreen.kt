package com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppCard
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppCheckboxRow
import com.thanhng224.androidcomposebase.core.ui.components.AppDialog
import com.thanhng224.androidcomposebase.core.ui.components.AppEmptyState
import com.thanhng224.androidcomposebase.core.ui.components.AppErrorState
import com.thanhng224.androidcomposebase.core.ui.components.AppLoadingState
import com.thanhng224.androidcomposebase.core.ui.components.AppModalBottomSheet
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.components.AppRadioRow
import com.thanhng224.androidcomposebase.core.ui.components.AppSecondaryButton
import com.thanhng224.androidcomposebase.core.ui.components.AppSwitchRow
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

// State components keep their size caller-controlled. heightIn here provides a useful gallery
// preview while still allowing content to grow at large font scales.
private val LoadingStateCardMinHeight = 200.dp
private val EmptyStateCardMinHeight = 260.dp
private val ErrorStateCardMinHeight = 280.dp

@Composable
public fun DesignSystemScreen(modifier: Modifier = Modifier) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showCustomDialog by rememberSaveable { mutableStateOf(false) }
    var showSheet by rememberSaveable { mutableStateOf(false) }

    DesignSystemContent(
        showDialog = showDialog,
        onOpenDialog = { showDialog = true },
        onDismissDialog = { showDialog = false },
        showCustomDialog = showCustomDialog,
        onOpenCustomDialog = { showCustomDialog = true },
        onDismissCustomDialog = { showCustomDialog = false },
        showSheet = showSheet,
        onOpenSheet = { showSheet = true },
        onDismissSheet = { showSheet = false },
        modifier = modifier,
    )
}

@Composable
private fun DesignSystemContent(
    showDialog: Boolean = false,
    onOpenDialog: () -> Unit = {},
    onDismissDialog: () -> Unit = {},
    showCustomDialog: Boolean = false,
    onOpenCustomDialog: () -> Unit = {},
    onDismissCustomDialog: () -> Unit = {},
    showSheet: Boolean = false,
    onOpenSheet: () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
                modifier = Modifier.widthIn(max = Dimens.maxContentWidth).fillMaxSize(),
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
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Dimens.spaceMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
                        ) {
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_primary_button),
                                modifier = Modifier.fillMaxWidth(),
                                icon = {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimens.iconSizeSmall),
                                    )
                                },
                                onClick = {},
                            )
                            AppSecondaryButton(
                                text = stringResource(R.string.design_system_secondary_button),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                            )
                            AppOutlinedButton(
                                text = stringResource(R.string.design_system_outlined_button),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {},
                            )
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_disabled_button),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                onClick = {},
                            )
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_saving_button),
                                modifier = Modifier.fillMaxWidth(),
                                isLoading = true,
                                onClick = {},
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.design_system_selection_rows),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = Dimens.spaceSmall)) {
                            var switchChecked by remember { mutableStateOf(true) }
                            var checkboxChecked by remember { mutableStateOf(false) }
                            var radioSelected by remember { mutableStateOf(0) }

                            AppSwitchRow(
                                title = stringResource(R.string.design_system_switch_row),
                                supportingText = stringResource(R.string.design_system_switch_supporting),
                                checked = switchChecked,
                                onCheckedChange = { switchChecked = it },
                            )
                            AppCheckboxRow(
                                title = stringResource(R.string.design_system_checkbox_row),
                                supportingText = stringResource(R.string.design_system_checkbox_supporting),
                                checked = checkboxChecked,
                                onCheckedChange = { checkboxChecked = it },
                            )
                            AppRadioRow(
                                title = stringResource(R.string.design_system_radio_row) + " 1",
                                supportingText = stringResource(R.string.design_system_radio_supporting),
                                selected = radioSelected == 0,
                                onClick = { radioSelected = 0 },
                            )
                            AppRadioRow(
                                title = stringResource(R.string.design_system_radio_row) + " 2",
                                selected = radioSelected == 1,
                                onClick = { radioSelected = 1 },
                            )
                        }
                    }
                }

                item {
                    InputRecipesSection()
                }

                item {
                    PopupAndMenuSection()
                }

                item {
                    FeedbackSection()
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
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        AppLoadingState(
                            modifier = Modifier.fillMaxWidth().heightIn(min = LoadingStateCardMinHeight),
                            message = stringResource(R.string.design_system_states_loading_message),
                        )
                    }
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        AppEmptyState(
                            title = stringResource(R.string.design_system_states_empty_title),
                            modifier = Modifier.fillMaxWidth().heightIn(min = EmptyStateCardMinHeight),
                            message = stringResource(R.string.design_system_states_empty_message),
                            action = {
                                AppOutlinedButton(
                                    text = stringResource(R.string.design_system_states_empty_action),
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {},
                                )
                            },
                        )
                    }
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        AppErrorState(
                            title = stringResource(R.string.design_system_states_error_title),
                            modifier = Modifier.fillMaxWidth().heightIn(min = ErrorStateCardMinHeight),
                            message = stringResource(R.string.design_system_states_error_message),
                            onRetry = {},
                            action = {
                                AppPrimaryButton(
                                    text = stringResource(com.thanhng224.androidcomposebase.core.ui.R.string.core_ui_retry),
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {},
                                )
                            },
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
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Dimens.spaceMedium),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
                        ) {
                            Text(
                                text = stringResource(R.string.design_system_dialog_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_open_dialog),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onOpenDialog,
                            )
                            AppOutlinedButton(
                                text = stringResource(R.string.design_system_dialog_title) + " (AppDialog)",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onOpenCustomDialog,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.design_system_sheets_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.spaceMedium)) {
                            Text(
                                text = stringResource(R.string.design_system_sheet_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                            AppPrimaryButton(
                                text = stringResource(R.string.design_system_open_sheet),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onOpenSheet,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(stringResource(R.string.design_system_dialog_title)) },
            text = {
                Text(
                    text = stringResource(R.string.design_system_dialog_body),
                    modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(stringResource(R.string.design_system_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showCustomDialog) {
        AppDialog(onDismissRequest = onDismissCustomDialog) {
            Text(
                text = stringResource(R.string.design_system_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
            Text(
                text = stringResource(R.string.design_system_dialog_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                AppPrimaryButton(
                    text = stringResource(R.string.design_system_dialog_confirm),
                    onClick = onDismissCustomDialog,
                )
            }
        }
    }

    AppModalBottomSheet(
        visible = showSheet,
        onDismissRequest = onDismissSheet,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceLarge, vertical = Dimens.spaceMedium),
        ) {
            Text(
                text = stringResource(R.string.design_system_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            Text(
                text = stringResource(R.string.design_system_sheet_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
            AppPrimaryButton(
                text = stringResource(R.string.design_system_dialog_confirm),
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismissSheet,
            )
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
        }
    }
}

@Preview(name = "Design system light", showBackground = true)
@Composable
private fun DesignSystemContentLightPreview() {
    AndroidComposeBaseTheme(darkTheme = false) {
        DesignSystemContent(
            showDialog = false,
            onOpenDialog = {},
            onDismissDialog = {},
        )
    }
}

@Preview(
    name = "Design system dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun DesignSystemContentDarkPreview() {
    AndroidComposeBaseTheme(darkTheme = true) {
        DesignSystemContent(
            showDialog = false,
            onOpenDialog = {},
            onDismissDialog = {},
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
