package com.example.effect.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.inapp.helpers.Constants.isProVersion
import com.google.android.material.tabs.TabLayoutMediator
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.utils.ConstantsCommon
import com.project.common.viewmodels.ApiViewModel
import com.project.sticker.databinding.FragmentStickerBinding
import com.project.sticker.datastore.StickerDataStore
import com.example.effect.ui.adapters.ViewPagerAdapter
import com.example.effect.ui.intent.StickerIntent
import com.example.effect.ui.viewmodel.StickerViewModel
import com.example.effect.ui.viewstate.StickersViewState
import com.example.effect.utils.checkForSafety
import com.example.effect.utils.createOrShowSnackBar
import com.example.effect.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class Sticker : Fragment() {

    private var _binding: FragmentStickerBinding? = null

    private var callback: OnBackPressedCallback? = null

    @set:Inject
    lateinit var stickerDataStore: StickerDataStore

    private val binding get() = _binding!!

    private val stickerViewModel: StickerViewModel by activityViewModels()

    private val apiViewModel by activityViewModels<ApiViewModel>()

    private var mActivity: Activity? = null
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

            init()

            initClick()
        }

        observeData()

        onBackPress()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            apiViewModel.stickers.observeOnce(viewLifecycleOwner) {
                when (it) {
                    is Response.Success -> {
                        ConstantsCommon.stickersList = it.data
                        lifecycleScope.launch(IO) {
                            stickerViewModel.stickerIntent.send(StickerIntent.GetStickers(context))
                        }
                    }

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

                    else -> {

                    }
                }
            }
        }catch (_: Exception){Log.d("FAHAD", "initObservers: crash")}
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
                    mActivity?.showRewardedInterstitial(
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

                is StickersViewState.Success -> {
                    Log.i("observeData", "observeData: ${it.list.size}")
                    binding.shimmerView.stopShimmer()
                    binding.shimmerView.isVisible = false
                    binding.viewPager.adapter = null
                    it.list.let {
                        binding.viewPager.adapter =
                            ViewPagerAdapter(this, it)
                        binding.viewPager.offscreenPageLimit = 2
                        binding.viewPager.currentItem = stickerViewModel.lastTab
                        if (stickerViewModel.stickersCategoriesAndData.checkForSafety(stickerViewModel.lastTab)) {
                            binding.proLayout.isVisible =
                                stickerViewModel.stickersCategoriesAndData[stickerViewModel.lastTab].tag == "pro" && !isProVersion()
                        }
                        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                            if (it.checkForSafety(position)) {
                                tab.text = it[position].catName
                            }
                        }.attach()
                    }
                    if (it.list.isNotEmpty()) {
                        binding.proLayout.isVisible = it.list.first().tag != "free"
                    }
                    stickerViewModel.resetStickerViewState()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
//        System.gc()
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

            requireActivity().onBackPressedDispatcher.addCallback(this.viewLifecycleOwner, it)
        }
    }

    private fun backPress() {
        stickerViewModel.updateCancel()
    }
}