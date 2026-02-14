package com.fahad.newtruelovebyfahad.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.ads.Constants.flowUninstall

import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.ui.activities.SplashActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutsManager @Inject constructor(
    @ApplicationContext private val context: Context
){

    private companion object{
        private const val TAG = "ShortcutsManager"
    }
    fun setupDynamicShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            shortcutManager?.removeAllDynamicShortcuts() // Clear old shortcuts
            val shortcuts = mutableListOf(
                //template,ai_blend,ai_edit
                createShortcut("collage", "Collage", "Collage", R.drawable.un_collage_ic, SplashActivity::class.java),
                createShortcut("enhance", "AI Enhance", "AI Enhance", R.drawable.un_enhance_ic, SplashActivity::class.java),
                createShortcut("ai_edit", "AI Edit Photo", "AI Edit Photo", R.drawable.un_edit_photo, SplashActivity::class.java)
            )
          //  Log.d(TAG, "setupDynamicShortcuts: flowUninstall $flowUninstall")

            if (flowUninstall) {
                shortcuts.add(createShortcut("uninstall", "Uninstall", "Uninstall App", R.drawable.un_install_icon, SplashActivity::class.java))
            }
            Log.d(TAG, "Max allowed: ${shortcutManager?.maxShortcutCountPerActivity}")
            shortcuts.forEach { Log.d(TAG, "Adding: ${it.id} - ${it.shortLabel}") }


            shortcutManager?.addDynamicShortcuts(shortcuts)

            Log.d(TAG, "Current: ${shortcutManager?.dynamicShortcuts?.map { it.id }}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun createShortcut(
        id: String,
        shortLabel: String,
        longLabel: String,
        iconResId: Int,
        targetActivity: Class<*>
    ): ShortcutInfo {
        return ShortcutInfo.Builder(context, id)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(Icon.createWithResource(context, iconResId))
            .setIntent(
                Intent(context, targetActivity)
                    .setAction(Intent.ACTION_VIEW)
                    .putExtra("shortcut_extra_key", id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            .build()
    }


}