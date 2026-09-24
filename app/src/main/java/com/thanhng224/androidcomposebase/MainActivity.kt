package com.thanhng224.androidcomposebase

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.thanhng224.androidcomposebase.presentation.AppRoot
import com.thanhng224.androidcomposebase.startup.AppStartupCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var startupCoordinator: AppStartupCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !startupCoordinator.isReady.value }
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}
