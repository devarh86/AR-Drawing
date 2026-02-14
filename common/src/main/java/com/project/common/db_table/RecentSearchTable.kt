package com.project.common.db_table

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "recent_search_model")
data class RecentSearchTable(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var searchValue: String,
    val timestamp: Long
)