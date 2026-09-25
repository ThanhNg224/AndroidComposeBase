package com.thanhng224.androidcomposebase.core.network.auth

/** Default `Authorization` header scheme prefix, e.g. `Bearer <token>`. */
public const val DEFAULT_AUTH_SCHEME: String = "Bearer"

/**
 * Formats [token] for the `Authorization` header under [scheme]. A blank [scheme] sends [token]
 * with no prefix.
 */
internal fun authorizationValue(
    scheme: String,
    token: String,
): String = if (scheme.isBlank()) token else "$scheme $token"
