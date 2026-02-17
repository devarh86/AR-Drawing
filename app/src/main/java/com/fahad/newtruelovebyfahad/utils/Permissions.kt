package com.fahad.newtruelovebyfahad.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ads.Constants.showAppOpen
import com.fahad.newtruelovebyfahad.databinding.DialogPermissionsLayoutBinding
import com.project.common.utils.ConstantsCommon.enableGeneralNotification
import dagger.hilt.android.AndroidEntryPoint

const val PERMISSION_REQUEST_CODE = 1240
const val PERMISSION_REQUEST_CODE_NOTIFICATION = 1241
const val PERMISSION_REQUEST_CODE_FULL_SCREEN = 100

@AndroidEntryPoint
open class Permissions : AppCompatActivity() {

    private var initApp: (() -> Unit)? = null
    private var declineApp: (() -> Unit)? = null
    private var forOnce = true
    private var onNotificationPermissionGranted: (() -> Unit)? = null
    private var onNotificationPermissionDenied: (() -> Unit)? = null

    private var onFullScreenPermissionGranted: (() -> Unit)? = null
    private var onFullScreenPermissionDenied: (() -> Unit)? = null

    private var requestFullScreenIntentLauncher: ActivityResultLauncher<Intent>? = null


    fun checkAndRequestPermissions(
        vararg appPermissions: String,
        action: (() -> Unit)?,
        declineAction: (() -> Unit)?
    ) {
        forOnce = true
        initApp = action
        declineApp = declineAction
        var grantedCount = 0

        // check which permission are granted
        val listOfPermissionNeeded = ArrayList<String>()
        appPermissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listOfPermissionNeeded.add(permission)
            } else if (
                ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                grantedCount += 1
            }
        }

        // Ask for the non-granted permissions
        if (listOfPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listOfPermissionNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else initApp?.invoke()
    }

/*
    fun checkAndRequestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            var grantedCount = 0
            val appPermissions =
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)

            // check which permission are granted
            val listOfPermissionNeeded = ArrayList<String>()
            appPermissions.forEach { permission ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    listOfPermissionNeeded.add(permission)
                } else if (
                    ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    grantedCount += 1
                }
            }
        }
    }
*/

    fun registerForFullScreenIntent(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            kotlin.runCatching {
                if (requestFullScreenIntentLauncher == null) {
                    requestFullScreenIntentLauncher =
                        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                            // Check if the user granted the permission
                            if (notificationManager.canUseFullScreenIntent()) {
                                onFullScreenPermissionGranted?.invoke()
                            } else {
                                onFullScreenPermissionDenied?.invoke()
                            }
                        }
                }
            }
        }
    }

    fun checkAndRequestFullScreenPermission(
        onGranted: (() -> Unit)?,
        onDenied: (() -> Unit)?,
        notificationManager: NotificationManager
    ) {
        onFullScreenPermissionGranted = onGranted
        onFullScreenPermissionDenied = onDenied

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            requestFullScreenIntentPermission(notificationManager)
        } else {
            onFullScreenPermissionGranted?.invoke()
        }
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun requestFullScreenIntentPermission(notificationManager: NotificationManager) {
        kotlin.runCatching {
            if (notificationManager.canUseFullScreenIntent()) {
                onFullScreenPermissionGranted?.invoke()
            } else {
                // Launch the settings screen to request the permission
                enableGeneralNotification = false
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:$packageName")
                )
                showAppOpen = false
                requestFullScreenIntentLauncher?.launch(intent)
            }
        }
    }

    fun checkAndRequestNotificationPermission(
        onGranted: (() -> Unit)?,
        onDenied: (() -> Unit)?

    ) {
        onNotificationPermissionGranted = onGranted
        onNotificationPermissionDenied = onDenied

        // Check if we're on Android 13+ and the permission is not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE_NOTIFICATION
            )

        } else {
            // Permission is already granted
            onNotificationPermissionGranted?.invoke()
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val permissionResult = HashMap<String, Int>()
                var deniedCount = 0
                var grantedCount = 0

                // gather permission grant results
                grantResults.forEachIndexed { index, grantResult ->
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        permissionResult[permissions[index]] = grantResult
                        deniedCount += 1
                    } else if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        grantedCount += 1
                    }
                }

                // check if all permissions are granted
                if (deniedCount != 0) {
                    permissionResult.entries.forEach {
                        val permName = it.key
                        // permission is denied (this is the first time, when "Never Ask Again" is not checked)
                        // so ask again explaining the usage of the permission
                        // showShouldRequestPermissionRationale will return true
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName)) {
                            // show dialog of permission
                            if (!this.isDestroyed && !this.isFinishing) {
                                if (forOnce) {
                                    forOnce = false

                                    createPermissionsDialog(
                                        acceptAction = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                checkAndRequestPermissions(
                                                    Manifest.permission.READ_MEDIA_IMAGES,
                                                    Manifest.permission.CAMERA,
                                                    action = initApp,
                                                    declineAction = declineApp
                                                )
//
                                            } else {
                                                checkAndRequestPermissions(

                                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.CAMERA,
                                                    action = initApp,
                                                    declineAction = declineApp
                                                )
                                            }
                                        },
                                        declineAction = {
                                            declineApp?.invoke()
                                        }
                                    )
                                }
                            }
                        } else {
                            if (!this.isDestroyed && !this.isFinishing) {
                                if (forOnce) {
                                    forOnce = false
                                    createPermissionsDialog(
                                        acceptAction = {
                                            showAppOpen = false
                                            startActivity(
                                                Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", packageName, null)
                                                )
                                            )
                                        },
                                        declineAction = {
                                            declineApp?.invoke()
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (permissions.isNotEmpty() && grantedCount == permissions.size) initApp?.invoke()
            }

            PERMISSION_REQUEST_CODE_NOTIFICATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onNotificationPermissionGranted?.invoke()
                   /* com.example.ads.Constants.firebaseAnalytics?.eventForScreenDisplay(
                        AnalyticsConstants.EventName.POPUP_PERMISSION_NOTI_ACCEPT
                    )*/
                } else {
                    onNotificationPermissionDenied?.invoke()
                   /* com.example.ads.Constants.firebaseAnalytics?.eventForScreenDisplay(
                        AnalyticsConstants.EventName.POPUP_PERMISSION_NOTI_DENY
                    )*/
                }
            }

            PERMISSION_REQUEST_CODE_FULL_SCREEN -> {
                // Handle notification permission
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onFullScreenPermissionGranted?.invoke()
                } else {
                    onFullScreenPermissionDenied?.invoke()
                }
            }
        }
    }
}
