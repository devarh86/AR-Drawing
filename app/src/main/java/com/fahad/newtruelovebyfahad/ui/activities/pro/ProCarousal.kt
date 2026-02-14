package com.fahad.newtruelovebyfahad.ui.activities.pro
import com.example.inapp.helpers.Constants.getProductDetailMicroValueNew
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.showOfferPanel
import com.example.ads.crosspromo.helper.hide
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
import com.fahad.newtruelovebyfahad.databinding.ActivityProCarousalBinding
import com.fahad.newtruelovebyfahad.databinding.ProActivityExperiNewBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setString
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.utils.hideNavigation
import com.project.common.utils.privacyPolicy
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSaveSingleClickListener
import com.project.common.utils.setOnSingleClickListener

import com.project.common.utils.termOfUse
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class ProCarousal : AppCompatActivity()
{
    private var binding: ActivityProCarousalBinding?=null
    private var selectedPosition: Int = 1
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProCarousalBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding?.initViews()
        try {
            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@ProCarousal as LifecycleOwner)
            }
            isProVersion.observe(this@ProCarousal) {
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
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }
    private var backpress = false

    private fun backPress(showAd: Boolean) {

        runCatching {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_SPLASH,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.ProScreen.CLOSE)
                })
            if (showAd) {
                runCatching {
                        kotlin.runCatching {
                            val intent =
                                Intent(this@ProCarousal, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }

                }
            } else {
                kotlin.runCatching {
                        finish()

                }
            }
        }
    }


    private fun ActivityProCarousalBinding.initViews() {
        firebaseAnalytics?.logEvent("premium_screen_open", null)

        val showAd = intent.getBooleanExtra("show_ad", false)

        if(showAd) {
            dataStoreViewModel.initFun()
            lifecycleScope.launch(IO) {
                dataStoreViewModel.updateIntroComplete()
            }
        }

      /*  getProductDetailMicroValue(SKU_LIST[0])?.let {

            getProductDetailMicroValue(SKU_LIST[2])?.let {
              //  priceMonthlyPlanC.text = "${it.second} ${it.first}"
               weeklyPlanPriceC.text = "${it.second} ${it.first}"
                try {
                    it.first.toDouble()
                } catch (ex: java.lang.Exception) {
                    0.0
                }
            } ?: 0.0

            try {
                priceMonthlyPlanC.text = "${it.second} ${it.first}"
                it.first.toDouble()
            } catch (ex: java.lang.Exception) {
                0.0
            }
        } ?: 0.0*/
        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
            kotlin.runCatching {
                weeklyPlanPriceC.text  = "${obj.currency} ${obj.price}"
                    kotlin.runCatching {
                        if (obj.currency.isEmpty()) {
                            continueBtnC.isClickable = false
                            return@let
                        } else {
                            continueBtnC.isClickable = true
                        }

                        val remoteString: String = if (obj.isTrailActive) {
                            "Try for Free"
                        } else {
                            setString(com.project.common.R.string._continue)
                        }

                        if (remoteString.isNotBlank() && !remoteString.contains(
                                setString(com.project.common.R.string._continue),
                                true
                            )
                        ) {
                            kotlin.runCatching {
                                weeklyPlanTrailC.visible()
                                continueBtnC.text = remoteString
                            }
                        } else {
                            kotlin.runCatching {
                                weeklyPlanTrailC.hide()
                                continueBtnC.text = remoteString
                            }
                        }
                    }


            }
        } ?: run {
            continueBtnC.isClickable = false
        }
        getProductDetailMicroValueNew(SKU_LIST[4])?.let { obj ->
            kotlin.runCatching {
                priceYearlyPlanC.text  = "${obj.currency} ${obj.price}"
                kotlin.runCatching {
                    if (obj.currency.isEmpty()) {
                        continueBtnC.isClickable = false
                        return@let
                    } else {
                        continueBtnC.isClickable = true
                    }

                    val remoteString: String = if (obj.isTrailActive) {
                        "Try for Free"
                    } else {
                        setString(com.project.common.R.string._continue)
                    }

                    if (remoteString.isNotBlank() && !remoteString.contains(
                            setString(com.project.common.R.string._continue),
                            true
                        )
                    ) {
                        kotlin.runCatching {
                            monthlyPlanTrailC.visible()
                            continueBtnC.text = remoteString
                        }
                    } else {
                        kotlin.runCatching {
                            monthlyPlanTrailC.hide()
                            continueBtnC.text = remoteString
                        }
                    }
                }

            }
        } ?: run {
            continueBtnC.isClickable = false
        }
        setSelection(1)
        initListeners()



    }
    private var lastClickTime: Long = 0
    fun View.OnSaveSingleClickListener(delayTimeInSeconds: Int = 1, action: () -> Unit) {
        setOnSaveSingleClickListener {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastClickTime) >= delayTimeInSeconds.seconds.inWholeMilliseconds / 2) {
                lastClickTime = currentTime
                action.invoke()
            }
        }
    }


    private fun ActivityProCarousalBinding.initListeners() {

        val showAd = intent.getBooleanExtra("show_ad", false)

        lifecycleScope.launch(Main) {
            delay(2000)
            binding?.closeBtn?.isVisible = true
        }

        closeBtn.setOnSaveSingleClickListener {
            kotlin.runCatching {
                if (!backpress) {
                    backpress = true
                    backPress(showAd)
                }
            }
        }

        monthlyContainerC.setOnSaveSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(1)
            }
        }

        weeklyContainerC.setOnSaveSingleClickListener {
            if (Constants.proScreenReady) {
                setSelection(0)
            }
        }
        continueBtnC.setOnSaveSingleClickListener {
            if (!this@ProCarousal.isNetworkAvailable()) {

                kotlin.runCatching {
                    Toast.makeText(
                        this@ProCarousal,
                        "Please connect to internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnSaveSingleClickListener
            }

            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_SPLASH,
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
                   // Constants.isProVersion.value = true
                        (application as MyApp).billing.subscribe(
                        this@ProCarousal,
                        SKU_LIST[5]
                    )

                    firebaseAnalytics?.logEvent("weekly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "weekly_sub_panel_open")

                }

                1 -> {
                   // Constants.isProVersion.value = true
                    (application as MyApp).billing.subscribe(
                        this@ProCarousal,
                        SKU_LIST[4]
                    )
                    firebaseAnalytics?.logEvent("yearly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "yearly_sub_panel_open")

                }

                2 -> {
                    (application as MyApp).billing.subscribe(
                        this@ProCarousal,
                        SKU_LIST[1]
                    )
                    firebaseAnalytics?.logEvent("monthly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "monthly_sub_panel_open")



                }
            }
        }
        privacyPolicy.setOnSaveSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_SPLASH,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.PRIVACY_POLICY
                    )
                })
            this@ProCarousal.privacyPolicy()
        }
        termOfUse.setOnSaveSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_SPLASH,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.TERM_OF_USE)
                })
            this@ProCarousal.termOfUse()
        }
        cancelPro.setOnSaveSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_SPLASH,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.ProScreen.CANCEL_PRO)
                })
            openPlayStoreAccount()
        }

        customTextView(cancelTxtC)
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
                        Events.Screens.PREMIUM_SPLASH,
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
                    Events.Screens.PREMIUM_SPLASH,
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
                        weeklyContainerC,
                        weeklyPlanPriceC,
                        weeklyPlanHeadingC,
                        null,
                        checkOneC
                    )
