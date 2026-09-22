package com.thanhng224.androidcomposebase.appshell.home

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
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

@Composable
public fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            AppCenterTopBar(title = "AndroidComposeBase")
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.spaceLarge,
                end = Dimens.spaceLarge,
                top = Dimens.spaceMedium,
                bottom = 100.dp, // Space for floating bottom bar
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
        ) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(Dimens.spaceLarge)) {
                        Text(
                            text = "Pure Jetpack Compose",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                        Text(
                            text = "Clean Architecture, Type-Safe Navigation, Material 3 Design System, and hardened core foundations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Core Capabilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = Dimens.spaceSmall),
                )
            }

            item {
                FeatureCard(
                    title = "Type-Safe Navigation",
                    description = "Navigation Compose 2.8+ using KotlinX Serialization @Serializable routes.",
                    icon = Icons.Default.Architecture,
                )
            }

            item {
                FeatureCard(
                    title = "Enterprise Security",
                    description = "Android Keystore-backed AES-GCM encryption with SecureStore.",
                    icon = Icons.Default.Security,
                )
            }

            item {
                FeatureCard(
                    title = "Material 3 Design System",
                    description = "Light, Dark, Dynamic Color, 8-point spacing tokens, and FloatingNavBar.",
                    icon = Icons.Default.Palette,
                )
            }

            item {
                FeatureCard(
                    title = "Self-Healing Network",
                    description = "Single-flight mutex token refresh authenticator and transfer progress tracking.",
                    icon = Icons.Default.Thunderstorm,
                )
            }

            item {
                FeatureCard(
                    title = "Strict Quality Gates",
                    description = "Detekt, Android Lint with abortOnError, and Baseline Profiles benchmark.",
                    icon = Icons.Default.CheckCircle,
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.size(Dimens.spaceMedium))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(Dimens.spaceXXSmall))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
