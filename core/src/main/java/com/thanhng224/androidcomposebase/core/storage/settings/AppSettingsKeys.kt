package com.thanhng224.androidcomposebase.core.storage.settings

import com.thanhng224.androidcomposebase.core.foundation.SettingsKey

public object AppSettingsKeys {
    public val THEME_MODE: SettingsKey.StringKey = SettingsKey.StringKey(name = "theme_mode", defaultValue = "system")
}
