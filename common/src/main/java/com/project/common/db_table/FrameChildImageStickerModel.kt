package com.project.common.db_table

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "frame_child_image_sticker_model",
    foreignKeys = [
        ForeignKey(
            entity = FrameParentModel::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FrameChildImageStickerModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var parentId: Long = -1L,
    var drawable: Bitmap,
    var positionMatrix: FloatArray,
    var tag: String = "",
    var blurRadius: Float = 1f,
    var alphaValue: Float = 255f,
    var originalBitmap: Bitmap? = null
)
