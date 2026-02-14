package com.abdul.pencil_sketch.main.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentPencilSketchRequestBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.intents.SketchIntent
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.main.viewstate.SketchImageActionViewState
import com.abdul.pencil_sketch.utils.navigateFragment
import com.bumptech.glide.Glide
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadRewarded
import com.example.ads.admobs.utils.showRewardedInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.nativeProcessingConfigEnhancer
import com.example.inapp.helpers.Constants.isProVersion
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getColorWithSafetyCheck
import com.project.common.utils.getProScreen
import com.project.common.utils.getSketchProgressMessages
import com.project.common.utils.setOnSingleClickListener
import com.project.gallery.utils.createOrShowSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PencilSketchRequest : Fragment() {

    private var _binding: FragmentPencilSketchRequestBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressMessagesList: List<String>


    private val restoreImageViewModel: PencilSketchViewModel by activityViewModels()
    private var callback: OnBackPressedCallback? = null
    private var rewardGranted = false


    fun hideOrShowAd(showAd: Boolean) {
        _binding?.let {
            if (showAd) {
                it.nativeContainer.isVisible = true
            } else {
                it.nativeContainer.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) {
            _binding = FragmentPencilSketchRequestBinding.inflate(inflater, container, false)
            binding.initViews()
            observeData()
            context?.let {
                progressMessagesList = it.getSketchProgressMessages()
            }
        }
        onBackPress()
        return binding.root
    }

    private fun observeData() {

        runCatching {
            isProVersion.observe(viewLifecycleOwner) {
                if (it) {
                    binding.proBtn.isVisible = false
                    binding.enhanceBtn.isVisible = true
                    binding.swapBtnSubTxt.isVisible = false
                    binding.nativeContainer.visibility = View.INVISIBLE
                } else {
                    binding.proBtn.isVisible = true
                    binding.swapBtnSubTxt.isVisible = true
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                restoreImageViewModel.state.buffer(1000, BufferOverflow.DROP_OLDEST).collect { it ->

                    when (it) {
                        is SketchImageActionViewState.Loading -> {
                            withContext(Main) {
                                binding.shimmerView.isVisible = true
                                binding.shimmerView.startShimmer()
                            }
                        }

                        is SketchImageActionViewState.Error -> {
                            Log.d("RESTORE_IMG_REQUEST", "observeData:-- Error")
                            withContext(Main) {
                                binding.shimmerView.isVisible = false
                                binding.shimmerView.stopShimmer()
                                if (_binding != null) {
                                    binding.seekBar.progress = 0
                                    binding.animBackView.visibility = View.GONE
                                    binding.loadingAnim.visibility = View.GONE
                                    binding.bottomLayout.visibility = View.VISIBLE
                                    binding.backImg.visibility = View.VISIBLE
                                    binding.cropBtn.isVisible = true
                                    binding.enhanceProgressLayout.visibility = View.INVISIBLE
                                    binding.progressText.text = "0%"
                                    binding.seekBar.progress = 0
                                    binding.animBackView.isVisible = false
                                    binding.loadingAnim.isVisible = false
                                    binding.loadingAnim.pauseAnimation()
                                    context?.createOrShowSnackBar(
                                        binding.root,
                                        0,
                                        it.message,
                                        true
                                    )
                                }
                                restoreImageViewModel.resetFrameState()
                            }
                        }

                        is SketchImageActionViewState.Success -> {
                            Log.d("RESTORE_IMG_REQUEST", "observeData:-- Success")

                        }

                        is SketchImageActionViewState.ImageEnhanceRequestComplete -> {
                            lifecycleScope.launch(Main) {
                                if (_binding != null) {
                                    binding.seekBar.progress = 100
                                    binding.animBackView.visibility = View.GONE
                                    binding.loadingAnim.visibility = View.GONE
                                    binding.bottomLayout.visibility = View.VISIBLE
                                    binding.cropBtn.isVisible = true
                                    binding.backImg.visibility = View.VISIBLE
                                    binding.enhanceProgressLayout.visibility = View.INVISIBLE
                                    binding.animBackView.isVisible = false
                                    binding.loadingAnim.isVisible = false
                                    binding.loadingAnim.pauseAnimation()
                                }

                                activity?.let { mActivity ->

                                    if (mActivity is PencilSketchActivity) {
                                        restoreImageViewModel.resetFrameState()
                                        activity?.navigateFragment(
                                            PencilSketchRequestDirections.actionPencilSketchRequestToPencilSketchResult(),
                                            R.id.pencilSketchRequest
                                        )
                                    }
                                }
                            }
                        }

                        is SketchImageActionViewState.ShowLoadingState -> {
                            withContext(Main) {
                                if (_binding != null) {
                                    binding.bottomLayout.visibility = View.INVISIBLE
                                    binding.cropBtn.isVisible = false
                                    binding.backImg.isVisible = false
                                    binding.enhanceProgressLayout.isVisible = true
                                    binding.animBackView.isVisible = true
                                    binding.loadingAnim.isVisible = true
                                    binding.loadingAnim.playAnimation()
                                    eventForGalleryAndEditor("sketch_request", "")
                                }
                            }
                        }


                        is SketchImageActionViewState.UpdateImagePathsWithEnhancement -> {
                            withContext(Main) {
                                _binding?.apply {
                                    Glide.with(fgImage.context)
                                        .load(if (restoreImageViewModel.imageEnhancedPath.isNotEmpty()) restoreImageViewModel.imageEnhancedPath[0].croppedPath else return@withContext)
                                        .into(fgImage)
                                    shimmerView.stopShimmer()
                                    shimmerView.isVisible = false
                                }
                            }
                        }

                        is SketchImageActionViewState.UpdateProgress -> {
                            withContext(Main) {
                                _binding?.let { binding ->
                                    context?.let { cntx ->
                                        if (binding.enhanceProgressLayout.isVisible) {
                                            if (it.progress <= 100) {
                                                binding.seekBar.progress = it.progress
                                                binding.progressText.text = "${it.progress}%"
                                                val isTarget =
                                                    it.progress != 0 && it.progress % 20 == 0
                                                if (isTarget) {
//                                                        val index = 5 - (100 / it.progress)
                                                    if (::progressMessagesList.isInitialized && progressMessagesList.isNotEmpty()) {
                                                        binding.progressMessage.text =
                                                            progressMessagesList.random()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            Log.d("RESTORE_IMG_REQUEST", "observeData:-- SetDimensionRatio")
                        }
                    }
                }
            }
        }


    }


    private fun FragmentPencilSketchRequestBinding.initViews() {

        context?.let {
            if (restoreImageViewModel.imageEnhancedPath.isNotEmpty()) {
                Glide.with(it).load(restoreImageViewModel.imageEnhancedPath[0].croppedPath)
                    .into(fgImage)
            }
        }

        proBtn.setOnSingleClickListener {
            runCatching {
                eventForGalleryAndEditor(
                    "sketch_request", "pro_btn"
                )
                activity?.let {
                    val intent = Intent()
                    intent.setClassName(
                        it.applicationContext, getProScreen()
                    )
                    it.startActivity(intent)
                }
            }
        }

        cropBtn.setOnSingleClickListener {
            activity?.let {
                eventForGalleryAndEditor(
                    "sketch_request", "crop"
                )
                if (restoreImageViewModel.imageEnhancedPath.isNotEmpty()) {
                    val path = restoreImageViewModel.imageEnhancedPath[0].croppedPath
                    it.navigateFragment(
                        PencilSketchRequestDirections.actionPencilSketchRequestToCrop().actionId,
                        R.id.pencilSketchRequest,
                        bundle = bundleOf(
                            "imagePath" to path,
                            "flipHorizontal" to false,
                            "flipVertical" to false,
                            "openForEnhancer" to true
                        ),
                    )
                }
            }
        }

        enhanceBtn.setOnSingleClickListener {
            eventForGalleryAndEditor(
                "sketch_request", "restore_btn"
            )
            binding.progressText.text = "0%"
            if (!isProVersion() && !rewardGranted) {
                activity?.showRewardedInterstitial(true, loadedAction = {
                    activity?.loadRewarded(loadedAction = {}, failedAction = {})
                    rewardGranted = true
                    eventForGalleryAndEditor(
                        "sketch_request",
                        "next"
                    )
                    lifecycleScope.launch(IO) {
                        activity?.let {
                            restoreImageViewModel.restoreIntent?.send(
                                SketchIntent.GenerateToken(
                                    it,
                                )
                            )
                        }
                    }

                }, failedAction = {
                    eventForGalleryAndEditor(
                        "sketch_request",
                        "failed"
                    )
                })
            } else {
                eventForGalleryAndEditor(
                    "sketch_request", "next"
                )
                lifecycleScope.launch(IO) {
                    activity?.let {
                        restoreImageViewModel.restoreIntent?.send(
                            SketchIntent.GenerateToken(
                                it
                            )
                        )
                    }
                }
            }
        }

        backImg.setOnSingleClickListener {
            backPress()
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCropResultListener()
        _binding?.enhanceProgressLayout?.visibility = View.INVISIBLE
        if (_binding?.enhanceProgressLayout?.visibility == View.INVISIBLE) {// && !isProVersion()
            //  (activity as? AIEnhancerActivity)?.hideBannerForNow()
        }
        _binding?.let { binding ->
            runCatching {
                activity?.let { mActivity ->
                    mActivity.loadAndShowNativeOnBoarding(
                        loadedAction = {
                            _binding?.let { binding ->
                                binding.nativeContainer.show()
                                binding.smallNativeLayout.adContainer.show()
                                binding.smallNativeLayout.shimmerViewContainer.hide()
                                binding.smallNativeLayout.adContainer.removeAllViews()
                                if (it?.parent != null) {
                                    (it.parent as ViewGroup).removeView(it)
                                }
                                binding.smallNativeLayout.adContainer.addView(it)
                            }
                        }, failedAction = {
                            _binding?.let { binding ->
                                binding.nativeContainer.visibility = View.INVISIBLE
                            }
                        }, config = mActivity.nativeProcessingConfigEnhancer(),
                        mActivity.nativeProcessingConfigEnhancer()
                    )
                }
            }
        }
    }


    private fun setCropResultListener() {
        setFragmentResultListener("fromCrop") { _, bundle ->
            val configChange = bundle.getBoolean("configChange", false)
            val shouldUpdate = bundle.getBoolean("replace", false)
            val refresh = bundle.getBoolean("refresh", false)
            val path = bundle.getString("croppedImagePath", "")

            if (shouldUpdate) {
                observeData()
                lifecycleScope.launch(IO) {
                    restoreImageViewModel.restoreIntent?.send(
                        SketchIntent.AddCroppedImage(
                            0, path
                        )
                    )
                }
            } else if (refresh) {
                observeData()
            } else if (configChange) {
                observeData()
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runCatching {
            if (_binding != null) {
                binding.let {
                    context?.let { it1 ->
                        it.root.setBackgroundColor(
                            it1.getColorWithSafetyCheck(com.project.common.R.color.container_clr_activity)
                        )

                        it.backImg.setColorFilter(
                            it1.getColorWithSafetyCheck(
                                com.project.common.R.color.btn_icon_clr
                            )
                        )
                        it.toolbarView.setBackgroundColor(
                            it1.getColorWithSafetyCheck(
                                com.project.common.R.color.container_clr_activity
                            )
                        )
                        it.headingTxt.setTextColor(
                            it1.getColorWithSafetyCheck(
                                com.project.common.R.color.btn_txt_clr
                            )
                        )
                    }
                }
            }
        }
    }


    private fun onBackPress() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPress()
            }
        }
        callback?.let {
            activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner, it)
        }

    }


    private fun backPress() {
        try {
            activity?.let {
//                if (faceSwapViewModel.fromPhotoEditor) {
//                    if (it is ColorizeActivity) {
//                        kotlin.runCatching {
//                            it.setResult(Activity.RESULT_CANCELED)
//                            it.finish()
//                        }
//                    } else {
//                    }
//                } else {
                eventForGalleryAndEditor("sketch_request", "back")
                restoreImageViewModel.resetFrameState()
                setFragmentResult("requestKeyGallery", bundleOf("refresh" to true))
                findNavController().popBackStack()

                //}
            }
        } catch (ex: Exception) {
            Log.e("error", "backPress: ", ex)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback?.remove()
    }


}