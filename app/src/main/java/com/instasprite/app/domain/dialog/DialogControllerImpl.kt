package com.instasprite.app.domain.dialog

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DialogControllerImpl<Dialog> : DialogController<Dialog> {

    private val _dialogState = MutableStateFlow<List<Dialog>>(emptyList())
    override val dialogState: StateFlow<List<Dialog>> = _dialogState.asStateFlow()

    override fun openDialog(dialog: Dialog) {
        _dialogState.update { it + dialog }
    }

    override fun closeTopDialog() {
        _dialogState.update { if (it.isNotEmpty()) it.dropLast(1) else it }
    }

    override fun closeAllDialogs() {
        _dialogState.value = emptyList()
    }
}