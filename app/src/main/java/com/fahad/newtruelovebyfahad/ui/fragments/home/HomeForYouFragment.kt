package com.fahad.newtruelovebyfahad.ui.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.example.ads.Constants.showForYouFrameClickAd
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.admobs.utils.showRewardedInterstitial
import com.example.ads.dialogs.ExitModel
import com.example.ads.dialogs.createExitDialog
import com.example.ads.dialogs.createProFramesDialog
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentHomeBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity.Companion.isFirstTime
import com.fahad.newtruelovebyfahad.ui.activities.pro.slider.SliderList.getImageListHome
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.HomeForYouAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.HomeSliderAdapter
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.printLog
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.repo.room.model.RecentsModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.ConstantsCommon.featureForYouData
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.enums.FeatureMainMenuOptions
import com.project.common.utils.enums.FeatureSubMenuOptions
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getProScreen
import com.project.common.utils.setDrawable
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HomeForYouFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null

    private val slideHandler = Handler(Looper.getMainLooper())
    private var slideRunnable: Runnable? = null

    private val apiViewModel by activityViewModels<ApiViewModel>()
    var exitModel: ExitModel? = null
    private var isDataLoaded = false
    private var forYouFramesAdapter: HomeForYouAdapter? = null
    private var featureData: MutableList<HomeForYouAdapter.FrameModel> = mutableListOf()

    @Inject
    lateinit var frameDataStore: FrameDataStore

    val TAG = "HomeForYouFragment"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kotlin.runCatching { navController = findNavController() }

        eventForGalleryAndEditor(Events.Screens.HOME, "", true)

        forYouFramesAdapter = HomeForYouAdapter(mContext, arrayListOf(), null, onClick = { frameBody, position ->
            Log.i(TAG, "onCreate: $position")
            Log.i(TAG, "onCreate: ${frameBody.baseUrl}${frameBody.thumb}")

            if (!isNetworkAvailable) {
                forYouFramesAdapter?.let {
                    kotlin.runCatching {
                        if (!frameBody.thumb.contains("android_asset")) {
                            kotlin.runCatching {
                                mActivity?.let {
                                    Toast.makeText(
                                        mActivity,
                                        "Please connect to internet",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            return@HomeForYouAdapter
                        }
                    }
                }
            }

            val permissions =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
                )
                else arrayOf(

                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )

            (mActivity as Permissions).checkAndRequestPermissions(*permissions, action = {
                mActivity?.let {
                    if (it is MainActivity) {
                        it.eventForFrameClick(
                            FrameObject(
                                frameBody.id,
                                frameBody.title,
                                Events.Screens.FEATURE,
                                "",
                                Events.Screens.FEATURE,
                                frameBody.tags ?: "",
                                frameBody.baseUrl ?: "",
                                frameBody.thumb,
                                frameBody.thumbtype,
                                true,
                                true,
                                frameBody,
                                "list"
                            )
                        )
                    }
                }

                mContext?.let { context ->
                    if (frameBody.tags?.isNotEmpty() == true && frameBody.tags != "Free" &&
                        !isProVersion() && !ConstantsCommon.rewardedAssetsList.contains(frameBody.id)
                    ) {
                        mActivity?.createProFramesDialog(
                            true,
                            thumb = "${frameBody.baseUrl}${frameBody.thumb}",
                            thumbType = ContextCompat.getDrawable(
                                context, when (frameBody.thumbtype.lowercase()) {
                                    FrameThumbType.PORTRAIT.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_portrait
                                    FrameThumbType.LANDSCAPE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_landscape
                                    FrameThumbType.SQUARE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_squre
                                    else -> com.project.common.R.drawable.frame_placeholder_portrait
                                }
                            ),
                            action = {

                                mActivity?.showRewardedInterstitial(
                                    true,
                                    loadedAction = {
                                        apiViewModel.addToRecent(RecentsModel(frame = frameBody))
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            frameDataStore.writeUnlockedId(frameBody.id)
                                            ConstantsCommon.rewardedAssetsList.add(
                                                frameBody.id
                                            )
                                            withContext(Main) {
                                                if (position != -1) forYouFramesAdapter?.notifyItemChanged(
                                                    position
                                                )
                                                val onlineOrOfflineFrame =
                                                    if (frameBody.thumb.contains("android_asset")) {
                                                        false
                                                    } else {
                                                        isNetworkAvailable
                                                    }
                                                apiViewModel.getFrame(
                                                    frameBody.id, onlineOrOfflineFrame
                                                )
                                            }
                                        }.invokeOnCompletion {}
                                    },
                                    failedAction = {

                                    })

                            },
                            goProAction = {
                                try {
                                    activity?.let {
                                        startActivity(Intent().apply {
                                            setClassName(
                                                it.applicationContext,
                                                getProScreen()
                                            )
                                            putExtra("from_frames", false)
                                        })
                                    }
                                } catch (_: Exception) {
                                }
                            },
                            dismissAction = {},
                            frameBody.tags?.lowercase() == "paid"
                        )
                    } else {
                        apiViewModel.addToRecent(RecentsModel(frame = frameBody))
                        mActivity.showInterstitial(
                            loadedAction = {
                                val onlineOrOfflineFrame =
                                    if (frameBody.thumb.contains("android_asset")) {
                                        false
                                    } else {
                                        isNetworkAvailable
                                    }
                                apiViewModel.getFrame(
                                    frameBody.id, onlineOrOfflineFrame
                                )
                            }, failedAction = {
                                val onlineOrOfflineFrame =
                                    if (frameBody.thumb.contains("android_asset")) {
                                        false
                                    } else {
                                        isNetworkAvailable
                                    }
                                apiViewModel.getFrame(
                                    frameBody.id, onlineOrOfflineFrame
                                )
                            }, showAd = showForYouFrameClickAd, onCheck = true
                        )
                    }
                }
            }, declineAction = {})

        }, onFavouriteClick = {
            apiViewModel.favourite(
                FavouriteModel(
                    isFavourite = it.isFavourite, frame = it.frame
                )
            )
        }, onPurchaseTypeTagClick = {})
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hideScreenAds() {
        if (isProVersion()) {
            _binding?.proBtn?.gone()
        } else {
            _binding?.proBtn?.gone()
        }
    }

    fun showScreenAd() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.initViews()
    }

    private fun FragmentHomeBinding.initSliderViewPager() {
        Log.e("initSliderViewPager", "initSliderViewPager: CALL")
        val list = getImageListHome(mContext ?: return)

        val adapter = HomeSliderAdapter(
            list
        ) { parent ->
            selectCategory(parent)
        }

        topPager.adapter = adapter

        // ✅ Build dots dynamically
        dotContainer.removeAllViews()
        val dots = Array(adapter.itemCount) { ImageView(context) }
        dots.forEachIndexed { index, imageView ->
            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.marginStart = 2
            params.marginEnd = 2
            imageView.layoutParams = params
            imageView.setImageDrawable(context.setDrawable(R.drawable.un_selected))
            dotContainer.addView(imageView)
        }

        if (dots.isNotEmpty()) {
            dots[0].setImageDrawable(context.setDrawable(R.drawable.selected))
        }

        // ✅ Handle page change
        topPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                dots.forEach {
                    it.setImageDrawable(context.setDrawable(R.drawable.un_selected))
                }
                dots[position].setImageDrawable(context.setDrawable(R.drawable.selected))

                slideRunnable?.let {
                    slideHandler.removeCallbacks(it)
                    slideHandler.postDelayed(it, 2600)
                }
            }
        })

        // ✅ Auto slide runnable
        slideRunnable = Runnable {
            topPager.adapter?.let {
                val currentItem = topPager.currentItem
                val nextItem = if (currentItem == it.itemCount - 1) 0 else currentItem + 1
                topPager.setCurrentItem(nextItem, nextItem != 0)
            }
        }
    }

    private fun FragmentHomeBinding.initViews() {

        if (this@HomeForYouFragment.isAdded && !this@HomeForYouFragment.isDetached) initObserver()

        initRecyclerViews()

        initListeners()
        initSliderViewPager()

        mActivity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (mActivity is MainActivity) {
                        exitModel = mActivity?.createExitDialog()
                    }
                }
            })
    }

    private fun FragmentHomeBinding.initObserver() {

        try {
            isProVersion.observe(viewLifecycleOwner) {
                if (it) {
                    _binding?.let {
                        hideScreenAds()
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onViewCreated: ", ex)
        }

        try {

            var isCompleted = true

            ConstantsCommon.updateInternetStatusFeature.observe(
                viewLifecycleOwner
            ) {
                checkInternet()
                if (it == true) {
                    try {
                        apiViewModel.featureScreen.observe(viewLifecycleOwner) myObserver@{ response ->
                            when (response) {
                                is Response.Loading -> {
                                    _binding?.swipeToRefresh?.isRefreshing = true
                                    offlinePlaceHolderViewStub.gone()
                                    loadingView.startShimmer()
                                    loadingView.visible()
                                    framesRv.invisible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {
                                    _binding?.swipeToRefresh?.isRefreshing = false
                                    if (featureData.isNotEmpty()) {

                                        loadingView.gone()
                                        loadingView.stopShimmer()
                                        if (isNetworkAvailable) {
                                            framesRv.visible()

                                            forYouFramesAdapter?.let {
                                                if (it.itemCount != featureData.size)
                                                    it.updateDataList(featureData)
                                            }
                                        }
                                    } else {

                                        if (isCompleted) {
                                            isCompleted = false

                                            kotlin.runCatching {
                                                response.data?.allTags?.filterNotNull()?.forEach {
                                                    when (it.title.lowercase()) {
                                                        FeatureSubMenuOptions.FEATURED.title.lowercase() -> {
                                                            val featureTags =
                                                                it.tags?.find { it?.title?.lowercase() == FeatureSubMenuOptions.FOR_YOU.title.lowercase() }
                                                            kotlin.runCatching {
                                                                featureTags?.frames?.let { mFrames ->
                                                                    if (_binding?.framesRv?.isComputingLayout == false) {
                                                                        forYouFramesAdapter?.clearData()
                                                                    }

                                                                    lifecycleScope.launch(
                                                                        Dispatchers.IO
                                                                    ) {
                                                                        mFrames.mapIndexed { index, frame ->
//                                                                            var position = index
                                                                            frame?.let {

                                                                                val result =
                                                                                    HomeForYouAdapter.FrameModel(
                                                                                        it
                                                                                    )

                                                                                result.isFavourite =
                                                                                    withContext(
                                                                                        Dispatchers.Default
                                                                                    ) {
                                                                                        favouriteFrames.mapNotNull { it?.id }
                                                                                            .contains(
                                                                                                FavouriteTypeConverter.fromJson(
                                                                                                    FavouriteTypeConverter.toJson(
                                                                                                        result.frame
                                                                                                    )
                                                                                                )?.id
                                                                                            )
                                                                                    }

                                                                                withContext(Main) {

                                                                                    loadingView.gone()
                                                                                    loadingView.stopShimmer()
                                                                                    if (isNetworkAvailable) framesRv.visible()

                                                                                    forYouFramesAdapter?.updateSingleItem(
                                                                                        result
                                                                                    )

                                                                                    featureData.add(
                                                                                        result
                                                                                    )

                                                                                }
                                                                                result
                                                                            }
                                                                        }
                                                                    }.invokeOnCompletion {
                                                                        isCompleted =
                                                                            true
                                                                    }

                                                                } ?: kotlin.run {
                                                                    isCompleted = true
                                                                }
                                                            }.onFailure {
                                                                isCompleted = true
                                                            }
                                                        }
                                                    }

                                                }
                                            }.onFailure {
                                                isCompleted = true
                                            }
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    _binding?.swipeToRefresh?.isRefreshing = false
                                    if (!mActivity.isNetworkAvailable()) {
                                        offlinePlaceHolderViewStub.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        framesRv.invisible()
                                    }
                                }
                            }
                        }


                    } catch (_: Exception) {

                    }
                } else {
                    try {
                        if (apiViewModel.offlineFeatureScreen.hasObservers()) {
                            apiViewModel.offlineFeatureScreen.removeObservers(this@HomeForYouFragment)
                        }
                        apiViewModel.offlineFeatureScreen.observe(viewLifecycleOwner) {
                            when (it) {
                                is Response.Loading -> {
                                    offlinePlaceHolderViewStub.gone()
                                    loadingView.startShimmer()
                                    loadingView.visible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {
                                    forYouFramesAdapter?.clearData()
                                    if (featureForYouData != null && featureForYouData?.second?.first()?.thumb?.contains(
                                            "android_asset"
                                        ) == true
                                    ) {
                                        if (forYouFramesAdapter != null) {
                                            loadingView.stopShimmer()
                                            loadingView.gone()
                                            Log.e("FINDCOMING", "initObserver: IF ")
                                            framesRv.visible()
                                            offlineDataSettingToAdapter()
                                        } else {
                                            Log.e("FINDCOMING", "initObserver: ELSE")
                                            loadingView.stopShimmer()
                                            loadingView.gone()
                                            initViewPager()
                                        }
                                    } else {
                                        Log.e("FINDCOMING", "initObserver: ELSE ELSE")
                                        it.data?.allTags?.let { mainMenuOptions ->
                                            mainMenuOptions.filterNotNull()
                                                .find { it.title == FeatureMainMenuOptions.FEATURED.title }?.tags?.let { subMenuOptions ->
                                                    isDataLoaded = true
                                                    checkInternet()
                                                    val subMenuList = subMenuOptions.filterNotNull()
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.FOR_YOU.title }
                                                        ?.let { taggedFrames ->
                                                            featureForYouData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList())
                                                        }
                                                    Log.e(
                                                        "FINDCOMING",
                                                        "initObserver: ELSE ELSE  DATA",
                                                    )
                                                    initViewPager()
                                                }

                                            mainMenuOptions.filterNotNull()
                                                .find { it.title == FeatureMainMenuOptions.SAVE.title }?.tags?.let { subMenuOptions ->
                                                    val subMenuList = subMenuOptions.filterNotNull()
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.TRENDING_SAVE.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.saveAndShareScreenTrendingData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList())
                                                        }
                                                }
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    if (mActivity?.isNetworkAvailable() == false) {
                                        offlinePlaceHolderViewStub.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        runCatching {
                            (mActivity as? MainActivity)?.showHomeScreen()
                        }
                        isFirstTime = true
                    }

                }


            }


        } catch (_: Exception) {

        }
    }

    private fun FragmentHomeBinding.initRecyclerViews() {
        framesRv.adapter = forYouFramesAdapter
    }

    private fun checkInternet() {
        _binding?.apply {
            offlinePlaceHolderViewStub.gone()

            if (isDataLoaded) {
                loadingView.stopShimmer()
                loadingView.gone()
            }
        }
    }

    private fun initViewPager() {
        checkInternet()
        offlineDataSettingToAdapter()
    }

    private fun offlineDataSettingToAdapter() {
        featureForYouData?.second?.forEach {
            it?.let {
                val frame = HomeForYouAdapter.FrameModel(it)
                frame.isFavourite = favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                forYouFramesAdapter?.updateSingleItem(frame)
            }
        }
    }


    private fun FragmentHomeBinding.initListeners() {


        swipeToRefresh.setOnRefreshListener {
            if (apiViewModel.featureScreen.value?.data?.allTags.isNullOrEmpty()) apiViewModel.getFeatureScreen(true)
        }

        menuContainer.setSingleClickListener {
            try {
                activity?.navigateFragment(
                    HomeForYouFragmentDirections.actionHomeForYouFragmentToSettingFragment(),
                    R.id.homeForYouFragment
                )
            } catch (ex: Exception) {
                Log.e("error", "initListeners: ", ex)
            }
        }

        proBtn.setSingleClickListener {
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

        arDrawing.setSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.SEAMLESS,
                    from = "ardrawing_btn",
                    frameBody = ""
                )
            )

            selectCategory(MainMenuOptions.DRAWING.title)
        }

        sketch.setSingleClickListener {
            if (isNetworkAvailable) {

                eventForCategoryClick(
                    FrameObject(
                        screenName = "home",
                        categoryName = Events.ParamsValues.HomeScreen.SEAMLESS,
                        from = "photo_sketch_btn",
                        frameBody = ""
                    )
                )

                selectCategory(MainMenuOptions.SKETCH.title)

            } else {
                context?.let { cntx ->
                    Toast.makeText(
                        cntx,
                        com.project.common.R.string.no_internet_connect_found_please_try_again,
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }


    }

    private fun selectCategory(from: String) {

        runCatching {
            when (from) {
                MainMenuOptions.DRAWING.title -> {

                    try {
                        val permissions =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.CAMERA
                            )
                            else arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                            )
                        (mActivity as Permissions).checkAndRequestPermissions(
                            *permissions,
                            action = {
                                runCatching {
                                    activity?.showNewInterstitial(activity?.homeInterstitial()) {
                                        activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                                        kotlin.runCatching {
                                            navController?.navigate(
                                                HomeForYouFragmentDirections.actionHomeForYouFragmentToDrawingFramesFragment(
                                                    from,
                                                    from
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            declineAction = {})
                    } catch (ex: Exception) {
                        printLog(ex.message.toString())
                    }

                }

                MainMenuOptions.SKETCH.title -> {

                    try {
                        val permissions =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.CAMERA
                            )
                            else arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                            )
                        (mActivity as Permissions).checkAndRequestPermissions(
                            *permissions,
                            action = {
                                runCatching {
                                    activity?.showNewInterstitial(activity?.homeInterstitial()) {
                                        activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                                        kotlin.runCatching {
                                            val intent = Intent(mActivity, PencilSketchActivity::class.java)
                                            getParentActivity()?.getActivityLauncher()?.launch(intent) //mActivity.startActivity(intent)
                                        }
                                    }
                                }
                            },
                            declineAction = {})
                    } catch (ex: Exception) {
                        printLog(ex.message.toString())
                    }

                }

                else -> {}
            }
        }
    }

    private fun getParentActivity(): MainActivity? {
        activity?.let {
            if (it is MainActivity) {
                return it
            }
        }
        return null
    }

    override fun onPause() {
        super.onPause()
        exitModel?.dialog?.apply { if (!isDetached && isVisible && isShowing) dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        slideRunnable?.let { slideHandler.removeCallbacks(it) }
        exitModel?.dialog?.apply { if (!isDetached && isVisible && isShowing) dismiss() }
    }

    fun eventForCategoryClick(frameBody: FrameObject) {
        val bundle = Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)

            putString(Events.ParamsKeys.FROM, frameBody.from)

            if (frameBody.categoryName.isNotBlank())
                putString(Events.ParamsKeys.CATNAME, frameBody.categoryName)

            if (frameBody.subCategoryName.isNotBlank())
                putString(Events.ParamsKeys.SUB_SCREEN, frameBody.subCategoryName)
        }
        firebaseAnalytics?.logEvent(frameBody.screenName, bundle)

        Log.i(
            "firebase_events_clicks",
            "events: screenName: ${frameBody.screenName} bundle:  $bundle"
        )
    }

}