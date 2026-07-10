package com.stillness.focus.monitor

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.stillness.focus.StillnessApp

object UnlockMonitorController {
    private const val TAG = "UnlockMonitor"

    fun sync(context: Context) {
        val appContext = context.applicationContext
        val preferences = (appContext as StillnessApp).preferences
        val shouldRun = preferences.isSetupCompleteBlocking() &&
            preferences.isUnlockMonitoringEnabledBlocking()

        Log.d(TAG, "sync: shouldRun=$shouldRun")

        if (shouldRun) {
            start(appContext)
        } else {
            stop(appContext)
        }
    }

    fun start(context: Context) {
        val appContext = context.applicationContext
        val intent = Intent(appContext, UnlockMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
            Log.d(TAG, "start: foreground service requested")
        } catch (error: Exception) {
            Log.e(TAG, "start: failed to start service", error)
        }
    }

    fun stop(context: Context) {
        val appContext = context.applicationContext
        appContext.stopService(Intent(appContext, UnlockMonitorService::class.java))
        Log.d(TAG, "stop: service stop requested")
    }
}
