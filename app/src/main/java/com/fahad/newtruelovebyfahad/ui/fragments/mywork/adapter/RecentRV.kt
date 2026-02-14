package com.fahad.newtruelovebyfahad.ui.fragments.mywork.adapter

import android.annotation.SuppressLint
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
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.databinding.MyWorkStaggeredScreenRowItemBinding
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.repo.api.apollo.helper.ApiConstants
import com.project.common.repo.room.helper.RecentTypeConverter
import com.project.common.repo.room.model.RecentsModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.enums.PurchaseTag

class RecentRV(
    private val mContext: Context?,
    private var dataList: List<RecentsModel> = emptyList(),
    private val recentRVCallback: (item: RecentsModel) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        RecentViewHolder(
            MyWorkStaggeredScreenRowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with((holder as RecentViewHolder).binding) {
            if (dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let {
                    RecentTypeConverter.fromJson(
                        it.frame
                    )?.let { item ->
                        try {
                            Glide
                                .with(mContext ?: contentIv.context)
                                .asBitmap()
                                .override(600)
                                .placeholder(
                                    when (item.thumbtype.lowercase()) {
                                        FrameThumbType.PORTRAIT.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_portrait
                                        FrameThumbType.LANDSCAPE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_landscape
                                        FrameThumbType.SQUARE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_squre
                                        else -> com.project.common.R.drawable.frame_placeholder_portrait
                                    }
                                )
                                .load("${item.baseUrl}${item.thumb}")
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap, transition: Transition<in Bitmap>?
                                    ) {
                                        contentIv.setImageBitmap(resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })

                            val purchaseTagList = item.tags ?: PurchaseTag.FREE.tag

                            item.let { frameItem ->
                                if (!isProVersion() && frameItem.tags?.isNotEmpty() == true && frameItem.tags != PurchaseTag.FREE.tag && !ConstantsCommon.rewardedAssetsList.contains(
                                        frameItem.id
                                    )
                                ) {
                                    when {
                                        purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
                                            purchaseTagIv.apply {
                                                setImageResource(com.project.common.R.drawable.ic_pro_tag)
                                                visible()
                                            }
                                        }

                                        purchaseTagList.contains((PurchaseTag.REWARDED.tag)) -> purchaseTagIv.apply {
                                            setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
                                            visible()
                                        }

                                        purchaseTagList.contains((PurchaseTag.FREE.tag)) -> purchaseTagIv.apply {
                                            invisible()
                                        }

                                        else -> {
                                            purchaseTagIv.apply { invisible() }
                                        }
                                    }
                                } else {
                                    purchaseTagIv.apply {
                                        invisible()
                                    }
                                }
                            }
                            holder.itemView.setSingleClickListener {
                                recentRVCallback.invoke(it)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(dataList: List<RecentsModel>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class RecentViewHolder(val binding: MyWorkStaggeredScreenRowItemBinding) :
        ViewHolder(binding.root)
}