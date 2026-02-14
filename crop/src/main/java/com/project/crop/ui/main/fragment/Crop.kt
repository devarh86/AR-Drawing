package com.project.crop.ui.main.fragment

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ads.Constants
import com.example.ads.Constants.interstitialStrategyOld
import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.utils.interstitialBack
import com.example.inapp.repo.datastore.BillingDataStore
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.setOnSingleClickListener
import com.project.crop.databinding.FragmentCropBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class Crop : Fragment() {

    private var _binding: FragmentCropBinding? = null
    private val binding get() = _binding!!

    private var lastSelected: ImageView? = null

    private var isOpenFromBlend: Boolean = false
    private var isOpenFromEnhancer: Boolean = false

    private var callback: OnBackPressedCallback? = null

    private var firstTime = true
    private var mActivity: Activity? = null

    private var filePath: String? = null
    private var isFlipVertical = false
    private var isFlipHorizontal = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    @set:Inject
    lateinit var billingDataStore: BillingDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        if (_binding == null) {
            _binding = FragmentCropBinding.inflate(inflater, container, false)
        }

        onBackPress()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (firstTime) {

            firstTime = false

            init()

            if (isOpenFromBlend) {
                eventForGalleryAndEditor("crop_blend", "")
            } else if (isOpenFromEnhancer) {
                eventForGalleryAndEditor("crop_enhancer", "")
            }

            initClick()
            hideHorizontalScroll()

        }
    }

    private fun init() {

        lastSelected = binding.oneRatioOne

        arguments?.let { bundle ->
            bundle.getString("imagePath", "")?.let {
                filePath = it
                bundle.getBoolean("flipVertical", false).let { flipVertical ->
                    bundle.getBoolean("flipHorizontal", false).let { flipHorizontal ->
                        binding.cropView.setImageFilePath(it, flipVertical, flipHorizontal)
                    }
                }
            }
            bundle.getBoolean("openForBlend", false).let { openFromBlend ->
                isOpenFromBlend = openFromBlend
            }
            bundle.getBoolean("openForEnhancer", false).let { openFromEnhancer ->
                isOpenFromEnhancer = openFromEnhancer
            }
            bundle.getString("frameRatio", "").let {
                if (it.isNotEmpty()) {
                    val ratio = it.split(':')
                    if (ratio.size > 1) {
                        binding.cropView.setAspectRatio(
                            ratio[0].toInt(),
                            ratio[1].toInt()
                        )
                        binding.cropView.invalidate()
                    }
                }
            }
        }

        lifecycleScope.launch(IO) {
            kotlin.runCatching {
                if (!billingDataStore.readIsAlreadyShownCropBlend().first()) {
                    withContext(Main) {
                        if (isOpenFromBlend) {
                            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    binding.animationView.setAnimation(com.project.common.R.raw.crop_image_animation_night)
                                }

                                Configuration.UI_MODE_NIGHT_NO -> {
                                    binding.animationView.setAnimation(com.project.common.R.raw.crop_image_animation_light)
                                }

                                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                    binding.animationView.setAnimation(com.project.common.R.raw.crop_image_animation_light)
                                }
                            }
                            binding.animationView.playAnimation()
                            binding.animationView.isVisible = true
                        }
                    }
                } else {
                    withContext(Main) {
                        binding.animationView.pauseAnimation()
                        binding.animationView.isVisible = false
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        runCatching {
            setFragmentResult(
                "fromCrop",
                bundleOf(
                    "configChange" to true,
                )
            )
            findNavController().popBackStack()
        }
    }


    private fun Context?.setColor(color: Int): Int {
        return try {
            this?.let {
                ContextCompat.getColor(this, color)
            } ?: 0
        } catch (ex: java.lang.Exception) {
            0
        }
    }

    private fun setSelection(currentView: ImageView) {
        try {
            lastSelected?.imageTintList =
                ColorStateList.valueOf(context.setColor(com.project.common.R.color.btn_icon_clr))
            lastSelected = currentView
            lastSelected?.imageTintList =
                ColorStateList.valueOf(context.setColor(com.project.common.R.color.selected_color))
        } catch (ex: java.lang.Exception) {
            Log.e("error", "initClick: ", ex)
        }
    }

    fun calculateAspectRatio(width: Int, height: Int): String {

        try {
            if (width == 0 || height == 0)
                return "1:1"
            // Calculate the greatest common divisor (GCD) to simplify the ratio
            fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

            val divisor = gcd(width, height)

            // Calculate simplified ratio
            val aspectWidth = width / divisor
            val aspectHeight = height / divisor

            // Return the aspect ratio as a string
            return "$aspectWidth:$aspectHeight"
        } catch (ex: java.lang.Exception) {
            return "1:1"
        }
    }

    private fun hideHorizontalScroll() {
        isOpenFromBlend.let {
            if (it) {
                binding.scrollView.visibility = View.GONE
                binding.linearLayoutFlip.visibility = View.GONE
            } else {
                binding.scrollView.visibility = View.VISIBLE
                binding.linearLayoutFlip.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        var fromCrop: Boolean = false
    }

    private fun initClick() {

        binding.animationView.setOnSingleClickListener {
            binding.animationView.pauseAnimation()
            binding.animationView.isVisible = false
            lifecycleScope.launch(IO) {
                billingDataStore.writeIsAlreadyShownCropBlend(true)
            }
        }
        binding.flipVertical.setOnSingleClickListener {
            filePath?.let {
                isFlipVertical = !isFlipVertical
                binding.cropView.setImageFilePath(it, isFlipVertical, false)
            }
        }

        binding.flipHorizon.setOnSingleClickListener {
            filePath?.let {
                isFlipHorizontal = !isFlipHorizontal
                binding.cropView.setImageFilePath(it, false, isFlipHorizontal)
            }
        }

        binding.oneRatioOne.setOnSingleClickListener {
            setSelection(binding.oneRatioOne)
            binding.cropView.setAspectRatio(1, 1)
        }
        binding.twoRatioThree.setOnSingleClickListener {
            setSelection(binding.twoRatioThree)
            binding.cropView.setAspectRatio(2, 3)
        }
        binding.threeRatioTwo.setOnSingleClickListener {
            setSelection(binding.threeRatioTwo)
            binding.cropView.setAspectRatio(3, 2)
        }
        binding.threeRatioFour.setOnSingleClickListener {
            setSelection(binding.threeRatioFour)
            binding.cropView.setAspectRatio(3, 4)
        }
        binding.fourRatioThree.setOnSingleClickListener {
            setSelection(binding.fourRatioThree)
            binding.cropView.setAspectRatio(4, 3)
        }
        binding.nineRatioSixteen.setOnSingleClickListener {
            setSelection(binding.nineRatioSixteen)
            binding.cropView.setAspectRatio(9, 16)
        }
        binding.sixteenRatioNine.setOnSingleClickListener {
            setSelection(binding.sixteenRatioNine)
            binding.cropView.setAspectRatio(16, 9)
        }
        binding.tickIcon.setOnSingleClickListener {
            context?.let {
                binding.cropView.croppedRect.let {
                    binding.shimmerLayout.x = it.left
                    binding.shimmerLayout.y = it.top
                    binding.shimmerLayout.layoutParams.height = it.height().toInt()
                    binding.shimmerLayout.layoutParams.width = it.width().toInt()
                    binding.shimmerLayout.isVisible = true
                    binding.shimmerLayout.startShimmer()
                }

                runCatching {
                    binding.cropView.croppedImage?.let { it1 ->
                        if (isOpenFromBlend) {
                            lifecycleScope.launch(IO) {
                                kotlin.runCatching {
                                    val job = async(IO) {

                                        storeImage(
                                            context = it,
                                            it1
                                        )
                                    }
                                    job.await()?.let {
                                        withContext(Main) {
                                            try {
                                                fromCrop = true
                                                setFragmentResult(
                                                    "fromCrop", bundleOf(
                                                        "replace" to true,
                                                        "croppedImagePath" to it
                                                    )
                                                )
                                                binding.shimmerLayout.isVisible = false
                                                binding.shimmerLayout.stopShimmer()
                                                findNavController().popBackStack()

                                            } catch (ex: java.lang.Exception) {
                                                Log.e("error", "initClick: ", ex)
                                            }
                                        }
                                    } ?: kotlin.run {}
                                }.onFailure {}
                            }
                        } else {
                            var ratio = ""
                            lifecycleScope.launch(IO) {
                                val job = async(IO) {

                                    ratio =
                                        calculateAspectRatio(it1.width, it1.height)

                                    storeImage(
                                        context = it,
                                        it1
                                    )
                                }
                                job.await()?.let {
                                    runCatching {
                                        withContext(Main) {
                                            if (isOpenFromEnhancer) {
                                                eventForGalleryAndEditor(
                                                    "crop_enhancer",
                                                    "next"
                                                )
                                            }
                                            setFragmentResult(
                                                "fromCrop",
                                                bundleOf(
                                                    "replace" to true,
                                                    "croppedImagePath" to it,
                                                    "ratio" to ratio,
                                                    "height" to it1.height,
                                                    "width" to it1.width,
                                                )
                                            )
                                            Log.i(
                                                "CROP_AD",
                                                "initClick:  cropper ad---${Constants.showCropAd} "
                                            )
                                            activity?.showInterstitial({
                                                findNavController().popBackStack()
                                            }, {
                                                findNavController().popBackStack()
                                            }, showAd = Constants.showCropAd, onCheck = true)
                                        }
                                    }.onFailure {}
                                } ?: run {}
                            }
                        }
                    } ?: kotlin.run {}
                }
            }
        }
        binding.crossIcon.setOnSingleClickListener {
            backPress()
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

                runCatching {
                    activity?.showNewInterstitial(activity?.interstitialBack()) {
                        runCatching {
                            if (isOpenFromBlend) {
                                eventForGalleryAndEditor("crop_blend", "back")
                            } else if (isOpenFromEnhancer) {
                                eventForGalleryAndEditor("crop_enhancer", "back")
                            }
                            setFragmentResult(
                                "fromCrop",
                                bundleOf(
                                    "refresh" to true
                                )
                            )
                            findNavController().popBackStack()
                        }
                    }
                }

        } catch (ex: Exception) {
            Log.e("error", "backPress: ", ex)
        }
    }

    private fun storeImage(context: Context, image: Bitmap): String? {

        val dirPath = File(context.filesDir, "TempDirectoryForSavingImages")

        if (!dirPath.exists()) {
            dirPath.mkdirs()
        }

        val pictureFile = File(
            dirPath, System.currentTimeMillis().toString() + ".png"
        )
        try {
            val fos = FileOutputStream(pictureFile)
            if (!image.isRecycled) image.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            return pictureFile.absolutePath
        } catch (e: FileNotFoundException) {
            Log.e("error", "storeImage: ", e)
        } catch (e: IOException) {
            Log.e("error", "storeImage: ", e)
        }
        return null
    }
}