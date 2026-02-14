package com.abdul.pencil_sketch.main.activity

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.ActivityPencilSketchBinding
import com.abdul.pencil_sketch.main.fragment.PencilSketchRequest
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.example.ads.Constants
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.loadRewarded
import com.example.ads.admobs.utils.onPauseBanner
import com.example.ads.admobs.utils.onResumeBanner
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.galleryBottom
import com.example.ads.utils.interstitialBack
import com.project.common.utils.getColorWithSafetyCheck
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.utils.setStatusAndNavigationLight
import com.project.common.utils.setStatusBarNavBarColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PencilSketchActivity : AppCompatActivity() {

    private var _binding: ActivityPencilSketchBinding? = null
    private val binding get() = _binding!!

    private val restoreViewModel: PencilSketchViewModel by viewModels()
    private var navHostFragment: NavHostFragment? = null
    private var navController: NavController? = null

    private var isGallery = false
    private var isEnhanceRequest = false
    var showAppOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            setLocale(Constants.languageCode)
        }
        _binding = ActivityPencilSketchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState != null) {
            // The app was killed, restart it
            runCatching {
                finish() // Close the current activity to avoid overlap
            }
        }

        restoreViewModel.initViewModel()
        loadNewInterstitial(interstitialBack()) {}
        runCatching {
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        }

        try {
            if (com.example.inapp.helpers.Constants.isProVersion.hasObservers()) {
                com.example.inapp.helpers.Constants.isProVersion.removeObservers(this)
            }
            com.example.inapp.helpers.Constants.isProVersion.observe(this) {
                if (it) {
                    _binding?.bannerContainer?.visibility = View.GONE
                    _binding?.nativeContainer?.visibility = View.GONE
                }
            }
        } catch (_: Exception) {
        }
        runCatching {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
            navController = navHostFragment?.navController

            navController?.addOnDestinationChangedListener { _, name, _ ->
                if (name.id != R.id.basePencilSketch && name.id != R.id.pencilSketchRequest) {
                    isEnhanceRequest = false
                    if (R.id.galleryPencilSketch == name.id && Constants.adSelectPhoto == "native") {
                        _binding?.bannerContainer?.isVisible = false
                        isGallery = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!isDestroyed && !isFinishing)
                                loadAndShowNativeAd()
                        }, 220)

                    } else {
                        isEnhanceRequest = true
                        _binding?.nativeContainer?.isVisible = false
                        isGallery = false
                        loadBannerAd()
                    }
                } else {
                    isEnhanceRequest = false
                    isGallery = false
                    binding.bannerContainer.visibility = View.GONE
                    binding.nativeContainer.visibility = View.GONE
                }

            }

        }

        loadRewarded(loadedAction = {}, failedAction = {})
        hideNavigation()

    }

    private fun getCurrentFragmentForAdVisibility(showAd: Boolean) {
        runCatching {
            navHostFragment?.let { it ->
                val currentFragment = it.childFragmentManager.fragments.firstOrNull()
                currentFragment?.let {
                    if (it is PencilSketchRequest) {
                        it.hideOrShowAd(showAd)
                        binding.bannerContainer.isVisible = false
                    }
                }
            }
        }
    }

    fun loadAndShowNativeAd(fromReload: Boolean = false) {
        if (showAppOpen) {
            return
        }
        runCatching parentRunCatch@{

            _binding?.parentLayout?.apply {
                runCatching {
                    if (!isVisible && tag.toString().isNotBlank() && tag == "from_progress") {
                        return@parentRunCatch
                    }
                }
            }

            _binding?.let { binding ->

                if (com.example.inapp.helpers.Constants.isProVersion() || Constants.adSelectPhoto != "native") {
                    binding.nativeContainer.hide()
                } else {
                    this.let { myActivity ->

                        if (!isGallery) {
                            binding.nativeContainer.visibility = View.GONE
                            return@parentRunCatch
                        } else {
                            binding.nativeContainer.visibility = View.VISIBLE
                            binding.smallNativeLayout.shimmerViewContainer.startShimmer()
                        }
                        myActivity.loadAndShowNativeOnBoarding(
                            loadedAction = {
                                runCatching {
                                    if (!isFinishing && !isDestroyed) {
                                        _binding?.let { binding ->
                                            if (!isGallery) {
                                                binding.nativeContainer.hide()
                                                return@loadAndShowNativeOnBoarding
                                            }
                                            _binding?.bannerContainer?.hide()
                                            binding.nativeContainer.show()
                                            binding.smallNativeLayout.shimmerViewContainer.visibility =
                                                View.INVISIBLE
                                            binding.nativeContainer.removeAllViews()
                                            if (it?.parent != null) {
                                                (it.parent as ViewGroup).removeView(it)
                                            }
                                            if (!isFinishing && !isDestroyed) {
                                                binding.nativeContainer.addView(it)
                                            }
                                        }
                                    }
                                }
                            },
                            failedAction = {
                                _binding?.bannerContainer?.hide()
                                _binding?.nativeContainer?.hide()
                            },
                            myActivity.galleryBottom(),
                            nextConfig = myActivity.galleryBottom(),
                            fromReload
                        )
                    }
                }
            }
        }
    }

    fun showAppOpenAd() {
        binding.let {

            adLogic(true)

            runCatching {
                getCurrentFragmentForAdVisibility(false)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                showAppOpen {
                    binding.let {
                        Handler(Looper.getMainLooper()).postDelayed({

                            runCatching {
                                getCurrentFragmentForAdVisibility(true)
                            }
                            loadAppOpen()
                            showAppOpen = false
                            adLogic(false)
                        }, 800L)
                    }
                }
            }, 600L)
        }
    }

    private fun loadBannerAd() {
        runCatching parentRunCatch@{
            _binding?.parentLayout?.apply {
                runCatching {
                    if (!isVisible && tag.toString().isNotBlank() && tag == "from_progress") {
                        return@parentRunCatch
                    }
                }
            }
            if (!showAppOpen && !com.example.inapp.helpers.Constants.isProVersion()) {
                _binding?.bannerContainer?.visibility = View.VISIBLE
                _binding?.let { binding ->
                    onResumeBanner(
                        binding.adBannerContainer,
                        binding.crossBannerIv,
                        binding.bannerLayout.adContainer,
                        binding.bannerLayout.shimmerViewContainer,
//                    fromEditor = true
                    )
                }
            } else {
                try {
                    if (com.example.inapp.helpers.Constants.isProVersion())
                        _binding?.bannerContainer?.visibility = View.GONE
                    else {
                        _binding?.bannerContainer?.visibility = View.INVISIBLE
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e("error", "onResume: ", ex)
                }
            }
        }
    }

    private fun adLogic(hide: Boolean) {
        if (hide) {
            if (isGallery && Constants.adSelectPhoto == "native") {
                _binding?.nativeContainer?.visibility = View.INVISIBLE
            } else {
                _binding?.bannerContainer?.visibility = View.INVISIBLE
            }
            return
        } else {
            if (isGallery && Constants.adSelectPhoto == "native") {
                loadAndShowNativeAd()
            } else {
                if (!isEnhanceRequest) {
                    loadBannerAd()
                } else {
                    _binding?.bannerContainer?.hide()
                }
                /* navHostFragment?.let { it ->
                     val currentFragment = it.childFragmentManager.fragments.firstOrNull()
                     currentFragment?.let {
                         if (it is RestoreImageRequest) {
                             binding.bannerContainer.visibility = View.GONE
                             it.hideOrShowAd(hide)
                         }else{
                             if (!isEnhanceRequest) {
                                 loadBannerAd()
                             } else {
                                 _binding?.bannerContainer?.hide()
                             }
                         }
                     }
                 }*/

            }
        }
    }

    fun hideAd() {
        if (!com.example.inapp.helpers.Constants.isProVersion()) {
            _binding?.parentLayout?.hide()
            _binding?.parentLayout?.tag = "from_progress"
        }
    }

    fun showAd() {
        if (!com.example.inapp.helpers.Constants.isProVersion()) {
            _binding?.parentLayout?.show()
            _binding?.parentLayout?.tag = ""
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        runCatching {
            setLocale(Constants.languageCode)
        }

        runCatching {

            setStatusBarNavBarColor(com.project.common.R.drawable.status_top_white)

            val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    setStatusAndNavigationLight(true)
                }

                Configuration.UI_MODE_NIGHT_YES -> {
                    setStatusAndNavigationLight(false)
                }
            }
            if (_binding != null) {
                binding.mainRoot.setBackgroundColor(this.getColorWithSafetyCheck(com.project.common.R.color.surface_clr))
            }
        }
    }


    override fun onPause() {
        super.onPause()
        onPauseBanner()
    }

    fun navigate(directions: NavDirections, currentId: Int) {
        try {
            if (findNavController(binding.navHostFragment.id).currentDestination?.id == currentId) {
                findNavController(binding.navHostFragment.id).navigate(directions)
            }
        } catch (ex: Exception) {
            Log.e("error", "navigate: ", ex)
        }
    }

    fun navigate(directions: Int, currentId: Int, bundle: Bundle) {

        try {
            if (findNavController(binding.navHostFragment.id).currentDestination?.id == currentId) {
                findNavController(binding.navHostFragment.id).navigate(directions, bundle)
            }
        } catch (ex: Exception) {
            Log.e("error", "navigate: ", ex)
        }
    }

}