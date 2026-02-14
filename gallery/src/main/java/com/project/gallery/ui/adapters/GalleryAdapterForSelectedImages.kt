package com.project.gallery.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.common.utils.setOnSaveSingleClickListener
import com.project.common.utils.setOnSingleClickListener
import com.project.gallery.R
import com.project.gallery.data.model.GalleryChildModel


class GalleryAdapterForSelectedImages(
    private val listener: OnItemClick,
) :
    RecyclerView.Adapter<GalleryAdapterForSelectedImages.ViewHolder>() {

    val myList: MutableList<GalleryChildModel> = mutableListOf()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        val forMedia: ImageView = view.findViewById(R.id.selected_img)
        val cancelImg: ImageView = view.findViewById(R.id.cross_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.selected_image_item, parent, false)
        )
    }

    override fun getItemCount(): Int {

        return myList.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        try {
            if (position < myList.size && position >= 0) {

                val obj = myList[position]

                try {
                    Glide.with(holder.forMedia.context).load(obj.path)
                        .sizeMultiplier(0.6f).apply {
                            holder.forMedia.scaleType = ImageView.ScaleType.CENTER_CROP
                            into(holder.forMedia)
                        }
                } catch (ex: Exception) {
                    Log.e("error", "onBindViewHolder: ", ex)
                }

                //holder.cancelImg.setOnSingleClickListener {
                holder.cancelImg.setOnSaveSingleClickListener {
                    listener.onMediaRemove(obj, position)
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "onBindViewHolder: ", ex)
        }
    }

    fun addList(newMediaList: MutableList<GalleryChildModel>) {

        val currentSize: Int = myList.size
        myList.clear()
        myList.addAll(newMediaList)
        notifyItemRangeRemoved(0, currentSize)
        notifyItemRangeInserted(0, newMediaList.size)
    }

    fun addItem(obj: GalleryChildModel) {
        myList.add(obj)
        notifyItemInserted(myList.size - 1)
    }

    fun clearList() {
        myList.clear()
        notifyItemInserted(myList.size - 1)
    }

    fun removeItem(index: Int) {
        if (index != -1 && index < myList.size) {
            myList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    interface OnItemClick {
        fun onMediaRemove(obj: GalleryChildModel, index: Int)
    }
}