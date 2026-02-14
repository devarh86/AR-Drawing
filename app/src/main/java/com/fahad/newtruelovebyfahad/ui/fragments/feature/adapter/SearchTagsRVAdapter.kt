package com.fahad.newtruelovebyfahad.ui.fragments.feature.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.project.common.utils.setOnSingleClickListener
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.StaggeredTagsRowItemBinding
import com.fahad.newtruelovebyfahad.ui.fragments.common.TagsRVAdapter

class SearchTagsRVAdapter(
    var dataList: List<TagsRVAdapter.TagModel>,
    private val onClick: (tag: TagsRVAdapter.TagModel, position: Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var filteredList: List<TagsRVAdapter.TagModel> = dataList

    fun filter(text: String) {
        filteredList = dataList.filter { it.tag.contains(text, true) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        FeaturedTagsViewHolder(
            StaggeredTagsRowItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with((holder as FeaturedTagsViewHolder).binding) {
            filteredList[position].let { item ->
                tagName.background = ContextCompat.getDrawable(
                    root.context,
                    if (item.mSelected) R.drawable.corner_radius_bg_5dp_with_solid else com.project.common.R.drawable.corner_radius_bg_5dp_with_stroke
                )
                tagName.text = item.tag
                tagName.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        if (item.mSelected) R.color.white else  com.project.common.R.color.tab_txt_clr
                    )
                )

                holder.itemView.setOnSingleClickListener {
                    item.mSelected = !item.mSelected
                    notifyItemChanged(position)
                    onClick.invoke(item, position)
                }
            }
        }
    }

    override fun getItemCount(): Int = filteredList.size

    /*fun updateDataList(dataList: List<String>?) {
        this.dataList = dataList?.map { tag -> TagsRVAdapter.TagModel(tag) } ?: emptyList()
        this.filteredList = this.dataList
        notifyDataSetChanged()
    }*/

    fun updateDataList(dataList: List<TagsRVAdapter.TagModel>?) {
        dataList?.let {
            this.dataList = it
            this.filteredList = this.dataList
            notifyDataSetChanged()
        }
    }

    inner class FeaturedTagsViewHolder(val binding: StaggeredTagsRowItemBinding) :
        ViewHolder(binding.root)
}