package com.example.questions_intro.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.ads.Constants
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.showBlendGuideScreen
import com.example.ads.Constants.showRoboPro
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.onPauseSurveyBanner
import com.example.ads.admobs.utils.onResumeBlendBoardingBanner
import com.example.ads.admobs.utils.onResumeSurveyBanner
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.survey
import com.example.inapp.helpers.Constants.isProVersion
import com.example.questions_intro.R
import com.example.questions_intro.databinding.ActivitySurveyBinding
import com.google.android.gms.ads.nativead.NativeAdView
import com.project.common.utils.ConstantsCommon.introOnBoardingCompleted
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSingleClickListener
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SurveyActivity : AppCompatActivity() {

    private var _binding: ActivitySurveyBinding? = null

    private val binding get() = _binding!!

    private var alreadyLaunch = false

    private val dataStoreViewModel by viewModels<DataStoreViewModel>()

    private var counter = 0

    private var lastSelected: ImageView? = null

    private var selectedList = arrayListOf(false, false, false, false)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kotlin.runCatching {
            setLocale(languageCode)
        }

        _binding = ActivitySurveyBinding.inflate(layoutInflater)
        setContentView(_binding?.root)

        kotlin.runCatching {
            hideNavigation()
        }

        _binding?.initViews()

        _binding?.clickAnim?.isVisible = true
        _binding?.clickAnim?.playAnimation()

        dataStoreViewModel.initViewModel()

        GlobalScope.launch(IO) {
            dataStoreViewModel.updateIntroComplete()
            introOnBoardingCompleted = true
        }

        logEvent("survey_view", null)
    }

    private fun logEvent(event: String, bundle: Bundle?) {
        bundle?.let {
            firebaseAnalytics?.logEvent(event, bundle)
        } ?: run {
            firebaseAnalytics?.logEvent(event, null)
        }

        Log.i("TAG", "firebase_event: $event  $bundle")
    }

    private fun ImageView.addSelectionUnSelection(path: Int) {

        Glide.with(this@SurveyActivity).load(path).into(this)
    }

    private fun ImageView.getPositionOfImg(): Int {
        this.tag.let {
            kotlin.runCatching {
                it.toString().let {
                    if (it.isNotBlank()) {
                        it.toInt().let { position ->
                            return position
                        }
                    }
                }
            }
        }
        return -1
    }

    private fun click(imgView: ImageView) {
        loadAndShowNativeAd()
        _binding?.clickAnim?.pauseAnimation()
        _binding?.clickAnim?.isVisible = false
        imgView.getPositionOfImg().let { position ->
            if (position < selectedList.size && position >= 0) {
                if (selectedList[position]) {
                    imgView.addSelectionUnSelection(R.drawable.unselected_check_box)
                } else {
                    imgView.addSelectionUnSelection(R.drawable.selected_check_survey)
                }
            }
            selectedList[position] = !selectedList[position]
        }
    }

    private fun ActivitySurveyBinding.initViews() {

        collageSurveyLayout.setOnSingleClickListener {
            click(collageSurveyCheck)
        }

        editLayout.setOnSingleClickListener {
            click(editCheck)
        }

        aiEnhanceLayout.setOnSingleClickListener {
            click(aiEnhanceCheck)
        }

        templateLayout.setOnSingleClickListener {
            click(templateCheck)
        }

        next.setOnSingleClickListener {
            logEvent("survey_click_next", null)
            if (selectedList.contains(true)) {
                nextClick()
            } else {
                Toast.makeText(
                    this@SurveyActivity,
                    getString(com.project.common.R.string.choose_an_answer), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndShowNativeAd()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus)
            hideNavigation()
    }

    private fun nextClick() {

        kotlin.runCatching {
            var ids = ""

            selectedList.forEachIndexed { index, b ->
                if (b) {
                    ids =
                        ids + (index + 1).toString() + if (index + 1 == selectedList.size) "" else ","
                }
            }

            logEvent("survey_click_answer", Bundle().apply {
                putString("id_survey", ids)
            })

            kotlin.runCatching {
                GlobalScope.launch(IO) {
                    dataStoreViewModel.updateSurveyComplete()
                }
            }
            if (showBlendGuideScreen) {
                kotlin.runCatching {
                    alreadyLaunch = true
                    val intent = Intent(this, BlendOnBoardingActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
            } else if (!isProVersion() && showRoboPro) {
                openPro()
            } else {
                kotlin.runCatching {
                    alreadyLaunch = true
                    val intent = Intent()
                    intent.setClassName(
                        applicationContext,
                        "com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity"
                    )
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
        }
    }

    private fun openPro() {
        kotlin.runCatching {
            alreadyLaunch = true
            val intent = Intent()
            intent.setClassName(
                applicationContext,
                getProScreen()
            )
            intent.putExtra("show_ad", true)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        kotlin.runCatching {
            finishAndRemoveTask()
        }
    }

    fun showAppOpen() {
        kotlin.runCatching {
            _binding?.nativeContainer?.visibility = INVISIBLE
            _binding?.bannerContainer?.visibility = INVISIBLE
            binding.let {
                showAppOpen = true
                Handler(Looper.getMainLooper()).postDelayed({
                    showAppOpen {
                        binding.let {
                            Handler(Looper.getMainLooper()).postDelayed({
                                showAppOpen = false
                                loadAndShowNativeAd()
                                loadAppOpen()
                            }, 800L)
                        }
                    }
                }, 600L)
            }
//            show app open
//            showAppOpen = false
//            loadAndShowNativeAd()
        }
    }


    fun hideOrShowAd(showAd: Boolean) {
        kotlin.runCatching {

            this.let {
//                if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && !isProVersion()) {
//                    bottomSheetProcessDialogBinding?.nativeContainer?.visibility = if (showAd) VISIBLE else INVISIBLE
//                }
            }
        }
    }

    private var previousNativeAd: NativeAdView? = null

    var showAppOpen = false

    private fun loadAndShowNativeAd() {

        if (showAppOpen) {
            return
        }

        kotlin.runCatching {
            if (isProVersion()) {
                _binding?.nativeContainer?.visibility = INVISIBLE
                _binding?.bannerContainer?.visibility = GONE
            } else {
                    if (Constants.loadBannerSurvey) {
                        _binding?.bannerContainer?.visibility = VISIBLE
                        _binding?.nativeContainer?.visibility = INVISIBLE
                        onResumeSurveyBanner(
                            binding.bannerContainer,
                            null,
                            binding.bannerLayout.adContainer,
                            binding.bannerLayout.shimmerViewContainer
                        )
                    } else {
                        _binding?.nativeContainer?.visibility = VISIBLE
                        _binding?.bannerContainer?.visibility = GONE
                        if (_binding?.shimmerContainerNative?.visibility == VISIBLE) {
                            _binding?.shimmerContainerNative?.startShimmer()
                        }

                        _binding?.let { binding ->
                       loadAndShowNativeOnBoarding(
                                loadedAction = {
                                    kotlin.runCatching {
                                        if (!isFinishing && !isDestroyed && _binding != null) {
                                            binding.nativeContainer.show()
                                            binding.nativeAdContainer.show()
                                            binding.shimmerContainerNative.visibility =
                                                INVISIBLE
                                            binding.nativeAdContainer.removeAllViews()
                                            if (it?.parent != null) {
                                                (it.parent as ViewGroup).removeView(it)
                                            }
                                            if (!isFinishing && !isDestroyed && _binding != null) {
                                                binding.nativeAdContainer.addView(it)
                                            }
                                        }
                                    }
                                },
                                failedAction = {
                                    if (!isFinishing && !isDestroyed) {
                                        _binding?.nativeContainer?.visibility = INVISIBLE
                                        _binding?.shimmerContainerNative?.visibility
                                    }
                                },
                                survey(), nextConfig = survey()
                            )
                        }
                    }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseSurveyBanner()
    }

    override fun onDestroy() {
        super.onDestroy()
//        resetNative()
    }
}