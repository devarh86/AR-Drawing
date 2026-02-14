package com.example.effect.ui.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.sticker.data.model.StickersPackByCategories
import com.example.effect.ui.fragment.StickerPacks

class ViewPagerAdapter(
    fragment: Fragment,
    private val pack: MutableList<StickersPackByCategories>
) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = pack.size

    override fun createFragment(position: Int): Fragment {

        val bundle = Bundle()
        bundle.putInt("position", position)
        val frag = StickerPacks()
        frag.arguments = bundle

        return frag
    }
}