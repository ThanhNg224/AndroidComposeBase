package com.thanhng224.androidcomposebase.feature.settings.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppDialog
import com.thanhng224.androidcomposebase.core.ui.text.resolve
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel.SettingsViewModel

@Composable
public fun SettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.pendingMessages) {
        val message = state.pendingMessages.firstOrNull()
        if (message != null) {
            val text = message.text.resolve(context)
            snackbarHostState.showSnackbar(message = text)
            viewModel.onMessageHandled(message.id)
        }
    }

    Scaffold(
        topBar = {
            AppCenterTopBar(title = "Settings")
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding =
                PaddingValues(
                    start = Dimens.spaceLarge,
                    end = Dimens.spaceLarge,
                    top = Dimens.spaceMedium,
                    bottom = 100.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
        ) {
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.size(Dimens.spaceSmall))
                            Text(
                                text = "Theme Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                        ) {
                            FilterChip(
                                selected = state.theme == AppTheme.SYSTEM,
                                onClick = { viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.SYSTEM)) },
                                label = { Text("System") },
                            )
                            FilterChip(
                                selected = state.theme == AppTheme.LIGHT,
                                onClick = { viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.LIGHT)) },
                                label = { Text("Light") },
                            )
                            FilterChip(
                                selected = state.theme == AppTheme.DARK,
                                onClick = { viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK)) },
                                label = { Text("Dark") },
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Localization",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Dimens.spaceSmall),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.size(Dimens.spaceSmall))
                            Text(
                                text = "App Language",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                        state.supportedLanguages.forEach { language ->
                            val isSelected = state.language?.languageTag == language.languageTag
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onEvent(SettingsUiEvent.LanguageSelected(language))
                                        }.padding(vertical = Dimens.spaceSmall),
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.onEvent(SettingsUiEvent.LanguageSelected(language))
                                    },
                                )
                                Spacer(modifier = Modifier.size(Dimens.spaceSmall))
                                Text(
                                    text =
                                        androidx.compose.ui.res
                                            .stringResource(language.displayNameResId),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Dimens.spaceSmall),
                )
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(Dimens.spaceMedium),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.size(Dimens.spaceMedium))
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    AppDialog(
        visible = showLogoutDialog,
        onDismiss = { showLogoutDialog = false },
        title = "Sign Out",
        message = "Are you sure you want to sign out? You will be routed back to the login screen.",
        primaryActionText = "Sign Out",
        onPrimaryAction = onLogout,
        secondaryActionText = "Cancel",
    )
}
