package com.project.common.db_table

import android.graphics.Bitmap
import androidx.annotation.Keep
import androidx.core.graphics.BlendModeCompat
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "frame_child_images_model",
    foreignKeys = [
        ForeignKey(
            entity = FrameParentModel::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FrameChildImagesModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var parentId: Long = -1L,
    var originalPath: String = "",
    var croppedPath: String = "",
    val filterMatrix: FloatArray,
    val dotPositionList: List<Int>,
    var filterPosition: Int = -1,
    var filterOpacity: Float = 100f,
    var adjustPosition: Int = -1,
    var adjustmentValue: Float = 100f,
    var imagePosition: Int = 0,
    var brightnessValue: Float = 100f,
    var contrastValue: Float = 100f,
    var highlightValue: Float = 100f,
    var shadowValue: Float = 100f,
    var sharpenValue: Float = 100f,
    var saturationValue: Float = 100f,
    var warmthValue: Float = 100f,
    var hueValue: Float = 100f,
    var tintValue: Float = 100f,
    var colorValue: Float = 100f,
    var exposureValue: Float = 100f,
    var vignetteValue: Float = 0f,
    var previousVignetteValue: Float = 0f,
    var imageMatrix: FloatArray,
    val percentX: Float,
    val percentY: Float,
    val percentHeight: Float,
    val percentWidth: Float,
    val percentX1: Float,
    val percentY1: Float,
    val imgPerScaleX: Float,
    val imgPerScaleY: Float,
    val imgPerSkewX: Float,
    val imgPerSkewY: Float,
    val rotation: Float,
    val collagePiece: Int = -1,
    val viewRotation: Float = 0f,
    var effectMatrix: FloatArray = floatArrayOf(),
    var effectBitmap: Bitmap? = null,
    var effectMode: BlendModeCompat? = BlendModeCompat.SCREEN,
    var effectOpacity: Float = 255f,
    var maskBitmap: Bitmap? = null,
    var maskMatrix: FloatArray = floatArrayOf()
) {
    var x = 0f
    var y = 0f
    var width = 0
    var height = 0
    var isLast = false
}