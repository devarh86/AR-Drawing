package com.example.questions_intro.ui.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.navigation.NavController
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.Constants.introScreenUiButton
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.showBlendGuideScreen
import com.example.ads.Constants.showRoboPro
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.intro
import com.example.ads.utils.survey
import com.example.inapp.helpers.Constants.isProVersion
import com.example.questions_intro.databinding.ActivityIntroBinding
import com.example.questions_intro.ui.compose_views.IntroView
import com.example.questions_intro.ui.view_model.IntroViewModel
import com.project.common.repo.datastore.AppDataStore
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.runCatchingWithLog
import com.project.common.utils.setColor
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntroActivity : AppCompatActivity() {

    private var navController: NavController? = null

    private var screen: Int = 0

    private val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

    private var callback: OnBackPressedCallback? = null

    private val introViewModel by viewModels<IntroViewModel>()

    @Inject
    lateinit var appDataStore: AppDataStore

    private var alreadyLaunched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            setLocale(languageCode)
        }

        setContentView(binding.root)

        kotlin.runCatching {
            hideNavigation()
        }

        firebaseAnalytics?.logEvent("intro_scr_view", null)

        onBackPress()

        screen = intent.getIntExtra("screen", 0)

        introViewModel.initializing()

        runCatching {
            GlobalScope.launch(IO) {
                var newScreen = screen
                if (newScreen == 2) newScreen = -1
                appDataStore.writeIntroCounter(++newScreen)
            }
        }

        binding.apply {
//            when (getLayoutIntro(introScreenAdUi != "full_layout_admob")) {
//                com.example.ads.R.layout.layout_native_large_intro -> {
//                    addViewIntoShimmer(com.example.ads.R.layout.layout_native_large_intro)
//                    addViewIntoFrameLayout(com.example.ads.R.layout.layout_native_large_intro)
//                }

//                com.example.ads.R.layout.layout_native_large_button_above_intro -> {
            addViewIntoShimmer(com.example.ads.R.layout.layout_native_large_button_above_intro)
            addViewIntoFrameLayout(com.example.ads.R.layout.layout_native_large_button_above_intro)
//                }

//                com.example.ads.R.layout.meta_native_layout_intro -> {
//                    addViewIntoShimmer(com.example.ads.R.layout.meta_native_layout_intro)
//                    addViewIntoFrameLayout(com.example.ads.R.layout.meta_native_layout_intro)
//                }
//
//                com.example.ads.R.layout.meta_native_layout_intro_bottom -> {
//                    addViewIntoShimmer(com.example.ads.R.layout.meta_native_layout_intro_bottom)
//                    addViewIntoFrameLayout(com.example.ads.R.layout.meta_native_layout_intro_bottom)
//                }
//            }
        }

        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = this@IntroActivity)
            )
            setContent {
                when (screen) {
                    0 -> IntroView(introViewModel.screenOneContent(this@IntroActivity))
                    1 -> IntroView(introViewModel.screenTwoContent(this@IntroActivity))
                    else -> IntroView(introViewModel.screenThreeContent(this@IntroActivity))
                }
            }
        }
        loadAndShowNativeAd()
        kotlin.runCatching {
            if (introScreenUiButton == "light") {
                binding.continueTxt.isVisible = true
                binding.continueTxtWithoutBg.isVisible = false
                binding.continueTxt.apply {
                    setTextColor(setColor(com.project.common.R.color.selected_color))
                    backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#293A89FB"))
                }
            } else if (introScreenUiButton == "dark") {
                binding.continueTxt.isVisible = true
                binding.continueTxtWithoutBg.isVisible = false
            } else {
                binding.continueTxt.isVisible = false
                binding.flAdsNative.layoutParams.let {
                    if (it is ConstraintLayout.LayoutParams) {
                        it.bottomMargin = 0
                    }
                }

                binding.flAdsNativeNew.layoutParams.let {
                    if (it is ConstraintLayout.LayoutParams) {
                        it.bottomMargin = 0
                    }
                }

                binding.continueTxtWithoutBg.isVisible = true
            }
        }

        binding.apply {

            continueTxt.setOnSingleClickListener {
                navigateToMainOrPro()
            }

            continueTxtWithoutBg.setOnSingleClickListener {
                navigateToMainOrPro()
            }
        }
    }

    private fun addViewIntoShimmer(view: Int) {
        binding.apply {
            runCatching {
                val shimmerContainer = shimmerContainerNative
                val admobLayout = LayoutInflater.from(this@IntroActivity)
                    .inflate(view, shimmerContainer, false)
                admobLayout.visibility = View.INVISIBLE
                shimmerContainer.addView(admobLayout)
            }
        }
    }

    private fun addViewIntoFrameLayout(view: Int) {
        binding.apply {
            runCatching {
                val shimmerContainer = flAdsNativeNew
                val admobLayout = LayoutInflater.from(this@IntroActivity)
                    .inflate(view, shimmerContainer, false)
                admobLayout.visibility = View.INVISIBLE
                shimmerContainer.addView(admobLayout)
            }
        }
    }

    fun showAppOpen() {
        kotlin.runCatching {
            binding.flAdsNative.visibility = INVISIBLE
            binding.let {
                showAppOpen = true
                binding.flAdsNative.visibility = INVISIBLE
                //  hideOrShowAd(false)
                Handler(Looper.getMainLooper()).postDelayed({
                    showAppOpen {
                        binding.let {
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.flAdsNative.visibility = View.VISIBLE
                                // hideOrShowAd(true)
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

    var showAppOpen = false

    private fun loadAndShowNativeAd() {

        if (showAppOpen) {
            return
        }
        kotlin.runCatching {
            if (isProVersion()) {
                binding.flAdsNative.visibility = INVISIBLE
            } else {
                this.let { myActivity ->
                    binding.flAdsNative.visibility = View.VISIBLE

                    binding.let { binding ->
                        myActivity.loadAndShowNativeOnBoarding(
                            loadedAction = {
                                kotlin.runCatching {
                                    if (!isFinishing && !isDestroyed) {
                                        binding.flAdsNative.show()
                                        binding.shimmerContainerNative.visibility =
                                            INVISIBLE
                                        binding.flAdsNative.removeAllViews()
                                        if (it?.parent != null) {
                                            (it.parent as ViewGroup).removeView(it)
                                        }
                                        if (!isFinishing && !isDestroyed) {
                                            binding.flAdsNative.addView(it)
                                        }
                                    }
                                }
                            },
                            failedAction = {

                                binding.flAdsNative.visibility = INVISIBLE
                            },
                            myActivity.intro(), nextConfig = myActivity.intro()
                        )
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus)
            hideNavigation()
    }

    private fun navigateToMainOrPro() {

        firebaseAnalytics?.logEvent("intro_scr_click_continue", null)

        if (alreadyLaunched)
            return

        alreadyLaunched = true

        kotlin.runCatching {
            if (showBlendGuideScreen) {
                val intent = Intent(this, BlendOnBoardingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            } else if (!isProVersion() && showRoboPro) {

                openPro()
            } else {
                val intent = Intent()
                intent.setClassName(
                    applicationContext,
                    "com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity"
                )
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
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
                }
            }
        }
        callback?.let {
            onBackPressedDispatcher.addCallback(this, it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}