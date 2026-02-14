package com.fahad.newtruelovebyfahad.ui.activities.pro

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.proScreenVariant
import com.example.ads.Constants.showOfferPanel
import com.example.ads.admobs.utils.onResumeBanner
import com.example.ads.crosspromo.helper.openUrl
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValue
import com.example.inapp.helpers.Constants.isProVersion
import com.example.inapp.helpers.showToast
import com.fahad.newtruelovebyfahad.MyApp
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.ProActivityExperiNewBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.pro.slider.SliderAdapter
import com.fahad.newtruelovebyfahad.ui.activities.pro.slider.SliderList.getImageList
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.project.common.utils.getNotchHeight
import com.project.common.utils.hideNavigation
import com.project.common.utils.privacyPolicy
import com.project.common.utils.setDrawable
import com.project.common.utils.setLocale
import com.project.common.utils.termOfUse
import com.project.common.viewmodels.DataStoreViewModel
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class ProActivityOld : AppCompatActivity() {

    private var binding: ProActivityExperiNewBinding? = null
    //    private var binding: ProActivityNewExperimentBinding? = null
    private var selectedPosition: Int = 1
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    private val slideHandler = Handler(Looper.getMainLooper())
    private var slideRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProActivityExperiNewBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding?.initViews()

        try {

            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@ProActivityOld as LifecycleOwner)
            }

            isProVersion.observe(this@ProActivityOld) {
                if (it) {
                    kotlin.runCatching {
                        if (!isFinishing && !isDestroyed) {
                            val showAd = intent.getBooleanExtra("show_ad", false)
                            backPress(showAd = showAd)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }
        hideNavigation()
        lifecycleScope.launch(IO) {
            delay(2000)
            withContext(Main) {
                binding?.let {
                    it.closeBtn.isVisible = true
                }
            }
        }
        kotlin.runCatching {
            setLocale(languageCode)
        }
    }


    override fun onResume() {
        super.onResume()
        slideRunnable?.let {
            slideHandler.postDelayed(
                it, 3000
            )
        }
        loadBannerAd()
    }

    private fun ProActivityExperiNewBinding.initSliderViewPager() {
        sliderPro.apply {
            binding?.let { binding ->

                adapter = SliderAdapter(getImageList(context)) { parent, editor ->
                    // selectCategory(parent, editor, "slider")
                }

                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 1
                getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER


                var lastSelected: ImageView = binding.dot1

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        lastSelected.setImageDrawable(context.setDrawable(com.project.common.R.drawable.s_unselected))

                        slideRunnable?.let {
                            slideHandler.removeCallbacks(it)
                            slideHandler.postDelayed(it, 2000)
                        }
                        when (position) {

                            0 -> {
                                binding.dot1.setImageDrawable(
                                    context.setDrawable(
                                        com.project.common.R.drawable.s_selected
                                    )
                                )
                                lastSelected = binding.dot1
                            }

                            1 -> {
                                binding.dot2.setImageDrawable(
                                    context.setDrawable(
                                        com.project.common.R.drawable.s_selected
                                    )
                                )
                                lastSelected = binding.dot2
                            }

                            2 -> {
                                binding.dot3.setImageDrawable(
                                    context.setDrawable(
                                        com.project.common.R.drawable.s_selected
                                    )
                                )
                                lastSelected = binding.dot3
                            }

                            3 -> {
                                binding.dot4.setImageDrawable(
                                    context.setDrawable(
                                        com.project.common.R.drawable.s_selected
                                    )
                                )
                                lastSelected = binding.dot4
                            }

                            4 -> {
                                binding.dot5.setImageDrawable(
                                    context.setDrawable(
                                        com.project.common.R.drawable.s_selected
                                    )
                                )
                                lastSelected = binding.dot5
                            }

                            5 -> {
                                binding.dot6.setImageDrawable(
                                    context.setDrawable(
                                        com.project.common.R.drawable.s_selected
                                    )
                                )
                                lastSelected = binding.dot6
                            }

                            else -> {
//                            binding.dot7.setImageDrawable(
//                                context.setDrawable(
//                                    R.drawable.selected
//                                )
//                            )
//                            lastSelected = binding.dot7
                            }

                        }

                    }
                })
            }
        }
        slideRunnable = Runnable {
            binding?.sliderPro?.adapter?.let {
                val currentItem = sliderPro.currentItem
                val nextItem = if (currentItem == it.itemCount - 1) 0 else currentItem + 1
                sliderPro.setCurrentItem(nextItem, nextItem != 0)
            }
        }
    }

    private fun loadBannerAd() {
        kotlin.runCatching {
            binding?.let {binding->
                if (!isProVersion()) {
                    binding.bannerContainer.visibility = View.VISIBLE
                    onResumeBanner(
                        binding.adBannerContainer,
                        binding.crossBannerIv,
                        binding.bannerLayout.adContainer,
                        binding.bannerLayout.shimmerViewContainer,
                    )
                } else {
                    try {
                        if (isProVersion())
                            binding.bannerContainer.visibility = View.GONE
                        else {
                            binding.bannerContainer.visibility = View.INVISIBLE
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.e("error", "onResume: ", ex)
                    }
                }
            }

        }
    }

    override fun onPause() {
        super.onPause()
        slideRunnable?.let { slideHandler.removeCallbacks(it) }
    }



    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    /* override fun onWindowFocusChanged(hasFocus: Boolean) {
         if (!hasFocus) {
             hideStatusAndNavigationBar()
         }
     }*/



    @SuppressLint("SetTextI18n")
    private fun ProActivityExperiNewBinding.initViews() {
//    private fun ProActivityNewExperimentBinding.initViews() {

//        if (proScreenVariant == 0L) {
//            binding?.monthlyContainer?.visibility = View.INVISIBLE
//            binding?.weeklyContainer?.isVisible = true
//            monthlyPlanHeading.visibility = View.INVISIBLE
//            selectedPosition = 2
//        } else {
//            binding?.monthlyContainer?.isVisible = true
//            monthlyPlanHeading.isVisible = true
//            binding?.weeklyContainer?.isVisible = false
//            selectedPosition = 1
//        }

        val showAd = intent.getBooleanExtra("show_ad", false)

        if(showAd) {
            dataStoreViewModel.initFun()
            lifecycleScope.launch(IO) {
                dataStoreViewModel.updateIntroComplete()
            }
        }
        val fromFrames = intent.getBooleanExtra("from_frames", true)

        val bundle = Bundle().apply {
            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
            if (!showAd && fromFrames)
                putString(Events.ParamsKeys.FROM, "frames")
            else {
                if (!showAd) {
                    putString(Events.ParamsKeys.FROM, "btn")
                }
            }
        }
        firebaseAnalytics?.logEvent(
            Events.Screens.PREMIUM,
            bundle
        )

        Log.i(
            "firebase_events_clicks",
            "events: screenName: ${Events.Screens.PREMIUM} bundle:  $bundle"
        )

        firebaseAnalytics?.logEvent("premium_screen_open", null)
        com.example.ads.Constants.firebaseAnalytics?.logEvent("premium_screen_open", null)
        initSliderViewPager()

        closeBtn.layoutParams?.let {
            if (it is ConstraintLayout.LayoutParams) {
                it.updateMargins(0, getNotchHeight(), 0, 0)
            }
        }
//        proScreenVariant = 1

        val price = if (proScreenVariant != 0L) {
            getProductDetailMicroValue(SKU_LIST[0])?.let {

                getProductDetailMicroValue(SKU_LIST[2])?.let {
                    weeklyPlanPrice.text = "${it.second} ${it.first}"
                    try {
                        it.first.toDouble()
                    } catch (ex: java.lang.Exception) {
                        0.0
                    }
                } ?: 0.0

                monthlyPlanPrice.text = "${it.second} ${it.first}"
                try {
//                    if (!haveTrial) {
//                        text.text = "Continue"
//                    }
                    it.first.toDouble()
                } catch (ex: java.lang.Exception) {
                    0.0
                }
            } ?: 0.0
        } else {
            getProductDetailMicroValue(SKU_LIST[2])?.let {

                getProductDetailMicroValue(SKU_LIST[0])?.let {
                    monthlyPlanPrice.text = "${it.second} ${it.first}"
                    try {
//                    if (!haveTrial) {
//                        text.text = "Continue"
//                    }
                        it.first.toDouble()
                    } catch (ex: java.lang.Exception) {
                        0.0
                    }
                } ?: 0.0

                weeklyPlanPrice.text = "${it.second} ${it.first}"
                try {
                    it.first.toDouble()
                } catch (ex: java.lang.Exception) {
                    0.0
                }
            } ?: 0.0
        }

        getProductDetailMicroValue(SKU_LIST[1])?.let {

            kotlin.runCatching {
                val totalPrice = if (proScreenVariant != 0L) {
                    price * 12
                } else {
                    price * 52
                }
                val remainingPrice = totalPrice - it.first.toDouble()
                val saving = remainingPrice.times(100) / totalPrice
                if (saving > 0) {
                    yearlyPlanHeading.isVisible = true
                    yearlyPlanHeading.text =
                        "Save ".plus(abs(saving.roundToInt()).toString()).plus("%")
                }

//                if (!haveTrial) {
//                    text.text = "Continue"
//                }
            }
            yearlyPlanPrice.text = "${it.second} ${it.first}"
        }

//        if (proScreenVariant == 0L) {
        setSelection(0)
        //  setSelection(2)
//        } else {
//            binding?.apply {
//                proSelectionModel = ProSelectionModel(
//                    yearlyContainer,
//                    yearlyPlanPrice,
//                    null,
//                    yearlyPlanHeading
//                )
//            } // reset first yearly container
//            setSelection(1)
//        }

        initListeners()

//        kotlin.runCatching {
//            if (!lottieRenderModeAutomatic) {
//                binding?.animationPro?.renderMode = RenderMode.HARDWARE
//            } else {
//                binding?.animationPro?.renderMode = RenderMode.AUTOMATIC
//            }
//        }

//        binding?.animationPro?.playAnimation()
    }

    private var backpress = false

    private fun backPress(showAd: Boolean) {

        runCatching {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.ProScreen.CLOSE)
                })
            if (showAd) {
                runCatching {
                    if (showOfferPanel) {
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
                        val intent = Intent(
                            applicationContext,
                            OfferPanelActivity::class.java
                        )
                        intent.putExtra("show_ad", true)
                        startActivity(intent)
                        finish()
                    } else {
//                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        if (!isDestroyed && !isFinishing) {
                            kotlin.runCatching {
                                binding?.statusBarView?.isVisible = true
                            }
                        }
                        kotlin.runCatching {
                            val intent =
                                Intent(this@ProActivityOld, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
//                        showInterstitial(
//                            loadedAction = {
//
//                            },
//                            failedAction = {
//                                kotlin.runCatching {
//                                    val intent =
//                                        Intent(this@ProActivityOld, MainActivity::class.java)
//                                    startActivity(intent)
//                                    finish()
//                                }
//                            },
//                            showAd = com.example.ads.Constants.roboProShowAd,
//                            onCheck = true
//                        )
                    }
                }
            } else {
                kotlin.runCatching {
                    if (showOfferPanel) {
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
                        val intent = Intent(
                            applicationContext,
                            OfferPanelActivity::class.java
                        )
                        intent.putExtra("show_ad", false)
                        startActivity(intent)
                        finish()
                    } else {
                        finish()
                    }
                }
            }
        }
    }

    private fun ProActivityExperiNewBinding.initListeners() {
//    private fun ProActivityNewExperimentBinding.initListeners() {

        val showAd = intent.getBooleanExtra("show_ad", false)

        lifecycleScope.launch(Main) {
            delay(2000)
            binding?.closeBtn?.isVisible = true
        }

        closeBtn.setSingleClickListener {
            kotlin.runCatching {
                if (!backpress) {
                    backpress = true
                    backPress(showAd)
                }
            }
        }

//        if (proScreenVariant != 0L) {
        monthlyContainer.setSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(1)
            }
        }
