package com.thanhng224.androidcomposebase.sample.demo.presentation.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppCenterTopBar
import com.thanhng224.androidcomposebase.core.ui.components.AppOutlinedButton
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.text.resolve
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
            val text = message.text.resolve(context)
            snackbarHostState.showSnackbar(message = text)
            viewModel.onMessageHandled(message.id)
        }
    }

    Scaffold(
        topBar = {
            AppCenterTopBar(title = "Demo Feature")
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
                    text = "Interactive Counter (DataStore)",
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
                    Column(
                        modifier = Modifier.padding(Dimens.spaceLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${state.count}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceSmall))
                        Text(
                            text = "Persisted via DataStore Preferences",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                        AppPrimaryButton(
                            text = "Increment Count",
                            icon = Icons.Default.Add,
                            onClick = { viewModel.onEvent(DemoUiEvent.IncrementClicked) },
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Live API Weather (NetworkClientFactory)",
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
                                        text = "Fetching weather data...",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            is DemoWeatherState.Error -> {
                                Text(
                                    text = stringResource(weather.reason.toStringResource()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                                AppOutlinedButton(
                                    text = "Retry",
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
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp),
                                    )
                                    Spacer(modifier = Modifier.size(Dimens.spaceMedium))
                                    Column {
                                        Text(
                                            text = "Open-Meteo API",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = "Fetched dynamically via Retrofit + OkHttp",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Thermostat, contentDescription = null)
                                        Spacer(modifier = Modifier.height(Dimens.spaceXXSmall))
                                        Text(
                                            text = "${weather.weather.temperatureCelsius}°C",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "Temperature",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Air, contentDescription = null)
                                        Spacer(modifier = Modifier.height(Dimens.spaceXXSmall))
                                        Text(
                                            text = "${weather.weather.windSpeedKph} km/h",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "Wind Speed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                                weather.refreshError?.let { error ->
                                    Text(
                                        text = stringResource(error.toStringResource()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                    Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                                }

                                AppOutlinedButton(
                                    text = "Refresh Weather",
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

private fun DemoWeatherError.toStringResource(): Int =
    when (this) {
        DemoWeatherError.SERVER -> R.string.demo_weather_error_server
        DemoWeatherError.NO_CONNECTION -> R.string.demo_weather_error_no_connection
        DemoWeatherError.UNEXPECTED_RESPONSE -> R.string.demo_weather_error_unexpected_response
        DemoWeatherError.EMPTY_RESPONSE -> R.string.demo_weather_error_empty_response
    }
