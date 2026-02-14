package com.fahad.newtruelovebyfahad.ui.fragments.mywork

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.ads.crosspromo.helper.hide
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.SKU_LIST
import com.example.inapp.helpers.Constants.getProductDetailMicroValueNew
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentMyWorkBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.favourite.FavouriteFragment
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.MyWorkPagerAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs.RecentlyUsedFragment
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.material.tabs.TabLayoutMediator
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getProScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyWorkFragment : Fragment() {

    private var _binding: FragmentMyWorkBinding? = null
    private val binding get() = _binding!!
    private var myWorkPagerAdapter: MyWorkPagerAdapter? = null
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private val myWorkPagerCallbackListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            activity?.let {
                if (it is MainActivity)
                    it.reLoadBannerAdForFeature()
            }

            when (position) {
                1 -> {
                    firebaseAnalytics?.logEvent(Events.Screens.MY_WORK, Bundle().apply {
                        putString(
                            Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.DRAFT
                        )
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
                    })
                }

                2 -> {
                    firebaseAnalytics?.logEvent(Events.Screens.MY_WORK, Bundle().apply {
                        putString(
                            Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SAVED
                        )
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
                    })
                }

                else -> {
                    firebaseAnalytics?.logEvent(Events.Screens.MY_WORK, Bundle().apply {
                        putString(
                            Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.RECENTLY_USED
                        )
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.DISPLAYED)
                    })
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    fun showGoProBottomRv() {
        if (!isProVersion())
            _binding?.proTrailLay?.visible()
        else {
            _binding?.proTrailLay?.hide()
        }
    }

    fun hideGoProBottomRv() {
        _binding?.proTrailLay?.hide()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventForGalleryAndEditor(Events.Screens.MY_WORK, "", true)
        parentScreen = Events.Screens.MY_WORK
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyWorkBinding.inflate(inflater, container, false)
        _binding?.initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            isProVersion.observe(viewLifecycleOwner) {
                if (it) {
                    _binding?.let {
                        hideGoProBottomRv()
                    }
                }
            }

        } catch (ex: java.lang.Exception) {
            Log.e("error", "onViewCreated: ", ex)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!visibleAd)
            showScreenAds()
    }

    private fun FragmentMyWorkBinding.initViews() {
        initListeners()
        initViewPager()

        getProductDetailMicroValueNew(SKU_LIST[5])?.let { obj ->
            kotlin.runCatching {
                val remoteString: String = if (obj.isTrailActive) {
                    "3-days Free Trial"
                } else {
                    "Go Pro"
                }
                _binding?.trailDaysTxt?.text = remoteString

            }
        }
    }


    private fun FragmentMyWorkBinding.initListeners() {

        proTrailLay.setSingleClickListener {
            activity?.let {
                startActivity(Intent().apply {
                    setClassName(
                        it.applicationContext,
                        getProScreen()
                    )
                    putExtra("from_frames", false)
                    hideGoProBottomRv()
                })
            }
        }
        menuContainer.setSingleClickListener {
            try {
                activity?.navigateFragment(
                    MyWorkFragmentDirections.navWorkToSettingFragment(),
                    R.id.nav_mywork
                )
            } catch (ex: Exception) {
                Log.e("error", "initListeners: ", ex)
            }

        }
    }

    private fun FragmentMyWorkBinding.initViewPager() {
        myworkPager.visible()
        myWorkPagerAdapter = MyWorkPagerAdapter(this@MyWorkFragment)
        myworkPager.adapter = myWorkPagerAdapter
        myworkPager.offscreenPageLimit = 1
        myworkPager.isUserInputEnabled = true
        TabLayoutMediator(myworkTabLayout, myworkPager) { tab, position ->
            mContext?.let { context ->
                tab.text = when (position) {
                    1 -> ContextCompat.getString(
                        context,
                        com.project.common.R.string.menu_favourite
                    )

                    2 -> ContextCompat.getString(context, com.project.common.R.string.save)
                    3 -> ContextCompat.getString(context, com.project.common.R.string.draft)
                    else -> ContextCompat.getString(
                        context,
                        com.project.common.R.string.recently_used
                    )
                }
            }
        }.attach()
        myworkPager.registerOnPageChangeCallback(myWorkPagerCallbackListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.myworkPager?.unregisterOnPageChangeCallback(myWorkPagerCallbackListener)
    }

    private var visibleAd = false

    fun hideScreenAds() {
        if (isProVersion()) {
            _binding?.myworkPager?.let {
                childFragmentManager.fragments.forEach {
                    if (it is FavouriteFragment) {
                        it.hideAd()
                    } else if (it is RecentlyUsedFragment) {
                        it.hideAds()
                    }
                }
            }
        }
        visibleAd = true
        //  _binding?.bannerContainer?.invisible()
    }

    fun showScreenAds() {
        visibleAd = false
//        _binding?.bannerContainer?.visible()
//        if (!isProVersion()) {
//            _binding?.let {
//                if (!visibleAd) {
//                    _binding?.bannerContainer?.show()
//
//                    mActivity?.onResumeBanner(
//                        binding.adBannerContainer,
//                        binding.crossBannerIv,
//                        binding.bannerLayout.adContainer,
//                        binding.bannerLayout.shimmerViewContainer
//                    )
//                }
//            }
//        } else {
//            _binding?.bannerContainer?.gone()
//        }
    }
}