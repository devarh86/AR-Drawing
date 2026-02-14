@file:Suppress("DEPRECATION")

package com.project.common.utils

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.getWindowInsetsController
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateMargins
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.project.common.R
import com.project.common.databinding.SnackbarViewSavedBinding
import com.project.common.enum_classes.SaveQuality
import kotlin.math.roundToInt

fun Context.getSketchProgressMessages(): List<String> {
    return try {
        listOf(
            ContextCompat.getString(this, R.string.sketch_loading),
            ContextCompat.getString(this, R.string.sketch_ready),
            ContextCompat.getString(this, R.string.memory_sketching),
            ContextCompat.getString(this, R.string.sketch_details_fit),
            ContextCompat.getString(this, R.string.sketch_progress),
            ContextCompat.getString(this, R.string.sketch_placing_details),
            ContextCompat.getString(this, R.string.sketch_looking_timeless),
            ContextCompat.getString(this, R.string.sketch_you_brought_it_back),
            ContextCompat.getString(this, R.string.sketch_healing_memories),
            ContextCompat.getString(this, R.string.sketch_almost),
            ContextCompat.getString(this, R.string.sketch_damage_detected),
            ContextCompat.getString(this, R.string.sketch_transforming),
            ContextCompat.getString(this, R.string.sketch_uploading),
            ContextCompat.getString(this, R.string.sketch_final_touch),
            ContextCompat.getString(this, R.string.sketch_blast),
            ContextCompat.getString(this, R.string.sketch_new_life),
            ContextCompat.getString(this, R.string.sketch_details_locked),
            ContextCompat.getString(this, R.string.sketch_wait),
            ContextCompat.getString(this, R.string.sketch_alignment),
            ContextCompat.getString(this, R.string.sketch_memory_repairing),
            ContextCompat.getString(this, R.string.sketch_about_to_shine),
            ContextCompat.getString(this, R.string.sketch_epic),
            ContextCompat.getString(this, R.string.sketch_ready_to_reveal),
            ContextCompat.getString(this, R.string.sketch_restoring_clarity),
            ContextCompat.getString(this, R.string.sketch_scratches_fading),
            ContextCompat.getString(this, R.string.sketch_time_travel_mode),
            ContextCompat.getString(this, R.string.sketch_magic_at_work)
        )
    } catch (ex: Exception) {
        emptyList()
    }
}

fun EditText.showKeyboard(activity: Activity?) {
    activity?.let {
        val inputMethodManager = it.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun EditText.hideKeyboard(activity: Activity?) {
    activity?.let {
        this.clearFocus()
        val imm: InputMethodManager? =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(this.windowToken, 0)
    }
}

fun Activity.setStatusBarNavBarColor(drawable: Int) {
    val background = ContextCompat.getDrawable(this, drawable)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    window.setBackgroundDrawable(background)

}

fun Activity.setStatusBarNavBarColor(
    drawable: Int,
    isLightStatusBar: Boolean = true,
    isLightNavBar: Boolean = true
) {
    val background = ContextCompat.getDrawable(this, drawable)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    window.setBackgroundDrawable(background)

    // Adjust the status bar and navigation bar icon/text colors
    val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
    windowInsetsController.isAppearanceLightStatusBars = isLightStatusBar
    windowInsetsController.isAppearanceLightNavigationBars = isLightNavBar
}

fun Int.toPx(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )
}

@Suppress("DEPRECATION")
fun Activity.setStatusAndNavigationLight(light: Boolean) {
    if (light) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            val windowInsetController = getWindowInsetsController(window.decorView)
            windowInsetController?.isAppearanceLightStatusBars = true
            windowInsetController?.isAppearanceLightNavigationBars = true
        }
    } else {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        } else {
            val windowInsetController = getWindowInsetsController(window.decorView)
            windowInsetController?.isAppearanceLightStatusBars = false
            windowInsetController?.isAppearanceLightNavigationBars = false
        }
    }
}


fun String.getEventCategoryName(): String {
    return try {
        if (this.isNotBlank()) {
            this.lowercase().replace(" ", "_")
        } else {
            ""
        }
    } catch (ex: java.lang.Exception) {
        ""
    }
}

