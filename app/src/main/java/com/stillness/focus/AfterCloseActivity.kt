package com.stillness.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.stillness.focus.data.InstalledAppsRepository
import com.stillness.focus.data.PurposeStats
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.ui.screens.AfterCloseScreen
import com.stillness.focus.ui.screens.PurposeStatsScreen
import com.stillness.focus.ui.theme.StillnessTheme

class AfterCloseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val closedPackage = intent.getStringExtra(EXTRA_CLOSED_PACKAGE)
        val purposeNote = SessionManager.purposeNote

        if (closedPackage == null) {
            SessionManager.isAfterScreenShowing.set(false)
            finish()
            return
        }

        val preferences = (application as StillnessApp).preferences
        val appLabel = InstalledAppsRepository(this).getAppLabel(closedPackage)

        var showStats by mutableStateOf(false)
        var stats by mutableStateOf(PurposeStats())

        setContent {
            StillnessTheme {
                if (showStats) {
                    PurposeStatsScreen(
                        appLabel = appLabel,
                        stats = stats,
                        onContinue = { finishReflection() },
                    )
                } else {
                    AfterCloseScreen(
                        purposeNote = purposeNote,
                        onNo = {
                            preferences.recordNotAccomplishedBlocking(closedPackage)
                            stats = preferences.getStatsBlocking(closedPackage)
                            showStats = true
                        },
                        onYes = {
                            preferences.recordAccomplishedBlocking(closedPackage)
                            finishReflection()
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        SessionManager.isAfterScreenShowing.set(false)
        super.onDestroy()
    }

    private fun finishReflection() {
        SessionManager.onAfterScreenDismissed()
        finish()
    }

    companion object {
        private const val EXTRA_CLOSED_PACKAGE = "extra_closed_package"

        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, AfterCloseActivity::class.java).apply {
                putExtra(EXTRA_CLOSED_PACKAGE, packageName)
            }
        }
    }
}
