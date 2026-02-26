package com.example.apponboarding.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.ads.Constants
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.showRoboPro
import com.example.ads.Constants.surveyScreenEnable
import com.example.ads.admobs.utils.loadNewInterstitialForPro
import com.example.ads.admobs.utils.showNewInterstitialPro
import com.example.ads.utils.startedInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.apponboarding.databinding.ActivityOnBoardingBinding
import com.example.apponboarding.ui.main.adapter.OnboardingAdapter
import com.example.inapp.helpers.Constants.isProVersion
import com.project.common.utils.ConstantsCommon.surveyCompleted
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {
    private var _binding: ActivityOnBoardingBinding? = null
    private val binding get() = _binding!!
    private var onboardingViewPager: ViewPager2? = null
    private var listener: OnBoarding? = null
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private var eventFullNativeOne = false
    private var eventFullNativeTwo = false
    private var eventFragmentOnBoardingOne = false
    private var eventFragmentOnBoardingTwo = false
    private var eventFragmentOnBoardingThree = false
    private var eventFragmentOnBoardingFour = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        _binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        kotlin.runCatching {
            this.setLocale(languageCode)
        }
        setContentView(binding.root)
        loadNewInterstitialForPro(startedInterstitial()) {}
        dataStoreViewModel.initViewModel()

        hideNavigation()
        initOnBoardingViewPager()

        // loadNewInterstitialWithoutStrategyCheck(obLastInterstitial()) {}
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    fun setListener(mListener: OnBoarding) {
        listener = mListener
    }

    override fun onBackPressed() {
        super.onBackPressed()
        kotlin.runCatching {
//            finishAffinity()
            finishAndRemoveTask()
        }
    }

    private fun initOnBoardingViewPager() {
        try {
            onboardingViewPager = binding.onBoardingViewPager
            onboardingViewPager?.let { onboardingViewPager ->
                onboardingViewPager.adapter = OnboardingAdapter(this@OnBoardingActivity)
                onboardingViewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        Log.d(
                            "VPagePosition",
                            "onPageSelected:${binding.onBoardingViewPager.currentItem} "
                        )
                    }

//                    override fun onPageScrollStateChanged(state: Int) {
//                        super.onPageScrollStateChanged(state)
//                    }
                })
            }
        } catch (ex: Exception) {
            Log.i("error", "initOnBoardingViewPager: $ex")
        }
    }

    fun getPagerState(): Boolean {
        return _binding?.onBoardingViewPager?.scrollState == 0
    }

    fun logEvent(event: String) {
        if (event == "event_full_native_one" && !eventFullNativeOne) {
            Constants.firebaseAnalytics?.logEvent("fragment_full_native_one", null)
            eventFullNativeOne = true
            Log.i("firebase_event", "logEvent: $event")
        } else if (event == "event_full_native_two" && !eventFullNativeTwo) {
            Constants.firebaseAnalytics?.logEvent("event_full_native_two", null)
            eventFullNativeTwo = true
            Log.i("firebase_event", "logEvent: $event")
        } else if (event == "event_fragment_onboarding_one" && !eventFragmentOnBoardingOne) {
            Constants.firebaseAnalytics?.logEvent("event_fragment_onboarding_one", null)
            eventFragmentOnBoardingOne = true
            Log.i("firebase_event", "logEvent: $event")
        } else if (event == "event_fragment_onboarding_two" && !eventFragmentOnBoardingTwo) {
            Constants.firebaseAnalytics?.logEvent("event_fragment_onboarding_two", null)
            eventFragmentOnBoardingTwo = true
            Log.i("firebase_event", "logEvent: $event")
        } else if (event == "event_fragment_onboarding_three" && !eventFragmentOnBoardingThree) {
            Constants.firebaseAnalytics?.logEvent("event_fragment_onboarding_three", null)
            eventFragmentOnBoardingThree = true
            Log.i("firebase_event", "logEvent: $event")
        } else if (event == "event_fragment_onboarding_four" && !eventFragmentOnBoardingFour) {
            Constants.firebaseAnalytics?.logEvent("event_fragment_onboarding_four", null)
            eventFragmentOnBoardingFour = true
            Log.i("firebase_event", "logEvent: $event")
        }
    }

    fun navigateToNextPage(fromAutoScroll: Boolean = false) {

        try {
            onboardingViewPager?.let { onboardingViewPager ->
                kotlin.runCatching {
                    onboardingViewPager.adapter?.let { adapter ->
                        if (onboardingViewPager.currentItem < adapter.itemCount.minus(1)
                        ) {
                            if (fromAutoScroll && onboardingViewPager.currentItem < adapter.itemCount.minus(
                                    1
                                )
                            ) {
                                onboardingViewPager.currentItem += 1
                            } else {
                                onboardingViewPager.currentItem += 1
                            }
                        } else {
                            // If on the last page, finish onboarding and go to MainActivity
                            if (!fromAutoScroll) {
                                completeOnboarding()
                            }
                        }
                    }

                }

            }
        } catch (_: Exception) {

        }
    }

    interface OnBoarding {
        fun onBoardingComplete()
    }

    private fun completeOnboarding() {
        kotlin.runCatching {
            // showNewInterstitial(obLastInterstitial()) {
            showNewInterstitialPro(startedInterstitial()) {
                if (!surveyCompleted && surveyScreenEnable) {
                    kotlin.runCatching {
                        val intent = Intent()
                        intent.setClassName(
                            applicationContext,
                            "com.example.questions_intro.ui.activity.SurveyActivity"
                        )
                        startActivity(intent)
                        finish()
                    }
                } else if (isProVersion()) {
                    kotlin.runCatching {
                        val intent = Intent()
                        intent.setClassName(
                            applicationContext,
                            "com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity"
                        )
                        startActivity(intent)
                        finish()
                    }
                } else if (showRoboPro) {
                    kotlin.runCatching {
                        sendEvent()
                        val intent = Intent()
                        intent.setClassName(
                            applicationContext,
                            getProScreen()
                        )
                        intent.putExtra("show_ad", true)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    /*  kotlin.runCatching {
                        sendEvent()
                        val intent = Intent()
                        intent.setClassName(
                            applicationContext,
                            getProScreen()
                        )
                        intent.putExtra("show_ad", true)
                        startActivity(intent)
                        finish()
                    }*/
                    kotlin.runCatching {
                        val intent = Intent()
                        intent.setClassName(
                            applicationContext,
                            "com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity"
                        )
                        startActivity(intent)
                        finish()
                    }
                }
            }
            // }
        }
    }

    private fun sendEvent() {
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
    }
}