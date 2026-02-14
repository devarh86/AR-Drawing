/*
 * Copyright (c) 2015 Naver Corp.
 * @Author Ohkyun Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.project.crop.ui.main.custom_views.imagecrop.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import java.io.IOException

/**
 * Created by helloyako on 15. 4. 16..
 */
object BitmapLoadUtils {
    private const val TAG = "BitmapLoadUtils"

    @JvmStatic
    @JvmOverloads
    fun decode(
        path: String?,
        reqWidth: Int,
        reqHeight: Int,
        useImageView: Boolean = false,
        flipVertical: Boolean,
        flipHorizontal: Boolean
    ): Bitmap? {
        if (path == null) {
            return null
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, useImageView)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        var decodeSampledBitmap: Bitmap? = null
        var isSuccess = false
        while (!isSuccess) {
            try {
                isSuccess = true
                decodeSampledBitmap = BitmapFactory.decodeFile(path, options)
            } catch (ex: OutOfMemoryError) {
                Log.w(TAG, "BitmapLoadUtils decode OutOfMemoryError")
                options.inSampleSize = options.inSampleSize * 2
                isSuccess = false
            }
        }
        val exif = getExif(path) ?: return decodeSampledBitmap
        val exifOrientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationInDegrees = exifToDegrees(exifOrientation)
        return rotate(
            decodeSampledBitmap,
            rotationInDegrees,
            flipVertical,
            flipHorizontal
        )
    }

    fun rotate(
        bitmap: Bitmap?,
        degrees: Int,
        flipVertical: Boolean,
        flipHorizontal: Boolean
    ): Bitmap? {

        var bitmapOriginalOrProcessed = bitmap

        bitmapOriginalOrProcessed?.let {
            if (flipVertical) {
                val matrix = Matrix().apply { postScale(1f, -1f, it.width / 2f, it.height / 2f) }
                try {
                    val converted = Bitmap.createBitmap(
                        it, 0, 0,
                        it.width, it.height, matrix, true
                    )
                    if (bitmapOriginalOrProcessed != converted) {
                        it.recycle()
                        bitmapOriginalOrProcessed = converted
                    }
                } catch (ex: OutOfMemoryError) {
                    // if out of memory, return original bitmap
                }
            }
        }
        bitmapOriginalOrProcessed?.let {
            if (flipHorizontal) {
                val matrix = Matrix().apply { postScale(-1f, 1f, it.width / 2f, it.height / 2f) }
                try {
                    val converted = Bitmap.createBitmap(
                        it, 0, 0,
                        it.width, it.height, matrix, true
                    )
                    if (bitmapOriginalOrProcessed != converted) {
                        it.recycle()
                        bitmapOriginalOrProcessed = converted
                    }
                } catch (ex: OutOfMemoryError) {
                    // if out of memory, return original bitmap
                }
            }
        }
        bitmapOriginalOrProcessed?.let {
            if (degrees != 0) {
                val m = Matrix()
                m.setRotate(
                    degrees.toFloat(), it.width.toFloat() / 2,
                    it.height.toFloat() / 2
                )
                try {
                    val converted = Bitmap.createBitmap(
                        it, 0, 0,
                        it.width, it.height, m, true
                    )
                    if (it != converted) {
                        it.recycle()
                        bitmapOriginalOrProcessed = converted
                    }
                } catch (ex: OutOfMemoryError) {
                    // if out of memory, return original bitmap
                }
            }
        }
        return bitmapOriginalOrProcessed
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
        useImageView: Boolean
    ): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight
                && halfWidth / inSampleSize > reqWidth
            ) {
                inSampleSize *= 2
            }
            if (useImageView) {
                val maxTextureSize = GLUtils.maxTextureSize
                while (height / inSampleSize > maxTextureSize
                    || width / inSampleSize > maxTextureSize
                ) {
                    inSampleSize *= 2
                }
            }
        }
        return inSampleSize
    }

    private fun getExif(path: String): ExifInterface? {
        try {
            return ExifInterface(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun exifToDegrees(exifOrientation: Int): Int {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270
        }
        return 0
    }
}