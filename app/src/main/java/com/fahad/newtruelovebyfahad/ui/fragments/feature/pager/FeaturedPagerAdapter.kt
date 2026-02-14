package com.fahad.newtruelovebyfahad.ui.fragments.feature.pager

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.childs.ForYouFragment
import com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.childs.MostUsedFragment
import com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.childs.TodaySpecialFragment

class FeaturedPagerAdapter(
    parentFragment: Fragment
) : FragmentStateAdapter(parentFragment) {

    var todaySpecialFragment: TodaySpecialFragment? = null
    var mostUsedFragment: MostUsedFragment? = null
    var forYouFragment: ForYouFragment? = null

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment =

        when (position) {

            1 -> {
                todaySpecialFragment = TodaySpecialFragment()
                todaySpecialFragment ?: TodaySpecialFragment()
            }

            2 -> {
                mostUsedFragment = MostUsedFragment()
                mostUsedFragment ?: MostUsedFragment()
            }

            else -> {
                forYouFragment = ForYouFragment()
                Log.i("TAG", "onCreate: foryoufragmentAdapter $forYouFragment")
                forYouFragment ?: ForYouFragment()
            }
        }
}