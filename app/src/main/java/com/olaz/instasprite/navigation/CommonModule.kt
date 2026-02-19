package com.olaz.instasprite.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.EntryProviderScope
import dagger.hilt.android.scopes.ActivityRetainedScoped

typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit

@ActivityRetainedScoped
class Navigator(
    startDestination: Any,
    val eventBus: ResultEventBus
) {

    val backStack: SnapshotStateList<Any> =
        mutableStateListOf(startDestination)

    var isNavigating = false

    fun goTo(destination: Any) {
        if (isNavigating) return
        isNavigating = true

        backStack.add(destination)
    }

    fun goBack() {
        if (isNavigating) return
        if (backStack.size <= 1) return

        isNavigating = true
        backStack.removeLastOrNull()
    }


    inline fun <reified T> goBackWithResult(result: T) {
        if (isNavigating) return
        if (backStack.size <= 1) return

        isNavigating = true
        eventBus.sendResult<T>(result = result)
        backStack.removeLastOrNull()
    }

    fun replace(destination: Any) {
        if (isNavigating) return
        if (backStack.isEmpty()) return

        isNavigating = true

        backStack.removeLastOrNull()
        backStack.add(destination)
    }

    fun onTransitionComplete() {
        isNavigating = false
    }
}