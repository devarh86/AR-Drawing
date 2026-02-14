package com.project.common.db_table

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project.common.utils.enums.CollageStyle

@Keep
@Entity(tableName = "parent_frame_model")
data class FrameParentModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String = "",
    var framePath: String = "",
    var type: String = "frame",
    var mask: String = "",
    var bgPath: String = "",
    var thumbnailPath: String = "",
    var blurRadius: Float = 1f,
    var borderBgColor: Int = 0,
    var borderBgColorOpacity: Float = 1f,
    var borderSize: Float = 0f,
    var borderRadius: Float = 15f,
    var pieces: Int = 0,
    var theme: Int = 0,
    var collageType: Int = 0,
    var templatePosition: Int = 0,
    var collageBg: String = "",
    var editor: String = "",
    var selectedId: Long = -1L,
    var originalWidth: Int = 0,
    var originalHeight: Int = 0,
    var borderBgColorPosition: Int = 0,
    var padding: Float = 0f,
    var collageStyle: CollageStyle = CollageStyle.REGULAR,
    var effectMatix: FloatArray = floatArrayOf(),
    var ratio: String = "",
    var isComplexShapes: Boolean = false,
    var categoryName: String = ""
)
