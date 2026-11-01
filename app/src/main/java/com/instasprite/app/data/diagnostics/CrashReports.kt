package com.instasprite.app.data.diagnostics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instasprite.app.BuildConfig
import kotlinx.coroutines.CancellationException

object CrashReports {
    val available: Boolean get() = BuildConfig.CRASH_REPORTING_CONFIGURED
    @Volatile private var enabled = false

    @Synchronized
    fun setEnabled(value: Boolean) {
        enabled = value && available
        if (!available) return
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
        if (!enabled) crashlytics.deleteUnsentReports()
    }

    @Synchronized
    fun record(operation: String, error: Throwable) {
        if (!enabled || error is CancellationException || error is OutOfMemoryError) return
        val sanitized = RuntimeException("$operation: ${error.javaClass.name}")
        sanitized.stackTrace = error.stackTrace
        FirebaseCrashlytics.getInstance().recordException(sanitized)
    }
}
