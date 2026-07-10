package com.stillness.focus.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private val unlockMonitoringKey = booleanPreferencesKey("unlock_monitoring_enabled")
    private val pendingUnlockReflectionKey = booleanPreferencesKey("pending_unlock_reflection")
    private val pendingUnlockPurposeKey = stringPreferencesKey("pending_unlock_purpose")
    private val pendingUnlockAudioPathKey = stringPreferencesKey("pending_unlock_audio_path")
    private val pendingUnlockAudioDurationKey = longPreferencesKey("pending_unlock_audio_duration")
    private val pendingUnlockWaveformKey = stringPreferencesKey("pending_unlock_waveform")

    val blockedApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[blockedAppsKey].orEmpty()
    }

    val setupComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[setupCompleteKey] ?: false
    }

    val unlockMonitoringEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[unlockMonitoringKey] ?: false
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

    suspend fun setUnlockMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[unlockMonitoringKey] = enabled
        }
    }

    fun getBlockedPackagesBlocking(): Set<String> = runBlocking {
        blockedApps.first()
    }

    fun isSetupCompleteBlocking(): Boolean = runBlocking {
        setupComplete.first()
    }

    fun isUnlockMonitoringEnabledBlocking(): Boolean = runBlocking {
        unlockMonitoringEnabled.first()
    }

    suspend fun getUnlockStats(): PurposeStats = getStats(UNLOCK_STATS_ID)

    fun getUnlockStatsBlocking(): PurposeStats = getStatsBlocking(UNLOCK_STATS_ID)

    suspend fun savePendingUnlockReflection(
        purpose: String,
        audioPath: String?,
        audioDurationMs: Long,
        waveformSamples: List<Float>,
    ) {
        context.dataStore.edit { prefs ->
            prefs[pendingUnlockReflectionKey] = true
            prefs[pendingUnlockPurposeKey] = purpose
            if (audioPath != null) {
                prefs[pendingUnlockAudioPathKey] = audioPath
            } else {
                prefs.remove(pendingUnlockAudioPathKey)
            }
            prefs[pendingUnlockAudioDurationKey] = audioDurationMs
            prefs[pendingUnlockWaveformKey] = encodeWaveform(waveformSamples)
        }
    }

    fun savePendingUnlockReflectionBlocking(
        purpose: String,
        audioPath: String?,
        audioDurationMs: Long,
        waveformSamples: List<Float>,
    ) = runBlocking {
        savePendingUnlockReflection(purpose, audioPath, audioDurationMs, waveformSamples)
    }

    suspend fun loadPendingUnlockReflection(): PendingUnlockReflection? {
        val prefs = context.dataStore.data.first()
        if (prefs[pendingUnlockReflectionKey] != true) return null
        return PendingUnlockReflection(
            purpose = prefs[pendingUnlockPurposeKey].orEmpty(),
            audioPath = prefs[pendingUnlockAudioPathKey],
            audioDurationMs = prefs[pendingUnlockAudioDurationKey] ?: 0L,
            waveformSamples = decodeWaveform(prefs[pendingUnlockWaveformKey]),
        )
    }

    fun loadPendingUnlockReflectionBlocking(): PendingUnlockReflection? = runBlocking {
        loadPendingUnlockReflection()
    }

    suspend fun clearPendingUnlockReflection() {
        context.dataStore.edit { prefs ->
            prefs.remove(pendingUnlockReflectionKey)
            prefs.remove(pendingUnlockPurposeKey)
            prefs.remove(pendingUnlockAudioPathKey)
            prefs.remove(pendingUnlockAudioDurationKey)
            prefs.remove(pendingUnlockWaveformKey)
        }
    }

    fun clearPendingUnlockReflectionBlocking() = runBlocking {
        clearPendingUnlockReflection()
    }

    suspend fun hasPendingUnlockReflection(): Boolean {
        return context.dataStore.data.first()[pendingUnlockReflectionKey] == true
    }

    fun hasPendingUnlockReflectionBlocking(): Boolean = runBlocking {
        hasPendingUnlockReflection()
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

    private fun encodeWaveform(samples: List<Float>): String =
        samples.joinToString(",") { it.toString() }

    private fun decodeWaveform(raw: String?): List<Float> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(",").mapNotNull { it.toFloatOrNull() }
    }

    companion object {
        const val UNLOCK_STATS_ID = "__device_unlock__"
    }
}

data class PendingUnlockReflection(
    val purpose: String,
    val audioPath: String?,
    val audioDurationMs: Long,
    val waveformSamples: List<Float>,
)
