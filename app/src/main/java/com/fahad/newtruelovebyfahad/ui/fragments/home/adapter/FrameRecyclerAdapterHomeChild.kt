package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.enums.PurchaseTag
import com.project.common.utils.setDrawable


class FrameRecyclerAdapterHomeChild(
    private val listener: OnItemClick,
) : RecyclerView.Adapter<ViewHolder>() {
    var apiOption: String = ""
    private var myRecyclerView: RecyclerView? = null
    var categoryName: String = ""
    private val myList: MutableList<GetHomeAndTemplateScreenDataQuery.Frame?> = mutableListOf()

    private val CONTENT_WITH_SLIDER = 0
    private val CONTENT_SINGLE_IMAGE = 1


    inner class ViewHolderSlider(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        val coverImg: AppCompatImageView = view.findViewById(R.id.content_iv)
        val purchaseImg: AppCompatImageView = view.findViewById(R.id.purchase_tag_iv)
        val coverSlider: ViewPager2 = view.findViewById(R.id.cover_slider)
        val dotsIndicator: LinearLayout = view.findViewById(R.id.dots_indicator)
        val maskCount: AppCompatTextView = view.findViewById(R.id.images_mask_count)
        val sliderCount: AppCompatTextView = view.findViewById(R.id.images_cover_count)

    }

    inner class ViewHolderSingle(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        val coverImg: AppCompatImageView = view.findViewById(R.id.content_iv_single)
        val purchaseImg: AppCompatImageView = view.findViewById(R.id.purchase_tag_iv_single)
        val maskCount: AppCompatTextView = view.findViewById(R.id.images_mask_count_single)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CONTENT_WITH_SLIDER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.child_recycler_item, parent, false)
                val holder = ViewHolderSlider(view)
                val pagerAdapter = CoverSliderAdapter(onClick = {
                    holder.itemView.callOnClick()
                })
                holder.coverSlider.adapter = pagerAdapter
                holder.coverSlider.offscreenPageLimit = 1
                return holder
            }

            CONTENT_SINGLE_IMAGE -> {
                ViewHolderSingle(
                    inflater.inflate(
                        R.layout.child_recycler_item_single,
                        parent,
                        false
                    )
                )
            }

            else -> {
                Log.i("TAG", "Unknown view type: $viewType")
                ViewHolderSingle(
                    inflater.inflate(
                        R.layout.child_recycler_item_single,
                        parent,
                        false
                    )
                )
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when {
            myList[position] is GetHomeAndTemplateScreenDataQuery.Frame? -> {
                val frameModel = myList[position] as GetHomeAndTemplateScreenDataQuery.Frame?
                // Determine if this item should use slider or single image
                when {
                    shouldUseSlider(frameModel) -> CONTENT_WITH_SLIDER
                    else -> CONTENT_SINGLE_IMAGE
                }
            }

            else -> CONTENT_SINGLE_IMAGE
        }
    }

    // Logic to determine which ViewHolder to use
    private fun shouldUseSlider(frameModel: GetHomeAndTemplateScreenDataQuery.Frame?): Boolean {
        return frameModel?.editor == "scrl" &&
                !frameModel.scrl.isNullOrEmpty() &&
                frameModel.scrlCount!! > 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < myList.size && position >= 0) {
            kotlin.runCatching {
                myList[position]?.let { currentItemObj ->
                    when {
                        holder is ViewHolderSlider -> {
                            bindSliderViewHolder(holder, currentItemObj, position)
                        }

                        holder is ViewHolderSingle -> {
                            bindSingleViewHolder(holder, currentItemObj, position)
                        }
                    }
                }

            }
        }
    }

    private fun bindSingleViewHolder(
        holder: ViewHolderSingle,
        currentItemObj: GetHomeAndTemplateScreenDataQuery.Frame,
        position: Int
    ) {
        holder.maskCount.text = currentItemObj.masks.toString()
        Glide.with(holder.itemView.context)
            .load("${currentItemObj.baseUrl}${currentItemObj.thumb}")
            .placeholder(
                when (currentItemObj.thumbtype.lowercase()) {
                    FrameThumbType.PORTRAIT.type.lowercase() -> holder.itemView.context.setDrawable(
                        com.project.common.R.drawable.frame_placeholder_portrait
                    )

                    FrameThumbType.LANDSCAPE.type.lowercase() -> holder.itemView.context.setDrawable(
                        com.project.common.R.drawable.frame_placeholder_landscape
                    )

                    FrameThumbType.SQUARE.type.lowercase() -> holder.itemView.context.setDrawable(
                        com.project.common.R.drawable.frame_placeholder_squre
                    )

                    else -> holder.itemView.context.setDrawable(com.project.common.R.drawable.frame_placeholder_portrait)
                }
            )
            .apply {
                into(holder.coverImg)

            }
        var purchaseTagList = currentItemObj.tags ?: "Free"

        if (!isProVersion() && currentItemObj.tags?.isNotEmpty() == true && currentItemObj.tags != "Free") {
            when {
                purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
                    holder.purchaseImg.apply {
                        setImageResource(com.project.common.R.drawable.ic_pro_tag)
                        visible()
                    }
                }

                purchaseTagList.contains(
                    (PurchaseTag.REWARDED.tag)
                ) && !ConstantsCommon.rewardedAssetsList.contains(
                    currentItemObj.id
                ) -> {
                    holder.purchaseImg.apply {
                        setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
                        visible()
                    }
                }

                else -> {
                    purchaseTagList = "Free"
                    holder.purchaseImg.apply { gone() }
                }
            }
        } else {
            purchaseTagList = "Free"
            holder.purchaseImg.apply { gone() }
        }

        holder.itemView.setSingleClickListener {
            if (position < myList.size) {
                val obj = myList[position]
                myRecyclerView?.let { recyclerView ->
                    obj?.let {
                        listener.onPackClick(
                            it,
                            position,
                            purchaseTagList,
                            apiOption,
                            recyclerView,
                            categoryName
                        )
                    }
                }
            }
        }
        holder.coverImg.setSingleClickListener {
            if (position < myList.size) {
                val obj = myList[position]
                myRecyclerView?.let { recyclerView ->
                    obj?.let {
                        listener.onPackClick(
                            it,
                            position,
                            purchaseTagList,
                            apiOption,
                            recyclerView,
                            categoryName
                        )
                    }
                }
            }
        }
    }

    private fun bindSliderViewHolder(
        holder: ViewHolderSlider,
        currentItemObj: GetHomeAndTemplateScreenDataQuery.Frame,
        position: Int
    ) {
        holder.coverSlider.adapter?.let {
            if (it is CoverSliderAdapter) {
                currentItemObj.scrl?.let { it1 -> it.addList(it1) }
                setupDotsIndicator(holder, it.itemCount)
            }
        }
        holder.maskCount.text = currentItemObj.masks.toString()
        holder.sliderCount.text = currentItemObj.scrl?.size.toString()
        var purchaseTagList = currentItemObj.tags ?: "Free"

        if (!isProVersion() && currentItemObj.tags?.isNotEmpty() == true && currentItemObj.tags != "Free") {
            when {
                purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
                    holder.purchaseImg.apply {
                        setImageResource(com.project.common.R.drawable.ic_pro_tag)
                        visible()
                    }
                }

                purchaseTagList.contains(
                    (PurchaseTag.REWARDED.tag)
                ) && !ConstantsCommon.rewardedAssetsList.contains(
                    currentItemObj.id
                ) -> {
                    holder.purchaseImg.apply {
                        setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
                        visible()
                    }
                }

                else -> {
                    purchaseTagList = "Free"
                    holder.purchaseImg.apply { gone() }
                }
            }
        } else {
            purchaseTagList = "Free"
            holder.purchaseImg.apply { gone() }
        }

        holder.itemView.setSingleClickListener {
            if (position < myList.size) {
                val obj = myList[position]
                myRecyclerView?.let { recyclerView ->
                    obj?.let {

                        listener.onPackClick(
                            it,
                            position,
                            purchaseTagList,
                            apiOption,
                            recyclerView,
                            categoryName
                        )
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return myList.size
    }

    /*   override fun onBindViewHolder(holder: ViewHolder, position: Int) {

           if (position < myList.size && position >= 0) {
               kotlin.runCatching {
                   myList[position]?.let { currentItemObj ->
                       holder.coverSlider.adapter?.let {
                           if (it is CoverSliderAdapter) {
                               currentItemObj.scrl?.let { it1 -> it.addList(it1) }
                               setupDotsIndicator(holder, it.itemCount)
                           }
                       }
                       holder.maskCount.text = currentItemObj.masks.toString()
                       holder.sliderCount.text = currentItemObj.scrl?.size.toString()
   //                    Glide.with(holder.itemView.context)
   //                        .load("${currentItemObj.baseUrl}${currentItemObj.thumb}")
   //                        .placeholder(
   //                            when (currentItemObj.thumbtype.lowercase()) {
   //                                FrameThumbType.PORTRAIT.type.lowercase() -> holder.itemView.context.setDrawable(
   //                                    com.project.common.R.drawable.frame_placeholder_portrait
   //                                )
   //
   //                                FrameThumbType.LANDSCAPE.type.lowercase() -> holder.itemView.context.setDrawable(
   //                                    com.project.common.R.drawable.frame_placeholder_landscape
   //                                )
   //
   //                                FrameThumbType.SQUARE.type.lowercase() -> holder.itemView.context.setDrawable(
   //                                    com.project.common.R.drawable.frame_placeholder_squre
   //                                )
   //
   //                                else -> holder.itemView.context.setDrawable(com.project.common.R.drawable.frame_placeholder_portrait)
   //                            }
   //                        )
   //                        .apply {
                               //into(holder.coverImg)

                               var purchaseTagList = currentItemObj.tags ?: "Free"

                               if (!isProVersion() && currentItemObj.tags?.isNotEmpty() == true && currentItemObj.tags != "Free") {
                                   when {
                                       purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
                                           holder.purchaseImg.apply {
                                               setImageResource(com.project.common.R.drawable.ic_pro_tag)
                                               visible()
                                           }
                                       }

                                       purchaseTagList.contains(
                                           (PurchaseTag.REWARDED.tag)
                                       ) && !ConstantsCommon.rewardedAssetsList.contains(
                                           currentItemObj.id
                                       ) -> {
                                           holder.purchaseImg.apply {
                                               setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
                                               visible()
                                           }
                                       }

                                       else -> {
                                           purchaseTagList = "Free"
                                           holder.purchaseImg.apply { gone() }
                                       }
                                   }
                               } else {
                                   purchaseTagList = "Free"
                                   holder.purchaseImg.apply { gone() }
                               }

                               holder.itemView.setSingleClickListener {
                                   if (position < myList.size) {
                                       val obj = myList[position]
                                       myRecyclerView?.let { recyclerView ->
                                           obj?.let {

                                               listener.onPackClick(
                                                   it,
                                                   position,
                                                   purchaseTagList,
                                                   apiOption,
                                                   recyclerView,
                                                   categoryName
                                               )
                                           }
                                       }
                                   }
                               }
   //                        }
                   }
               }
           }
       }
       */

    private fun setupDotsIndicator(holder: ViewHolderSlider, count: Int) {
        holder.dotsIndicator.removeAllViews()

        if (count <= 1) {
            holder.dotsIndicator.gone()
            return
        }

        holder.dotsIndicator.visible()
        val dots = arrayOfNulls<ImageView>(count)

        for (i in 0 until count) {
            dots[i] = ImageView(holder.itemView.context)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    com.project.common.R.drawable.s_unselected
                )
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(6, 0, 6, 0)
            holder.dotsIndicator.addView(dots[i], params)

        }
        // Set first dot as active
        if (dots.isNotEmpty()) {
            dots[0]?.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.itemView.context,
                    com.project.common.R.drawable.s_selected
                )
            )
        }
        // Register page change callback
        holder.coverSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Update dots
                for (i in dots.indices) {
                    dots[i]?.setImageDrawable(
                        ContextCompat.getDrawable(
                            holder.itemView.context,
                            if (i == position) com.project.common.R.drawable.s_selected
                            else com.project.common.R.drawable.s_unselected
                        )
                    )
                }
            }
        })
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        myRecyclerView = recyclerView
    }

    fun addList(newMediaList: List<GetHomeAndTemplateScreenDataQuery.Frame?>) {
        val currentSize: Int = myList.size
        myList.clear()
        myList.addAll(newMediaList)
        notifyItemRangeRemoved(0, currentSize)
        notifyItemRangeInserted(0, newMediaList.size)
    }

    interface OnItemClick {
        fun onPackClick(
            frameBody: GetHomeAndTemplateScreenDataQuery.Frame,
            position: Int,
            tagTitle: String,
            apiOption: String,
            recyclerView: RecyclerView,
            categoryName: String = ""
        ): Boolean
    }
}