//        } else {
        weeklyContainer.setSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(0)
            }
        }
//        }

        yearlyContainer.setSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(2)
            }
        }

        continueBtn.setSingleClickListener {

            if (!this@ProActivityOld.isNetworkAvailable()) {

                kotlin.runCatching {
                    Toast.makeText(
                        this@ProActivityOld,
                        "Please connect to internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setSingleClickListener
            }

            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.ProScreen.CONTINUE_PURCHASE
                    )
                })
            Log.i("MyFirebaseEvent", "setSelection: CONTINUE_PURCHASE")

            when (selectedPosition) {
                0 -> {
                    (application as MyApp).billing.subscribe(
                        this@ProActivityOld,
                        SKU_LIST[2]
                    )

                    firebaseAnalytics?.logEvent("weekly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "weekly_sub_panel_open")

                }

                1 -> {
                    (application as MyApp).billing.subscribe(
                        this@ProActivityOld,
                        SKU_LIST[0]
                    )
                    firebaseAnalytics?.logEvent("monthly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "monthly_sub_panel_open")

                }

                2 -> {
                    (application as MyApp).billing.subscribe(
                        this@ProActivityOld,
                        SKU_LIST[1]
                    )
                    firebaseAnalytics?.logEvent("yearly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "yearly_sub_panel_open")
                }
            }
        }
        privacyPolicy.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.PRIVACY_POLICY
                    )
                })
            this@ProActivityOld.privacyPolicy()
        }
        termOfUse.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.TERM_OF_USE)
                })
            this@ProActivityOld.termOfUse()
        }
        cancelPro.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.ProScreen.CANCEL_PRO)
                })
            openPlayStoreAccount()
        }

        customTextView(cancelTxt)
    }

    private fun customTextView(view: TextView) {
        try {
            val spanTxt = SpannableStringBuilder()
            spanTxt.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this,
                        com.project.common.R.color.sub_heading_txt
                    )
                ),
                0,
                spanTxt.length,
                0
            )
            /*spanTxt.append(
                "On completion, trial will automatically convert to subscription.\n" +
                        "Subscription auto renews. Cancel anytime on "
            )*/
            spanTxt.append(
                "Subscription auto renews. Cancel anytime on "
            )
            spanTxt.append("Google Play Store")
            spanTxt.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    firebaseAnalytics?.logEvent(
                        Events.Screens.PREMIUM,
                        Bundle().apply {
                            putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                            putString(
                                Events.ParamsKeys.BUTTON,
                                Events.ParamsValues.ProScreen.GOOGLE_PLAY_STORE
                            )
                        })
                    openUrl("https://play.google.com/store/account/subscriptions".toUri())
                }
            }, spanTxt.length - "Google Play Store".length, spanTxt.length, 0)
