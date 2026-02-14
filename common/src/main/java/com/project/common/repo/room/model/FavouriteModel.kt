package com.project.common.repo.room.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project.common.repo.room.helper.FavouriteTypeConverter

@Keep
@Entity(tableName = "favourite_frames")
class FavouriteModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("isFavourite") var isFavourite: Boolean = false,
    @ColumnInfo("frame") var frame: String?
) {
    constructor(isFavourite: Boolean, frame: Any?) : this(
        isFavourite = isFavourite,
        frame = FavouriteTypeConverter.toJson(frame)
    )
}