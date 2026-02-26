package com.abdul.pencil_sketch.main.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentDrawingBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.utils.AdjustableFrameLayout
import com.abdul.pencil_sketch.utils.ZoomableImageView
import com.abdul.pencil_sketch.utils.navigateFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.ads.Constants.rewardedShown
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.homeInterstitial
import com.example.ads.utils.nativeDialogsConfig
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.labstyle.darioscrollruler.ScrollRulerListener
import com.project.common.databinding.BottomSheetDiscardPhotoEditorBinding
import com.project.common.utils.setOnSingleClickListener
import com.project.gallery.utils.createOrShowSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@AndroidEntryPoint
class DrawingFragment : Fragment(), ZoomableImageView.ZoomImgEvents, AdjustableFrameLayout.FrameClicks {

    private var _binding: FragmentDrawingBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController

    private val sketchImageViewModel: PencilSketchViewModel by activityViewModels()

    private var isLocked = false
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isSketch = false
    private var callback: OnBackPressedCallback? = null
    private var bottomSheetDiscardDialogBinding: BottomSheetDiscardPhotoEditorBinding? = null
    private var bottomSheetDiscardDialog: BottomSheetDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null) {
            _binding = FragmentDrawingBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUIBottom()
        startCamera()
        setUserImage()
        listener()
        onBackPress()
    }

    private fun updateUIBottom() {
        binding.apply {
            isSketch = sketchImageViewModel.sketchMode == "sketch"
            binding.surfaceView.isInvisible = !isSketch
            binding.seekBarLayout.isVisible = !isSketch
            binding.seekBarLayoutCamera.isVisible = isSketch

            binding.apply {

                pictureMode.updateMode(
                    isSelected = !isSketch,
                    selectedIcon = R.drawable.ic_picture_on,
                    unSelectedIcon = R.drawable.ic_picture_off
                )

                cameraMode.updateMode(
                    isSelected = isSketch,
                    selectedIcon = R.drawable.ic_camera_select,
                    unSelectedIcon = R.drawable.ic_camera_unselect
                )

            }
        }
    }

    private fun startCamera(camerafacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA) {
        if (!isAdded) return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    if (!isAdded) return@addListener
                    it.surfaceProvider = binding.surfaceView.surfaceProvider
                }

            // ImageCapture
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val useCaseGroup = UseCaseGroup.Builder()
                .setViewPort(binding.surfaceView.viewPort ?: return@addListener)
                .addUseCase(imageCapture)
                .addUseCase(preview)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                Log.d("CheckCameraTAG", "Use case binding failed b4")
                val camera = cameraProvider?.bindToLifecycle(viewLifecycleOwner, camerafacing, useCaseGroup)
                Log.d("CheckCameraTAG", "Camera $camera")
            } catch (exc: Exception) {
                Log.e("CheckCameraTAG", "${exc.message}")
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    private fun setUserImage() {
        val parent = lifecycleScope
        parent.launch(Main) {
            binding.fgImage.frameChangingState = true
            binding.fgImage.tag = 0
            binding.fgImage.touchDisable = true
            binding.fgImage.setImageDrawable(null)
            withContext(IO) {
                context?.let { context ->
                    parent.launch(IO) {
                        Glide.with(context).asBitmap().override(1000)
                            .load(sketchImageViewModel.imageEnhancedPath[0].croppedPath)
                            .listener(object : RequestListener<Bitmap> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Bitmap>,
                                    isFirstResource: Boolean,
                                ): Boolean {
                                    try {
                                        if (isVisible) {
                                            _binding?.let {
                                                context.createOrShowSnackBar(
                                                    binding.root,
                                                    0,
                                                    "Failed to place image",
                                                    true,
                                                    null
                                                )
                                            }
                                        }
                                    } catch (ex: java.lang.Exception) {
                                        Log.e("error", "onLoadFailed: ", ex)
                                    }
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Bitmap,
                                    model: Any,
                                    target: Target<Bitmap>?,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean,
                                ): Boolean {
                                    _binding?.fgImage?.originalBitmap = resource
                                    return false
                                }

                            }).apply {
                                delay(40)
                                _binding?.let {
                                    withContext(Main) {
                                        into(binding.fgImage)
                                        _binding?.fgImage?.let { fgImage ->
                                            fgImage.viewTreeObserver.addOnGlobalLayoutListener(
                                                object :
                                                    ViewTreeObserver.OnGlobalLayoutListener {
                                                    override fun onGlobalLayout() {
                                                        _binding?.let {
                                                            val listener = this
                                                            if (fgImage.width > 100 && fgImage.height > 100 && _binding != null) {
                                                                fgImage.viewTreeObserver.removeOnGlobalLayoutListener(
                                                                    listener
                                                                )

                                                                binding.frameContainer.setFrameListener(
                                                                    this@DrawingFragment
                                                                )

                                                                binding.fgImage.fillOrCenterImageIntoView(
                                                                    ImageView.ScaleType.MATRIX
                                                                )
                                                                binding.frameContainer.userImage = binding.fgImage

                                                                binding.fgImage.setListener(
                                                                    this@DrawingFragment
                                                                )

                                                                binding.fgImage.frameChangingState = false
                                                                binding.fgImage.touchDisable = false
                                                                binding.fgImage.enableUserZoom(true)

                                                            }
                                                        }
                                                    }
                                                })
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun listener() {
        binding.apply {

            mediaPlayer?.setOnCompletionListener {
                isPlaying = false
                music.setImageResource(R.drawable.ic_music_off)
            }

            darioScrollRuler.scrollListener = object : ScrollRulerListener {
                override fun onRulerScrolled(value: Float) {
                    val rotation = value.roundToInt()
                    rotateTV.text = rotation.toString()
                    fgImage.setImageRotationUsingMatrix(rotation.toFloat())
                }
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val value = seekBar?.progress ?: 10
                        percentageTxt.text = "${value + 10}"
                        fgImage.setImageOpacity(value + 10)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            seekBarCamera.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val value = seekBar?.progress ?: 10
                        percentageTxtCamera.text = "${value + 10}"
                        fgImage.setImageOpacity(value + 10)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            lockMode.setOnSingleClickListener {

                isLocked = !isLocked

                if (isLocked) {
                    fgImage.touchDisable = true
                    fgImage.enableUserZoom(false)
                } else {
                    fgImage.touchDisable = false
                    fgImage.enableUserZoom(true)
                }

                updateLockUI(isLocked)
            }

            fullMode.setOnSingleClickListener {
                toolBar.isVisible = false
                bottom.isVisible = false
                fullModeOn.isVisible = true
            }

            fullModeOn.setOnSingleClickListener {
                toolBar.isVisible = true
                bottom.isVisible = true
                fullModeOn.isVisible = false
            }

            music.setOnSingleClickListener {

                if (mediaPlayer == null) {
                    // First time create
                    mediaPlayer = MediaPlayer.create(mContext, com.project.common.R.raw.music_file_1)
                }

                mediaPlayer?.let { player ->

                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                        music.setImageResource(R.drawable.ic_music_off)
                    } else {
                        player.start()  // Resume from paused position
                        isPlaying = true
                        music.setImageResource(R.drawable.ic_music_on)
                    }
                }
            }

            pictureMode.setOnSingleClickListener {
                pictureMode.updateMode(
                    isSelected = true,
                    selectedIcon = R.drawable.ic_picture_on,
                    unSelectedIcon = R.drawable.ic_picture_off
                )

                cameraMode.updateMode(
                    isSelected = false,
                    selectedIcon = R.drawable.ic_camera_select,
                    unSelectedIcon = R.drawable.ic_camera_unselect
                )

                rotateMode.updateMode(
                    isSelected = false,
                    selectedIcon = R.drawable.ic_rotate_select,
                    unSelectedIcon = R.drawable.ic_rotate_unselect
                )

                isSketch = true
                seekBarLayout.isVisible = true
                seekBarLayoutCamera.isVisible = false
                rotationLL.isVisible = false
                darioScrollRuler.isVisible = false
                cameraShow(true)
            }

            rotateMode.setOnSingleClickListener {
                rotateMode.updateMode(
                    isSelected = true,
                    selectedIcon = R.drawable.ic_rotate_select,
                    unSelectedIcon = R.drawable.ic_rotate_unselect
                )

                pictureMode.updateMode(
                    isSelected = false,
                    selectedIcon = R.drawable.ic_picture_on,
                    unSelectedIcon = R.drawable.ic_picture_off
                )

                cameraMode.updateMode(
                    isSelected = false,
                    selectedIcon = R.drawable.ic_camera_select,
                    unSelectedIcon = R.drawable.ic_camera_unselect
                )

                rotationLL.isVisible = true
                darioScrollRuler.isVisible = true
                reset.isVisible = true
                seekBarLayout.isVisible = false
                seekBarLayoutCamera.isVisible = false


                darioScrollRuler.reload(-360f, 360f, 0f)
                val initialRotation = darioScrollRuler.currentPositionValue.roundToInt()
                rotateTV.text = initialRotation.toString()
                fgImage.setImageRotationUsingMatrix(initialRotation.toFloat())

            }

            cameraMode.setOnSingleClickListener {
                cameraMode.updateMode(
                    isSelected = true,
                    selectedIcon = R.drawable.ic_camera_select,
                    unSelectedIcon = R.drawable.ic_camera_unselect
                )

                pictureMode.updateMode(
                    isSelected = false,
                    selectedIcon = R.drawable.ic_picture_on,
                    unSelectedIcon = R.drawable.ic_picture_off
                )

                rotateMode.updateMode(
                    isSelected = false,
                    selectedIcon = R.drawable.ic_rotate_select,
                    unSelectedIcon = R.drawable.ic_rotate_unselect
                )

                isSketch = false
                seekBarLayout.isVisible = false
                seekBarLayoutCamera.isVisible = true
                rotationLL.isVisible = false
                darioScrollRuler.isVisible = false
                cameraShow(false)
                startCamera()
            }

            reset.setOnSingleClickListener {
                fgImage.resetRotation()
                rotateTV.text = "0"
            }

            flipVertical.setOnSingleClickListener {
                fgImage.flipVertical()
            }

            flipHorizon.setOnSingleClickListener {
                fgImage.flipHorizontal()
            }

            rotateRight.setOnSingleClickListener {
                fgImage.setImageRotationUsingMatrix(fgImage.rotation + 90f)
            }

            rotateLeft.setOnSingleClickListener {
                fgImage.setImageRotationUsingMatrix(fgImage.rotation - 90f)
            }

            backPress.setOnSingleClickListener {
                backPress()
            }

            doneBtn.setOnSingleClickListener {
                mActivity.navigateFragment(
                    DrawingFragmentDirections.actionDrawingFragmentToCameraFragment(),
                    R.id.drawingFragment
                )
            }

        }
    }

    private fun cameraShow(mode: Boolean) {
        binding.surfaceView.isInvisible = mode
    }

    private fun updateLockUI(isLocked: Boolean) {
        if (isLocked) {
            binding.lockMode.setImageResource(R.drawable.ic_zoom_lock)
        } else {
            binding.lockMode.setImageResource(R.drawable.ic_zoom_unlock)
        }
    }

    override fun onLongPress(zoomableImageView: ZoomableImageView?) {

    }

    override fun onSinglePress(zoomableImageView: ZoomableImageView?) {

    }

    override fun actionUpFromDrag(zoomableImageView: ZoomableImageView?) {

    }

    override fun actionUpFromDragForDragDisable() {

    }

    override fun updateRotation(img: ZoomableImageView, rotation: Float) {

    }

    override fun updateRatioAfterRotation(img: ZoomableImageView?, bitmap: Bitmap) {
        img?.apply {
            lifecycleScope.launch(IO) {
                withContext(Main) {
                    imageInCenter = false
                    setImageBitmap(bitmap)
                    frameChangingState = false
                }
            }
        }
    }

    override fun onFrameClick() {

    }

    private fun TextView.updateMode(
        isSelected: Boolean,
        selectedIcon: Int,
        unSelectedIcon: Int
    ) {
        val color = if (isSelected)
            com.project.common.R.color.selected_color
        else
            com.project.common.R.color.text_color_drawing_screen

        setTextColor(ContextCompat.getColor(context, color))

        val drawableRes = if (isSelected) selectedIcon else unSelectedIcon

        setCompoundDrawablesWithIntrinsicBounds(0, drawableRes, 0, 0)
    }

    override fun onPause() {
        super.onPause()

        if (bottomSheetDiscardDialog != null && bottomSheetDiscardDialog?.isShowing == true && !isDetached) {
            runCatching {
                bottomSheetDiscardDialog?.dismiss()
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
            initDiscardDialog()
        } catch (ex: Exception) {
            Log.e("error", "backPress: ", ex)
        }
    }

    private fun initDiscardDialog() {

        if (bottomSheetDiscardDialogBinding == null) {
            bottomSheetDiscardDialogBinding = BottomSheetDiscardPhotoEditorBinding.inflate(layoutInflater)
            bottomSheetDiscardDialog = context?.let {
                BottomSheetDialog(it, com.project.common.R.style.BottomSheetDialogNew).apply {
                    window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
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
                        },
                        failedAction = {
                            if (isVisible && !isDetached) {
                                binding.apply {
                                    nativeContainer.hide()
                                    mediumNativeLayout.adContainer.hide()
                                    mediumNativeLayout.shimmerViewContainer.show()
                                }
                            }
                        },
                        mActivity.nativeDialogsConfig(),
                        mActivity.nativeDialogsConfig(),
                        showContainer = {
                            if (isVisible && !isDetached) {
                                binding.apply {
                                    nativeContainer.show()
                                    if (mediumNativeLayout.shimmerViewContainer.isVisible) {
                                        mediumNativeLayout.shimmerViewContainer.startShimmer()
                                    }
                                }
                            }
                        })
                }
            }
        }

        initDiscardBottomSheetClicks()

        runCatching {
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

            if (rewardedShown) rewardedShown = false

            activity?.let {

                if (!it.isFinishing && !it.isDestroyed && bottomSheetDiscardDialog?.isShowing == true) {
                    bottomSheetDiscardDialog?.dismiss()
                }

                it.showNewInterstitial(it.homeInterstitial()) {
                    it.loadNewInterstitial(it.homeInterstitial()) {}

                    runCatching {
                        if (it is PencilSketchActivity) {

                            if (it.isOpenFromMain) {
                                it.finish()
                            } else {

                                navController.navigateUp()

                                // Notify the previous fragment if needed
                                /*eventForGalleryAndEditor("drawing_screen", "back")
                                setFragmentResult(
                                    "requestKeyGallery", bundleOf("refresh" to true)
                                )
                                val direction = FaceSwapResultDirections.actionFaceSwapResultToGalleryFaceSwap(false)

                                val navOptions = NavOptions.Builder().setPopUpTo(R.id.faceSwapResult, true).build()

                                findNavController().navigate(direction, navOptions)*/
                            }

                        }
                    }

                }

            }
        }

        bottomSheetDiscardDialogBinding?.stayBtn?.setOnSingleClickListener {
            if (isVisible && !isDetached && bottomSheetDiscardDialog?.isShowing == true) {
                bottomSheetDiscardDialog?.dismiss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }


}
