package com.fahad.newtruelovebyfahad.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fahad.newtruelovebyfahad.utils.interfaces.InternetConnectivityListener

@SuppressLint("UnsafeProtectedBroadcastReceiver")
open class InternetConnectivityReceiver : BroadcastReceiver() {
    private var connectivityListener: InternetConnectivityListener? = null

    fun setConnectivityListener(listener: InternetConnectivityListener) {
        connectivityListener = listener
    }

    override fun onReceive(context: Context, intent: Intent) {
        connectivityListener?.onConnectivityChanged(context.isNetworkAvailable())
    }
}