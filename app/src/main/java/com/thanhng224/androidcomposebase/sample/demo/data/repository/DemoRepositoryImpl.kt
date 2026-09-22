package com.thanhng224.androidcomposebase.sample.demo.data.repository

import com.thanhng224.androidcomposebase.sample.demo.data.datasource.DemoRemoteDataSource
import com.thanhng224.androidcomposebase.sample.demo.data.mapper.toWeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DemoRepositoryImpl
    @Inject
    constructor(
        private val settingsStore: SettingsStore,
        private val remoteDataSource: DemoRemoteDataSource,
    ) : DemoRepository {
        override fun observeCount(): Flow<Int> = settingsStore.observe(DEMO_COUNTER_COUNT)

        override suspend fun saveCount(count: Int) {
            settingsStore.set(DEMO_COUNTER_COUNT, count)
        }

        override suspend fun fetchWeather(): WeatherResult = remoteDataSource.fetchCurrentWeather().toWeatherResult()

        private companion object {
            val DEMO_COUNTER_COUNT = SettingsKey.IntKey(name = "demo_counter_count", defaultValue = 0)
        }
    }
