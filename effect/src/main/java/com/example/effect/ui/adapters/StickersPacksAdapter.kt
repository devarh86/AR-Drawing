package com.example.effect.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.effect.R
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.project.common.repo.api.apollo.helper.ApiConstants
import com.example.effect.utils.setOnSingleClickListener


class StickersPacksAdapter(
    private val listener: OnItemClick,
) :
    RecyclerView.Adapter<StickersPacksAdapter.ViewHolder>() {

    private var myRecyclerView: RecyclerView? = null

    val myList: MutableList<GetStickersQuery.Sticker> = mutableListOf()

    var rewarded: Boolean = false

    var lastSelected: Int? = null

    var isThisCategory = false

    var secondLastSelected: Int? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        val packImg: ImageFilterView = view.findViewById(R.id.packImg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.sticker_packs_item, parent, false)
        )
    }

    override fun getItemCount(): Int {

        return myList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position < myList.size && position >= 0) {

            val obj = myList[position]

            holder.packImg.load(if (obj.title == "offline") obj.file else ApiConstants.BASE_URL_MEDIA + obj.file) {
                crossfade(true)
            }


//            val checker = "free"
//            if (obj.tag_title.isNotEmpty() && !obj.tag_title.equals(checker, true) && !isFromReward && !rewarded && !Constants.isProVersion()) {
//
//                holder.lockUnlockImg.isVisible = true
//                Glide.with(holder.itemView.context).load(obj.tag_img).apply {
//                    into(holder.lockUnlockImg)
//                }
//            } else {
//                holder.lockUnlockImg.isVisible = false
//            }
//
//            holder.selectedImg.isVisible = position == lastSelected
//
            holder.packImg.setOnSingleClickListener {
//                val tempVH =
//                    lastSelected?.let { myRecyclerView?.findViewHolderForAdapterPosition(it) }
//                tempVH?.let {
//                    if (it is ViewHolder) {
//                        it.selectedImg.isVisible = false
//                    }
//                }
                listener.onPackClick(obj, position)
//                lastSelected = position
//                holder.selectedImg.isVisible = true
//                listener.onOpacityClick(false, 0f)
            }
//
//            holder.selectedImg.setOnSingleClickListener {
//                listener.onOpacityClick(true, 0f)
//            }
//
//                if (obj.tag_title.isNotEmpty() && !obj.tag_title.equals(checker, true) && !isFromReward && !rewarded) {
//                    listener.onPackClick(obj, position)
//                }
//                else{
//                    val tempVH =
//                        lastSelected?.let { myRecyclerView?.findViewHolderForAdapterPosition(it) }
//                    tempVH?.let {
//                        if (it is ViewHolder) {
//                            setSelectedUnSelectedColor(true, it.itemView.context, it.forBack)
//                        }
//                    }
//                    listener.onPackClick(obj, position)
//                    lastSelected = position
//                    setSelectedUnSelectedColor(false, holder.itemView.context, holder.forBack)
//                }
//            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        myRecyclerView = recyclerView
    }

    fun pxFromDp(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

//    private fun setSelectedUnSelectedColor(reset: Boolean, context: Context, selection: ImageView) {
//
//        val drawable =
//            if (!reset) {
//                ContextCompat.getDrawable(context, R.drawable.rounded_stroke_blue_10dp)
//            } else
//                null
//
//        selection.background = drawable
//
//        selection.isVisible = !reset
//    }

    fun addList(newMediaList: List<GetStickersQuery.Sticker>) {

        val currentSize: Int = myList.size
        myList.clear()
        myList.addAll(newMediaList)
        notifyItemRangeRemoved(0, currentSize)
        notifyItemRangeInserted(0, newMediaList.size)
    }

    interface OnItemClick {
        fun onPackClick(pack: GetStickersQuery.Sticker, position: Int)
    }
}