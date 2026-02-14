package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.fahad.newtruelovebyfahad.databinding.CoverSliderItemBinding
import com.fahad.newtruelovebyfahad.databinding.SliderItemContainerBinding
import com.fahad.newtruelovebyfahad.ui.activities.pro.slider.SliderItem
import com.project.common.utils.setDrawable

class CoverSliderAdapter(
    //parent: String, navigateTo: String
    private val onClick: () -> Unit
) : RecyclerView.Adapter<ViewHolder>()
{
    private val dataList: MutableList<GetHomeAndTemplateScreenDataQuery.Scrl?> = mutableListOf()
    inner class SliderViewHolder(val binding: CoverSliderItemBinding) :
        ViewHolder(binding.root)

    fun addList(newMediaList: List<GetHomeAndTemplateScreenDataQuery.Scrl?>) {
        val currentSize: Int = dataList.size
        dataList.clear()
        dataList.addAll(newMediaList)
        notifyItemRangeRemoved(0, currentSize)
        notifyItemRangeInserted(0, newMediaList.size)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SliderViewHolder(
            CoverSliderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with((holder as SliderViewHolder).binding) {
            if (dataList.isNotEmpty() && dataList.size > position) {
                dataList[position]?.let {
                    Glide.with(imageSlider.context)
                        .load("${it.baseUrl}${it.file}")
                        .placeholder(holder.itemView.context.setDrawable(
                            com.project.common.R.drawable.frame_placeholder_squre))
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .into(imageSlider)
                    holder.itemView.setOnClickListener { _ ->
                       onClick.invoke()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}