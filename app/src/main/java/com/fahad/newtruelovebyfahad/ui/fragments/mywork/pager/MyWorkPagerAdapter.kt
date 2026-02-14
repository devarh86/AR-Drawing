package com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.favourite.FavouriteFragment
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs.DraftFragment
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs.RecentlyUsedFragment
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs.SavedFragment

class MyWorkPagerAdapter(
    parentFragment: Fragment
) :
    FragmentStateAdapter(parentFragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            1 -> FavouriteFragment()
            2 -> SavedFragment()
            else -> RecentlyUsedFragment()
        }
}

//  2 -> DraftFragment()