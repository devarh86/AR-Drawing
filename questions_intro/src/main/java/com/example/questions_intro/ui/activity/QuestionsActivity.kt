package com.example.questions_intro.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.showBlendGuideScreen
import com.example.ads.Constants.showRoboPro
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.question
import com.example.ads.utils.survey
import com.example.inapp.helpers.Constants.isProVersion
import com.example.questions_intro.R
import com.example.questions_intro.databinding.ActivityQuestionsBinding
import com.example.questions_intro.ui.fragment.QuestionOne
import com.project.common.repo.datastore.AppDataStore
import com.project.common.utils.getProScreen
import com.project.common.utils.hideNavigation
import com.project.common.utils.runCatchingWithLog
import com.project.common.utils.setLocale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuestionsActivity : AppCompatActivity() {

    private var navController: NavController? = null

    private val binding by lazy { ActivityQuestionsBinding.inflate(layoutInflater) }

    private var callback: OnBackPressedCallback? = null

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

        initNavListener()

        onBackPress()
    }

    private fun initNavListener() {
        kotlin.runCatching {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_questions) as? NavHostFragment
            navController = navHostFragment?.navController
            navController?.addOnDestinationChangedListener { _, destination, _ ->
                loadAndShowNativeAd()
            }
        }.onFailure { Log.e("error", "initNavListener: ", it) }
    }

    /*   fun loadAndShowNativeAd() {
           binding.apply {
              *//* aperoNativeQuestions(
                this@QuestionsActivity,
                flAdsNative,
                shimmerNativeAds.shimmerContainerNative
            )*//*
        }
    }*/

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus)
            hideNavigation()
    }

    var showAppOpen = false

    fun loadAndShowNativeAd() {
        if (showAppOpen) {
            return
        }
        kotlin.runCatching {
            if (isProVersion()) {
                binding?.flAdsNative?.visibility = INVISIBLE
            } else {
                this.let { myActivity ->
                    binding?.flAdsNative?.visibility = View.VISIBLE
                    binding?.let { binding ->
                        myActivity.loadAndShowNativeOnBoarding(
                            loadedAction = {
//                                _binding?.apply {
//                                    previousNativeAd?.let { prevAdView ->
//                                        it?.let { newAdView ->
//                                            if (prevAdView == newAdView && flAdsNative.isVisible && flAdsNative.childCount != 0 && flAdsNative.contains(
//                                                    prevAdView
//                                                )
//                                            ) {
//                                                return@loadAndShowNativeOnBoarding
//                                            }
//                                        }
//                                    }
//                                }

//                                previousNativeAd = it

                                kotlin.runCatching {
                                    if (!isFinishing && !isDestroyed && binding != null) {
                                        binding.flAdsNative.show()
                                        binding.shimmerContainerNative.visibility = INVISIBLE
                                        binding.flAdsNative.removeAllViews()
                                        if (it?.parent != null) {
                                            (it.parent as ViewGroup).removeView(it)
                                        }
                                        if (!isFinishing && !isDestroyed && binding != null) {
                                            binding.flAdsNative.addView(it)
                                        }
                                    }
                                }
                            },
                            failedAction = {
                                //binding.flAdsNative.visibility = INVISIBLE
                            },
                            myActivity.question(), nextConfig = myActivity.question()
                        )
                    }
                }
            }
        }
    }


    fun showAppOpen() {
        kotlin.runCatching {
            binding?.flAdsNative?.visibility = INVISIBLE
            binding.let {
                showAppOpen = true
                binding?.flAdsNative?.visibility = INVISIBLE
                //  hideOrShowAd(false)
                Handler(Looper.getMainLooper()).postDelayed({
                    showAppOpen {
                        binding.let {
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding?.flAdsNative?.visibility = View.VISIBLE
                                // hideOrShowAd(true)
                                showAppOpen = false
                                loadAndShowNativeAd()
                                loadAppOpen()
                            }, 800L)
                        }
                    }
                }, 600L)
            }
        }
    }


    fun visibleNativeAd() {
        binding.flAdsNative.visibility = View.VISIBLE
    }

    fun navigate(directions: NavDirections, currentId: Int) {
        try {
            if (findNavController(binding.navHostFragmentQuestions.id).currentDestination?.id == currentId) {
//                binding.flAdsNative.visibility = View.INVISIBLE
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build()
                findNavController(binding.navHostFragmentQuestions.id).navigate(
                    directions,
                    navOptions
                )
            }
        } catch (ex: Exception) {
            Log.e("error", "navigate: ", ex)
        }
    }


    fun navigateToIntroActivity() {

        if (alreadyLaunched)
            return

        alreadyLaunched = true

        kotlin.runCatching {
            GlobalScope.launch(IO) {
                appDataStore.writeQuestionComplete()
            }
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
        }.onFailure {
            alreadyLaunched = false
        }
    }

    fun skipToIntroActivity() {

        if (alreadyLaunched)
            return

        alreadyLaunched = true

        kotlin.runCatching {
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
                    //com.fahad.newtruelovebyfahad.ui.activities.main
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

    fun navigateBack() {
        try {
            findNavController(binding.navHostFragmentQuestions.id).popBackStack()
        } catch (ex: Exception) {
            Log.e("error", "navigate: ", ex)
        }
    }

    private fun onBackPress() {

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                runCatchingWithLog {
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_questions) as? NavHostFragment
                    navHostFragment?.childFragmentManager?.fragments?.get(0)?.let {
                        if (it is QuestionOne) {
                            finishAndRemoveTask()
                        } else {
                            navigateBack()
                        }
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
        // resetNative()
    }
}