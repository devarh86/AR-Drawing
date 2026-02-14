package com.fahad.newtruelovebyfahad.ui.activities.pro.slider

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.fahad.newtruelovebyfahad.databinding.SliderItemContainerBinding

class SliderAdapter(
    private val dataList: ArrayList<SliderItem>,
    private val onClick: (parent: String, navigateTo: String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    inner class SliderViewHolder(val binding: SliderItemContainerBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SliderViewHolder(
            SliderItemContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with((holder as SliderViewHolder).binding) {
            if (dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let {
                    Glide.with(imageSlider.context).load(it.image).into(imageSlider)
                    titleText.text = it.heading
                    descriptionText.text = it.detail
                    holder.itemView.setOnClickListener { _ ->
                        onClick.invoke(it.editorCategory, it.detail)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size
}