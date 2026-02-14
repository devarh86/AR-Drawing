package com.project.common.viewmodels

import androidx.lifecycle.ViewModel
import com.project.common.states.MultiFitStitchViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RatioViewModel : ViewModel() {


    private val _ratiosStateUpdatesFlow = MutableStateFlow<MultiFitStitchViewState>(MultiFitStitchViewState.Back)
    val ratiosStateUpdatesFlow: StateFlow<MultiFitStitchViewState?> get() = _ratiosStateUpdatesFlow

    private val _ratiosValueFlow = MutableStateFlow("3:4")
    val ratiosValueFlow: StateFlow<String?> get() = _ratiosValueFlow

    // Store the original and current ratio values
    private var previousRatio: String = "3:4" // Default initial ratio
    var dimensionRatio: String = "3:4"


    fun updateTick() {
        dimensionRatio = _ratiosValueFlow.value // Commit the updated ratio value
        _ratiosStateUpdatesFlow.value = MultiFitStitchViewState.Tick
    }



    fun updateRatioValue(value: String?) {
        value?.let {
            previousRatio = _ratiosValueFlow.value // Save the current value as the previous value before update
            _ratiosValueFlow.value = it
        }
    }


    fun updateCancel() {
        _ratiosValueFlow.value = previousRatio // Restore the previous value
        _ratiosStateUpdatesFlow.value = MultiFitStitchViewState.Back
    }


    fun resetViewState() {
        _ratiosStateUpdatesFlow.value = MultiFitStitchViewState.Idle
    }
}