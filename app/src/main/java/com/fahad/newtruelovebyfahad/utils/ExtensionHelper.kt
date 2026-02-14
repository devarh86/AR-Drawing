package com.fahad.newtruelovebyfahad.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.DialogPermissionsLayoutBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.utils.setOnSingleClickListener
import java.io.File
import kotlin.time.Duration.Companion.seconds

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

fun Activity?.navigateFragment(direction: NavDirections, currentId: Int) {
    try {
        this?.let {
            if (this is MainActivity) {
                this.navigate(direction, currentId)
            }
        }
    } catch (ex: Exception) {
        Log.e("error", "navigate: ", ex)
    }
}

fun Context?.isNetworkAvailable(): Boolean {
    this?.let {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = cm?.activeNetwork ?: return false
            try {
                val actNw = cm.getNetworkCapabilities(networkCapabilities) ?: return false
                when {
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> true
                }
            } catch (ex: java.lang.Exception) {
                false
            }
        } else {
            cm?.let { it.activeNetworkInfo?.isConnected } ?: run { false }
        }
        return isAvailable
    }
    return false
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
fun Activity.createPermissionsDialog(
    acceptAction: () -> Unit,
    declineAction: () -> Unit,
) = BottomSheetDialog(this).apply {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setCancelable(false)
    with(DialogPermissionsLayoutBinding.inflate(LayoutInflater.from(this@createPermissionsDialog))) {
        setContentView(root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.70).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        crossImg.setOnSingleClickListener {
            if (!isDestroyed && !isFinishing && isShowing) dismiss()
            declineAction.invoke()
        }

        doneLayout.setOnSingleClickListener {
            if (!isDestroyed && !isFinishing && isShowing) dismiss()
            acceptAction.invoke()
        }

        cancelTxt.setOnSingleClickListener {
            if (!isDestroyed && !isFinishing && isShowing) dismiss()
            declineAction.invoke()
        }

        if (!isDestroyed && !isFinishing && !isShowing) {
            show()
        }
    }
}


fun Activity.rateUs() {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun Activity.setStatusBarGradiant(drawable: Int) {
    val background = ContextCompat.getDrawable(this, drawable)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
//    window.navigationBarColor = ContextCompat.getColor(this, android.R.color.transparent)
    window.setBackgroundDrawable(background)
}


fun Activity.shareImage(filePath: String) {
    val share = Intent(Intent.ACTION_SEND)
    share.type = "image/png"
    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
    startActivity(Intent.createChooser(share, "Share Image"))
}

fun Activity.shareApp(appName: String) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
        var shareMessage = "\n${getString(com.project.common.R.string.share_text)}\n\n"
        shareMessage =
            """${shareMessage}https://play.google.com/store/apps/details?id=$packageName""".trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, "choose one"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Activity.printLog(msg: String) = Log.d("TrueLove:", msg)
fun Fragment.printLog(msg: String) = Log.d("TrueLove:", msg)
fun Context.showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun <T> MutableList<T>.checkForSafety(index: Int): Boolean {
    return index in 0 until size
}

fun Int.pxToDp(): Int {
    return (this / Resources.getSystem().displayMetrics.density).toInt()
}

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}