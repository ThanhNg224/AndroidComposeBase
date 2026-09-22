package com.thanhng224.androidcomposebase.core.network

public interface ApiClient {
    public suspend fun <T> execute(call: suspend () -> retrofit2.Response<T>): ApiResult<T>
}
