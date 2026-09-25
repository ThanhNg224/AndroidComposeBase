package com.thanhng224.androidcomposebase.core.foundation

import kotlinx.coroutines.CoroutineDispatcher

public interface AppDispatchers {
    public val main: CoroutineDispatcher
    public val io: CoroutineDispatcher
    public val default: CoroutineDispatcher

    public companion object {
        public fun default(): AppDispatchers = DefaultAppDispatchers()
    }
}
