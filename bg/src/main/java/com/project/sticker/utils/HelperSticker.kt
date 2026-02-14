package com.project.sticker.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.project.sticker.R
import com.project.sticker.databinding.SnackbarViewBinding

fun Context?.createOrShowSnackBar(
    view: View,
    length: Int,
    message: String,
    error: Boolean,
    anchorView: View? = null
) {
    try {
//        val snackBar: Snackbar?
//
//        val actualLength =
//            when (length) {
//                0 -> {
//                    Snackbar.LENGTH_SHORT
//                }
//
//                1 -> {
//                    Snackbar.LENGTH_LONG
//                }
//
//                else -> {
//                    Snackbar.LENGTH_INDEFINITE
//                }
//            }
//
//        if (view.height != 0 && view.width != 0) {
//            snackBar = Snackbar.make(view, "", actualLength)
//
//            anchorView?.let {
//                snackBar.anchorView = it
//            }
//
//            val layoutInflater = LayoutInflater.from(this)
//
//            val binding = SnackbarViewBinding.inflate(layoutInflater)
//
//            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
//
//            val snackBarLayout = (snackBar.view as Snackbar.SnackbarLayout)
//
//            binding.message.text = message
//
//            binding.message.setTextColor(setColor(R.color.white))
//
//            snackBarLayout.setPadding(0, 0, 0, 0)
//
//            if (error) {
//                binding.root.backgroundTintList = ColorStateList.valueOf(
//                    setColor(R.color.red)
//                )
//            }
//
//            snackBarLayout.addView(binding.root, 0)
//
//            snackBar.show()
//        }

        this?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    } catch (ex: java.lang.Exception) {
        Log.e("error", "navigate: ", ex)
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

fun <T> MutableList<T>.checkForSafety(index: Int): Boolean {
    return index in 0 until size
}
//fun Context?.loadBitmap(path: Any, myCallback: (Bitmap) -> Unit) {
//    this?.let {
//        Glide.with(it).asBitmap().load(path)
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(
//                    resource: Bitmap, transition: Transition<in Bitmap>?
//                ) {
//                    myCallback.invoke(resource)
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {
//
//                }
//            })
//    }
//}