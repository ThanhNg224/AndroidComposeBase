package com.thanhng224.androidcomposebase.sample.demo.domain.repository

import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import kotlinx.coroutines.flow.Flow

interface DemoRepository {
    fun observeCount(): Flow<Int>

    suspend fun saveCount(count: Int)

    suspend fun fetchWeather(): WeatherResult
}
