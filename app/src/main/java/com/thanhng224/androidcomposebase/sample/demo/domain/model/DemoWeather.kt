package com.thanhng224.androidcomposebase.sample.demo.domain.model

data class DemoWeather(
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val weatherCode: Int,
    val windSpeedKph: Double,
)
