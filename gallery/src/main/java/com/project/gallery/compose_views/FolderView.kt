package com.project.gallery.compose_views

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.gallery.ui.adapters.GalleryFolderAdapterNew

@Composable
fun CreateFolderView(folderAdapter: GalleryFolderAdapterNew, showFolders: MutableState<Boolean>) {

    if (showFolders.value) {
        val customBackgroundColor = colorResource(id = com.project.common.R.color.white)
        Box(
            modifier = Modifier
//            .fillMaxSize()
                .wrapContentHeight()
                .fillMaxWidth()
                .background(customBackgroundColor)
                .clip(RoundedCornerShape(0.dp))
        ) {

            AndroidView(
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        //layoutManager = LinearLayoutManager(context)
                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        adapter = folderAdapter
                    }
                },
                update = { recyclerView ->
                    //Callback that runs on each recomposition.
                },
                modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    }
}