package com.project.sticker.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.ads.admobs.utils.showRewarded
import com.example.ads.crosspromo.helper.isNetworkAvailable
import com.example.inapp.helpers.Constants.isProVersion
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.utils.ConstantsCommon
import com.project.common.viewmodels.ApiViewModel
import com.project.sticker.databinding.FragmentStickerBinding
import com.project.sticker.datastore.StickerDataStore
import com.project.sticker.ui.adapters.ViewPagerAdapter
import com.project.sticker.ui.intent.StickerIntent
import com.project.sticker.ui.viewmodel.StickerViewModel
import com.project.sticker.ui.viewstate.StickersViewState
import com.project.sticker.utils.checkForSafety
import com.project.sticker.utils.createOrShowSnackBar
import com.project.sticker.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class Sticker : Fragment() {

    private var tabLayoutMediator: TabLayoutMediator? = null

    private var _binding: FragmentStickerBinding? = null

    private var callback: OnBackPressedCallback? = null

    @set:Inject
    lateinit var stickerDataStore: StickerDataStore

    private val binding get() = _binding!!

    private val stickerViewModel: StickerViewModel by activityViewModels()

    private val apiViewModel by activityViewModels<ApiViewModel>()

    private var mActivity: Activity? = null

    private var firstTime = true
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (_binding == null) {

            _binding = FragmentStickerBinding.inflate(inflater, container, false)
        }

        onBackPress()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {

            if (firstTime) {
                init()

                initClick()

                firstTime = false
            }

            observeData()

            val isInternetActive = mActivity?.isNetworkAvailable()
            apiViewModel.getStickers(isInternetActive ?: false)
            if (isInternetActive == true) {
                apiViewModel.stickers.observeOnce(viewLifecycleOwner) {
                    when (it) {
                        is Response.Success -> {
                            ConstantsCommon.stickersList = it.data

                            kotlin.runCatching {
                            if (stickerViewModel.stickersCategoriesAndData.isNotEmpty() && stickerViewModel.stickersCategoriesAndData.first().packList.isNotEmpty() && stickerViewModel.stickersCategoriesAndData.first().packList.first().file.contains(
                                    "file:///android_asset/"
                                )
                            )
                                stickerViewModel.stickersCategoriesAndData.clear()
}

                            lifecycleScope.launch(IO) {
                                stickerViewModel.cancelJobs()
                                stickerViewModel.stickerIntent.send(
                                    StickerIntent.GetStickers(
                                        context
                                    )
                                )
                            }
                        }

                        is Response.ShowSlowInternet -> {}

                        is Response.Loading -> {
                            binding.shimmerView.isVisible = true
                            binding.shimmerView.startShimmer()
                        }

                        is Response.Error -> {
                            binding.shimmerView.isVisible = false
                            binding.shimmerView.stopShimmer()
                            context?.createOrShowSnackBar(
                                binding.root,
                                0,
                                "Something went wrong",
                                true,
                                null
                            )
                        }
                    }
                }
            } else {
                apiViewModel.offlineStickers.observeOnce(viewLifecycleOwner) {
                    when (it) {
                        is Response.Success -> {
                            ConstantsCommon.stickersList = it.data

                            kotlin.runCatching {
                                if (stickerViewModel.stickersCategoriesAndData.isNotEmpty() && stickerViewModel.stickersCategoriesAndData.first().packList.isNotEmpty() && !stickerViewModel.stickersCategoriesAndData.first().packList.first().file.contains(
                                        "file:///android_asset/"
                                    )
                                )
                                    stickerViewModel.stickersCategoriesAndData.clear()
                            }
                            lifecycleScope.launch(IO) {
                                stickerViewModel.cancelJobs()
                                stickerViewModel.stickerIntent.send(
                                    StickerIntent.GetStickers(
                                        context
                                    )
                                )
                            }
                        }

                        is Response.ShowSlowInternet -> {}

                        is Response.Loading -> {
                            binding.shimmerView.isVisible = true
                            binding.shimmerView.startShimmer()
                        }

                        is Response.Error -> {
                            binding.shimmerView.isVisible = false
                            binding.shimmerView.stopShimmer()
                            context?.createOrShowSnackBar(
                                binding.root,
                                0,
                                "Something went wrong",
                                true,
                                null
                            )
                        }
                    }
                }
            }

        } catch (_: Exception) {
            Log.d("FAHAD", "initObservers: crash")
        }
    }

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    private fun init() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                stickerViewModel.lastTab = position
                if (stickerViewModel.stickersCategoriesAndData.checkForSafety(position)) {
                    binding.proLayout.isVisible =
                        stickerViewModel.stickersCategoriesAndData[position].tag == "pro" && !isProVersion()
                }
                super.onPageSelected(position)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initClick() {

        binding.crossImg.setOnSingleClickListener {
            backPress()
        }

        binding.rewardTxt.setOnSingleClickListener {
            binding.viewPager.currentItem.apply {
                if (stickerViewModel.stickersCategoriesAndData.checkForSafety(this)) {
                    mActivity?.showRewashowRewardedInterstitial(
                        loadedAction = {
                            _binding?.let {
                                stickerViewModel.stickersCategoriesAndData[binding.viewPager.currentItem].tag =
                                    "free"
                                binding.proLayout.isVisible = false
                                lifecycleScope.launch(IO) {
                                    if (stickerViewModel.stickersCategoriesAndData.checkForSafety(
                                            this@apply
                                        )
                                    ) {
                                        stickerDataStore.writeUnlockedId(stickerViewModel.stickersCategoriesAndData[binding.viewPager.currentItem].id)
                                    }
                                }
                            }
                        },
                        failedAction = {}
                    )
                }
            }
        }

        binding.proLayout.setOnSingleClickListener {}

        binding.proClickLayout.setOnSingleClickListener {
            activity?.let {
                runCatching {
                    val intent = Intent()
                    intent.setClassName(
                        it.applicationContext,
                        getProScreen()
                    )
                    it.startActivity(intent)
                }
            }
        }

        binding.tickImg.setOnSingleClickListener {
            stickerViewModel.updateTick()
        }
    }

    private fun observeData() {

        isProVersion.observe(viewLifecycleOwner) {
            it?.let {
                binding.proLayout.isVisible = false
            }
        }

        stickerViewModel.stickersLiveData.observe(viewLifecycleOwner) {
            when (it) {

                is StickersViewState.Idle -> {

                    Log.i("observeData", "observeData: idle")
                }

                is StickersViewState.Loading -> {

                    binding.shimmerView.isVisible = true
                    binding.shimmerView.startShimmer()

                    Log.i("observeData", "observeData: loading")
                }

                is StickersViewState.UpdateStickerObject -> {

                    Log.i("observeData", "observeData: ${it.obj}")
                }

                is StickersViewState.Error -> {
                    Log.i("observeData", "observeData: ${it.message}")
                }

                is StickersViewState.UpdateUi -> {
                    changeUiAccordingTheme(it.frameLayout)
                    stickerViewModel.resetStickerViewState()
                }

                is StickersViewState.Success -> {
                    Log.i("observeData", "observeData: ${it.list.size}")
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.isVisible = false

                    it.list.let {
                        if (it.isNotEmpty()) {
                            runCatching {
                                binding.viewPager.adapter =
                                    ViewPagerAdapter(this, it)
                                if (stickerViewModel.stickersCategoriesAndData.checkForSafety(
                                        stickerViewModel.lastTab
                                    )
                                ) {
                                    binding.proLayout.isVisible =
                                        stickerViewModel.stickersCategoriesAndData[stickerViewModel.lastTab].tag == "pro" && !isProVersion()
                                }

                                tabLayoutMediator = TabLayoutMediator(
                                    binding.tabLayout,
                                    binding.viewPager
                                ) { tab, position ->
                                    if (it.checkForSafety(position)) {
                                        kotlin.runCatching {
                                            tab.text = it[position].catName
                                        }
                                    }
                                }
                                tabLayoutMediator?.attach()

                                binding.viewPager.setCurrentItem(stickerViewModel.lastTab, false)
                            }
                        }
                    }
                    if (it.list.isNotEmpty() && it.list.size > stickerViewModel.lastTab) {
                        binding.proLayout.isVisible =
                            it.list[stickerViewModel.lastTab].tag != "free"
                    }
                    stickerViewModel.resetStickerViewState()
                }
            }
        }
    }

    fun Int.setColor(tab: TabLayout.Tab?) {
        kotlin.runCatching {
            context?.let { mContext ->
                tab?.apply {
                    val tabTextView =
                        (tab.view.getChildAt(1) as? TextView)
                    tabTextView?.setTextColor(
                        mContext.getColor(this@setColor)
                    )
                }
            }
        }
    }

    private fun changeUiAccordingTheme(frameLayout: FrameLayout) {
        kotlin.runCatching {
            if (_binding != null) {
                context?.let { cntx ->
//                    binding.crossImg.setColorFilter(
//                        ContextCompat.getColor(
//                            cntx,
//                            com.project.common.R.color.btn_icon_clr
//                        )
//                    )
//                    binding.tickImg.setColorFilter(
//                        ContextCompat.getColor(
//                            cntx,
//                            com.project.common.R.color.selected_txt_clr
//                        )
//                    )
//                    binding.viewPager.setBackgroundResource(com.project.sticker.R.drawable.rounded_10_dp)
//                    binding.crossImg.setColorFilter(
//                        ContextCompat.getColor(
//                            cntx,
//                            com.project.common.R.color.btn_icon_clr
//                        )
//                    )
//                    binding.stickerRootV.setBackgroundColor(
//                        ContextCompat.getColor(
//                            cntx,
//                            com.project.common.R.color.editor_bar_clr
//                        )
//                    )
//                    binding.tabLayout.setBackgroundColor(
//                        ContextCompat.getColor(
//                            cntx,
//                            com.project.common.R.color.editor_bar_clr
//                        )
//                    )

//                    recreateFragment(this, frameLayout)

//                    stickerViewModel.getSuccessState()


//                    binding.tabLayout.tabCount.apply {
//                        for (i in 0 until this) {
//                            kotlin.runCatching {
//                                binding.tabLayout.getTabAt(i).let {
//                                    it?.apply {
//                                        if (isSelected) {
//                                            com.project.common.R.color.selected_color.setColor(it)
//                                        } else {
//
//                                            com.project.common.R.color.tab_txt_clr.setColor(it)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    binding.viewPager.adapter = null
//                    binding.viewPager.adapter =
//                        ViewPagerAdapter(this, stickerViewModel.stickersCategoriesAndData)

//                    binding.tabLayout.setTabRippleColorResource(com.project.common.R.color.primary_50)
//                    binding.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(cntx,com.project.common.R.color.selected_color))
//                    binding.tabLayout.requestLayout()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.adapter = null
        callback?.remove()
    }

    private fun onBackPress() {

        callback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    backPress()
                }
            }
        callback?.let {
            activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner, it)
        }
    }

    private fun backPress() {
        stickerViewModel.updateCancel()
    }
}