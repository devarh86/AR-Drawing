package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.animation.Animator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ads.admobs.utils.showNative
import com.example.ads.databinding.NativeLayoutPortraitRvBinding
import com.fahad.newtruelovebyfahad.GetHomeAndTemplateScreenDataQuery
import com.fahad.newtruelovebyfahad.GetMainScreenQuery
import com.fahad.newtruelovebyfahad.databinding.ChildRecyclerItemBinding
import com.fahad.newtruelovebyfahad.databinding.ChildRecyclerItemSingleBinding
import com.fahad.newtruelovebyfahad.utils.checkForSafety
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.project.common.utils.setOnSingleClickListener

class FramesRVLikeHome(
    private val mContext: Context?,
    private var dataList: ArrayList<Any>,
    private var nativeAd: NativeAd?,
    private val onClick: (item: GetMainScreenQuery.Frame, position: Int) -> Unit,
    private val onFavouriteClick: (item: FramesRV.FrameModel) -> Unit,
    private val onPurchaseTypeTagClick: (item: GetMainScreenQuery.Frame) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    // ViewHolder Types
    private val CONTENT_WITH_SLIDER = 0
    private val CONTENT_SINGLE_IMAGE = 1
    private val AD_VIEW = 2

    private var hideAd = false
    var nativeAlreadyShown = false
    var categoryName = "all"
    private var isScrollingRight = 0

    fun findPositionById(itemId: Int): Int {
        return dataList.indexOfFirst { it is FramesRV.FrameModel && it.frame.id == itemId }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            AD_VIEW -> AdViewHolder(
                NativeLayoutPortraitRvBinding.inflate(inflater, parent, false)
            )

            CONTENT_WITH_SLIDER -> {
                val binding = ChildRecyclerItemBinding.inflate(inflater, parent, false)
                val holder = SliderViewHolder(binding)
                // Initialize ViewPager adapter here
                val pagerAdapter = CoverSliderAdapter(onClick = {
                    // binding.useTemplateBtn.callOnClick()
                    binding.sliderRoot.callOnClick()
                })
                binding.coverSlider.adapter = pagerAdapter
                binding.coverSlider.offscreenPageLimit = 1
                holder
            }

            CONTENT_SINGLE_IMAGE -> {
                val binding = ChildRecyclerItemSingleBinding.inflate(inflater, parent, false)
                val holder = SingleImageViewHolder(binding)
                holder
            }

            else -> {
                Log.i("TAG", "Unknown view type: $viewType")
                val binding = ChildRecyclerItemSingleBinding.inflate(inflater, parent, false)
                val holder = SingleImageViewHolder(binding)
                holder
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (dataList.checkForSafety(position)) {
            dataList[position].let { item ->
                when {
                    item is FramesRV.FrameModel && holder is SliderViewHolder -> {
                        bindSliderViewHolder(holder, item, position)
                    }

                    item is FramesRV.FrameModel && holder is SingleImageViewHolder -> {
                        bindSingleImageViewHolder(holder, item, position)
                    }

                    item != "AD" && holder is AdViewHolder -> {
                        bindAdViewHolder(holder)
                    }
                }
            }
        }
    }

    private fun bindSliderViewHolder(
        holder: SliderViewHolder,
        item: FramesRV.FrameModel,
        position: Int
    ) {
        when (isScrollingRight) {
            1 -> nextAnim(holder.itemView)
            -1 -> previousAnim(holder.itemView)
            else -> {}
        }

        with(holder.binding) {
            imagesCoverCount.text = item.frame.scrlCount.toString()
            imagesMaskCount.text = item.frame.masks.toString()

            // Handle ViewPager content for slider
            coverSlider.adapter?.let { adapter ->
                if (adapter is CoverSliderAdapter) {
                    val mappedList = item.frame.scrl?.mapNotNull { scrl ->
                        scrl?.file?.let { file ->
                            GetHomeAndTemplateScreenDataQuery.Scrl(
                                baseUrl = scrl.baseUrl,
                                file = file
                            )
                        }
                    } ?: emptyList()

                    adapter.addList(mappedList)
                    setupDotsIndicator(holder.binding, mappedList.size, holder.itemView.context)
                }
            }

            // Common binding logic
            bindSliderContent(this, item, position, holder.itemView)
        }
    }

    private fun bindSingleImageViewHolder(
        holder: SingleImageViewHolder,
        item: FramesRV.FrameModel,
        position: Int
    ) {
        with(holder.binding) {
            imagesMaskCountSingle.text = item.frame.masks.toString()
            bindSingleContent(this, item, position, holder.itemView)
        }
    }

    private fun bindSingleContent(
        binding: ChildRecyclerItemSingleBinding,
        item: FramesRV.FrameModel,
        position: Int,
        itemView: View
    ) {
        with(binding) {
            try {
                Glide.with(contentIvSingle.context)
                    .asBitmap()
                    .override(500, 500)
                    .load("${item.frame.baseUrl}${item.frame.thumb}")
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onLoadStarted(placeholder: Drawable?) {
                            contentIvSingle.setImageDrawable(
                                ContextCompat.getDrawable(
                                    mContext ?: contentIvSingle.context,
                                    when (item.frame.thumbtype.lowercase()) {
                                        FrameThumbType.PORTRAIT.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_portrait
                                        FrameThumbType.LANDSCAPE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_landscape
                                        FrameThumbType.SQUARE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_squre
                                        else -> com.project.common.R.drawable.frame_placeholder_portrait
                                    }
                                )
                            )
                        }

                        override fun onResourceReady(
                            resource: Bitmap, transition: Transition<in Bitmap>?
                        ) {
                            contentIvSingle.setImageBitmap(resource)
                            itemView.setSingleClickListener {
                                onClick.invoke(item.frame, position)
                            }
                            /*   useTemplateBtn.setSingleClickListener {
                                   onClick.invoke(item.frame, position)
                               }
                               favouriteIv.setSingleClickListener {
                                   item.isFavourite = !item.isFavourite
                                   favouriteIv.setImageResource(if (item.isFavourite) R.drawable.ic_favourite_filled else R.drawable.ic_favourite_unselected_white)
                                   onFavouriteClick.invoke(item)
                               }
                               purchaseTagIv.setSingleClickListener {
                                   onPurchaseTypeTagClick.invoke(item.frame)
                               }*/

                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } catch (_: Exception) {
                Log.d("TAG", "onBindViewHolder: ")
            }


            // Handle purchase tags
//            val purchaseTagList = item.frame.tags ?: "Free"
//            if (!isProVersion() && item.frame.tags?.isNotEmpty() == true && item.frame.tags != "Free" && !ConstantsCommon.rewardedAssetsList.contains(item.frame.id)) {
//                when {
//                    purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
//                        purchaseTagIv.apply {
//                            setImageResource(com.project.common.R.drawable.ic_pro_tag)
//                            visible()
//                        }
//                    }
//                    purchaseTagList.contains((PurchaseTag.REWARDED.tag)) -> purchaseTagIv.apply {
//                        setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
//                        visible()
//                    }
//                    purchaseTagList.contains((PurchaseTag.FREE.tag)) -> purchaseTagIv.apply { invisible() }
//                }
//            } else {
//                purchaseTagIv.apply { invisible() }
//            }
//
//            favouriteIv.setImageResource(if (item.isFavourite) R.drawable.ic_favourite_filled else R.drawable.ic_favourite_unselected_white)
        }
    }

    private fun bindSliderContent(
        binding: ChildRecyclerItemBinding,
        item: FramesRV.FrameModel,
        position: Int,
        itemView: View
    ) {
        with(binding) {
            sliderRoot.setOnSingleClickListener {
                onClick.invoke(item.frame, position)
            }

//            itemView.setSingleClickListener {
//                 onClick.invoke(item.frame, position)
//            }
            /*         useTemplateBtn.setSingleClickListener {
                         onClick.invoke(item.frame, position)
                     }
                     favouriteIv.setSingleClickListener {
                         item.isFavourite = !item.isFavourite
                         favouriteIv.setImageResource(if (item.isFavourite) R.drawable.ic_favourite_filled else R.drawable.ic_favourite_unselected_white)
                         onFavouriteClick.invoke(item)
                     }
                     purchaseTagIv.setSingleClickListener {
                         onPurchaseTypeTagClick.invoke(item.frame)
                     }
                     // Handle purchase tags
                     val purchaseTagList = item.frame.tags ?: "Free"
                     if (!isProVersion() && item.frame.tags?.isNotEmpty() == true && item.frame.tags != "Free" && !ConstantsCommon.rewardedAssetsList.contains(item.frame.id)) {
                         when {
                             purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
                                 purchaseTagIv.apply {
                                     setImageResource(com.project.common.R.drawable.ic_pro_tag)
                                     visible()
                                 }
                             }
                             purchaseTagList.contains((PurchaseTag.REWARDED.tag)) -> purchaseTagIv.apply {
                                 setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
                                 visible()
                             }
                             purchaseTagList.contains((PurchaseTag.FREE.tag)) -> purchaseTagIv.apply { invisible() }
                         }
                     } else {
                         purchaseTagIv.apply { invisible() }
                     }
         */
            // favouriteIv.setImageResource(if (item.isFavourite) R.drawable.ic_favourite_filled else R.drawable.ic_favourite_unselected_white)
        }
    }

    private fun bindAdViewHolder(holder: AdViewHolder) {
        with(holder.binding) {
            nativeAd?.let {
                if (!hideAd && !nativeAlreadyShown) {
                    (mContext as? Activity)?.showNative(
                        com.example.ads.R.layout.native_ad_portrait_rv,
                        it,
                        loadedAction = {
                            nativeAlreadyShown = true
                            adContainer.visible()
                            shimmerViewContainer.gone()
                            adContainer.removeAllViews()
                            if (it?.parent != null) {
                                (it.parent as ViewGroup).removeView(it)
                            }
                            adContainer.addView(it)
                        },
                        failedAction = {
                            adContainer.visible()
                            shimmerViewContainer.visible()
                        })
                } else if (hideAd) {
                    Log.d("FAHAD", "showAppOpenAd: RV hide in rv happened")
                    adContainer.invisible()
                    shimmerViewContainer.invisible()
                } else {
                    Log.d("FAHAD", "showAppOpenAd: RV show in rv happened")
                    adContainer.visible()
                    shimmerViewContainer.invisible()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dataList[position] == "AD" -> AD_VIEW
            dataList[position] is FramesRV.FrameModel -> {
                val frameModel = dataList[position] as FramesRV.FrameModel
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
    private fun shouldUseSlider(frameModel: FramesRV.FrameModel): Boolean {
        return frameModel.frame.editor == "scrl" &&
                !frameModel.frame.scrl.isNullOrEmpty() &&
                frameModel.frame.scrlCount!! > 1
    }

    private fun setupDotsIndicator(
        holder: ChildRecyclerItemBinding,
        count: Int,
        context: Context
    ) {
        holder.dotsIndicator.removeAllViews()

        if (count <= 1) {
            holder.dotsIndicator.gone()
            return
        }

        holder.dotsIndicator.visible()
        val dots = arrayOfNulls<ImageView>(count)

        for (i in 0 until count) {
            dots[i] = ImageView(context)
            dots[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
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
                    context,
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
                            context,
                            if (i == position) com.project.common.R.drawable.s_selected
                            else com.project.common.R.drawable.s_unselected
                        )
                    )
                }
            }
        })
    }

    override fun getItemCount(): Int = dataList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataList(dataList: List<FramesRV.FrameModel>, scrollDirection: Int = 0) {
        isScrollingRight = scrollDirection
        this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    fun updateSingleItem(frame: FramesRV.FrameModel) {
        runCatching {
            dataList.add(frame)
            Log.d("FAHAD", "updateSingleItem: FRAME_RV = position = ${dataList.indices.last()}")
            notifyItemInserted(dataList.indices.last())
        }
    }

    fun updateAd(nativeAd: NativeAd?) {
        this.nativeAd = nativeAd
        if (dataList.contains("AD")) {
            val adPosition = dataList.indexOf("AD")
            hideAd = false
            nativeAlreadyShown = false
            notifyItemChanged(adPosition)
        }
    }

    fun hideRvAd() {
        kotlin.runCatching {
            notifyDataSetChanged()
            Log.i("TAG", "hideRvAd: notifyAdapter")
        }
    }

    fun showRvAd() {
        if (hideAd) {
            hideAd = false
            if (dataList.contains("AD")) {
                val adPosition = dataList.indexOf("AD")
                notifyItemChanged(adPosition)
            }
        }
    }

    fun clearData() {
        try {
            dataList = arrayListOf()
            notifyDataSetChanged()
        } catch (ex: java.lang.Exception) {
            Log.e("error", "clearData: ", ex)
        }
    }

    private fun previousAnim(view: View) {
        view.translationX = -view.width.toFloat() * 1.5f
        view.animate().translationX(0f).setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isScrollingRight = 0
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
    }

    private fun nextAnim(view: View) {
        view.translationX = view.width.toFloat() * 1.5f
        view.animate().translationX(0f).setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isScrollingRight = 0
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
    }

    fun getCurrentDataList() = dataList

    // ViewHolder Classes
    inner class SliderViewHolder(val binding: ChildRecyclerItemBinding) : ViewHolder(binding.root)
    inner class SingleImageViewHolder(val binding: ChildRecyclerItemSingleBinding) :
        ViewHolder(binding.root)

    inner class AdViewHolder(val binding: NativeLayoutPortraitRvBinding) : ViewHolder(binding.root)

//    class FrameModel(
//        var frame: GetMainScreenQuery.Frame,
//        var isFavourite: Boolean = false
//    )
}

