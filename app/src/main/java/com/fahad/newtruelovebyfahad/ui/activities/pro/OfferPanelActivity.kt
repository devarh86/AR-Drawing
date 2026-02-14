package com.fahad.newtruelovebyfahad.ui.activities.pro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.lifecycle.lifecycleScope
import com.example.ads.Constants
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.offerTypeYearly
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.crosspromo.helper.openUrl
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValue
import com.example.inapp.helpers.showToast
import com.fahad.newtruelovebyfahad.MyApp
import com.fahad.newtruelovebyfahad.databinding.ActivityOfferPanelBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.project.common.utils.getNotchHeight
import com.project.common.utils.privacyPolicy
import com.project.common.utils.setLocale
import com.project.common.utils.termOfUse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class OfferPanelActivity : AppCompatActivity() {

    private var binding: ActivityOfferPanelBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            setLocale(languageCode)
        }
        binding = ActivityOfferPanelBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding?.initViews()
    }

    private fun ActivityOfferPanelBinding.initViews() {

        firebaseAnalytics?.logEvent(
            Events.Screens.PREMIUM_OFFER,
            Bundle().apply {
                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
            })

        firebaseAnalytics?.logEvent("premium_offer_screen_open", null)
        com.example.ads.Constants.firebaseAnalytics?.logEvent("premium_offer_screen_open", null)

//        binding?.imageView9?.viewTreeObserver?.addOnGlobalLayoutListener(object :
//            ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                binding?.imageView9?.let {
//                    if (it.height > 50 && it.width > 50) {
//
//                        it.viewTreeObserver.removeOnGlobalLayoutListener(
//                            this
//                        )
//
//                        binding?.percentageTxt?.apply {
//                            x = ((((56f * 100f) / 560f) / 100f) * it.width)
//                            val width = ((((400f * 100f) / 560f) / 100f) * it.width)
//                            val height = ((((218f * 100f) / 763f) / 100f) * it.height)
//                            val layoutParameters = FrameLayout.LayoutParams(width.roundToInt(), height.roundToInt())
//                            layoutParams = layoutParameters
//                            pivotX = 0f
//                            rotation = 15f
//                        }
//                    }
//                }
//            }
//        })

        closeBtn.layoutParams?.let {
            if (it is ConstraintLayout.LayoutParams) {
                it.updateMargins(0, getNotchHeight(), 0, 0)
            }
        }

        if (offerTypeYearly) {

            priceTxt.text = "$3.58"
            afterPriceTxt.text = "/ Year"

            getProductDetailMicroValue(SKU_LIST[1])?.let {
                val totalPrice = it.first
                getProductDetailMicroValue(SKU_LIST[3])?.let {
                    priceTxt.text = "${it.second} ${it.first}"
                    afterPriceTxt.text = "/ Year"
                    kotlin.runCatching {
                        it.first.toDouble().let {
                            val percentage = (it * 100.0) / totalPrice.toDouble()
                            binding?.percentageTxt?.text =
                                abs(100 - percentage.roundToInt()).toString()
                        }
                    }
                }
            }
        } else {

            priceTxt.text = "$1.79"
            afterPriceTxt.text = "/ Month"

            getProductDetailMicroValue(SKU_LIST[0])?.let {
                val totalPrice = it.first
                getProductDetailMicroValue(SKU_LIST[4])?.let {
                    priceTxt.text = "${it.second} ${it.first}"
                    afterPriceTxt.text = "/ Month"
                    kotlin.runCatching {
                        it.first.toDouble().let {
                            val percentage = (it * 100.0) / totalPrice.toDouble()
                            binding?.percentageTxt?.text =
                                abs(100 - percentage.roundToInt()).toString()
                        }
                    }
                }
            }
        }

        initClicks()
        customTextView(cancelTxt)
    }

    private var backPress = false

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        kotlin.runCatching {
            if (!backPress) {
                backPress = true
                backPress()
            }
            return
        }
        super.onBackPressed()
    }

    private fun backPress() {

        runCatching {

            val showAd = intent.getBooleanExtra("show_ad", false)

            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_OFFER,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.ProScreen.CLOSE)
                })
            if (showAd) {
                showInterstitial(
                    loadedAction = {
                        kotlin.runCatching {
                            val intent =
                                Intent(this@OfferPanelActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    },
                    failedAction = {
                        kotlin.runCatching {
                            val intent =
                                Intent(this@OfferPanelActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    },
                    showAd = Constants.roboOfferShowAd,
                    onCheck = true
                )
            } else {
                finish()
            }
        }
    }

    private fun ActivityOfferPanelBinding.initClicks() {

        val showAd = intent.getBooleanExtra("show_ad", false)

        lifecycleScope.launch(Main) {
            delay(2000)
            binding?.closeBtn?.isVisible = true
        }

        closeBtn.setSingleClickListener {
            if (!backPress) {
                backPress = true
                backPress()
            }
        }

        continueBtn.setSingleClickListener {

            if (!this@OfferPanelActivity.isNetworkAvailable()) {

                kotlin.runCatching {
                    Toast.makeText(
                        this@OfferPanelActivity,
                        "Please connect to internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setSingleClickListener
            }

            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_OFFER,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.ProScreen.CONTINUE_PURCHASE
                    )
                })

            if (offerTypeYearly) {

                (application as MyApp).billing.subscribe(
                    this@OfferPanelActivity,
                    SKU_LIST[3]
                )

                firebaseAnalytics?.logEvent("yearly_offer_sub_panel_open", null)
                Log.i("MyFirebaseEvent", "yearly_offer_sub_panel_open")

            } else {
                (application as MyApp).billing.subscribe(
                    this@OfferPanelActivity,
                    SKU_LIST[4]
                )

                firebaseAnalytics?.logEvent("monthly_offer_sub_panel_open", null)
                Log.i("MyFirebaseEvent", "monthly_offer_sub_panel_open")
            }
        }
        privacyPolicy.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_OFFER,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.PRIVACY_POLICY
                    )
                })
            this@OfferPanelActivity.privacyPolicy()
        }
        termOfUse.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_OFFER,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.TERM_OF_USE)
                })
            this@OfferPanelActivity.termOfUse()
        }
        cancelPro.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_OFFER,
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
                        Events.Screens.PREMIUM_OFFER,
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

}