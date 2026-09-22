package com.thanhng224.androidcomposebase.sample.demo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather

@Entity(tableName = "weather_cache")
public data class WeatherEntity(
    @PrimaryKey val id: Int = 1,
    val temperatureCelsius: Double,
    val apparentTemperatureCelsius: Double,
    val weatherCode: Int,
    val windSpeedKph: Double,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    public fun toDomain(): DemoWeather =
        DemoWeather(
            temperatureCelsius = temperatureCelsius,
            apparentTemperatureCelsius = apparentTemperatureCelsius,
            weatherCode = weatherCode,
            windSpeedKph = windSpeedKph,
        )

    public companion object {
        public fun fromDomain(weather: DemoWeather): WeatherEntity =
            WeatherEntity(
                id = 1,
                temperatureCelsius = weather.temperatureCelsius,
                apparentTemperatureCelsius = weather.apparentTemperatureCelsius,
                weatherCode = weather.weatherCode,
                windSpeedKph = weather.windSpeedKph,
            )
    }
}
