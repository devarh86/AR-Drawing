package com.fahad.newtruelovebyfahad.ui.activities

import android.app.Activity
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.analytics.Constants.firebaseAnalytics
import com.fahad.newtruelovebyfahad.databinding.ActivityFullScreenIntentBinding
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.project.common.R
import com.project.common.utils.ConstantsCommon.enableGeneralNotification
import com.project.common.utils.hideNavigation


class FullScreenIntentActivity : AppCompatActivity() {

    private var _binding: ActivityFullScreenIntentBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.TransparentActivityTheme)

        // Modern approach using window attributes
        window.apply {
            kotlin.runCatching {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    setShowWhenLocked(true)
                    setTurnScreenOn(false)
                } else {
                    addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
//                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    )
                }


                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

                // Ensure transparency
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
            }
        }

        _binding = ActivityFullScreenIntentBinding.inflate(layoutInflater)

        Log.i("showNotification", "onCreate: FullScreenIntentActivity")

        enableGeneralNotification = false

        setContentView(binding.root)

        _binding?.apply {
            intent.let {
                notificationTitle.text = it.getStringExtra("title") ?: ""
                notificationBody.text = it.getStringExtra("body") ?: ""

                val notificationId = it.getIntExtra("id", 0)
                notificationTitle.text = it.getStringExtra("title")

                val imageRes = when (notificationId) {
                    1 -> com.example.event_notifications.R.drawable.notification_image_one
                    2 -> com.example.event_notifications.R.drawable.notification_image_two
                    else -> com.example.event_notifications.R.drawable.notification_image_one // Optional default image
                }

                Glide.with(this@FullScreenIntentActivity)
                    .load(imageRes)
                    .into(notificationImage)

            }

            closeImg.setSingleClickListener {

                firebaseAnalytics?.logEvent("noti_lockscreen_close", null)
                Log.i(TAG, "onCreate: event: noti_lockscreen_click")

                enableGeneralNotification = false

                kotlin.runCatching {
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(200)
                }

                kotlin.runCatching {
                    finish()
                }
            }

            openNotificationBtn.setSingleClickListener {
                kotlin.runCatching {
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(200)
                }
                firebaseAnalytics?.logEvent("noti_lockscreen_click", null)
                Log.i(TAG, "onCreate: event: noti_lockscreen_click")

                kotlin.runCatching {

                    enableGeneralNotification = false

                    val intent = Intent(
                        this@FullScreenIntentActivity,
                        SplashActivity::class.java
                    )
                    intent.putExtra("noti_event", true)
                    startActivity(intent)
                    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            requestDismissKeyguard(this@FullScreenIntentActivity, null)
                        }
                    }
                    finish()
                }
            }
        }

        firebaseAnalytics?.logEvent("noti_lockscreen_show", null)
        Log.i(TAG, "onCreate: event: noti_lockscreen_click")
        hideNavigation()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus)
            hideNavigation()
    }

    override fun onDestroy() {
        super.onDestroy()
        turnScreenOffAndKeyguardOn()
    }

    private fun Activity.turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.addFlags(
                (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED) or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }

//        with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
//            }
//        }
    }

    private fun Activity.turnScreenOffAndKeyguardOn() {
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(false)
                setTurnScreenOn(false)
            } else {
                window.clearFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                )
            }
        }
    }
}