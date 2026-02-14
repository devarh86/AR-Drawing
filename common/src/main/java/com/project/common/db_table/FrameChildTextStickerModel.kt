package com.project.common.db_table

import android.graphics.Color
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "frame_child_text_sticker_model",
    foreignKeys = [
        ForeignKey(
            entity = FrameParentModel::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FrameChildTextStickerModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var parentId: Long = -1L,
    var fontName: String = "",
    var fontPosition: Int = -1,
    var text: String = "",
    var fontColor: Int = Color.parseColor("#000000"),
    var fontColorPosition: Int = -1,
    var fontColorOpacity: Float = 1f,
    var textBg: Int = 0,
    var textBgColor: Int = 0,
    var textBgColorPosition: Int = -1,
    var textBgColorOpacity: Float = 1f,
    var textStickerPosTag: String = "",
    var positionMatrix: FloatArray = floatArrayOf(),
    var fontResource:Int = -1
)
