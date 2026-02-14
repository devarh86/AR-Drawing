package com.fahad.newtruelovebyfahad.ui.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ads.Constants.dripArtShowAd
import com.example.ads.Constants.profilePictureShowAd
import com.example.ads.Constants.showGiftIconHome
import com.example.ads.admobs.utils.onPauseLargeBanner
import com.example.ads.crosspromo.helper.hide
import com.example.ads.dialogs.ExitModel
import com.example.ads.dialogs.createExitDialog
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValueNew
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.MainScreenNavigationDirections
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentHome2Binding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FrameRecyclerAdapterHomeParent
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.datastore.FrameDataStore
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.ConstantsCommon.saveSession
import com.project.common.utils.enums.MainMenuBlendOptions
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getProScreen
import com.project.common.utils.setColor
import com.project.common.utils.setOnSingleClickListener
import com.project.common.viewmodels.DataStoreViewModel
import com.project.common.viewmodels.HomeAndTemplateViewModel
import com.project.common.viewmodels.ViewStates
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var recyclerParentAdapter: FrameRecyclerAdapterHomeParent? = null
    private var _binding: FragmentHome2Binding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private var navController: NavController? = null
    private var beforePro = false
    var exitModel: ExitModel? = null
    private val dataStoreViewModel by activityViewModels<DataStoreViewModel>()

    private val homeAndTemplateViewModel by activityViewModels<HomeAndTemplateViewModel>()

    @Inject
    lateinit var frameDataStore: FrameDataStore
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
    else arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    private fun openTemplates(categoryName: String, itemId: Int) {
        val direction = HomeFragmentDirections.actionNavHomeToNavTemplates(
            categoryName.lowercase(),
            itemId,
            fromHome = true
        )
        activity?.navigateFragment(
            direction,
            R.id.nav_home
        )
    }

    private fun initRecyclerAdapter() {
        recyclerParentAdapter = FrameRecyclerAdapterHomeParent(onCategorySeeAllClick = {
            Log.i("CATEGORYNam", "initRecyclerAdapter:---Editor--$it")
            // selectCategory(it, it, "see_all")
            eventForGalleryAndEditor("home", "see_all_btn_$it")
            openTemplates(it, 0)
        }, onThumbClick = { frameBody, position, apiOption, tagTitle, categoryName ->
            Log.i("CATEGORYNam", "initRecyclerAdapter:$categoryName  and idFrame--${frameBody.id}")
            val categoryEvent = getEventAndAd(categoryName)
            openTemplates(categoryName, frameBody.id)
        })

        _binding?.homeParentRecyclerView?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                activity?.let {
                    kotlin.runCatching {
                        if (it is MainActivity) {
                            if (dy <= 0) {
                                _binding?.fabGoTop?.hide()
                                it.goProBottom(true)
                            } else {
                                _binding?.fabGoTop?.show()
                                it.goProBottom(false)
                            }
                        }
                    }
                }
            }

        })
    }

    fun showGoProBottomRv() {
        if (!isProVersion())
            _binding?.proTrialLay?.visible()
        else {
            _binding?.proTrialLay?.hide()
        }
    }

    fun hideGoProBottomRv() {
        _binding?.proTrialLay?.hide()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {

        isFirstTime = true

        _binding = FragmentHome2Binding.inflate(inflater, container, false)

        initRecyclerAdapter()

        parentScreen = Events.Screens.HOME

        eventForGalleryAndEditor(Events.Screens.HOME, "", true)

        return binding.root
    }

    override fun onDestroyView() {
        kotlin.runCatching {
            _binding?.homeParentRecyclerView?.adapter = null
            _binding?.appBarLayout?.apply {
                homeAndTemplateViewModel.isExpanded =
                    (height - bottom) === 0
            }
        }
        super.onDestroyView()

        _binding = null
    }

    private var isFirstTime = true

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

//            if (isFirstTime) {
            isFirstTime = false
            _binding?.homeParentRecyclerView?.itemAnimator = null
            _binding?.homeParentRecyclerView?.setHasFixedSize(true)
            _binding?.homeParentRecyclerView?.adapter = recyclerParentAdapter
            _binding?.appBarLayout?.setExpanded(homeAndTemplateViewModel.isExpanded, false)

            binding.initViews()

            try {
                isProVersion.observe(viewLifecycleOwner) {
                    if (it) {
                        _binding?.let {
                            it.proBtn.isVisible = false
                            hideGoProBottomRv()
                        }
                    }
                }

            } catch (ex: java.lang.Exception) {
                Log.e("error", "onViewCreated: ", ex)
            }
//            }

            homeAndTemplateViewModel.currentScreen = "home"
            dataStoreViewModel.readIsRatingShownAfterFirstSave.observeOnce(viewLifecycleOwner) { it ->
                if (!it && saveSession >= 1) {
                    Log.i("VALUE_DATSTORE", "onViewCreated:${it} ")
                    runCatching {
                        navController?.navigate(
                            MainScreenNavigationDirections.actionGlobalNavRating()
                        )
                    }
                }

            }

            /* if (saveSession == 2) {
                 saveSession += 1
               navController?.navigate(
                     MainScreenNavigationDirections.actionGlobalNavRating()
                 )
             } else {
                 if (!isProVersion() && !showRoboPro && fromSaved) {
                     fromSaved = false
                     try {
                         getParentActivity()?.let {
                             startActivity(Intent().apply {
                                 setClassName(
                                     it.applicationContext,
                                     getProScreen()
                                 )
                             })
                         }
                     } catch (ex: Exception) {
                         Log.e("error", "onCreate: ", ex)
                     }
                 }
             }*/

            kotlin.runCatching {
                _binding?.swipeToRefresh?.setProgressBackgroundColorSchemeColor(
                    activity?.setColor(
                        com.project.common.R.color.surface_clr
                    ) ?: Color.WHITE
                )
                _binding?.swipeToRefresh?.setColorSchemeColors(
                    activity?.setColor(com.project.common.R.color.selected_color) ?: Color.BLUE
                )
            }

            initLiveData()

            if (::mActivity.isInitialized) {
                mActivity.onBackPressedDispatcher.addCallback(
                    viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            kotlin.runCatching {
                                exitModel = mActivity.createExitDialog()
                            }
                        }
                    })
            }


        } catch (ex: java.lang.Exception) {
            Log.e("error", "onViewCreated: ", ex)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initLiveData() {

        try {
            if (homeAndTemplateViewModel.homeScreen.hasObservers()) {
                homeAndTemplateViewModel.homeScreen.removeObservers(this)
            }
            homeAndTemplateViewModel.homeScreen.observe(viewLifecycleOwner) {
                when (it) {

                    is ViewStates.Idle -> {
                        Log.i("TAG", "initLiveData: idle")

                    }

                    is ViewStates.Loading -> {
                        Log.i("TAG", "initLiveData: loading")
                        _binding?.swipeToRefresh?.isRefreshing = true
                    }

                    is ViewStates.Error -> {
                        Log.i("TAG", "initLiveData: Error")

                        kotlin.runCatching {
                            _binding?.apply {
                                if (!isNetworkAvailable && homeParentRecyclerView.isVisible) {
                                    _binding?.appBarLayout?.setExpanded(true, true)
                                    _binding?.homeParentRecyclerView?.scrollToPosition(0)
                                    _binding?.offlinePlaceHolderViewStub?.visible()
                                    homeParentRecyclerView.invisible()
                                } else {
                                    _binding?.appBarLayout?.setExpanded(true, true)
                                }
                            }

                            _binding?.swipeToRefresh?.isRefreshing = false
                        }
                    }

                    is ViewStates.UpdateObject -> {
                        Log.i("TAG", "initLiveData: UpdateObject")
                        kotlin.runCatching {
                            recyclerParentAdapter?.let { adapter ->
                                if (adapter.items.isNotEmpty() && adapter.items.last() != it.objectValue) {
                                    recyclerParentAdapter?.addItem(it.objectValue)
                                } else if (adapter.items.isEmpty()) {
                                    recyclerParentAdapter?.addItem(it.objectValue)
                                }
                                it
                            }
                        }
                    }

                    is ViewStates.Offline -> {
                        Log.i("TAG", "initLiveData: Offline")
                        kotlin.runCatching {
                            recyclerParentAdapter?.setList(it.list)

                            _binding?.apply {

                                if (homeParentRecyclerView.isVisible) {
                                    _binding?.appBarLayout?.setExpanded(true, true)
                                    _binding?.offlinePlaceHolderViewStub?.visible()
                                    homeParentRecyclerView.invisible()
                                }
                            }

                            onPauseLargeBanner()
                            _binding?.swipeToRefresh?.isRefreshing = false
                        }
                    }

                    is ViewStates.UpdateList -> {
                        Log.i("TAG", "initLiveData: UpdateList")

                        kotlin.runCatching {

                            val list = it.list

                            recyclerParentAdapter?.let {
                                if (isNetworkAvailable && it.items.size != list.size) {
                                    it.setList(list)
                                } else if (beforePro || ConstantsCommon.notifyAdapterForRewardedAssets) {
                                    ConstantsCommon.notifyAdapterForRewardedAssets = false
                                    beforePro = false
                                    it.setList(list)
                                }

                                if (isNetworkAvailable && it.items.isNotEmpty()) {
                                    _binding?.apply {
                                        if (!homeParentRecyclerView.isVisible) {
                                            _binding?.offlinePlaceHolderViewStub?.gone()
                                            homeParentRecyclerView.visible()
                                        }

                                        Log.i("TAG", "initLiveData: updateList")
                                        if (!isProVersion() && !visibleAd) {//&& !bannerContainer.isVisible
                                            lifecycleScope.launch(Main) {
                                                activity?.let {
                                                    if (it is MainActivity) {
                                                        delay(
                                                            if (it.hideCollapsibleBanner) {
                                                                // bannerLayout.adContainer.isVisible = false
                                                                it.hideCollapsibleBanner = false
                                                                2000
                                                            } else 0
                                                        )
                                                    }
                                                }

                                            }
                                        }
                                    }
                                } else {
                                    _binding?.appBarLayout?.setExpanded(true, true)
                                    _binding?.homeParentRecyclerView?.invisible()
                                }
                            }
                            _binding?.swipeToRefresh?.isRefreshing = false
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun hideScreenAds() {
        if (isProVersion()) {
            beforePro = true
            _binding?.proBtn?.gone()
            _binding?.proImgBanner?.gone()

        } else {
            visibleAd = true
            _binding?.proImgBanner?.visibility = View.VISIBLE
            _binding?.proBtn?.gone()

        }
    }

    fun showScreenAd() {
        visibleAd = false
        if (isNetworkAvailable) {
        } else {
        }
    }

    private fun FragmentHome2Binding.initViews() {

        if (!isProVersion()) {

            getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
                kotlin.runCatching {
                    val remoteString: String = if (obj.isTrailActive) {
                        "3-days Free Trial"
                    } else {
                        "Go Pro"
                    }

                    _binding?.trialDaysTxt?.text = remoteString

                }
            }
        }

        fabGoTop.setSingleClickListener {
            (binding?.homeParentRecyclerView?.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(0, 0)
        }

        proTrialLay.setSingleClickListener {
            // if (!ConstantsCommon.isGoProBottomRvClicked) {
            activity?.let {
                startActivity(Intent().apply {
                    setClassName(
                        it.applicationContext,
                        getProScreen()
                    )
                    putExtra("from_frames", false)
                    hideGoProBottomRv()
                })
            }
            //  }
        }

        offlinePlaceHolderViewStub.setOnInflateListener { stub, inflated -> }

        appBarLayout.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    if (appBarLayout.height > 50 && appBarLayout.width > 50 && _binding != null) {

                        kotlin.runCatching {
                            appBarLayout.viewTreeObserver.removeOnGlobalLayoutListener(
                                this
                            )

                            offlinePlaceHolderViewStub.layoutParams?.let {
                                if (it is CoordinatorLayout.LayoutParams) {
                                    it.updateMargins(
                                        0,
                                        0,
                                        0,
                                        ((root.height - appBarLayout.height) / (4 - 0.5)).roundToInt()
                                    )
                                }
                            }
                        }
                    }
                }
            })

        if (isNetworkAvailable && !isProVersion()) {
//            _binding?.bannerContainer?.visible()
        } else {
//            _binding?.bannerContainer?.gone()
//            _binding?.homeImgNew?.visibility = View.VISIBLE
        }

        showGiftHome()
        initListeners()
//        initSliderViewPager()
    }

    private fun showGiftHome() {
        if (!isProVersion()) {
            if (showGiftIconHome) {
                _binding?.proBtn?.isVisible = false
                //   _binding?.giftIcon?.isVisible = true

            } else {
                //  _binding?.giftIcon?.isVisible = false
                _binding?.proBtn?.isVisible = false
                _binding?.proImgBanner?.isVisible = true
            }
        } else {
            _binding?.proBtn?.isVisible = false
            //_binding?.giftIcon?.isVisible = false
            _binding?.proImgBanner?.isVisible = false
        }
    }

    override fun onPause() {
        super.onPause()
        exitModel?.dialog?.apply { if (!isDetached && isVisible && isShowing) dismiss() }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitModel?.dialog?.apply { if (!isDetached && isVisible && isShowing) dismiss() }
    }

    private var visibleAd = false

    override fun onResume() {
        super.onResume()


        /*     Log.i("TAG", "initLiveData: onResume")
             _binding?.homeParentRecyclerView?.let {
                 val recyclerView =it
                     recyclerView.post {
                         if (recyclerView.adapter != null && recyclerView.adapter!!.itemCount > 0) {
                             recyclerView.smoothScrollToPosition(0)
                         }
                     }
             }
     */

        if (!visibleAd && !isProVersion()) {
            _binding?.let {
                lifecycleScope.launch(Main) {
                    activity?.let {
                        if (it is MainActivity) {
                            delay(
                                if (it.hideCollapsibleBanner) {
                                    //_binding?.bannerLayout?.adContainer?.isVisible = false
                                    it.hideCollapsibleBanner = false
                                    2000
                                } else 0
                            )
                        }
                    }

                }
            }
        }
    }


    private fun selectCategory(parent: String, editor: String, from: String) {

        runCatching {
            when (parent) {
                MainMenuOptions.SEAMLESS.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )

                    )
                }

                MainMenuOptions.TAPE.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                MainMenuOptions.PAPER.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                MainMenuOptions.REWIND.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                MainMenuOptions.FILM.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                MainMenuOptions.PLASTIC.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                MainMenuOptions.STORIES.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                MainMenuOptions.COLLAGE.title -> {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            editor, editor
                        )
                    )
                }

                else -> {

                    showAd = false

                    val event = getEventAndAd(editor)
                    if (::mActivity.isInitialized && mActivity is MainActivity) {
                        (mActivity as MainActivity).eventForCategoryClick(
                            FrameObject(
                                screenName = "home",
                                categoryName = event,
                                from = from,
                                frameBody = ""
                            )
                        )
                    }

                    val direction =
                        HomeFragmentDirections.actionNavHomeToNavTemplates(editor.lowercase())
                    activity?.navigateFragment(
                        direction,
                        R.id.nav_home
                    )

                }
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

    var showAd: Boolean = false

    private var forTemplateScreen = false

    private fun getEventAndAd(editor: String): String {

        forTemplateScreen = false

        return when (editor) {
            MainMenuOptions.TOP_PICK.title -> {
                showAd = profilePictureShowAd

                Events.ParamsValues.HomeScreen.TOP_PICK
            }

            MainMenuBlendOptions.BLEND.title -> {


                Events.ParamsValues.HomeScreen.BLEND
            }

            MainMenuBlendOptions.EFFECTS.title -> {


                Events.ParamsValues.HomeScreen.EFFECT
            }

            MainMenuBlendOptions.DRIP_ART.title -> {
                showAd = dripArtShowAd

                Events.ParamsValues.HomeScreen.DRIP_ART
            }

            MainMenuBlendOptions.SPIRAL.title -> {

                Events.ParamsValues.HomeScreen.SPIRAL
            }

            MainMenuBlendOptions.NEON.title -> {

                Events.ParamsValues.HomeScreen.NEON
            }

            MainMenuOptions.SOLO.title -> {

                Events.ParamsValues.HomeScreen.SOLO
            }

            MainMenuOptions.COLLAGEFRAME.title -> {

                Events.ParamsValues.HomeScreen.COLLAGE_FRAMES
            }

            MainMenuOptions.DUAL.title -> {

                Events.ParamsValues.HomeScreen.DUAL
            }

            MainMenuOptions.MULTIPLEX.title -> {

                Events.ParamsValues.HomeScreen.MULTIPLEX
            }

            MainMenuOptions.PIP.title -> {

                Events.ParamsValues.HomeScreen.PIP
            }

            MainMenuBlendOptions.OVERLAY.title -> {

                Events.ParamsValues.HomeScreen.Overlay
            }

            MainMenuBlendOptions.BG_ART.title -> {

                Events.ParamsValues.HomeScreen.BG_ART
            }

            MainMenuBlendOptions.DOUBLE_EXPOSURE.title -> {

                Events.ParamsValues.HomeScreen.DOUBLE_EXPOSURE
            }

            else -> {

                forTemplateScreen = true
                editor.lowercase() // for subcategories
            }
        }

    }

    private fun FragmentHome2Binding.initListeners() {


        swipeToRefresh.setOnRefreshListener {
            homeAndTemplateViewModel.getHomeTemplateScreen()
        }

        menuContainer.setSingleClickListener {
            try {
                activity?.navigateFragment(
                    HomeFragmentDirections.actionNavHomeToSettingFragment(),
                    R.id.nav_home
                )
            } catch (ex: Exception) {
                Log.e("error", "initListeners: ", ex)
            }

        }
        proBtn.setSingleClickListener {
            /*activity?.let {
                startActivity(Intent().apply {
                    setClassName(
                        it.applicationContext,
                        getProScreen()
                    )
                    putExtra("from_frames", false)
                })
            }*/
        }

        proImgBanner.setSingleClickListener {
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



        seamlessBtn.setSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.SEAMLESS,
                    from = "seamless_btn",
                    frameBody = ""
                )
            )

            selectCategory(MainMenuOptions.SEAMLESS.title, MainMenuOptions.SEAMLESS.title, "btn")
        }
        stitchBtn.setSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.STITCH,
                    from = "stitch_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.STITCH.title, MainMenuOptions.STITCH.title, "btn")
        }
        collageBtnN.setSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.COLLAGE,
                    from = "collage_btn",
                    frameBody = ""
                )
            )

            selectCategory(MainMenuOptions.COLLAGE.title, MainMenuOptions.COLLAGE.title, "btn")
        }
        storyBtn.setSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.STORIES,
                    from = "story_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.STORIES.title, MainMenuOptions.STORIES.title, "btn")

        }
        filmBtn.setOnSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.FILM,
                    from = "film_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.FILM.title, MainMenuOptions.FILM.title, "btn")
        }
        plasticBtn.setOnSingleClickListener {

            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.PLASTIC,
                    from = "plastic_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.PLASTIC.title, MainMenuOptions.PLASTIC.title, "btn")
        }
        tapeBtn.setOnSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.TAPE,
                    from = "tape_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.TAPE.title, MainMenuOptions.TAPE.title, "btn")
        }
        paperBtn.setOnSingleClickListener {
            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.PAPER,
                    from = "paper_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.PAPER.title, MainMenuOptions.PAPER.title, "btn")
        }
        rewindBtn.setOnSingleClickListener {

            eventForCategoryClick(
                FrameObject(
                    screenName = "home",
                    categoryName = Events.ParamsValues.HomeScreen.REWIND,
                    from = "rewind_btn",
                    frameBody = ""
                )
            )
            selectCategory(MainMenuOptions.REWIND.title, MainMenuOptions.REWIND.title, "btn")
        }


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