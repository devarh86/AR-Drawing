package com.fahad.newtruelovebyfahad.ui.activities.main
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    var isSplashVisible = true
    var runCheck = 0
}
