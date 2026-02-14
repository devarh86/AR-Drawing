package com.example.ads.crosspromo.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.ads.crosspromo.api.retrofit.model.CrossPromoItem
import com.example.ads.databinding.CrosspromoIconsRowItemBinding

class CrossPromoAppsRV(
    private var dataList: List<CrossPromoItem>,
    private val onImpression: (item: CrossPromoItem) -> Unit,
    private val onCLick: (item: CrossPromoItem) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        CrossPromoAppViewHolder(
            CrosspromoIconsRowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= 0 && position < dataList.size) {
            with((holder as CrossPromoAppViewHolder).binding) {
                if (dataList.isNotEmpty() && dataList.size > position) {
                    dataList[position].let {
                        onImpression.invoke(it)
                        Glide
                            .with(thumbnailIv)
                            .load(it.adFile)
                            .centerCrop()
                            .into(thumbnailIv)

                        holder.itemView.setOnClickListener { _ ->
                            onCLick.invoke(it)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun updateDataList(dataList: List<CrossPromoItem>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    inner class CrossPromoAppViewHolder(val binding: CrosspromoIconsRowItemBinding) :
        ViewHolder(binding.root)
}