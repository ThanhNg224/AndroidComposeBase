package com.thanhng224.androidcomposebase.appshell.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppCard
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

@Composable
public fun HomeScreen(modifier: Modifier = Modifier) {
    HomeContent(modifier = modifier)
}

@Composable
private fun HomeContent(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            AppCenterTopBar(title = stringResource(R.string.app_name))
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
                    ) {
                        Text(
                            text = stringResource(R.string.home_welcome_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = stringResource(R.string.home_welcome_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.home_next_steps_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }
                items(NextSteps) { step -> NextStepCard(step) }
            }
        }
    }
}

/** A starter task shown on Home to orient a developer who has just generated the project. */
private class NextStep(
    val icon: ImageVector,
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
)

private val NextSteps: List<NextStep> =
    listOf(
        NextStep(Icons.Default.Edit, R.string.home_next_steps_rename_title, R.string.home_next_steps_rename_body),
        NextStep(Icons.Default.Add, R.string.home_next_steps_feature_title, R.string.home_next_steps_feature_body),
        NextStep(Icons.Default.CheckCircle, R.string.home_next_steps_checks_title, R.string.home_next_steps_checks_body),
    )

@Composable
private fun NextStepCard(step: NextStep) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .padding(Dimens.spaceMedium)
                    .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeMedium),
            )
            Spacer(modifier = Modifier.width(Dimens.spaceMedium))
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceXSmall)) {
                Text(
                    text = stringResource(step.titleRes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(step.bodyRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Home light", showBackground = true)
@Composable
private fun HomeContentLightPreview() {
    AndroidComposeBaseTheme(darkTheme = false) {
        HomeContent()
    }
}

@Preview(
    name = "Home dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun HomeContentDarkPreview() {
    AndroidComposeBaseTheme(darkTheme = true) {
        HomeContent()
    }
}
