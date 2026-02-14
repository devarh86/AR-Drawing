package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fahad.newtruelovebyfahad.databinding.SliderItemHomeBinding
import com.fahad.newtruelovebyfahad.ui.activities.pro.slider.SliderItem


class HomeSliderAdapter(
    private val dataList: ArrayList<SliderItem>,
    private val onClick: (navigateTo: String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    inner class SliderViewHolder(val binding: SliderItemHomeBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SliderViewHolder(
            SliderItemHomeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        when (holder) {
            is SliderViewHolder -> {
                item.image.let { resId ->

                    holder.binding.imageSlider.setImageResource(resId)
                    holder.binding.heading1.text=item.heading
                    holder.binding.heading2.text=item.subHeading
                    holder.binding.heading3.text=item.detail

                }
                holder.itemView.setOnClickListener {
                    onClick.invoke(item.editorCategory)
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}
