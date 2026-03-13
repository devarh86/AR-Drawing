package com.abdul.pencil_sketch.main.fragment

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.abdul.pencil_sketch.databinding.FragmentSaveShareBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.intents.SaveIntentSketch
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.main.viewstate.SketchSaveViewState
import com.abdul.pencil_sketch.utils.loadBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.ads.Constants.rewardedShown
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.showNewInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.homeInterstitial
import com.example.ads.utils.nativeDialogsConfig
import com.example.inapp.helpers.showToast
import com.example.inapp.repo.datastore.BillingDataStore
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.databinding.BottomSheetDiscardPhotoEditorBinding
import com.project.common.databinding.BottomSheetProcessDialogBinding
import com.project.common.enum_classes.EditorBottomTypes
import com.project.common.enum_classes.SaveQuality
import com.project.common.model.SavingModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.fromSaved
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.gettingQuality
import com.project.common.utils.setOnSingleClickListener
import com.project.common.utils.setString
import com.project.gallery.utils.createOrShowSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SaveAndShareFragment : Fragment() {

    private var _binding: FragmentSaveShareBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController

    private val sketchImageViewModel: PencilSketchViewModel by activityViewModels()
    private var isSaving = false
    private var alreadyAdShown = false

    private var currentFeature: EditorBottomTypes = EditorBottomTypes.NONE

    private var bottomSheetProcessDialog: BottomSheetDialog? = null
    private var bottomSheetProcessDialogBinding: BottomSheetProcessDialogBinding? = null

    private var bottomSheetDiscardDialog: BottomSheetDialog? = null
    private var bottomSheetDiscardDialogBinding: BottomSheetDiscardPhotoEditorBinding? = null
    private var callback: OnBackPressedCallback? = null

    @set:Inject
    lateinit var billingDataStore: BillingDataStore

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
            _binding = FragmentSaveShareBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUserImage()
        listener()
        observerSave()
        onBackPress()

        activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
    }

    private fun setUserImage() {
        _binding?.shimmerView?.isVisible = true
        _binding?.previewIV?.isVisible = true
        _binding?.let { binding ->
            Glide.with(mActivity)
                .asBitmap().load(sketchImageViewModel.cameraPath)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        sketchImageViewModel.originalWidth = resource.width
                        sketchImageViewModel.originalHeight = resource.height
                        _binding?.shimmerView?.isVisible = false
                        _binding?.shimmerView?.stopShimmer()
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
                            Toast.makeText(
                                mContext,
                                mContext.setString(com.project.common.R.string.no_internet_connect_found_please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return false
                    }
                }).into(binding.previewIV)
        }
    }

    private fun listener() {
        binding.apply {

            backPress.setOnSingleClickListener {
                backPress("back")
            }

            home.setOnSingleClickListener {
                if (!isSaving) {
                    backPress("home")
                } else {
                    runCatching {
                        val resultIntent = Intent()
                        resultIntent.putExtra("where", "home")
                        if (activity is PencilSketchActivity) {
                            activity?.setResult(RESULT_OK, resultIntent)
                            activity?.finish()
                        }
                    }
                }

            }

            share.setOnSingleClickListener {
                activity?.shareImage(sketchImageViewModel.cameraPath)
            }

            saveTxt.setOnSingleClickListener {
                if (!isSaving) {
                    isSaving = true
//                    if (isOpenCVSuccess) {
                    eventForGalleryAndEditor("sketch_result", "save")
                    currentFeature = EditorBottomTypes.SAVE

                    lifecycleScope.launch(IO) {
                        runCatching {
                            billingDataStore.readSaveQuality().first().let {
                                sketchImageViewModel.currentQuality =
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
                                sketchImageViewModel.currentQuality,
                                sketchImageViewModel.originalWidth,
                                sketchImageViewModel.originalHeight
                            )?.let { qualityWithWaterMark ->
                                qualityWithWaterMark.pair.apply {
                                    sketchImageViewModel.savingWidth = first
                                    sketchImageViewModel.savingHeight = second
                                }
                                sketchImageViewModel.waterMarkAsset = qualityWithWaterMark.drawable
                            }


                            if (sketchImageViewModel.savingWidth != 0 || sketchImageViewModel.savingHeight != 0) {
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
//                    } else {
//                        isSaving = false
//                        activity?.createOrShowSnackBar(binding.root, 0, "Saving Failed!", true)
//                    }

                } else {
                    mContext.showToast("Image Already Saved!")
                }
            }

        }
    }

    private fun saving() {
        initProcessDialog()
        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == false && isVisible && !isDetached) bottomSheetProcessDialog?.show()
        try {
            lifecycleScope.launch(IO) {
                context?.loadBitmap(sketchImageViewModel.cameraPath) {
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
                            sketchImageViewModel.saveIntent?.send(it)
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

    private fun observerSave() {

        sketchImageViewModel.saveState.observe(viewLifecycleOwner) {
            when (it) {
                is SketchSaveViewState.SaveClick -> {
                    isSaving = true
                    sketchImageViewModel.resetSaveState()
                }

                is SketchSaveViewState.UpdateProgress -> {
                    bottomSheetProcessDialogBinding?.progressBar?.progress = it.progress
                }

                is SketchSaveViewState.UpdateProgressText -> {
                    bottomSheetProcessDialogBinding?.textView7?.text = it.text
                }

                is SketchSaveViewState.Success -> {
                    navigateSaveAndShare()
                }

                is SketchSaveViewState.Error -> {
                    currentFeature = EditorBottomTypes.NONE
                    if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
                    bottomSheetProcessDialog = null
                    bottomSheetProcessDialogBinding = null
                    sketchImageViewModel.resetSaveState()
                    isSaving = false
                }

                is SketchSaveViewState.Cancel -> {
                    currentFeature = EditorBottomTypes.NONE
                    if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
                    bottomSheetProcessDialog = null
                    bottomSheetProcessDialogBinding = null
                    sketchImageViewModel.resetSaveState()
                    isSaving = false
                }

                is SketchSaveViewState.Back -> {
                    currentFeature = EditorBottomTypes.NONE
                    sketchImageViewModel.resetSaveState()
                    isSaving = false
                }

                is SketchSaveViewState.Idle -> {}
            }
        }
    }

    private fun navigateSaveAndShare() {
        if (!alreadyAdShown) {
            alreadyAdShown = true
            activity?.showNewInterstitial(activity?.homeInterstitial()) {
                afterSaveAdNavigate()
            }
        } else {
            ConstantsCommon.saveSession += 1
            currentFeature = EditorBottomTypes.NONE
            if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
            bottomSheetProcessDialog = null
            bottomSheetProcessDialogBinding = null
            fromSaved = true
            if (rewardedShown) rewardedShown = false
            sketchImageViewModel.resetSaveState()
            alreadyAdShown = false
            mContext.showToast("Image Saved!")
        }
    }

    private fun afterSaveAdNavigate() {
        ConstantsCommon.saveSession += 1
        currentFeature = EditorBottomTypes.NONE
        if (bottomSheetProcessDialog != null && bottomSheetProcessDialog?.isShowing == true && isVisible && !isDetached) bottomSheetProcessDialog?.dismiss()
        bottomSheetProcessDialog = null
        bottomSheetProcessDialogBinding = null
        fromSaved = true
        if (rewardedShown) rewardedShown = false
        sketchImageViewModel.resetSaveState()
        alreadyAdShown = false
        mContext.showToast("Image Saved!")
    }

    private fun Activity.shareImage(uri: String) {
        runOnUiThread {
            kotlin.runCatching {
                val newUri = if (fromSaved) {
                    uri.toUri()
                } else {
                    val authority = "${packageName}.provider"
                    FileProvider.getUriForFile(this, authority, File(uri))
                }
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                // shareIntent.putExtra(Intent.EXTRA_STREAM, uri.toUri())
                shareIntent.putExtra(Intent.EXTRA_STREAM, newUri)
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${getString(com.project.common.R.string.share_text)} https://play.google.com/store/apps/details?id=$packageName"
                )
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }
    }

    private fun onBackPress() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPress("back")
            }
        }
        callback?.let {
            activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner, it)
        }
    }

    private fun backPress(from: String = "") {
        try {
            initDiscardDialog(from)
        } catch (ex: Exception) {
            Log.e("error", "backPress: ", ex)
        }
    }

    private fun initDiscardDialog(from: String = "") {

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

        initDiscardBottomSheetClicks(from)

        kotlin.runCatching {
            if (isVisible && !isDetached && bottomSheetDiscardDialog?.isShowing == false) {
                bottomSheetDiscardDialog?.show()
            }
        }
    }

    private fun initDiscardBottomSheetClicks(from: String = "") {

        bottomSheetDiscardDialogBinding?.crossImg?.setOnSingleClickListener {
            if (isVisible && !isDetached && bottomSheetDiscardDialog?.isShowing == true) {
                bottomSheetDiscardDialog?.dismiss()
            }
        }

        runCatching {
            bottomSheetDiscardDialogBinding?.discardBtn?.text = activity?.setString(com.project.common.R.string.discord)
            bottomSheetDiscardDialogBinding?.stayBtn?.text = activity?.setString(com.project.common.R.string.stay)
            bottomSheetDiscardDialogBinding?.textView8?.text = activity?.setString(com.project.common.R.string.are_you_want_to_discard)
            bottomSheetDiscardDialogBinding?.textView11?.text = activity?.setString(com.project.common.R.string.if_you_go_back_your_work_will_be_discarded)
        }

        bottomSheetDiscardDialogBinding?.discardBtn?.setOnSingleClickListener {

            if (rewardedShown)
                rewardedShown = false

            activity?.let {

                if (!it.isFinishing && !it.isDestroyed && bottomSheetDiscardDialog?.isShowing == true) {
                    bottomSheetDiscardDialog?.dismiss()
                }

                activity?.showNewInterstitial(activity?.homeInterstitial()) {
                    activity?.loadNewInterstitial(activity?.homeInterstitial()) {}
                    if (from == "back") {
                        navController.navigateUp()
                    } else {
                        runCatching {
                            val resultIntent = Intent()
                            resultIntent.putExtra("where", "home")
                            if (activity is PencilSketchActivity) {
                                activity?.setResult(RESULT_OK, resultIntent)
                                activity?.finish()
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


}