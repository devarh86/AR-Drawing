package com.fahad.newtruelovebyfahad.ui.activities.pro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import com.project.common.utils.termOfUse
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.ads.Constants.homeNewInterStrategy
import com.example.ads.Constants.languageCode
import com.example.ads.Constants.proCounter
import com.example.ads.Constants.proNewContinue
import com.example.ads.Constants.showOfferPanel
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.loadNewInterstitialWithoutStrategyCheck
import com.example.ads.admobs.utils.onResumeBanner
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.crosspromo.helper.openUrl
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValueNew
import com.example.inapp.helpers.Constants.isProVersion
import com.example.inapp.helpers.showToast
import com.fahad.newtruelovebyfahad.MyApp
import com.fahad.newtruelovebyfahad.databinding.ActivityProExperimentBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.project.common.utils.hideNavigation
import com.project.common.utils.privacyPolicy
import com.project.common.utils.setLocale
import com.project.common.utils.setString
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProActivity : AppCompatActivity() {
    private var binding: ActivityProExperimentBinding? = null
    private var backpress = false
    private var countDownTimer: CountDownTimer? = null
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kotlin.runCatching {
            this.setLocale(languageCode)
        }
        binding = ActivityProExperimentBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        kotlin.runCatching {
            dataStoreViewModel.writeCurrentTime()
        }
        closeActivityWhenPurchase()
        binding?.initViews()
        hideNavigation()

        loadNewInterstitialWithoutStrategyCheck(homeInterstitial()) {}

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    private fun ActivityProExperimentBinding.initViews() {
        firebaseAnalytics?.logEvent(
            Events.Screens.PREMIUM_NEW,
            Bundle().apply {
                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
            })
        firebaseAnalytics?.logEvent("premium_screen_open_new", null)
        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
            kotlin.runCatching {
                val remoteString: String = if (obj.isTrailActive) {
                    val stringOne = setString(com.project.common.R.string.start_free_trail)
                    val stringOther = setString(com.project.common.R.string.start_now)
                    if (proNewContinue.isNotBlank() && proNewContinue == "Start Free Trial") {
                        stringOne
                    } else if (proNewContinue.isNotBlank() && proNewContinue == "Start Now") {
                        stringOther
                    } else if (proNewContinue.isNotBlank() && proNewContinue == "Continue") {
                        setString(com.project.common.R.string.continue_text)
                    } else proNewContinue
                } else setString(com.project.common.R.string.continue_text)
                if (remoteString.isNotBlank() && !remoteString.contains(
                        setString(com.project.common.R.string.continue_text),
                        true
                    )
                ) {
                    kotlin.runCatching {
                        heading.text = remoteString
                        subheading.text =
                            setString(com.project.common.R.string.then).plus(
                                " ${obj.currency} ${obj.price}" + " ${
                                    setString(com.project.common.R.string.per_week)
                                }" + " " + setString(com.project.common.R.string.after_trial)
                            )
                        subheading.isVisible = true
                    }
                } else {
                    kotlin.runCatching {
                        subheading.isVisible = true
                        subheading.text = " ${obj.currency} ${obj.price}".plus(
                            " ${
                                setString(com.project.common.R.string.per_week)
                            }"
                        )
// continueLayout.updatePadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
                        heading.text = remoteString
                    }
                }
            }
        }
        initListeners()
    }

    private fun ActivityProExperimentBinding.initListeners() {
        val showAd = intent.getBooleanExtra("show_ad", false)
        if (showAd && homeNewInterStrategy) {
            var counter = proCounter+1
            binding?.closeBtnTime?.isVisible = true
            binding?.closeBtnTimeTxt?.text = counter.toString()
            countDownTimer = object : CountDownTimer(proCounter*1000, 1000) {
                override fun onTick(p0: Long) {
                    counter -= 1
                    if (!isFinishing && !isDestroyed)
                        binding?.closeBtnTimeTxt?.text = counter.toString()
                }

                override fun onFinish() {
                    if (!isFinishing && !isDestroyed) {
                        binding?.closeBtnTime?.isVisible = false
                        binding?.closeBtn?.isVisible = true
                    }
                }
            }

            countDownTimer?.start() ?: run {
                binding?.closeBtn?.isVisible = true
            }
        } else {
            binding?.closeBtn?.isVisible = true
        }
        closeBtn.setSingleClickListener {
            kotlin.runCatching {
                backPress(showAd)
            }
        }
        continueLayout.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_NEW,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.ProScreen.CONTINUE_PURCHASE
                    )
                })
            (application as MyApp).billing.subscribe(
                this@ProActivity,
                SKU_LIST[5]
            )
            firebaseAnalytics?.logEvent("weekly_new_sub_panel_open", null)
        }
        likeLayout.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_NEW,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.ProScreen.CONTINUE_PURCHASE
                    )
                })
            (application as MyApp).billing.subscribe(
                this@ProActivity,
// SKU_LIST[4]
                SKU_LIST[5]
            )
            firebaseAnalytics?.logEvent("weekly_new_sub_panel_open", null)
        }
        privacyPolicy.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_NEW,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(
                        Events.ParamsKeys.BUTTON,
                        Events.ParamsValues.PRIVACY_POLICY
                    )
                })
            this@ProActivity.privacyPolicy()
        }
        termOfUse.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_NEW,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.TERM_OF_USE)
                })
            this@ProActivity.termOfUse()
        }
        cancelPro.setSingleClickListener {
            firebaseAnalytics?.logEvent(
                Events.Screens.PREMIUM_NEW,
                Bundle().apply {
                    putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                    putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.ProScreen.CANCEL_PRO)
                })
            openPlayStoreAccount()
        }
        customTextView(cancelTxt)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    override fun onBackPressed() {
        kotlin.runCatching {
            val showAd = intent.getBooleanExtra("show_ad", false)
            if(homeNewInterStrategy) {
                if (!showAd ) {
                    backPress(false)
                }
            }else{
                backPress(showAd)
            }
            return
        }
        super.onBackPressed()
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
                setString(com.project.common.R.string.subscription_auto_renew)
            )
            spanTxt.append("Google Play Store")
            spanTxt.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    firebaseAnalytics?.logEvent(
                        Events.Screens.PREMIUM_NEW,
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
// spanTxt.append(".")
            view.movementMethod = LinkMovementMethod.getInstance()
            view.setText(spanTxt, TextView.BufferType.SPANNABLE)
        } catch (ex: Exception) {
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

    private fun backPress(showAd: Boolean) {
        if (!backpress) {
            backpress = true
            runCatching {
                firebaseAnalytics?.logEvent(
                    Events.Screens.PREMIUM_NEW,
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
                            kotlin.runCatching {
                                val intent =
                                    Intent(
                                        this@ProActivity,
                                        MainActivity::class.java
                                    )
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            }
                            /*      showInterstitial(
                                      loadedAction = {
                                          kotlin.runCatching {
                                              val intent =
                                                  Intent(
                                                      this@ProActivity,
                                                      MainActivity::class.java
                                                  )
                                              intent.flags =
                                                  Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                              startActivity(intent)
                                              finish()
                                          }
                                      },
                                      failedAction = {
                                          kotlin.runCatching {
                                              val intent =
                                                  Intent(
                                                      this@ProActivity,
                                                      MainActivity::class.java
                                                  )
                                              intent.flags =
                                                  Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                              startActivity(intent)
                                              finish()
                                          }
                                      },
                                      showAd = com.example.ads.Constants.roboProShowAd,
                                      onCheck = true
                                  )*/
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
                            kotlin.runCatching {
                                val intent =
                                    Intent(
                                        this@ProActivity,
                                        MainActivity::class.java
                                    )
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    //    override fun onResume() {
//        super.onResume()
//        loadBannerAd()
//    }
//    private fun loadBannerAd() {
//        kotlin.runCatching {
//            if (!isProVersion()) {
//                binding?.bannerContainer?.visibility = View.VISIBLE
//                binding?.apply {
//                    onResumeBanner(
//                        adBannerContainer,
//                        crossBannerIv,
//                        bannerLayout.adContainer,
//                        bannerLayout.shimmerViewContainer
//                    )
//                }
//            } else {
//                try {
//                    if (isProVersion())
//                        binding?.bannerContainer?.visibility = View.GONE
//                    else {
//                        binding?.bannerContainer?.visibility = View.INVISIBLE
//                    }
//                } catch (ex: java.lang.Exception) {
//                    Log.e("error", "onResume: ", ex)
//                }
//            }
//        }
//    }
    private fun closeActivityWhenPurchase() {
        try {
            if (isProVersion.hasObservers()) {
                isProVersion.removeObservers(this@ProActivity as LifecycleOwner)
            }
            isProVersion.observe(this@ProActivity) {
                if (it) {
                    kotlin.runCatching {
                        if (!isFinishing && !isDestroyed) {
                            binding?.bannerContainer?.isVisible = false
                            val showAd = intent.getBooleanExtra("show_ad", false)
                            backPress(showAd = false)
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "onCreate: ", ex)
        }
    }
}



