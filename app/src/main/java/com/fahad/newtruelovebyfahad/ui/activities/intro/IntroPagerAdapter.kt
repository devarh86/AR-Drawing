package com.fahad.newtruelovebyfahad.ui.activities.intro

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.IntroItemBinding

class IntroAdapter(private val context: Context) :
    RecyclerView.Adapter<IntroAdapter.IntroItemViewHolder>() {
    private var dataList = ArrayList<Int>()

    init {
//        dataList.add(R.raw.intro_anim1)
//        dataList.add(R.raw.intro_anim2)
//        dataList.add(R.raw.intro_anim3)
//        dataList.add(R.raw.intro_anim4)
//        dataList.add(R.raw.intro_anim5)
//        dataList.add(R.raw.intro_anim6)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroItemViewHolder =
        IntroItemViewHolder(
            IntroItemBinding.inflate(LayoutInflater.from(context), parent, false)
        )

    override fun onBindViewHolder(holder: IntroItemViewHolder, position: Int) {
        with(holder.binding) {
            dataList[position].let {
                lottieAnimationView.setAnimation(it)
            }
        }
    }

    override fun getItemCount() = dataList.size

    inner class IntroItemViewHolder(val binding: IntroItemBinding) : RecyclerView.ViewHolder(binding.root)
}