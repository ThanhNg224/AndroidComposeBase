package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * A modern floating capsule navigation bar designed with glassmorphic surface tint,
 * animated icon transitions, expanding active labels, and tactile haptic feedback.
 */
@Composable
public fun AppFloatingNavBar(
    items: List<AppNavItem>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val haptic = LocalHapticFeedback.current
    val capsuleShape = RoundedCornerShape(Dimens.radiusPill)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = capsuleShape,
        color = containerColor,
        tonalElevation = Dimens.elevationMedium,
        shadowElevation = Dimens.elevationMedium,
        border =
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceSmall, vertical = Dimens.spaceSmall),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val interactionSource = remember { MutableInteractionSource() }
                val isSelected = item.selected

                val itemIndicatorColor by animateColorAsState(
                    targetValue = if (isSelected) selectedIndicatorColor else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "indicatorColor",
                )

                val itemContentColor by animateColorAsState(
                    targetValue = if (isSelected) selectedContentColor else contentColor,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "contentColor",
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec =
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    label = "iconScale",
                )

                Box(
                    modifier =
                        Modifier
                            .defaultMinSize(minWidth = Dimens.minTouchTarget, minHeight = Dimens.minTouchTarget)
                            .clip(capsuleShape)
                            .background(itemIndicatorColor, shape = capsuleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true, color = selectedContentColor),
                                onClick = {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        item.onClick()
                                    }
                                },
                            ).padding(horizontal = Dimens.spaceMediumSmall, vertical = Dimens.spaceSmall),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        val iconVector = if (isSelected) item.selectedIcon else item.unselectedIcon
                        val accessibilityLabel = item.contentDescription ?: item.label

                        if (item.badgeCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    ) {
                                        Text(
                                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = accessibilityLabel,
                                    tint = itemContentColor,
                                    modifier =
                                        Modifier
                                            .size(24.dp)
                                            .graphicsLayer {
                                                scaleX = iconScale
                                                scaleY = iconScale
                                            },
                                )
                            }
                        } else {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = accessibilityLabel,
                                tint = itemContentColor,
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        },
                            )
                        }

                        AnimatedVisibility(
                            visible = isSelected,
                            enter =
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    expandHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                            exit =
                                fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                    shrinkHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                        ) {
                            Text(
                                text = item.label,
                                color = itemContentColor,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier.padding(start = Dimens.spaceSmall),
                            )
                        }
                    }
                }
            }
        }
    }
}
