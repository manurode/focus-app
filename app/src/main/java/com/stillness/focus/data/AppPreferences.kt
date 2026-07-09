package com.stillness.focus.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

    suspend fun recordAccomplished(packageName: String) {
        context.dataStore.edit { prefs ->
            val key = accomplishedKey(packageName)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun recordNotAccomplished(packageName: String) {
        context.dataStore.edit { prefs ->
            val key = notAccomplishedKey(packageName)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun recordPreventedEntry(packageName: String) {
        context.dataStore.edit { prefs ->
            val key = preventedEntryKey(packageName)
            prefs[key] = (prefs[key] ?: 0) + 1
        }
    }

    suspend fun getStats(packageName: String): PurposeStats {
        return context.dataStore.data.first().let { prefs ->
            PurposeStats(
                accomplished = prefs[accomplishedKey(packageName)] ?: 0,
                notAccomplished = prefs[notAccomplishedKey(packageName)] ?: 0,
                preventedEntries = prefs[preventedEntryKey(packageName)] ?: 0,
            )
        }
    }

    fun recordAccomplishedBlocking(packageName: String) = runBlocking {
        recordAccomplished(packageName)
    }

    fun recordNotAccomplishedBlocking(packageName: String) = runBlocking {
        recordNotAccomplished(packageName)
    }

    fun recordPreventedEntryBlocking(packageName: String) = runBlocking {
        recordPreventedEntry(packageName)
    }

    fun getStatsBlocking(packageName: String): PurposeStats = runBlocking {
        getStats(packageName)
    }

    suspend fun getStatsForApps(packageNames: Set<String>): Map<String, PurposeStats> {
        val prefs = context.dataStore.data.first()
        return packageNames.associateWith { packageName ->
            PurposeStats(
                accomplished = prefs[accomplishedKey(packageName)] ?: 0,
                notAccomplished = prefs[notAccomplishedKey(packageName)] ?: 0,
                preventedEntries = prefs[preventedEntryKey(packageName)] ?: 0,
            )
        }
    }

    fun getStatsForAppsBlocking(packageNames: Set<String>): Map<String, PurposeStats> = runBlocking {
        getStatsForApps(packageNames)
    }

    private fun accomplishedKey(packageName: String) =
        intPreferencesKey("stats_accomplished_$packageName")

    private fun notAccomplishedKey(packageName: String) =
        intPreferencesKey("stats_not_accomplished_$packageName")

    private fun preventedEntryKey(packageName: String) =
        intPreferencesKey("stats_prevented_entry_$packageName")
}
