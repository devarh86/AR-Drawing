package com.project.common.data_source

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.common.db_table.FrameChildImageStickerModel
import com.project.common.db_table.FrameChildImagesModel
import com.project.common.db_table.FrameChildTextStickerModel
import com.project.common.db_table.FrameParentModel

@Keep
@Database(
    entities = [FrameParentModel::class, FrameChildImagesModel::class, FrameChildTextStickerModel::class, FrameChildImageStickerModel::class],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {

    abstract fun getYourDao(): MyDao
}