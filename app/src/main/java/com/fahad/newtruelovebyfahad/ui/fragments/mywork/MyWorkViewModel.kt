package com.fahad.newtruelovebyfahad.ui.fragments.mywork

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.database.getLongOrNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fahad.newtruelovebyfahad.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MyWorkViewModel @Inject constructor() : ViewModel() {

    private val _imageList: MutableLiveData<List<Uri>> = MutableLiveData()
    val imageList: LiveData<List<Uri>> get() = _imageList

    fun getImages(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val imageList = ArrayList<Uri>()
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        )
       // val path = "${Environment.DIRECTORY_PICTURES}/${context.getString(com.project.common.R.string.folder_name)}"
        val path = "${Environment.DIRECTORY_PICTURES}/Ar Drawing"
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.ImageColumns.RELATIVE_PATH + " like ? "
        } else {
            MediaStore.Images.Media.DATA + " LIKE ?"
        }
        val selectionArgs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf("%$path%")
        } else {
            arrayOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + "Ar Drawing" + "%"
                   // .toString() + File.separator + context.getString(com.project.common.R.string.folder_name) + "%"
            )
        }

        context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndexId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    do {
                        val imageId: Long? = cursor.getLongOrNull(columnIndexId)
                        imageId?.let {
                            imageList.add(
                                ContentUris.withAppendedId(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    it
                                )
                            )
                        }
                    } while (cursor.moveToNext())
                }
            }
        _imageList.postValue(imageList.toList().reversed())
    }

}