fun <T> runCatchingWithLog(
    tag: String = "AppError",
    message: String = "An error occurred",
    action: () -> T
): Result<T> {
    return runCatching {
        action()
    }.onFailure { throwable ->
        Log.e(tag, "$message: ${throwable.message}", throwable)
    }
}

fun eventForGalleryAndEditor(screenName: String, button: String, fromMainMenu: Boolean = false) {

    val bundle = Bundle().apply {
        putString(
            Events.ParamsKeys.ACTION,
            if (button.isBlank()) Events.ParamsValues.DISPLAYED else Events.ParamsValues.CLICKED
        )
        if (!fromMainMenu) {
            putString(Events.ParamsKeys.PARENT_SCREEN, parentScreen)
        }
        if (button.isNotBlank()) {
            putString(Events.ParamsKeys.BUTTON, button)
        }
    }

    Log.i("firebase_events_clicks", "events: screenName: $screenName bundle:  $bundle")

    firebaseAnalytics?.logEvent(screenName, bundle)
}

fun Context?.createOrShowSnackBarSaved(
    view: View?,
    message: String,
    error: Boolean,
    anchorView: View? = null,
    marginTop: Int
) {
    try {
        view?.let { myView ->
            val snackBar: Snackbar?

            if (myView.height != 0 && myView.width != 0) {

                snackBar = Snackbar.make(myView, "", Snackbar.LENGTH_LONG)

                anchorView?.let {
                    snackBar.anchorView = it
                }

                val layoutInflater = LayoutInflater.from(this)

                val binding = SnackbarViewSavedBinding.inflate(layoutInflater)

                snackBar.view.setBackgroundColor(Color.TRANSPARENT)

                val snackBarLayout = (snackBar.view as Snackbar.SnackbarLayout)

                binding.message.text = "Image successfully saved"

                binding.message.setTextColor(Color.BLACK)

                snackBarLayout.setPadding(50, 0, 50, 0)

                if (error) {
                    binding.root.backgroundTintList = ColorStateList.valueOf(
                        setColor(Color.RED)
                    )
                }

                snackBarLayout.addView(binding.root, 0)

                val params = snackBar.view.layoutParams

                params?.let {
                    if (it is CoordinatorLayout.LayoutParams) {
                        it.gravity = Gravity.TOP
                        it.updateMargins(0, 50.dpToPx() + marginTop, 0, 0)
                    } else {
                        (it as FrameLayout.LayoutParams).gravity = Gravity.TOP
                        it.updateMargins(0, 50.dpToPx() + marginTop, 0, 0)
                    }
                    snackBar.view.setLayoutParams(it)
                }

                snackBar.show()
            }
        }
    } catch (ex: java.lang.Exception) {
        Log.e("error", "navigate: ", ex)
    }
}

fun Activity?.hideNavigation() {
    kotlin.runCatching {
        this?.let {
            val uiMode = it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val flag = when (uiMode) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }

                else -> {
                    null
                }
            }
            if (flag == null)
                this.window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            else
                this.window?.decorView?.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or flag
                        )
        }
    }
}

fun Int.dpToPx(): Int {
    return try {
        (this * Resources.getSystem().displayMetrics.density).toInt()
    } catch (ex: java.lang.Exception) {
        80
    }
}
//
//fun Int.dpToPxForAll(): Int {
//    return try {
//        (this * Resources.getSystem().displayMetrics.density).toInt()
//    } catch (ex: java.lang.Exception) {
//        5
//    }
//}

fun Int.setColor(tab: TabLayout.Tab?, context: Context) {
    kotlin.runCatching {
        tab?.apply {
            val tabTextView =
                (tab.view.getChildAt(1) as? TextView)
            tabTextView?.setTextColor(
                context.getColor(this@setColor)
            )
        }

    }
}

