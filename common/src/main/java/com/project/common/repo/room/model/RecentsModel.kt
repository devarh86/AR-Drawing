package com.project.common.repo.room.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.project.common.repo.room.helper.RecentTypeConverter

@Keep
@Entity(tableName = "recents_frames")
class RecentsModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo("frame") var frame: String?
) {
    constructor(frame: Any?) : this(
        frame = RecentTypeConverter.toJson(frame)
    )
}