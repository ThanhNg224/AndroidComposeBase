package com.thanhng224.androidcomposebase.sample.demo.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.text.resolve
import com.thanhng224.androidcomposebase.core.ui.components.AppCard
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppErrorState
import com.thanhng224.androidcomposebase.core.ui.components.AppLoadingState
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiEvent
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiState
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherError
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherState
import com.thanhng224.androidcomposebase.sample.demo.presentation.viewmodel.DemoViewModel

// AppLoadingState/AppErrorState fill their available height, so the weather card needs a
// minimum to lay out inside the unbounded LazyColumn item; heightIn(min = ...) lets the
// message still grow beyond it (e.g. at large font scale) instead of clipping.
private val WeatherLoadingMinHeight = 112.dp
private val WeatherErrorMinHeight = 190.dp
private val WeatherIconSize = 36.dp

@Composable
public fun DemoScreen(
    modifier: Modifier = Modifier,
    viewModel: DemoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DemoContent(
        state = state,
        onEvent = viewModel::onEvent,
        onMessageHandled = viewModel::onMessageHandled,
        onMessageAction = viewModel::onMessageAction,
        modifier = modifier,
    )
}

@Composable
private fun DemoContent(
    state: DemoUiState,
    onEvent: (DemoUiEvent) -> Unit,
    onMessageHandled: (Long) -> Unit,
    onMessageAction: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.pendingMessages) {
        val message = state.pendingMessages.firstOrNull() ?: return@LaunchedEffect
        val result =
            snackbarHostState.showSnackbar(
                message = message.text.resolve(context),
                actionLabel = message.actionLabel?.resolve(context),
            )
        if (result == SnackbarResult.ActionPerformed) onMessageAction(message.id) else onMessageHandled(message.id)
    }

    Scaffold(
        topBar = { AppCenterTopBar(title = stringResource(R.string.demo_title)) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                        text = stringResource(R.string.demo_counter_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Dimens.spaceLarge),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(R.string.demo_count_format, state.count),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                            Text(
                                text = stringResource(R.string.demo_counter_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                            AppPrimaryButton(
                                text = stringResource(R.string.demo_increment),
                                icon = Icons.Default.Add,
                                onClick = { onEvent(DemoUiEvent.IncrementClicked) },
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.demo_weather_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = Dimens.spaceSmall).semantics { heading() },
                    )
                }
                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.spaceLarge)) {
                            when (val weather = state.weather) {
                                DemoWeatherState.Loading ->
                                    AppLoadingState(
                                        modifier = Modifier.fillMaxWidth().heightIn(min = WeatherLoadingMinHeight),
                                        message = stringResource(R.string.demo_weather_loading),
                                    )

                                is DemoWeatherState.Error ->
                                    AppErrorState(
                                        title = stringResource(weather.reason.toStringResource()),
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = WeatherErrorMinHeight)
                                                .semantics { liveRegion = LiveRegionMode.Assertive },
                                        onRetry = { onEvent(DemoUiEvent.RefreshWeatherClicked) },
                                    )

                                is DemoWeatherState.Success -> WeatherContent(weather, onEvent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherContent(
    weather: DemoWeatherState.Success,
    onEvent: (DemoUiEvent) -> Unit,
) {
    if (weather.isRefreshing) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_wb_sunny),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(WeatherIconSize),
        )
        Spacer(modifier = Modifier.size(Dimens.spaceMedium))
        Column {
            Text(
                text = stringResource(R.string.demo_weather_conditions_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.demo_weather_conditions_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium),
    ) {
        WeatherMetric(
            iconRes = R.drawable.ic_thermostat,
            value = stringResource(R.string.demo_weather_temperature_value, weather.weather.temperatureCelsius),
            label = stringResource(R.string.demo_weather_temperature_label),
            modifier = Modifier.weight(1f),
        )
        WeatherMetric(
            iconRes = R.drawable.ic_air,
            value = stringResource(R.string.demo_weather_wind_value, weather.weather.windSpeedKph),
            label = stringResource(R.string.demo_weather_wind_label),
            modifier = Modifier.weight(1f),
        )
    }

    Spacer(modifier = Modifier.height(Dimens.spaceLarge))
    weather.refreshError?.let { error ->
        Text(
            text = stringResource(error.toStringResource()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
        )
        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
    }
    AppOutlinedButton(
        text = stringResource(R.string.demo_weather_refresh),
        icon = Icons.Default.Refresh,
        onClick = { onEvent(DemoUiEvent.RefreshWeatherClicked) },
    )
}

/**
 * One weather reading: decorative icon, value, then label. Semantics are merged so TalkBack reads
 * the cell as a single item ("31.7°C, Temperature").
 */
@Composable
private fun WeatherMetric(
    @DrawableRes iconRes: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXSmall),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Dimens.iconSizeMedium),
        )
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun DemoWeatherError.toStringResource(): Int =
    when (this) {
        DemoWeatherError.SERVER -> R.string.demo_weather_error_server
        DemoWeatherError.NO_CONNECTION -> R.string.demo_weather_error_no_connection
        DemoWeatherError.UNEXPECTED_RESPONSE -> R.string.demo_weather_error_unexpected_response
        DemoWeatherError.EMPTY_RESPONSE -> R.string.demo_weather_error_empty_response
        DemoWeatherError.STORAGE_FAILURE -> R.string.demo_weather_error_storage
    }

@Preview(showBackground = true)
@Composable
private fun DemoContentPreview() {
    AndroidComposeBaseTheme {
        DemoContent(
            state = DemoUiState(count = 7, weather = DemoWeatherState.Success(DEMO_WEATHER)),
            onEvent = {},
            onMessageHandled = {},
            onMessageAction = {},
        )
    }
}

private val DEMO_WEATHER =
    DemoWeather(
        temperatureCelsius = 31.8,
        apparentTemperatureCelsius = 37.0,
        weatherCode = 2,
        windSpeedKph = 11.0,
    )
