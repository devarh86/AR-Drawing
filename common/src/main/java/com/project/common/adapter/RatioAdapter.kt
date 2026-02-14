package com.project.common.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.project.common.R
import com.project.common.databinding.ItemRatioButtonBinding
import com.project.common.model.RatioItem

class RatioAdapter(
    private val ratioList: List<RatioItem>,
    private val onRatioSelected: (String) -> Unit
) : RecyclerView.Adapter<RatioAdapter.RatioViewHolder>() {

    // ViewHolder to represent each item
    inner class RatioViewHolder(private val binding: ItemRatioButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ratioItem: RatioItem) {
            // Set image and text for each item
            binding.ratioImageView.setImageResource(ratioItem.imageResId)
            binding.ratioTextView.text = ratioItem.ratioText

            // Change the appearance based on selection
            val textColor = if (ratioItem.isSelected) {
                ContextCompat.getColor(itemView.context, R.color.selected_color)
            } else {
//                ContextCompat.getColor(itemView.context, R.color.black)
                ContextCompat.getColor(itemView.context, R.color.un_selected_new)
            }
            binding.ratioImageView.imageTintList = ColorStateList.valueOf(textColor)
            binding.ratioTextView.setTextColor(textColor)

            // Set click listener to update selected ratio
            binding.root.setOnClickListener {
                onRatioSelected(ratioItem.ratioText)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatioViewHolder {
        val binding = ItemRatioButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RatioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RatioViewHolder, position: Int) {
        holder.bind(ratioList[position])
    }

    override fun getItemCount(): Int = ratioList.size
}

