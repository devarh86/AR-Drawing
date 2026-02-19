package com.abdul.pencil_sketch.main.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.abdul.pencil_sketch.databinding.ItemSliderHowSketchBinding
import com.project.common.R

class SliderAdapterSketch(
    private var list: List<SliderItemSketch>
) : RecyclerView.Adapter<SliderAdapterSketch.SliderViewHolder>() {

    private var selectedPosition = 0

    inner class SliderViewHolder(val binding: ItemSliderHowSketchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val binding = ItemSliderHowSketchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SliderViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.binding.imageView.setImageResource(list[position].imageRes)

        if (position == selectedPosition) {
            holder.binding.root.strokeWidth = 6
            holder.binding.root.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.selected_color)
        } else {
            holder.binding.root.strokeWidth = 0
        }

    }

    fun setSelectedPosition(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(position)
    }

}
