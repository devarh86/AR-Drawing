package com.project.common.data_source

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.project.common.db_table.FrameChildImageStickerModel
import com.project.common.db_table.FrameChildImagesModel
import com.project.common.db_table.FrameChildTextStickerModel
import com.project.common.db_table.FrameParentModel
import kotlinx.coroutines.flow.Flow

@Keep
@Dao
interface MyDao {

    @Insert()
    fun addFrameParentData(data: FrameParentModel): Long

    @Insert()
    fun addFrameChildImageData(data: FrameChildImagesModel)

    @Insert()
    fun addFrameChildImageStickerData(data: FrameChildImageStickerModel)

    @Insert()
    fun addFrameChildTextStickerData(data: FrameChildTextStickerModel)

    @Query("SELECT * FROM parent_frame_model WHERE id = :id")
    fun readParentFrame(id: Long): FrameParentModel

    @Query("DELETE FROM parent_frame_model WHERE id = :id")
    fun deleteParentFrame(id: Long)

    @Query("SELECT * FROM parent_frame_model")
    fun readAllParentFrame(): Flow<List<FrameParentModel>>

    @Query("SELECT * FROM frame_child_images_model WHERE parentId = :parentId")
    fun readImages(parentId: Long): List<FrameChildImagesModel>

    @Query("SELECT * FROM frame_child_image_sticker_model WHERE parentId = :parentId")
    fun readStickerImage(parentId: Long): List<FrameChildImageStickerModel>

    @Query("SELECT * FROM frame_child_text_sticker_model WHERE parentId = :parentId")
    fun readStickerText(parentId: Long): List<FrameChildTextStickerModel>

    @Query("SELECT * FROM parent_frame_model WHERE id = :parentId")
    fun readDraftType(parentId: Long): FrameParentModel
}