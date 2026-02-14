package com.fahad.newtruelovebyfahad.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.ads.Constants.ADS_SDK_INITIALIZE
import com.example.ads.Constants.InterstitialUnInstall
import com.example.ads.Constants.adGalleryNative
import com.example.ads.Constants.loadNativeSplash
import com.example.ads.Constants.adGalleryNativeFloor
import com.example.ads.Constants.allBannerReloadLimit
import com.example.ads.Constants.enableHomeInterAd
import com.example.ads.Constants.flowUninstall
import com.example.ads.Constants.galleryButtonNewFlow
import com.example.ads.Constants.homeNewInterStrategy
import com.example.ads.Constants.interstitialHomeAfterStartCount
import com.example.ads.Constants.interstitialHomeAlwaysShow
import com.example.ads.Constants.interstitialHomeFloor
import com.example.ads.Constants.interstitialHomeStartCount
import com.example.ads.Constants.interstitialNew
import com.example.ads.Constants.introScreen
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.lfoOneNativeId
import com.example.ads.Constants.lfoTwoNativeId
import com.example.ads.Constants.loadBannerOnBoardFour
import com.example.ads.Constants.loadBannerOnBoardMedium
import com.example.ads.Constants.loadBannerOnBoardOne
import com.example.ads.Constants.loadBannerOnBoardThree
import com.example.ads.Constants.loadBannerOnBoardTwo
import com.example.ads.Constants.loadInterstitialSave
import com.example.ads.Constants.loadInterstitialSplash
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeLfOne
import com.example.ads.Constants.loadNativeLfTwo
import com.example.ads.Constants.loadNativeObFour
import com.example.ads.Constants.loadNativeObOne
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.Constants.loadNativeObTwo
import com.example.ads.Constants.loadNativeOld
import com.example.ads.Constants.loadNativeOnResume
import com.example.ads.Constants.loadSplashAppOpen
import com.example.ads.Constants.nativeReasonUninstall
import com.example.ads.Constants.newAdsConfig
import com.example.ads.Constants.onBoardingFourNativeId
import com.example.ads.Constants.onBoardingOneNativeId
import com.example.ads.Constants.onBoardingThreeNativeId
import com.example.ads.Constants.onBoardingTwoNativeId
import com.example.ads.Constants.openScreenIntroduction
import com.example.ads.Constants.openTutorial
import com.example.ads.Constants.popupEventValentine
import com.example.ads.Constants.proCounter
import com.example.ads.Constants.proSplashOrHome
import com.example.ads.Constants.questionScreenEnable
import com.example.ads.Constants.showAllAppOpenAd
import com.example.ads.Constants.showBlendGuideScreen
import com.example.ads.Constants.splashInterAdId
import com.example.ads.Constants.splashTime
import com.example.ads.Constants.splashTimeOut
import com.example.ads.Constants.surveyScreenEnable
import com.example.ads.Constants.tutorialScr
import com.example.ads.admobs.scripts.InterstitialNew
import com.example.ads.admobs.utils.MobileAds
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadAppOpenSplash
import com.example.ads.admobs.utils.onPauseSplashBanner
import com.example.ads.admobs.utils.preLoadNative
import com.example.ads.admobs.utils.setOnClick
import com.example.ads.admobs.utils.showAppOpenSplash
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.model.AdConfigModel
import com.example.ads.utils.fullNativeOne
import com.example.ads.utils.fullNativeTwo
import com.example.ads.utils.nativeLanguageOne
import com.example.ads.utils.nativeLanguageTwo
import com.example.ads.utils.nativeSplash
import com.example.ads.utils.onBoardNativeOne
import com.example.ads.utils.onBoardNativeThree
import com.example.ads.utils.onBoardNativeTwo
import com.example.ads.utils.question
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.apponboarding.ui.main.viewModels.LanguageViewModel
import com.example.inapp.helpers.Constants.SINGULAR_API_KEY
import com.example.inapp.helpers.Constants.SINGULAR_SECRET
import com.example.inapp.helpers.Constants.isProVersion
import com.example.questions_intro.ui.activity.BlendOnBoardingActivity
import com.example.questions_intro.ui.activity.QuestionsActivity
import com.fahad.newtruelovebyfahad.BuildConfig
import com.fahad.newtruelovebyfahad.databinding.ActivitySplashBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.google.firebase.messaging.FirebaseMessaging
import com.project.common.remote_config.RemoteConfigViewModel
import com.project.common.repo.api.apollo.helper.ApiConstants
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.utils.Constants.isProWithInterOn
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.introOnBoardingCompleted
import com.project.common.utils.ConstantsCommon.receivedData
import com.project.common.utils.ConstantsCommon.showQuestionScreenTimeCheck
import com.project.common.utils.ConstantsCommon.surveyCompleted
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.viewmodels.ApiViewModel
import com.project.common.viewmodels.DataStoreViewModel
import com.project.common.viewmodels.HomeAndTemplateViewModel
import com.singular.sdk.Singular
import com.singular.sdk.SingularConfig
import com.xan.event_notifications.data.constants.Constants.notiLockScreen
import com.xan.event_notifications.data.constants.Constants.notiLockscreenCountry
import com.xan.event_notifications.data.constants.Constants.notiStatusBarCountry
import com.xan.event_notifications.data.constants.Constants.timePushNotiLockscreen1
import com.xan.event_notifications.data.constants.Constants.timePushNotiLockscreen2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumMap
import kotlin.collections.set

