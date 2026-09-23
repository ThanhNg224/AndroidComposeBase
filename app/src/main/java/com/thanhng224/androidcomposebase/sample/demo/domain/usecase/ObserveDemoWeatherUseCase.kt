package com.thanhng224.androidcomposebase.sample.demo.domain.usecase

import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveDemoWeatherUseCase
    @Inject
    constructor(
        private val repository: DemoRepository,
    ) {
        operator fun invoke(): Flow<DemoWeather?> = repository.observeWeather()
    }
