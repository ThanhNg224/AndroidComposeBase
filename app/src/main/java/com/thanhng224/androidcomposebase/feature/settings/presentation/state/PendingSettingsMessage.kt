package com.thanhng224.androidcomposebase.feature.settings.presentation.state

import com.thanhng224.androidcomposebase.core.text.UiText

data class PendingSettingsMessage(
    val id: Long,
    val text: UiText,
)
