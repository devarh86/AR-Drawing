package com.project.gallery.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.common.utils.setOnSingleClickListener
import com.project.gallery.R
import com.project.gallery.data.model.GalleryChildModel
import com.project.gallery.data.model.GalleryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GalleryFolderAdapter(private val listener: OnFolderClick) :
    RecyclerView.Adapter<GalleryFolderAdapter.ViewHolder>() {

    val myList: MutableList<GalleryModel> = mutableListOf()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        val forMedia: ImageFilterView = view.findViewById(R.id.gallerImg)
        val folderName: TextView = view.findViewById(R.id.folderName)
        val countTxt: TextView = view.findViewById(R.id.count_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gallery_folder_item, parent, false)
        )
    }

    override fun getItemCount(): Int {

        return myList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            if (position < myList.size && position >= 0) {

                val obj = myList[position]

                holder.folderName.text = obj.folderName

                if (myList[position].folderImagesVideoPaths.isNotEmpty()) {

                    val firstImagePath = if (position == 0) {
                        if (2 < myList[position].folderImagesVideoPaths.size) {
                            if (myList[position].folderImagesVideoPaths[1].path == "offline")
                                myList[position].folderImagesVideoPaths[2].path
                            else {
                                myList[position].folderImagesVideoPaths[1].path
                            }
                        } else
                            ""
                    } else {
                        if (myList[position].folderImagesVideoPaths.isNotEmpty())
                            myList[position].folderImagesVideoPaths[0].path
                        else
                            ""
                    }

                    holder.countTxt.text = myList[position].folderImagesVideoPaths.size.toString()

                    CoroutineScope(IO).launch {

                        Glide.with(holder.forMedia.context).load(firstImagePath)
                            .sizeMultiplier(0.4f).apply {
                                withContext(Main) {
                                    into(holder.forMedia)
                                }
                            }
                    }
                }

                holder.itemView.setOnSingleClickListener {

                    if (position < myList.size) {
                        myList[position].folderName?.let {
                            listener.folderClick(
                                position, myList[position].folderImagesVideoPaths,
                                it
                            )
                        }
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
    }

    fun addList(newMediaList: MutableList<GalleryModel>) {

        val currentSize: Int = myList.size
        myList.clear()
        myList.addAll(newMediaList)
        notifyItemRangeRemoved(0, currentSize)
        notifyItemRangeInserted(0, newMediaList.size)
    }

    interface OnFolderClick {

        fun folderClick(
            position: Int,
            folderImages: MutableList<GalleryChildModel>,
            folderName: String
        )
    }
}