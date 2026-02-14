package com.project.common.viewmodels

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.project.common.R
import com.project.common.repo.api.apollo.NetworkCallRepo
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.offline.OfflineDataRepo
import com.project.common.repo.room.FavouriteRepo
import com.project.common.repo.room.RecentRepo
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.enums.FeatureMainMenuOptions
import com.project.common.utils.enums.MainMenuBlendOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeAndTemplateViewModel @Inject constructor(
    private val networkCallRepo: NetworkCallRepo
) : ViewModel() {

    var scrollX: Int = 0
    var scrollY: Int = 0
    var isExpanded: Boolean = true
    private val _homeScreen: MutableLiveData<ViewStates> = MutableLiveData()
    val homeScreen: LiveData<ViewStates> get() = _homeScreen

    private val _templateScreen: MutableLiveData<ViewStates> = MutableLiveData()
    val templateScreen: LiveData<ViewStates> get() = _templateScreen

    val homeList: MutableList<FramesModelHomeAndTemplates> = mutableListOf()

    private var homeAndTemplateJob: Job? = null

    var currentScreen = "home"

    var alreadyWorking = false

    private val templateList: MutableList<FramesModelHomeAndTemplates> = mutableListOf()

    fun onlyCallToEndPointHomeAndTemplateScreen() {
        CoroutineScope(IO).launch {
            networkCallRepo.getHomeAndTemplateScreen().collect {}
        }
    }

    fun getHomeTemplateScreen() {
//        Log.i("TAG", "getHomeAndTemplateScreen: ${homeList.size}")

        if (_homeScreen.value is ViewStates.Offline && homeList.isNotEmpty() && _templateScreen.value is ViewStates.Offline && templateList.isNotEmpty()) {
            _homeScreen.value = ViewStates.UpdateList(homeList)
            _templateScreen.value = ViewStates.UpdateList(templateList)
            return
        }

        when {

            currentScreen == "home" && homeList.isNotEmpty() -> {
                _homeScreen.value = ViewStates.UpdateList(homeList)
                return
            }

            currentScreen == "template" && templateList.isNotEmpty() -> {
                _templateScreen.value = ViewStates.UpdateList(templateList)
                return
            }
        }

        if (alreadyWorking) {
            return
        }

        _homeScreen.postValue(ViewStates.Loading)

        homeAndTemplateJob = viewModelScope.launch(IO) {
            kotlin.runCatching {

                alreadyWorking = true

                networkCallRepo.getHomeAndTemplateScreen().onCompletion {
//                    Log.i("TAG", "getHomeAndTemplateScreen: complete")
                    alreadyWorking = false
                }.collect { response ->
                    when (response) {
                        is Response.ShowSlowInternet -> {
                           // postSlowInternetToScreens(message ="SlowInternet" )


                        }
                        is Response.Loading -> Unit
                        is Response.Error -> {
                            postErrorToScreens("Please try again")
                        }

                        is Response.Success -> {

                            val screens = response.data?.screens ?: run {
                                postErrorToScreens("Please try again")
                                return@collect
                            }

                            homeList.clear()
                            templateList.clear()

                            val homeFramesList =
                                screens.find { it?.title == FeatureMainMenuOptions.HOME.title }
                            val templateFramesList =
                                screens.find { it?.title == FeatureMainMenuOptions.TEMPLATE.title }

//                            Log.i("TAG", "getHomeAndTemplateScreen: $homeFramesList")
//                            Log.i("TAG", "getHomeAndTemplateScreen: $templateFramesList")

                            val homeListDeferred = async(IO) {
                                processFramesList(homeFramesList, homeList, "home")
                            }

                            val templateListDeferred = async(IO) {
                                processFramesList(templateFramesList, templateList, "template")
                            }

                            homeListDeferred.await()
                            templateListDeferred.await()
                        }
                    }
                }
            }.onFailure {
                Log.i("TAG", "getHomeAndTemplateScreenErrorViewModel: ${it.message}")
                postErrorToScreens("Please try again")
            }
        }
    }

    private suspend fun processFramesList(
        framesList: GetHomeAndTemplateScreenDataQuery.Screen?,
        targetList: MutableList<FramesModelHomeAndTemplates>,
        screenType: String
    ) {
        framesList?.categories?.forEachIndexed { index, categoryObj ->
            categoryObj?.let { category ->
                val framesModel = FramesModelHomeAndTemplates().apply {
                    categoryId = category.id.toLong()
                    categoryName = category.title
                    apiOption = category.title
                    frames.addAll(category.frames.orEmpty())
                }

                if (screenType == "home" && (category.title == "AI Enhancer\uD83D\uDCAB")) { //AI Enhancer💫
                    framesModel.type = ViewHolderTypes.PHOTOEDITOR
                    framesModel.thumbnail = R.raw.enhancer_home
                    framesModel.apiOption = framesModel.categoryName
                }else{
                    Log.i("ENHNACER_ANIM", "processFramesList:  ELSE")
                }

                targetList.add(framesModel)
                postUpdateToScreen(framesModel, screenType)

//                if (screenType == "home" && (index == 1 || index == 3)) {
//                    addSpecialCategories(index, targetList)
//                }
            } ?: postErrorToScreens("Please try again")
        } ?: run {
            postErrorToScreens("Please try again")
        }

        postListUpdateToScreen(targetList, screenType)
    }

    private suspend fun addSpecialCategories(
        index: Int,
        targetList: MutableList<FramesModelHomeAndTemplates>
    ) {
        val specialCategory = FramesModelHomeAndTemplates().apply {
            categoryName = if (index == 1) "Overlay" else "Double Exposure"
            type = ViewHolderTypes.PHOTOEDITOR
            thumbnail = if (index == 1) R.raw.enhancer_home else R.raw.enhancer_home
            apiOption = categoryName
        }

        targetList.add(specialCategory)
        withContext(Main) {
            _homeScreen.postValue(ViewStates.UpdateObject(specialCategory))
        }
    }

    private fun postErrorToScreens(message: String) {
        _homeScreen.postValue(ViewStates.Error(message))
        _templateScreen.postValue(ViewStates.Error(message))
    }

  /*  private fun postSlowInternetToScreens(message: String) {
        _homeScreen.postValue(ViewStates.SlowInternet(message))
        _templateScreen.postValue(ViewStates.SlowInternet(message))
    }
*/
    private suspend fun postUpdateToScreen(
        framesModel: FramesModelHomeAndTemplates,
        screenType: String
    ) {
        withContext(Main) {
            when (screenType) {
                "home" -> _homeScreen.postValue(ViewStates.UpdateObject(framesModel))
                "template" -> _templateScreen.postValue(ViewStates.UpdateObject(framesModel))
            }
        }
    }

    private suspend fun postListUpdateToScreen(
        targetList: MutableList<FramesModelHomeAndTemplates>,
        screenType: String
    ) {
        withContext(Main) {
            when (screenType) {
                "home" -> _homeScreen.postValue(ViewStates.UpdateList(targetList))
                "template" -> _templateScreen.postValue(ViewStates.UpdateList(targetList))
            }
        }
    }

    fun removeFromList() {
        _homeScreen.postValue(
            ViewStates.Offline(
                emptyList<FramesModelHomeAndTemplates>().toMutableList()
            )
        )
        _templateScreen.postValue(
            ViewStates.Offline(
                emptyList<FramesModelHomeAndTemplates>().toMutableList()
            )
        )
    }

    fun resetHomeState() {
        _homeScreen.value = ViewStates.Idle
    }

    fun resetTemplateState() {
        _templateScreen.value = ViewStates.Idle
    }
}

