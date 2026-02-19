package com.abdul.pencil_sketch.main.fragment

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentPencilSketchResultBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.intents.SaveIntentSketch
import com.abdul.pencil_sketch.main.intents.SketchIntent
import com.abdul.pencil_sketch.main.model.BeforeAfterModel
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.main.viewstate.SketchImageActionViewState
import com.abdul.pencil_sketch.main.viewstate.SketchSaveViewState
import com.abdul.pencil_sketch.utils.loadBitmap
import com.abdul.pencil_sketch.utils.navigateFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.ads.Constants.interstitialStrategyOld
import com.example.ads.Constants.rewardedShown
import com.example.ads.Constants.showDiscardClickAd
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadNewInterstitialWithoutStrategyCheck
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.interstitialBack
import com.example.ads.utils.nativeDialogsConfig
import com.example.ads.utils.saveInterstitial
import com.example.inapp.helpers.Constants.isProVersion
import com.example.inapp.repo.datastore.BillingDataStore
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.databinding.BottomSheetDiscardPhotoEditorBinding
import com.project.common.databinding.BottomSheetProcessDialogBinding
import com.project.common.enum_classes.EditorBottomTypes
import com.project.common.enum_classes.SaveQuality
import com.project.common.model.ImagesModel
import com.project.common.model.SavingModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.ConstantsCommon.isOpenCVSuccess
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.gettingQuality
import com.project.common.utils.setOnSingleClickListener
import com.project.common.utils.setString
import com.project.gallery.utils.createOrShowSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class PencilSketchResult : Fragment() {

    private var _binding: FragmentPencilSketchResultBinding? = null
    private val binding get() = _binding!!

    private val restoreImageViewModel: PencilSketchViewModel by activityViewModels()
    private var motionListener: MotionLayout.TransitionListener? = null
    private var beforeAfterModel = BeforeAfterModel()
    private var bottomSheetProcessDialog: BottomSheetDialog? = null
    private var bottomSheetProcessDialogBinding: BottomSheetProcessDialogBinding? = null

    private var callback: OnBackPressedCallback? = null
    private var bottomSheetDiscardDialog: BottomSheetDialog? = null
    private var bottomSheetDiscardDialogBinding: BottomSheetDiscardPhotoEditorBinding? = null
    private var currentFeature: EditorBottomTypes = EditorBottomTypes.NONE

    private var isSaving = false
    private var finalPath = ""
    private var alreadyAdShown = false
    private var fromSaved: Boolean = false
    private var gallerySaveTracker = true
    private var clickable = true
    private var isAnimating: Boolean = false
    private var animationProgress: Float = 0f
    private var currentState = -1
    private var touchEnable = false


    @set:Inject
    lateinit var billingDataStore: BillingDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSaveLauncher()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (_binding == null) {
            _binding = FragmentPencilSketchResultBinding.inflate(inflater, container, false)
            observeData()
            restoreImageViewModel.resultImgPath?.let { path ->
                if (restoreImageViewModel.imageEnhancedPath.isNotEmpty()) {
                    beforeAfterModel.before = restoreImageViewModel.imageEnhancedPath[0].croppedPath
                    beforeAfterModel.after = path
                }

            }
            eventForGalleryAndEditor("sketch_result", "")
            activity?.loadNewInterstitialWithoutStrategyCheck(activity?.saveInterstitial()) {}
            binding.initViews()

        }
        checkForWaterMark()
        observerSave()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBackPress()
    }


    private fun observeData() {

        restoreImageViewModel.state.asLiveData().observe(viewLifecycleOwner) {
            when (it) {

                is SketchImageActionViewState.Loading -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- Loading")
                    lifecycleScope.launch(Main) {
                        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == false && isVisible && !isDetached)
                            bottomSheetProcessDialog?.show()
                    }
                }

                is SketchImageActionViewState.Error -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- Error")
                    lifecycleScope.launch(Main) {
                        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached)
                            bottomSheetProcessDialog?.dismiss()
                        context?.createOrShowSnackBar(binding.root, 0, it.message, true)
                        clickable = true
                        restoreImageViewModel.imageEnhancedPath.clear()
                        restoreImageViewModel.resetFrameState()
                    }
                }

                is SketchImageActionViewState.Idle -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- Idle")
                }

                is SketchImageActionViewState.Success -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- Success")
                    lifecycleScope.launch(Main) {
                        restoreImageViewModel.resetFrameState()
                    }
                }

                is SketchImageActionViewState.UpdateImage -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- UpdateImage")
                }


                is SketchImageActionViewState.UpdateImagePathsWithEnhancement -> {
                    Log.d(
                        "RESTORE_IMG_RESULT",
                        " Gallery---observeData:-- UpdateImagePathsWithEnhancement"
                    )
                    lifecycleScope.launch(IO) {
                        context?.let {
                            kotlin.runCatching {
                                _binding?.let { binding ->
                                    restoreImageViewModel.restoreIntent?.send(
                                        SketchIntent.SaveImageForEditor(
                                            it,
                                            binding.afterImage.drawToBitmap(Bitmap.Config.ARGB_8888)
                                        )
                                    )
                                }

                            }

                        }
                    }
                }

                is SketchImageActionViewState.SaveLoading -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- SaveLoading")
                    lifecycleScope.launch(Main) {
                        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == false && isVisible && !isDetached)
                            bottomSheetProcessDialog?.show()
                    }
                }

                is SketchImageActionViewState.SaveComplete -> {
                    Log.d("RESTORE_IMG_RESULT", " Gallery---observeData:-- SaveComplete")
                    lifecycleScope.launch(Main) {
                        gallerySaveTracker = false
                        gallerySaveTracker = true
                        restoreImageViewModel.resetFrameState()
                        lifecycleScope.launch(Main) {
                            delay(2000)
                            if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached)
                                bottomSheetProcessDialog?.dismiss()
                            if (restoreImageViewModel.imageEnhancedPath.isNotEmpty())
                                ConstantsCommon.enhanceImageUrl =
                                    restoreImageViewModel.imageEnhancedPath[0].originalPath
                            eventForGalleryAndEditor("sketch_result", "editor_btn")
                            // openPhotoEditor()

                        }

                    }

                }


                else -> {
                    Log.d("RESTORE_IMG_RESULT", " ELSE---observeData:-- UpdateFrame")
                }
            }
        }

    }


    private fun FragmentPencilSketchResultBinding.initViews() {
        runCatching {
            successText.text = HtmlCompat.fromHtml(
                getString(com.project.common.R.string.save_colored_text),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        saveTxt.setOnSingleClickListener {
            if (!isSaving) {
                isSaving = true
                if (isOpenCVSuccess) {
                    eventForGalleryAndEditor("sketch_result", "save")
                    currentFeature = EditorBottomTypes.SAVE

                    lifecycleScope.launch(IO) {
                        runCatching {
                            billingDataStore.readSaveQuality().first().let {
                                restoreImageViewModel.currentQuality =
                                    when (it) {
                                        SaveQuality.HIGH.name -> {
                                            SaveQuality.HIGH
                                        }

                                        SaveQuality.MEDIUM.name -> {
                                            SaveQuality.MEDIUM
                                        }

                                        else -> {
                                            SaveQuality.LOW
                                        }
                                    }
                            }

                            context?.gettingQuality(
                                restoreImageViewModel.currentQuality,
                                restoreImageViewModel.originalWidth,
                                restoreImageViewModel.originalHeight
                            )?.let { qualityWithWaterMark ->
                                qualityWithWaterMark.pair.apply {
                                    restoreImageViewModel.savingWidth = first
                                    restoreImageViewModel.savingHeight = second
                                }
                                restoreImageViewModel.waterMarkAsset = qualityWithWaterMark.drawable
                            }


                            if (restoreImageViewModel.savingWidth != 0 || restoreImageViewModel.savingHeight != 0) {
                                withContext(Main) {
                                    saving()
                                }
                            } else {
                                withContext(Main) {
                                    isSaving = false
                                    activity?.createOrShowSnackBar(
                                        binding.root,
                                        0,
                                        "Saving Failed!",
                                        true
                                    )
                                }
                            }
                        }
                    }
                } else {
                    isSaving = false
                    activity?.createOrShowSnackBar(binding.root, 0, "Saving Failed!", true)
                }

            }


        }

        backImg.setOnSingleClickListener {
            backPress()
        }
        homeIV.setOnSingleClickListener {
            runCatching {
                val resultIntent = Intent()
                resultIntent.putExtra("where", "home")
                activity?.setResult(RESULT_OK, resultIntent)
                if (activity is PencilSketchActivity) {
                    activity?.finish()
                }
            }
        }

        draw.setOnSingleClickListener {
            activity?.let { mActivity ->
                if (mActivity is PencilSketchActivity) {
                    val drawingPath = beforeAfterModel.after.ifBlank {
                        restoreImageViewModel.resultImgPath ?: beforeAfterModel.before
                    }


                    if (drawingPath.isNotBlank()) {

                        restoreImageViewModel.imageEnhancedPath.add(ImagesModel())
                        restoreImageViewModel.imageEnhancedPath[0].croppedPath = drawingPath
                        restoreImageViewModel.imageEnhancedPath[0].originalPath = drawingPath

                        restoreImageViewModel.imageEnhancedPath.clear()
                        restoreImageViewModel.imageEnhancedPath.add(ImagesModel(originalPath = drawingPath, croppedPath = drawingPath))

                        mActivity.imgPath = drawingPath
                    }

                    mActivity.isOpenFromMain = false
                    mActivity.navigateFragment(
                        PencilSketchResultDirections.actionPencilSketchResultToHowToDrawFragment(),
                        R.id.pencilSketchResult
                    )
                }
            }
        }


        root.removeTransitionListener(motionListener)
        motionListener = object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
            ) {
                isAnimating = true
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float,
            ) {

                animationProgress = progress
                val dividerX = (divider.x - afterImage.x) + divider.width / 2
                afterImage.clipBounds =
                    Rect(dividerX.toInt(), 0, afterImage.width, afterImage.height)
                //  afterImage.invalidate()
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                isAnimating = false
                if (!touchEnable) {
                    if (currentId == R.id.start_before_after) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            currentState = R.id.end_before_after
                            _binding?.let {
                                it.root.transitionToState(currentState, 1500)
                            }

                        }, 1000)
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            currentState = R.id.start_before_after
                            _binding?.let {
                                binding.root.transitionToState(currentState, 1500)
                            }

                        }, 1000)
                    }
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float,
            ) {

            }

        }
        root.addTransitionListener(motionListener)
        handle.setOnTouchListener(null)
        handle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchEnable = true
                    // getParentBaseFragment()?.removeViewPagerSwipe(true)
                    pauseAnimation()
                    event.moveSlider()
                    afterImage.clipBounds = Rect(
                        0,
                        0,
                        ((event.rawX - afterImage.x) + (divider.width / 2)).roundToInt(),
                        afterImage.height
                    )
                }

                MotionEvent.ACTION_MOVE -> {
                    event.moveSlider()
                }

                MotionEvent.ACTION_UP -> {
                    event.moveSlider()
                    afterImage.clipBounds = Rect(
                        0,
                        0,
                        ((event.rawX - afterImage.x) + (divider.width / 2)).roundToInt(),
                        beforeImage.height
                    )
                    //  getParentBaseFragment()?.removeViewPagerSwipe(false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        resumeAnimation()
                    }, 1000)
                    touchEnable = false
                }
            }
            true
        }


    }

    private fun MotionEvent.moveSlider() {
        _binding?.apply {
            var progress = (((rawX - afterImage.x) * 100f / afterImage.width) / 100f)
            if (progress > 1)
                progress = 1f
            else if (progress < 0)
                progress = 0f
            if (motionLayout.startState == R.id.start_before_after) {
                _binding?.motionLayout?.progress = progress
            } else {
                val progress = (((rawX - beforeImage.x) * 100f / afterImage.width) / 100f)
                _binding?.motionLayout?.progress = 1f - progress
            }
        }
    }

    private fun saving() {
        initProcessDialog()
        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == false && isVisible && !isDetached) bottomSheetProcessDialog?.show()
        try {
            lifecycleScope.launch(IO) {
                context?.loadBitmap(beforeAfterModel.after) {
                    val list: MutableList<SavingModel> = mutableListOf()
                    list.add(
                        SavingModel(
                            userImageBitmap = it,
                        )
                    )
                    lifecycleScope.launch(IO) {
                        context?.let {
                            SaveIntentSketch.Saving(it, list)
                        }?.let {
                            restoreImageViewModel.saveIntent?.send(it)
                        }

                    }
                }

            }
        } catch (ex: Exception) {
            Log.e("error", "saving: ", ex)
        }
    }

    private fun initProcessDialog() {
        bottomSheetProcessDialog = context?.let {
            BottomSheetDialog(it, com.project.common.R.style.BottomSheetDialogNew)
        }
        bottomSheetProcessDialog?.let {
            bottomSheetProcessDialogBinding =
                BottomSheetProcessDialogBinding.inflate(layoutInflater)
            bottomSheetProcessDialogBinding?.root?.let { it1 -> it.setContentView(it1) }
        }
        bottomSheetProcessDialogBinding?.textView7?.text =
            context?.getText(com.project.common.R.string.processing_please_wait)
        bottomSheetProcessDialog?.setCancelable(false)
        bottomSheetProcessDialogBinding?.imageView?.isVisible = false
        bottomSheetProcessDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        bottomSheetProcessDialogBinding?.let { binding ->
            binding.nativeContainer.visibility = View.GONE
        }
    }


    private fun pauseAnimation() {
        _binding?.root?.let {
            animationProgress = it.progress
            isAnimating = it.progress > 0f && it.progress < 1f
            it.progress = animationProgress
            it.transitionToState(it.currentState)
        }
    }


    override fun onResume() {
        super.onResume()
        Log.i("TAG", "onResume: fragmentPosition ${beforeAfterModel.position}")
        _binding?.apply {
            motionLayout.postOnAnimation {
                applyImage(beforeAfterModel.after, afterImage, true) { successBefore ->
                    if (successBefore) {
                        applyImage(beforeAfterModel.before, beforeImage, false) { successAfter ->
                            if (successAfter) {
                                _binding?.apply {
                                    beforeTxt.isVisible = true
                                    afterTxt.isVisible = true
                                    divider.isVisible = true
                                    handle.isVisible = true
                                    if (currentState == -1) {
                                        root.progress = animationProgress
                                        currentState = R.id.end_before_after
                                        root.transitionToState(currentState, 1500)
                                    } else {
                                        resumeAnimation()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun resumeAnimation() {
        _binding?.root?.let {
            it.progress = animationProgress
            if (currentState != -1) {
                if (it.progress == 1.0f) {
                    it.transitionToState(
                        if (currentState == R.id.start_before_after) R.id.end_before_after else R.id.start_before_after,
                        1500
                    )
                } else {
                    it.transitionToState(currentState, 1500)
                }
            }
        }
    }

    private fun applyImage(
        path: String,
        view: ImageView,
        isEnhancePath: Boolean = false,
        myCallback: (success: Boolean) -> Unit,
    ) {
        Log.i("TAG", "onResume: fragmentPosition $view")

        if (isEnhancePath) {
            // Apply transformation when isEnhancePath is true
            Glide.with(view.context)
                .asBitmap()
                .load(path)
                .dontAnimate()
                .transform(object : BitmapTransformation() {
                    override fun transform(
                        pool: BitmapPool,
                        toTransform: Bitmap,
                        outWidth: Int,
                        outHeight: Int,
                    ): Bitmap {
                        return toTransform
                    }

                    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
                        messageDigest.update("color_matrix_transform".toByteArray())
                    }
                })
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        ConstantsCommon.originalHeightEnhanceImage = resource.height
                        ConstantsCommon.originalWidthEnhanceImage = resource.width
                        restoreImageViewModel.originalWidth = resource.width
                        restoreImageViewModel.originalHeight = resource.height
                        myCallback.invoke(true)
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        Log.i("TAG", "onLoadFailed: $e")
                        if (!isNetworkAvailable) {
                            _binding?.motionLayout?.let {
                                Toast.makeText(
                                    it.context,
                                    it.context.setString(com.project.common.R.string.no_internet_connect_found_please_try_again),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        myCallback.invoke(false)
                        return false
                    }
                })
                .into(view)
        } else {
            // Original implementation without transformation
            Glide.with(view.context)
                .asBitmap()
                .load(path)
                .dontAnimate()
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        myCallback.invoke(true)
                        return false
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        if (!isNetworkAvailable) {
                            _binding?.motionLayout?.let {
                                Toast.makeText(
                                    it.context,
                                    it.context.setString(com.project.common.R.string.no_internet_connect_found_please_try_again),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        myCallback.invoke(false)
                        return false
                    }
                })
                .into(view)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.root?.removeTransitionListener(motionListener)
        motionListener = null
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
            initDiscardDialog()
        } catch (ex: Exception) {
            Log.e("error", "backPress: ", ex)
        }
    }

    private fun initDiscardDialog() {

        if (bottomSheetDiscardDialogBinding == null) {
            bottomSheetDiscardDialogBinding =
                BottomSheetDiscardPhotoEditorBinding.inflate(layoutInflater)
            bottomSheetDiscardDialog = context?.let {
                BottomSheetDialog(it, com.project.common.R.style.BottomSheetDialogNew).apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            }
            bottomSheetDiscardDialogBinding?.root?.let {
                bottomSheetDiscardDialog?.setContentView(it)
            }
            bottomSheetDiscardDialog?.setCancelable(true)
        }

        bottomSheetDiscardDialogBinding?.let { binding ->
            runCatching {
                activity?.let { mActivity ->
                    mActivity.loadAndShowNativeOnBoarding(
                        loadedAction = {
                            if (isVisible && !isDetached) {
                                binding.nativeContainer.show()
                                binding.mediumNativeLayout.adContainer.show()
                                binding.mediumNativeLayout.shimmerViewContainer.hide()
                                binding.mediumNativeLayout.adContainer.removeAllViews()
                                if (it?.parent != null) {
                                    (it.parent as ViewGroup).removeView(it)
                                }
                                binding.mediumNativeLayout.adContainer.addView(it)
                            }
                        }, failedAction = {
                            if (isVisible && !isDetached) {
                                binding.apply {
                                    nativeContainer.hide()
                                    mediumNativeLayout.adContainer.hide()
                                    mediumNativeLayout.shimmerViewContainer.hide()
                                }
                            }
                        },
                        mActivity.nativeDialogsConfig(), mActivity.nativeDialogsConfig(),
                        showContainer = {
                            if (isVisible && !isDetached) {
                                binding.apply {
                                    nativeContainer.show()
                                    if (mediumNativeLayout.shimmerViewContainer.isVisible) {
                                        mediumNativeLayout.shimmerViewContainer.startShimmer()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        initDiscardBottomSheetClicks()

        kotlin.runCatching {
            if (isVisible && !isDetached && bottomSheetDiscardDialog?.isShowing == false) {
                bottomSheetDiscardDialog?.show()
            }
        }
    }

    private fun initDiscardBottomSheetClicks() {

        bottomSheetDiscardDialogBinding?.crossImg?.setOnSingleClickListener {
            if (isVisible && !isDetached && bottomSheetDiscardDialog?.isShowing == true) {
                bottomSheetDiscardDialog?.dismiss()
            }
        }
        bottomSheetDiscardDialogBinding?.discardBtn?.setOnSingleClickListener {

            if (rewardedShown)
                rewardedShown = false

            activity?.let {

                if (!it.isFinishing && !it.isDestroyed && bottomSheetDiscardDialog?.isShowing == true) {
                    bottomSheetDiscardDialog?.dismiss()
                }

                if (!interstitialStrategyOld) {
                    activity?.showNewInterstitial(activity?.interstitialBack()) {
                        runCatching {
                            if (it is PencilSketchActivity) {

                                // Notify the previous fragment if needed
                                eventForGalleryAndEditor("sketch_result", "back")
                                setFragmentResult(
                                    "requestKeyGallery",
                                    bundleOf("refresh" to true)
                                )
                                val targetDestinationId = R.id.galleryPencilSketch // Replace with the actual ID of your target fragment in the navigation graph
                                findNavController().popBackStack(targetDestinationId, false)
                            }
                        }
                    }
                } else {
                    it.showInterstitial(loadedAction = {

                        runCatching {
                            if (it is PencilSketchActivity) {

                                // Notify the previous fragment if needed
                                eventForGalleryAndEditor("sketch_result", "back")
                                setFragmentResult(
                                    "requestKeyGallery",
                                    bundleOf("refresh" to true)
                                )
                                val targetDestinationId = R.id.galleryPencilSketch // Replace with the actual ID of your target fragment in the navigation graph
                                findNavController().popBackStack(targetDestinationId, false)
                            }
                        }
                    }, failedAction = {
                        runCatching {
                            if (it is PencilSketchActivity) {

                                // Notify the previous fragment if needed
                                eventForGalleryAndEditor("sketch_result", "back")
                                setFragmentResult(
                                    "requestKeyGallery",
                                    bundleOf("refresh" to true)
                                )
                                val targetDestinationId = R.id.galleryPencilSketch // Replace with the actual ID of your target fragment in the navigation graph
                                findNavController().popBackStack(targetDestinationId, false)
                            }
                        }
                    }, showAd = showDiscardClickAd, onCheck = true)
                }
            }
        }

        runCatching {
            bottomSheetDiscardDialogBinding?.stayBtn?.text =
                activity?.setString(com.project.common.R.string.stay)
        }

        bottomSheetDiscardDialogBinding?.stayBtn?.setOnSingleClickListener {
            if (isVisible && !isDetached && bottomSheetDiscardDialog?.isShowing == true) {
                bottomSheetDiscardDialog?.dismiss()
            }
        }
    }


    private fun checkForWaterMark() {
        if (!isProVersion()) {
            lifecycleScope.launch(IO) {
                if (billingDataStore.readAndShowWaterMark()) {
                    withContext(Main) {
                        restoreImageViewModel.removeWaterMark = isProVersion()
                        //   binding.waterMarkLayout.isVisible = !isProVersion()

                        if (!isProVersion()) {

                            /* binding.waterMarkLayout.animateWaterMarkNew {
                                 _binding?.let {
                                     binding.waterMarkLayout.zoomInAnimation()
                                 }
                             }*/
                        }
                    }
                } else {
                    withContext(Main) {
                        // binding.waterMarkLayout.isVisible = false
                        restoreImageViewModel.removeWaterMark = true
                    }
                }
            }
        } else {
            // binding.waterMarkLayout.isVisible = false
            restoreImageViewModel.removeWaterMark = true
        }
    }


    private fun observerSave() {

        restoreImageViewModel.saveState.observe(viewLifecycleOwner) {
            when (it) {
                is SketchSaveViewState.SaveClick -> {
                    isSaving = true
                    restoreImageViewModel.resetSaveState()
                }

                is SketchSaveViewState.UpdateProgress -> {
                    bottomSheetProcessDialogBinding?.progressBar?.progress = it.progress
                }

                is SketchSaveViewState.UpdateProgressText -> {
                    bottomSheetProcessDialogBinding?.textView7?.text = it.text
                }

                is SketchSaveViewState.Success -> {
                    finalPath = it.path
                    navigateSaveAndShare()
                }

                is SketchSaveViewState.Error -> {
                    currentFeature = EditorBottomTypes.NONE
                    if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
                    bottomSheetProcessDialog = null
                    bottomSheetProcessDialogBinding = null
                    restoreImageViewModel.resetSaveState()
                    isSaving = false
                }

                is SketchSaveViewState.Cancel -> {
                    currentFeature = EditorBottomTypes.NONE
                    if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
                    bottomSheetProcessDialog = null
                    bottomSheetProcessDialogBinding = null
                    restoreImageViewModel.resetSaveState()
                    isSaving = false
                }

                is SketchSaveViewState.Back -> {
                    currentFeature = EditorBottomTypes.NONE
                    binding.motionLayout.transitionToState(R.id.start_before_after, 250)
                    restoreImageViewModel.resetSaveState()
                    isSaving = false
                }

                is SketchSaveViewState.Idle -> {}
            }
        }
    }


    private fun navigateSaveAndShare() {
        if (!alreadyAdShown) {
            alreadyAdShown = true
            activity?.showNewInterstitial(activity?.saveInterstitial()) {
                afterSaveAdNavigate()
            }
        } else {
            ConstantsCommon.saveSession += 1
            currentFeature = EditorBottomTypes.NONE
            if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
            bottomSheetProcessDialog = null
            bottomSheetProcessDialogBinding = null
            fromSaved = true
            if (rewardedShown)
                rewardedShown = false
            restoreImageViewModel.resetSaveState()
            kotlin.runCatching {
                activity?.let {
                    val intent = Intent()
                    intent.setClassName(
                        it.applicationContext,
                        "com.fixai.main.ui.activities.save.SaveAndShareActivity"
                    )
                    intent.putExtra("image_path", finalPath)
                    saveActivityLauncher?.launch(intent)
                    if (it is PencilSketchActivity) {
                        it.finish()
                    }
                }
                alreadyAdShown = false
            }
            isSaving = false
        }
    }

    private fun afterSaveAdNavigate() {
        ConstantsCommon.saveSession += 1
        currentFeature = EditorBottomTypes.NONE
        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
        bottomSheetProcessDialog = null
        bottomSheetProcessDialogBinding = null
        fromSaved = true
        restoreImageViewModel.resetSaveState()
        kotlin.runCatching {
            activity?.let {
                val intent = Intent()
                intent.setClassName(
                    it.applicationContext,
                    "com.fixai.main.ui.activities.save.SaveAndShareActivity"
                )
                intent.putExtra("image_path", finalPath)
                intent.putExtra("from_editor", "enhancer")
                saveActivityLauncher?.launch(intent)
            }
            alreadyAdShown = false
        }
        isSaving = false
    }


    private var saveActivityLauncher: ActivityResultLauncher<Intent>? = null


    private fun registerSaveLauncher() {
        kotlin.runCatching {
            saveActivityLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        val clickFrom = it.getStringExtra("where") ?: ""
                        if (clickFrom.isNotBlank()) {
                            when (clickFrom) {
                                "home" -> {
                                    kotlin.runCatching {
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("backpress", true)
                                        resultIntent.putExtra("from_draft", ConstantsCommon.isDraft)
                                        activity?.setResult(RESULT_OK, resultIntent)
                                        activity?.finish()
                                    }
                                }

                                "back" -> {

                                }

                                "makeAnOther" -> {
                                    kotlin.runCatching {
                                        activity?.let {
                                            if (it is PencilSketchActivity) {
                                                it.finish()
                                            }
                                        }

                                    }
                                }

                                else -> {
                                    val resultIntent = Intent()
                                    //resultIntent.putExtra("where", parentScreen.lowercase())
                                    resultIntent.putExtra("resultBack", true)
                                    resultIntent.putExtra("where", clickFrom)
                                    resultIntent.putExtra("from_draft", ConstantsCommon.isDraft)
                                    activity?.setResult(RESULT_OK, resultIntent)
                                    activity?.finish()
                                }

                            }
                        }
                    }
                }
            }
        }

    }


}
