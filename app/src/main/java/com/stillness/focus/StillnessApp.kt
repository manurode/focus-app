package com.stillness.focus

import android.app.Application
import com.stillness.focus.data.AppPreferences

class StillnessApp : Application() {
    lateinit var preferences: AppPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = AppPreferences(this)
    }
}
