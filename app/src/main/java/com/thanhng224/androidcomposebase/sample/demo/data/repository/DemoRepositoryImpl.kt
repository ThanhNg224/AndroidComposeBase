package com.thanhng224.androidcomposebase.sample.demo.data.repository

import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.sample.demo.data.datasource.DemoRemoteDataSource
import com.thanhng224.androidcomposebase.sample.demo.data.local.WeatherDao
import com.thanhng224.androidcomposebase.sample.demo.data.local.WeatherEntity
import com.thanhng224.androidcomposebase.sample.demo.data.mapper.toWeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class DemoRepositoryImpl
    @Inject
    constructor(
        private val settingsStore: SettingsStore,
        private val remoteDataSource: DemoRemoteDataSource,
        private val weatherDao: WeatherDao,
    ) : DemoRepository {
        private val refreshGeneration = AtomicLong(0)
        private val weatherCommitMutex = Mutex()

        override fun observeCount(): Flow<Int> = settingsStore.observe(DEMO_COUNTER_COUNT)

        override suspend fun saveCount(count: Int) {
            settingsStore.set(DEMO_COUNTER_COUNT, count)
        }

        override fun observeWeather(): Flow<DemoWeather?> = weatherDao.observeWeather().map { it?.toDomain() }

        override suspend fun refreshWeather(): WeatherResult {
            val generation = weatherCommitMutex.withLock { refreshGeneration.incrementAndGet() }
            val remote = remoteDataSource.fetchCurrentWeather().toWeatherResult()
            weatherCommitMutex.withLock {
                if (generation == refreshGeneration.get()) {
                    when (remote) {
                        is WeatherResult.Success -> weatherDao.saveWeather(WeatherEntity.fromDomain(remote.weather))
                        is WeatherResult.Failure -> Unit
                    }
                }
            }
            return remote
        }

        private companion object {
            val DEMO_COUNTER_COUNT = SettingsKey.IntKey(name = "demo_counter_count", defaultValue = 0)
        }
    }
