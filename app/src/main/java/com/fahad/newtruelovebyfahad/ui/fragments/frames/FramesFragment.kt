package com.fahad.newtruelovebyfahad.ui.fragments.frames

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.ads.Constants.showCategoriesFrameClickAd

import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentFramesBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.common.CategoriesRVAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.home.HomeFragmentDirections
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FramesRV
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FramesRVLikeHome
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.ConstantsCommon.soloFramesSubData
import com.project.common.utils.ConstantsCommon.templatesFramesSubData
import com.project.common.utils.enums.MainMenuBlendOptions
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getProScreen
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class FramesFragment : Fragment() {

    private var _binding: FragmentFramesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private val args by navArgs<FramesFragmentArgs>()
    private var categoryTagsAdapter: CategoriesRVAdapter? = null
    private var downloadDialog: BottomSheetDialog? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var framesAdapter: FramesRVLikeHome? = null
//    private var framesAdapter: FramesRV? = null
    private var categoriesFramesSubData: LinkedHashMap<String, List<FramesRV.FrameModel>>? =
        linkedMapOf()
    private var nativeAd: NativeAd? = null

    private var showRewardAdForThisCategory = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    private var event = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

        event = when (args.mainScreenMenu) {
            MainMenuBlendOptions.SEAMLESS.title -> {
                Events.ParamsValues.HomeScreen.SEAMLESS
            }

            MainMenuBlendOptions.TAPE.title -> {
                Events.ParamsValues.HomeScreen.TAPE
            }

            MainMenuBlendOptions.PAPER.title -> {
                Events.ParamsValues.HomeScreen.PAPER
            }

            MainMenuBlendOptions.REWIND.title -> {
                Events.ParamsValues.HomeScreen.REWIND
            }

            MainMenuBlendOptions.FILM.title -> {
                Events.ParamsValues.HomeScreen.FILM
            }

            MainMenuBlendOptions.PLASTIC.title -> {
                Events.ParamsValues.HomeScreen.PLASTIC
            }

            MainMenuBlendOptions.COLLAGE.title -> {
                Events.ParamsValues.HomeScreen.COLLAGE
            }

            MainMenuBlendOptions.STORIES.title -> {
                Events.ParamsValues.HomeScreen.STORIES
            } else -> {
                Events.ParamsValues.HomeScreen.Templates
            }



        }

        categoryTagsAdapter = CategoriesRVAdapter(emptyList()) { tag, position ->
//            if (::mActivity.isInitialized && mActivity is MainActivity) {
//                (mActivity as MainActivity).eventForCategoryClick(
//                    FrameObject(
//                        screenName = "categories",
//                        categoryName = event,
//                        subCategoryName = tag.lowercase(),
//                        from = "see_all",
//                        frameBody = ""
//                    )
//                )
//            }
            _binding?.framesRv?.scrollToPosition(0)
            _binding?.categoryTagsRv?.scrollToPosition(position)
            categoriesFramesSubData?.get(tag)?.let {
                if (it.isNotEmpty()) {
                    //_binding?.framesRv?.visible()
                    categoryTagsAdapter?.select()
                    if (_binding?.framesRv?.isComputingLayout != true) {
                        framesAdapter?.clearData()
                    }
                    /*it.forEach { framesAdapter?.updateDataList(it) }*/
                    framesAdapter?.updateDataList(it)
                    framesAdapter?.categoryName = tag.lowercase()
                } else {
                    categoryTagsAdapter?.unselect()
                    //_binding?.framesRv?.gone()
                }
            } ?: run {
                categoryTagsAdapter?.unselect()
            }
        }

        framesAdapter = FramesRVLikeHome(
            mContext,
            arrayListOf(),
            nativeAd,
            onClick = { frameBody, position ->
                if (::mActivity.isInitialized && mActivity is MainActivity) {
                    (mActivity as MainActivity).frameClickFrames(
                        FrameObject(
                            frameBody.id,
                            frameBody.title,
                            "categories",
                            framesAdapter?.categoryName ?: "",
                            event,
                            frameBody.tags ?: "",
                            frameBody.baseUrl ?: "",
                            frameBody.thumb,
                            frameBody.thumbtype,
                            showCategoriesFrameClickAd,
                            showRewardAdForThisCategory,
                            frameBody,
                            "list"
                        )
                    ) {
                        if (position != -1) framesAdapter?.notifyItemChanged(
                            position
                        )
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

        /*mActivity.loadNative(
            loadedAction = {
                nativeAd = it
                framesAdapter?.updateAd(it)
            },
            failedAction = {
                nativeAd = null
            },
        )*/
    }



    private var visibleAd = false

    fun hideScreenAds() {
        visibleAd = true
        if (isProVersion()) {
            framesAdapter?.hideRvAd()
//            _binding?.proBtn?.pauseAnimation()
            _binding?.proBtn?.gone()
//            if (!seeAllAdaptiveBanner) {
//                removeCollapsableBanner()
//            }
        }
    }

    fun showScreenAds() {
        visibleAd = false
        if(!isProVersion()){
           // _binding?.proBtn?.visible()
        }

    }


/*
    fun hideScreenAds() {
        if (isProVersion()){
            framesAdapter?.hideRvAd()
            _binding?.proBtn?.hide()
        }


    }

    fun showScreenAds() {
        if(!isProVersion()){
            _binding?.proBtn?.visible()
        }

    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFramesBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    private fun FragmentFramesBinding.initViews() {
        categoriesFramesSubData?.clear()
        titleNameTv.text = "Choose Template"
//            when (args.mainScreenMenu.lowercase()) {
//            "solo", "dual", "multiplex", "pip" -> {
//                "Choose Frame"
//            }
//
//            else -> {
//                "Choose " + args.mainScreenMenu
//            }
//        }
        initObservers()
        initRecyclerViews()
        initListeners()
    }

    private fun FragmentFramesBinding.initListeners() {

        if (isProVersion()) {
//            _binding?.proBtn?.pauseAnimation()
            _binding?.proBtn?.isVisible = false
        }

        homeUpBtn.setSingleClickListener {
            navController.navigateUp()
        }

        proBtn.setSingleClickListener {
            kotlin.runCatching {
                activity?.let {
                    startActivity(Intent().apply {
                        setClassName(
                            it.applicationContext,
                            getProScreen()
                        )
                        putExtra("from_frames", false)
                    })
                }
            }
        }
    }

    private fun FragmentFramesBinding.initRecyclerViews() {
        categoryTagsRv.adapter = categoryTagsAdapter
        framesRv.layoutManager= StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        framesRv.adapter = framesAdapter
    }

    private fun checkInternet() {
        _binding?.apply {
            if (!mActivity.isNetworkAvailable()) {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
//                tryNowBtn.visible()
                loadingView.stopShimmer()
                loadingView.gone()
                framesRv.invisible()
                categoryTagsRv.invisible()
            } else {
                tryNowPlaceholder.gone()
                noResultFoundTv.gone()
//                tryNowBtn.gone()
                loadingView.gone()
                loadingView.stopShimmer()
                framesRv.visible()
                if (!whichCategory())
                    categoryTagsRv.visible()
            }
        }
    }

    private fun whichCategory(): Boolean {
        return when (args.mainScreenMenu) {
            MainMenuBlendOptions.PROFILE_PICTURE.title -> true
            MainMenuBlendOptions.BLEND.title -> true
            MainMenuBlendOptions.EFFECTS.title -> true
            MainMenuBlendOptions.OVERLAY.title -> true
            MainMenuBlendOptions.DRIP_ART.title -> true
            MainMenuBlendOptions.SPIRAL.title -> true
            MainMenuBlendOptions.NEON.title -> true
            MainMenuBlendOptions.BG_ART.title -> true
            MainMenuBlendOptions.DOUBLE_EXPOSURE.title -> true
            else -> false
        }
    }

    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            if (backDecision) {
                navController.navigateUp()
            }
        }
    }

    private var tryCounter = 0

    private fun FragmentFramesBinding.initObservers() {
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
//                                    tryNowBtn.gone()
                                    loadingView.startShimmer()
                                    if (isNetworkAvailable)
                                        loadingView.visible()
                                    framesRv.invisible()
                                }

                                is Response.ShowSlowInternet -> {
                                    Log.i("SLOWINTERNET", "FramesFragement: ShowSlowInternet ")
                                    kotlin.runCatching {
                                        Toast.makeText(
                                            mActivity,
                                            it.errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }

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

                                                    MainMenuOptions.TEMPLATES.title.lowercase() -> {
                                                        it.children?.forEach {
                                                            it?.apply {
                                                                if (title.lowercase() != "all") {
                                                                    frames?.let {
                                                                        templatesFramesSubData?.set(
                                                                            title, it
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                      //  when (args.mainScreenMenu.lowercase()) {

                                           // MainMenuOptions.TEMPLATES.title.lowercase() -> {
                                                showRewardAdForThisCategory = true
                                                templatesFramesSubData?.forEach { (key, value) ->
                                                    if (_binding?.framesRv?.isComputingLayout != true) {
                                                        framesAdapter?.clearData()
                                                    }
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        val frames =
                                                            arrayListOf<FramesRV.FrameModel>()
                                                        value.forEach { it ->
                                                            it?.let {
                                                                val frame = FramesRV.FrameModel(it)
                                                                frames.add(frame)
                                                                Log.d(
                                                                    "FAHAD",
                                                                    "initObservers: SUCCESS --- ${frame.frame}"
                                                                )
                                                                frame.isFavourite =
                                                                    withContext(Dispatchers.Default) {
                                                                        favouriteFrames.mapNotNull { it?.id }
                                                                            .contains(
                                                                                FavouriteTypeConverter.fromJson(
                                                                                    FavouriteTypeConverter.toJson(
                                                                                        frame.frame
                                                                                    )
                                                                                )?.id
                                                                            )
                                                                    }
//                                                                if (key.lowercase() == args.mainScreenMenu.lowercase()) {
//                                                                    withContext(Dispatchers.Main) {
//                                                                        categoryTagsRv.visible()
//                                                                        loadingView.gone()
//                                                                        loadingView.stopShimmer()
//                                                                        if (isNetworkAvailable)
//                                                                            framesRv.visible()
////                                                                        checkInternet()
//                                                                        framesAdapter?.updateSingleItem(
//                                                                            frame
//                                                                        )
//                                                                    }
//                                                                }else
//                                                                    if((key.lowercase() == "seamless")){
//                                                                    withContext(Dispatchers.Main) {
//                                                                        categoryTagsRv.visible()
//                                                                        loadingView.gone()
//                                                                        loadingView.stopShimmer()
//                                                                        if (isNetworkAvailable)
//                                                                            framesRv.visible()
////                                                                        checkInternet()
//                                                                        framesAdapter?.updateSingleItem(
//                                                                            frame
//                                                                        )
//                                                                    }
//                                                                }
                                                            }
                                                        }
                                                        categoriesFramesSubData?.put(
                                                            key, frames
                                                        )
                                                    }.invokeOnCompletion {
                                                        isCompleted = true
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            setupTagsAndSelection()
                                                        }

                                                    }
                                                }
                                                Log.i(
                                                    "GREETCAT",
                                                    "initObservers: before update list"
                                                )
                                               /* categoryTagsAdapter?.updateDataList(
                                                    templatesFramesSubData?.keys?.toList()
                                                )*/

                                            //  setupTagsAndSelection()

                                                Log.i(
                                                    "GREETCAT",
                                                    "initObservers: ${soloFramesSubData?.keys?.toList()?.size}"
                                                )
                                           // }

//                                            else -> {
//
//                                                categoryTagsAdapter?.updateDataList(
//                                                    blendFramesSubData?.keys?.toList()
//                                                )
//                                            }
//                                        }
                                    }
                                }

                                is Response.Error -> {
                                    Log.d("FAHAD", "initObservers: ERROR")
                                    if (!mActivity.isNetworkAvailable()) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
//                                        tryNowBtn.visible()
                                        noResultFoundTv.visible()
//                                        tryNowBtn.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        framesRv.invisible()
                                    } else {
                                        if (tryCounter == 0) {
                                            activity?.let {
                                                if (it is MainActivity) {
                                                    it.refreshTemplateAndFrames()
                                                }
                                            }
                                        }
                                        tryCounter += 1
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


    private fun setupTagsAndSelection() {
        kotlin.runCatching {
            _binding?.let {binding->
                // Update tags list
                val tagsList = templatesFramesSubData?.keys?.toList()
                categoryTagsAdapter?.updateDataList(tagsList)


                // Show UI
                binding.loadingView.gone()
                binding.loadingView.stopShimmer()
                binding.categoryTagsRv.visible()
                if (isNetworkAvailable) binding.framesRv.visible()

                // Select tag based on arguments
                val targetTag = args.mainScreenMenu
                val selectedIndex = if (targetTag != null) {
                    tagsList?.indexOf(targetTag) ?: 0
                } else {
                    0
                }

                if (selectedIndex >= 0 && !tagsList.isNullOrEmpty()) {
                    val selectedTag = tagsList[selectedIndex]
                    categoryTagsAdapter?.setSelectedIndex(selectedIndex)
                    // Manually trigger the same logic as manual click
                    categoriesFramesSubData?.get(selectedTag)?.let { frames ->
                        if (frames.isNotEmpty()) {
                            categoryTagsAdapter?.select()
                            if (_binding?.framesRv?.isComputingLayout != true) {
                                framesAdapter?.clearData()
                            }
                            framesAdapter?.updateDataList(frames)
                            framesAdapter?.categoryName = selectedTag.lowercase()
                            binding.framesRv.visible()
                        } else {
                            categoryTagsAdapter?.unselect()
                        }
                    } ?: run {
                        categoryTagsAdapter?.unselect()
                    }

                    binding.categoryTagsRv.scrollToPosition(selectedIndex)
                }
            }
        }


    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
//        if (!isProVersion()) {
//            _binding?.let {
//                if (!visibleAd)
//                    _binding?.bannerContainer?.visible()
//                if (seeAllAdaptiveBanner) {
//                    activity.onResumeBanner(
//                        binding.adBannerContainer,
//                        binding.crossBannerIv,
//                        binding.bannerLayout.adContainer,
//                        binding.bannerLayout.shimmerViewContainer
//                    )
//                } else {
//                    activity?.onResumeCollapsableBanner(
//                        binding.adBannerContainer,
//                        binding.crossBannerIv,
//                        binding.bannerLayout.adContainer,
//                        binding.bannerLayout.shimmerViewContainer,
//                        lifecycleOwner = viewLifecycleOwner
//                    )
//                }
//            }
//        } else {
//            _binding?.bannerContainer?.gone()
//        }
    }

    override fun onPause() {
        super.onPause()
//        if (seeAllAdaptiveBanner) {
//            onPauseBanner()
//        } else {
//            onPauseCollapsableBanner()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadDialog?.apply { if (isShowing) dismiss() }
        _binding = null
    }
}