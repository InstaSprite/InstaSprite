package com.instasprite.app.ui.gallery.contract

sealed interface BottomBarEvent {
    data object ToggleSearchBar : BottomBarEvent
    data object OpenSelectSortOption : BottomBarEvent
}