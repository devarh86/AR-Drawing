package com.project.common.repository

import androidx.annotation.Keep
import com.project.common.data_source.MyDatabase
import com.project.common.model.DbAllTablesModel
import com.project.common.db_table.FrameChildImageStickerModel
import com.project.common.db_table.FrameChildImagesModel
import com.project.common.db_table.FrameChildTextStickerModel
import com.project.common.db_table.FrameParentModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.Flow

@Keep
class DraftRepository(
    private val db: MyDatabase
) {
    fun addFrameData(
        obj: DbAllTablesModel
    ) {
        val parentId = db.getYourDao().addFrameParentData(obj.parentFrameModel)
        obj.imagesPathList.forEach {
            it.parentId = parentId
            db.getYourDao().addFrameChildImageData(it)
        }
        obj.stickerImageList.forEach {
            it.parentId = parentId
            db.getYourDao().addFrameChildImageStickerData(it)
        }
        obj.stickerTextList.forEach {
            it.parentId = parentId
            db.getYourDao().addFrameChildTextStickerData(it)
        }
    }

    fun getFrameData(
        id: Long
    ) = flow<Any> {
        emit(db.getYourDao().readParentFrame(id))
        emit(db.getYourDao().readImages(id))
        emit(db.getYourDao().readStickerImage(id))
        emit(db.getYourDao().readStickerText(id))
    }

    fun getAllData() = db
        .getYourDao().readAllParentFrame()

    fun deleteSpecificId(id: Long) = db
        .getYourDao().deleteParentFrame(id)
}