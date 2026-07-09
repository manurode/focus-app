package com.stillness.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stillness.focus.data.InstalledAppsRepository
import com.stillness.focus.monitor.StillnessAccessibilityService
import com.stillness.focus.ui.screens.HomeScreen
import com.stillness.focus.ui.screens.PermissionsScreen
import com.stillness.focus.ui.screens.SetupScreen
import com.stillness.focus.ui.theme.StillnessTheme
import com.stillness.focus.util.openAccessibilitySettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private enum class MainRoute {
        Setup,
        Permissions,
        Home,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = (application as StillnessApp).preferences
        val appsRepository = InstalledAppsRepository(this)

        setContent {
            StillnessTheme {
                val setupComplete by preferences.setupComplete.collectAsStateWithLifecycle(initialValue = false)
                val blockedApps by preferences.blockedApps.collectAsStateWithLifecycle(initialValue = emptySet())
                var route by remember { mutableStateOf<MainRoute?>(null) }
                var accessibilityEnabled by remember { mutableStateOf(StillnessAccessibilityService.isEnabled(this)) }
                val scope = rememberCoroutineScope()
                val installedApps = remember { appsRepository.getLaunchableApps() }

                LaunchedEffect(setupComplete) {
                    route = when {
                        !setupComplete -> MainRoute.Setup
                        else -> MainRoute.Home
                    }
                }

                when (route) {
                    MainRoute.Setup -> SetupScreen(
                        apps = installedApps,
                        initialSelection = blockedApps,
                        onSave = { selected ->
                            scope.launch {
                                preferences.setBlockedApps(selected)
                                preferences.setSetupComplete(true)
                                route = MainRoute.Permissions
                            }
                        },
                    )

                    MainRoute.Permissions -> PermissionsScreen(
                        accessibilityEnabled = accessibilityEnabled,
                        onEnableAccessibility = {
                            openAccessibilitySettings(this)
                        },
                        onContinue = {
                            accessibilityEnabled = StillnessAccessibilityService.isEnabled(this)
                            route = MainRoute.Home
                        },
                    )

                    MainRoute.Home -> HomeScreen(
                        blockedCount = blockedApps.size,
                        accessibilityEnabled = accessibilityEnabled,
                        onEditApps = { route = MainRoute.Setup },
                        onEnableAccessibility = { openAccessibilitySettings(this) },
                    )

                    null -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh accessibility state when returning from Settings.
        invalidateOptionsMenu()
    }
}
