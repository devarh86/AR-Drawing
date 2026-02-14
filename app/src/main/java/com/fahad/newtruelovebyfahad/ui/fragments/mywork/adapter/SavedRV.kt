package com.fahad.newtruelovebyfahad.ui.fragments.mywork.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fahad.newtruelovebyfahad.databinding.MyWorkSaveStaggeredScreenRowItemBinding
import com.fahad.newtruelovebyfahad.databinding.MyWorkStaggeredScreenRowItemBinding
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.repo.api.apollo.helper.ApiConstants

class SavedRV(
    private var mContext: Context?,
    private var dataList: List<Uri> = emptyList(),
    private val myWorkRVCallback: (uri: Uri) -> Unit,
    private val myWorkRVShareCallback: (uri: Uri) -> Unit,
    private val myWorkRVDeleteCallback: (uri: Uri) -> Unit
) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        SavedViewHolder(
            MyWorkSaveStaggeredScreenRowItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with((holder as SavedViewHolder).binding) {
            if(dataList.isNotEmpty() && dataList.size > position) {
                dataList[position].let {
                    try {
                        Glide
                            .with(mContext ?: contentIv.context)
                            .load(it)
                            .placeholder(com.project.common.R.drawable.frame_placeholder_squre)
                            .into(contentIv)
                        holder.itemView.setSingleClickListener {
                            myWorkRVCallback.invoke(it)
                        }
                        deleteIv.setSingleClickListener {
                            myWorkRVDeleteCallback.invoke(it)
                        }
                        shareIv.setSingleClickListener {
                            myWorkRVShareCallback.invoke(it)
                        }
                    }catch (_:Exception){}
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(mContext: Context, dataList: List<Uri>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size

    inner class SavedViewHolder(val binding: MyWorkSaveStaggeredScreenRowItemBinding) :
        ViewHolder(binding.root)
}