package com.thanhng224.androidcomposebase.feature.settings.presentation.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.text.resolve
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel.SettingsViewModel

@Composable
public fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshCurrentLanguage()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
            AppCenterTopBar(title = stringResource(R.string.settings_title))
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
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
                        text = stringResource(R.string.settings_appearance_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() },
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
                                    painter = painterResource(R.drawable.ic_dark_mode),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.size(Dimens.spaceSmall))
                                Text(
                                    text = stringResource(R.string.settings_theme_heading),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                            ) {
                                FilterChip(
                                    selected = state.theme == AppTheme.SYSTEM,
                                    onClick = { viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.SYSTEM)) },
                                    label = { Text(stringResource(R.string.settings_theme_system)) },
                                )
                                FilterChip(
                                    selected = state.theme == AppTheme.LIGHT,
                                    onClick = { viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.LIGHT)) },
                                    label = { Text(stringResource(R.string.settings_theme_light)) },
                                )
                                FilterChip(
                                    selected = state.theme == AppTheme.DARK,
                                    onClick = { viewModel.onEvent(SettingsUiEvent.ThemeSelected(AppTheme.DARK)) },
                                    label = { Text(stringResource(R.string.settings_theme_dark)) },
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.settings_language_heading),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_settings_language),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.size(Dimens.spaceSmall))
                                Text(
                                    text = stringResource(R.string.settings_language_section_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }

                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

                            LanguageOptions(
                                languages = state.supportedLanguages,
                                selectedLanguage = state.language,
                                onLanguageSelected = { language ->
                                    viewModel.onEvent(SettingsUiEvent.LanguageSelected(language))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun LanguageOptions(
    languages: List<AppLanguage>,
    selectedLanguage: AppLanguage?,
    onLanguageSelected: (AppLanguage?) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedLanguage == null,
                        role = Role.RadioButton,
                        onClick = { onLanguageSelected(null) },
                    ).padding(vertical = Dimens.spaceXXSmall),
        ) {
            RadioButton(selected = selectedLanguage == null, onClick = null)
            Spacer(modifier = Modifier.size(Dimens.spaceSmall))
            Text(
                text = stringResource(R.string.settings_language_system),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        languages.forEach { language ->
            val selected = selectedLanguage?.languageTag == language.languageTag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onLanguageSelected(language) },
                        ).padding(vertical = Dimens.spaceXXSmall),
            ) {
                RadioButton(selected = selected, onClick = null)
                Spacer(modifier = Modifier.size(Dimens.spaceSmall))
                Text(
                    text = stringResource(language.displayNameResId),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
