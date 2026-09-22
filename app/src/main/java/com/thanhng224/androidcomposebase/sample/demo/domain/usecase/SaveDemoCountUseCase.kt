package com.thanhng224.androidcomposebase.sample.demo.domain.usecase

import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import javax.inject.Inject

class SaveDemoCountUseCase
    @Inject
    constructor(
        private val repository: DemoRepository,
    ) {
        suspend operator fun invoke(count: Int) {
            repository.saveCount(count)
        }
    }
