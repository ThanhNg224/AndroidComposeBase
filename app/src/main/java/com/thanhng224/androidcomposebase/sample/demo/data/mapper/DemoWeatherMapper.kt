package com.thanhng224.androidcomposebase.sample.demo.data.mapper

import com.thanhng224.androidcomposebase.sample.demo.data.dto.DemoWeatherResponseDto
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherError
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import com.thanhng224.androidcomposebase.core.network.ApiFailure
import com.thanhng224.androidcomposebase.core.network.ApiResult

fun ApiResult<DemoWeatherResponseDto>.toWeatherResult(): WeatherResult =
    when (this) {
        is ApiResult.Success ->
            WeatherResult.Success(
                DemoWeather(
                    temperatureCelsius = value.current.temperatureCelsius,
                    apparentTemperatureCelsius = value.current.apparentTemperatureCelsius,
                    weatherCode = value.current.weatherCode,
                    windSpeedKph = value.current.windSpeedKph,
                ),
            )
        is ApiResult.Failure -> WeatherResult.Failure(error.toWeatherError())
    }

private fun ApiFailure.toWeatherError(): WeatherError =
    when (this) {
        is ApiFailure.Http -> WeatherError.Server(code = code, message = serverMessage.orEmpty())
        is ApiFailure.Network -> WeatherError.Network(cause)
        is ApiFailure.Serialization -> WeatherError.Parse(cause)
        ApiFailure.EmptyBody -> WeatherError.EmptyBody
    }
