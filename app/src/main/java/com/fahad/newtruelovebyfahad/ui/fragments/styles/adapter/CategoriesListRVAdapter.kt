package com.fahad.newtruelovebyfahad.ui.fragments.styles.adapter

import android.animation.Animator
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fahad.newtruelovebyfahad.databinding.CategoriesListItemBinding
import com.project.common.utils.setOnSingleClickListener


class CategoriesListRVAdapter(
    private var dataList: List<String>,
    private val onClick: (title: String, position: Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var mSelected = 0
    private var mLastSelected = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        CategoriesListViewHolder(
            CategoriesListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        with((holder as CategoriesListViewHolder).binding) {
            if (dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let { item ->
                    indicator.setBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            if (mSelected == position) com.project.common.R.color.selected_txt_clr else android.R.color.transparent
                        )
                    )
                    titleTv.text = item
                    titleTv.setTextColor(ContextCompat.getColor(root.context,if (mSelected == position) com.project.common.R.color.selected_txt_clr else com.project.common.R.color.tab_txt_clr))
                    holder.itemView.setOnSingleClickListener {
                        //notifyItemChanged(mSelected)
                        mLastSelected = mSelected
                        mSelected = position
                        //notifyItemChanged(mSelected)
                        onClick.invoke(
                            item,
                            mSelected
                        )
                    }
                }
            }
        }
    }

    fun updateDataList(dataList: List<String>?) {
        this.dataList = dataList ?: emptyList()
        notifyDataSetChanged()
    }

    fun selectNext(position: Int) {
        if (mSelected < dataList.size - 1) {
            mLastSelected = mSelected
            mSelected = position + 1
            //notifyItemChanged(mSelected)
            onClick.invoke(
                dataList[mSelected],
                mSelected
            )
        }
    }

    fun selectPrevious(position: Int) {
        if (mSelected > 0) {
            mLastSelected = mSelected
            mSelected = position - 1
            //notifyItemChanged(mSelected)
            onClick.invoke(
                dataList[mSelected],
                mSelected
            )
        }
    }

    fun select() {
        notifyItemChanged(mLastSelected)
        notifyItemChanged(mSelected)
    }

    fun unselect() {
        mSelected = mLastSelected
    }

    inner class CategoriesListViewHolder(val binding: CategoriesListItemBinding) :
        ViewHolder(binding.root)

}