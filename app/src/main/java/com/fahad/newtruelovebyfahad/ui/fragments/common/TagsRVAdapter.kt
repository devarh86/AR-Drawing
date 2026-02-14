package com.fahad.newtruelovebyfahad.ui.fragments.common

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.TagsRowItemBinding
import com.project.common.utils.setOnSingleClickListener


class TagsRVAdapter(
    var dataList: List<TagModel>,
    private val onClick: (tag: TagModel, position: Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        FeaturedTagsViewHolder(
            TagsRowItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with((holder as FeaturedTagsViewHolder).binding) {
            if (dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let { item ->
                    tagName.background = ContextCompat.getDrawable(
                        root.context,
                        if (item.mSelected) R.drawable.corner_radius_bg_5dp_with_solid else com.project.common.R.drawable.corner_radius_bg_5dp_with_stroke
                    )
                    tagName.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (item.mSelected) R.color.white else  com.project.common.R.color.tab_txt_clr
                        )
                    )

                    tagName.text = item.tag

                    holder.itemView.setOnSingleClickListener {
                        item.mSelected = !item.mSelected
                        notifyItemChanged(position)
                        onClick.invoke(item, position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun updateDataList(dataList: List<String>?) {
        this.dataList = dataList?.map { tag ->
            TagModel(tag)
        } ?: emptyList()
        notifyDataSetChanged()
    }

    fun getCurrentTagsList() = dataList

    fun selectTag(tag: TagModel): Int {
        val position = dataList.indexOf(tag)
        dataList.filter { it.tag == tag.tag }.map { it.mSelected = tag.mSelected }
        notifyDataSetChanged()
        return position
    }

    inner class FeaturedTagsViewHolder(val binding: TagsRowItemBinding) :
        ViewHolder(binding.root)

    @Keep
    data class TagModel(var tag: String, var mSelected: Boolean = false)
}