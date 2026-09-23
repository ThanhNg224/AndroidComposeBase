package com.thanhng224.androidcomposebase.sample.demo.domain.repository

import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import kotlinx.coroutines.flow.Flow

interface DemoRepository {
    fun observeCount(): Flow<Int>

    suspend fun saveCount(count: Int)

    fun observeWeather(): Flow<DemoWeather?>

    suspend fun refreshWeather(): WeatherResult
}
