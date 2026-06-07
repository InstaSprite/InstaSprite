package com.instasprite.app.ui.tutorial

import androidx.compose.ui.geometry.Rect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface TutorialStep {
    val titleRes: Int
    val descriptionRes: Int
}

data class TutorialState(
    val showWelcomeDialog: Boolean = false,
    val showTutorial: Boolean = false,
    val currentStep: TutorialStep? = null,
    val bounds: Map<TutorialStep, Rect> = emptyMap()
)

sealed class TutorialEvent {
    data class OnBoundsChanged(val step: TutorialStep, val rect: Rect) : TutorialEvent()
    object OnStartTutorial : TutorialEvent()
    object OnNextStep : TutorialEvent()
    object OnDismiss : TutorialEvent()
}

class TutorialManager(
    private val sequence: List<TutorialStep>,
    hasSeenTutorial: Boolean,
    private val onCompleteTutorial: () -> Unit
) {
    private val _state = MutableStateFlow(
        TutorialState(
            showWelcomeDialog = !hasSeenTutorial && sequence.isNotEmpty(),
            showTutorial = false,
            currentStep = sequence.firstOrNull()
        )
    )
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    fun onEvent(event: TutorialEvent) {
        when (event) {
            is TutorialEvent.OnBoundsChanged -> {
                val newBounds = _state.value.bounds.toMutableMap()
                newBounds[event.step] = event.rect
                _state.value = _state.value.copy(bounds = newBounds)
            }
            is TutorialEvent.OnStartTutorial -> {
                _state.value = _state.value.copy(
                    showWelcomeDialog = false,
                    showTutorial = true
                )
            }
            is TutorialEvent.OnNextStep -> {
                val currentStep = _state.value.currentStep
                val currentIndex = sequence.indexOf(currentStep)
                
                val nextStep = if (currentIndex != -1 && currentIndex < sequence.size - 1) {
                    sequence[currentIndex + 1]
                } else {
                    null
                }
                
                if (nextStep == null) {
                    dismissTutorial()
                } else {
                    _state.value = _state.value.copy(currentStep = nextStep)
                }
            }
            is TutorialEvent.OnDismiss -> {
                dismissTutorial()
            }
        }
    }

    private fun dismissTutorial() {
        _state.value = _state.value.copy(
            showWelcomeDialog = false,
            showTutorial = false,
            currentStep = null
        )
        onCompleteTutorial()
    }
}
