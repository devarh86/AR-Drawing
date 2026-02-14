package com.project.sticker.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.project.common.utils.ConstantsCommon
import com.project.sticker.data.model.StickersPackByCategories
import com.project.sticker.datastore.StickerDataStore
import com.project.sticker.ui.intent.StickerIntent
import com.project.sticker.ui.viewstate.StickersUpdateViewState
import com.project.sticker.ui.viewstate.StickersViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var stickerJob: Job? = null

    private val _stickersLiveData = MutableLiveData<StickersViewState>()

    @set:Inject
    lateinit var stickerDataStore: StickerDataStore
    val stickersLiveData: LiveData<StickersViewState>
        get() = _stickersLiveData

    private val _stickersUpdatesLiveData = MutableLiveData<StickersUpdateViewState>()

    var lastTab = 0

    val stickersUpdatesLiveData: LiveData<StickersUpdateViewState>
        get() = _stickersUpdatesLiveData

    val stickerIntent: Channel<StickerIntent> =
        Channel(Channel.UNLIMITED)

    var stickersCategoriesAndData: MutableList<StickersPackByCategories> = mutableListOf()

    init {
        handleIntent()
    }

    private fun handleIntent() {

        viewModelScope.launch(IO) {

            _stickersLiveData.postValue(StickersViewState.Idle)

            stickerIntent.consumeAsFlow().collect {
                when (it) {
                    is StickerIntent.GetStickers -> {
                        getStickersCatAndPacks()
                    }
                }
            }
        }
    }

    fun updateUi(bottomFeaturesContainer: FrameLayout) {
        _stickersLiveData.postValue(StickersViewState.UpdateUi(bottomFeaturesContainer))
    }

    fun cancelJobs() {
        stickerJob?.cancel()
    }

    private fun getStickersCatAndPacks() {

        kotlin.runCatching {
            stickerJob = viewModelScope.launch(IO) {

                if (stickersCategoriesAndData.isEmpty()) {

                    _stickersLiveData.postValue(StickersViewState.Loading)

//                getOfflinePacks(context)

                    withContext(IO) {
                        kotlin.runCatching {
                            ConstantsCommon.stickersList?.let {
                                it.parentCategories?.forEachIndexed { index, parentCategory ->
                                    ensureActive()

                                    parentCategory?.let { parentObj ->
                                        parentObj.stickers?.let {
                                            val mParentObj = StickersPackByCategories(
                                                parentObj.title,
                                                if (stickerDataStore.readIsUnlock(parentObj.id.toInt())
                                                        ?.first() == true
                                                    || parentObj.tag.title == "Free" || isProVersion()
                                                ) "free" else "pro",
                                                parentObj.id.toInt(),
                                                it.filterNotNull()
                                            )
                                            Log.i(
                                                "getStickersCatAndPacks",
                                                "getStickersCatAndPacks: ${parentObj.tag.title}"
                                            )
                                            stickersCategoriesAndData.add(mParentObj)
//                                    _stickersLiveData.postValue(
//                                        StickersViewState.UpdateStickerObject(
//                                            mParentObj
//                                        )
//                                    )
                                        }
                                    }
                                    if (index == (it.parentCategories?.size?.minus(1) ?: -1)) {
                                        _stickersLiveData.postValue(
                                            StickersViewState.Success(
                                                stickersCategoriesAndData
                                            )
                                        )
                                    }
                                }
                            }
                        }.onFailure {
                            _stickersLiveData.postValue(
                                StickersViewState.Error("")
                            )
                        }
                    }
                } else {
                    _stickersLiveData.postValue(
                        StickersViewState.Success(
                            stickersCategoriesAndData
                        )
                    )
                }
            }
        }.onFailure {
            _stickersLiveData.postValue(
                StickersViewState.Error("")
            )
        }
    }

    fun getSuccessState() {
        _stickersLiveData.value = (StickersViewState.Success(
            stickersCategoriesAndData
        ))
    }

    private fun getOfflinePacks(context: Context?) {

        val list: MutableList<GetStickersQuery.Sticker> = mutableListOf()
        context?.let {
            val emojiFolderName = "stickers/emoji"
            val filesList = it.assets.list(emojiFolderName)
            filesList?.forEachIndexed { index, item ->
                val pack = GetStickersQuery.Sticker(
                    id = "1000",
                    "offline",
                    "file:///android_asset/" + emojiFolderName + File.separator + (index + 1) + ".webp",
                    baseUrl = ""
                )
                list.add(pack)
            }

            if (list.isNotEmpty()) {
                val mParentObj = StickersPackByCategories(
                    "Emoji",
                    "free",
                    10000,
                    list
                )
                stickersCategoriesAndData.add(mParentObj)
                _stickersLiveData.postValue(StickersViewState.UpdateStickerObject(mParentObj))
            }
        }
    }

    fun resetStickerViewState() {
        _stickersLiveData.value = StickersViewState.Idle
    }

//    private fun getOfflinePacks(catName: MutableList<String>) {
//
//        repo.getOfflinePacks(catName)?.let {
//            allStickersList.addAll(it.packList)
//            stickersCategoriesAndData.add(it)
//            _stickersLiveData.postValue(
//                StickersViewState.UpdateStickerObject(
//                    obj = it
//                )
//            )
//        }
//    }

    fun updateTick() {
        _stickersUpdatesLiveData.value = StickersUpdateViewState.Tick
    }

    fun updateSticker(obj: GetStickersQuery.Sticker) {
        _stickersUpdatesLiveData.value = StickersUpdateViewState.UpdateStickerObject(obj = obj)
    }

    fun updateCancel() {
        _stickersUpdatesLiveData.value = StickersUpdateViewState.Back
    }

    fun resetViewState() {
        _stickersUpdatesLiveData.value = StickersUpdateViewState.Idle
    }
}