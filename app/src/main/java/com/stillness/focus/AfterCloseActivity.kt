package com.stillness.focus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stillness.focus.monitor.SessionManager
import com.stillness.focus.ui.screens.AfterCloseScreen
import com.stillness.focus.ui.theme.StillnessTheme

class AfterCloseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val closedPackage = intent.getStringExtra(EXTRA_CLOSED_PACKAGE)
        val purposeNote = SessionManager.purposeNote

        setContent {
            StillnessTheme {
                AfterCloseScreen(
                    purposeNote = purposeNote,
                    onNo = { finishReflection() },
                    onYes = { finishReflection() },
                )
            }
        }

        if (closedPackage == null) {
            SessionManager.isAfterScreenShowing.set(false)
            finish()
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
