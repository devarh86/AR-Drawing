package com.project.gallery.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import com.project.gallery.R
import com.project.gallery.databinding.SnackbarViewBinding

fun Context?.createOrShowSnackBar(
    view: View,
    length: Int,
    message: String,
    error: Boolean,
    anchorView: View? = null
) {
    try {
        val snackBar: Snackbar?

        val actualLength =
            when (length) {
                0 -> {
                    Snackbar.LENGTH_SHORT
                }

                1 -> {
                    Snackbar.LENGTH_LONG
                }

                else -> {
                    Snackbar.LENGTH_INDEFINITE
                }
            }

        if (view.height != 0 && view.width != 0) {
            snackBar = Snackbar.make(view, "", actualLength)

            anchorView?.let {
                snackBar.anchorView = it
            }

            val layoutInflater = LayoutInflater.from(this)

            val binding = SnackbarViewBinding.inflate(layoutInflater)

            snackBar.view.setBackgroundColor(Color.TRANSPARENT)

            val snackBarLayout = (snackBar.view as Snackbar.SnackbarLayout)

            binding.message.text = message

            binding.message.setTextColor(setColor(R.color.white))

            snackBarLayout.setPadding(0, 0, 0, 0)

            if (error) {
                binding.root.backgroundTintList = ColorStateList.valueOf(
                    setColor(R.color.red)
                )
            }

            snackBarLayout.addView(binding.root, 0)

            snackBar.show()
        }

    } catch (ex: java.lang.Exception) {
        Log.e("error", "navigate: ", ex)
    }
}

fun Context?.createOrShowSnackBar(
    view: View,
    length: Int,
    message: String,
    error: Boolean,
    anchorView: View? = null,
    setAction: Boolean,
    myCallback: () -> Unit
) {
    try {
        val snackBar: Snackbar?

        val actualLength =
            when (length) {
                0 -> {
                    Snackbar.LENGTH_SHORT
                }

                1 -> {
                    Snackbar.LENGTH_LONG
                }

                else -> {
                    Snackbar.LENGTH_INDEFINITE
                }
            }

        if (view.height != 0 && view.width != 0) {
            snackBar = Snackbar.make(view, "", actualLength)

            if (setAction) {
                snackBar.setAction("retry",
                    View.OnClickListener {
                        snackBar.dismiss()
                        myCallback.invoke()
                    })
            }

            anchorView?.let {
                snackBar.anchorView = it
            }

            val layoutInflater = LayoutInflater.from(this)

            val binding = SnackbarViewBinding.inflate(layoutInflater)

            snackBar.view.setBackgroundColor(Color.TRANSPARENT)

            val snackBarLayout = (snackBar.view as Snackbar.SnackbarLayout)

            binding.message.text = message

            binding.message.setTextColor(setColor(R.color.white))

            snackBarLayout.setPadding(0, 0, 0, 0)

            if (error) {
                binding.root.backgroundTintList = ColorStateList.valueOf(
                    setColor(R.color.red)
                )
            }

            snackBarLayout.addView(binding.root, 0)

            snackBar.show()
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