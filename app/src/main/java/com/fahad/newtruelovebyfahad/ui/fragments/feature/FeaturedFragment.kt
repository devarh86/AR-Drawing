package com.fahad.newtruelovebyfahad.ui.fragments.feature

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ads.Constants
import com.example.ads.crosspromo.helper.hide
import com.example.ads.dialogs.ExitModel
import com.example.ads.dialogs.onDismissDialog
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentFeaturedBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity.Companion.isFirstTime
import com.fahad.newtruelovebyfahad.ui.activities.pro.OfferPanelActivity
import com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.FeaturedPagerAdapter
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.enums.FeatureMainMenuOptions
import com.project.common.utils.enums.FeatureSubMenuOptions
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getProScreen
import com.project.common.utils.hideKeyboard
import com.project.common.utils.setDrawable
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs


@AndroidEntryPoint
class FeaturedFragment : Fragment() {

    private var _binding: FragmentFeaturedBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    private val featuredViewModel by activityViewModels<FeaturedViewModel>()
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private var featuredPagerAdapter: FeaturedPagerAdapter? = null
    private var isDataLoaded = false
    private var exitModel: ExitModel? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var downloadDialog: BottomSheetDialog? = null
        var mFeaturePager: ViewPager2? = null
    }

    private val featurePagerCallbackListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            featuredViewModel.currentPagerPosition = position
            _binding?.featuredPager?.isUserInputEnabled = true
            when (position) {
                1 -> {

                }

                2 -> {

                }

                else -> {

                }
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

        eventForGalleryAndEditor(Events.Screens.FEATURE, "", true)

        setFragmentResultListener("show_home_screen_feature") { _, bundle ->
            val showHomeScreen = bundle.getBoolean("show_home_screen_feature")
            Log.i("TAG", "onCreate featurefragment: $showHomeScreen")
            if (showHomeScreen) {
                activity?.let {
                    if (it is MainActivity) {
                        it.showHomeScreen()
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFeaturedBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            isProVersion.observe(viewLifecycleOwner) {
                if (it) {
                    _binding?.let {
                        it.proBtn.isVisible = false
                        hideGoProBottomRv()
//                        hideScreenAds()
                    }
                }
            }

            showGiftIcon()
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onViewCreated: ", ex)
        }
    }

    fun showGiftIcon() {

        kotlin.runCatching {
            if (Constants.showGiftIconFeature) {
                _binding?.goProBottomRv?.setImageDrawable(activity?.setDrawable(R.drawable.gift_feature))
                _binding?.goProBottomRv?.tag = "gift"
            } else {
                _binding?.goProBottomRv?.setImageDrawable(activity?.setDrawable(R.drawable.go_pro_bottom_rv))
                _binding?.goProBottomRv?.tag = "pro"
            }
        }
    }

    private fun FragmentFeaturedBinding.initViews() {
        if (this@FeaturedFragment.isAdded && !this@FeaturedFragment.isDetached) initObservers()
        initListeners()
    }

    fun viewPagerScrollEnable(enable: Boolean) {
        _binding?.featuredPager?.isUserInputEnabled = enable
    }

    fun hideScreenAds() {
        featuredPagerAdapter?.let {
            if (isProVersion()) {
                _binding?.featuredPager?.adapter = null
                _binding?.featuredPager?.adapter = it
//            when (featuredViewModel.currentPagerPosition) {
//                    1 -> {
//                        it.todaySpecialFragment.hideScreenAds()
//                    }
//
//                    2 -> {
//                        it.mostUsedFragment.hideScreenAds()
//                    }
//
//                    else -> {
//                        it.forYouFragment.hideScreenAds()
//                    }
//                }
            }

            if (exitModel?.dialog?.isShowing == true && !isProVersion()) {
                exitModel?.nativeContainer?.visibility = View.INVISIBLE
            }
        }
    }

    fun showScreenAds() {
        featuredPagerAdapter?.let {
            when (featuredViewModel.currentPagerPosition) {
                1 -> {
                    it.todaySpecialFragment?.showScreenAds()
                }

                2 -> {
                    it.mostUsedFragment?.showScreenAds()
                }

                else -> {
                    it.forYouFragment?.showScreenAds()
                }
            }
        }

        if (exitModel?.dialog?.isShowing == true && !isProVersion()) {
            exitModel?.nativeContainer?.isVisible = true
        }
    }

    private fun initData() {
//        if (mActivity.isNetworkAvailable()) {
//            if (apiViewModel.featureScreen.value?.data?.allTags.isNullOrEmpty()) apiViewModel.getFeatureScreen(
//                true
//            )
//        } else {
//            if (apiViewModel.offlineFeatureScreen.value?.data?.allTags.isNullOrEmpty()) apiViewModel.getFeatureScreen(
//                false
//            )
//        }
    }

    private fun FragmentFeaturedBinding.initListeners() {

        goProBottomRv.setSingleClickListener {
            if (goProBottomRv.tag != null && goProBottomRv.tag == "gift") {
                if (!ConstantsCommon.isGoProBottomRvClicked) {
                    mActivity?.let {
                        startActivity(Intent(it, OfferPanelActivity::class.java))
                        hideGoProBottomRv()
                    }
                }
            } else {
                if (!ConstantsCommon.isGoProBottomRvClicked) {
                    mActivity?.let {

                        startActivity(Intent().apply {
                            setClassName(
                                it.applicationContext,
                                getProScreen()
                            )
                            putExtra("from_frames", false)
                        })
                        hideGoProBottomRv()
                    }
                }
            }
        }

        proBtn.setSingleClickListener {
            mActivity?.let {
                startActivity(Intent().apply {
                    setClassName(
                        it.applicationContext,
                        getProScreen()
                    )
                    putExtra("from_frames", false)
                })
            }
        }
        crossImg.setSingleClickListener {
            searchEdtv.text?.clear()
        }
        searchContainer.setSingleClickListener {
            searchContainer.gone()
            enableToolBarScrolling()
        }
        cancelTv.setSingleClickListener {
            searchContainer.gone()
            enableToolBarScrolling()
            searchEdtv.text?.clear()
            searchEdtv.hideKeyboard(mActivity)
        }

        binding.searchEdtv.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.searchEdtv.showKeyboard()
            }
        }
    }

    fun showGoProBottomRv() {
        if (!isProVersion())
            _binding?.goProBottomRv?.visible()
        else {
            _binding?.goProBottomRv?.hide()
        }
    }

    fun hideGoProBottomRv() {
        _binding?.goProBottomRv?.hide()
    }

    private fun EditText.showKeyboard() {
        mActivity?.let {
            val inputMethodManager =
                it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private val activityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
                if (backDecision) {
                    (mActivity as? MainActivity)?.showHomeScreen()
                }
            }
        }

    private fun disableToolBarScrolling() {
        val params = _binding?.collapsableToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = 0
        _binding?.collapsableToolbar?.setLayoutParams(params)
    }

    private fun enableToolBarScrolling() {
        val params = _binding?.collapsableToolbar?.layoutParams as? AppBarLayout.LayoutParams
        params?.scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_ENTER_ALWAYS
        _binding?.collapsableToolbar?.setLayoutParams(params)
    }

    private fun FragmentFeaturedBinding.initObservers() {
        try {
            if (ConstantsCommon.updateInternetStatusFeature.hasObservers()) {
                ConstantsCommon.updateInternetStatusFeature.removeObservers(this@FeaturedFragment)
            }
            ConstantsCommon.updateInternetStatusFeature.observe(viewLifecycleOwner) {

//                Log.i("TAG", "initObservers: offline $it")

                checkInternet()

                if (it == true) {
                    try {
                        if (apiViewModel.featureScreen.hasObservers()) {
                            apiViewModel.featureScreen.removeObservers(this@FeaturedFragment)
                        }

                        initData()

                        apiViewModel.featureScreen.observe(viewLifecycleOwner) {
                            when (it) {
                                is Response.Loading -> {
                                    tryNowPlaceholder.gone()
                                    noResultFoundTv.gone()
                                    loadingView.startShimmer()
                                    loadingView.visible()
                                    loadingFrames.visible()
                                    featuredPager.invisible()
                                }

                                is Response.ShowSlowInternet -> {
                                    Log.i("TAG", "getFeatureScreen: ShowSlowInternet ")
                                    kotlin.runCatching {
                                        Toast.makeText(
                                            mActivity,
                                            it.errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                is Response.Success -> {

                                    if (ConstantsCommon.featureForYouData != null && ConstantsCommon.featureForYouData?.second?.first()?.thumb?.contains(
                                            "android_asset"
                                        ) == false
                                    ) {
                                        if (featuredPagerAdapter != null) {
                                            loadingView.stopShimmer()
                                            loadingView.gone()
                                            loadingFrames.gone()
                                            featuredPager.visible()
                                        } else {
                                            loadingView.stopShimmer()
                                            loadingView.gone()
                                            loadingFrames.gone()
                                            initViewPager()
                                        }
                                    } else {
                                        it.data?.allTags?.let { mainMenuOptions ->
                                            mainMenuOptions.filterNotNull()
                                                .find { it.title == FeatureMainMenuOptions.FEATURED.title }
                                                ?.tags?.let { subMenuOptions ->
                                                    isDataLoaded = true
                                                    checkInternet()
                                                    val subMenuList = subMenuOptions.filterNotNull()
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.TODAY_SPECIAL.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.featureTodaySpecialData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.FOR_YOU.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.featureForYouData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.MOST_USED.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.featureMostUsedData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                    initViewPager()
                                                }

                                            mainMenuOptions.filterNotNull()
                                                .find { it.title == FeatureMainMenuOptions.SAVE.title }
                                                ?.tags?.let { subMenuOptions ->
                                                    val subMenuList = subMenuOptions.filterNotNull()
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.TRENDING_SAVE.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.saveAndShareScreenTrendingData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                }
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    if (mActivity?.isNetworkAvailable() == false) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        loadingFrames.gone()
                                        featuredPager.invisible()
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
                } else {
                    try {
                        if (apiViewModel.offlineFeatureScreen.hasObservers()) {
                            apiViewModel.offlineFeatureScreen.removeObservers(this@FeaturedFragment)
                        }

                        initData()

                        apiViewModel.offlineFeatureScreen.observe(viewLifecycleOwner) {
//                            Log.i("TAG", "initObservers: offline $it")
                            when (it) {
                                is Response.Loading -> {
                                    tryNowPlaceholder.gone()
                                    noResultFoundTv.gone()
                                    loadingView.startShimmer()
                                    loadingView.visible()
                                    loadingFrames.visible()
                                    featuredPager.invisible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {

                                    if (ConstantsCommon.featureForYouData != null && ConstantsCommon.featureForYouData?.second?.first()?.thumb?.contains(
                                            "android_asset"
                                        ) == true
                                    ) {
                                        if (featuredPagerAdapter != null) {
                                            loadingView.stopShimmer()
                                            loadingView.gone()
                                            loadingFrames.gone()
                                            featuredPager.visible()
                                        } else {
                                            loadingView.stopShimmer()
                                            loadingView.gone()
                                            loadingFrames.gone()
                                            initViewPager()
                                        }
                                    } else {
                                        it.data?.allTags?.let { mainMenuOptions ->
                                            mainMenuOptions.filterNotNull()
                                                .find { it.title == FeatureMainMenuOptions.FEATURED.title }
                                                ?.tags?.let { subMenuOptions ->
                                                    isDataLoaded = true
                                                    val subMenuList = subMenuOptions.filterNotNull()
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.TODAY_SPECIAL.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.featureTodaySpecialData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.FOR_YOU.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.featureForYouData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.MOST_USED.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.featureMostUsedData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                    initViewPager()
                                                }

                                            mainMenuOptions.filterNotNull()
                                                .find { it.title == FeatureMainMenuOptions.SAVE.title }
                                                ?.tags?.let { subMenuOptions ->
                                                    val subMenuList = subMenuOptions.filterNotNull()
                                                    subMenuList.find { it.title == FeatureSubMenuOptions.TRENDING_SAVE.title }
                                                        ?.let { taggedFrames ->
                                                            ConstantsCommon.saveAndShareScreenTrendingData =
                                                                Pair(
                                                                    taggedFrames.tags?.filterNotNull()
                                                                        ?.map { it.title }
                                                                        ?: emptyList(),
                                                                    taggedFrames.frames?.filterNotNull()
                                                                        ?: emptyList()
                                                                )
                                                        }
                                                }
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    if (mActivity?.isNetworkAvailable() == false) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        loadingFrames.gone()
                                        featuredPager.invisible()
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

        try {
            if (apiViewModel.offlineFrame.hasObservers()) {
                apiViewModel.offlineFrame.removeObservers(this@FeaturedFragment)
            }
            apiViewModel.offlineFrame.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Loading -> {
                        Log.d("FAHAD", "initObservers: crash")
                    }

                    is Response.ShowSlowInternet -> {}

                    is Response.Success -> {
                        ConstantsCommon.resetCurrentFrames()
                        ConstantsCommon.currentFrameFeature = it.data?.frame
                        downloadDialog?.onDismissDialog(1000L) {

                            apiViewModel.clearFrame(false)
                            try {
                                if (it.data?.frame == null) {
                                    mActivity?.let {
                                        Toast.makeText(
                                            it,
                                            "Please try again",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                } else if (it.data?.frame?.editor == MainMenuOptions.BLEND.title) {

                                } else {

                                }
                                // mContext.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    }

                    is Response.Error -> {
                        downloadDialog?.onDismissDialog(1000L) {}
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("FAHAD", "initObservers: crash")
        }
    }

    private fun checkInternet() {
        _binding?.apply {
            /*if (!isNetworkAvailable) {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
                tryNowBtn.visible()
                loadingView.stopShimmer()
                loadingView.gone()
                loadingFrames.gone()
                featuredPager.invisible()
            } else {*/
            tryNowPlaceholder.gone()
            noResultFoundTv.gone()

            if (isDataLoaded) {
                loadingView.stopShimmer()
                loadingView.gone()
                loadingFrames.gone()
                featuredPager.visible()
            }
            /*}*/
        }
    }

    private fun FragmentFeaturedBinding.initViewPager() {
        checkInternet()
        featuredPagerAdapter = FeaturedPagerAdapter(this@FeaturedFragment)
        featuredPager.adapter = featuredPagerAdapter
        featuredPager.offscreenPageLimit = 3
        featuredPager.isUserInputEnabled = true
        mFeaturePager = featuredPager
        TabLayoutMediator(featuredTabLayout, featuredPager) { tab, position ->
            mContext?.let { context ->
                tab.text = when (position) {
                    1 -> ContextCompat.getString(context, com.project.common.R.string.today_special)
                    2 -> ContextCompat.getString(context, com.project.common.R.string.most_used)
                    else -> ContextCompat.getString(context, com.project.common.R.string.for_you)
                }
            }
        }.attach()
        featuredPager.registerOnPageChangeCallback(featurePagerCallbackListener)
    }

    override fun onPause() {
        super.onPause()
        exitModel?.dialog?.apply { if (isShowing) dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        featuredPagerAdapter = null
        exitModel?.dialog?.apply { if (isShowing) dismiss() }
        downloadDialog?.apply { if (isShowing) dismiss() }
        _binding?.featuredPager?.unregisterOnPageChangeCallback(featurePagerCallbackListener)
    }

    class HorizontalScrollDetector(private val viewPager: ViewPager2) :
        RecyclerView.OnItemTouchListener {
        private var touchX = 0f
        private var isDragging = false
        override fun onInterceptTouchEvent(
            rv: RecyclerView,
            e: MotionEvent,
        ): Boolean {
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = e.x
                    isDragging = false
                    viewPager.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> if (!isDragging) {
                    val dx = abs(e.x - touchX)
                    if (dx > 10) {
                        isDragging = true
                        rv.requestDisallowInterceptTouchEvent(true)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    viewPager.requestDisallowInterceptTouchEvent(false)
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }
}