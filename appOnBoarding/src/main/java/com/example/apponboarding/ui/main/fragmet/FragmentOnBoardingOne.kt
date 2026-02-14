package com.example.apponboarding.ui.main.fragmet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ads.Constants
import com.example.ads.Constants.loadBannerOnBoardOne
import com.example.ads.Constants.loadNativeFullOne
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeObFour
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.Constants.loadNativeObTwo
import com.example.ads.admobs.utils.OnBoardingAds
import com.example.ads.admobs.utils.loadAndShowOnBoardingAds
import com.example.ads.admobs.utils.loadOnBoardingBanner
import com.example.ads.admobs.utils.onPauseONBoardingBanner
import com.example.ads.dialogs.hide
import com.example.ads.dialogs.show
import com.example.ads.model.AdConfigModel
import com.example.ads.utils.fullNativeOne
import com.example.ads.utils.fullNativeTwo
import com.example.ads.utils.onBoardNativeFour
import com.example.ads.utils.onBoardNativeOne
import com.example.ads.utils.onBoardNativeThree
import com.example.ads.utils.onBoardNativeTwo
import com.example.apponboarding.databinding.FragmentOnBoardingOneBinding
import com.example.apponboarding.ui.main.activity.OnBoardingActivity
import com.project.common.utils.setOnSingleClickListener
import java.lang.ref.WeakReference


class FragmentOnBoardingOne : Fragment() {

    private var _binding: FragmentOnBoardingOneBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            if (it is OnBoardingActivity) {
                it.logEvent("event_fragment_onboarding_one")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingOneBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runCatching {
//            _binding?.onBoardImage?.playAnimation()
            if (Constants.loadBannerOnBoardMedium) {
                _binding?.bannerLayout?.root?.show()
                _binding?.bannerLayoutAdaptive?.root?.hide()
            } else {
                _binding?.bannerLayout?.root?.hide()
                _binding?.bannerLayoutAdaptive?.root?.show()
            }

            activity?.loadOnBoardingBanner(getObBannerPosition())
        }
    }

    private fun getObBannerPosition(): Int? {
        return if (Constants.loadBannerOnBoardTwo) {
            1
        } else if (Constants.loadBannerOnBoardThree) {
            2
        } else if (Constants.loadBannerOnBoardFour) {
            3
        } else {
            null
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mContext = context
            mActivity = context as AppCompatActivity
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onAttach: ", ex)
        }
    }

    override fun onResume() {
        super.onResume()
        kotlin.runCatching {
            _binding?.let { binding ->
                loadAndShowOnBoardingAds(
                    OnBoardingAds(
                        WeakReference(binding.parentLayout),
                        WeakReference(binding.nativeContainer),
                        WeakReference(binding.mediumNativeLayout.adContainer),
                        WeakReference(binding.mediumNativeLayout.shimmerViewContainer),
                        WeakReference(binding.bannerContainer),
                        WeakReference(if (Constants.loadBannerOnBoardMedium) binding.bannerLayout.adContainer else binding.bannerLayoutAdaptive.adContainer),
                        WeakReference(if (Constants.loadBannerOnBoardMedium) binding.bannerLayout.shimmerViewContainer else binding.bannerLayoutAdaptive.shimmerViewContainer),
                        WeakReference(binding.crossBannerIv),
                        position = 0,
                        isBanner = loadBannerOnBoardOne,
                        isMedium = Constants.loadBannerOnBoardMedium
                    ),
                    activity?.onBoardNativeOne(), getNextConfig()
                )
            }
        }
    }

    private fun getNextConfig(): AdConfigModel? {

        return if (loadNativeFullOne) {
            activity?.fullNativeOne()
        } else if (loadNativeObTwo) {
            activity?.onBoardNativeTwo()
        } else if (loadNativeFullTwo) {
            activity?.fullNativeTwo()
        } else if (loadNativeObThree) {
            activity?.onBoardNativeThree()
        } else if (loadNativeObFour) {
            activity?.onBoardNativeFour()
        } else {
            null
        }
    }

    private fun initViews() {
        binding.btnNextOnboarding1.setOnSingleClickListener {
            mActivity?.let {
                if (it is OnBoardingActivity) {
                    it.navigateToNextPage()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseONBoardingBanner(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}