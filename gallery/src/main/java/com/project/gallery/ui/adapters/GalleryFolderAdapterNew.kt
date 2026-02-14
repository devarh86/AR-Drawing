package com.project.gallery.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.content.ContextCompat
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


class GalleryFolderAdapterNew(private val listener: OnFolderClick) :
    RecyclerView.Adapter<GalleryFolderAdapterNew.ViewHolder>() {

    val myList: MutableList<GalleryModel> = mutableListOf()
    private var mSelected = 0
    private var mLastSelected = 0
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        val folderName: TextView = view.findViewById(R.id.folderNameN)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gallery_folder_item_new, parent, false)
        )
    }

    override fun getItemCount(): Int {

        return myList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            if (position < myList.size && position >= 0) {
                val obj = myList[position]
                holder.folderName.background = ContextCompat.getDrawable(
                    holder.itemView.context,
                    if (mSelected == position) com.project.common.R.drawable.rounded_red_btn_bg else com.project.common.R.drawable.corner_radius_bg_5dp_with_stroke
                )
                holder.folderName.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        if (mSelected == position) android.R.color.white else com.project.common.R.color.text_color
                    )
                )


                holder.folderName.text = obj.folderName
                holder.itemView.setOnSingleClickListener {
                    if (position < myList.size) {
                        myList[position].folderName?.let {
                            mLastSelected = mSelected
                            mSelected = position
                            listener.folderClick(
                                position, myList[position].folderImagesVideoPaths,
                                it
                            )
                            runCatching {
                                notifyItemChanged(mSelected)
                            }
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