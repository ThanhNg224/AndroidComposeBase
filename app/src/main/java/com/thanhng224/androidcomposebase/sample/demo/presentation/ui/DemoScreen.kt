package com.thanhng224.androidcomposebase.sample.demo.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.text.resolve
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiEvent
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherError
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherState
import com.thanhng224.androidcomposebase.sample.demo.presentation.viewmodel.DemoViewModel

@Composable
public fun DemoScreen(
    modifier: Modifier = Modifier,
    viewModel: DemoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.pendingMessages) {
        val message = state.pendingMessages.firstOrNull()
        if (message != null) {
            snackbarHostState.showSnackbar(message = message.text.resolve(context))
            viewModel.onMessageHandled(message.id)
        }
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
                        text = stringResource(R.string.demo_counter_title),
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
                                onClick = { viewModel.onEvent(DemoUiEvent.IncrementClicked) },
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
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationLow),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(Dimens.spaceLarge)) {
                            when (val weather = state.weather) {
                                is DemoWeatherState.Loading -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(Dimens.spaceMedium),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.size(Dimens.spaceMedium))
                                        Text(
                                            text = stringResource(R.string.demo_weather_loading),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }

                                is DemoWeatherState.Error -> {
                                    Text(
                                        text = stringResource(weather.reason.toStringResource()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                                    AppOutlinedButton(
                                        text = stringResource(R.string.demo_weather_retry),
                                        icon = Icons.Default.Refresh,
                                        onClick = { viewModel.onEvent(DemoUiEvent.RefreshWeatherClicked) },
                                    )
                                }

                                is DemoWeatherState.Success -> {
                                    if (weather.isRefreshing) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_wb_sunny),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp),
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
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val temperature =
                                            stringResource(
                                                R.string.demo_weather_temperature_value,
                                                weather.weather.temperatureCelsius,
                                            )
                                        val windSpeed =
                                            stringResource(
                                                R.string.demo_weather_wind_value,
                                                weather.weather.windSpeedKph,
                                            )
                                        val temperatureLabel = stringResource(R.string.demo_weather_temperature_label)
                                        val windLabel = stringResource(R.string.demo_weather_wind_label)
                                        if (maxWidth < 360.dp) {
                                            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
                                                WeatherMetric(
                                                    icon = { Icon(painterResource(R.drawable.ic_thermostat), contentDescription = null) },
                                                    value = temperature,
                                                    label = temperatureLabel,
                                                )
                                                WeatherMetric(
                                                    icon = { Icon(painterResource(R.drawable.ic_air), contentDescription = null) },
                                                    value = windSpeed,
                                                    label = windLabel,
                                                )
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround,
                                            ) {
                                                WeatherMetric(
                                                    icon = { Icon(painterResource(R.drawable.ic_thermostat), contentDescription = null) },
                                                    value = temperature,
                                                    label = temperatureLabel,
                                                )
                                                WeatherMetric(
                                                    icon = { Icon(painterResource(R.drawable.ic_air), contentDescription = null) },
                                                    value = windSpeed,
                                                    label = windLabel,
                                                )
                                            }
                                        }
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
                                        onClick = { viewModel.onEvent(DemoUiEvent.RefreshWeatherClicked) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(modifier = Modifier.height(Dimens.spaceXXSmall))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
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
