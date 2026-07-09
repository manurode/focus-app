package com.stillness.focus.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stillness_prefs")

class AppPreferences(private val context: Context) {
    private val blockedAppsKey = stringSetPreferencesKey("blocked_apps")
    private val setupCompleteKey = booleanPreferencesKey("setup_complete")

    val blockedApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[blockedAppsKey].orEmpty()
    }

    val setupComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[setupCompleteKey] ?: false
    }

    suspend fun setBlockedApps(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[blockedAppsKey] = packages
        }
    }

    suspend fun setSetupComplete(complete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[setupCompleteKey] = complete
        }
    }

    fun getBlockedPackagesBlocking(): Set<String> = runBlocking {
        blockedApps.first()
    }

    fun isSetupCompleteBlocking(): Boolean = runBlocking {
        setupComplete.first()
    }
}
