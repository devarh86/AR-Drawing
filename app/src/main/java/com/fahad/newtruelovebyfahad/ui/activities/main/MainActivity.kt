package com.fahad.newtruelovebyfahad.ui.activities.main


import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.ads.Constants.CAN_LOAD_ADS
import com.example.ads.Constants.failureMsg
import com.example.ads.Constants.featureMenuShowAd
import com.example.ads.Constants.homeMenuShowAd
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.myWorkMenuShowAd
import com.example.ads.Constants.native
import com.example.ads.Constants.popupEventValentine
import com.example.ads.Constants.showHomeScreen
import com.example.ads.Constants.styleMenuShowAd
import com.example.ads.Constants.templatesMenuShowAd
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.loadNewInterstitialForPro
import com.example.ads.admobs.utils.loadNewInterstitialWithoutStrategyCheck
import com.example.ads.admobs.utils.loadRewarded
import com.example.ads.admobs.utils.onPauseBanner
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.admobs.utils.showRewardedInterstitial
import com.example.ads.crosspromo.viewModel.CrossPromoViewModel
import com.example.ads.dialogs.createDownloadingDialog
import com.example.ads.dialogs.createProFramesDialog
import com.example.ads.dialogs.onDismissDialog
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.MainScreenNavigationDirections
import com.fahad.newtruelovebyfahad.MyApp
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.ActivityMainBinding
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragment
import com.fahad.newtruelovebyfahad.ui.fragments.frames.FramesFragment
import com.fahad.newtruelovebyfahad.ui.fragments.home.HomeForYouFragment
import com.fahad.newtruelovebyfahad.ui.fragments.home.HomeFragment
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.MyWorkFragment
import com.fahad.newtruelovebyfahad.ui.fragments.styles.StylesFragment
import com.fahad.newtruelovebyfahad.ui.fragments.template.TemplatesBaseFragment
import com.fahad.newtruelovebyfahad.ui.fragments.template.TemplatesFragment
import com.fahad.newtruelovebyfahad.utils.InternetConnectivityReceiver
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.interfaces.InternetConnectivityListener
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.databinding.BottomFullScreenPermissionBinding
import com.project.common.databinding.ValentinePopUpBinding
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.ApiConstants
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.RecentsModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.ConstantsCommon.receivedData
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.utils.setString
import com.project.common.viewmodels.ApiViewModel
import com.project.common.viewmodels.DataStoreViewModel
import com.project.common.viewmodels.HomeAndTemplateViewModel
import com.project.common.viewmodels.SearchViewModel
import com.xan.event_notifications.data.NotificationLockScreenHelper
import com.xan.event_notifications.data.constants.Constants.notiLockScreen
import com.xan.event_notifications.data.constants.Constants.notiLockscreenCountry
import com.xan.event_notifications.data.constants.Constants.timePushNotiLockscreen1
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : Permissions(), InternetConnectivityListener {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private val apiViewModel by viewModels<ApiViewModel>()
    private val searchViewModel by viewModels<SearchViewModel>()
    private val homeAndTemplateViewModel by viewModels<HomeAndTemplateViewModel>()
    private val mainViewModel by viewModels<MainViewModel>()
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private val crossPromoViewModel by viewModels<CrossPromoViewModel>()
    var showAppOpen = false
    var hideCollapsibleBanner = false
    var receivedData1: String? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    //private var mLoaderCallback: LoaderCallbackInterface? = null

    private var alreadyPro = false

    companion object {
        var isFirstTime: Boolean = false
    }

    fun reLoadBannerAdForFeature() {
        loadBannerAd(fromButton = true)
    }

    private fun initNavigationGraph() {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            navController = navHostFragment?.navController

            setupSmoothBottomMenu()
            navController?.addOnDestinationChangedListener { _, destination, _ ->

                when (destination.id) {
                    R.id.homeForYouFragment, R.id.nav_featured, R.id.nav_home, R.id.nav_mywork, R.id.nav_frame_types,
                    R.id.nav_coming_soon, R.id.nav_favourite_menu, R.id.nav_styles, R.id.nav_templates_base -> {
                        binding.bottomBar.gone()
                        binding.crossPromoAdsCv.gone()
                        binding.adTv.gone()
                        binding.addIcon.gone()
                    }

                    else -> {
                        binding.bottomBar.gone()
                        binding.addIcon.gone()
                        binding.crossPromoAdsCv.gone()
                        binding.adTv.gone()
                        loadBannerAd()
                    }
                }
                if (destination.id == R.id.nav_featured && isFirstTime) {
                    runCatching {
                        isFirstTime = false
                        showHomeScreen()
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "initNavigationGraph: ", ex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TAG", "onStop: mainActivity onDestroy")
    }


    private var isPopUpShown = false

    private fun showApplyPopUpValentine() {
        if (isPopUpShown) {
            return
        }
        isPopUpShown = true
        var isValentineShown: Boolean

        kotlin.runCatching {
            dataStoreViewModel.valentinePopShown.observeOnce(this) {
                isValentineShown = it
                dataStoreViewModel.updateValentinePopShown()
                Log.i("TAG", "showApplyPopUp: $it")
                lifecycleScope.launch(Main) {
                    if (popupEventValentine && !isValentineShown) {
                        showValentinePopUp()

                    }
                }
            }
        }
    }

    private fun showValentinePopUp() {

        val popUp = Dialog(this, com.project.common.R.style.PopUp)
        popUp.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popUp.setCancelable(false)
        val bindingPopUp = ValentinePopUpBinding.inflate(layoutInflater)
        popUp.setContentView(bindingPopUp.root)

        bindingPopUp.closeImg.visibility = View.INVISIBLE

        lifecycleScope.launch(Default) {
            delay(2000)
            withContext(Main) {
                kotlin.runCatching {
                    if (!isFinishing && !isDestroyed && popUp.isShowing && isActive) {
                        bindingPopUp.closeImg.visibility = View.VISIBLE
                    }
                }
            }
        }

        bindingPopUp.closeImg.setSingleClickListener {
            if (!isDestroyed && !isFinishing) {
                popUp.dismiss()
            }
        }

        bindingPopUp.applyNow.setSingleClickListener {
            if (!isDestroyed && !isFinishing) {
                popUp.dismiss()
            }

            kotlin.runCatching {
                navigateToValentineFrames()
            }
        }

        if (!popUp.isShowing && !isDestroyed && !isFinishing) {
            popUp.show()


        }

    }


    private fun navigateToValentineFrames() {
        val type = "valentine day\uD83D\uDC95".lowercase()
        navController?.navigate(
            MainScreenNavigationDirections.actionGlobalNavFramesFragment(type, type)
        )


    }

    private fun scheduleNotification() {

        notiLockScreen?.let { notiLockcreen ->
            if (!notiLockcreen) {
                return
            }
            val notificationHelper = NotificationLockScreenHelper(this)
            Log.d("TAG", "Splash notiLockcreen: $notiLockcreen")
            notiLockscreenCountry.let { it1 ->
                timePushNotiLockscreen1?.let { timePushNotiLockscreen1 ->
                    Log.d("TAG", "Splash timePushNotiLockscreen1: $timePushNotiLockscreen1")
                    notificationHelper.scheduleAlarmForConditions(
                        it1,
                        timePushNotiLockscreen1,
                        null
                    )
                }
//                timePushNotiLockscreen2?.let { timePushNotiLockscreen2 ->
//                    Log.d("TAG", "Splash timePushNotiLockscreen2: $timePushNotiLockscreen2")
//                    notificationHelper.scheduleAlarmForConditions(
//                        it1,
//                        null,
//                        timePushNotiLockscreen2
//                    )
//                }
            }
        }
    }

    private var fullScreenPopUp: BottomSheetDialog? = null
    private var fullScreenPopUpBinding: BottomFullScreenPermissionBinding? = null
    private var intentPermissionCounter = 1
    private fun initFullScreenPopUp() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            kotlin.runCatching {
                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (notificationManager.canUseFullScreenIntent()) {
                    scheduleNotification()

                } else {
                    fullScreenPopUp?.let {
                        if (!isDestroyed && !isFinishing && !it.isShowing) {
                            fullScreenPopUp?.show()
                        }
                    } ?: kotlin.run {

                        fullScreenPopUp =
                            BottomSheetDialog(
                                this,
                                com.project.common.R.style.BottomSheetDialog_full_screen
                            )

                        fullScreenPopUpBinding =
                            BottomFullScreenPermissionBinding.inflate(layoutInflater)
                        fullScreenPopUpBinding?.root?.let { it1 ->
                            fullScreenPopUp?.setContentView(
                                it1
                            )
                        }

                        fullScreenPopUp?.setCancelable(false)

                        fullScreenPopUp?.setOnDismissListener {

                            kotlin.runCatching {
                                if (!notificationManager.canUseFullScreenIntent()) {
                                    Toast.makeText(
                                        this,
                                        setString(com.project.common.R.string.full_screen_intent),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }


                        }

                        fullScreenPopUpBinding?.allow?.setSingleClickListener {
                            // AppOpenManager.getInstance().disableAdResumeByClickAction()
                            checkAndRequestFullScreenPermission(
                                onGranted = {
                                    if (!isDestroyed && !isFinishing) {
                                        fullScreenPopUp?.dismiss()
                                    }

                                    scheduleNotification()
                                }, onDenied = {

                                    intentPermissionCounter += 1

                                    if (intentPermissionCounter < 4) {
                                        initFullScreenPopUp()
                                    } else {
                                        if (!isDestroyed && !isFinishing) {
                                            fullScreenPopUp?.dismiss()
                                        }
                                    }
                                },
                                notificationManager
                            )
                        }

                        fullScreenPopUpBinding?.dontAllow?.setSingleClickListener {

                            if (!isDestroyed && !isFinishing && fullScreenPopUp?.isShowing == true) {
                                fullScreenPopUp?.dismiss()
                            }

                            Toast.makeText(
                                this,
                                setString(com.project.common.R.string.full_screen_intent),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        fullScreenPopUp?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        if (!isDestroyed && !isFinishing && fullScreenPopUp?.isShowing == false) {
                            fullScreenPopUp?.show()
                        }
                    }
                }
            }
        } else {
            scheduleNotification()
        }
    }


    private fun initUninstallIntentValue() {
        receivedData1 = intent.getStringExtra("shortcut_extra_key1")
        if (receivedData1 != null) {
            Log.d("UNISNATLLINT", "Received from shortcut: $receivedData1")

        } else {
            Log.d("UNISNATLLINT", "initGetValue: receivedData1 is null")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUninstallIntentValue()

//        installSplashScreen()
        kotlin.runCatching {
            setLocale(languageCode)
        }
        _binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)


        loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}


        kotlin.runCatching {
            dataStoreViewModel.initFun()
            lifecycleScope.launch(IO) {
                dataStoreViewModel.updateIntroComplete()
            }
        }

        kotlin.runCatching {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            registerForFullScreenIntent(notificationManager)
        }

        kotlin.runCatching {
            failureMsg = setString(com.project.common.R.string.try_again)
        }

        isNetworkAvailable = isNetworkAvailable()

        mainViewModel.isSplashVisible = false

        initNavigationGraph()

//        showApplyPopUpValentine()

        checkFragment(hideViews = false)

//        LeakCanary.config = LeakCanary.config.copy(retainedVisibleThreshold = 1)

        checkAndRequestNotificationPermission(
            onGranted = {
                initFullScreenPopUp()
            },
            onDenied = {
                Log.d("Notification", "Notification permission denied")
                //showApplyPopUp()
            }
        )
        kotlin.runCatching {
            dataStoreViewModel.incrementAppSession()
            dataStoreViewModel.writeCurrentTime()
        }

        firebaseAnalytics?.logEvent(Events.NetworkKeys.INTERNET, Bundle().apply {
            putString(
                Events.NetworkParams.INTERNET_STATE, if (isNetworkAvailable()) "true" else "false"
            )
        })



        try {
            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@MainActivity as LifecycleOwner)
            }

            alreadyPro = isProVersion()

            isProVersion.observe(this@MainActivity) {
                if (!alreadyPro) {
                    checkFragment(it)
                }
                alreadyPro = it
            }
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }

        try {
            if (apiViewModel.frame.hasObservers()) {
                apiViewModel.frame.removeObservers(this)
            }

            apiViewModel.frame.observe(this) {
                when (it) {
                    is com.project.common.repo.api.apollo.helper.Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is com.project.common.repo.api.apollo.helper.Response.ShowSlowInternet -> {}

                    is com.project.common.repo.api.apollo.helper.Response.Success -> {
                        Log.i("TAG", "initClickMain: ${it.data?.frame}")
                        ConstantsCommon.resetCurrentFrames()
                        ConstantsCommon.currentFrameMain = it.data?.frame
                        downloadDialog?.onDismissDialog(1000L) {
                            try {
                                if (it.data?.frame == null) {
                                    Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT)
                                        .show()
                                    apiViewModel.clearFrame()

                                } else if (it.data?.frame?.editor == MainMenuOptions.BLEND.title) {
                                    Log.i("TAG", "initClickMainelseif: ${it.data?.frame}")
                                } else {
                                    Log.i("TAG", "initClickMainelse: ${it.data?.frame}")
                                }
                            } catch (_: Exception) {
                            }

                            downloadDialog = null
                        }
                    }

                    is com.project.common.repo.api.apollo.helper.Response.Error -> {
                        downloadDialog?.onDismissDialog(1000L) {
                            downloadDialog = null
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }

        firebaseAnalytics?.logEvent(Events.Screens.MAIN, Bundle().apply {
            putString(
                Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SLIDER_MENU
            )
            putString(
                Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED
            )
            putString(
                Events.ParamsKeys.CROSS_PROMO_AD_TITLE, "FloraGarden".replace(".", "_").lowercase()
            )
            putString(
                Events.ParamsKeys.CROSS_PROMO_AD_PLACEMENT, "MainMenu".replace(".", "_").lowercase()
            )
            putString(
                Events.ParamsKeys.CROSS_PROMO_AD_TYPE, "Banner".replace(".", "_").lowercase()
            )
        })


        try {
            application.let {
                runCatching {
                    if (it is MyApp) {
                        it.billing.firebaseAnalytics = firebaseAnalytics
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onCreate: ", ex)
        }

        if (isNetworkAvailable()) {
            ConstantsCommon.updateInternetStatusFrames.postValue(true)
        } else {
            ConstantsCommon.updateInternetStatusFrames.postValue(false)
            searchViewModel.setNetworkState(false)
        }

        initNetworkCallbacks()

        initApiObservers()

        try {
            if (CAN_LOAD_ADS) {

                //loadInterstitial(loadedAction = {}, failedAction = {}, false)

                //  loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                //   MediationTestSuite.launch(this)
                loadNewInterstitialForPro(homeInterstitial()) {}

                loadAppOpen()

                loadRewarded(loadedAction = {}, failedAction = {})

                //loadRewardedInterstitial(loadedAction = {}, failedAction = {})
            }
        } catch (ex: Exception) {
            Log.e("TAG", "onCreate: ", ex)
        }
        hideNavigation()


        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
//                    val intent = result.data
                    kotlin.runCatching {
                        isRecreate = true
                        Log.i("TAG", "onResumesuper: recreate")
                    }
                }
            }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    private fun loadBannerAd(fromButton: Boolean = false) {
        kotlin.runCatching {
            /*   runOnUiThread {
                   if (!showAppOpen && !isProVersion()) {
                       _binding?.let {
                           _binding?.bannerContainer?.visibility = View.VISIBLE
                           onResumeBanner(
                               binding.adBannerContainer,
                               binding.crossBannerIv,
                               binding.bannerLayout.adContainer,
                               binding.bannerLayout.shimmerViewContainer,
                               fromButton = fromButton
                           )
                       }
                   } else {
                       try {
                           if (isProVersion()) {
                               _binding?.bannerContainer?.visibility = View.GONE
                           } else {
                               _binding?.bannerContainer?.visibility = View.INVISIBLE
                           }
                       } catch (ex: java.lang.Exception) {
                           Log.e("error", "onResume: ", ex)
                       }
                   }
               }*/
        }
    }

    fun getActivityLauncher(): ActivityResultLauncher<Intent> {
        return activityLauncher
    }


    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val fromDraft = result.data?.getBooleanExtra("from_draft", false) ?: false
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            val resultBack = true//result.data?.getBooleanExtra("resultBack", false) ?: false
            val where = result.data?.getStringExtra("where") ?: ""
            if (backDecision) {

                kotlin.runCatching {
                    navController?.navigateUp()
                }

            } else {

                if (where.isNotEmpty() && where == "collage" && resultBack) {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            MainMenuOptions.COLLAGE.title, MainMenuOptions.COLLAGE.title
                        )
                    )

                } else if (where.isNotEmpty() && where == "carousal" && resultBack) {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            MainMenuOptions.SEAMLESS.title, MainMenuOptions.SEAMLESS.title
                        )
                    )
                } else if (where.isNotEmpty() && where == "stories" && resultBack) {
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            MainMenuOptions.STORIES.title, MainMenuOptions.STORIES.title
                        )
                    )

                    /* navController?.navigate(
                         MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                             MainMenuOptions.STORIES.title,  MainMenuOptions.STORIES.title
                         )
                     )*/

                } else if (where.isNotEmpty() && where == "overlay" && resultBack) {
                    val overlay = "Overlay Effects".lowercase()
                    navController?.navigate(
                        MainScreenNavigationDirections.actionGlobalNavFramesFragment(
                            overlay,
                            overlay
                        )
                    )
                } else if (where.isNotEmpty() && where == "home") {
                    kotlin.runCatching {
                        navController?.navigateUp()
                    }
                }
            }
        }
    }

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    fun getSplashVisible(): Boolean {
        return try {
            mainViewModel.isSplashVisible
        } catch (ex: java.lang.Exception) {
            true
        }
    }

    fun showHomeScreen() {
        try {
            ConstantsCommon.isSavedScreenHomeClicked = false
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomBar)
            bottomNavigationView?.menu?.getItem(0)?.let { menuItem ->
                navController?.let {
                    if (it.currentDestination?.parent != null)
                        NavigationUI.onNavDestinationSelected(menuItem, it)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun showAppOpenAd() {
        binding.let {
            //it.bannerContainer.invisible()
            showAppOpen = true
            checkFragment(hideViews = true)
            Handler(Looper.getMainLooper()).postDelayed({
                showAppOpen {
                    binding.let {
                        Handler(Looper.getMainLooper()).postDelayed({
                            //binding.bannerContainer.visible()
                            checkFragment(hideViews = false)
                            showAppOpen = false
                            loadBannerAd()
                            loadAppOpen()
                        }, 800L)
                    }
                }
            }, 500L)
        }
    }

    fun goProBottom(show: Boolean) {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            when (val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {
                is HomeFragment -> {
                    if (show) fragment.showGoProBottomRv() else fragment.hideGoProBottomRv()
                }

                is MyWorkFragment -> {
                    if (show) fragment.showGoProBottomRv() else fragment.hideGoProBottomRv()
                }

                else -> {}
            }
        } catch (_: Exception) {
        }
    }

    private fun checkFragment(hideViews: Boolean = false) {
        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            when (val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)) {
                is FramesFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAds()
                }

                is HomeFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAd()
                }

                is HomeForYouFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAd()
                }

                is FeaturedFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAds()
                }

                is MyWorkFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAds()
                }

                is TemplatesFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAds()
                }

                is TemplatesBaseFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAds()
                }

                is StylesFragment -> {
                    if (hideViews) fragment.hideScreenAds() else fragment.showScreenAds()
                }

                else -> {
                    Log.d("AppOpen", "onForegroundEntered: else")
                }
            }
        } catch (_: Exception) {
        }
    }

    private var isRecreate = false
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    fun changeLanguage() {
        try {
            native.nativeAd = null
            val intent = Intent(
                applicationContext,
                com.example.apponboarding.ui.main.activity.LanguageActivitySetting::class.java
            )
            intent.putExtra("from_setting", true)
            resultLauncher?.launch(intent)
        } catch (ex: java.lang.Exception) {
            Log.e("error", ": ", ex)
        }
    }

    private fun initDataOffline() {
//        if (apiViewModel.offlineFeatureScreen.value?.data?.allTags.isNullOrEmpty()) apiViewModel.getFeatureScreen(
//            false
//        )

        homeAndTemplateViewModel.removeFromList()

        if (apiViewModel.offlineStickers.value?.data?.parentCategories.isNullOrEmpty()) apiViewModel.getStickers(
            false
        )

        if (apiViewModel.offlineFilters.value?.data?.parentCategories.isNullOrEmpty()) apiViewModel.getFilters(
            false
        )
    }

    fun refreshTemplateAndFrames() {
        if (ApiConstants.KEY.isNotBlank()) {
            if (apiViewModel.mainScreen.value?.data?.childCategories.isNullOrEmpty()) apiViewModel.getMainScreen()
        }
    }

    private fun initData() {

        if (!isNetworkAvailable) {
            initDataOffline()
            return
        }

        if (ApiConstants.KEY.isNotBlank()) {

            if (apiViewModel.featureScreen.value?.data?.allTags.isNullOrEmpty()) apiViewModel.getFeatureScreen(
                true
            )

            apiViewModel.getSearchTags()

            searchViewModel.setNetworkState(true)

            if (isNetworkAvailable) {
                homeAndTemplateViewModel.getHomeTemplateScreen()
            } else {
                homeAndTemplateViewModel.removeFromList()
            }

            if (apiViewModel.mainScreen.value?.data?.childCategories.isNullOrEmpty()) apiViewModel.getMainScreen()

            if (apiViewModel.stickers.value?.data?.parentCategories.isNullOrEmpty()) apiViewModel.getStickers(
                true
            )

            if (apiViewModel.backgrounds.value?.data?.parentCategories.isNullOrEmpty()) apiViewModel.getBackgrounds()

            if (apiViewModel.filters.value?.data?.parentCategories.isNullOrEmpty()) apiViewModel.getFilters(
                true
            )

            if (apiViewModel.effects.value?.data?.parentCategories.isNullOrEmpty()) apiViewModel.getEffects()
        }
    }

    private fun initApiObservers() {

        try {
            if (apiViewModel.favouriteFrames.hasObservers()) {
                apiViewModel.favouriteFrames.removeObservers(this@MainActivity as LifecycleOwner)
            }

            apiViewModel.favouriteFrames.observeOnce(this) {
                it.let {
                    if (it.isNotEmpty()) {
                        favouriteFrames =
                            it.mapNotNull { FavouriteTypeConverter.fromJson(it.frame) }
                    }
                }
            }

            if (apiViewModel.searchTags.hasObservers()) {
                apiViewModel.searchTags.removeObservers(this@MainActivity as LifecycleOwner)
            }

            apiViewModel.searchTags.observe(this) {
                it.let {
                    lifecycleScope.launch(IO) {
                        searchViewModel.insertDataInTrie(it.data)
                    }
                }
            }

            if (apiViewModel.effects.hasObservers()) {
                apiViewModel.effects.removeObservers(this@MainActivity as LifecycleOwner)
            }

            apiViewModel.effects.observe(this) {
                when (it) {
                    is com.project.common.repo.api.apollo.helper.Response.Success -> {
                        ConstantsCommon.effectList = it.data
                    }

                    is com.project.common.repo.api.apollo.helper.Response.ShowSlowInternet -> {}

                    is com.project.common.repo.api.apollo.helper.Response.Loading -> {
                    }

                    is com.project.common.repo.api.apollo.helper.Response.Error -> {
                    }
                }
            }

            if (apiViewModel.token.hasObservers()) {
                apiViewModel.token.removeObservers(this@MainActivity as LifecycleOwner)
            }

            apiViewModel.token.observe(this) {
                when (it) {
                    is com.project.common.repo.api.apollo.helper.Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is com.project.common.repo.api.apollo.helper.Response.ShowSlowInternet -> {}


                    is com.project.common.repo.api.apollo.helper.Response.Success -> {
                        it.data?.let {
                            ApiConstants.KEY = "JWT $it"

                            lifecycleScope.launch(IO) {

                                if (!isProVersion()) {
                                    kotlin.runCatching {
                                        frameDataStore.readAll()?.collect {
                                            ConstantsCommon.rewardedAssetsList.clear()
                                            ConstantsCommon.rewardedAssetsList.addAll(it)
                                            withContext(Main) {
                                                initData()
                                            }
                                        } ?: run {
                                            withContext(Main) {
                                                initData()
                                            }
                                        }
                                    }.onFailure {
                                        withContext(Main) {
                                            initData()
                                        }
                                    }
                                } else {
                                    withContext(Main) {
                                        initData()
                                    }
                                }
                            }
                        }
                    }

                    is com.project.common.repo.api.apollo.helper.Response.Error -> {
                        Log.d("Fahad", "initApiObservers: Error ")

                        lifecycleScope.launch(IO) {

                            if (!isProVersion()) {
                                kotlin.runCatching {
                                    frameDataStore.readAll()?.collect {
                                        ConstantsCommon.rewardedAssetsList.clear()
                                        ConstantsCommon.rewardedAssetsList.addAll(it)
                                        withContext(Main) {
                                            initData()
                                        }
                                    } ?: run {
                                        withContext(Main) {
                                            initData()
                                        }
                                    }
                                }.onFailure {
                                    withContext(Main) {
                                        initData()
                                    }
                                }
                            } else {
                                withContext(Main) {
                                    initData()
                                }
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.d("Fahad", "initApiObservers: ")
        }

        try {

            if (apiViewModel.mainFromMainScreen.hasObservers()) {
                apiViewModel.mainFromMainScreen.removeObservers(this@MainActivity as LifecycleOwner)
            }

            apiViewModel.mainFromMainScreen.observe(this@MainActivity) {
                when (it) {
                    is com.project.common.repo.api.apollo.helper.Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is com.project.common.repo.api.apollo.helper.Response.ShowSlowInternet -> {

                        Log.i("SLOWINTERNET", "mainFromMainScreen: ShowSlowInternet ")
                        kotlin.runCatching {
                            Toast.makeText(
                                this,
                                it.errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }


                    is com.project.common.repo.api.apollo.helper.Response.Success -> {

                        it.data?.childCategories?.let { mainMenuOptions ->

                            mainMenuOptions.filterNotNull().forEach {
                                val name = it.title
                                it.children?.forEach {
                                    it?.apply {
                                        frames?.let {
                                            when (name.lowercase()) {
                                                MainMenuOptions.DRAWING.title.lowercase() -> {
                                                    ConstantsCommon.drawingFramesSubData?.set(
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

                    is com.project.common.repo.api.apollo.helper.Response.Error -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("Fahad", "initApiObservers: ")
        }
    }

    private fun setupSmoothBottomMenu() {
        try {
            navController?.let {
                _binding?.bottomBar?.setupWithNavController(it)
            }
            var lastSelected: Int? = null
            _binding?.addIcon?.setSingleClickListener {

            }
            fun handleMenuItemSelection(showAd: Boolean, itemId: Int) {
                try {
                    if (lastSelected != null && lastSelected != itemId) {
                        lastSelected = itemId
                        if (itemId == R.id.nav_mywork || itemId == R.id.nav_home) {
                            Log.i("ISWORKID", "handleMenuItemSelection:yes ")
                            showNewInterstitial(homeInterstitial()) {
                                loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                            }
                        }
                        loadBannerAd()
                    } else if (lastSelected == null) {
                        lastSelected = itemId
                        if (itemId == R.id.nav_mywork || itemId == R.id.nav_home) {
                            Log.i("ISWORKID", "handleMenuItemSelection:yes-B ")
                            showNewInterstitial(homeInterstitial()) {
                                loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                            }
                        }
                        loadBannerAd()
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e("error", "handleMenuItemSelection: ", ex)
                }
            }

            kotlin.runCatching {
                _binding?.bottomBar?.menu?.children?.forEach {
                    it.setOnMenuItemClickListener {
                        updateBottomBarBackground(it.itemId)
                        when (it.itemId) {
                            R.id.nav_featured -> {
                                handleMenuItemSelection(
                                    featureMenuShowAd,
                                    it.itemId
                                )
                            }

                            R.id.nav_templates_base -> handleMenuItemSelection(
                                templatesMenuShowAd,
                                it.itemId
                            )

                            R.id.nav_home -> {
                                handleMenuItemSelection(homeMenuShowAd, it.itemId)
                            }

                            R.id.nav_styles -> handleMenuItemSelection(styleMenuShowAd, it.itemId)
                            R.id.nav_mywork -> handleMenuItemSelection(myWorkMenuShowAd, it.itemId)
                        }
                        false
                    }
                }
            }

            /*    if (!showHomeScreen) {
                    kotlin.runCatching {
                        _binding?.bottomBar?.menu?.findItem(R.id.nav_featured)?.let { menuItem ->
                            navController?.let {
    //                        Log.i("TAG", "setupSmoothBottomMenu: ${it.currentDestination?.parent}")
                                if (it.currentDestination?.parent != null) {
                                    showHomeScreen = true
                                    NavigationUI.onNavDestinationSelected(menuItem, it)
                                }
                            }
                        }
                    }
                }*/

            if (receivedData != null) {
                if (receivedData.equals("ai_edit")) {
                    kotlin.runCatching {
                        _binding?.bottomBar?.menu?.findItem(R.id.nav_home)?.let { menuItem ->
                            navController?.let {
//                        Log.i("TAG", "setupSmoothBottomMenu: ${it.currentDestination?.parent}")
                                if (it.currentDestination?.parent != null) {
                                    showHomeScreen = true
                                    NavigationUI.onNavDestinationSelected(menuItem, it)
                                }
                            }
                        }
                    }

                } else if (receivedData.equals("uninstall")) {
                    runCatching {
                        receivedData = ""
//                        startActivity(Intent(this@MainActivity, UnInStallActivity::class.java))
//                        finish()
                    }
                } else {
                    if (!showHomeScreen) {
                        kotlin.runCatching {
                            _binding?.bottomBar?.menu?.findItem(R.id.nav_featured)
                                ?.let { menuItem ->
                                    navController?.let {
//                                   Log.i("TAG", "setupSmoothBottomMenu: ${it.currentDestination?.parent}")
                                        if (it.currentDestination?.parent != null) {
                                            showHomeScreen = true
                                            NavigationUI.onNavDestinationSelected(menuItem, it)
                                        }
                                    }
                                }
                        }
                    }
                }
            } else if (receivedData1 != null) {
                if (receivedData1.equals("ai_edit")) {
                    kotlin.runCatching {
                        _binding?.bottomBar?.menu?.findItem(R.id.nav_home)?.let { menuItem ->
                            navController?.let {
//                        Log.i("TAG", "setupSmoothBottomMenu: ${it.currentDestination?.parent}")
                                if (it.currentDestination?.parent != null) {
                                    showHomeScreen = true
                                    NavigationUI.onNavDestinationSelected(menuItem, it)
                                }
                            }
                        }
                    }

                } else {
                    if (!showHomeScreen) {
                        kotlin.runCatching {
                            _binding?.bottomBar?.menu?.findItem(R.id.nav_featured)
                                ?.let { menuItem ->
                                    navController?.let {
//                                   Log.i("TAG", "setupSmoothBottomMenu: ${it.currentDestination?.parent}")
                                        if (it.currentDestination?.parent != null) {
                                            showHomeScreen = true
                                            NavigationUI.onNavDestinationSelected(menuItem, it)
                                        }
                                    }
                                }
                        }
                    }
                }
            } else {

                if (!showHomeScreen) {
                    kotlin.runCatching {
                        _binding?.bottomBar?.menu?.findItem(R.id.nav_featured)?.let { menuItem ->
                            navController?.let {
//                              Log.i("TAG", "setupSmoothBottomMenu: ${it.currentDestination?.parent}")
                                if (it.currentDestination?.parent != null) {
                                    showHomeScreen = true
                                    NavigationUI.onNavDestinationSelected(menuItem, it)
                                }
                            }
                        }
                    }
                }
            }


        } catch (ex: java.lang.Exception) {
            Log.e("error", "setupSmoothBottomMenu: ", ex)
        }
    }

    private fun updateBottomBarBackground(itemId: Int) {
        //TODO LATER
        /*_binding?.bottomBar?.apply {
            when (itemId) {
                R.id.nav_home -> {
                    setBackgroundResource(com.project.frame_placer.R.drawable.bg_nav_homn) // Gradient background
                }

                R.id.nav_mywork -> {
                    setBackgroundResource(com.project.frame_placer.R.drawable.bg_navi) // Solid background
                }

                else -> {
                    setBackgroundResource(com.project.frame_placer.R.drawable.bg_nav_homn) // Default gradient
                }
            }
        }*/
    }

    override fun onResume() {
        super.onResume()
        if (!isRecreate) {

            Log.i("TAG", "onResumesuper: onResume")

            loadBannerAd()

        } else {
            recreate()
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseBanner()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp() == true || super.onSupportNavigateUp()
    }

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var internetConnectivityReceiver: InternetConnectivityReceiver? = null
    private var onCreateInitialize = false
    override fun onStart() {
        super.onStart()
        initNetworkCallbacks()
    }

    override fun onStop() {
        super.onStop()
        Log.i("TAG", "onStop: mainActivity OnStop")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
            connectivityManager = null
            networkCallback = null
        } else {
            unregisterReceiver(internetConnectivityReceiver)
            internetConnectivityReceiver = null
        }
    }

    private fun initNetworkCallbacks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectivityManager == null && networkCallback == null) {
                connectivityManager =
                    getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                networkCallback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        if (!isProVersion() && crossPromoViewModel.crossPromoAds.value?.data == null) {
                            crossPromoViewModel.getCrossPromoAds(packageName)
                        }
                        loadBannerAd()
                        when (apiViewModel.token.value) {
                            is com.project.common.repo.api.apollo.helper.Response.Success -> {
                                apiViewModel.token.value?.data?.let {
                                    ApiConstants.KEY = "JWT ${it}"
                                }
                                runOnUiThread {
                                    isNetworkAvailable = true
                                    initData()
                                }
                            }

                            else -> {
                                apiViewModel.getAuthToken(true)
                            }
                        }
                        runOnUiThread {
                            isNetworkAvailable = true
//                            ConstantsCommon.updateInternetStatusFeature.postValue(true)
                            ConstantsCommon.updateInternetStatusFrames.postValue(true)
                        }
                    }

                    override fun onLost(network: Network) {
                        runOnUiThread {
                            isNetworkAvailable = false
//                            ConstantsCommon.updateInternetStatusFeature.postValue(false)
                            ConstantsCommon.updateInternetStatusFrames.postValue(false)
                            searchViewModel.setNetworkState(false)
                            initDataOffline()
                        }
                    }
                }

                val networkRequest =
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

                networkCallback?.let {
                    connectivityManager?.registerNetworkCallback(networkRequest, it)
                }

                if (onCreateInitialize) {
                    isNetworkAvailable = isNetworkAvailable()
                    if (!isNetworkAvailable) {
                        isNetworkAvailable = false
//                        ConstantsCommon.updateInternetStatusFeature.postValue(false)
                        ConstantsCommon.updateInternetStatusFrames.postValue(false)
                        searchViewModel.setNetworkState(false)
                        initDataOffline()
                    }
                }
            }

            onCreateInitialize = true

        } else {
            if (internetConnectivityReceiver == null) {
                internetConnectivityReceiver = InternetConnectivityReceiver()
                internetConnectivityReceiver?.setConnectivityListener(this@MainActivity)
                val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                registerReceiver(internetConnectivityReceiver, intentFilter)
            }
        }
    }

    override fun onConnectivityChanged(isConnected: Boolean) {
        isNetworkAvailable = isConnected
//        ConstantsCommon.updateInternetStatusFeature.postValue(isConnected)
        ConstantsCommon.updateInternetStatusFrames.postValue(isConnected)
        if (!isConnected) {
            searchViewModel.setNetworkState(false)
        }
        if (isConnected) {
            loadBannerAd()
            if (!isProVersion() && crossPromoViewModel.crossPromoAds.value?.data == null) {
                crossPromoViewModel.getCrossPromoAds(packageName)
            }

            when (apiViewModel.token.value) {
                is com.project.common.repo.api.apollo.helper.Response.Success -> {
                    runOnUiThread {
                        isNetworkAvailable = true
                        initData()
                    }
                }

                else -> {
                    apiViewModel.getAuthToken(true)
                }
            }
        } else {
            Log.i("TAG", "onConnectivityChanged: initDataOffline")
            initDataOffline()
        }
    }

    fun navigate(directions: NavDirections, currentId: Int) {

//        try {
        if (findNavController(binding.navHostFragment.id).currentDestination?.id == currentId) {
            findNavController(binding.navHostFragment.id).navigate(directions)
        }
//        } catch (ex: Exception) {
//            Log.e("error", "navigate: ", ex)
//        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
    else arrayOf(

        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    var downloadDialog: BottomSheetDialog? = null

    fun eventForFrameClick(frameBody: FrameObject) {

        parentScreen = frameBody.categoryName

        val bundle = Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
            putString(Events.ParamsKeys.FROM, frameBody.from)

            if (frameBody.categoryName.isNotBlank())
                putString(Events.ParamsKeys.CATNAME, frameBody.categoryName)

            if (frameBody.subCategoryName.isNotBlank())
                putString(Events.ParamsKeys.SUB_SCREEN, frameBody.subCategoryName)

            putString(Events.ParamsKeys.FRAME_ID, frameBody.id.toString())
            putString(Events.ParamsKeys.FRAME_NAME, frameBody.name)
        }

        firebaseAnalytics?.logEvent(frameBody.screenName, bundle)

        Log.i(
            "firebase_events_clicks",
            "events: screenName: ${frameBody.screenName} bundle:  $bundle"
        )
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


    private fun openTemplates(categoryName: String, itemId: Int) {

        navController?.navigate(
            MainScreenNavigationDirections.actionGlobalNavTemplatesFragment(
                categoryName.lowercase(),
                itemId,
                fromHome = false
            )
        )

    }

    fun frameClickFrames(frameBody: FrameObject, updateRecycler: () -> Unit) {
        if (!isNetworkAvailable) {
            kotlin.runCatching {
                Toast.makeText(
                    this,
                    "Please connect to internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        kotlin.runCatching {
            Log.d("WHOLEOBJ", "frameClick:${frameBody.subCategoryName},${frameBody.id} ")
            eventForFrameClick(frameBody)
            openTemplates(frameBody.subCategoryName, frameBody.id)

        }
    }


    fun frameClick(frameBody: FrameObject, updateRecycler: () -> Unit) {

        if (!isNetworkAvailable) {
            kotlin.runCatching {
                Toast.makeText(
                    this,
                    "Please connect to internet",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }

        kotlin.runCatching {
            Log.d("WHOLEOBJ", "frameClick:${frameBody.subCategoryName},${frameBody.id} ")


            checkAndRequestPermissions(*permissions, action = {

                eventForFrameClick(frameBody)

                if (!isProVersion() && frameBody.tags?.isNotEmpty() == true && frameBody.tags != "Free" && !ConstantsCommon.rewardedAssetsList.contains(
                        frameBody.id
                    )
                ) {
                    createProFramesDialog(
                        frameBody.rewardedAd,
                        thumb = "${frameBody.baseUrl}${frameBody.thumb}",
                        thumbType = ContextCompat.getDrawable(
                            this, when (frameBody.thumbtype.lowercase()) {
                                FrameThumbType.PORTRAIT.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_portrait
                                FrameThumbType.LANDSCAPE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_landscape
                                FrameThumbType.SQUARE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_squre
                                else -> com.project.common.R.drawable.frame_placeholder_portrait
                            }
                        ),
                        action = {
                            downloadDialog = createDownloadingDialog(
                                frameBody.baseUrl, frameBody.thumb, frameBody.thumbtype
                            )

                            showRewardedInterstitial(
                                frameBody.rewardedAd,
                                loadedAction = {
                                    apiViewModel.addToRecent(RecentsModel(frame = frameBody.frameBody))
                                    lifecycleScope.launch(IO) {
                                        frameDataStore.writeUnlockedId(frameBody.id)
                                        ConstantsCommon.rewardedAssetsList.add(frameBody.id)
                                        withContext(Main) {
                                            updateRecycler.invoke()
                                            apiViewModel.getFrame(frameBody.id)
                                        }
                                    }

                                },
                                failedAction = {
                                    downloadDialog?.apply { if (isShowing) dismiss() }
                                })
                        },
                        goProAction = {
                            try {
                                startActivity(
                                    Intent().apply {
                                        setClassName(
                                            applicationContext,
                                            getProScreen()
                                        )
                                    }
                                )
                            } catch (ex: Exception) {
                                Log.e("error", "onCreate: ", ex)
                            }
                        },
                        dismissAction = {},
                        frameBody.tags.lowercase() == "paid"
                    )
                } else {

                    apiViewModel.addToRecent(RecentsModel(frame = frameBody.frameBody))
                    downloadDialog = createDownloadingDialog(
                        frameBody.baseUrl, frameBody.thumb, frameBody.thumbtype
                    )

                    showNewInterstitial(homeInterstitial()) {
                        loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}
                        apiViewModel.getFrame(frameBody.id)
                    }

                }
            }, declineAction = {

            })
        }
    }


}

data class FrameObject(
    var id: Int = 0,
    var name: String = "",
    var screenName: String = "",
    var subCategoryName: String = "",
    var categoryName: String = "",
    var tags: String = "",
    var baseUrl: String = "",
    var thumb: String = "",
    var thumbtype: String = "",
    var interstitialAd: Boolean = true,
    var rewardedAd: Boolean = false,
    var frameBody: Any,
    var from: String = "",
)
