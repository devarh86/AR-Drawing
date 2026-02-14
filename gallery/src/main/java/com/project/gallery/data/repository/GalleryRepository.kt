package com.project.gallery.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.project.gallery.data.model.GalleryChildModel
import com.project.gallery.data.model.GalleryModel
import com.project.gallery.data.util.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.io.File

class GalleryRepository(private val appContext: Context) {

    suspend fun getAllImageWithFolders() = flow {

        try {
            val videoFolders = mutableListOf<String>()
            val contentResolver: ContentResolver = appContext.contentResolver
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            // Columns to retrieve
            val projection = arrayOf(
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media._ID,
            )

            // Retrieve all videos
            val selection =
                "${MediaStore.Images.Media.MIME_TYPE} LIKE 'image/%'"
//            val selectionArgs = arrayOf("video/mp4")

            val sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"

            // Execute the query
            val cursor = contentResolver.query(uri, projection, selection, null, sortOrder)

            cursor?.use { it ->
                try {
                    val folderNameColumnIndex =
                        it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val videoIdColumnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    while (it.moveToNext()) {
                        try {
                            var folderName = it.getString(folderNameColumnIndex) ?: "0"
                            val videoId = it.getLong(videoIdColumnIndex)
                            val folderCoverUri = getImageUriById(contentResolver, videoId)

                            val filePath = folderCoverUri?.let { it1 ->
                                Utils().getRealPathFromURI(
                                    appContext,
                                    it1
                                )?.let { File(it) }
                            }

//                            Log.i(
//                                "getAllVideoFolders",
//                                "getAllVideoFolders: ${filePath?.absolutePath}"
//                            )

                            val parentPath = filePath?.parent

                            if (!videoFolders.contains(parentPath)) {
                                if (folderName == "0") {
                                    folderName = "Internal Storage"
                                }
                                parentPath?.let { path ->
                                    folderName.let { folderName ->
                                        val galleryList: MutableList<GalleryChildModel> =
                                            mutableListOf()
                                        videoFolders.add(path)
                                        val galleryModel = GalleryModel(
                                            "folder",
                                            false,
                                            folderName,
                                            galleryList,
                                            false,
                                        )
                                        emit(galleryModel)
                                    }
                                }
                            }
                            filePath?.let {
                                parentPath?.let { folderPath ->
                                    val folderIndex = videoFolders.indexOf(folderPath)
                                    emit(
                                        GalleryChildModel(it.absolutePath, false, folderIndex)
                                    )
                                }
                            }
                        } catch (ex: Exception) {
                            Log.i("getAllVideoFolders", "getAllVideoFolders: $it")
                        }
                    }
                } catch (ex: Exception) {
                    Log.e("error", "getAllVideoFolders: ", ex)
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "getAllVideoFolders: ", ex)
        }
    }.onCompletion {
        emit(
            GalleryModel(
                "finished-1"
            )
        )
    }.flowOn(Dispatchers.IO)

    private fun getImageUriById(contentResolver: ContentResolver, videoId: Long): Uri? {
        try {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media._ID} = ?"
            val selectionArgs = arrayOf(videoId.toString())

            val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val id = it.getLong(columnIndex)
                    return Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("error", "getVideoUriById: ", ex)
        }

        return null
    }
}