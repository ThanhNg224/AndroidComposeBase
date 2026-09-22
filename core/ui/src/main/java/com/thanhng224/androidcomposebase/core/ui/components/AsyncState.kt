package com.thanhng224.androidcomposebase.core.ui.components

/**
 * Represents the state of an asynchronous operation.
 */
public sealed interface AsyncState<out T> {
    public data object Idle : AsyncState<Nothing>

    public data object Loading : AsyncState<Nothing>

    public data class Success<T>(
        val data: T,
    ) : AsyncState<T>

    public data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : AsyncState<Nothing>
}
