package com.abdul.pencil_sketch.main.viewmodel

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.media.ExifInterface
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdul.pencil_sketch.main.intents.SaveIntentSketch
import com.abdul.pencil_sketch.main.intents.SketchIntent
import com.abdul.pencil_sketch.main.viewstate.SketchImageActionViewState
import com.abdul.pencil_sketch.main.viewstate.SketchSaveViewState
import com.abdul.pencil_sketch.utils.saveMediaToStorage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.ads.Constants.flowSelectPhotoScr
import com.project.common.R
import com.project.common.enum_classes.SaveQuality
import com.project.common.model.ImagesModel
import com.project.common.model.SavingModel
import com.project.common.repo.datastore.AppDataStore
import com.project.common.repository.EditorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.math.abs
import kotlin.math.roundToInt


@HiltViewModel
class PencilSketchViewModel @Inject constructor(
    private val editorRepository: EditorRepository,
    private val appDataStore: AppDataStore
) : ViewModel() {

    var restoreIntent: Channel<SketchIntent>? = null
    var saveIntent: Channel<SaveIntentSketch>? = null
    var currentRotation = 0f

    var imageEnhancedPath: MutableList<ImagesModel> = mutableListOf()
    var sketchMode = ""

    private val _state =
        MutableStateFlow<SketchImageActionViewState>(SketchImageActionViewState.Idle)
    val state: StateFlow<SketchImageActionViewState> get() = _state

    private val _saveState = MutableLiveData<SketchSaveViewState>(SketchSaveViewState.Idle)
    val saveState: LiveData<SketchSaveViewState> get() = _saveState

    private var mask: Any = ""
    var resultImgPath: String? = null
    var removeWaterMark: Boolean = false
    var originalWidth = 0
    var originalHeight = 0
    var savingWidth = 0
    var savingHeight = 0
    var waterMarkAsset: Int = 0
    var currentQuality = SaveQuality.LOW
    private var maxCounter = 20
    private val ramThresholdMb = 250

    private var ramMonitoringJob: Job? = null
    private var authKeyJob: Job? = null
    private var imageEnhancementJob: Job? = null
    private var savingJob: CoroutineScope? = null


    private val colorizeLogger = "COLORIZE"


    fun initViewModel() {}

    init {
        restoreIntent = Channel(Channel.UNLIMITED)
        saveIntent = Channel(Channel.UNLIMITED)
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch(IO) {
            restoreIntent?.consumeAsFlow()?.collect {
                when (it) {
                    is SketchIntent.GenerateToken -> {
                        if (imageEnhancedPath.isNotEmpty()) {
                            _state.value = SketchImageActionViewState.ShowLoadingState
                            try {
                                val context = it.context
                                runCatching {
                                    appDataStore.readAuthKeySketchCompleteRunBlock(context).let {
                                        runCatching {
                                            Log.i(colorizeLogger, "GenerateToken: $it")

                                            if (it.isBlank()) {
                                                restoreImage(context, "")
                                            } else {
                                                restoreImage(context, it)
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                updateErrorForEnhance()
                            }
                        }

                    }

                    is SketchIntent.AddCroppedImage -> {
                        _state.value = SketchImageActionViewState.Loading
                        addCroppedImage(it.index, it.path)
                    }

                    is SketchIntent.ImageEnhancementAndPlacing -> {

                    }

                    is SketchIntent.SaveImageForEditor -> {
                        copyIntoDataDirForEditor(context = it.context, bitmap = it.editorBitmap)

                    }

                    is SketchIntent.SaveImages -> {
                        copyIntoDataDir(
                            context = it.context
                        )
                    }

                    SketchIntent.SetFrame -> {
                        setLoadingState()
                        withContext(Main) {
//                            _state.value = ColorizeActionViewState.UpdateFrame(
//                                filePath,
//                                isShape
//                            )
                        }

                    }

                    SketchIntent.SetImage -> {
                        setLoadingState()

                        withContext(Main) {
                            if (imageEnhancedPath.isNotEmpty())
                                _state.value =
                                    SketchImageActionViewState.SetUserImage(imageEnhancedPath[0].croppedPath)
                        }
                    }

                    is SketchIntent.SingleImageEnhancementAndPlacing -> {
                        singleImageEnhancement(
                            path = it.path, index = it.index
                        )
                    }
                }
            }
        }
        viewModelScope.launch(IO) {
            saveIntent?.consumeAsFlow()?.collect {
                when (it) {
                    is SaveIntentSketch.SaveClick -> {
                        saveClicked(it.resolution)
                    }

                    is SaveIntentSketch.Saving -> {
                        saving(it.context, it.savingModelList)
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun saveClicked(resolution: SaveQuality) {
        withContext(Main) {
            _saveState.value = SketchSaveViewState.SaveClick(resolution)
        }
    }


    private fun saving(
        context: Context, savingModelList: MutableList<SavingModel>,
    ) {
        savingJob = viewModelScope

        savingJob?.launch(IO) {
            try {

                ramMonitoringJob?.cancel()

                startMonitoringRAM(context)

                val outputSize = Size((savingWidth.toDouble()), (savingHeight.toDouble()))


                val newImageWidth = outputSize.width.toFloat()
                val newImageHeight = outputSize.height.toFloat()

                if (newImageHeight <= 0 || newImageWidth <= 0) {
                    return@launch
                }

                val outBitmap = Bitmap.createBitmap(
                    newImageWidth.roundToInt(), newImageHeight.roundToInt(), Config.ARGB_8888
                )

                val canvas = Canvas(outBitmap)

                val total = imageEnhancedPath.size + 3 // 3 steps

                var currentProgress = 1

                withContext(Main) {
                    if (savingJob?.isActive == true) {
                        _saveState.value = SketchSaveViewState.UpdateProgress(
                            currentProgress.times(100).div(total)
                        )
                    } else {
                        return@withContext
                    }
                }

                imageEnhancedPath.forEachIndexed { index, _ ->
                    if (savingJob?.isActive == true) {
                        if (index < savingModelList.size && index >= 0) {
                            savingModelList[index].apply {
                                userImageBitmap?.let {
                                    val cropMat = Mat()
                                    val resizeMatCropImg = Mat()
                                    Utils.bitmapToMat(it, cropMat)

                                    val cropSize = Size(
                                        (newImageWidth.toDouble() + 0),
                                        (newImageHeight.toDouble() + 0)
                                    )

                                    Imgproc.resize(
                                        cropMat,
                                        resizeMatCropImg,
                                        cropSize,
                                        0.0,
                                        0.0,
                                        Imgproc.INTER_LINEAR
                                    )
                                    val finalImgBitmap = try {
                                        Bitmap.createBitmap(
                                            resizeMatCropImg.cols(),
                                            resizeMatCropImg.rows(),
                                            Config.ARGB_8888
                                        )
                                    } catch (ex: OutOfMemoryError) {
                                        return@apply
                                    }

                                    Utils.matToBitmap(resizeMatCropImg, finalImgBitmap)
                                    resizeMatCropImg.release()

//                                    if (!it.isRecycled) {
//                                        it.recycle()
//                                    }

                                    canvas.drawBitmap(finalImgBitmap, 0f, 0f, null)
                                    if (!finalImgBitmap.isRecycled) {
                                        finalImgBitmap.recycle()
                                    }
                                }

                                if (savingJob?.isActive != true) {
                                    return@launch
                                }

                                currentProgress += 1

                                if (index + 1 == imageEnhancedPath.size) {

                                    savingJob?.launch(IO) newChild@{
                                        if (savingJob?.isActive != true) {
                                            return@newChild
                                        }

                                        withContext(Main) {
                                            _saveState.value = SketchSaveViewState.UpdateProgress(
                                                currentProgress.times(100)
                                                    .div(total)
                                            )
                                        }

                                        overlayBitmap?.let {
                                            val overlay = it

                                            val overlayMat = Mat()

                                            val resizeOverlayMat = Mat()

                                            Utils.bitmapToMat(overlay, overlayMat)

                                            if (savingJob?.isActive != true) {
                                                return@newChild
                                            }

                                            Imgproc.resize(
                                                overlayMat,
                                                resizeOverlayMat,
                                                outputSize,
                                                0.0,
                                                0.0,
                                                Imgproc.INTER_LINEAR
                                            )

                                            overlayMat.release()

                                            val overlayBitmap = Bitmap.createBitmap(
                                                resizeOverlayMat.cols(),
                                                resizeOverlayMat.rows(),
                                                Config.ARGB_8888
                                            )

                                            Utils.matToBitmap(
                                                resizeOverlayMat, overlayBitmap
                                            )

                                            canvas.drawBitmap(
                                                overlayBitmap, 0f, 0f, null
                                            )

                                            if (!overlayBitmap.isRecycled) overlayBitmap.recycle()

                                            if (!overlay.isRecycled) overlay.recycle()
                                        }

                                        currentProgress += 1

                                        withContext(Main) {
                                            _saveState.value = SketchSaveViewState.UpdateProgress(
                                                currentProgress.times(100)
                                                    .div(total)
                                            )
                                            _saveState.value =
                                                SketchSaveViewState.UpdateProgressText(
                                                    context.getString(R.string.saving_your_image)
                                                )
                                        }

//                                        if (!removeWaterMark && waterMarkAsset != 0) {
//                                            try {
//                                                val bitmapWaterMark =
//                                                    suspendCancellableCoroutine { continuation ->
//                                                        context.getBitmapWithGlideCache(
//                                                            waterMarkAsset
//                                                        ) { bitmap ->
//                                                            if (bitmap != null) {
//                                                                continuation.resume(bitmap)
//                                                            } else {
//                                                                continuation.resume(null)
//                                                            }
//                                                        }
//                                                    }
//                                                bitmapWaterMark?.let {
//                                                    val x = if (outBitmap.width == outBitmap.height)
//                                                        ((outBitmap.width.div(2)) - it.width.div(
//                                                            2
//                                                        )).toFloat()
//                                                    else
//                                                        (outBitmap.width - it.width).toFloat()
//                                                    val y = (outBitmap.height - it.height).toFloat()
//                                                    canvas.drawBitmap(bitmapWaterMark, x, y, null)
//                                                }
//                                            } catch (e: kotlin.Exception) {
//                                                Log.e("error", "savingBlend: ", e)
//                                            }
//                                        }


                                        if (savingJob?.isActive == true) {
                                            outBitmap.saveMediaToStorage(context = context) { path ->

                                                if (!outBitmap.isRecycled) outBitmap.recycle()
                                                savingJob?.let { coroutineScope ->
                                                    if (coroutineScope.isActive) {
                                                        savingJob?.launch(Main) {
                                                            if (path == null) {
                                                                ramMonitoringJob?.cancel()
                                                                _saveState.value =
                                                                    SketchSaveViewState.Error(
                                                                        context.getString(com.project.common.R.string.failed_to_save_image)
                                                                    )
                                                                // "Failed to save image"
                                                            } else {
                                                                savingJob?.let {
                                                                    if (it.isActive) {
                                                                        if (File(path).length() == 0L) {
                                                                            ramMonitoringJob?.cancel()
                                                                            _saveState.value =
                                                                                SketchSaveViewState.Error(
                                                                                    context.getString(
                                                                                        com.project.common.R.string.failed_to_save_image
                                                                                    )
                                                                                )
                                                                            // "Failed to save image"
                                                                        } else {
                                                                            currentProgress += 1
                                                                            _saveState.value =
                                                                                SketchSaveViewState.UpdateProgress(
                                                                                    currentProgress.times(
                                                                                        100
                                                                                    )
                                                                                        .div(
                                                                                            total
                                                                                        )
                                                                                )
                                                                            ramMonitoringJob?.cancel()
                                                                            _saveState.value =
                                                                                SketchSaveViewState.Success(
                                                                                    path
                                                                                )
                                                                        }
                                                                    } else {
                                                                        ramMonitoringJob?.cancel()
                                                                        _saveState.value =
                                                                            SketchSaveViewState.Cancel
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (!outBitmap.isRecycled) outBitmap.recycle()
                                            return@newChild
                                        }
                                    }

                                } else {
                                    withContext(Main) {
                                        _saveState.value = SketchSaveViewState.UpdateProgress(
                                            currentProgress.times(100).div(total)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        return@launch
                    }
                }
            } catch (ex: java.lang.Exception) {
                withContext(Main) {
                    _saveState.value =
                        SketchSaveViewState.Error(
                            context.getString(com.project.common.R.string.failed_to_save_image)

                        )
                    //"Failed to save image"
                }
            }
        }
    }

    private fun copyIntoDataDirForEditor(context: Context, bitmap: Bitmap) {
        imageEnhancementJob = viewModelScope.launch(IO) {
            Log.i("TAG", "copyIntoDataDir: imageEnhancementJob start")
            startMonitoringRAM(context)

            if (imageEnhancedPath.isEmpty()) {
                ramMonitoringJob?.cancel()
                return@launch
            }

            _state.value = SketchImageActionViewState.SaveLoading
            val tempList: MutableList<ImagesModel> = mutableListOf()
            tempList.addAll(imageEnhancedPath)
            var counter = 0

            // Function to update progress
            suspend fun updateProgress() {
                withContext(Main) {
                    _saveState.value =
                        SketchSaveViewState.UpdateProgress(counter * 100 / tempList.size)
                }
            }

            // Function to handle completion
            suspend fun completeSave(newPath: String?, index: Int) {
                withContext(Main) {
                    _saveState.value = SketchSaveViewState.Idle
                    _state.value = if (newPath == null) {
                        SketchImageActionViewState.Error("$index${context.getString(R.string.image_loading_failed)}")
                    } else {
                        SketchImageActionViewState.SaveComplete(true)
                    }
                    ramMonitoringJob?.cancel()
                }
            }

            tempList.forEachIndexed { index, imagesModel ->
                if (!isActive) {
                    _saveState.postValue(SketchSaveViewState.Idle)
                    _state.value =
                        SketchImageActionViewState.Error(context.getString(R.string.image_loading_failed))
                    ramMonitoringJob?.cancel()
                    return@forEachIndexed
                }

                val imagePath = imagesModel.croppedPath

                // Handling offline and online paths
                if (imagePath.isNotEmpty() && !context.isFilePathInDataDir(imagePath)) {
                    val newPath = storeImage(context, bitmap)
                    if (newPath != null) {
                        if (imageEnhancedPath.size > index) {
                            imageEnhancedPath[index].apply {
                                originalPath = newPath
                                croppedPath = newPath
                            }
                        }
                        counter++
                        updateProgress()

                        if (counter == tempList.size) {
                            completeSave(newPath, index)
                        }
                    } else {
                        withContext(Main) {
                            _state.value =
                                SketchImageActionViewState.Error(context.getString(R.string.image_saving_failed))
                        }
                    }
                } else {
                    counter++
                    updateProgress()
                    if (counter == tempList.size) {
                        withContext(IO) {
                            withContext(Main) {
                                _saveState.value = SketchSaveViewState.Idle
                                ramMonitoringJob?.cancel()
                                _state.value = SketchImageActionViewState.SaveComplete(true)
                            }
                        }
                    }
                }
            }
        }
    }


    private suspend fun addCroppedImage(index: Int, path: String) {
        Log.i("observeData", "observeData: $coroutineContext")
        if (index < imageEnhancedPath.size && index >= 0) {
            Log.i("observeData", "loop")
            imageEnhancedPath[index].croppedPath = path
            withContext(Main) {
                if (imageEnhancedPath.isNotEmpty())
                    _state.value = SketchImageActionViewState.UpdateImagePathsWithEnhancement(
                        index, path, true, mask, fromCrop = true
                    )
            }
        } else {
            Log.i("observeData", "else ---loop")
        }
    }

    private fun singleImageEnhancement(path: String, index: Int) {

        if (flowSelectPhotoScr != "new")
            _state.value = SketchImageActionViewState.Loading

        if (imageEnhancedPath.size > index) {
            imageEnhancedPath[index].originalPath = path
            imageEnhancedPath[index].croppedPath = path
        } else {
            imageEnhancedPath.add(ImagesModel(path, path))
        }

        _state.value = SketchImageActionViewState.UpdateImagePathsWithEnhancement(
            index, path, true, mask
        )
    }

    private fun setLoadingState() {
        _state.value = SketchImageActionViewState.Loading
    }

    private fun startMonitoringRAM(context: Context) {
        ramMonitoringJob = viewModelScope.launch(IO) {
            while (true) {
                if (ramMonitoringJob?.isActive == true) {
                    val currentRamUsageMb = getCurrentRamUsageMb(context = context)
                    currentRamUsageMb?.let { currentRam ->
                        Log.i("TAG", "startMonitoringRAM: $currentRam")
                        if (currentRam < ramThresholdMb) {
                            ramMonitoringJob?.cancel()
                            ramMonitoringJob = null
                            imageEnhancementJob?.cancel()
                            /*   bgRemoverJob?.cancel()
                               savingJob?.cancel()
                               applyBlendJob?.cancel()*/
                            Log.i("observeData", "observeData: error")
                            _state.value =
                                SketchImageActionViewState.Error(context.getString(R.string.close_background_apps_try_again))
                            return@launch
                        }
                    } ?: run {
                        return@launch
                    }
                    delay(1000)
                } else {
                    break
                }
            }
        }
    }


    private fun copyIntoDataDir(context: Context) {
        imageEnhancementJob = viewModelScope.launch(IO) {
            Log.i("TAG", "copyIntoDataDir: imageEnhancementJob start")
            startMonitoringRAM(context)

            if (imageEnhancedPath.isEmpty()) {
                ramMonitoringJob?.cancel()
                return@launch
            }

            _state.value = SketchImageActionViewState.SaveLoading
            val tempList: MutableList<ImagesModel> = mutableListOf()
            tempList.addAll(imageEnhancedPath)
            var counter = 0

            // Refactored method to update progress
            suspend fun updateProgress() {
                withContext(Main) {
                    _saveState.value =
                        SketchSaveViewState.UpdateProgress(counter * 100 / tempList.size)
                }
            }

            // Refactored method to handle completion
            suspend fun completeSave(newPath: String?, index: Int) {
                withContext(Main) {
                    _saveState.value = SketchSaveViewState.Idle
                    _state.value = if (newPath == null) {
                        SketchImageActionViewState.Error("$index${context.getString(R.string.image_loading_failed)}")//image loading failed, please try again
                    } else {
                        SketchImageActionViewState.SaveComplete()
                    }
                    ramMonitoringJob?.cancel()
                }
            }

            tempList.forEachIndexed { index, imagesModel ->
                if (!isActive) {
                    _saveState.postValue(SketchSaveViewState.Idle)
                    _state.value =
                        SketchImageActionViewState.Error(context.getString(R.string.image_loading_failed))//"image loading failed, please try again"
                    ramMonitoringJob?.cancel()
                    return@forEachIndexed
                }

                val imagePath = imagesModel.croppedPath

                val offlineSelection = (imagePath == "offline")

                if (offlineSelection) {
                    val path =
                        R.drawable.blend_gallery
                    val bitmap = decodeSampledBitmapFromResourceOffline(context, path)
                    if (bitmap != null) {
                        val newPath = storeImage(context, bitmap)
                        if (!bitmap.isRecycled) bitmap.recycle()
                        if (imageEnhancedPath.size > index) {
                            imageEnhancedPath[index].apply {
                                originalPath = newPath ?: ""
                                croppedPath = newPath ?: ""
                            }
                        }
                        counter++
                        updateProgress()

                        if (counter == tempList.size) {
                            completeSave(newPath, index)
                        }
                    } else {
                        withContext(Main) {
                            _state.value =
                                SketchImageActionViewState.Error(context.getString(R.string.image_is_corrupt))//"Image is corrupt"
                        }
                    }
                } else {

                    if (imagePath.isNotEmpty() && !context.isFilePathInDataDir(imagePath)) {
                        val imageFile = File(imagePath)
                        if (imageFile.exists()) {
                            val bitmap = decodeSampledBitmapFromResource(context, imagePath)
                            if (bitmap != null) {
                                val rotatedBitmap = rotateImage(bitmap, imagePath)?.also {
                                    if (it != bitmap && !bitmap.isRecycled) {
                                        bitmap.recycle()
                                    }
                                } ?: bitmap

                                val newPath = storeImage(context, rotatedBitmap)
                                if (!rotatedBitmap.isRecycled) rotatedBitmap.recycle()

                                if (imageEnhancedPath.size > index) {
                                    imageEnhancedPath[index].apply {
                                        originalPath = newPath ?: ""
                                        croppedPath = newPath ?: ""
                                    }
                                }

                                counter++
                                updateProgress()

                                if (counter == tempList.size) {

                                    completeSave(newPath, index)
                                }
                            } else {
                                withContext(Main) {
                                    _state.value =
                                        SketchImageActionViewState.Error(context.getString(R.string.image_is_corrupt))//"Image is corrupt"
                                }
                            }
                        } else {
                            withContext(Main) {
                                _state.value =
                                    SketchImageActionViewState.Error(context.getString(R.string.image_is_corrupt))
                            }
                        }
                    } else {
                        counter++
                        updateProgress()
                        if (counter == tempList.size) {
                            withContext(IO) {
                                withContext(Main) {
                                    _saveState.value = SketchSaveViewState.Idle
                                    ramMonitoringJob?.cancel()
                                    _state.value = SketchImageActionViewState.SaveComplete()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun rotateImage(bitmap: Bitmap, path: String): Bitmap? {
        try {
            val ei = ExifInterface(path)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
                ExifInterface.ORIENTATION_NORMAL -> bitmap
                else -> bitmap
            }
        } catch (ex: Exception) {
            Log.e("error", "rotateImage: ", ex)
            return null
        }
    }


    private fun decodeSampledBitmapFromResourceOffline(
        context: Context, path: Int,
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        return if (path == -1) null else BitmapFactory.Options().run {
            try {
                inJustDecodeBounds = true
                BitmapFactory.decodeResource(context.resources, path)

                // Calculate inSampleSize
                calculateInSampleSize(this)?.let {
                    inSampleSize = it
                } ?: {
                    null
                }
                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                if (ramMonitoringJob?.isActive == true) {
                    val bitmap = BitmapFactory.decodeResource(context.resources, path, this)
                    bitmap
                } else null
            } catch (ex: Exception) {
                null
            }
        }
    }

    private fun Context.isFilePathInDataDir(filePath: String): Boolean {
        val appDataDir = filesDir
        val file = File(filePath)
        return file.canonicalPath.startsWith(appDataDir.canonicalPath)
    }

    private fun decodeSampledBitmapFromResource(
        context: Context,
        path: String,
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            try {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, this)

                calculateInSampleSize(this)?.let {
                    inSampleSize = it
                } ?: run {
                    return null
                }

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                if (ramMonitoringJob?.isActive == true) {
                    val bitmap = BitmapFactory.decodeFile(path, this)
                    bitmap
                } else null
            } catch (ex: Exception) {
                Log.e("GALAIENHANCE", "decodeSampledBitmapFromResource: ${ex.message.toString()} ")
                null
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
    ): Int? {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height <= 0 || width <= 0) return null
        if (height > 600 || width > 600) {
            if (ramMonitoringJob?.isActive == false)
                return null
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= 750 && halfWidth / inSampleSize >= 750) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private suspend fun restoreImage(context: Context, key: String) {
        if (imageEnhancedPath.isNotEmpty()) {
            val path = File(imageEnhancedPath[0].croppedPath)
            var newcounter = 0
            editorRepository.resetValues()
            editorRepository.applyImageSketch(
                context.packageName,
                key,
                path,
                updateToken = {
                    viewModelScope.launch(IO) {
                        appDataStore.writeAuthKeySketch(it)
                    }
                },
                successCallback = { callback ->
                    callback?.let { resultPath ->
                        if (newcounter < 18) {
                            newcounter = 17
                        } else {
                            val difference = abs(newcounter - maxCounter)
                            if (newcounter == 18) {
                                maxCounter += (difference - 1)

                            } else {
                                maxCounter += (difference + 2) - difference
                            }
                        }
                        newcounter += 1
                        updateCounterAndViewState(newcounter)
                        viewModelScope.launch(IO) {
                            delay(1000)

                            preloadImageAndNavigate(
                                context = context,
                                resultPath,
                                newcounter
                            )
                        }
                        authKeyJob?.cancel()


                        Log.d(
                            "ENHNACEIMG",
                            "EnhancerViewModel: token--$resultPath"
                        )
                    } ?: run {
                        // _state.value = FrameViewState.Error("Found issue went something wrong")
                        updateErrorForEnhance()

                        Log.d("ENHNACEIMG", "EnhancerViewModel: run block")
                    }
                }, errorCallback = {
                    updateErrorForEnhance()
                }, loading = {
                    if (newcounter <= maxCounter) {
                        newcounter += 1
                        updateCounterAndViewState(newcounter)
                    }
                }
            )
        }
    }

    private suspend fun preloadImageAndNavigate(
        context: Context,
        resultPath: String,
        newcounter: Int
    ) {

        // Only proceed if context is still valid
        if (context is Activity)
            if (context.isDestroyed || context.isFinishing) {
                return
            }

        if (context is FragmentActivity) {
            if (context.isDestroyed || context.isFinishing) {
                return
            }
        }

        getCurrentRamUsageMb(context)?.let { ram ->
            Glide.with(context)
                .asBitmap()
                .override(if (ram > 500L) 1000 else 500)
                .load(resultPath)
                .timeout(60000)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap,
                        model: Any,
                        target: Target<Bitmap>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {

                        updateCounterAndViewState(newcounter + 1)

                        resultImgPath = resultPath

                        if (resource.height > 0 && resource.width > 0) {
                            viewModelScope.launch(IO) {
                                val newPath = storeImage(context, resource)
                                withContext(Main) {
                                    newPath?.let {
                                        resultImgPath = it
                                        _state.value =
                                            SketchImageActionViewState.ImageEnhanceRequestComplete(
                                                it,
                                                resource.width,
                                                resource.height
                                            )
                                    } ?: run {
                                        updateErrorForEnhance()
                                    }
                                }
                            }
                        } else {
                            _state.value =
                                SketchImageActionViewState.ImageEnhanceRequestComplete(resultPath)
                        }
                        // You can notify or take action here when the image is preloaded
                        return false // Return false to allow Glide to load the image normally
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        Log.d(
                            "ENHNACEIMG",
                            "Image preload failed for $resultPath"
                        )
                        viewModelScope.launch(Main) {
                            updateErrorForEnhance()
                        }
                        return false // Return false to allow Glide to handle the error
                    }
                }).preload()
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
        } catch (e: Exception) {
            Log.e("error", "storeImage: ", e)
        }
        return null
    }


    private suspend fun getCurrentRamUsageMb(context: Context): Long? {
        try {
            return withContext(IO) {
                val activityManager =
                    context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val memoryInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                return@withContext (memoryInfo.availMem) / 1024L / 1024L
            }
        } catch (ex: Exception) {
            return null
        }
    }

    private fun updateErrorForEnhance() {
        viewModelScope.launch(Main) {
            _state.value = SketchImageActionViewState.Error("Error while Sketching Image")
            runCatching {
                ramMonitoringJob?.cancel()

            }
        }
    }

    private fun updateCounterAndViewState(newCounter: Int) {

        val percentage = newCounter.times(100).div(maxCounter)
        viewModelScope.launch(Main) {
            _state.value =
                SketchImageActionViewState.UpdateProgress(
                    percentage,
                    "Processing your masterpiece..."
                )
        }
    }

    fun resetFrameState() {
        _state.value = SketchImageActionViewState.Idle
    }

    fun resetSaveState() {
        _saveState.value = SketchSaveViewState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        saveIntent?.cancel()
        imageEnhancementJob?.cancel()
        savingJob?.cancel()
        authKeyJob?.cancel()
        ramMonitoringJob?.cancel()
    }


}