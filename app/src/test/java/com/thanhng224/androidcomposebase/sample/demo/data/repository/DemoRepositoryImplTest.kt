package com.thanhng224.androidcomposebase.sample.demo.data.repository

import app.cash.turbine.test
import com.thanhng224.androidcomposebase.core.network.ApiFailure
import com.thanhng224.androidcomposebase.core.network.ApiResult
import com.thanhng224.androidcomposebase.core.testing.FakeSettingsStore
import com.thanhng224.androidcomposebase.sample.demo.data.datasource.DemoRemoteDataSource
import com.thanhng224.androidcomposebase.sample.demo.data.dto.DemoCurrentWeatherDto
import com.thanhng224.androidcomposebase.sample.demo.data.dto.DemoWeatherResponseDto
import com.thanhng224.androidcomposebase.sample.demo.data.local.WeatherDao
import com.thanhng224.androidcomposebase.sample.demo.data.local.WeatherEntity
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherError
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DemoRepositoryImplTest {
    private class FakeWeatherDao(
        initial: WeatherEntity? = null,
        private val saveGate: CompletableDeferred<Unit>? = null,
        private val saveFailure: Exception? = null,
    ) : WeatherDao {
        private val weather = MutableStateFlow(initial)

        override fun observeWeather(): Flow<WeatherEntity?> = weather

        override suspend fun saveWeather(entity: WeatherEntity) {
            saveGate?.await()
            saveFailure?.let { throw it }
            weather.value = entity
        }

        override suspend fun clearWeather() {
            weather.value = null
        }

        fun current(): WeatherEntity? = weather.value
    }

    private class QueuedRemoteDataSource : DemoRemoteDataSource {
        private val responses = mutableListOf<CompletableDeferred<ApiResult<DemoWeatherResponseDto>>>()
        var fetchCount: Int = 0
            private set

        fun enqueue(): CompletableDeferred<ApiResult<DemoWeatherResponseDto>> =
            CompletableDeferred<ApiResult<DemoWeatherResponseDto>>().also(responses::add)

        override suspend fun fetchCurrentWeather(): ApiResult<DemoWeatherResponseDto> {
            fetchCount += 1
            return responses.removeAt(0).await()
        }
    }

    @Test
    fun `observed weather emits Room cache as its source of truth`() =
        runTest {
            val cached = WeatherEntity(temperatureCelsius = 21.0, apparentTemperatureCelsius = 22.0, weatherCode = 1, windSpeedKph = 4.0)
            val dao = FakeWeatherDao(cached)
            val remote = QueuedRemoteDataSource()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)

            repository.observeWeather().test {
                assertEquals(cached.toDomain(), awaitItem())
                val response = remote.enqueue()
                val refresh = async { repository.refreshWeather() }
                runCurrent()
                response.complete(weather(30.0))
                assertEquals(demoWeather(30.0), awaitItem())
                refresh.await()
            }
        }

    @Test
    fun `failed refresh leaves existing Room cache unchanged`() =
        runTest {
            val cached = WeatherEntity(temperatureCelsius = 21.0, apparentTemperatureCelsius = 22.0, weatherCode = 1, windSpeedKph = 4.0)
            val dao = FakeWeatherDao(cached)
            val remote = QueuedRemoteDataSource()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)
            val response = remote.enqueue()
            val refresh = async { repository.refreshWeather() }
            runCurrent()
            response.complete(ApiResult.Failure(ApiFailure.Network(IOException("offline"))))

            refresh.await()

            assertEquals(cached, dao.current())
        }

    @Test
    fun `older success is retained when a newer refresh fails`() =
        runTest {
            val dao = FakeWeatherDao()
            val remote = QueuedRemoteDataSource()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)
            val olderResponse = remote.enqueue()
            val olderRefresh = async { repository.refreshWeather() }
            runCurrent()
            val newerResponse = remote.enqueue()
            val newerRefresh = async { repository.refreshWeather() }
            runCurrent()

            newerResponse.complete(ApiResult.Failure(ApiFailure.Network(IOException("offline"))))
            assertTrue(newerRefresh.await() is WeatherResult.Failure)
            olderResponse.complete(weather(25.0))
            assertTrue(olderRefresh.await() is WeatherResult.Success)

            assertEquals(25.0, dao.current()?.temperatureCelsius ?: 0.0, 0.0)
        }

    @Test
    fun `no-cache failure does not create a database value`() =
        runTest {
            val dao = FakeWeatherDao()
            val remote = QueuedRemoteDataSource()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)
            val response = remote.enqueue()
            val refresh = async { repository.refreshWeather() }
            runCurrent()
            response.complete(ApiResult.Failure(ApiFailure.Network(IOException("offline"))))

            refresh.await()

            assertNull(dao.current())
        }

    @Test
    fun `older response arriving last cannot overwrite newer cache`() =
        runTest {
            val dao = FakeWeatherDao()
            val remote = QueuedRemoteDataSource()
            val firstResponse = remote.enqueue()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)
            val first = async { repository.refreshWeather() }
            runCurrent()
            val secondResponse = remote.enqueue()
            val second = async { repository.refreshWeather() }
            runCurrent()

            secondResponse.complete(weather(30.0))
            second.await()
            firstResponse.complete(weather(10.0))
            first.await()

            assertEquals(30.0, dao.current()?.temperatureCelsius ?: 0.0, 0.0)
        }

    @Test
    fun `newer response commits after an older Room write finishes`() =
        runTest {
            val firstSaveGate = CompletableDeferred<Unit>()
            val dao = FakeWeatherDao(saveGate = firstSaveGate)
            val remote = QueuedRemoteDataSource()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)
            val firstResponse = remote.enqueue()
            val first = async { repository.refreshWeather() }
            runCurrent()
            firstResponse.complete(weather(10.0))
            runCurrent()

            val secondResponse = remote.enqueue()
            val second = async { repository.refreshWeather() }
            runCurrent()
            assertEquals("The next request must reach the network while Room is writing", 2, remote.fetchCount)
            secondResponse.complete(weather(30.0))
            runCurrent()
            assertNull(dao.current())

            firstSaveGate.complete(Unit)
            runCurrent()
            first.await()
            second.await()

            assertEquals(30.0, dao.current()?.temperatureCelsius ?: 0.0, 0.0)
        }

    @Test
    fun `Room write exception returns retryable storage failure and preserves cache`() =
        runTest {
            val cached = WeatherEntity(temperatureCelsius = 21.0, apparentTemperatureCelsius = 22.0, weatherCode = 1, windSpeedKph = 4.0)
            val dao = FakeWeatherDao(initial = cached, saveFailure = IllegalStateException("database unavailable"))
            val remote = QueuedRemoteDataSource()
            val repository = DemoRepositoryImpl(FakeSettingsStore(), remote, dao)
            val response = remote.enqueue()
            val refresh = async { repository.refreshWeather() }
            runCurrent()
            response.complete(weather(30.0))

            val result = refresh.await() as WeatherResult.Failure

            assertTrue(result.error is WeatherError.Storage)
            assertEquals(cached, dao.current())
        }

    private fun demoWeather(temperatureCelsius: Double): DemoWeather =
        DemoWeather(
            temperatureCelsius = temperatureCelsius,
            apparentTemperatureCelsius = temperatureCelsius + 1,
            weatherCode = 1,
            windSpeedKph = 4.0,
        )

    private fun weather(temperatureCelsius: Double) =
        ApiResult.Success(
            DemoWeatherResponseDto(
                current =
                    DemoCurrentWeatherDto(
                        temperatureCelsius = temperatureCelsius,
                        apparentTemperatureCelsius = temperatureCelsius + 1,
                        weatherCode = 1,
                        windSpeedKph = 4.0,
                    ),
            ),
        )
}
