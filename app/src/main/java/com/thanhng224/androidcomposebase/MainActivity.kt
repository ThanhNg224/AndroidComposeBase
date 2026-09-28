package com.thanhng224.androidcomposebase

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.thanhng224.androidcomposebase.core.ui.base.BaseComposeActivity
import com.thanhng224.androidcomposebase.presentation.AppRoot
import com.thanhng224.androidcomposebase.presentation.AppUiState
import com.thanhng224.androidcomposebase.presentation.AppViewModel
import com.thanhng224.androidcomposebase.startup.AppStartupCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {
    @Inject
    lateinit var startupCoordinator: AppStartupCoordinator

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            !startupCoordinator.isReady.value || appViewModel.uiState.value is AppUiState.Loading
        }
    }

    @Composable
    override fun Content() {
        AppRoot(appViewModel)
    }
}
