package com.example.questions_intro.ui.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.ads.Constants.bannerTutorial
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.showRoboPro
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.onPauseBlendBoardingBanner
import com.example.ads.admobs.utils.onResumeBanner
import com.example.ads.admobs.utils.onResumeBlendBoardingBanner
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.admobs.utils.showAppOpenBlendGuide
import com.example.analytics.Constants
import com.example.inapp.helpers.Constants.isProVersion
import com.example.questions_intro.R
import com.example.questions_intro.databinding.ActivityBlendOnBoardingBinding
import com.example.questions_intro.ui.view_model.IntroViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.project.common.repo.datastore.AppDataStore
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.runCatchingWithLog
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class BlendOnBoardingActivity : ComponentActivity() {

    private var countDown: CountDownTimer? = null

    private var navController: NavController? = null

    private var screen: Int = 0

    var showAppOpen = false

    var alreadyClick = false

    private val binding by lazy { ActivityBlendOnBoardingBinding.inflate(layoutInflater) }

    private var callback: OnBackPressedCallback? = null

    private val introViewModel by viewModels<IntroViewModel>()

    @Inject
    lateinit var appDataStore: AppDataStore

    private var alreadyLaunched = false

    private var prevImgViewSelection: ShapeableImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            setLocale(languageCode)
        }

        setContentView(binding.root)

        Constants.firebaseAnalytics?.logEvent("blend_ob_show", null)

        kotlin.runCatching {
            hideNavigation()
        }

        onBackPress()

        if (bannerTutorial) {
            loadBannerAd(false)
        } else {
            binding.bannerContainer.isVisible = false
        }

        binding.initView()
    }

    fun showAppOpenAd() {
        kotlin.runCatching {
            binding.let {
                showAppOpen = true
                if (bannerTutorial) {
                    binding.bannerContainer.visibility = INVISIBLE
                }
                //  hideOrShowAd(false)
                Handler(Looper.getMainLooper()).postDelayed({
                    showAppOpen {
                        binding.let {
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (bannerTutorial) {
                                    binding.bannerContainer.visibility = View.VISIBLE
                                }
                                // hideOrShowAd(true)
                                showAppOpen = false
                                if (bannerTutorial) {
                                    loadBannerAd(false)
                                } else {
                                    binding.bannerContainer.isVisible = false
                                }
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus)
            hideNavigation()
    }

    override fun onPause() {
        super.onPause()
        onPauseBlendBoardingBanner()
    }

    private fun ActivityBlendOnBoardingBinding.initView() {

        prevImgViewSelection = item3Img

        runCatchingWithLog {
            val popUp = Dialog(
                this@BlendOnBoardingActivity,
                com.project.common.R.style.BottomSheetDialog_full_screen
            )
            val view =
                LayoutInflater.from(this@BlendOnBoardingActivity)
                    .inflate(R.layout.pop_up_guide, null)
            popUp.setContentView(view)
            popUp.setCancelable(false)

            popUp.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnClose = view.findViewById<TextView>(R.id.skip_txt)
            val btnContinue = view.findViewById<TextView>(R.id.done_txt)
            val subHeading = view.findViewById<TextView>(R.id.sub_heading_txt)

            kotlin.runCatching {

                val stringResult = SpannableStringBuilder().apply {
                    val boldText = SpannableString(
                        ContextCompat.getString(
                            this@BlendOnBoardingActivity,
                            com.project.common.R.string.templates
                        ) + ": "
                    )
                    boldText.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        boldText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    append(boldText)
                    append(
                        ContextCompat.getString(
                            this@BlendOnBoardingActivity,
                            com.project.common.R.string.change_photos_with_just_one_touch
                        )
                    )
                }
                subHeading.text = stringResult
            }

            popUp.setOnShowListener {
                hideNavigation()
            }

            btnClose.setOnClickListener {
                lifecycleScope.launch(Main) {
                    binding.bannerContainer.visibility = View.INVISIBLE
                    showAppOpen = true
                    Constants.firebaseAnalytics?.logEvent("blend_ob_skip_popup", null)


                    showAppOpenBlendGuide ({
                        kotlin.runCatching {
                            if (popUp.isShowing && !isDestroyed && !isFinishing)
                                popUp.dismiss()
                            navigateToNext()
                        }

                    }, showLoading = {
                        if(!isFinishing && !isDestroyed){
                            binding.loadingAdLy.isVisible = true
                        }
                    })
                }
            }

            btnContinue.setOnClickListener {
                Constants.firebaseAnalytics?.logEvent("blend_ob_continue_popup", null)
                kotlin.runCatching {
                    if (popUp.isShowing && !isDestroyed && !isFinishing)
                        popUp.dismiss()
                }
            }

            if (!popUp.isShowing && !isDestroyed && !isFinishing)
                popUp.show()
        }

        skipTxt.setOnSingleClickListener {
            lifecycleScope.launch(Main) {
                Constants.firebaseAnalytics?.logEvent("blend_ob_skip", null)
                binding.bannerContainer.visibility = View.INVISIBLE
                showAppOpen = true
                showAppOpenBlendGuide ({
                    navigateToNext()
                }, showLoading = {
                    if(!isFinishing && !isDestroyed){
                        binding.loadingAdLy.isVisible = true
                    }
                })
            }
        }

        item0Img.setOnSingleClickListener {
            if(alreadyClick){
               return@setOnSingleClickListener
            }

            alreadyClick = true

            progressLayout.isVisible = true
            animationProcessing.playAnimation()
            item0Img.selection()
            startCountDownTime(0)
        }

        item1Img.setOnSingleClickListener {
            if(alreadyClick){
                return@setOnSingleClickListener
            }

            alreadyClick = true
            progressLayout.isVisible = true
            animationProcessing.playAnimation()
            item1Img.selection()
            startCountDownTime(1)
        }

        item2Img.setOnSingleClickListener {
            if(alreadyClick){
                return@setOnSingleClickListener
            }

            alreadyClick = true
            progressLayout.isVisible = true
            animationProcessing.playAnimation()
            item2Img.selection()
            startCountDownTime(2)
        }

        item3Img.setOnSingleClickListener {
            if(alreadyClick){
                return@setOnSingleClickListener
            }

            alreadyClick = true
            progressLayout.isVisible = true
            animationProcessing.playAnimation()
            item3Img.selection()
            startCountDownTime(3)
        }

        applyGuide.setOnSingleClickListener {
            progressLayout.isVisible = true
            animationProcessing.playAnimation()
            startCountDownTime(3)
        }

        doneTxt.setOnSingleClickListener {
            kotlin.runCatching {
                GlobalScope.launch(IO) {
                    appDataStore.writeBlendOnBoardComplete()
                }

                lifecycleScope.launch(Main) {
                    Constants.firebaseAnalytics?.logEvent("blend_ob_done", null)
                    binding.bannerContainer.visibility = View.INVISIBLE
                    showAppOpen = true
                    showAppOpenBlendGuide( {
                        navigateToNext()
                    }, showLoading = {
                        if(!isFinishing && !isDestroyed){
                            binding.loadingAdLy.isVisible = true
                        }
                    })
                }
            }
        }
    }
    private  fun navigateToNext(){
     if (!isProVersion() && showRoboPro) {
            openPro()
        }else{
         kotlin.runCatching {
             val intent = Intent()
             intent.setClassName(
                 applicationContext,
                 "com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity"
             )
             startActivity(intent)
             overridePendingTransition(0, 0)
             showAppOpen = false
             finish()
         }
     }
    }

    private fun ShapeableImageView.selection() {
        prevImgViewSelection?.strokeWidth = 0f
        strokeWidth = 5f
        prevImgViewSelection = this
    }

    private fun startCountDownTime(position: Int) {

        binding.applyGuide.pauseAnimation()
        binding.applyGuide.isVisible = false

        countDown = object : CountDownTimer(2000, 500) {
            override fun onTick(l: Long) {
                kotlin.runCatching {
                    if (!isFinishing && !isDestroyed) {
                        val progress = ((abs(l - 2000) * 100) / 2000L)
                        binding.progressTextBelow.text = "$progress%"
                        binding.progressBar.progress = progress.toInt()
                    }
                }
            }

            override fun onFinish() {
                kotlin.runCatching {
                    if (!isFinishing && !isDestroyed) {
                        binding.progressBar.progress = 100
                        binding.progressTextBelow.text = "100%"
                        when (position) {
                            0 -> R.drawable.item_0
                            1 -> R.drawable.item_1
                            2 -> R.drawable.item_2
                            else -> R.drawable.item_3
                        }
                        Glide.with(this@BlendOnBoardingActivity).load(
                            when (position) {
                                0 -> R.drawable.result_0
                                1 -> R.drawable.result_1
                                2 -> R.drawable.result_2
                                else -> R.drawable.result_3
                            }
                        ).apply {
                            kotlin.runCatching {
                                binding.applyGuide.isVisible = false
                                binding.progressLayout.isVisible = false
                                binding.skipTxt.isVisible = false
                                into(
                                    binding.mainImg
                                )
                                if (bannerTutorial) {
                                    loadBannerAd(false)
                                } else {
                                    binding.bannerContainer.isVisible = false
                                }
                                lifecycleScope.launch(Main) {
                                    delay(2000)
                                    if (!isFinishing && !isDestroyed) {
                                        binding.bottomLayout.isVisible = false
                                        binding.greatWorkLayout.isVisible = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.start()
    }

    private fun loadBannerAd(fromButton: Boolean = false) {
        kotlin.runCatching {
            runOnUiThread {
                if (!showAppOpen && !isProVersion()) {
                    binding.let { binding ->
                        binding.bannerContainer?.visibility = View.VISIBLE
                        onResumeBlendBoardingBanner(
                            binding.adBannerContainer,
                            null,
                            binding.bannerLayout.adContainer,
                            binding.bannerLayout.shimmerViewContainer
                        )
                    }
                } else {
                    try {
                        if (isProVersion()) {
                            binding.bannerContainer?.visibility = View.GONE
                        } else {
                            binding.bannerContainer?.visibility = View.INVISIBLE
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.e("error", "onResume: ", ex)
                    }
                }
            }
        }
    }

    private fun navigateToMainOrPro() {

        firebaseAnalytics?.logEvent("intro_scr_click_continue", null)

        if (alreadyLaunched)
            return

        alreadyLaunched = true

        kotlin.runCatching {
            kotlin.runCatching {
//                if (isProVersion()) {
                kotlin.runCatching {
                    val intent = Intent()
                    intent.setClassName(
                        applicationContext,
                        "com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity"
                    )
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
//                } else {
//                    openPro()
//                }
            }.onFailure {
                alreadyLaunched = false
            }
        }.onFailure {
            alreadyLaunched = false
        }
    }

    private fun openPro() {
        kotlin.runCatching {
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

    private fun onBackPress() {

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                runCatchingWithLog {
                    lifecycleScope.launch(Main) {
                        binding.bannerContainer.visibility = View.INVISIBLE
                        showAppOpen = true
                        showAppOpenBlendGuide({
                         navigateToNext()
                        },showLoading = {
                            if(!isFinishing && !isDestroyed){
                                binding.loadingAdLy.isVisible = true
                            }
                        })
                    }
                }
            }
        }
        callback?.let {
            onBackPressedDispatcher.addCallback(this, it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDown?.cancel()
    }
}