//        spanTxt.append(".")
            view.movementMethod = LinkMovementMethod.getInstance()
            view.setText(spanTxt, TextView.BufferType.SPANNABLE)
        } catch (ex: Exception) {
            Log.e("error", "customTextView: ", ex)
        }
    }

    private fun openPlayStoreAccount() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions")
                )
            )
        } catch (e: ActivityNotFoundException) {
            showToast("Cant open the browser")
            e.printStackTrace()
        }
    }

    private var proSelectionModel: ProSelectionModel? = null

    private fun setSelection(index: Int) {

        selectedPosition = index

        proSelectionModel?.resetSelection()

        when (index) {
            0 -> {

                firebaseAnalytics?.logEvent(
                    Events.Screens.PREMIUM,
                    Bundle().apply {
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                        putString(
                            Events.ParamsKeys.BUTTON,
                            Events.ParamsValues.ProScreen.QUATERLY_PLAN
                        )
                    })

                Log.i("MyFirebaseEvent", "setSelection: QUATERLY_PLAN")

                binding?.apply {
                    proSelectionModel = ProSelectionModel(
                        weeklyContainer,
                        weeklyPlanPrice,
                        weeklyPlanHeading,
                        null,
                        checkOne
                    )
                    text.text = "Continue"
                    trialTxt.text = ""
                }
            }

            1 -> {

                firebaseAnalytics?.logEvent(
                    Events.Screens.PREMIUM,
                    Bundle().apply {
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                        putString(
                            Events.ParamsKeys.BUTTON,
                            Events.ParamsValues.ProScreen.MONTHLY_PLAN
                        )
                    })


                Log.i("MyFirebaseEvent", "setSelection: MONTHLY_PLAN")

                binding?.apply {
                    proSelectionModel = ProSelectionModel(
                        monthlyContainer,
                        monthlyPlanPrice,
                        null,
                        monthlyPlanHeading,
                        checkTwo
                    )
//                    if (haveTrial)
//                    text.text = "Start 3 Day Free Trial"
//                    kotlin.runCatching {
//                        trialTxt.text =
//                            "Try 3 days for 0.000.00, then ${monthlyPlanPrice.text}/month"
//                    }

                    text.text = "Continue"
                    trialTxt.text = ""
                }
            }

            2 -> {

                firebaseAnalytics?.logEvent(
                    Events.Screens.PREMIUM,
                    Bundle().apply {
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                        putString(
                            Events.ParamsKeys.BUTTON,
                            Events.ParamsValues.ProScreen.YEARLY_PLAN
                        )
                    })

                Log.i("MyFirebaseEvent", "setSelection: YEARLY_PLAN")

                binding?.apply {
                    proSelectionModel = ProSelectionModel(
                        yearlyContainer,
                        yearlyPlanPrice,
                        null,
                        yearlyPlanHeading,
                        checkThree
                    )
//                    if (haveTrial)
//                    text.text = "Start 7 Day Free Trial"
//                    kotlin.runCatching {
//                        trialTxt.text =
//                            "Try 7 days for 0.000.00, then ${yearlyPlanPrice.text}/year"
//                    }
                    text.text = "Continue"
                    trialTxt.text = ""
                }
            }
        }

        proSelectionModel?.setSelection()
    }

    private fun ProSelectionModel?.setSelection() {
        this?.let {
            runCatching {
                val layout = it.linearLayout
                if (layout is LinearLayout) {
                    layout.background = ContextCompat.getDrawable(this@ProActivityOld, com.project.common.R.drawable.border_selected_pro)
                } else if (layout is ConstraintLayout) {
                    layout.background = ContextCompat.getDrawable(this@ProActivityOld, com.project.common.R.drawable.border_selected_pro)
                }

                it.bestValueTxtView?.let {

                    it.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ProActivityOld,
                            com.project.common.R.color.selected_color
                        )
                    )
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProActivityOld,
                            R.color.white
                        )
                    )
                }
                it.circleImage?.let {
                    it.setImageDrawable(ContextCompat.getDrawable(
                        this@ProActivityOld,R.drawable.s_fill_check))
                }
            }
        }
    }

    private fun ProSelectionModel?.resetSelection() {
        this?.let {
            runCatching {
                val layout = it.linearLayout

                if (layout is LinearLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProActivityOld,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                } else if (layout is ConstraintLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProActivityOld,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                }

                it.bestValueTxtView?.let {

                    it.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ProActivityOld,
                            com.project.common.R.color.tab_txt_clr
                        )
                    )
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProActivityOld,
                            com.project.common.R.color.drawer_surface_clr
                        )
                    )
                }

                it.circleImage?.let {
                    it.setImageDrawable(ContextCompat.getDrawable(
                        this@ProActivityOld, com.project.common.R.drawable.s_check))
                }
            }
        }
    }

    override fun onBackPressed() {
        kotlin.runCatching {
            if (!backpress) {
                backpress = true
                val showAd = intent.getBooleanExtra("show_ad", false)
                backPress(showAd)
            }
            return
        }
        super.onBackPressed()
    }

    private var lastClickTime: Long = 0
    fun View.setSingleClickListener(delayTimeInSeconds: Int = 1, action: () -> Unit) {
        setOnSingleClickListener {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastClickTime) >= delayTimeInSeconds.seconds.inWholeMilliseconds / 2) {
                lastClickTime = currentTime
                action.invoke()
            }
        }
    }
}

data class ProSelectionModel(
    var linearLayout: Any,
    var priceTxtView: TextView,
    var planTxtView: TextView?,
    var bestValueTxtView: TextView?,
    var circleImage: ImageView?,
)