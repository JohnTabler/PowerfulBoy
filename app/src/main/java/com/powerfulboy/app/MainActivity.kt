package com.powerfulboy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.powerfulboy.app.data.repository.SettingsRepository
import com.powerfulboy.app.ui.PowerfulBoyNavGraph
import com.powerfulboy.app.ui.Screen
import com.powerfulboy.app.ui.theme.PowerfulBoyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var startDestination = Screen.Onboarding.route
        var isReady = false

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            startDestination = if (settings?.onboardingDone == true) {
                Screen.Dashboard.route
            } else {
                Screen.Onboarding.route
            }
            isReady = true
        }

        // Wait for the coroutine to finish before setting content
        // We use a simple approach: set content after checking
        lifecycleScope.launch {
            // Give the above coroutine time to complete
            val settings = settingsRepository.getSettingsOnce()
            val start = if (settings?.onboardingDone == true) Screen.Dashboard.route else Screen.Onboarding.route

            enableEdgeToEdge()
            setContent {
                PowerfulBoyTheme {
                    PowerfulBoyNavGraph(startDestination = start)
                }
            }
        }
    }
}
