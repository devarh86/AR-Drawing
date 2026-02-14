package com.fahad.newtruelovebyfahad.ui.fragments.mywork.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.MyWorkStaggeredScreenRowItemBinding
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.db_table.FrameParentModel

class MyWorkRV(
    private val mContext: Context?,
    private var dataList: ArrayList<FrameParentModel>,
    private val onClick: (item: FrameParentModel) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        MyWorkViewHolder(
            MyWorkStaggeredScreenRowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with((holder as MyWorkViewHolder).binding) {
            if (dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let { item ->
                    try {
                        Glide
                            .with(mContext ?: contentIv.context)
                            .asBitmap()
                            .override(500)
                            .signature(ObjectKey(System.currentTimeMillis()))
                            .placeholder(com.project.common.R.drawable.frame_placeholder_portrait)
                            .load(item.thumbnailPath)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap, transition: Transition<in Bitmap>?
                                ) {
                                    contentIv.setImageBitmap(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })

                        holder.itemView.setSingleClickListener { onClick.invoke(item) }
                    } catch (_: Exception) {

                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun updateDataList(dataList: List<FrameParentModel>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    inner class MyWorkViewHolder(val binding: MyWorkStaggeredScreenRowItemBinding) :
        ViewHolder(binding.root)
}