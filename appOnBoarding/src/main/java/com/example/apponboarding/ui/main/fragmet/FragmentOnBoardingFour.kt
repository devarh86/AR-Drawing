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
import com.example.ads.Constants.homeNewInterStrategy
import com.example.ads.Constants.loadBannerOnBoardFour
import com.example.ads.Constants.loadBannerOnBoardThree
import com.example.ads.admobs.utils.OnBoardingAds
import com.example.ads.admobs.utils.loadAndShowOnBoardingAds
import com.example.ads.admobs.utils.loadNewInterstitialWithoutStrategyCheck
import com.example.ads.admobs.utils.onPauseONBoardingBanner
import com.example.ads.dialogs.hide
import com.example.ads.dialogs.show
import com.example.ads.utils.homeInterstitial
import com.example.ads.utils.obLastInterstitial
import com.example.ads.utils.onBoardNativeFour
import com.example.ads.utils.onBoardNativeThree
import com.example.apponboarding.databinding.FragmentOnBoardingFourBinding
import com.example.apponboarding.databinding.FragmentOnBoardingThreeBinding
import com.example.apponboarding.ui.main.activity.OnBoardingActivity
import com.project.common.utils.setOnSingleClickListener
import java.lang.ref.WeakReference


class FragmentOnBoardingFour : Fragment() {
    private var _binding: FragmentOnBoardingFourBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            if (it is OnBoardingActivity) {
                it.logEvent("event_fragment_onboarding_four")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runCatching {
            _binding?.onBoardImage?.playAnimation()

            if (Constants.loadBannerOnBoardMedium) {
                _binding?.bannerLayout?.root?.show()
                _binding?.bannerLayoutAdaptive?.root?.hide()
            } else {
                _binding?.bannerLayout?.root?.hide()
                _binding?.bannerLayoutAdaptive?.root?.show()
            }
        }


            activity?.loadNewInterstitialWithoutStrategyCheck(activity?.homeInterstitial()) {}

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
                        position = 3,
                        isBanner = loadBannerOnBoardFour,
                        isMedium = Constants.loadBannerOnBoardMedium
                    ),
                    activity?.onBoardNativeFour(), null
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnBoardingFourBinding.inflate(inflater, container, false)
        _binding?.initViews()
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        onPauseONBoardingBanner(3)
    }

    private fun FragmentOnBoardingFourBinding.initViews() {
        btnNextOnboarding.setOnSingleClickListener {
            mActivity?.let {
                if (it is OnBoardingActivity) {
                    it.navigateToNextPage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}