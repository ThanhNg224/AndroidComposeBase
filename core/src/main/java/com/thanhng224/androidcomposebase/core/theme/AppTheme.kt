package com.thanhng224.androidcomposebase.core.theme

public enum class AppTheme(
    public val key: String,
) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system"),
    ;

    public companion object {
        public fun fromKey(key: String): AppTheme = values().firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SYSTEM
    }
}
