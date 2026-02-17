package com.project.common.baseActivity


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.common.R
import com.project.common.utils.createPermissionsDialog


const val PERMISSION_REQUEST_CODE = 1240

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        // Common setup code here, like logging, theme management, etc.
    }



    // Utility method to show a Toast message
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private var initApp: (() -> Unit)? = null
    private var declineApp: (() -> Unit)? = null
    private var forOnce = true

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

        }
    }
}
