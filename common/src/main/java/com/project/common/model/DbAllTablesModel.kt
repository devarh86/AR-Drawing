package com.project.common.model

import androidx.annotation.Keep
import com.project.common.db_table.FrameChildImageStickerModel
import com.project.common.db_table.FrameChildImagesModel
import com.project.common.db_table.FrameChildTextStickerModel
import com.project.common.db_table.FrameParentModel

@Keep
data class DbAllTablesModel(
    val parentFrameModel: FrameParentModel,
    val imagesPathList: MutableList<FrameChildImagesModel>,
    val stickerImageList: MutableList<FrameChildImageStickerModel>,
    val stickerTextList: MutableList<FrameChildTextStickerModel>
)