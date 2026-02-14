package com.project.common.utils

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.fahad.newtruelovebyfahad.GetEffectsQuery
import com.fahad.newtruelovebyfahad.GetFeatureScreenQuery
import com.fahad.newtruelovebyfahad.GetFiltersQuery
import com.fahad.newtruelovebyfahad.GetFrameQuery
import com.fahad.newtruelovebyfahad.GetMainScreenQuery
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.project.common.model.ImagesModel

@Keep
object ConstantsCommon {
    var carouselImagesCount = 0
    var receivedData: String? = null

    var introOnBoardingCompleted = false
    var showQuestionScreenTimeCheck = false

    var fromSaved = false

    var showInterstitialAd = true

    var introScreenCounter = 0

    var surveyCompleted = false

    var fromSaveAndShare = false  // this variable is use to check selection on gallery next when user is coming from save and share

    const val BASE_URL_Enhancer = "https://enhancer.xen-studios.com/"
    const val BASE_URL_BG_REMOVER = "https://bgr.xen-studios.com/"

    const val BASE_URL_SKETCH = "https://sketchpix.xen-studios.com/"
    var TOKEN = ""
    var TOKEN_ENHANCE = ""
    var isNetworkAvailable: Boolean = true
    var updateInternetStatusFeature: MutableLiveData<Boolean> = MutableLiveData(true)
    var updateInternetStatusFrames: MutableLiveData<Boolean> = MutableLiveData(true)
    var filterList: GetFiltersQuery.Data? = null
    var effectList: GetEffectsQuery.Data? = null
    var saveSession = 0
    var stickersList: GetStickersQuery.Data? = null
    var backgroundsList: GetStickersQuery.Data? = null
    var isOpenCVSuccess = false
    var lottieRenderModeAutomatic = false
    var enableClicks: Boolean = false
    var isDraft: Boolean = false
    var parentId: Long = -1
    var ratio: String = "1:1"
    var type: String = ""
    var editor: String = ""
    var enhanceImageUrl: String = ""
    var originalWidthEnhanceImage = 0
    var originalHeightEnhanceImage = 0
    var filePathForDraft: String = ""
    var selectedId: Long = -1
    var favouriteFrames: List<GetFeatureScreenQuery.Frame?> = emptyList()
    var saveAndShareScreenTrendingData: Pair<List<String>, List<GetFeatureScreenQuery.Frame?>>? =
        null

    var multiFitImageEnhancedPath: MutableList<ImagesModel> = mutableListOf()

    var featureTodaySpecialData: Pair<List<String>, List<GetFeatureScreenQuery.Frame?>>? = null
    var featureForYouData: Pair<List<String>, List<GetFeatureScreenQuery.Frame?>>? = null
    var featureMostUsedData: Pair<List<String>, List<GetFeatureScreenQuery.Frame?>>? = null

    var soloFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var drawingFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var multiplexFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? =
        linkedMapOf()
    var pipFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var collageFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var greetingFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var shapeFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var templatesFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()
    var blendFramesSubData: LinkedHashMap<String, List<GetMainScreenQuery.Frame?>>? = linkedMapOf()

    var stickersSubData: LinkedHashMap<String, List<GetStickersQuery.Sticker?>>? = linkedMapOf()

    var notifyAdapterForRewardedAssets = false

    val rewardedAssetsList: MutableList<Int> = mutableListOf()

    var currentFrameFeature: GetFrameQuery.Frame? = null
    var currentFrameMain: GetFrameQuery.Frame? = null

    fun resetCurrentFrames() {
        currentFrameMain = null
        currentFrameFeature = null
        isDraft = false
    }

    var isSavedScreenHomeClicked = false
    var isGoProBottomRvClicked = false

    var enableGeneralNotification = true
}