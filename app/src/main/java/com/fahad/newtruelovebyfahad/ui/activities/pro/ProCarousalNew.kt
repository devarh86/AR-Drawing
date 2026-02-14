package com.fahad.newtruelovebyfahad.ui.activities.pro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.RenderEffect.createBlurEffect
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.ads.Constants.introScreen
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.proSplashOrHome
import com.example.ads.Constants.showOfferPanel
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.openUrl
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValueNew
import com.example.inapp.helpers.Constants.isProVersion
import com.example.inapp.helpers.showToast
import com.fahad.newtruelovebyfahad.MyApp
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.ActivityProCarousalNewBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.utils.hideNavigation
import com.project.common.utils.privacyPolicy
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSaveSingleClickListener
import com.project.common.utils.setString
import com.project.common.utils.termOfUse
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ProCarousalNew : AppCompatActivity() {
    private val binding by lazy {
        ActivityProCarousalNewBinding.inflate(layoutInflater)
    }
    private var selectedPosition: Int = 1
    private var showIntroScreens = false
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.initViews()
        try {

            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@ProCarousalNew as LifecycleOwner)
            }

            isProVersion.observe(this@ProCarousalNew) {
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

        //  loadNewInterstitial(inAppInterstitialAD()) {}

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    private fun ActivityProCarousalNewBinding.initViews() {
        val showAd = intent.getBooleanExtra("show_ad", false)
        if (showAd) {
            dataStoreViewModel.initFun()
//            lifecycleScope.launch(IO) {
//                dataStoreViewModel.updateIntroComplete()
//            }
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

        getProductDetailMicroValueNew(SKU_LIST[2])?.let { obj ->
            offerPriceWek.text = "${obj.currency} ${obj.price}"
            kotlin.runCatching {
                if (obj.currency.isEmpty()) {
                    weeklyGrabOffer.isClickable = false
                    return@let
                } else {
                    weeklyGrabOffer.isClickable = true
                }
            }
        } ?: 0.0


        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
            subheadingPrice.text = "${obj.currency} ${obj.price}"
            kotlin.runCatching {
                if (obj.currency.isEmpty()) {
                    heading.isClickable = false
                    return@let
                } else {
                    heading.isClickable = true
                }

                val remoteString: String = if (obj.isTrailActive) {
                    "Try for Free"
                } else {
                    setString(com.project.common.R.string.continue_text)
                }

                if (remoteString.isNotBlank() && !remoteString.contains(
                        setString(com.project.common.R.string.continue_text),
                        true
                    )
                ) {
                    kotlin.runCatching {
                        trialText.visible()
                        heading.text = remoteString
                    }
                } else {
                    kotlin.runCatching {
                        trialText.hide()
                        heading.text = remoteString
                    }
                }
            }
        } ?: 0.0

        getProductDetailMicroValueNew(SKU_LIST[3])?.let { obj ->
            subheadingPriceMonth.text = "${obj.currency} ${obj.price}"

            kotlin.runCatching {
                if (obj.currency.isEmpty()) {
                    heading.isClickable = false
                    return@let
                } else {
                    heading.isClickable = true
                }

                val remoteString: String = if (obj.isTrailActive) {
                    "Try for Free"
                } else {
                    setString(com.project.common.R.string.continue_text)
                }

                if (remoteString.isNotBlank() && !remoteString.contains(
                        setString(com.project.common.R.string.continue_text),
                        true
                    )
                ) {
                    kotlin.runCatching {
                        trialTextMonth.visible()
                        heading.text = remoteString
                    }
                } else {
                    kotlin.runCatching {
                        trialTextMonth.hide()
                        heading.text = remoteString
                    }
                }
            }


        }
        getProductDetailMicroValueNew(SKU_LIST[4])?.let { obj ->
            subheadingPriceYearly.text = "${obj.currency} ${obj.price}"
            kotlin.runCatching {
                if (obj.currency.isEmpty()) {
                    heading.isClickable = false
                    return@let
                } else {
                    heading.isClickable = true
                }

                val remoteString: String = if (obj.isTrailActive) {
                    "Try for Free"
                } else {
                    setString(com.project.common.R.string.continue_text)
                }

                if (remoteString.isNotBlank() && !remoteString.contains(
                        setString(com.project.common.R.string.continue_text),
                        true
                    )
                ) {
                    kotlin.runCatching {
                        trialTextYearly.visible()
                        heading.text = remoteString
                    }
                } else {
                    kotlin.runCatching {
                        trialTextYearly.hide()
                        heading.text = remoteString
                    }
                }
            }
        }

        setSelection(0)
        initListeners()
    }

    private var backpress = false

    private fun backPress(showAd: Boolean) {
        // showNewInterstitial(inAppInterstitialAD()) {
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
                        removerRenderEffect()
                        finish()
                    } else {
                        Log.i("LANG_CHECK_INTRO", "PRO_ACTIVITY:${showIntroScreens}")
                        if (introScreen && proSplashOrHome && showIntroScreens) {
                            kotlin.runCatching {
                                val intent = Intent(
                                    applicationContext,
                                    com.example.apponboarding.ui.main.activity.LanguageActivity::class.java
                                )
                                startActivity(intent)
                                removerRenderEffect()
                                finish()

                            }
                        } else {
                            //  showNewInterstitial(overAllInterstitialAD()) {
                            // loadNewInterstitial(overAllInterstitialAD()) {}

                            kotlin.runCatching {
                                val intent =
                                    Intent(this@ProCarousalNew, MainActivity::class.java)
                                startActivity(intent)
                                removerRenderEffect()
                                finish()
                            }
                        }

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
                        removerRenderEffect()
                        finish()
                    } else {
                        removerRenderEffect()
                        finish()
                    }
                }
            }
        }
        // }
    }

    private fun removerRenderEffect() {
        binding.weekLayout.isVisible = false
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding.rootLay.setRenderEffect(null)
            }
        }
    }

    private fun ActivityProCarousalNewBinding.initListeners() {

        val showAd = intent.getBooleanExtra("show_ad", false)
        showIntroScreens = intent.getBooleanExtra("is_intro_complete", false)
        Log.i(
            "LANG_CHECK_INTRO",
            "PRO_ACTIVITY_CREATE:${showIntroScreens} received: ${
                intent.getBooleanExtra(
                    "is_intro_complete",
                    false
                )
            }"
        )

        lifecycleScope.launch(Main) {
            delay(2000)
            binding?.closeBtn?.isVisible = true
        }

        closeBtn.setOnSaveSingleClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurRadius = 15f
                val blurEffect = createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.CLAMP
                )

                binding.rootLay.setRenderEffect(blurEffect)
            }
            binding.offerWeekly.isVisible = true


        }

        crossOffer.setOnSaveSingleClickListener {
            kotlin.runCatching {
                if (!backpress) {
                    backpress = true
                    backPress(showAd)
                }
            }
        }

        monthLayout.setOnSaveSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(1)
            }
        }

        weekLayout.setOnSaveSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(0)
            }
        }

        yearLayout.setOnSaveSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(2)
            }
        }
        weeklyGrabOffer.setOnSaveSingleClickListener {
            if (!this@ProCarousalNew.isNetworkAvailable()) {

                kotlin.runCatching {
                    Toast.makeText(
                        this@ProCarousalNew,
                        "Please connect to internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnSaveSingleClickListener
            }

            (application as MyApp).billing.subscribe(
                this@ProCarousalNew,
                SKU_LIST[2]
            )

            firebaseAnalytics?.logEvent("weekly_offer_panel_open", null)
            Log.i("MyFirebaseEvent", "weekly_sub_panel_open")
        }

        heading.setOnSaveSingleClickListener {

            if (!this@ProCarousalNew.isNetworkAvailable()) {

                kotlin.runCatching {
                    Toast.makeText(
                        this@ProCarousalNew,
                        "Please connect to internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnSaveSingleClickListener
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
                        this@ProCarousalNew,
                        SKU_LIST[5]
                    )

                    firebaseAnalytics?.logEvent("weekly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "weekly_sub_panel_open")

                }

                1 -> {
                    (application as MyApp).billing.subscribe(
                        this@ProCarousalNew,
                        SKU_LIST[3]
                    )
                    /* (application as MyApp).billing.subscribe(
                         this@ProCarousalNew,
                         SKU_LIST[0]
                     )*/
                    firebaseAnalytics?.logEvent("monthly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "monthly_sub_panel_open")

                }

                2 -> {
                    (application as MyApp).billing.subscribe(
                        this@ProCarousalNew,
                        SKU_LIST[4]
                    )
                    firebaseAnalytics?.logEvent("yearly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "yearly_sub_panel_open")
                }
            }
        }
        privacyPolicy.setOnSaveSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.PRIVACY_POLICY
                    )
                })
            this@ProCarousalNew.privacyPolicy()
        }
        termOfUse.setOnSaveSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.TERM_OF_USE)
                })
            this@ProCarousalNew.termOfUse()
        }
        cancelPro.setOnSaveSingleClickListener {
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
                        weekLayout,
                        subheadingPrice,
                        null,
                        null,
                        null
                    )

                    runCatching {
                        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
                            kotlin.runCatching {

                                kotlin.runCatching {


                                    val remoteString: String = if (obj.isTrailActive) {
                                        "Try for Free"
                                    } else {
                                        setString(com.project.common.R.string.continue_text)
                                    }

                                    if (remoteString.isNotBlank() && !remoteString.contains(
                                            setString(com.project.common.R.string.continue_text),
                                            true
                                        )
                                    ) {
                                        kotlin.runCatching {
                                            trialText.visible()
                                            heading.text = remoteString
                                        }
                                    } else {
                                        kotlin.runCatching {
                                            trialText.hide()
                                            heading.text = remoteString
                                        }
                                    }
                                }


                            }
                        }
                    }


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

                binding.apply {

                    proSelectionModel = ProSelectionModel(
                        monthLayout,
                        subheadingPriceMonth,
                        null,
                        null,
                        null
                    )
                    //  heading.text = "Try Now"
                    //trialTxt.text = ""
                    runCatching {
                        getProductDetailMicroValueNew(SKU_LIST[3])?.let { obj ->
                            val remoteString: String = if (obj.isTrailActive) {
                                "Try for Free"
                            } else {
                                setString(com.project.common.R.string.continue_text)
                            }

                            if (remoteString.isNotBlank() && !remoteString.contains(
                                    setString(com.project.common.R.string.continue_text),
                                    true
                                )
                            ) {
                                kotlin.runCatching {
                                    trialTextMonth.visible()
                                    heading.text = remoteString
                                }
                            } else {
                                kotlin.runCatching {
                                    trialTextMonth.hide()
                                    heading.text = remoteString
                                }
                            }
                        }
                    }
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

                binding.apply {
                    proSelectionModel = ProSelectionModel(
                        yearLayout,
                        subheadingPriceYearly,
                        null,
                        null,
                        null
                    )
                    //heading.text = "Continue"
                    // trialTxt.text = ""

                    runCatching {
                        getProductDetailMicroValueNew(SKU_LIST[4])?.let { obj ->
                            val remoteString: String = if (obj.isTrailActive) {
                                "Try for Free"
                            } else {
                                setString(com.project.common.R.string.continue_text)
                            }

                            if (remoteString.isNotBlank() && !remoteString.contains(
                                    setString(com.project.common.R.string.continue_text),
                                    true
                                )
                            ) {
                                kotlin.runCatching {
                                    trialTextYearly.visible()
                                    heading.text = remoteString
                                }
                            } else {
                                kotlin.runCatching {
                                    trialTextYearly.hide()
                                    heading.text = remoteString
                                }
                            }
                        }
                    }
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
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProCarousalNew,
                            com.project.common.R.drawable.border_selected_pro
                        )
                } else if (layout is RelativeLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProCarousalNew,
                            com.project.common.R.drawable.border_selected_pro
                        )
                } else if (layout is ConstraintLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProCarousalNew,
                            com.project.common.R.drawable.border_selected_pro
                        )
                }
                it.priceTxtView?.let {
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProCarousalNew,
                            com.project.common.R.color.tab_txt_clr
                        )
                    )
                }
                it.bestValueTxtView?.let {

                    it.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ProCarousalNew,
                            com.project.common.R.color.selected_color
                        )
                    )
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProCarousalNew,
                            R.color.white
                        )
                    )
                }
                it.circleImage?.let {
                    it.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ProCarousalNew, com.project.common.R.drawable.checked_icon_lang
                        )
                    )
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
                            this@ProCarousalNew,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                } else if (layout is RelativeLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProCarousalNew,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                } else if (layout is ConstraintLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProCarousalNew,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                }
                it.priceTxtView?.let {
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProCarousalNew,
                            com.project.common.R.color.tab_txt_clr
                        )
                    )
                }

                it.bestValueTxtView?.let {
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProCarousalNew,
                            com.project.common.R.color.normal_txt_clr
                        )
                    )
                    it.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ProCarousalNew,
                            com.project.common.R.color.pro_new_unselected
                        )
                    )
                }


                it.circleImage?.let {
                    it.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@ProCarousalNew, com.project.common.R.drawable.s_check
                        )
                    )
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


}
