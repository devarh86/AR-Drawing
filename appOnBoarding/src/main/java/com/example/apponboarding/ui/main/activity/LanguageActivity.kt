package com.example.apponboarding.ui.main.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ads.Constants
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.loadBannerOnBoardOne
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeLfTwo
import com.example.ads.Constants.loadNativeObFour
import com.example.ads.Constants.loadNativeObOne
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.Constants.loadNativeObTwo
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadNewInterstitialForPro
import com.example.ads.admobs.utils.loadNewInterstitialWithoutStrategyCheck
import com.example.ads.admobs.utils.loadOnBoardingBanner
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.admobs.utils.showNewInterstitialPro
import com.example.ads.crosspromo.helper.show
import com.example.ads.model.AdConfigModel
import com.example.ads.utils.fullNativeOne
import com.example.ads.utils.fullNativeTwo
import com.example.ads.utils.languageInterstitial
import com.example.ads.utils.nativeLanguageOne
import com.example.ads.utils.nativeLanguageTwo
import com.example.ads.utils.onBoardNativeFour
import com.example.ads.utils.onBoardNativeOne
import com.example.ads.utils.onBoardNativeThree
import com.example.ads.utils.onBoardNativeTwo
import com.example.apponboarding.databinding.ActivityLanguageBinding
import com.example.apponboarding.ui.main.adapter.LanguageAdapter
import com.example.apponboarding.ui.main.viewModels.LanguageViewModel
import com.example.inapp.helpers.Constants.isProVersion
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "LanguageActivity"

@AndroidEntryPoint
class LanguageActivity : AppCompatActivity() {

    private var _binding: ActivityLanguageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LanguageViewModel by viewModels()

    private var beforeCallFromLfOne = true

    private lateinit var languageAdapter: LanguageAdapter

    private fun getObBannerPosition(): Int? {
        return if (loadBannerOnBoardOne) {
            0
        } else if (Constants.loadBannerOnBoardTwo) {
            1
        } else if (Constants.loadBannerOnBoardThree) {
            2
        }else if (Constants.loadBannerOnBoardFour) {
            3
        } else {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //loadOnBoardingBanner(getObBannerPosition())
     //   loadNewInterstitialForPro(languageInterstitial()) {}
        kotlin.runCatching {
            firebaseAnalytics?.logEvent("event_fragment_language_one", null)
            Log.i("firebase_event", "logEvent: event_fragment_language_one")
        }

        binding.initLanguage()
        hideNavigation()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    private var shownEvent = false

    private fun ActivityLanguageBinding.initLanguage() {

        checkIcon.setOnSingleClickListener {

           // showNewInterstitialPro(languageInterstitial()){
                intent.getBooleanExtra("from_setting", false).let {
                    if (it) {
                        setResult(Activity.RESULT_OK)
                        finish()

                    } else {
                        kotlin.runCatching {
                            val intent = Intent(
                                applicationContext,
                                OnBoardingActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        }
                    }
             //   }
            }

        }

        checkIcon.isClickable = false

        val currentLanguageCode = viewModel.getSelectedLanguageCode()

        Log.d(TAG, "currentLanguageCode: $currentLanguageCode")
        // Initialize the adapter
        languageAdapter = LanguageAdapter(emptyList(), currentLanguageCode) { selectedLanguage ->
            Log.d(TAG, "initLanguage: $selectedLanguage")
            viewModel.selectLanguage(selectedLanguage)
//            localizationManager.setLocale(selectedLanguage.languageCode)
            setLocale(selectedLanguage.languageCode)
            languageCode = selectedLanguage.languageCode

            checkIcon.backgroundTintList = null
            checkIcon.setBackgroundDrawable(
                ContextCompat.getDrawable(applicationContext, com.project.common.R.drawable.rounded_red_btn_bg)
            )
            checkIcon.setTextColor(ContextCompat.getColor(applicationContext, com.example.ads.R.color.white))
            checkIcon.isClickable = true

            kotlin.runCatching {

                if (!shownEvent) {
                    firebaseAnalytics?.logEvent("event_fragment_language_two", null)
                    Log.i("firebase_event", "logEvent: event_fragment_language_two")
                    shownEvent = true
                }
                beforeCallFromLfOne = false

                loadNativeAd(
                    nativeLanguageTwo(),
                    nextConfig = getNextConfig()
                )
            }
        }


        intent.getBooleanExtra("from_setting", false).let {
            if (it) {
                languageAdapter.isSelectedStart = -1
            }
        }

        rvLanguages.layoutManager = LinearLayoutManager(this@LanguageActivity)
        rvLanguages.adapter = languageAdapter
        viewModel.languages.observe(this@LanguageActivity) { languages ->
            if (::languageAdapter.isInitialized) {
                languageAdapter.updateData(languages, currentLanguageCode)
            }
        }

        viewModel.loadLanguages()
    }

    private fun getNextConfig(): AdConfigModel? {
        return if (loadNativeObOne) {
            onBoardNativeOne()
        } else if (loadNativeFullOne) {
            fullNativeOne()
        } else if (loadNativeObTwo) {
            onBoardNativeTwo()
        } else if (loadNativeFullTwo) {
            fullNativeTwo()
        } else if (loadNativeObThree) {
            onBoardNativeThree()
        }  else if (loadNativeObFour) {
            onBoardNativeFour()
        } else {
            null
        }
    }

    override fun onResume() {
        super.onResume()
        if (beforeCallFromLfOne) {
            loadNativeAd(
                nativeLanguageOne(),
                if (loadNativeLfTwo) nativeLanguageTwo() else getNextConfig()
            )
            beforeCallFromLfOne = false
        } else {
//            loadNativeAd(nativeLanguageTwo())
        }
    }

    private fun loadNativeAd(
        adConfigModel: AdConfigModel?,
        nextConfig: AdConfigModel?
    ) {

        runCatching {
            if (isProVersion() || (adConfigModel != null && !adConfigModel.enable)) {
                _binding?.bannerContainer?.isVisible = false
                _binding?.nativeContainer?.isVisible = false
            } else {

                _binding?.bannerContainer?.isVisible = false
                _binding?.nativeContainer?.visibility = View.VISIBLE
                _binding?.mediumNativeLayout?.shimmerViewContainer?.visibility =
                    View.VISIBLE

                _binding?.let { binding ->

                    loadAndShowNativeOnBoarding(
                        loadedAction =
                            {
                                if (!isFinishing && !isDestroyed && _binding != null) {
                                    binding.nativeContainer.show()
                                    binding.mediumNativeLayout.adContainer.show()
                                    binding.mediumNativeLayout.shimmerViewContainer.visibility =
                                        View.INVISIBLE
                                    binding.mediumNativeLayout.adContainer.removeAllViews()
                                    if (!isFinishing && !isDestroyed && _binding != null) {
                                        if (it?.parent != null) {
                                            (it.parent as ViewGroup).removeView(it)
                                        }
                                    }
                                    Log.d(
                                        "ActivityState",
                                        "isFinishing 0 LF: ${isFinishing}, isDestroyed: ${isDestroyed}"
                                    )

                                    if (!isFinishing && !isDestroyed && _binding != null) {
                                        binding.mediumNativeLayout.adContainer.addView(it)
                                    }
                                }
                            },
                        failedAction = {

                            binding.mediumNativeLayout.shimmerViewContainer.visibility =
                                View.INVISIBLE
                        },
                        config = adConfigModel,
                        nextConfig
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        finishAndRemoveTask()
    }


    override fun onPause() {
        super.onPause()
    }
}