package com.fahad.newtruelovebyfahad.utils.tabsync.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.fahad.newtruelovebyfahad.GetMainScreenQuery
import com.fahad.newtruelovebyfahad.databinding.CategoryRowItemBinding
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FramesRV

class CategoriesAdapter(
    private var listOfCategories: List<Category>,
    private val onClick: (item: GetMainScreenQuery.Frame) -> Unit,
    private val onFavouriteClick: (item: FramesRV.FrameModel) -> Unit,
    private val onPurchaseTypeTagClick: (item: GetMainScreenQuery.Frame) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            CategoryRowItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        with(holder.binding) {
            listOfCategories[position].let {
                /*framesRv.adapter = FramesRV(
                    it.items,
                    onClick = { onClick.invoke(it) },
                    onFavouriteClick = { onFavouriteClick.invoke(it) },
                    onPurchaseTypeTagClick = { onPurchaseTypeTagClick.invoke(it) }
                )*/
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfCategories.size
    }

    fun updateDataList(dataList: List<Category>?) {
        this.listOfCategories = dataList ?: emptyList()
        notifyDataSetChanged()
    }


    inner class CategoryViewHolder(val binding: CategoryRowItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    @Keep
    data class Category(val name: String, val items: List<FramesRV.FrameModel>)
}
