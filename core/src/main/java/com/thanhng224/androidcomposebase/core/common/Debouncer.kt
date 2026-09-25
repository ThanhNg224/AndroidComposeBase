package com.thanhng224.androidcomposebase.core.common

/**
 * Pure, JVM-testable rate limiter: [shouldAllow] only returns true once per [intervalMs] window,
 * based on caller-supplied timestamps (no dependency on any clock).
 */
public class Debouncer(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
) {
    private var lastAllowedAtMs: Long = 0L

    public fun shouldAllow(nowMs: Long): Boolean {
        if (nowMs - lastAllowedAtMs < intervalMs) return false
        lastAllowedAtMs = nowMs
        return true
    }

    public companion object {
        public const val DEFAULT_INTERVAL_MS: Long = 600L
    }
}
