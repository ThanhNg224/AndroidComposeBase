package com.thanhng224.androidcomposebase.feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.components.AppCard
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.feedback.AppSnackbarEffect
import com.thanhng224.androidcomposebase.core.ui.feedback.AppSnackbarMessage
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiEvent
import com.thanhng224.androidcomposebase.feature.settings.presentation.state.SettingsUiState
import com.thanhng224.androidcomposebase.feature.settings.presentation.viewmodel.SettingsViewModel

@Composable
public fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshLanguage()
        onPauseOrDispose { }
    }

    SettingsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

@Composable
public fun SettingsContent(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onMessageShown: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val message =
        state.pendingMessages.firstOrNull()?.let { pending ->
            AppSnackbarMessage(id = pending.id, text = pending.text)
        }

    AppSnackbarEffect(
        message = message,
        hostState = snackbarHostState,
        onResult = { id, _ -> onMessageShown(id) },
    )

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
                        text = stringResource(R.string.settings_appearance_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() },
                    )
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
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

                            ThemeOptions(
                                selectedTheme = state.theme,
                                onThemeSelected = { theme -> onEvent(SettingsUiEvent.ThemeSelected(theme)) },
                            )
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
                    AppCard(modifier = Modifier.fillMaxWidth()) {
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
                                    onEvent(SettingsUiEvent.LanguageSelected(language))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Settings light")
@Composable
private fun SettingsContentLightPreview() {
    AndroidComposeBaseTheme(darkTheme = false) {
        SettingsContent(
            state = SettingsUiState(supportedLanguages = AppLanguage.BUILT_IN),
            onEvent = {},
            onMessageShown = {},
        )
    }
}

@Preview(name = "Settings dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentDarkPreview() {
    AndroidComposeBaseTheme(darkTheme = true) {
        SettingsContent(
            state = SettingsUiState(supportedLanguages = AppLanguage.BUILT_IN),
            onEvent = {},
            onMessageShown = {},
        )
    }
}

private val ThemeOptionOrder: List<Pair<AppTheme, Int>> =
    listOf(
        AppTheme.SYSTEM to R.string.settings_theme_system,
        AppTheme.LIGHT to R.string.settings_theme_light,
        AppTheme.DARK to R.string.settings_theme_dark,
    )

@Composable
private fun ThemeOptions(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    // IntrinsicSize.Min + fillMaxHeight() keep every segment as tall as the tallest one when a
    // label wraps.
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        ThemeOptionOrder.forEachIndexed { index, (theme, labelRes) ->
            val selected = selectedTheme == theme
            SegmentedButton(
                selected = selected,
                onClick = { onThemeSelected(theme) },
                // A fixed radius (pill-shaped at the usual height) so that when labels wrap at a
                // large font scale the rounded ends don't grow into the label area.
                shape =
                    SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeOptionOrder.size,
                        baseShape = RoundedCornerShape(Dimens.radiusLarge),
                    ),
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = Dimens.spaceSmall, vertical = Dimens.spaceSmall),
                // The default checkmark sits in a fixed slot beside the label, but the label is
                // still measured at the full segment width, so a label that wraps ("System
                // default" on a narrow screen or at a large font scale) is pushed past the edge
                // and clipped. The checkmark is drawn inline with the label text instead.
                icon = {},
            ) {
                SegmentLabel(label = stringResource(labelRes), selected = selected)
            }
        }
    }
}

private const val SEGMENT_CHECK_ID = "check"

/** A segment label that wraps as a whole with its leading checkmark instead of being clipped. */
@Composable
private fun SegmentLabel(
    label: String,
    selected: Boolean,
) {
    val text =
        buildAnnotatedString {
            if (selected) {
                appendInlineContent(SEGMENT_CHECK_ID, alternateText = " ")
                append(" ")
            }
            append(label)
        }
    val inlineContent =
        if (selected) {
            mapOf(
                SEGMENT_CHECK_ID to
                    InlineTextContent(Placeholder(1.em, 1.em, PlaceholderVerticalAlign.TextCenter)) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    },
            )
        } else {
            emptyMap()
        }
    Text(
        text = text,
        inlineContent = inlineContent,
        textAlign = TextAlign.Center,
        // At large font scales even one word can exceed a third of the row; hyphenate it rather
        // than breaking it at an arbitrary letter.
        style = LocalTextStyle.current.copy(hyphens = Hyphens.Auto, lineBreak = LineBreak.Paragraph),
        // The segment already exposes its selected state; read only the label.
        modifier = Modifier.clearAndSetSemantics { this.text = AnnotatedString(label) },
    )
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
                    .heightIn(min = Dimens.minTouchTarget)
                    .selectable(
                        selected = selectedLanguage == null,
                        role = Role.RadioButton,
                        onClick = { onLanguageSelected(null) },
                    ),
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
                        .heightIn(min = Dimens.minTouchTarget)
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onLanguageSelected(language) },
                        ),
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
