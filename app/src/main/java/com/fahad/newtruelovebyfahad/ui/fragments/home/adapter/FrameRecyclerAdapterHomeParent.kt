package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.ParentItemBinding
import com.fahad.newtruelovebyfahad.databinding.ParentRecyclerItemBinding
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.project.common.viewmodels.FramesModelHomeAndTemplates
import com.project.common.viewmodels.ViewHolderTypes


class FrameRecyclerAdapterHomeParent(
    val onCategorySeeAllClick: (apiOption: String) -> Unit,
    val onThumbClick: (frameBody: GetHomeAndTemplateScreenDataQuery.Frame, position: Int, apiOption: String, tagTitle: String, categoryName: String) -> Unit,
) : RecyclerView.Adapter<BaseViewHolder>() {

    private var parentRecyclerView: RecyclerView? = null

    val items = mutableListOf<FramesModelHomeAndTemplates>()

    var clickedView: RecyclerView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.parent_recycler_item -> {
                val binding = ParentRecyclerItemBinding.inflate(inflater, parent, false)
                binding.childRecyclerView.setHasFixedSize(true)
                binding.childRecyclerView.layoutManager=
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

                binding.childRecyclerView.itemAnimator = null
                val recyclerAdapter = FrameRecyclerAdapterHomeChild(object :
                    FrameRecyclerAdapterHomeChild.OnItemClick {
                    override fun onPackClick(
                        frameBody: GetHomeAndTemplateScreenDataQuery.Frame,
                        position: Int,
                        tagTitle: String,
                        apiOption: String,
                        recyclerView: RecyclerView,
                        categoryName: String
                    ): Boolean {
                        clickedView = recyclerView
                        onThumbClick.invoke(frameBody, position, apiOption, tagTitle, categoryName)
                        return false
                    }
                })
                binding.childRecyclerView.adapter = recyclerAdapter
                TypeAViewHolder(binding)
            }

            R.layout.parent_item -> {
                val binding = ParentItemBinding.inflate(inflater, parent, false)
                TypeBViewHolder(binding)
            }

            else -> {
                val binding = ParentItemBinding.inflate(inflater, parent, false)
                TypeBViewHolder(binding)
            }
        }
    }

    fun updateNotifyChangesForChild(position: Int) {
        clickedView?.adapter?.let {
            if (it is FrameRecyclerAdapterHomeChild) {
                it.notifyItemChanged(position)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (position < items.size && position >= 0) {
            Log.e(""," onBindViewHolder $position")
            when (items[position].type) {
                ViewHolderTypes.FRAMES -> (holder as TypeAViewHolder).bind(
                    items[position],
                    position,
                    onCategorySeeAllClick
                )

                ViewHolderTypes.PHOTOEDITOR -> (holder as TypeBViewHolder).bind(
                    items[position],
                    position,
                    onCategorySeeAllClick
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < items.size && position >= 0) {
            when (items[position].type) {
                ViewHolderTypes.FRAMES -> R.layout.parent_recycler_item
                ViewHolderTypes.PHOTOEDITOR -> R.layout.parent_item
            }
        } else {
            R.layout.parent_recycler_item
        }
    }

    override fun getItemCount(): Int = items.size

    // Helper functions

    @SuppressLint("NotifyDataSetChanged")
    fun setList(newItems: List<FramesModelHomeAndTemplates>) {
        if (parentRecyclerView?.isComputingLayout == false) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
    }

    fun addItem(item: FramesModelHomeAndTemplates) {
        if (parentRecyclerView?.isComputingLayout == false) {
            items.add(item)
            notifyItemInserted(items.size - 1)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.parentRecyclerView = recyclerView
    }

    fun updateItem(position: Int, newItem: FramesModelHomeAndTemplates) {
        if (parentRecyclerView?.isComputingLayout == false) {
            if (position in items.indices) {
                items[position] = newItem
                notifyItemChanged(position)
            }
        }
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

abstract class BaseViewHolder(binding: Any) :
    RecyclerView.ViewHolder(if (binding is ParentRecyclerItemBinding) binding.root else (binding as ParentItemBinding).root) {
    abstract fun bind(
        item: FramesModelHomeAndTemplates,
        positionItem: Int,
        onCategorySeeAllClick: (apiOption: String) -> Unit
    )
}

class TypeAViewHolder(private val binding: ParentRecyclerItemBinding) :
    BaseViewHolder(binding) {
    override fun bind(
        item: FramesModelHomeAndTemplates,
        positionItem: Int,
        onCategorySeeAllClick: (apiOption: String) -> Unit
    ) {
        kotlin.runCatching {
            binding.apply {
                categoryName.text = item.categoryName
                if (!binding.childRecyclerView.isComputingLayout) {
                    binding.childRecyclerView.adapter?.let {
                        if (it is FrameRecyclerAdapterHomeChild) {
                            it.addList(item.frames)
                            it.categoryName = item.categoryName
                            it.apiOption = item.apiOption
                        }
                    }
                }
                seeAll.setSingleClickListener {
                    onCategorySeeAllClick.invoke(item.apiOption)
                }
            }
        }
    }
}

class TypeBViewHolder(private val binding: ParentItemBinding) :
    BaseViewHolder(binding) {
    override fun bind(
        item: FramesModelHomeAndTemplates,
        positionItem: Int,
        onCategorySeeAllClick: (apiOption: String) -> Unit
    ) {
        kotlin.runCatching {
            binding.apply {
                categoryName.text = item.categoryName
                thumbImg.setAnimation(item.thumbnail)
                thumbImg.loop(true)
                thumbImg.playAnimation()
                thumbImg.setSingleClickListener {
                    onCategorySeeAllClick.invoke(item.apiOption)
                }
            }
        }
    }
}