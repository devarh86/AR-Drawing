package com.fahad.newtruelovebyfahad.ui.fragments.learning.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fahad.newtruelovebyfahad.databinding.ItemSubFrameStepBinding

@Keep
data class SubFrameItem(
    val file: String,
    val baseUrl: String,
    val fullUrl: String
)

class SubFramesGridAdapter : RecyclerView.Adapter<SubFramesGridAdapter.SubFrameViewHolder>() {

    private val items: MutableList<SubFrameItem> = mutableListOf()

    inner class SubFrameViewHolder(private val binding: ItemSubFrameStepBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubFrameItem) {
            Glide.with(binding.subFrameIv.context)
                .load(item.fullUrl)
                .placeholder(com.project.common.R.drawable.frame_placeholder_squre)
                .error(com.project.common.R.drawable.frame_placeholder_squre)
                .into(binding.subFrameIv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubFrameViewHolder {
        val binding = ItemSubFrameStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubFrameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubFrameViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<SubFrameItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