@Keep
sealed class ViewStates() {

    data object Loading : ViewStates()
    class Error(var message: String) : ViewStates()
    class UpdateObject(var objectValue: FramesModelHomeAndTemplates) : ViewStates()
//    class SlowInternet(var message: String) : ViewStates()
    class UpdateList(var list: MutableList<FramesModelHomeAndTemplates>) : ViewStates()
    class Offline(var list: MutableList<FramesModelHomeAndTemplates>) : ViewStates()
    data object Idle : ViewStates()
}

@Keep
data class FramesModelHomeAndTemplates(
    var categoryId: Long = 0L,
    var categoryName: String = "",
    var frames: MutableList<GetHomeAndTemplateScreenDataQuery.Frame?> = mutableListOf(),
    var type: ViewHolderTypes = ViewHolderTypes.FRAMES,
    var thumbnail: Int = -1,
    var apiOption: String = ""
)

@Keep
enum class ViewHolderTypes {
    FRAMES, PHOTOEDITOR
}

//    fun getHomeTemplateScreen(screen: String) {
//
//        if (screen == "home" && homeList.isNotEmpty()) {
//            _homeScreen.value = (ViewStates.UpdateList(
//                this@HomeAndTemplateViewModel.homeList
//            ))
//            return
//        } else if (screen == "template" && templateList.isNotEmpty()) {
//            _templateScreen.value = (ViewStates.UpdateList(
//                this@HomeAndTemplateViewModel.templateList
//            ))
//            return
//        }
//
//        homeAndTemplateJob?.cancel()
//
//        homeAndTemplateJob = viewModelScope.launch(IO) {
//            kotlin.runCatching {
//                networkCallRepo.getHomeAndTemplateScreen().collect {
//                    Log.i("TAG", "getHomeTemplateScreen: $it")
//                    when (it) {
//
//                        is Response.ShowSlowInternet -> {}
//
//                        is Response.Loading -> {}
//
//                        is Response.Error -> {
//                            _homeScreen.postValue(ViewStates.Error("Please try again"))
//                        }
//
//                        is Response.Success -> {
//
//                            templateList.clear()
//                            homeList.clear()
//
//                            val rewardsIdsList: MutableList<Int> = mutableListOf()
//                            rewardsIdsList.addAll(ConstantsCommon.rewardedAssetsList)
//
//                            val homeFramesList =
//                                it.data?.screens?.find { it?.title == FeatureMainMenuOptions.HOME.title }
//                            val templateFramesList =
//                                it.data?.screens?.find { it?.title == FeatureMainMenuOptions.TEMPLATE.title }
//
//                            val child1 = async(IO) {
//                                homeFramesList?.let { homeList ->
//                                    var counter = 0
//                                    homeList.categories?.forEach {
//
//                                        Log.i("TAG", "getHomeTemplateScreen: $isActive")
//
//                                        counter += 1
//                                        it?.let { categoryObj ->
//
//                                            val framesModelHomeAndTemplates =
//                                                FramesModelHomeAndTemplates()
//                                            framesModelHomeAndTemplates.categoryId =
//                                                categoryObj.id.toLong()
//                                            framesModelHomeAndTemplates.categoryName =
//                                                categoryObj.title
//
//                                            categoryObj.frames?.let {
//
//                                                framesModelHomeAndTemplates.frames.addAll(it.toMutableList())
//
//                                                this@HomeAndTemplateViewModel.homeList.add(
//                                                    framesModelHomeAndTemplates
//                                                )
//                                                withContext(Main) {
//                                                    _homeScreen.postValue(
//                                                        ViewStates.UpdateObject(
//                                                            framesModelHomeAndTemplates
//                                                        )
//                                                    )
//                                                }
//                                            } ?: run {
//                                                _homeScreen.postValue(ViewStates.Error("Please try again"))
//                                            }
//
//                                            if (counter == 2) {
//                                                val framesModelHomeAndTemplatesLandscape =
//                                                    FramesModelHomeAndTemplates()
//                                                framesModelHomeAndTemplatesLandscape.categoryName =
//                                                    "Overlay"
//                                                framesModelHomeAndTemplatesLandscape.type =
//                                                    ViewHolderTypes.PHOTOEDITOR
//                                                framesModelHomeAndTemplatesLandscape.thumbnail =
//                                                    R.raw.overlay
//                                                framesModelHomeAndTemplatesLandscape.apiOption =
//                                                    "Overlay"
//                                                this@HomeAndTemplateViewModel.homeList.add(
//                                                    framesModelHomeAndTemplatesLandscape
//                                                )
//                                                withContext(Main) {
//                                                    _homeScreen.postValue(
//                                                        ViewStates.UpdateObject(
//                                                            framesModelHomeAndTemplatesLandscape
//                                                        )
//                                                    )
//                                                }
//                                            } else if (counter == 4) {
//                                                val framesModelHomeAndTemplatesLandscape =
//                                                    FramesModelHomeAndTemplates()
//                                                framesModelHomeAndTemplatesLandscape.categoryName =
//                                                    "Double Exposure"
//                                                framesModelHomeAndTemplatesLandscape.type =
//                                                    ViewHolderTypes.PHOTOEDITOR
//                                                framesModelHomeAndTemplatesLandscape.thumbnail =
//                                                    R.raw.double_exposure
//                                                framesModelHomeAndTemplatesLandscape.apiOption =
//                                                    "Double Exposure"
//                                                this@HomeAndTemplateViewModel.homeList.add(
//                                                    framesModelHomeAndTemplatesLandscape
//                                                )
//                                                withContext(Main) {
//                                                    _homeScreen.postValue(
//                                                        ViewStates.UpdateObject(
//                                                            framesModelHomeAndTemplatesLandscape
//                                                        )
//                                                    )
//                                                }
//                                            }
//
//                                        } ?: run {
//                                            _homeScreen.postValue(ViewStates.Error("Please try again"))
//                                        }
//                                    }
//                                } ?: run {
//                                    _homeScreen.postValue(ViewStates.Error("Please try again"))
//                                }
//                            }
//
//                            val child2 = async(IO) {
//                                templateFramesList?.let { templateList ->
//                                    var counter = 0
//                                    templateList.categories?.forEach {
//
//                                        counter += 1
//                                        it?.let { categoryObj ->
//
//                                            val framesModelHomeAndTemplates =
//                                                FramesModelHomeAndTemplates()
//                                            framesModelHomeAndTemplates.categoryId =
//                                                categoryObj.id.toLong()
//                                            framesModelHomeAndTemplates.categoryName =
//                                                categoryObj.title
//                                            framesModelHomeAndTemplates.apiOption =
//                                                categoryObj.apioption ?: ""
//                                            categoryObj.frames?.let {
//                                                framesModelHomeAndTemplates.frames.addAll(it.toMutableList())
//
//                                                this@HomeAndTemplateViewModel.templateList.add(
//                                                    framesModelHomeAndTemplates
//                                                )
//                                                _templateScreen.postValue(
//                                                    ViewStates.UpdateObject(
//                                                        framesModelHomeAndTemplates
//                                                    )
//                                                )
//                                            } ?: run {
//                                                _templateScreen.postValue(ViewStates.Error("Please try again"))
//                                            }
//                                        } ?: run {
//                                            _templateScreen.postValue(ViewStates.Error("Please try again"))
//                                        }
//                                    }
//                                } ?: run {
//                                    _templateScreen.postValue(ViewStates.Error("Please try again"))
//                                }
//                            }
//
//                            child1.await()
//                            withContext(Main) {
//                                _homeScreen.postValue(
//                                    ViewStates.UpdateList(
//                                        this@HomeAndTemplateViewModel.homeList
//                                    )
//                                )
//                            }
//
//                            child2.await()
//                            withContext(Main) {
//                                _templateScreen.postValue(
//                                    ViewStates.UpdateList(
//                                        this@HomeAndTemplateViewModel.templateList
//                                    )
//                                )
//                            }
//
//                        }
//                    }
//                }
//            }.onFailure {
//                Log.i("TAG", "initLiveData: ${it.message}")
//                _homeScreen.postValue(ViewStates.Error("Please try again"))
//            }
//        }
//    }
