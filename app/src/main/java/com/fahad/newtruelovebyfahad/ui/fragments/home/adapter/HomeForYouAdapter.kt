package com.fahad.newtruelovebyfahad.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ads.admobs.utils.showNative
import com.example.ads.databinding.NativeLayoutPortraitRvBinding
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.GetFeatureScreenQuery
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.StaggeredScreenRowItemHomeBinding
import com.fahad.newtruelovebyfahad.utils.checkForSafety
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.enums.PurchaseTag

class HomeForYouAdapter(
    private var mContext: Context?,
    var dataList: ArrayList<Any>,
    private var nativeAd: NativeAd?,
    private val onClick: (item: GetFeatureScreenQuery.Frame, position: Int) -> Unit,
    private val onFavouriteClick: (item: FrameModel) -> Unit,
    private val onPurchaseTypeTagClick: (item: GetFeatureScreenQuery.Frame) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    private val CONTENT = 0
    private val AD_VIEW = 1
    private var hideAd = false
    private var currentPosition = 0
    var nativeAlreadyShown = false

    var categoryName = "all"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            AD_VIEW -> AdViewHolder(
                NativeLayoutPortraitRvBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> FeaturedViewHolder(
                StaggeredScreenRowItemHomeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        currentPosition = position

        if (dataList.checkForSafety(position)) {
            try {
                var isPro = false
                dataList[position].let { item ->
                    if (item is FrameModel) {
                        if (holder is FeaturedViewHolder) {
                            with(holder.binding) {
                                try {
                                    Glide
                                        .with(contentIv.context)
                                        .asBitmap()
                                        .load("${item.frame.baseUrl}${item.frame.thumb}")
                                        .into(object : CustomTarget<Bitmap>() {
                                            override fun onLoadStarted(placeholder: Drawable?) {
                                                contentIv.setImageDrawable(
                                                    ContextCompat.getDrawable(
                                                        mContext ?: contentIv.context,
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
                                                runCatching {
                                                    contentIv.setImageBitmap(resource)

                                                    purchaseTagIv.isVisible = isPro

                                                    holder.itemView.setSingleClickListener {
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
                                                }
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {}
                                        })
                                } catch (_: Exception) {
                                }

                                val purchaseTagList = item.frame.tags ?: PurchaseTag.FREE.tag

                                item.frame.let { frameItem ->
                                    if (!isProVersion() && frameItem.tags?.isNotEmpty() == true && frameItem.tags != PurchaseTag.FREE.tag && !ConstantsCommon.rewardedAssetsList.contains(
                                            frameItem.id
                                        )
                                    ) {
                                        when {
                                            purchaseTagList.contains((PurchaseTag.PRO.tag)) -> {
                                                purchaseTagIv.apply {
                                                    setImageResource(com.project.common.R.drawable.ic_pro_tag)
                                                    visible()
                                                    isPro = true
                                                }
                                            }

                                            purchaseTagList.contains((PurchaseTag.REWARDED.tag)) -> purchaseTagIv.apply {
                                                setImageResource(com.project.common.R.drawable.ic_rewarded_tag)
                                                visible()
                                                isPro = true
                                            }

                                            purchaseTagList.contains((PurchaseTag.FREE.tag)) -> purchaseTagIv.apply {
                                                isPro = false
                                                invisible()
                                            }

                                            else -> {
                                                isPro = false
                                                purchaseTagIv.apply { invisible() }
                                            }
                                        }
                                    } else {
                                        purchaseTagIv.apply {
                                            isPro = false
                                            invisible()
                                        }
                                    }
                                }

                                favouriteIv.setImageResource(if (item.isFavourite) R.drawable.ic_favourite_filled else R.drawable.ic_favourite_unselected_white)
                            }
                        }
                    } else {
                        if (holder is AdViewHolder) {
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
                                            }
                                        )
                                    } else if (hideAd) {
                                        adContainer.post { adContainer.invisible() }
                                        shimmerViewContainer.invisible()
                                    } else {
                                        adContainer.visible()
                                        shimmerViewContainer.invisible()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ex: java.lang.Exception) {
                Log.e("TAG", "onBindViewHolder: $ex")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] == "AD") AD_VIEW else CONTENT
    }

    override fun getItemCount(): Int = dataList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateDataList(dataList: List<FrameModel>) {
        this.dataList.clear()
        this.dataList.addAll(dataList)
        notifyDataSetChanged()
    }

    fun updateSingleItem(frame: FrameModel) {
        this.dataList.add(frame)
        notifyItemInserted(dataList.indices.last)
        /*if (dataList.size == 3 && mContext.isNetworkAvailable() && !isProVersion()) {
            nativeAlreadyShown = false
            dataList.add("AD")
            notifyItemInserted(dataList.indices.last)
        }*/
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
//        if (!hideAd) {
//            hideAd = true
//            if (dataList.contains("AD")) {
//                val adPosition = dataList.indexOf("AD")
//                notifyItemChanged(adPosition)
//            }
//        }
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

    fun getCurrentPosition(): Int {
        return currentPosition
    }

    fun clearData() {
        this.dataList.clear()
        notifyDataSetChanged()
    }

    fun getCurrentDataList() = dataList.filterIsInstance<FrameModel>()

    inner class FeaturedViewHolder(val binding: StaggeredScreenRowItemHomeBinding) :
        ViewHolder(binding.root) {}

    inner class AdViewHolder(val binding: NativeLayoutPortraitRvBinding) :
        ViewHolder(binding.root)

    @Keep
    class FrameModel(
        var frame: GetFeatureScreenQuery.Frame,
        var isFavourite: Boolean = false,
        var isAd: Boolean = false
    )
}