package com.fahad.newtruelovebyfahad.ui.fragments.common

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.TagsRowItemBinding
import com.project.common.utils.setOnSingleClickListener


class CategoriesRVAdapter(
    var dataList: List<String>,
    private val onClick: (tag: String, position: Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var mSelected = 0
    private var mLastSelected = 0

    fun setSelectedIndex(index: Int) {
        kotlin.runCatching {
            val previous = mSelected
            mSelected = index
            notifyItemChanged(previous)
            notifyItemChanged(mSelected)
        }

    }

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
                        if (mSelected == position) com.project.common.R.drawable.rounded_red_btn_bg else com.project.common.R.drawable.corner_radius_bg_5dp_with_stroke
                    )
                    tagName.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (mSelected == position) com.project.common.R.color.white else com.project.common.R.color.text_color
                        )
                    )

                    tagName.text = item

                    holder.itemView.setOnSingleClickListener {
                        //notifyItemChanged(mSelected)
                        mLastSelected = mSelected
                        mSelected = position
                        //notifyItemChanged(mSelected)
                        onClick.invoke(item, mSelected)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun select() {
        notifyItemChanged(mLastSelected)
        notifyItemChanged(mSelected)
    }

    fun unselect() {
        mSelected = mLastSelected
    }

    fun updateDataList(dataList: List<String>?) {
        this.dataList = dataList ?: emptyList()
        notifyDataSetChanged()
    }

    fun getCurrentTagsList() = dataList

    inner class FeaturedTagsViewHolder(val binding: TagsRowItemBinding) :
        ViewHolder(binding.root)
}