fun Context?.setColor(color: Int): Int {

    try {
        this?.let {
            return ContextCompat.getColor(it, color)
        }
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
    return 0
}

fun Context?.setDrawable(drawable: Int): Drawable? {

    try {
        this?.let {
            return ContextCompat.getDrawable(it, drawable)
        }
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
    return null
}

fun Context?.setString(string: Int): String {

    try {
        this?.let {
            return it.resources.getString(string)
        }
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
    return ""
}

fun Activity.getNotchHeight(): Int {
    return try {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            val notchValue =
                windowManager.currentWindowMetrics.windowInsets.displayCutout?.safeInsetTop
            val statusBarHeight = statusBarHeight()
            if (notchValue != null && notchValue == 0) {
                if (statusBarHeight == 0) 80 else statusBarHeight
            } else {
                notchValue ?: if (statusBarHeight == 0) 80 else statusBarHeight
            }
        } else if (SDK_INT >= Build.VERSION_CODES.Q) {
            val notchValue =
                windowManager.defaultDisplay.cutout?.safeInsetTop
            val statusBarHeight = statusBarHeight()
            if (notchValue != null && notchValue == 0) {
                if (statusBarHeight == 0) 80 else statusBarHeight
            } else {
                notchValue ?: if (statusBarHeight == 0) 80 else statusBarHeight
            }
        } else {
            val statusBarHeight = statusBarHeight()
            if (statusBarHeight == 0) 80 else statusBarHeight
        }
    } catch (ex: java.lang.Exception) {
        try {
            val statusBarHeight = statusBarHeight()
            if (statusBarHeight == 0) 80 else statusBarHeight
        } catch (ex: java.lang.Exception) {
            80
        }
    }
}

fun Activity.statusBarHeight(): Int {
    return try {
        val metrics = resources.displayMetrics
        (24f * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    } catch (ex: Exception) {
        60
    }
}

fun Context?.getColorWithSafetyCheck(color: Int): Int {

    try {
        this?.let {
            return ContextCompat.getColor(it, color)
        }
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
    }
    return 0
}

fun Activity.termOfUse() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://xen-studios.com/terms.html")
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun getProScreen(): String {
    return "com.fahad.newtruelovebyfahad.ui.activities.pro.ProCarousalNew"

}

fun Activity.privacyPolicy() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://xen-studios.com/appmigo-privacy-policy.html")
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


data class QualityWithWaterMark(
    val pair: Pair<Int, Int>,
    val drawable: Int
)

fun Context.gettingQualityForEditorS(
    currentQuality: SaveQuality,
    originalWidth: Int,
    originalHeight: Int
): QualityWithWaterMark {

    val savingWidth: Int
    val savingHeight: Int

    savingWidth = originalWidth
    savingHeight = originalHeight
    return QualityWithWaterMark(
        Pair(savingWidth, savingHeight),
        R.drawable.watermark_720p
    )
}


fun Context.gettingQuality(
    currentQuality: SaveQuality,
    originalWidth: Int,
    originalHeight: Int
): QualityWithWaterMark {

    val savingWidth: Int
    val savingHeight: Int

    if (DeviceCheck.isLowEndDevice(this)) {
        when (currentQuality) {
            SaveQuality.HIGH -> {

                val ratio = originalWidth.toFloat().div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 720
                    savingHeight = 720
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (1280).div(ratio)
                    // val height = (1350).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_720p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            SaveQuality.MEDIUM -> {
                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 480
                    savingHeight = 480
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (858).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_480p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            else -> {
                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 360
                    savingHeight = 360
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (480).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_360p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }
        }
    } else {

        when (currentQuality) {

            SaveQuality.HIGH -> {

                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 1152
                    savingHeight = 1152
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_480p
                    )
                } else {
                    val height = (2048).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_1080p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            SaveQuality.MEDIUM -> {

                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 1080
                    savingHeight = 1080
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_480p
                    )
                } else {
                    val height = (1920).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_1080p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            else -> {

                val ratio =
                    originalWidth.toFloat()
                        .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 720
                    savingHeight = 720
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_480p
                    )
                } else {
                    val height = (1280).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_720p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }
        }
    }
}

fun Context.gettingQualityMultiFit(
    currentQuality: SaveQuality,
    originalWidth: Int,
    originalHeight: Int
): QualityWithWaterMark {

    val savingWidth: Int

    val savingHeight: Int

    if (DeviceCheck.isLowEndDevice(this)) {
        when (currentQuality) {
            SaveQuality.HIGH -> {

                val ratio =
                    originalWidth.toFloat()
                        .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 720
                    savingHeight = 720
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (1280).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_480p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            SaveQuality.MEDIUM -> {
                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 480
                    savingHeight = 480
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (858).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_360p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            else -> {
                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 360
                    savingHeight = 360
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (480).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_360p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }
        }
    } else {

        when (currentQuality) {

            SaveQuality.HIGH -> {

                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 1152
                    savingHeight = 1152
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (2048).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_1080p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            SaveQuality.MEDIUM -> {

                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 1080
                    savingHeight = 1080
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (1920).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_720p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            else -> {

                val ratio =
                    originalWidth.toFloat()
                        .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 720
                    savingHeight = 720
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = (1280).div(ratio)
                    val width = (ratio).times(height)
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_480p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }
        }
    }
}

fun Context.gettingQualityStitch(
    currentQuality: SaveQuality,
    originalWidth: Int,
    originalHeight: Int
): QualityWithWaterMark {

    val savingWidth: Int

    val savingHeight: Int

    if (DeviceCheck.isLowEndDevice(this)) {
        when (currentQuality) {
            SaveQuality.HIGH -> {

                val ratio =
                    originalWidth.toFloat()
                        .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 720
                    savingHeight = 720
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = 720f
                    val width = 720f
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_720p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            SaveQuality.MEDIUM -> {
                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 480
                    savingHeight = 480
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = 480f
                    val width = 480f
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_480p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            else -> {
                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 360
                    savingHeight = 360
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_360p
                    )
                } else {
                    val height = 360f
                    val width = 360f
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_360p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }
        }
    } else {

        when (currentQuality) {

            SaveQuality.HIGH -> {

                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 1440
                    savingHeight = 1440
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_480p
                    )
                } else {
                    val height = 1440f
                    val width = 1440f
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_1080p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            SaveQuality.MEDIUM -> {

                val ratio = originalWidth.toFloat()
                    .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 1080
                    savingHeight = 1080
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_480p
                    )
                } else {
                    val height = 1080f
                    val width = 1080f
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_1080p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }

            else -> {

                val ratio =
                    originalWidth.toFloat()
                        .div(originalHeight.toFloat())
                if (ratio == 1f) {
                    savingWidth = 720
                    savingHeight = 720
                    return QualityWithWaterMark(
                        Pair(savingWidth, savingHeight),
                        R.drawable.watermark_480p
                    )
                } else {
                    val height = 720f
                    val width = 720f
                    if (!height.isNaN() && !width.isNaN()) {
                        savingWidth = width.roundToInt()
                        savingHeight = height.roundToInt()
                        return QualityWithWaterMark(
                            Pair(savingWidth, savingHeight),
                            R.drawable.watermark_720p
                        )
                    } else {
                        return QualityWithWaterMark(
                            Pair(originalWidth, originalHeight),
                            R.drawable.watermark_480p
                        )
                    }
                }
            }
        }
    }
}

fun Context.getBitmapWithGlideCache(
    path: Any,
    pair: Pair<Int, Int> = Pair(0, 0),
    myCallback: (bitmap: Bitmap?) -> Unit
) {
    // Create RequestOptions to skip the memory and disk cache
    //val requestOptions = RequestOptions()

    /*        .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)*/

    val size = if (pair.first == 0) {
        Pair(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    } else
        pair

    if (path is String && path.isEmpty()) {
        myCallback.invoke(null)
        return
    }

    // Load the image dimensions without loading the entire bitmap
    Glide.with(this)
        .asBitmap()
        .load(path)
        .timeout(25000)
        .signature(ObjectKey(System.currentTimeMillis()))
        .override(size.first, size.second)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                myCallback.invoke(null)
                return true
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                myCallback.invoke(resource)
                return true
            }
        })
        .submit()
}

fun View.animateWaterMarkNew(myCallback: () -> Unit) {

    runCatching {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f)

        val animator =
            ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                myCallback.invoke()
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }

        })

        animator.duration = 600 // 1 second duration
        animator.start()
    }
}

fun View.zoomInAnimation() {

    runCatching {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)
        val animator =
            ObjectAnimator.ofPropertyValuesHolder(this, scaleX, scaleY)
        animator.duration = 150
        animator.start()
    }
}