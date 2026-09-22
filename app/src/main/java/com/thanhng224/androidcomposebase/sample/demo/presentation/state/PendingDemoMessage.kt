package com.thanhng224.androidcomposebase.sample.demo.presentation.state

import com.thanhng224.androidcomposebase.core.ui.text.UiText

data class PendingDemoMessage(
    val id: Long,
    val text: UiText,
    val actionLabel: UiText? = null,
    val action: DemoMessageAction? = null,
)

sealed interface DemoMessageAction {
    data object ResetCounter : DemoMessageAction
}
