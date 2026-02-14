package com.fahad.newtruelovebyfahad.utils.tabsync.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.TagsRowItemBinding
import com.project.common.utils.setOnSingleClickListener


class CategoryTagsAdapter(
    var dataList: List<String>,
    var mSelected: Int = 0,
    private val onClick: (position: Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    var mPrevious = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        FeaturedTagsViewHolder(
            TagsRowItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with((holder as FeaturedTagsViewHolder).binding) {
            if(dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let { item ->
                    tagName.background = ContextCompat.getDrawable(
                        root.context,
                        if (position == mSelected) R.drawable.corner_radius_bg_5dp_with_solid else com.project.common.R.drawable.corner_radius_bg_5dp_with_stroke
                    )
                    tagName.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            if (position == mSelected) R.color.white else  com.project.common.R.color.tab_txt_clr
                        )
                    )

                    tagName.text = item

                    holder.itemView.setOnSingleClickListener {
                        mPrevious = mSelected
                        mSelected = position
                        notifyItemChanged(mSelected)
                        notifyItemChanged(mPrevious)
                        onClick.invoke(mSelected)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun updateDataList(dataList: List<String>?) {
        this.dataList = dataList ?: emptyList()
        notifyDataSetChanged()
    }

    inner class FeaturedTagsViewHolder(val binding: TagsRowItemBinding) :
        ViewHolder(binding.root)
}