//                    text.text = "Continue"
//                    trialTxt.text = ""

                    runCatching {
                        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
                            kotlin.runCatching {

                                kotlin.runCatching {


                                    val remoteString: String = if (obj.isTrailActive) {
                                        "Try for Free"
                                    } else {
                                        setString(com.project.common.R.string._continue)
                                    }

                                    if (remoteString.isNotBlank() && !remoteString.contains(
                                            setString(com.project.common.R.string._continue),
                                            true
                                        )
                                    ) {
                                        kotlin.runCatching {
                                            weeklyPlanTrailC.visible()
//                                            weeklyContainerC.setPadding(
//                                                weeklyContainerC.paddingLeft,
//                                                5, // top padding in px
//                                                weeklyContainerC.paddingRight,
//                                                5  // bottom padding in px
//                                            )
                                            continueBtnC.text = remoteString
                                        }
                                    } else {
                                        kotlin.runCatching {
                                            weeklyPlanTrailC.hide()
                                           // val weeklyContainer = findViewById<ConstraintLayout>(R.id.weekly_container_c)
//                                            weeklyContainerC.setPadding(
//                                                weeklyContainerC.paddingLeft,
//                                                24, // top padding in px
//                                                weeklyContainerC.paddingRight,
//                                                24  // bottom padding in px
//                                            )
                                            continueBtnC.text = remoteString
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
                    Events.Screens.PREMIUM_SPLASH,
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
                        monthlyContainerC,
                        priceYearlyPlanC,
                        monthlyPlanNameC,
                        null,
                        checkTwoC
                    )

                    runCatching {
                        getProductDetailMicroValueNew(SKU_LIST[4])?.let { obj ->
                            val remoteString: String = if (obj.isTrailActive) {
                                "Try for Free"
                            } else {
                                setString(com.project.common.R.string._continue)
                            }

                            if (remoteString.isNotBlank() && !remoteString.contains(
                                    setString(com.project.common.R.string._continue),
                                    true
                                )
                            ) {
                                kotlin.runCatching {
                                    monthlyPlanTrailC.visible()
//                                    monthlyContainerC.setPadding(
//                                        monthlyContainerC.paddingLeft,
//                                        5, // top padding in px
//                                        monthlyContainerC.paddingRight,
//                                        5  // bottom padding in px
//                                    )
                                    continueBtnC.text = remoteString
                                }
                            } else {
                                kotlin.runCatching {
                                    monthlyPlanTrailC.hide()
//                                    monthlyContainerC.setPadding(
//                                        monthlyContainerC.paddingLeft,
//                                        24, // top padding in px
//                                        monthlyContainerC.paddingRight,
//                                        24  // bottom padding in px
//                                    )
                                    continueBtnC.text = remoteString
                                }
                            }
                        }
                    }


//                    if (haveTrial)
//                    text.text = "Start 3 Day Free Trial"
//                    kotlin.runCatching {
//                        trialTxt.text =
//                            "Try 3 days for 0.000.00, then ${monthlyPlanPrice.text}/month"
//                    }

//                    text.text = "Continue"
//                    trialTxt.text = ""
                }
            }

            2 -> {

            }
        }

        proSelectionModel?.setSelection()
    }

    private fun ProSelectionModel?.setSelection() {
        this?.let {
            runCatching {
                val layout = it.linearLayout
                if (layout is LinearLayout) {
                    layout.background = ContextCompat.getDrawable(this@ProCarousal, com.project.common.R.drawable.border_selected_pro)
                } else if (layout is ConstraintLayout) {
                    layout.background = ContextCompat.getDrawable(this@ProCarousal, com.project.common.R.drawable.border_selected_pro)
                }

                it.bestValueTxtView?.let {

                    it.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ProCarousal,
                            com.project.common.R.color.selected_color
                        )
                    )
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProCarousal,
                            R.color.white
                        )
                    )
                }
                it.circleImage?.let {
                    it.setImageDrawable(
                        ContextCompat.getDrawable(
                        this@ProCarousal, com.project.common.R.drawable.checked_icon_lang))
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
                            this@ProCarousal,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                } else if (layout is ConstraintLayout) {
                    layout.background =
                        ContextCompat.getDrawable(
                            this@ProCarousal,
                            com.project.common.R.drawable.border_unselected_pro
                        )
                }

                it.bestValueTxtView?.let {

                    it.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            this@ProCarousal,
                            com.project.common.R.color.tab_txt_clr
                        )
                    )
                    it.setTextColor(
                        ContextCompat.getColor(
                            this@ProCarousal,
                            com.project.common.R.color.drawer_surface_clr
                        )
                    )
                }

                it.circleImage?.let {
                    it.setImageDrawable(
                        ContextCompat.getDrawable(
                        this@ProCarousal, com.project.common.R.drawable.s_check))
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