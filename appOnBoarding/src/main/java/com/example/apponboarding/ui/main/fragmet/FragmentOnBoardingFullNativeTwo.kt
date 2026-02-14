package com.example.apponboarding.ui.main.fragmet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ads.Constants.autoScrollFullNative
import com.example.ads.Constants.loadNativeFullTwo
import com.example.ads.Constants.loadNativeObFour
import com.example.ads.Constants.loadNativeObThree
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.onPauseONBoardingBanner
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.model.AdConfigModel
import com.example.ads.utils.fullNativeTwo
import com.example.ads.utils.onBoardNativeFour
import com.example.ads.utils.onBoardNativeThree
import com.example.apponboarding.databinding.FragmentOnBoardingFullNativeOneBinding
import com.example.apponboarding.ui.main.activity.OnBoardingActivity
import com.example.inapp.helpers.Constants.isProVersion
import com.project.common.utils.setOnSingleClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentOnBoardingFullNativeTwo : Fragment() {

    private var swipeJob: Job? = null
    private var _binding: FragmentOnBoardingFullNativeOneBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            if (it is OnBoardingActivity) {
                it.logEvent("event_full_native_two")
            }
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

            if (isProVersion()) {
                _binding?.nativeContainer?.visibility = View.INVISIBLE
            } else {

                activity?.let { myActivity ->

                    _binding?.nativeContainer?.visibility = View.VISIBLE

                    _binding?.let { binding ->

                        myActivity.loadAndShowNativeOnBoarding(
                            loadedAction = {
                                if (isVisible && !isDetached && _binding != null) {
                                    binding.nativeContainer.show()
                                    binding.mediumNativeLayout.adContainer.show()
                                    binding.mediumNativeLayout.shimmerViewContainer.hide()
                                    binding.mediumNativeLayout.adContainer.removeAllViews()
                                    if (it?.parent != null) {
                                        (it.parent as ViewGroup).removeView(it)
                                    }
                                    if (isVisible && !isDetached && _binding != null) {
                                        binding.mediumNativeLayout.adContainer.addView(it)
                                    }
                                    it?.findViewById<View>(com.example.ads.R.id.fo_iv_close_nfs_meta)
                                        ?.let {
                                            if (!it.hasOnClickListeners()) {
                                                it.setOnSingleClickListener {
                                                    if (isVisible && !isDetached && _binding != null) {
                                                        mActivity?.let {
                                                            if (it is OnBoardingActivity) {
                                                                it.navigateToNextPage(true)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    if (swipeJob == null)
                                        initSwipe()
                                }
                            },
                            failedAction = {
                                if (isVisible && !isDetached && _binding != null) {
                                    mActivity?.let {
                                        if (it is OnBoardingActivity && it.getPagerState()) {
                                            it.navigateToNextPage(true)
                                        }
                                    }
                                }
                            },
                            myActivity.fullNativeTwo(), getNextConfig()
                        )
                    }
                }
            }
        }
    }

    private fun getNextConfig(): AdConfigModel? {

        return if (loadNativeObThree) {
            activity?.onBoardNativeThree()
        } else if (loadNativeObFour) {
            activity?.onBoardNativeFour()
        } else {
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingFullNativeOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initSwipe() {

        if (autoScrollFullNative > 0) {
            swipeJob = CoroutineScope(Dispatchers.Default).launch {
                delay(autoScrollFullNative)
                withContext(Main) {
                    Log.i("TAG", "initSwipe: $swipeJob")
                    if (isVisible && !isDetached && _binding != null && isActive && swipeJob != null) {
                        mActivity?.let {
                            if (it is OnBoardingActivity) {
                                it.navigateToNextPage(true)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        swipeJob?.cancel()
        swipeJob = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}