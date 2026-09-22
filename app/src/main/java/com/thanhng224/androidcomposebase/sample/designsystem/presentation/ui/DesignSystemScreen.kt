package com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppDialog
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.components.AppSecondaryButton
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

@Composable
public fun DesignSystemScreen(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppCenterTopBar(title = "Design System")
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
                    text = "Material 3 Color Roles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                ) {
                    ColorChip(name = "Primary", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    ColorChip(name = "Secondary", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                    ColorChip(name = "Tertiary", color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
                    ColorChip(name = "Surface", color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.weight(1f))
                }
            }

            item {
                Text(
                    text = "Interactive Buttons",
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
                    Column(
                        modifier = Modifier.padding(Dimens.spaceMedium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
                    ) {
                        AppPrimaryButton(
                            text = "Primary Button",
                            icon = Icons.Default.CheckCircle,
                            onClick = {},
                        )
                        AppSecondaryButton(
                            text = "Secondary Tonal Button",
                            onClick = {},
                        )
                        AppOutlinedButton(
                            text = "Outlined Button",
                            onClick = {},
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Animated Dialog",
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
                        Text(
                            text = "Interactive dialog with scale and fade animations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                        AppPrimaryButton(
                            text = "Open Sample Dialog",
                            onClick = { showDialog = true },
                        )
                    }
                }
            }
        }
    }

    AppDialog(
        visible = showDialog,
        onDismiss = { showDialog = false },
        icon = Icons.Default.Info,
        title = "Design System Dialog",
        message = "This dialog demonstrates the animated scale and fade transitions with Material 3 tokens.",
        primaryActionText = "Got It",
        onPrimaryAction = { showDialog = false },
        secondaryActionText = "Dismiss",
    )
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
