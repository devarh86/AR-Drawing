package com.project.common.sealed_classes


sealed class BackgroundItem {
    data class StaticItem(val id: Int, val name: String, val imageRes: Int) : BackgroundItem()
    data class ApiItem(val id: String, val title: String, val imageUrl: String) : BackgroundItem()
}
