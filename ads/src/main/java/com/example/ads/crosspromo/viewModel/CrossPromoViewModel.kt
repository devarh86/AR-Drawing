package com.example.ads.crosspromo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.crosspromo.api.CrossPromoCallRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrossPromoViewModel @Inject constructor(
    app: Application,
    private val crossPromoCallRepo: CrossPromoCallRepo
) : AndroidViewModel(app) {

    val crossPromoAds get() = crossPromoCallRepo.crossPromoBody
    fun getCrossPromoAds(appPackage: String) =
        viewModelScope.launch { crossPromoCallRepo.getCrossPromoBody(appPackage) }

    /*init {
        getCrossPromoAds(app.packageName)
    }*/
}