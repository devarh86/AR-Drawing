package com.fahad.newtruelovebyfahad.ui.fragments.feature

import androidx.lifecycle.ViewModel
import com.fahad.newtruelovebyfahad.ui.fragments.common.TagsRVAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeaturedViewModel @Inject constructor(
) : ViewModel() {
    var currentPagerPosition = 0
}