@SuppressLint("CustomSplashScreen")
const val TAG = "SplashActivity"

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private var _binding: ActivitySplashBinding? = null
    private val apiViewModel by viewModels<ApiViewModel>()
    private val homeAndTemplateViewModel by viewModels<HomeAndTemplateViewModel>()
    private val binding get() = _binding!!
    private var splashTimerJob: Job? = null
    private var isConsentFormCompleted = false
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private var showAppOpen = false
    private val languageViewModel by viewModels<LanguageViewModel>()
    private val remoteConfigModel by viewModels<RemoteConfigViewModel>()

    private fun initUninstallIntent() {
        receivedData = intent.getStringExtra("shortcut_extra_key")
        if (receivedData != null) {
            Log.d(TAG, "initUninstallIntent :Received from shortcut: $receivedData")
        } else {
            Log.d(TAG, "initUninstallIntent: receivedData is null")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideNavigation()
        runCatching {
            animationStopListener()
            nextWork()
        }
    }

    private fun nextWork() {

        kotlin.runCatching {

            languageViewModel.selectedLanguage.observe(this@SplashActivity) { selectedLanguage ->
                selectedLanguage?.let {
                    Log.d(TAG, "onCreate: selectedLanguage $selectedLanguage")
                    Log.d(
                        TAG,
                        "onCreate: selectedLanguage.languageCode ${selectedLanguage.languageCode}"
                    )
                    languageCode = selectedLanguage.languageCode
                    setLocale(languageCode)
                }
            }
        }
        initUninstallIntent()
        ConstantsCommon.isNetworkAvailable = isNetworkAvailable()
        ADS_SDK_INITIALIZE.set(false)
//        ADS_SDK_INITIALIZE_BIGO.set(false)
        interstitialNew = InterstitialNew()

        try {
            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@SplashActivity as LifecycleOwner)
            }

            isProVersion.observe(this@SplashActivity) {
                if (it) {
                    _binding?.nativeContainer?.hide()
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }

        initConsentForum()
        firebaseAnalytics?.logEvent(Events.Screens.SPLASH, Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
        })
    }

    private fun animationStopListener() {
      /*  _binding?.appIconAnim?.let { lottieV ->
            // Add a listener to detect when the animation ends
            lottieV.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    _binding?.animView?.isVisible = true
                    _binding?.animView?.playAnimation()
                }
            })

        }*/

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseSplashBanner()
    }

    private fun initConsentForum() {
        try {
            MobileAds().apply {
                checkAdmobConsent(this@SplashActivity, onCompletion = {
                    if (!isConsentFormCompleted) {
                        isConsentFormCompleted = true

                        initSingularSDK()
                        Log.i(TAG, "initConsentForum: firstCall")
                        if (!showAppOpen && !isProVersion() && isNetworkAvailable()) {//loadNativeSplash &&
                            loadAndShowNativeOnBoarding(
                                loadedAction = {
                                    kotlin.runCatching {
                                        if (!isFinishing && !isDestroyed && _binding != null) {
                                            binding.mediumNativeLayout.adContainer.show()
                                            binding.mediumNativeLayout.shimmerViewContainer.visibility =
                                                View.INVISIBLE
                                            binding.mediumNativeLayout.adContainer.removeAllViews()
                                            if (it?.parent != null) {
                                                (it.parent as ViewGroup).removeView(it)
                                            }
                                            if (!isFinishing && !isDestroyed && _binding != null) {
                                                Log.d(
                                                    "ActivityState",
                                                    "isFinishing 0 OB: $isFinishing, isDestroyed: $isDestroyed"
                                                )
                                                binding.mediumNativeLayout.adContainer.addView(it)
                                            }
                                        }
                                    }
                                },
                                failedAction = {
                                    if (!isFinishing && !isDestroyed && _binding != null) {
                                        binding.nativeContainer.hide()
                                        binding.mediumNativeLayout.shimmerViewContainer.visibility = View.INVISIBLE
                                    }
                                },
                                nativeSplash(),
                                adImpression = {
                                },
                            )
                        } else {
                            if (!isFinishing && !isDestroyed && _binding != null) {
                                binding.nativeContainer.hide()
                            }
                        }

                        remoteConfigModel.getRemoteConfigSplash(this@SplashActivity)
                        remoteConfigModel.adConfig.observe(this@SplashActivity) { newConfig ->
                            newConfig?.let {
                                newAdsConfig = it
                                remoteConfig(it)
                                lifecycleScope.launch(Main) {
                                    /*kotlin.runCatching {
                                        application?.let {
                                            if (it is MyApp) {
                                                it.initNotificationHelper()
                                            }
                                        }
                                    }*/


//                                    if (!loadSplashAppOpen) {
//                                        if (loadInterstitialSplash) {
//                                            loadNewInterstitialForPro(splashInterstitial()) {}
//
//                                            /* splashInterstitial()?.let { config ->
//                                                 loadInterstitial({}, {}, true, config = config)
//                                             }*/
//                                        }
//                                    } else {
                                    if(loadSplashAppOpen) {
                                        loadAppOpenSplash(true)
                                    }
                                  //  }

                                    if (!isProVersion() && !showAppOpen) {
                                        if (loadNativeSplash) {
                                            _binding?.nativeContainer?.show()
                                        } else {
                                            _binding?.nativeContainer?.hide()
                                        }
                                    }

                                    kotlin.runCatching {
                                        dataStoreViewModel.questionScreenCompleted.observeOnce(this@SplashActivity) {
                                            showQuestionScreenTimeCheck = !it
                                            lifecycleScope.launch(if (it) Main else IO) {
                                                if (!it) {
                                                    dataStoreViewModel.readCurrentTime()
                                                        .let { completedTimeInterval ->
                                                            showQuestionScreenTimeCheck =
                                                                completedTimeInterval && questionScreenEnable
                                                        }
                                                }

                                                withContext(Main) {
                                                    if (showQuestionScreenTimeCheck) {
                                                        preLoadNative(question())
                                                    }
                                                }
                                            }
                                        }
                                    }


                                }
                                apiViewModel.getAuthToken(isNetworkAvailable())

                                dataStoreViewModel.introComplete.observeOnce(this@SplashActivity) { it ->
                                    Log.i(TAG, "initConsentForum: introComplete")
                                    if (!it) {
                                        preLoadNative(getNextConfig())
                                        initObserver()
                                    } else {
                                        initObserver()
                                    }
                                }

                            }

                        }


                        /*  initObserver()}*/
                        val consentMap: MutableMap<ConsentType, FirebaseAnalytics.ConsentStatus> =
                            EnumMap(ConsentType::class.java)
                        consentMap[ConsentType.ANALYTICS_STORAGE] =
                            FirebaseAnalytics.ConsentStatus.GRANTED
                        consentMap[ConsentType.AD_STORAGE] = FirebaseAnalytics.ConsentStatus.GRANTED
                        consentMap[ConsentType.AD_USER_DATA] =
                            FirebaseAnalytics.ConsentStatus.GRANTED
                        consentMap[ConsentType.AD_PERSONALIZATION] =
                            FirebaseAnalytics.ConsentStatus.GRANTED
                        firebaseAnalytics?.setConsent(consentMap)

                    }
                }, consentCompletionCallback = {})
            }
        } catch (ex: Exception) {
            Log.e("TAG", "onCreate: ", ex)
        }
    }

    private fun getNextConfig(): AdConfigModel? {

        return if (loadNativeLfOne) {
            nativeLanguageOne()
        } else if (loadNativeLfTwo) {
            nativeLanguageTwo()
        } else if (loadNativeObOne) {
            onBoardNativeOne()
        } else if (loadNativeFullOne) {
            fullNativeOne()
        } else if (loadNativeObTwo) {
            onBoardNativeTwo()
        } else if (loadNativeFullTwo) {
            fullNativeTwo()
        } else if (loadNativeObThree) {
            onBoardNativeThree()
        } else {
            null
        }
    }

    private fun initObserver() {

        if (isNetworkAvailable()) {
            apiViewModel.token.observe(this) {
                when (it) {
                    is Response.Loading -> {}
                    is Response.ShowSlowInternet -> {}
                    is Response.Success -> {
                        Log.i("TAG", "initData: success")
                        Log.i(TAG, "initConsentForum: success")
                        it.data?.let {
                            ApiConstants.KEY = "JWT $it"
                            if (!initDataInit) {
                                initDataInit = true
                                initData()
                            }
                        }
                    }
                    is Response.Error -> {
                        Log.i("TAG", "initData: error")
                        if (!initDataInit) {
                            initDataInit = true
                            initData()
                        }
                    }
                }
            }
        } else {
            initData()
        }
    }

    private var initDataInit = false

    private fun initSingularSDK() {
        kotlin.runCatching {
            val config: SingularConfig =
                SingularConfig(SINGULAR_API_KEY, SINGULAR_SECRET).withSingularLink(
                    intent
                ) { }.withLoggingEnabled().withLogLevel(1)
            config.withFacebookAppId(getString(com.example.ads.R.string.facebook_id))

            val initialized = Singular.init(this, config)
            if (initialized) {
                fetchFirebaseToken()
            }
            Log.d("Singular: ", "Singular Initialized")
        }
    }

    private fun fetchFirebaseToken() {
        kotlin.runCatching {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FirebaseToken", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                try {
                    // Get new FCM registration token
                    val token = task.result
                    Log.d("FirebaseToken", token)
                    /* send Firebase Event to Singular */
                    Singular.setFCMDeviceToken(token)
                } catch (e: Exception) {
                    Log.e("error", "fetchFirebaseToken: ", e)
                }
            })
        }
    }

    private fun initData() {
        if (isNetworkAvailable()) {
            homeAndTemplateViewModel.onlyCallToEndPointHomeAndTemplateScreen()
            ConstantsCommon.updateInternetStatusFrames.postValue(true)
        } else {

            ConstantsCommon.updateInternetStatusFrames.postValue(false)
        }

        Log.i("TAG", "initData: initData")

        initTimer(splashTime)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        _binding?.apply {
            kotlin.runCatching {
                root.setBackgroundColor(
                    ContextCompat.getColor(
                        this@SplashActivity,
                        com.project.common.R.color.container_clr_activity
                    )
                )

            }
        }
    }

    private fun initTimer(waitTime: Long = 2000) {
        splashTimerJob = lifecycleScope.launch(IO) {
            //delay(waitTime)
            withContext(Main) {
                lifecycleScope.launch {
                    delay(waitTime)
                    navigateToAfterNative()
                }
                Log.i("TAG", "initData: initTimer")


            }
        }
    }

    private fun navigateToAfterNative() {
        runCatching {
            val myCallback: () -> Unit = {
                Log.i("TAG", "initData: initCallback")
                initIntro()
            }

            if (BuildConfig.DEBUG){
                myCallback.invoke()
                return@runCatching
            }

//            if (loadInterstitialSplash && !loadSplashAppOpen) {
//                showNewInterstitialPro(splashInterstitial()) {
//                    myCallback.invoke()
//                }
//            } else {
                if(loadSplashAppOpen) {
                    _binding?.nativeContainer?.visibility = View.INVISIBLE
                    showAppOpenSplash {
                        myCallback.invoke()
                    }
                }
           // }
        }
    }

    private var lastSelected: ImageView? = null

    private fun initIntro() {

        kotlin.runCatching {
            dataStoreViewModel.blendObGuideCompleted.observeOnce(this) {
                lifecycleScope.launch(Main) {
                    showBlendGuideScreen = !it
                    if (showBlendGuideScreen) {
                        showBlendGuideScreen = tutorialScr
                    }
                    if (showBlendGuideScreen && openTutorial) {
                        lifecycleScope.launch(Main) {
                            //  loadAppOpenBlendGuide()
                        }
                    }

                    dataStoreViewModel.introComplete.observeOnce(this@SplashActivity) { intro ->
                        dataStoreViewModel.questionScreenCompleted.observeOnce(this@SplashActivity) {
                            showQuestionScreenTimeCheck = !it
                            lifecycleScope.launch(if (it) Main else IO) {
                                if (!it) {
                                    dataStoreViewModel.readCurrentTime()
                                        .let { completedTimeInterval ->
                                            showQuestionScreenTimeCheck =
                                                completedTimeInterval && questionScreenEnable
                                        }
                                }
                                withContext(Main) {
                                    dataStoreViewModel.introCounter.observeOnce(this@SplashActivity) { counter ->

                                        kotlin.runCatching {

                                            if (!intro && !introScreen) {
                                                dataStoreViewModel.updateIntroComplete()
                                            }

                                            introOnBoardingCompleted = intro

                                            dataStoreViewModel.surveyComplete.observeOnce(this@SplashActivity) { survey ->

                                                lifecycleScope.launch(Main) {

                                                    surveyCompleted = survey

                                                    if (!intro && introScreen) {
                                                            if(!proSplashOrHome){
                                                                kotlin.runCatching {
                                                                val intent = Intent(
                                                                    applicationContext,
                                                                    com.example.apponboarding.ui.main.activity.LanguageActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                                finish()
                                                            }
                                                                //
                                                            }else if(proSplashOrHome&& !isProVersion()){
                                                                kotlin.runCatching {
                                                                    sendEvent(true)
                                                                    val intent = Intent()
                                                                    intent.setClassName(
                                                                        applicationContext,
                                                                        getProScreen()
                                                                    )
                                                                    intent.putExtra("show_ad", true)
                                                                    intent.putExtra("is_intro_complete", !intro )
                                                                    kotlin.runCatching {
                                                                        this@SplashActivity.setLocale(languageCode)
                                                                    }
                                                                    startActivity(intent)
                                                                    finish()
                                                                }
                                                            }else{
                                                                kotlin.runCatching {
                                                                    val intent = Intent(
                                                                        applicationContext,
                                                                        com.example.apponboarding.ui.main.activity.LanguageActivity::class.java
                                                                    )
                                                                    startActivity(intent)
                                                                    finish()
                                                                }
                                                            }


                                                    } else if (!surveyCompleted && surveyScreenEnable) {
                                                        kotlin.runCatching {
                                                            val intent = Intent()
                                                            intent.setClassName(
                                                                applicationContext,
                                                                "com.example.questions_intro.ui.activity.SurveyActivity"
                                                            )
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    } else if (showQuestionScreenTimeCheck) {
                                                        kotlin.runCatching {
                                                            val intent = Intent(
                                                                applicationContext,
                                                                QuestionsActivity::class.java
                                                            )
                                                            startActivity(intent)
                                                            overridePendingTransition(0, 0)
                                                            finish()
                                                        }
                                                    } else if (openScreenIntroduction) {
                                                        kotlin.runCatching {
                                                            if (receivedData != null) {
                                                                val intent = Intent(
                                                                    applicationContext,
                                                                    MainActivity::class.java
                                                                )
                                                                intent.putExtra(
                                                                    "shortcut_extra_key",
                                                                    receivedData
                                                                )
                                                                startActivity(intent)
                                                                overridePendingTransition(0, 0)
                                                                //  completeFOFlow()
                                                                finish()
                                                            } else {
                                                                val intent = Intent(
                                                                    applicationContext,
                                                                    MainActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                                overridePendingTransition(0, 0)
                                                                finish()
//                                                                val intent = Intent(
//                                                                    applicationContext,
//                                                                    IntroActivity::class.java
//                                                                )
//                                                                intent.putExtra(
//                                                                    "shortcut_extra_key",
//                                                                    receivedData
//                                                                )
//                                                                intent.putExtra("screen", counter)
//                                                                startActivity(intent)
//                                                                overridePendingTransition(0, 0)
//                                                                finish()
                                                            }
                                                        }
                                                    } else if (showBlendGuideScreen) {
                                                        kotlin.runCatching {
                                                            val intent = Intent(
                                                                this@SplashActivity,
                                                                BlendOnBoardingActivity::class.java
                                                            )
                                                            startActivity(intent)
                                                            overridePendingTransition(0, 0)
                                                            finish()
                                                        }
                                                    } else if (isProVersion()) {
                                                        kotlin.runCatching {
                                                            val intent = Intent(
                                                                applicationContext,
                                                                MainActivity::class.java
                                                            )
                                                            startActivity(intent)
                                                            finish()
                                                        }
                                                    } else {
                                                        dataStoreViewModel.appSessions.observeOnce(
                                                            this@SplashActivity
                                                        ) {
                                                            it.let {
                                                                Log.d(
                                                                    "FAHAD",
                                                                    "onCreate: observeTwice $it"
                                                                )
                                                                if (it > 0 && !isProVersion()) {
                                                                    runCatching {
                                                                        if (proSplashOrHome&& !isProVersion()) {
                                                                            kotlin.runCatching {
                                                                                sendEvent(true)
                                                                                val intent = Intent()
                                                                                intent.setClassName(
                                                                                    applicationContext,
                                                                                    getProScreen()
                                                                                )
                                                                                intent.putExtra("show_ad", true)
                                                                                intent.putExtra("is_intro_complete", !intro )
                                                                                kotlin.runCatching {
                                                                                    this@SplashActivity.setLocale(languageCode)
                                                                                }
                                                                                startActivity(intent)
                                                                                finish()
                                                                            }
                                                                        }else{
                                                                            kotlin.runCatching {
                                                                                val intent = Intent(
                                                                                    applicationContext,
                                                                                    MainActivity::class.java
                                                                                )
                                                                                startActivity(intent)
                                                                                finish()
                                                                            }
                                                                        }

                                                                      /*  if (showRoboPro) {
                                                                            kotlin.runCatching {
                                                                                sendEvent(true)
                                                                                val intent =
                                                                                    Intent()
                                                                                intent.setClassName(
                                                                                    applicationContext,
                                                                                    getProScreen()
                                                                                )
                                                                                intent.putExtra(
                                                                                    "show_ad",
                                                                                    true
                                                                                )
                                                                                kotlin.runCatching {
                                                                                    this@SplashActivity.setLocale(
                                                                                        languageCode
                                                                                    )
                                                                                }
                                                                                startActivity(intent)
                                                                                finish()
                                                                            }
                                                                        } else {
                                                                            kotlin.runCatching {
                                                                                val intent = Intent(
                                                                                    applicationContext,
                                                                                    MainActivity::class.java
                                                                                )
                                                                                startActivity(intent)
                                                                                finish()
                                                                            }
                                                                        }*/
//                                }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendEvent(premium: Boolean) {
        if (premium) {
            firebaseAnalytics?.logEvent(
                Events.Screens.MAIN,
                Bundle().apply {
                    putString(
                        Events.ParamsKeys.ACTION,
                        Events.ParamsValues.ROBO_OPEN
                    )
                    putString(
                        Events.ParamsKeys.OPENING_SCREEN,
                        Events.Screens.PREMIUM
                    )
                })
        } else {
            firebaseAnalytics?.logEvent(
                Events.Screens.MAIN,
                Bundle().apply {
                    putString(
                        Events.ParamsKeys.ACTION,
                        Events.ParamsValues.ROBO_OPEN
                    )
                    putString(
                        Events.ParamsKeys.OPENING_SCREEN,
                        Events.Screens.PREMIUM_OFFER
                    )
                })
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

    private fun remoteConfig(adConfigModel: com.project.common.remote_config.AdConfigModel) {
        kotlin.runCatching {
            loadBannerOnBoardOne = false
            loadBannerOnBoardTwo = false
            loadBannerOnBoardThree = false
            loadBannerOnBoardFour = false
            loadBannerOnBoardMedium = false
            proSplashOrHome =adConfigModel.splashScreen?.splashProHome ?: false
            allBannerReloadLimit = (newAdsConfig?.appBanner?.reloadLimit ?: 2L).toLong()
            loadNativeSplash = newAdsConfig?.splashScreen?.native?.isEnabled ?: false
            loadInterstitialSplash = newAdsConfig?.splashScreen?.interstitial?.isEnabled ?: false
            enableHomeInterAd = adConfigModel.appInterstitial?.isEnabled ?: false
            isProWithInterOn = enableHomeInterAd // to on off pro with inter
            loadSplashAppOpen = adConfigModel.splashScreen?.appOpen?.isEnabled ?: false
            interstitialHomeFloor = adConfigModel.appInterstitial?.floor ?: 2
            interstitialHomeStartCount = adConfigModel.appInterstitial?.startCount ?: 0
            interstitialHomeAfterStartCount = adConfigModel.appInterstitial?.afterStartCount ?: 1
            interstitialHomeAlwaysShow = adConfigModel.appInterstitial?.alwaysShow ?: false
            splashTime =(adConfigModel.splashScreen?.time ?: 500L).toLong()
          //  splashTimeOut = (adConfigModel.splashScreen?.timeout ?: 10000L).toLong()
            splashTimeOut = (adConfigModel.splashScreen?.timeout ?: 7000L).toLong()
            // set in remote json
            Log.d("SPLASH_TIME_ISSUE", "remoteConfig splash time  :$splashTime ")
            Log.d("SPLASH_TIME_ISSUE", "remoteConfig time out  :$splashTimeOut ")
            loadNativeLfOne = adConfigModel.languageScreen?.nativeBeforeSelection?.isEnabled ?: true
            loadNativeLfTwo = adConfigModel.languageScreen?.nativeAfterSelection?.isEnabled ?: true
            loadNativeObOne = adConfigModel.onboarding1Native?.isEnabled ?: true
            loadNativeObTwo = adConfigModel.onboarding2Native?.isEnabled ?: true
            loadNativeObThree = adConfigModel.onboarding3Native?.isEnabled ?: true
            loadNativeObFour = adConfigModel.onboarding4Native?.isEnabled ?: true
            showAllAppOpenAd = adConfigModel.appOpenResume?.isEnabled ?: true
            onBoardingOneNativeId = adConfigModel.onboarding1Native?.adUnitIds?.getOrNull(0) ?: "ca-app-pub-4276074242154795/8502828396"
            onBoardingTwoNativeId = adConfigModel.onboarding2Native?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/8852923006"
            onBoardingThreeNativeId = adConfigModel.onboarding3Native?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/1799627163"
            onBoardingFourNativeId = adConfigModel.onboarding4Native?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/8855990101"
            lfoOneNativeId =
                adConfigModel.languageScreen?.nativeBeforeSelection?.adUnitIds?.getOrNull(0)
                    ?: "ca-app-pub-4276074242154795/3847868719"
            lfoTwoNativeId =
                adConfigModel.languageScreen?.nativeAfterSelection?.adUnitIds?.getOrNull(0)
                    ?: "ca-app-pub-4276074242154795/6254254792"
            splashInterAdId = adConfigModel.splashScreen?.interstitial?.adUnitIds?.getOrNull(0)
                ?: "ca-app-pub-4276074242154795/7571205893"
            notiLockScreen = adConfigModel.globalSettings?.notification?.lockscreen ?: false
            timePushNotiLockscreen1 =
                adConfigModel.globalSettings?.notification?.timePush1?.toLong() ?: 10L
            timePushNotiLockscreen2 = 9
            notiStatusBarCountry = 1
            loadNativeOnResume = true
            homeNewInterStrategy = true
            flowUninstall = false
            nativeReasonUninstall = false
            galleryButtonNewFlow = true
            proCounter = 4L
            InterstitialUnInstall = false
            adGalleryNativeFloor = 4
            adGalleryNative = true
            notiLockscreenCountry = "1"
            loadNativeOld = true
            popupEventValentine = false
            loadNativeFullOne = false
            loadNativeFullTwo = false
            questionScreenEnable = false
            tutorialScr = false
            showBlendGuideScreen = false
            loadInterstitialSave = true
            setOnClick()


            Log.i("TAG", "//remoteConfig: completed")
            //onCompletion.invoke()
        }
    }


}