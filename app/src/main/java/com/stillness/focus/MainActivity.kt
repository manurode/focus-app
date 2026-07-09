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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stillness.focus.data.InstalledApp
import com.stillness.focus.data.InstalledAppsRepository
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.data.aggregateStats
import com.stillness.focus.monitor.StillnessAccessibilityService
import com.stillness.focus.ui.screens.AppStatsDetailScreen
import com.stillness.focus.ui.screens.AppStatsEntry
import com.stillness.focus.ui.screens.HomeScreen
import com.stillness.focus.ui.screens.PermissionsScreen
import com.stillness.focus.ui.screens.SetupScreen
import com.stillness.focus.ui.screens.StatsOverviewScreen
import com.stillness.focus.ui.theme.StillnessTheme
import com.stillness.focus.util.openAccessibilitySettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private enum class MainRoute {
        Setup,
        Permissions,
        Home,
        Stats,
        AppStats,
    }

    private val protectedRoutes = setOf(
        MainRoute.Home,
        MainRoute.Stats,
        MainRoute.AppStats,
    )

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
                var selectedAppPackage by remember { mutableStateOf<String?>(null) }
                var accessibilityEnabled by remember { mutableStateOf(StillnessAccessibilityService.isEnabled(this)) }
                var appStats by remember { mutableStateOf<List<AppStatsEntry>>(emptyList()) }
                var globalStats by remember { mutableStateOf(PurposeStats()) }
                val scope = rememberCoroutineScope()
                val installedApps = remember { appsRepository.getLaunchableApps() }
                val installedAppsByPackage = remember(installedApps) {
                    installedApps.associateBy { it.packageName }
                }

                fun isAccessibilityEnabled() = StillnessAccessibilityService.isEnabled(this)

                fun routeAfterSetup(): MainRoute =
                    if (isAccessibilityEnabled()) MainRoute.Home else MainRoute.Permissions

                suspend fun refreshStats() {
                    val statsByApp = preferences.getStatsForApps(blockedApps)
                    globalStats = aggregateStats(statsByApp)
                    appStats = blockedApps.map { packageName ->
                        val app = installedAppsByPackage[packageName]
                            ?: InstalledApp(
                                packageName = packageName,
                                label = appsRepository.getAppLabel(packageName),
                                icon = packageManager.defaultActivityIcon,
                            )
                        AppStatsEntry(app = app, stats = statsByApp[packageName] ?: PurposeStats())
                    }
                }

                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                    val enabled = isAccessibilityEnabled()
                    accessibilityEnabled = enabled
                    scope.launch { refreshStats() }

                    when {
                        !setupComplete -> Unit
                        !enabled && route in protectedRoutes -> route = MainRoute.Permissions
                        enabled && route == MainRoute.Permissions -> route = MainRoute.Home
                    }
                }

                LaunchedEffect(setupComplete) {
                    route = when {
                        !setupComplete -> MainRoute.Setup
                        !isAccessibilityEnabled() -> MainRoute.Permissions
                        else -> MainRoute.Home
                    }
                }

                LaunchedEffect(blockedApps, route) {
                    if (route in protectedRoutes) {
                        refreshStats()
                    }
                }

                when (route) {
                    MainRoute.Setup -> SetupScreen(
                        apps = installedApps,
                        initialSelection = blockedApps,
                        onSave = { selected ->
                            scope.launch {
                                preferences.setBlockedApps(selected)
                                route = if (setupComplete) {
                                    routeAfterSetup()
                                } else {
                                    preferences.setSetupComplete(true)
                                    MainRoute.Permissions
                                }
                            }
                        },
                        onBack = if (setupComplete) {
                            { route = routeAfterSetup() }
                        } else {
                            null
                        },
                    )

                    MainRoute.Permissions -> PermissionsScreen(
                        accessibilityEnabled = accessibilityEnabled,
                        onEnableAccessibility = {
                            openAccessibilitySettings(this)
                        },
                        onContinue = {
                            accessibilityEnabled = isAccessibilityEnabled()
                            if (accessibilityEnabled) {
                                route = MainRoute.Home
                            }
                        },
                    )

                    MainRoute.Home -> HomeScreen(
                        blockedCount = blockedApps.size,
                        globalStats = globalStats,
                        onViewStats = { route = MainRoute.Stats },
                        onEditApps = { route = MainRoute.Setup },
                    )

                    MainRoute.Stats -> StatsOverviewScreen(
                        appStats = appStats,
                        onBack = { route = MainRoute.Home },
                        onAppClick = { packageName ->
                            selectedAppPackage = packageName
                            route = MainRoute.AppStats
                        },
                    )

                    MainRoute.AppStats -> {
                        val packageName = selectedAppPackage
                        if (packageName != null) {
                            val entry = appStats.find { it.app.packageName == packageName }
                            AppStatsDetailScreen(
                                appLabel = entry?.app?.label ?: appsRepository.getAppLabel(packageName),
                                stats = entry?.stats ?: PurposeStats(),
                                onBack = { route = MainRoute.Stats },
                            )
                        }
                    }

                    null -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
    }
}
