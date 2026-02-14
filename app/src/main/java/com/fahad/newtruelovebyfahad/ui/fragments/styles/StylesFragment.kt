package com.fahad.newtruelovebyfahad.ui.fragments.styles

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.Constants.showStylesFrameClickAd
import com.example.ads.admobs.utils.onResumeBanner
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.databinding.FragmentStylesBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FramesRV
import com.fahad.newtruelovebyfahad.ui.fragments.styles.adapter.CategoriesListRVAdapter
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getEventCategoryName
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class StylesFragment : Fragment() {
    private var _binding: FragmentStylesBinding? = null
    private val binding get() = _binding!!

    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    var categoriesFramesSubData: LinkedHashMap<String, List<FramesRV.FrameModel>>? = linkedMapOf()

    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var framesAdapter: FramesRV? = null
    private var categoryListAdapter: CategoriesListRVAdapter? = null
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private var downloadDialog: BottomSheetDialog? = null

    var currentTag: String = "All"
    var currentPosition = 0
    var tagsList: List<String>? = null
    var scrollDirection = 0

    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            if (backDecision) {
//                navController?.navigateUp()
                (mActivity as? MainActivity)?.showHomeScreen()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        parentScreen = Events.Screens.STYLES

        eventForGalleryAndEditor(Events.Screens.STYLES, "", true)

        categoryListAdapter =
            CategoriesListRVAdapter(emptyList()) { tag, position ->

                _binding?.framesRv?.scrollToPosition(0)
                _binding?.categoryListRv?.scrollToPosition(position)
                currentTag = tag
                currentPosition = position
                framesAdapter?.categoryName = tag
                categoriesFramesSubData?.get(tag)?.let {
                    if (it.isNotEmpty()) {
                        //_binding?.framesRv?.visible()
                        categoryListAdapter?.select()
                        if (_binding?.framesRv?.isComputingLayout != true) {
                            framesAdapter?.clearData()
                        }
                        /*it.forEach { framesAdapter?.updateDataList(it) }*/
                        framesAdapter?.updateDataList(it, scrollDirection)
                        scrollDirection = 0

                    } else {
                        categoryListAdapter?.unselect()
                        //_binding?.framesRv?.gone()
                    }
                } ?: run {
                    categoryListAdapter?.unselect()
                }
            }

        framesAdapter = FramesRV(
            mContext,
            arrayListOf(),
            null,
            onClick = { frameBody, position ->

                mActivity?.let { it ->
                    if (it is MainActivity) {
                        val categoryName = (framesAdapter?.categoryName ?: "").getEventCategoryName()
                        it.frameClick(
                            FrameObject(
                                frameBody.id,
                                frameBody.title,
                                Events.Screens.STYLES,
                                "",
                                categoryName,
                                frameBody.tags ?: "",
                                frameBody.baseUrl ?: "",
                                frameBody.thumb,
                                frameBody.thumbtype,
                                showStylesFrameClickAd,
                                true,
                                frameBody,
                                "list"
                            )
                        ) {
                            if (position > -1) framesAdapter?.notifyItemChanged(
                                position
                            )
                        }
                    }
                }
            }, onFavouriteClick = {
                apiViewModel.favourite(
                    FavouriteModel(
                        isFavourite = it.isFavourite, frame = it.frame
                    )
                )
            },
            onPurchaseTypeTagClick = {}
        )
    }

    fun hideScreenAds() {
        visibleAd = true
        _binding?.bannerContainer?.invisible()
        if (isProVersion())
            framesAdapter?.hideRvAd()
    }

    fun showScreenAds() {
        visibleAd = false
        _binding?.bannerContainer?.visible()
    }

    private var visibleAd = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStylesBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    private fun FragmentStylesBinding.initViews() {
        categoriesFramesSubData?.clear()
        initObservers()
        initRecyclerViews()
    }

    private fun FragmentStylesBinding.initObservers() {
        try {
            var isCompleted = true
            ConstantsCommon.updateInternetStatusFrames.observe(
                viewLifecycleOwner
            ) {
                checkInternet()
                if (it == true) {
                    try {
                        apiViewModel.mainScreen.observe(viewLifecycleOwner) {
                            when (it) {
                                is Response.Loading -> {
                                    tryNowPlaceholder.gone()
                                    noResultFoundTv.gone()
                                    loadingView.startShimmer()
                                    loadingView.visible()
                                    framesRv.invisible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {
                                    categoriesFramesSubData?.let {
                                        if (it.isNotEmpty()) {
                                            loadingView.gone()
                                            loadingView.stopShimmer()
                                            if (isNetworkAvailable)
                                                framesRv.visible()
                                            return@observe
                                        }
                                    }
                                    if (isCompleted) {
                                        isCompleted = false
                                        it.data?.childCategories?.let { mainMenuOptions ->
                                            mainMenuOptions.filterNotNull().forEach {
                                                when (it.title.lowercase()) {
                                                    MainMenuOptions.BLEND.title.lowercase() -> {
                                                        it.children?.forEach {
                                                            it?.apply {
                                                                frames?.let {
                                                                    ConstantsCommon.blendFramesSubData?.set(
                                                                        title, it
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        ConstantsCommon.blendFramesSubData?.forEach { (key, value) ->
                                            if (_binding?.framesRv?.isComputingLayout != true) {
                                                framesAdapter?.clearData()
                                            }
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                val frames = value.filterNotNull().map { frame ->
                                                    FramesRV.FrameModel(frame)
                                                }
                                                frames.forEach { frame ->
                                                    Log.d(
                                                        "FAHAD",
                                                        "initObservers: SUCCESS --- ${frame.frame}"
                                                    )
                                                    frame.isFavourite =
                                                        withContext(Dispatchers.Default) {
                                                            ConstantsCommon.favouriteFrames.mapNotNull { it?.id }
                                                                .contains(
                                                                    FavouriteTypeConverter.fromJson(
                                                                        FavouriteTypeConverter.toJson(
                                                                            frame.frame
                                                                        )
                                                                    )?.id
                                                                )
                                                        }
                                                    if (key.lowercase() == "all") {
                                                        withContext(Dispatchers.Main) {
                                                            loadingView.gone()
                                                            loadingView.stopShimmer()
                                                            if (isNetworkAvailable)
                                                                framesRv.visible()
                                                            framesAdapter?.updateSingleItem(
                                                                frame
                                                            )
                                                        }
                                                    }
                                                }
                                                categoriesFramesSubData?.put(
                                                    key, frames
                                                )
                                            }.invokeOnCompletion {
                                                isCompleted = true
                                            }
                                        }
                                        tagsList =
                                            ConstantsCommon.blendFramesSubData?.keys?.toList()
                                        categoryListAdapter?.updateDataList(tagsList)
                                    }
                                }

                                is Response.Error -> {
                                    Log.d("FAHAD", "initObservers: ERROR")
                                    if (!mActivity.isNetworkAvailable()) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        framesRv.invisible()
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun FragmentStylesBinding.initRecyclerViews() {
        categoryListRv.adapter = categoryListAdapter
        framesRv.adapter = framesAdapter
        /*framesRv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            var lastX = 0
            var processComplete = true
            override fun onInterceptTouchEvent(
                rv: RecyclerView,
                e: MotionEvent
            ): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        processComplete = true
                        lastX = e.x.toInt()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (processComplete) {
                            val isScrollingRight = e.x < lastX
                            val delta = abs(e.x.toInt() - lastX) > 220
                            processComplete = !delta
                            framesRv.adapter?.let {
                                if (delta) {
                                    tagsList?.apply {
                                        if (isNotEmpty()) {
                                            tagsList?.get(currentPosition)?.let { key ->
                                                categoriesFramesSubData?.get(key)?.let {
                                                    if (it.isNotEmpty()) {
                                                        _binding?.framesRv?.visible()
                                                        if (isScrollingRight) {
                                                            scrollDirection = 1
                                                            categoryListAdapter?.selectNext(
                                                                currentPosition
                                                            )
                                                        } else {
                                                            scrollDirection = -1
                                                            categoryListAdapter?.selectPrevious(
                                                                currentPosition
                                                            )
                                                        }
                                                    } else {
                                                        categoryListAdapter?.unselect()
                                                    }
                                                } ?: run {
                                                    categoryListAdapter?.unselect()
                                                }
                                            } ?: run {
                                                categoryListAdapter?.unselect()
                                            }
                                        } else {
                                            categoryListAdapter?.unselect()
                                        }
                                    } ?: run {
                                        categoryListAdapter?.unselect()
                                    }
                                }
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        lastX = 0
                        processComplete = true
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })*/
    }

    override fun onResume() {
        super.onResume()
        runCatching {
            if (!isProVersion()) {

                if (!visibleAd)
                    _binding?.bannerContainer?.show()

                _binding?.let {
                    mActivity?.onResumeBanner(
                        binding.adBannerContainer,
                        binding.crossBannerIv,
                        binding.bannerLayout.adContainer,
                        binding.bannerLayout.shimmerViewContainer
                    )
                }
            } else {
                _binding?.bannerContainer?.hide()
            }
        }
    }

    private fun checkInternet() {
        _binding?.apply {
            if (!mActivity.isNetworkAvailable()) {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
                loadingView.stopShimmer()
                loadingView.gone()
                categoryListRv.invisible()
                framesRv.invisible()
            } else {
                tryNowPlaceholder.gone()
                noResultFoundTv.gone()
                loadingView.gone()
                loadingView.stopShimmer()
                categoryListRv.visible()
                framesRv.visible()
            }
        }
    }
}