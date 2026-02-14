package com.project.common.repo.offline

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fahad.newtruelovebyfahad.GetFeatureScreenQuery
import com.fahad.newtruelovebyfahad.GetFiltersQuery
import com.fahad.newtruelovebyfahad.GetFrameQuery
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.fahad.newtruelovebyfahad.type.FramesFilesKeyChoices
import com.fahad.newtruelovebyfahad.type.FramesFramesAssettypeChoices
import com.project.common.repo.api.apollo.helper.Response
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton


@Keep
@Singleton
class OfflineDataRepo @Inject constructor(
    @ApplicationContext private val context: Context
) : OfflineService {

    private var isAlreadyFeatureLoading = false

    private val _featureScreen: MutableLiveData<Response<GetFeatureScreenQuery.Data?>> =
        MutableLiveData()
    val featureScreen: LiveData<Response<GetFeatureScreenQuery.Data?>> get() = _featureScreen
    override suspend fun getFeatureScreen() {
        when (_featureScreen.value) {
            is Response.Loading -> {
                Log.d("Fahad", "local Data Observers Loading: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "local Data Observers: Success")
            }

            else -> {

                if (isAlreadyFeatureLoading)
                    return

                isAlreadyFeatureLoading = true

                _featureScreen.postValue(Response.Loading())
                withContext(Dispatchers.IO) {
                    _featureScreen.postValue(Response.Loading())
                    try {
                        val result = GetFeatureScreenQuery.Data(
                            allTags = listOf(
                                GetFeatureScreenQuery.AllTag(
                                    title = "Featured",
                                    tags = listOf(
                                        GetFeatureScreenQuery.Tag(
                                            title = "For You",
                                            tags = listOf(
                                                GetFeatureScreenQuery.Tag1(
                                                    title = "#Bg_Art"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Blending"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Overlay"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Solo"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Dual"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Pip"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Profile_Pic"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Effects"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Drip_Art"
                                                ), GetFeatureScreenQuery.Tag1(
                                                    title = "#Double_Exposure"
                                                )
                                            ),
                                            frames = listOf(
                                                GetFeatureScreenQuery.Frame(
                                                    id = 1,
                                                    title = "Blending",
                                                    thumb = "file:///android_asset/offline_data/blending/cover.webp",
                                                    thumbtype = "Portrait",
                                                    assettype = "blend",
                                                    rembg = true,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Blending",
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 10,
                                                    title = "Effects",
                                                    thumb = "file:///android_asset/offline_data/effects/cover.webp",
                                                    thumbtype = "Square",
                                                    assettype = "effect",
                                                    rembg = false,
                                                    masks = 0,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Effects"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 2,
                                                    title = "BG Art",
                                                    thumb = "file:///android_asset/offline_data/bg_art/cover.webp",
                                                    thumbtype = "Portrait",
                                                    assettype = "blend",
                                                    rembg = true,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Bg_Art"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 3,
                                                    title = "Overlay",
                                                    thumb = "file:///android_asset/offline_data/overlay/cover.webp",
                                                    thumbtype = "Portrait",
                                                    assettype = "blend",
                                                    rembg = true,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Overlay"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 9,
                                                    title = "Drip Art",
                                                    thumb = "file:///android_asset/offline_data/drip_art/cover.webp",
                                                    thumbtype = "Square",
                                                    assettype = "blend",
                                                    rembg = true,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Drip_Art"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 4,
                                                    title = "Dual",
                                                    thumb = "file:///android_asset/offline_data/dual/cover.webp",
                                                    thumbtype = "Portrait",
                                                    assettype = "frame",
                                                    rembg = false,
                                                    masks = 2,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Dual"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 5,
                                                    title = "Solo",
                                                    thumb = "file:///android_asset/offline_data/solo_frame/cover.webp",
                                                    thumbtype = "Portrait",
                                                    assettype = "frame",
                                                    rembg = false,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Solo"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 8,
                                                    title = "Double Exposure",
                                                    thumb = "file:///android_asset/offline_data/double_exposure/cover.webp",
                                                    thumbtype = "Square",
                                                    assettype = "blend",
                                                    rembg = true,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Double_Exposure"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 6,
                                                    title = "PIP",
                                                    thumb = "file:///android_asset/offline_data/pip/cover.webp",
                                                    thumbtype = "Portrait",
                                                    assettype = "pip",
                                                    rembg = false,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Pip"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 7,
                                                    title = "Profile Pic",
                                                    thumb = "file:///android_asset/offline_data/profile_pic/cover.webp",
                                                    thumbtype = "Square",
                                                    assettype = "blend",
                                                    rembg = true,
                                                    masks = 1,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Profile_Pic"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 11,
                                                    title = "Empty",
                                                    thumb = "",
                                                    thumbtype = "Portrait",
                                                    assettype = "",
                                                    rembg = false,
                                                    masks = 0,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Empty"
                                                        )
                                                    )
                                                ), GetFeatureScreenQuery.Frame(
                                                    id = 12,
                                                    title = "Empty",
                                                    thumb = "",
                                                    thumbtype = "Portrait",
                                                    assettype = "",
                                                    rembg = false,
                                                    masks = 0,
                                                    tags = "Free",
                                                    baseUrl = "",
                                                    scrlCount = 1,
                                                    scrl = emptyList(),
                                                    hashtag = listOf(
                                                        GetFeatureScreenQuery.Hashtag(
                                                            title = "#Empty"
                                                        )
                                                    )
                                                )
                                            )
                                        ),
                                        GetFeatureScreenQuery.Tag(
                                            title = "Most Used",
                                            tags = listOf(),
                                            frames = listOf()
                                        ),
                                        GetFeatureScreenQuery.Tag(
                                            title = "Today's Special",
                                            tags = listOf(),
                                            frames = listOf()
                                        )
                                    )
                                )
                            )
                        )
                        _featureScreen.postValue(Response.Success(result))
                        isAlreadyFeatureLoading = false
                    } catch (ex: Exception) {
                        _featureScreen.postValue(Response.Error(ex.message.toString()))
                        isAlreadyFeatureLoading = false
                    }
                }
            }
        }
    }

    private val _frame: MutableLiveData<Response<GetFrameQuery.Data?>> = MutableLiveData()
    val frame: LiveData<Response<GetFrameQuery.Data?>> get() = _frame

    override suspend fun getFrame(id: Int) {
        when (_frame.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {
                _frame.postValue(Response.Loading())
                withContext(Dispatchers.IO) {
                    try {
                        _frame.postValue(Response.Loading())
                        val result = when (id) {
                            2 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "2",
                                    title = "BG Art",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.BLEND,
                                    rembg = true,
                                    category = GetFrameQuery.Category(title = "BG Art"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/bg_art/cover.webp",
                                            width = "285.00",
                                            height = "475.00",
                                            xaxis = "139.00",
                                            yaxis = "230.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/bg_art/bg.webp",
                                            width = "562.00",
                                            height = "935.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.CLIP,
                                            file = "file:///android_asset/offline_data/bg_art/blend.webp",
                                            width = "562.00",
                                            height = "888.00",
                                            xaxis = "0.00",
                                            yaxis = "47.00",
                                            rotation = "0.00",
                                            clipFile = "masks",
                                            effect = "BlendMode.OVERLAY",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/bg_art/cover.webp",
                                            width = "264.00",
                                            height = "652.00",
                                            xaxis = "149.00",
                                            yaxis = "199.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.OVERLAY,
                                            file = "file:///android_asset/offline_data/bg_art/overlay.webp",
                                            width = "562.00",
                                            height = "935.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.SOFT_LIGHT",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            3 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "3",
                                    title = "Overlay",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.BLEND,
                                    rembg = true,
                                    category = GetFrameQuery.Category(title = "Overlay"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.OVERLAY,
                                            file = "file:///android_asset/offline_data/overlay/overlay.webp",
                                            width = "561.00",
                                            height = "935.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.SCREEN",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/overlay/cover.webp",
                                            width = "284.00",
                                            height = "474.00",
                                            xaxis = "138.00",
                                            yaxis = "230.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/overlay/bg.webp",
                                            width = "561.00",
                                            height = "935.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/overlay/cover.webp",
                                            width = "514.00",
                                            height = "873.00",
                                            xaxis = "21.00",
                                            yaxis = "62.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            4 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "4",
                                    title = "Dual",
                                    editor = "Dual",
                                    assettype = FramesFramesAssettypeChoices.FRAME,
                                    rembg = false,
                                    category = GetFrameQuery.Category(title = "Dual"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/dual/cover.webp",
                                            width = "280.00",
                                            height = "467.00",
                                            xaxis = "140.00",
                                            yaxis = "233.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/dual/cover.webp",
                                            width = "442.00",
                                            height = "443.00",
                                            xaxis = "75.00",
                                            yaxis = "84.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/dual/cover.webp",
                                            width = "376.00",
                                            height = "377.00",
                                            xaxis = "75.00",
                                            yaxis = "527.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.FRAME,
                                            file = "file:///android_asset/offline_data/dual/frame.webp",
                                            width = "560.00",
                                            height = "933.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 2,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            5 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "5257",
                                    title = "Solo",
                                    editor = "Solo",
                                    assettype = FramesFramesAssettypeChoices.FRAME,
                                    rembg = false,
                                    category = GetFrameQuery.Category(title = "Solo"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/solo_frame/cover.webp",
                                            width = "248.00",
                                            height = "415.00",
                                            xaxis = "156.00",
                                            yaxis = "259.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.FRAME,
                                            file = "file:///android_asset/offline_data/solo_frame/frame.webp",
                                            width = "560.00",
                                            height = "933.00",
                                            xaxis = "0.00",
                                            yaxis = "2.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/solo_frame/cover.webp",
                                            width = "446.00",
                                            height = "570.00",
                                            xaxis = "63.00",
                                            yaxis = "66.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            6 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "6",
                                    title = "PIP",
                                    editor = "Pip",
                                    assettype = FramesFramesAssettypeChoices.PIP,
                                    rembg = false,
                                    category = (GetFrameQuery.Category(title = "PIP")),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/pip/cover.webp",
                                            width = "285.00",
                                            height = "475.00",
                                            xaxis = "138.00",
                                            yaxis = "230.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/pip/mask.webp",
                                            width = "408.00",
                                            height = "363.00",
                                            xaxis = "52.00",
                                            yaxis = "341.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.FRAME,
                                            file = "file:///android_asset/offline_data/pip/frame.webp",
                                            width = "561.00",
                                            height = "934.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "0",
                                            effect = "0",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            7 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "7",
                                    title = "Profile Pic",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.BLEND,
                                    rembg = true,
                                    category = GetFrameQuery.Category(
                                        title = "Profile Pic"
                                    ),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/profile_pic/cover.webp",
                                            width = "424.00",
                                            height = "424.00",
                                            xaxis = "328.00",
                                            yaxis = "328.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/profile_pic/bg.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.CLIP,
                                            file = "file:///android_asset/offline_data/profile_pic/blend.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "masks",
                                            effect = "BlendMode.SOFT_LIGHT",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/profile_pic/cover.webp",
                                            width = "591.00",
                                            height = "740.00",
                                            xaxis = "250.00",
                                            yaxis = "113.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.FG,
                                            file = "file:///android_asset/offline_data/profile_pic/fg.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            8 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "8",
                                    title = "Double Exposure",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.BLEND,
                                    rembg = true,
                                    category = GetFrameQuery.Category(title = "Double Exposure"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/double_exposure/cover.webp",
                                            width = "548.00",
                                            height = "548.00",
                                            xaxis = "266.00",
                                            yaxis = "266.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/double_exposure/bg.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.CLIP,
                                            file = "file:///android_asset/offline_data/double_exposure/blend.webp",
                                            width = "781.00",
                                            height = "1080.00",
                                            xaxis = "147.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "masks",
                                            effect = "BlendMode.LIGHTEN",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/double_exposure/cover.webp",
                                            width = "972.00",
                                            height = "1047.00",
                                            xaxis = "54.00",
                                            yaxis = "34.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = true
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.OVERLAY,
                                            file = "file:///android_asset/offline_data/double_exposure/overlay.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.LIGHTEN",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            9 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "9",
                                    title = "Drip Art",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.BLEND,
                                    rembg = true,
                                    category = GetFrameQuery.Category(title = "Drip Art"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/drip_art/cover.webp",
                                            width = "381.00",
                                            height = "381.00",
                                            xaxis = "350.00",
                                            yaxis = "350.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/drip_art/bg.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.CLIP,
                                            file = "file:///android_asset/offline_data/drip_art/blend.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "masks",
                                            effect = "BlendMode.OVERLAY",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/drip_art/cover.webp",
                                            width = "630.00",
                                            height = "879.00",
                                            xaxis = "213.00",
                                            yaxis = "94.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = true
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.FG,
                                            file = "file:///android_asset/offline_data/drip_art/fg.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            10 -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "10",
                                    title = "Effects",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.EFFECT,
                                    rembg = false,
                                    category = GetFrameQuery.Category(title = "Blend"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/effects/cover.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/effects/bg.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.USER,
                                            file = "file:///android_asset/offline_data/effects/cover.webp",
                                            width = "904.00",
                                            height = "1058.00",
                                            xaxis = "84.00",
                                            yaxis = "14.00",
                                            rotation = "0.00",
                                            clipFile = "maskbg",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BLEND,
                                            file = "file:///android_asset/offline_data/effects/blend.webp",
                                            width = "1080.00",
                                            height = "1080.00",
                                            xaxis = "0.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "maskbg",
                                            effect = "BlendMode.SOFT_LIGHT",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKBG,
                                            file = "file:///android_asset/offline_data/effects/maskbg.webp",
                                            width = "880.00",
                                            height = "1080.00",
                                            xaxis = "100.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 0,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )

                            else -> GetFrameQuery.Data(
                                frame = GetFrameQuery.Frame(
                                    id = "1",
                                    title = "Blending",
                                    editor = "Blend",
                                    assettype = FramesFramesAssettypeChoices.BLEND,
                                    rembg = true,
                                    category = GetFrameQuery.Category(title = "Blending"),
                                    files = listOf(
                                        GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.COVER,
                                            file = "file:///android_asset/offline_data/blending/cover.webp",
                                            width = "286.00",
                                            height = "475.00",
                                            xaxis = "137.00",
                                            yaxis = "229.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.BG,
                                            file = "file:///android_asset/offline_data/blending/bg.webp",
                                            width = "562.00",
                                            height = "935.00",
                                            xaxis = "-1.00",
                                            yaxis = "-1.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.MASKS,
                                            file = "file:///android_asset/offline_data/blending/cover.webp",
                                            width = "541.00",
                                            height = "654.00",
                                            xaxis = "8.00",
                                            yaxis = "9.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = true
                                        ), GetFrameQuery.File(
                                            key = FramesFilesKeyChoices.FG,
                                            file = "file:///android_asset/offline_data/blending/fg.webp",
                                            width = "563.00",
                                            height = "935.00",
                                            xaxis = "-1.00",
                                            yaxis = "0.00",
                                            rotation = "0.00",
                                            clipFile = "",
                                            effect = "BlendMode.NORMAL",
                                            baseUrl = "",
                                            adjustment = false
                                        )
                                    ),
                                    masks = 1,
                                    baseUrl = "",
                                    tags = "Free",
                                    isComplex = false,
                                    scrlCount = 0
                                )
                            )
                        }
                        _frame.postValue(Response.Success(result))
                    } catch (ex: Exception) {
                        _frame.postValue(Response.Error(ex.message.toString()))
                    }
                }
            }
        }
    }

    private val _stickers: MutableLiveData<Response<GetStickersQuery.Data?>> = MutableLiveData()
    val stickers: LiveData<Response<GetStickersQuery.Data?>> get() = _stickers

    private val isLoading = AtomicBoolean(false)

    override suspend fun getStickers() {
        when (_stickers.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {

                if (isLoading.get()) {
                    return
                }

                isLoading.set(true)

                withContext(Dispatchers.IO) {
                    _stickers.postValue(Response.Loading())
                    try {
                        val result = GetStickersQuery.Data(
                            parentCategories = listOf(
                                GetStickersQuery.ParentCategory(
                                    id = "313",
                                    title = "Emoji",
                                    tag = GetStickersQuery.Tag(title = "Free"),
                                    stickers = listOf(
                                        GetStickersQuery.Sticker(
                                            id = "1",
                                            title = "Emoji 1",
                                            file = "file:///android_asset/stickers/emoji/1.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "2",
                                            title = "Emoji 2",
                                            file = "file:///android_asset/stickers/emoji/2.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "3",
                                            title = "Emoji 3",
                                            file = "file:///android_asset/stickers/emoji/3.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "4",
                                            title = "Emoji 4",
                                            file = "file:///android_asset/stickers/emoji/4.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "5",
                                            title = "Emoji 5",
                                            file = "file:///android_asset/stickers/emoji/5.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "6",
                                            title = "Emoji 6",
                                            file = "file:///android_asset/stickers/emoji/6.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "7",
                                            title = "Emoji 7",
                                            file = "file:///android_asset/stickers/emoji/7.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "8",
                                            title = "Emoji 8",
                                            file = "file:///android_asset/stickers/emoji/8.webp",
                                            baseUrl = ""
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "9",
                                            title = "Emoji 9",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/9.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "10",
                                            title = "Emoji 10",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/10.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "11",
                                            title = "Emoji 11",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/11.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "12",
                                            title = "Emoji 12",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/12.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "13",
                                            title = "Emoji 13",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/13.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "13",
                                            title = "Emoji 13",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/13.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "13",
                                            title = "Emoji 13",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/13.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "13",
                                            title = "Emoji 13",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/13.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "14",
                                            title = "Emoji 14",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/14.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "15",
                                            title = "Emoji 15",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/15.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "16",
                                            title = "Emoji 16",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/16.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "17",
                                            title = "Emoji 17",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/17.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "18",
                                            title = "Emoji 18",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/18.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "19",
                                            title = "Emoji 19",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/19.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "20",
                                            title = "Emoji 20",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/20.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "21",
                                            title = "Emoji 21",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/21.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "22",
                                            title = "Emoji 22",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/22.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "23",
                                            title = "Emoji 23",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/23.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "24",
                                            title = "Emoji 24",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/24.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "25",
                                            title = "Emoji 25",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/25.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "26",
                                            title = "Emoji 26",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/26.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "27",
                                            title = "Emoji 27",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/27.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "28",
                                            title = "Emoji 28",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/28.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "29",
                                            title = "Emoji 29",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/29.webp"
                                        ),
                                        GetStickersQuery.Sticker(
                                            id = "30",
                                            title = "Emoji 30",
                                            baseUrl = "",
                                            file = "file:///android_asset/stickers/emoji/30.webp"
                                        )
                                    )
                                )
                            )
                        )
                        _stickers.postValue(Response.Success(result))
                        isLoading.set(false)

                    } catch (ex: Exception) {
                        _stickers.postValue(Response.Error(ex.message.toString()))
                        isLoading.set(false)
                    }
                }
            }
        }
    }

    private val _filters: MutableLiveData<Response<GetFiltersQuery.Data?>> =
        MutableLiveData()
    val filters: LiveData<Response<GetFiltersQuery.Data?>> get() = _filters

    private val isLoadingFilter = AtomicBoolean(false)

    override suspend fun getFilters() {
        when (_filters.value) {
            is Response.Loading -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            is Response.Success -> {
                Log.d("Fahad", "initApiObservers: ")
            }

            else -> {

                if (isLoadingFilter.get()) {
                    return
                }

                isLoadingFilter.set(true)

                withContext(Dispatchers.IO) {
                    _filters.postValue(Response.Loading())
                    try {
                        val result = GetFiltersQuery.Data(
                            parentCategories = listOf(
                                GetFiltersQuery.ParentCategory(
                                    id = "338",
                                    title = "Euro",
                                    tag = GetFiltersQuery.Tag(title = "Free"),
                                    effects = listOf(
                                        GetFiltersQuery.Effect(
                                            id = "148",
                                            title = "Euro 1",
                                            effect = "1.0124,0.1539,0.2501,0.1022,-21.5900,0.1569,1.2597,-0.0002,0.0594,-21.5900,-0.0353,0.4494,1.0023,0.0381,-21.5900,0.0,0.0,0.0,1.0,0.0",
                                            cover = "",
                                            baseUrl = "",
                                            tags = listOf(GetFiltersQuery.Tag1(title = "Free"))
                                        ),
                                        GetFiltersQuery.Effect(
                                            id = "150",
                                            title = "Euro 3",
                                            effect = "1.2427,0.3042,-0.0341,-0.0410,-7.1144,0.0418,1.4344,0.0365,-0.0180,-7.1144,0.0960,0.2208,1.1960,-0.0064,-7.1144,0.0,0.0,0.0,1.0,0.0",
                                            cover = "",
                                            baseUrl = "",
                                            tags = listOf(GetFiltersQuery.Tag1(title = "Free"))
                                        ),
                                        GetFiltersQuery.Effect(
                                            id = "149",
                                            title = "Euro 2",
                                            effect = "0.8430,0.1547,0.3673,0.0514,-5.4748,0.1612,1.2372,-0.0847,0.0239,-5.4748,-0.0843,0.6884,0.8112,0.0102,-5.4748,0.0,0.0,0.0,1.0,0.0",
                                            cover = "",
                                            baseUrl = "",
                                            tags = listOf(GetFiltersQuery.Tag1(title = "Free"))
                                        ),
                                        GetFiltersQuery.Effect(
                                            id = "151",
                                            title = "Euro 4",
                                            effect = "1.1926,0.1693,0.0783,0.0326,-38.0561,0.0990,1.2401,0.1011,0.0177,-38.0561,0.1165,0.1425,1.1812,0.0103,-38.0561,0.0,0.0,0.0,1.0,0.0",
                                            cover = "",
                                            baseUrl = "",
                                            tags = listOf(GetFiltersQuery.Tag1(title = "Free"))
                                        )
                                    )
                                )
                            )
                        )
                        _filters.postValue(Response.Success(result))
                        isLoadingFilter.set(false)
                    } catch (ex: Exception) {
                        _filters.postValue(Response.Error(ex.message.toString()))
                        isLoadingFilter.set(false)
                    }
                }
            }
        }
    }

    fun clearFrame() = _frame.postValue(Response.Error(""))
}