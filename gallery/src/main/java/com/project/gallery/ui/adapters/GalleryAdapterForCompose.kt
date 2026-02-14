package com.project.gallery.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.common.utils.ConstantsCommon.carouselImagesCount
import com.project.common.utils.setOnSaveSingleClickListener
import com.project.common.utils.setOnSingleClickListener
import com.project.gallery.R
import com.project.gallery.data.model.GalleryChildModel
import kotlin.math.max


class GalleryAdapterForCompose(
    private val listener: OnItemClick,
    private val fromCollage: Boolean = false,
) : RecyclerView.Adapter<GalleryAdapterForCompose.ViewHolder>() {

    var newFlow: Boolean = false

    val myList: MutableList<GalleryChildModel> = mutableListOf()

    var maxCounter = 0
    // Add global counter for sequential numbering
    private var globalSelectionCounter = 0

    fun resetOldSelection(){
        lastSelected?.let {
            it.isSelected = false
            if (it.indexInAdapter > -1 && it.indexInAdapter < itemCount)
                notifyItemChanged(it.indexInAdapter)
        }
        lastSelected = null
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view referencet
        val forMedia: ImageFilterView = view.findViewById(R.id.gallerImg)
        val cancelImg: ImageView = view.findViewById(R.id.cancel_img)
        val checkSelected: ImageView = view.findViewById(R.id.check_selected_img)
        val imageCounter: TextView = view.findViewById(R.id.image_counter)
        val imageCounterNew: TextView = view.findViewById(R.id.image_counter_new)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.gallery_item, parent, false)
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

                obj.indexInAdapter = position

                when (obj.path) {
                    "Camera" -> {
                        Glide.with(holder.forMedia.context).load(R.drawable.camera_icon)
                            .sizeMultiplier(0.6f).apply {
                                holder.forMedia.scaleType = ImageView.ScaleType.CENTER_INSIDE
                                into(holder.forMedia)
                            }
                    }

                    "offline" -> {
                        try {
                            Glide.with(holder.forMedia.context).load(obj.id)
                                .sizeMultiplier(0.65f).apply {
                                    holder.forMedia.scaleType = ImageView.ScaleType.FIT_XY
                                    into(holder.forMedia)
                                }
                        } catch (ex: Exception) {
                            Log.e("error", "onBindViewHolder: ", ex)
                        }
                    }

                    else -> {
                        try {
                            Glide.with(holder.forMedia.context).load(obj.path)
                                .sizeMultiplier(0.6f).apply {
                                    holder.forMedia.scaleType = ImageView.ScaleType.CENTER_CROP
                                    into(holder.forMedia)
                                }
                        } catch (ex: Exception) {
                            Log.e("error", "onBindViewHolder: ", ex)
                        }
                    }
                }

                holder.checkSelected.isVisible = obj.isSelected && newFlow

                if(obj.isSelected && lastSelected == null){
                    lastSelected = obj
                }

                if (obj.fromCollage&& !obj.fromCarousel) {
                    //holder.cancelImg.setOnSingleClickListener {
                    holder.cancelImg.setOnSaveSingleClickListener {
                        listener.onMediaRemove(obj, position)
                        removeCounter(obj, position)
                    }
                    if (obj.selectedImageCounter > 0) {
                        holder.cancelImg.visibility = View.VISIBLE

                        holder.imageCounter.text =
                            (obj.selectedImageCounter).toString()

                        holder.imageCounter.visibility = View.VISIBLE
                    } else {
                        holder.cancelImg.visibility = View.GONE
                        holder.imageCounter.visibility = View.GONE
                    }
                    //holder.cancelImg.setOnSingleClickListener {
                    holder.cancelImg.setOnSaveSingleClickListener {
                        listener.onMediaRemove(obj, position)
                        removeCounter(obj, position)
                    }
                }else if(obj.fromCarousel&& obj.fromCollage) {
                    //holder.cancelImg.setOnSingleClickListener {
                    holder.cancelImg.setOnSaveSingleClickListener {
                        listener.onMediaRemove(obj, position)
                        removeCounter(obj, position)
                    }
                    if (obj.selectedImageCounter > 0) {
                        //    if (obj.selectedImageCounter > 0) {
                        holder.cancelImg.visibility = View.GONE
                        holder.imageCounter.text = (obj.selectedImageCounter).toString()
                        Log.i("GCOUNNUM", "CONDITION ${(obj.globalSelectionNumber)}")
                        holder.imageCounterNew.text = (obj.globalSelectionNumber).toString()
                        holder.imageCounter.visibility = View.GONE
                        holder.imageCounterNew.visibility = View.VISIBLE
                    } else {
                        holder.cancelImg.visibility = View.GONE
                        holder.imageCounter.visibility = View.GONE
                        holder.imageCounterNew.visibility = View.GONE
                    }
                   // holder.cancelImg.setOnSingleClickListener {
                    holder.cancelImg.setOnSaveSingleClickListener {
                        listener.onMediaRemove(obj, position)
                        removeCounter(obj, position)
                    }
                } else {
                    holder.cancelImg.visibility = View.GONE
                    holder.imageCounter.visibility = View.GONE
                    holder.imageCounterNew.visibility = View.GONE
                }

               // holder.forMedia.setOnSingleClickListener {
                holder.forMedia.setOnSaveSingleClickListener {
                    if (obj.path == "Camera") {
                        listener.onCameraClick()
                    } else {
                        val totalSelected = listener.onMediaClick(obj)
                        if (obj.fromCollage&&!obj.fromCarousel) {
                            if (totalSelected < maxCounter) {
                                obj.selectedImageCounter += 1
                                holder.cancelImg.visibility = View.VISIBLE
                                holder.imageCounter.text = (obj.selectedImageCounter).toString()
                                holder.imageCounter.visibility = View.VISIBLE
                            } else {
                                listener.onLimitReached()
                            }
                        }else if(obj.fromCarousel&&obj.fromCollage) {
                            if (totalSelected < maxCounter) {
                                Log.i(
                                    "GCOUNNUM",
                                    "onBindViewHolder:totalSelected--Second IF: $totalSelected"
                                )
                                globalSelectionCounter++
                                obj.globalSelectionNumber = globalSelectionCounter
                                holder.imageCounterNew.text = (obj.globalSelectionNumber).toString()
                                holder.imageCounterNew.visibility = View.VISIBLE
                                holder.imageCounter.visibility = View.GONE
                                holder.cancelImg.visibility = View.GONE

                            } else {
                                listener.onLimitReached()
                            }
                        }
                        else {
                            holder.cancelImg.visibility = View.GONE
                            holder.imageCounter.visibility = View.GONE
                            holder.imageCounterNew.visibility = View.GONE
                        }

                        if (newFlow) {
                            lastSelected?.isSelected = false
                            lastSelected?.let {
                                it.isSelected = false
                                if (it.indexInAdapter > -1 && it.indexInAdapter < itemCount)
                                    notifyItemChanged(it.indexInAdapter)
                            }
                            lastSelected = null

                            lastSelected = obj

                            obj.isSelected = true

                            holder.checkSelected.isVisible = true
                        }

                    }
                }

                holder.forMedia.setOnLongClickListener {
                    listener.onLongPress(obj.path)
                    it.setOnTouchListener { _, p1 ->
                        if (p1?.action == MotionEvent.ACTION_UP) {
                            listener.onLongPress("")
                            it.setOnTouchListener(null)
                        } else if (p1?.action == MotionEvent.ACTION_CANCEL) {
                            listener.onLongPress("")
                            it.setOnTouchListener(null)
                        }
                        true
                    }
                    true
                }
            }
        } catch (ex: Exception) {
            Log.e("error", "onBindViewHolder: ", ex)
        }
    }

    private var lastSelected: GalleryChildModel? = null

    fun removeCounter(obj: GalleryChildModel, index: Int) {
//        globalSelectionCounter--
//        obj.globalSelectionNumber = globalSelectionCounter
        obj.selectedImageCounter -= 1
       // obj.globalSelectionNumber -= 1
        if (index < myList.size && index >= 0)
            notifyItemChanged(index)
    }

    fun addList(newMediaList: MutableList<GalleryChildModel>) {

        val currentSize: Int = myList.size
        myList.clear()
        myList.addAll(newMediaList)
        notifyItemRangeRemoved(0, currentSize)
        notifyItemRangeInserted(0, newMediaList.size)
    }


    interface OnItemClick {
        fun onMediaClick(obj: GalleryChildModel): Int
        fun onMediaRemove(obj: GalleryChildModel, index: Int)
        fun onLongPress(path: String)
        fun onCameraClick()
        fun onLimitReached()
    }
}