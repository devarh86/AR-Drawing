package com.fahad.newtruelovebyfahad.utils

import android.R
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.ads.admobs.utils.loadNewInterstitialForPro
import com.example.ads.admobs.utils.showNewInterstitialPro
import com.example.ads.crosspromo.helper.hide
import com.example.ads.utils.homeInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Events
import com.example.inapp.helpers.Constants
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValue
import com.example.inapp.helpers.Constants.getProductDetailMicroValueNew
import com.fahad.newtruelovebyfahad.MyApp
import com.project.common.databinding.ProDialogCarousalBinding

fun Activity.createProDialog(
    purchaseSuccessAction: () -> Unit,
    dismissAction: () -> Unit,
) {
    Dialog(this, R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val binding = ProDialogCarousalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
            kotlin.runCatching {
                binding.weeklyPlanPriceC.text  = "${obj.currency} ${obj.price}"
                kotlin.runCatching {
                    if (obj.currency.isEmpty()) {
                        binding.continueBtnC.isClickable = false
                        return@let
                    } else {
                        binding.continueBtnC.isClickable = true
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
                            binding.weeklyPlanTrailC.visible()
                            binding.continueBtnC.text = remoteString
                        }
                    } else {
                        kotlin.runCatching {
                           binding.weeklyPlanTrailC.hide()
                            binding.continueBtnC.text = remoteString
                        }
                    }
                }


            }
        } ?: run {
            binding.continueBtnC.isClickable = false
        }
        getProductDetailMicroValueNew(SKU_LIST[4])?.let { obj ->
            kotlin.runCatching {
                binding.priceYearlyPlanC.text  = "${obj.currency} ${obj.price}"
                kotlin.runCatching {
                    if (obj.currency.isEmpty()) {
                        binding.continueBtnC.isClickable = false
                        return@let
                    } else {
                        binding.continueBtnC.isClickable = true
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
                            binding.monthlyPlanTrailC.visible()
                            binding.continueBtnC.text = remoteString
                        }
                    } else {
                        kotlin.runCatching {
                            binding.monthlyPlanTrailC.hide()
                            binding.continueBtnC.text = remoteString
                        }
                    }
                }

            }
        } ?: run {
            binding.continueBtnC.isClickable = false
        }

     /*   getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
            kotlin.runCatching {
                binding.weeklyPlanPriceC.text  = "${obj.currency} ${obj.price}"


            }
        } ?: run {
        }

        getProductDetailMicroValueNew(SKU_LIST[4])?.let { obj ->
            kotlin.runCatching {
                binding.priceYearlyPlanC.text  = "${obj.currency} ${obj.price}"


            }
        } ?: run {
        }*/
        var currentSelection = 1
        binding.weeklyContainerC.setOnClickListener {
//            if (Constants.proScreenReady) {
//                setSelection(0)
//            }

            currentSelection = 0
            binding.checkOneC.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@createProDialog, com.project.common.R.drawable.checked_icon_lang))
            }
            binding.weeklyContainerC.background =
                ContextCompat.getDrawable(
                    this@createProDialog,
                    com.project.common.R.drawable.border_selected_pro
                )
            binding.checkTwoC.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@createProDialog, com.project.common.R.drawable.s_check))
            }

            binding.monthlyContainerC.background =
                ContextCompat.getDrawable(
                    this@createProDialog,
                    com.project.common.R.drawable.border_unselected_pro
                )
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
                                    binding.weeklyPlanTrailC.visible()
                                    binding.continueBtnC.text = remoteString
                                }
                            } else {
                                kotlin.runCatching {
                                    binding.weeklyPlanTrailC.hide()
                                    binding.continueBtnC.text = remoteString
                                }
                            }
                        }


                    }
                }
            }

        }
        binding.monthlyContainerC.setOnClickListener {
            currentSelection = 1

            binding.checkTwoC.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@createProDialog, com.project.common.R.drawable.checked_icon_lang))
            }
            binding.monthlyContainerC.background =
                ContextCompat.getDrawable(
                    this@createProDialog,
                    com.project.common.R.drawable.border_selected_pro
                )
            binding.checkOneC.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@createProDialog, com.project.common.R.drawable.s_check))
            }

            binding.weeklyContainerC.background =
                ContextCompat.getDrawable(
                    this@createProDialog,
                    com.project.common.R.drawable.border_unselected_pro
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
                            binding.monthlyPlanTrailC.visible()
                            binding.continueBtnC.text = remoteString
                        }
                    } else {
                        kotlin.runCatching {
                            binding.monthlyPlanTrailC.hide()
                            binding.continueBtnC.text = remoteString
                        }
                    }
                }
            }

        }

        binding.continueBtnC.setOnClickListener {
            if (!this@createProDialog.isNetworkAvailable()) {
                kotlin.runCatching {
                    Toast.makeText(
                        this@createProDialog,
                        "Please connect to internet",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
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

            when(currentSelection){
                0->{
                  //  Constants.isProVersion.value = true
                    (application as MyApp).billing.subscribe(
                        this@createProDialog,
                        SKU_LIST[5]
                    )

                    firebaseAnalytics?.logEvent("weekly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "weekly_sub_panel_open")
                }
                1->{
                 //   Constants.isProVersion.value = true
                    (application as MyApp).billing.subscribe(
                        this@createProDialog,
                        SKU_LIST[4]
                    )

                    firebaseAnalytics?.logEvent("yearly_sub_panel_open", null)
                    Log.i("MyFirebaseEvent", "yearly_sub_panel_open")
                }
            }


        }

        binding.closeBtn.setOnClickListener {
            showNewInterstitialPro(homeInterstitial()) {
                    kotlin.runCatching {
                        loadNewInterstitialForPro(homeInterstitial()) {}
                    }
                }
            dismiss()
        }
        setOnDismissListener {
            dismissAction.invoke()
        }
        show()
    }



}

/* through reflection
   val activity = ActivityTracker.getCurrentActivity()
        if (activity != null) {
            val app = GlobalApp.instance
            if (app != null) {
                try {
                    val appClass = app.javaClass
                    val billingField = appClass.getDeclaredField("billing")
                    billingField.isAccessible = true
                    val billing = billingField.get(app)

                    // Check if context is Activity
                    if (activity is Activity) {
                        val method = billing.javaClass.getMethod(
                            "subscribe",
                            Activity::class.java,
                            String::class.java
                        )
                        method.invoke(billing, context, SKU_LIST[5])
                    } else {
                        Log.d("PRODIALOGEXT", "Context is not an Activity, cannot subscribe")
                    }
                } catch (e: Exception) {
                    Log.d("PRODIALOGEXT", "Exception occurred: ${e.message}")
                    e.printStackTrace()
                }
            }
        }*/
