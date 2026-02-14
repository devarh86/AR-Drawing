package com.project.crop.ui.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.crop.ui.main.intent.MainIntent
import com.project.crop.ui.main.viewstate.RotateViewState
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class RotateViewModel() : ViewModel() {

    val rotateIntent: Channel<MainIntent> = Channel(Channel.UNLIMITED)

    private val _state = MutableLiveData<RotateViewState>()

    val state: LiveData<RotateViewState> get() = _state

    private val _updateState = MutableLiveData<Float>()

    val updateState: LiveData<Float> get() = _updateState

    private val _updateUI = MutableLiveData<String>()
    val updateUI: LiveData<String> get() = _updateUI
    
//    private var lastStateRotationList: MutableList<Float> = mutableListOf()
//
//    private var currentStateRotationList: MutableList<Float> = mutableListOf()

    var currentRotation = 0f

    var currentRotationForRuleView = 0f

    var currentIndex = 0

    var applyingRotation = false

    var maxIndex = 0

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch(Main) {
            rotateIntent.consumeAsFlow().collect {
                when (it) {
                    is MainIntent.RotateImage -> {
                        rotate(it.rotaion)
                    }

                    is MainIntent.RotateImageFromIcon -> {
                        rotateImageFromIcon(it.rotaion)
                    }

                    is MainIntent.ResetRotation -> {
                        resetRotation()
                    }
                }
            }
        }
    }

    fun updateRotateUi(){
        _updateUI.postValue("Update")
    }

//    fun addAllInCurrentState() {
//        currentStateRotationList.clear()
//        currentStateRotationList.addAll(lastStateRotationList)
//    }

//    fun addInLastState() {
//        val previousRotation = currentRotation
//        if (currentIndex < currentStateRotationList.size && currentIndex >= 0) {
//            currentStateRotationList[currentIndex] = previousRotation
//        } else {
//            val start = currentStateRotationList.size
//            for (i in start..maxIndex) {
//                if (i == currentIndex) {
//                    currentStateRotationList.add(previousRotation)
//                } else {
//                    currentStateRotationList.add(-1f)
//                }
//            }
//        }
//    }

    fun updateTick() {
//        viewModelScope.launch {
//            addInLastState()
//            lastStateRotationList.clear()
//            lastStateRotationList.addAll(currentStateRotationList)
//            withContext(Main) {
//                _state.value = RotateViewState.Tick
//            }
//        }
        _state.value = RotateViewState.Tick
    }

    fun updateCancel() {

//        if (lastStateRotationList.isEmpty()) {
//            _state.value = RotateViewState.UpdateRotation(0f)
//        } else {
//            lastStateRotationList.forEach {
//                if (it != -1f) _state.value = RotateViewState.UpdateRotation(it)
//            }
//        }
        _state.value = RotateViewState.Back
    }

    private fun resetRotation() {
        _state.value = RotateViewState.ResetRotation()
    }

    private fun rotate(rotation: Float) {
        _state.value = RotateViewState.UpdateRotation(rotation = rotation)
    }

    private fun rotateImageFromIcon(rotation: Float) {

        currentRotation = rotation
        _state.value = RotateViewState.UpdateRotationFromIcon(rotation = rotation)
    }

    fun updateCurrentValue() {
        _updateState.value = currentRotationForRuleView
    }

    fun updateCurrentValue(index: Int, rotation: Float) {


//                val start = lastStateRotationList.size
//                for (i in start..maxIndex) {
//                    if (i == index) {
//                        lastStateRotationList.add(rotation)
//                    } else {
//                        lastStateRotationList.add(rotation)
//                    }
//                }
//                lastStateRotationList.add(rotation)
//            }
//        if(index )
        if (currentIndex == index) {
            currentIndex = index
            currentRotation = rotation
            _updateState.value = currentRotation
        }
    }

    fun resetState() {
        _state.value = RotateViewState.Idle
    }
}