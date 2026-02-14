package com.example.apponboarding.ui.main.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.apponboarding.ui.main.fragmet.FragmentOnBoardingFullNativeOne
import com.example.apponboarding.ui.main.fragmet.FragmentOnBoardingFullNativeTwo
import com.example.apponboarding.ui.main.fragmet.FragmentOnBoardingOne
import com.example.apponboarding.ui.main.fragmet.FragmentOnBoardingThree
import com.example.apponboarding.ui.main.fragmet.FragmentOnBoardingTwo
import com.example.inapp.helpers.Constants.isProVersion


/*class OnboardingAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = checkCount()

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentOnBoardingOne()

            1 -> if (loadNativeFullOne) {
                FragmentOnBoardingFullNativeOne()
            } else {
                FragmentOnBoardingTwo()
            }

            2 -> if (loadNativeFullOne) {
                FragmentOnBoardingTwo()
            } else if (loadNativeFullTwo) {
                FragmentOnBoardingFullNativeTwo()
            } else {
                FragmentOnBoardingThree()
            }

            3 -> if (loadNativeFullOne && loadNativeFullTwo) {
                FragmentOnBoardingFullNativeTwo()
            } else if (loadNativeFullOne || loadNativeFullTwo) {
                FragmentOnBoardingThree()
            } else {
                FragmentOnBoardingFour()
            }

            4 -> FragmentOnBoardingFour()
            5 -> FragmentOnBoardingFour()
            else -> FragmentOnBoardingFour()
        }
    }

    private fun checkCount(): Int {
        if (isProVersion()) {
            loadNativeFullOne = false
            loadNativeFullTwo = false
        }

        return when {
            loadNativeFullOne && loadNativeFullTwo -> 6
            loadNativeFullOne || loadNativeFullTwo -> 5
            else -> 4
        }
    }
}*/

class OnboardingAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = checkCount()

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentOnBoardingOne()
            1 -> if (loadNativeFullOne) FragmentOnBoardingFullNativeOne() else FragmentOnBoardingTwo()
            2 -> if (loadNativeFullOne) FragmentOnBoardingTwo() else if (loadNativeFullTwo) FragmentOnBoardingFullNativeTwo() else FragmentOnBoardingThree()
            3 -> if (!loadNativeFullOne && loadNativeFullTwo) FragmentOnBoardingThree() else if (loadNativeFullTwo) FragmentOnBoardingFullNativeTwo() else FragmentOnBoardingThree()
            4 -> FragmentOnBoardingThree()

            else -> FragmentOnBoardingThree()
        }
    }

    private fun checkCount(): Int {

        if (isProVersion()) {
            loadNativeFullOne = false
            loadNativeFullTwo = false
        }

        return if (loadNativeFullOne && loadNativeFullTwo) {
            5
        } else if (loadNativeFullOne) {
            4
        } else if (loadNativeFullTwo) {
            4
        } else {
            3
        }
    }

}
