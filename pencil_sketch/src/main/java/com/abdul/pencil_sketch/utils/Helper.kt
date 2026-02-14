package com.abdul.pencil_sketch.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.navigation.NavDirections
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.project.common.utils.Utils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Objects

fun Activity?.navigateFragment(direction: NavDirections, currentId: Int) {
    try {

        this?.let {
            if (this is PencilSketchActivity) {
                this.navigate(direction, currentId)
            }
        }
    } catch (ex: Exception) {
        Log.e("error", "navigateFragment: ")
    }
}

fun Activity?.navigateFragment(direction: Int, currentId: Int, bundle: Bundle) {
    try {
        this?.let {
            if (this is PencilSketchActivity) {
                this.navigate(direction, currentId, bundle)
            }
        }
    } catch (ex: Exception) {
        Log.e("error", "navigate: ", ex)
    }
}

fun Context?.loadBitmap(path: Any, myCallback: (Bitmap) -> Unit) {
    this?.let {
        Glide.with(it).asBitmap().load(path)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap, transition: Transition<in Bitmap>?,
                ) {
                    myCallback.invoke(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }
}

suspend fun Bitmap.saveMediaToStorage(context: Context, onUriCreated: (String?) -> Unit) {

    var fos: OutputStream? = null
    val filename = "${System.currentTimeMillis()}.jpg"
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(
//                    MediaStore.MediaColumns.RELATIVE_PATH,
//                    Environment.DIRECTORY_PICTURES + File.separator + context.getString(R.string.folder_name)
//                )
//            } else {
//                put(
//                    MediaStore.MediaColumns.DATA,
//                    Environment.DIRECTORY_PICTURES + File.separator + context.getString(
//                        R.string.folder_name
//                    )
//                )
//            }

            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Ar Drawing"
            )
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let {
                fos = Objects.requireNonNull(it).let {
                    resolver.openOutputStream(it)
                }
//                fos?.let { compress(Bitmap.CompressFormat.PNG, 100, it) }
//                Objects.requireNonNull(fos)
//                delay(200)
//                fos?.close()
                try {
                    fos?.use {
                        compress(Bitmap.CompressFormat.PNG, 100, it)
                        Objects.requireNonNull(it)
                        delay(200)
                        it.close()
                    }
                } catch (ex: java.lang.Exception) {
                    Objects.requireNonNull(fos)
                    fos?.let { compress(Bitmap.CompressFormat.PNG, 100, it) }
                    delay(200)
                    fos?.close()
                }
                it.let {
                    Utils().getRealPathFromURI(context, it)?.let {
                        withContext(Main) {
                            onUriCreated(it)
                            withContext(IO) {
                                try {
                                    MediaScannerConnection.scanFile(
                                        context,
                                        arrayOf<String>(it),
                                        null
                                    ) { _, _ -> }
                                } catch (ex: java.lang.Exception) {
                                    Log.e("error", "saveMediaToStorage: ", ex)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            //These for devices running on android < Q
            val imagesDir =
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Ar Drawing"
                )
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

            fos?.use {
                compress(Bitmap.CompressFormat.PNG, 100, it)
                delay(200)
                it.close()
            }
            withContext(Main) {
                onUriCreated(image.absolutePath)
                withContext(IO) {
                    try {
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf<String>(image.absolutePath),
                            null
                        ) { _, _ -> }
                    } catch (ex: java.lang.Exception) {
                        Log.e("error", "saveMediaToStorage: ", ex)
                    }
                }
            }
        }
    } catch (e: Exception) {
        onUriCreated(null)
        Log.d("error", e.toString())
    }
}


