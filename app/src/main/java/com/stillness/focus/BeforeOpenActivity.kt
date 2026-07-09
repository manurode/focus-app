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
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.ui.screens.BeforeOpenScreen
import com.stillness.focus.ui.theme.StillnessTheme

class BeforeOpenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE)
        if (targetPackage == null) {
            SessionManager.onBeforeScreenDismissed()
            finish()
            return
        }

        val appLabel = InstalledAppsRepository(this).getAppLabel(targetPackage)
        var purpose by mutableStateOf("")

        setContent {
            StillnessTheme {
                BeforeOpenScreen(
                    appLabel = appLabel,
                    purpose = purpose,
                    onPurposeChange = { purpose = it },
                    onProceed = {
                        SessionManager.grantAccess(targetPackage, purpose.trim())
                        launchTargetApp(targetPackage)
                        finish()
                    },
                    onBack = {
                        SessionManager.onBeforeScreenDismissed()
                        goHome()
                        finish()
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        SessionManager.onBeforeScreenDismissed()
        super.onDestroy()
    }

    private fun launchTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
        )
    }

    companion object {
        private const val EXTRA_TARGET_PACKAGE = "extra_target_package"

        fun createIntent(context: Context, packageName: String): Intent {
            return Intent(context, BeforeOpenActivity::class.java).apply {
                putExtra(EXTRA_TARGET_PACKAGE, packageName)
            }
        }
    }
}
