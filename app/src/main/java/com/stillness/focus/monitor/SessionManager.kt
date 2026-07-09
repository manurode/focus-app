package com.stillness.focus.monitor

import java.util.concurrent.atomic.AtomicBoolean

object SessionManager {
    @Volatile
    var allowedPackage: String? = null

    @Volatile
    var activeBlockedPackage: String? = null

    @Volatile
    var purposeNote: String = ""

    val isBeforeScreenShowing = AtomicBoolean(false)
    val isAfterScreenShowing = AtomicBoolean(false)

    fun grantAccess(packageName: String, purpose: String) {
        allowedPackage = packageName
        activeBlockedPackage = packageName
        purposeNote = purpose
        isBeforeScreenShowing.set(false)
    }

    fun markActiveInBlockedApp(packageName: String) {
        activeBlockedPackage = packageName
    }

    fun clearSession() {
        allowedPackage = null
        activeBlockedPackage = null
        purposeNote = ""
        isBeforeScreenShowing.set(false)
        isAfterScreenShowing.set(false)
    }

    fun onBeforeScreenDismissed() {
        isBeforeScreenShowing.set(false)
    }

    fun onAfterScreenDismissed() {
        isAfterScreenShowing.set(false)
        clearSession()
    }
}
