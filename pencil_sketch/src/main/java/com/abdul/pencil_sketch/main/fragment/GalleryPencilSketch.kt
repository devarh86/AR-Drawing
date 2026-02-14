package com.abdul.pencil_sketch.main.fragment

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentGalleryPencilSketchBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.intents.SketchIntent
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.main.viewstate.SketchImageActionViewState
import com.abdul.pencil_sketch.utils.navigateFragment
import com.example.ads.Constants
import com.example.ads.Constants.flowSelectPhotoScr
import com.example.ads.Constants.interstitialStrategyOld
import com.example.ads.Constants.native
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.interstitialBack
import com.example.ads.utils.nativeProcessingConfig
import com.example.analytics.Constants.firebaseAnalytics
import com.example.inapp.helpers.Constants.isProVersion
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.databinding.BottomSheetProcessDialogBinding
import com.project.common.model.ImagesModel
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getColorWithSafetyCheck
import com.project.gallery.ui.main.GalleryListDialogFragment
import com.project.gallery.ui.main.GalleryListDialogFragment.Companion.selectedFolderPos
import com.project.gallery.ui.main.viewmodel.GalleryViewModel
import com.project.gallery.utils.createOrShowSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GalleryPencilSketch : Fragment(), GalleryListDialogFragment.OnImageSelection {

    private var _binding: FragmentGalleryPencilSketchBinding? = null
    private val binding get() = _binding!!

    private var bottomSheetProcessDialog: BottomSheetDialog? = null
    private var bottomSheetProcessDialogBinding: BottomSheetProcessDialogBinding? = null

    private val galleryViewModel: GalleryViewModel by activityViewModels()
    private val restoreImageViewModel: PencilSketchViewModel by activityViewModels()

    private var callback: OnBackPressedCallback? = null
    private var currentFragment: GalleryListDialogFragment? = null
    private val args: GalleryPencilSketchArgs by navArgs()
    private var activity: Activity? = null
    private var tempPathList: MutableList<ImagesModel> = mutableListOf()
    private var clickable = true
    private var gallerySaveTracker = true


    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activity = (context as Activity)
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onAttach: ", ex)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedFolderPos = 0
        galleryViewModel.initializing()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runCatching {
            if (_binding != null) {
                context?.let {
                    binding.galColorizeRoot.setBackgroundColor(it.getColorWithSafetyCheck(com.project.common.R.color.surface_clr))
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) {
            _binding = FragmentGalleryPencilSketchBinding.inflate(inflater, container, false)
            observeData()
            init()
            eventForGalleryAndEditor("gallery_sketch_img", "")
            childFragmentManager.navigate("gallery")
        }
        onBackPress()
        setGalleryResultListener()
        return binding.root
    }

    private fun setGalleryResultListener() {
        setFragmentResultListener("requestKeyGallery") { _, bundle ->
            val refresh = bundle.getBoolean("refresh", false)
            if (refresh) {
                if (flowSelectPhotoScr != "new")
                    restoreImageViewModel.imageEnhancedPath.clear()
                observeData()
            }

        }
    }

    private fun FragmentManager.navigate(
        name: String,
    ) {
        try {

            val bundle = bundleOf(
                "showDivider" to (flowSelectPhotoScr == "new"),
                "fromReplace" to args.replace,
                "new_flow" to (flowSelectPhotoScr == "new")
            )

            currentFragment = GalleryListDialogFragment()

            currentFragment?.arguments = bundle

            val newFragment = this.findFragmentByTag(name)?.let {
                val trans = this.beginTransaction().replace(binding.fragmentContainer.id, it)
                trans.commitNowAllowingStateLoss()
                it
            } ?: let {
                currentFragment?.let {
                    val trans = childFragmentManager.beginTransaction()
                        .add(binding.fragmentContainer.id, it)
                    trans.commit()
                    it.setListener(this@GalleryPencilSketch)
                }
            }

        } catch (ex: java.lang.Exception) {
            Log.e("error", "navigate: ", ex)
        }
    }

    private fun observeData() {

        restoreImageViewModel.state.asLiveData().observe(viewLifecycleOwner) {
            when (it) {
                SketchImageActionViewState.Idle -> {
                    Log.d("COLORIZE", " Gallery---observeData:-- Idle")

                }

                SketchImageActionViewState.Loading -> {
                    lifecycleScope.launch(Main) {
                        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == false && isVisible && !isDetached) {
                            bottomSheetProcessDialog?.show()
                        }
                    }
                }

                is SketchImageActionViewState.Error -> {
                    lifecycleScope.launch(Main) {
                        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached)
                            bottomSheetProcessDialog?.dismiss()
                        context?.createOrShowSnackBar(binding.root, 0, it.message, true)
                        clickable = true
                        if (flowSelectPhotoScr != "new")
                            restoreImageViewModel.imageEnhancedPath.clear()
                        restoreImageViewModel.resetFrameState()
                    }
                }

                SketchImageActionViewState.Success -> {
                    Log.d("COLORIZE", " Gallery---observeData:-- Success")
                    lifecycleScope.launch(Main) {
                        restoreImageViewModel.resetFrameState()
                    }

                }

                is SketchImageActionViewState.UpdateImagePathsWithEnhancement -> {
                    if (flowSelectPhotoScr != "new") {
                        lifecycleScope.launch(IO) {
                            context?.let {
                                restoreImageViewModel.restoreIntent?.send(
                                    SketchIntent.SaveImages(it)
                                )
                            }
                        }
                    } else {
                        clickable = true
                        activity?.let {
                            if (it is PencilSketchActivity) {
                                it.loadAndShowNativeAd(true)
                            }
                        }
                        galleryViewModel.updateTickIcon(true)
                    }
                }

                SketchImageActionViewState.SaveLoading -> {
                    lifecycleScope.launch(Main) {
                        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == false && isVisible && !isDetached)
                            bottomSheetProcessDialog?.show()

                        bottomSheetProcessDialogBinding?.let { binding ->
                            native.nativeAd = null
                            if (!isProVersion())
                                binding.nativeContainer.show()
                            runCatching {
                                activity?.let { mActivity ->
                                    mActivity.loadAndShowNativeOnBoarding(
                                        loadedAction = {
                                            binding.nativeContainer.show()
                                            binding.mediumNativeLayout.adContainer.show()
                                            binding.mediumNativeLayout.shimmerViewContainer.hide()
                                            binding.mediumNativeLayout.adContainer.removeAllViews()
                                            if (it?.parent != null) {
                                                (it.parent as ViewGroup).removeView(it)
                                            }
                                            binding.mediumNativeLayout.adContainer.addView(it)

                                        }, failedAction = {
                                            binding.nativeContainer.hide()
                                        },
                                        config = mActivity.nativeProcessingConfig()
                                    )
                                }
                            }
                        }
                    }

                }

                is SketchImageActionViewState.SaveComplete -> {
                    Log.d("COLORIZE", " Gallery---observeData:-- SaveComplete")
                    lifecycleScope.launch(Main) {
                        gallerySaveTracker = false
                        gallerySaveTracker = true
                        restoreImageViewModel.resetFrameState()
                        lifecycleScope.launch(Main) {
                            eventForGalleryAndEditor("gallery_sketch", "next")
                            delay(2000)
                            if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached)
                                bottomSheetProcessDialog?.dismiss()

                            if (args.replace) {
                                //  navigateAfterReplace()
                            } else {
                                clickable = true
                                activity?.showInterstitial(
                                    loadedAction = {
                                        activity?.navigateFragment(
                                            GalleryPencilSketchDirections.actionGalleryPencilSketchToPencilSketchRequest(),
                                            R.id.galleryPencilSketch
                                        )
                                    },
                                    failedAction = {

                                        activity?.navigateFragment(
                                            GalleryPencilSketchDirections.actionGalleryPencilSketchToPencilSketchRequest(),
                                            R.id.galleryPencilSketch
                                        )
                                    }, showAd = true, onCheck = true
                                )
                            }
                        }
                    }
                }

                is SketchImageActionViewState.ImageEnhanceRequestComplete -> {}
                is SketchImageActionViewState.SetUserImage -> {}
                SketchImageActionViewState.ShowLoadingState -> {}
                is SketchImageActionViewState.UpdateImage -> {}
                is SketchImageActionViewState.UpdateProgress -> {}
            }
        }
    }

    private fun init() {
        bottomSheetProcessDialog =
            context?.let { BottomSheetDialog(it, com.project.common.R.style.BottomSheetDialogNew) }
        bottomSheetProcessDialog?.let {
            bottomSheetProcessDialogBinding =
                BottomSheetProcessDialogBinding.inflate(layoutInflater)
            bottomSheetProcessDialogBinding?.root?.let { it1 -> it.setContentView(it1) }
        }

        bottomSheetProcessDialogBinding?.textView7?.text =
            context?.getText(com.project.common.R.string.processing_please_wait)

        bottomSheetProcessDialogBinding?.imageView?.isVisible = false

        bottomSheetProcessDialogBinding?.progressBar?.visibility = View.INVISIBLE

        bottomSheetProcessDialog?.setCancelable(false)

        bottomSheetProcessDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (args.replace) {
            tempPathList.addAll(restoreImageViewModel.imageEnhancedPath)
        }
    }

    private fun onBackPress() {

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPress()
            }

        }
        callback?.let {
            requireActivity().onBackPressedDispatcher?.addCallback(viewLifecycleOwner, it)
        }

    }

    private fun backPress() {
        try {
            if (!args.replace) {
                eventForGalleryAndEditor("gallery_sketch", "back")
            }

            if (gallerySaveTracker) {
                if (!interstitialStrategyOld) {
                    runCatching {
                        activity?.showNewInterstitial(activity?.interstitialBack()) {
                            activity?.let {
                                if (it is PencilSketchActivity) {
                                    if (!it.isFinishing && !it.isDestroyed) {
                                        it.finish()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    activity?.showInterstitial(loadedAction = {
                        runCatching {
                            activity?.let {
                                if (it is PencilSketchActivity) {
                                    if (!it.isFinishing && !it.isDestroyed) {
                                        it.finish()
                                    }
                                }
                            }
                        }
                    }, failedAction = {
                        runCatching {
                            activity?.let {
                                if (it is PencilSketchActivity) {
                                    if (!it.isFinishing && !it.isDestroyed) {
                                        it.finish()
                                    }
                                }
                            }
                        }
                    }, showAd = Constants.showImageSelectionBackAd, onCheck = true)
                }
            }

        } catch (ex: Exception) {
            Log.e("error", "backPress: ", ex)
        }
    }


    override fun onSelection(path: String) {
        if (gallerySaveTracker) {
            if (clickable) {
                lifecycleScope.launch(IO) {
                    try {
                        clickable = false
                        restoreImageViewModel.imageEnhancedPath.clear()
                        restoreImageViewModel.imageEnhancedPath.add(ImagesModel())
                        restoreImageViewModel.restoreIntent?.send(
                            SketchIntent.SingleImageEnhancementAndPlacing(
                                path,
                                restoreImageViewModel.imageEnhancedPath.size - 1
                            )
                        )


                    } catch (ex: java.lang.Exception) {
                        Log.e("error", "onSelection: ", ex)
                    }
                }
            }
        }

    }

    override fun setRecyclerViewListener(view: RecyclerView) {}

    override fun onNextClick() {
        if (flowSelectPhotoScr == "new") {
            lifecycleScope.launch(IO) {
                context?.let {
                    restoreImageViewModel.restoreIntent?.send(SketchIntent.SaveImages(it))
                }
            }
            firebaseAnalytics?.logEvent("select_photo_click_next", null)
        }
    }

    override fun onBackClick() {
        backPress()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        callback?.remove()
    }

    override fun onDestroy() {
        super.onDestroy()

        callback?.remove()
    }


}