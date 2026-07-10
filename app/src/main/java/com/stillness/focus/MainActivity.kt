package com.stillness.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stillness.focus.data.InstalledApp
import com.stillness.focus.data.InstalledAppsRepository
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.data.aggregateStats
import com.stillness.focus.monitor.StillnessAccessibilityService
import com.stillness.focus.monitor.UnlockMonitorController
import com.stillness.focus.ui.components.RetainedRoute
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
                val unlockMonitoringEnabled by preferences.unlockMonitoringEnabled.collectAsStateWithLifecycle(initialValue = false)
                var route by remember { mutableStateOf<MainRoute?>(null) }
                var visitedRoutes by remember { mutableStateOf(setOf<MainRoute>()) }
                var selectedAppPackage by remember { mutableStateOf<String?>(null) }
                var accessibilityEnabled by remember { mutableStateOf(StillnessAccessibilityService.isEnabled(this)) }
                var appStats by remember { mutableStateOf<List<AppStatsEntry>>(emptyList()) }
                var globalStats by remember { mutableStateOf(PurposeStats()) }
                var unlockStats by remember { mutableStateOf(PurposeStats()) }
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
                    unlockStats = preferences.getUnlockStats()
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

                LaunchedEffect(route) {
                    route?.let { current ->
                        if (current !in visitedRoutes) {
                            visitedRoutes = visitedRoutes + current
                        }
                    }
                }

                LaunchedEffect(blockedApps, route) {
                    if (route in protectedRoutes) {
                        refreshStats()
                    }
                }

                BackHandler(
                    enabled = when (route) {
                        MainRoute.Stats, MainRoute.AppStats -> true
                        MainRoute.Setup -> setupComplete
                        else -> false
                    },
                ) {
                    when (route) {
                        MainRoute.Stats -> route = MainRoute.Home
                        MainRoute.AppStats -> route = MainRoute.Stats
                        MainRoute.Setup -> route = routeAfterSetup()
                        else -> Unit
                    }
                }

                when (route) {
                    MainRoute.Setup -> if (!setupComplete) {
                        SetupScreen(
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
                    } else {
                        MainAppNavigation(
                            route = MainRoute.Setup,
                            visitedRoutes = visitedRoutes,
                            blockedApps = blockedApps,
                            globalStats = globalStats,
                            unlockStats = unlockStats,
                            unlockMonitoringEnabled = unlockMonitoringEnabled,
                            appStats = appStats,
                            selectedAppPackage = selectedAppPackage,
                            installedApps = installedApps,
                            appsRepository = appsRepository,
                            onRouteChange = { route = it },
                            onSelectApp = { selectedAppPackage = it },
                            onSaveApps = { selected ->
                                scope.launch {
                                    preferences.setBlockedApps(selected)
                                    route = routeAfterSetup()
                                }
                            },
                            onSetupBack = { route = routeAfterSetup() },
                            onUnlockMonitoringChange = { enabled ->
                                scope.launch {
                                    preferences.setUnlockMonitoringEnabled(enabled)
                                    UnlockMonitorController.sync(this@MainActivity)
                                }
                            },
                        )
                    }

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

                    MainRoute.Home,
                    MainRoute.Stats,
                    MainRoute.AppStats,
                    -> {
                        val currentRoute = route!!
                        MainAppNavigation(
                            route = currentRoute,
                        visitedRoutes = visitedRoutes,
                        blockedApps = blockedApps,
                        globalStats = globalStats,
                        unlockStats = unlockStats,
                        unlockMonitoringEnabled = unlockMonitoringEnabled,
                        appStats = appStats,
                        selectedAppPackage = selectedAppPackage,
                        installedApps = installedApps,
                        appsRepository = appsRepository,
                        onRouteChange = { route = it },
                        onSelectApp = { selectedAppPackage = it },
                        onSaveApps = { selected ->
                            scope.launch {
                                preferences.setBlockedApps(selected)
                                route = routeAfterSetup()
                            }
                        },
                        onSetupBack = { route = routeAfterSetup() },
                        onUnlockMonitoringChange = { enabled ->
                            scope.launch {
                                preferences.setUnlockMonitoringEnabled(enabled)
                                UnlockMonitorController.sync(this@MainActivity)
                            }
                        },
                        )
                    }

                    null -> Unit
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        UnlockMonitorController.sync(this)
    }

    @Composable
    private fun MainAppNavigation(
        route: MainRoute,
        visitedRoutes: Set<MainRoute>,
        blockedApps: Set<String>,
        globalStats: PurposeStats,
        unlockStats: PurposeStats,
        unlockMonitoringEnabled: Boolean,
        appStats: List<AppStatsEntry>,
        selectedAppPackage: String?,
        installedApps: List<InstalledApp>,
        appsRepository: InstalledAppsRepository,
        onRouteChange: (MainRoute) -> Unit,
        onSelectApp: (String) -> Unit,
        onSaveApps: (Set<String>) -> Unit,
        onSetupBack: () -> Unit,
        onUnlockMonitoringChange: (Boolean) -> Unit,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (MainRoute.Home in visitedRoutes || route == MainRoute.Home) {
                RetainedRoute(visible = route == MainRoute.Home) {
                    HomeScreen(
                        blockedCount = blockedApps.size,
                        globalStats = globalStats,
                        unlockMonitoringEnabled = unlockMonitoringEnabled,
                        unlockStats = unlockStats,
                        onUnlockMonitoringChange = onUnlockMonitoringChange,
                        onViewStats = { onRouteChange(MainRoute.Stats) },
                        onEditApps = { onRouteChange(MainRoute.Setup) },
                    )
                }
            }

            if (MainRoute.Stats in visitedRoutes || route == MainRoute.Stats) {
                RetainedRoute(visible = route == MainRoute.Stats) {
                    StatsOverviewScreen(
                        appStats = appStats,
                        unlockStats = unlockStats,
                        unlockMonitoringEnabled = unlockMonitoringEnabled,
                        onBack = { onRouteChange(MainRoute.Home) },
                        onAppClick = { packageName ->
                            onSelectApp(packageName)
                            onRouteChange(MainRoute.AppStats)
                        },
                    )
                }
            }

            if (MainRoute.AppStats in visitedRoutes || route == MainRoute.AppStats) {
                RetainedRoute(visible = route == MainRoute.AppStats) {
                    val packageName = selectedAppPackage
                    if (packageName != null) {
                        key(packageName) {
                            val entry = appStats.find { it.app.packageName == packageName }
                            AppStatsDetailScreen(
                                appLabel = entry?.app?.label ?: appsRepository.getAppLabel(packageName),
                                stats = entry?.stats ?: PurposeStats(),
                                onBack = { onRouteChange(MainRoute.Stats) },
                            )
                        }
                    }
                }
            }

            if (MainRoute.Setup in visitedRoutes || route == MainRoute.Setup) {
                RetainedRoute(visible = route == MainRoute.Setup) {
                    SetupScreen(
                        apps = installedApps,
                        initialSelection = blockedApps,
                        onSave = onSaveApps,
                        onBack = onSetupBack,
                    )
                }
            }
        }
    }
}
