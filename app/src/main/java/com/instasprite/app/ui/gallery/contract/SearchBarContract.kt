package com.instasprite.app.ui.gallery.contract

sealed interface SearchBarContract {
    data object ToggleSearchBar : SearchBarContract
    data class UpdateSearchQuery(val query: String) : SearchBarContract
}
