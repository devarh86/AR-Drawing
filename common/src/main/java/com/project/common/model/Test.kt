package com.project.common.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
@Keep
@Entity
data class Test(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) {
}