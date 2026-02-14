package com.project.common.repository

import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.graphics.values
import com.example.analytics.Constants.firebaseAnalytics
import com.project.common.R
import com.project.common.data_source.MyDatabase
import com.project.common.data_source.retrofit.BgRemoverRetrofitInterface
import com.project.common.data_source.retrofit.EnhancerRetrofitInstance
import com.project.common.data_source.retrofit.SketchRetrofitInterface
import com.project.common.model.ImageResponseModel
import com.project.common.model.ImageResponseModelColorize
import com.project.common.model.ImagesModel
import com.project.common.model.TokenResponse
import com.project.common.states.BackgroundState
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.FileUtils
import com.project.common.utils.getBitmapWithGlideCache
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Keep
class EditorRepository(
    private val db: MyDatabase,
    private val enhancerRetrofitInstance: EnhancerRetrofitInstance,
    private val bgRemoverRetrofitInstance: BgRemoverRetrofitInterface,
    private val sketchRetrofitInterface: SketchRetrofitInterface,
    @ApplicationContext private val context: Context,
) {

    fun getFrameData(
        id: Long,
    ) = flow {
        emit(db.getYourDao().readParentFrame(id))
    }

    fun getImagesAndStickersData(id: Long) = flow {
        emit(db.getYourDao().readImages(id).sortedBy { it.imagePosition })
//        emit(db.getYourDao().readStickerImage(id))
//        emit(db.getYourDao().readStickerText(id))
    }

    fun getImageStickersData(id: Long) = flow {
//        emit(db.getYourDao().readImages(id).sortedBy { it.imagePosition })
        emit(db.getYourDao().readStickerImage(id))
//        emit(db.getYourDao().readStickerText(id))
    }

    fun getTextStickersData(id: Long) = flow {
//        emit(db.getYourDao().readImages(id).sortedBy { it.imagePosition })
//        emit(db.getYourDao().readStickerImage(id))
        emit(db.getYourDao().readStickerText(id))
    }

    fun getMaskCount(id: Long): Int {
        return try {
            db.getYourDao().readImages(id).size
        } catch (ex: Exception) {
            0
        }
    }

    private var retryEnhance = 0
    private var retrySketch = 0
    private var currentSeconds = 0L

    fun resetValues() {
        retryEnhance = 0
        retrySketch = 0
        currentSeconds = 0L
    }

    suspend fun applyEnhancement(
        packageName: String,
        key: String,
        imgPathFile: File,
        updateToken: (String) -> Unit,
        successCallback: (String?) -> Unit,
        errorCallback: () -> Unit,
        loading: () -> Unit,
    ) {
        var isLoading = true
        try {
            coroutineScope {
                // Launch loading loop within current scope
                val loadingJob = launch {
                    while (isLoading) {
                        delay(if (currentSeconds >= 5000) 2500 else 1000)
                        currentSeconds += 1000
                        runCatching { loading() }.onFailure {
                            isLoading = false
                        }
                    }
                }

                val loadingTime = System.currentTimeMillis()

                val tokenToUse = if (key.isBlank() || retryEnhance == 1) {
                    suspendCancellableCoroutine<String?> { cont ->
                        getToken(packageName) { token ->
                            cont.resume(token, null)
                        }
                    }?.also {
                        updateToken(it)
                    } ?: run {
                        isLoading = false
                        errorCallback()
                        return@coroutineScope
                    }
                } else key

                val result = suspendCancellableCoroutine { cont ->
                    getEnhanceImage(loadingTime, imgPathFile, tokenToUse) { imagePath ->
                        cont.resume(imagePath, null)
                    }
                }

                isLoading = false
                loadingJob.cancel()

                if (result != null) {
                    retryEnhance = 0
                    successCallback(result)
                } else {
                    if (retryEnhance == 0) {
                        retryEnhance++
                        Log.i(aiEnhancerLogger, "enhancing main retry")

                        // retry inside the same scope
                        applyEnhancement(
                            packageName,
                            key,
                            imgPathFile,
                            updateToken,
                            successCallback,
                            errorCallback,
                            loading
                        )
                    } else {
                        retryEnhance = 0
                        errorCallback()
                    }
                }
            }
        } catch (e: Exception) {
            Log.i(aiEnhancerLogger, "enhancing exception main: ${e.message}")
            isLoading = false
            errorCallback()
        }
    }


    private val aiEnhancerLogger = "Ai_Enhancer"

    private fun getToken(packageName: String, getTokenCallback: (String?) -> Unit) {

        kotlin.runCatching {
            Log.i(aiEnhancerLogger, "getToken: requesting")

            "multipart/form-data".toMediaTypeOrNull()?.let { mediaType ->
                val appPackage =
                    packageName.toRequestBody(
                        mediaType
                    )
                enhancerRetrofitInstance.getTokenForEnhancer(
                    appPackage
                ).enqueue(object : retrofit2.Callback<TokenResponse> {
                    override fun onResponse(
                        call: Call<TokenResponse>,
                        response: Response<TokenResponse>,
                    ) {
                        if (response.isSuccessful && response.code() == 200) {

                            Log.i(aiEnhancerLogger, "getToken: response success $response")

                            response.body()?.token?.let { token ->
                                getTokenCallback.invoke(token)
                            } ?: run {
                                getTokenCallback(null)
                            }
                        } else {
                            Log.i(aiEnhancerLogger, "getToken: response not success $response")
                            getTokenCallback(null)
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        kotlin.runCatching {
                            Log.i(aiEnhancerLogger, "getToken: response failure ${t.message}")
                        }
                    }
                })
            }
        }.onFailure {
            Log.i(aiEnhancerLogger, "getToken: exception ${it.message}")
        }
    }

    private fun getEnhanceImage(
        loadingTime: Long,
        imgPathFile: File,
        token: String,
        callback: (String?) -> Unit,
    ) {
        try {
            Log.i(aiEnhancerLogger, "getEnhanceImage: start")
            val mediaType = "multipart/form-data".toMediaTypeOrNull()
            mediaType?.let {
                val requestFile = imgPathFile.asRequestBody(it)

                val contentType = "text/plain".toMediaTypeOrNull()
                contentType?.let {

                    val body = MultipartBody.Part.createFormData(
                        "image",
                        imgPathFile.name,
                        requestFile
                    )
                    enhancerRetrofitInstance.uploadImageAndGetEnhanceImage(
                        token,
                        body,
                    ).enqueue(object : retrofit2.Callback<ImageResponseModel> {
                        override fun onResponse(
                            call: Call<ImageResponseModel>,
                            response: Response<ImageResponseModel>,
                        ) {
                            if (response.isSuccessful && response.code() == 200) {
                                kotlin.runCatching {
                                    val newTime =
                                        abs(System.currentTimeMillis() - loadingTime)
                                    val hours =
                                        TimeUnit.MILLISECONDS.toHours(newTime)
                                    val minutes =
                                        TimeUnit.MILLISECONDS.toMinutes(newTime) % 60
                                    val seconds =
                                        TimeUnit.MILLISECONDS.toSeconds(newTime) % 60
                                    val formattedTime = String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d:%02d",
                                        hours,
                                        minutes,
                                        seconds
                                    )

                                    Log.i(
                                        aiEnhancerLogger,
                                        "getEnhanceImage: enhanceTime: $formattedTime"
                                    )

                                    val bundleData = Bundle().apply {
                                        putString("time", formattedTime)
                                    }
                                    firebaseAnalytics?.logEvent(
                                        "enhance_time",
                                        bundleData
                                    )
                                }

                                Log.i(aiEnhancerLogger, "getEnhanceImage: success $response")

                                callback(response.body()?.url)
                            } else {

                                Log.i(aiEnhancerLogger, "getEnhanceImage: failed $response")

                                callback(null)
                            }
                        }

                        override fun onFailure(
                            call: Call<ImageResponseModel>,
                            t: Throwable,
                        ) {
                            Log.i(aiEnhancerLogger, "getEnhanceImage: failed ${t.message}")
                            callback(null)
                        }
                    })
                }
            }

        } catch (ex: Exception) {
            Log.d("ENHANCE_IMAGE", "Exception occurred: ${ex.localizedMessage}", ex)
            callback(null)
            // null
        }

    }


    private suspend fun getBgRemoverToken(
        context: Context,
        callback: (String?) -> Unit,
    ) {

        try {

            if (ConstantsCommon.TOKEN.isNotBlank()) {
                callback(ConstantsCommon.TOKEN)
                return
            }

            withContext(IO) {

                val mediaType = "multipart/form-data".toMediaTypeOrNull()
                mediaType?.let {
                    val contentType = "text/plain".toMediaTypeOrNull()
                    contentType?.let {
                        val appPackage =
                            context.applicationContext.packageName.toRequestBody(it)
                        bgRemoverRetrofitInstance.getTokenForBgRemover(
                            appPackage
                        ).enqueue(object : retrofit2.Callback<TokenResponse> {
                            override fun onResponse(
                                call: Call<TokenResponse>,
                                response: Response<TokenResponse>,
                            ) {

                                if (response.isSuccessful && response.code() == 200) {

                                    response.body()?.token?.let { token ->
                                        Log.d("REMOVE_IMAGE", "LET")
                                        ConstantsCommon.TOKEN = token
                                        callback(token)
                                    } ?: run {
                                        callback(null)
                                    }

                                } else {
                                    callback(null)
                                }
                            }

                            override fun onFailure(
                                call: Call<TokenResponse>,
                                t: Throwable,
                            ) {
                                callback(null)
                            }
                        })
                    }
                }
            }
        } catch (ex: Exception) {
            callback(null)
        }
    }

    private var retry = 0
    var isLoading = true
    suspend fun getBgRemovedImage(
        context: Context,
        imgPathFile: File,
        callback: (String?) -> Unit,
        loading: () -> Unit,
    ) {
        try {

            getBgRemoverToken(context) { result ->
                if (result == null) {
                    isLoading = false
                    callback(null)
                } else {
                    isLoading = true

                    CoroutineScope(IO).launch(IO) newContext@{

                        launch(IO) {
                            while (isLoading) {
                                kotlin.runCatching {
                                    delay(1000)
                                    loading.invoke()
                                }.onFailure {
                                    isLoading = false
                                }
                            }
                        }

                        val mediaType = "multipart/form-data".toMediaTypeOrNull()
                        mediaType?.let {
                            val requestFile = imgPathFile.asRequestBody(it)

                            val contentType = "text/plain".toMediaTypeOrNull()
                            contentType?.let {

                                val body = MultipartBody.Part.createFormData(
                                    "image",
                                    imgPathFile.name,
                                    requestFile
                                )
                                bgRemoverRetrofitInstance.uploadImageAndGetRemoveBgImage(
                                    ConstantsCommon.TOKEN,
                                    body,
                                ).enqueue(object :
                                    retrofit2.Callback<ImageResponseModel> {
                                    override fun onResponse(
                                        call: Call<ImageResponseModel>,
                                        response: Response<ImageResponseModel>,
                                    ) {
                                        if (response.isSuccessful && response.code() == 200) {

                                            retry = 0

                                            isLoading = false

                                            callback(response.body()?.url)

                                        } else if (response.code() == 401) {

                                            ConstantsCommon.TOKEN = ""

                                            isLoading = false

                                            retry += 1

                                            if (retry <= 1) {
                                                CoroutineScope(IO).launch {
                                                    getBgRemovedImage(
                                                        context,
                                                        imgPathFile,
                                                        callback,
                                                        loading
                                                    )
                                                }
                                            } else {
                                                retry = 0
                                                callback(null)
                                            }
                                        } else {

                                            retry = 0

                                            isLoading = false
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<ImageResponseModel>,
                                        t: Throwable,
                                    ) {

                                        retry = 0

                                        isLoading = false

                                        callback(null)
                                    }
                                })
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            isLoading = false
            retry = 0
            callback(null)
        }
    }

    private var counter = 0

    private fun getBitmapFromResource(
        path: String,
    ): Bitmap? {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            try {
                inJustDecodeBounds = true
                BitmapFactory.decodeFile(path, this)

                // Calculate inSampleSize
                calculateInSampleSize(this)?.let {
                    inSampleSize = it
                } ?: {
                    null
                }

                // Decode bitmap with inSampleSize set
                inJustDecodeBounds = false

                BitmapFactory.decodeFile(path, this)
            } catch (ex: java.lang.Exception) {
                null
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
    ): Int? {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height <= 0 || width <= 0) return null

        if (height > 600 || width > 600) {

            if (isLowOnMemory(context))
                return null

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= 600 && halfWidth / inSampleSize >= 600) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun getBitmapResolution(imageFile: File): Pair<Int, Int>? {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(imageFile.absolutePath, options)

            return if (options.outWidth > 0 && options.outHeight > 0) {
                Pair(options.outWidth, options.outHeight)
            } else {
                null // Failed to decode dimensions
            }
        } catch (ex: java.lang.Exception) {
            return null
        }
    }


    private fun logMessage(message: String) {
        Log.i("editorRepository", "logMessage: $message")
    }

    suspend fun processAndSaveImagesStitchHorizontal(
        height: Int,
        spacing: Int,
        waterMarkAsset: Int,
        isProVersion: Boolean,
        imageEnhancedPath: MutableList<ImagesModel>,
        colorMatrix: ColorMatrix,
        action: (String) -> Unit, failure: (String) -> Unit, updateProgress: (Int, String) -> Unit,
    ) {
        withContext(IO) {

            kotlin.runCatching parentCatching@{

                val imageEnhancedPathCopy = mutableListOf<ImagesModel>()

                imageEnhancedPathCopy.addAll(imageEnhancedPath)

                logMessage("success copy initial images list")

                logMessage("initial images list loop start")

                var finalWidth = 0

                val limitHeight = 0.7f * height

                logMessage("image limited height $limitHeight")

                imageEnhancedPathCopy.forEachIndexed { index, imagesModels ->

                    logMessage("image $index process start")

                    if (isLowOnMemory(context)) {

                        logMessage("image $index ram check memory low")

                        failure.invoke("Please close some background apps and try again")

                        return@withContext
                    }

                    logMessage("image $index ram check success")

                    val imagePath = File(imagesModels.croppedPath)

                    if (imagePath.exists()) {

                        logMessage("image $index path exist")

                        logMessage("image $index get bitmap resolution")

                        getBitmapResolution(imagePath)?.let {

                            logMessage("image $index success image resolution original ${it.first}x${it.second}")

                            val afterScalingWidthAndHeight =
                                calculateAspectRatioFit(
                                    imagesModels.imageWidth,
                                    imagesModels.imageHeight
                                )

                            if (afterScalingWidthAndHeight == null) {
                                logMessage("image $index after scaling is null")
                                failure.invoke("failed to save image")
                                return@withContext
                            }

                            logMessage("image $index success image resolution after scaling ${afterScalingWidthAndHeight.first}x${afterScalingWidthAndHeight.second}")

                            val scaleMatrix =
                                afterScalingWidthAndHeight.scaleImageWithRespectToHeight(limitHeight)

                            val scaleMatrixValues = scaleMatrix.values()

                            if (scaleMatrixValues.isEmpty() || scaleMatrixValues.size <= Matrix.MSCALE_X) {
                                logMessage("image $index matrix is empty")
                                failure.invoke("failed to save image")
                                return@withContext
                            }

                            finalWidth += ((afterScalingWidthAndHeight.first * scaleMatrixValues[Matrix.MSCALE_X]).roundToInt() + if (index == imageEnhancedPathCopy.size - 1) 0 else spacing)

                            logMessage("image $index final width $finalWidth")

                            if (imageEnhancedPathCopy.size == index + 1) {
                                logMessage("finalizing fetching canvas width")
                            }

                        } ?: kotlin.run {
                            logMessage("image $index failed bitmap resolution")
                            failure.invoke("failed to save image")
                            return@withContext
                        }

                    } else {
                        failure.invoke("image not found")
                        return@withContext
                    }
                }

                logMessage("------------")

                var counter = 0

                if (finalWidth != 0 && limitHeight != 0f) {

                    val filterPaint = Paint().apply {
                        colorFilter = ColorMatrixColorFilter(colorMatrix)
                    }

                    logMessage("again images list loop start")

                    logMessage("canvas width and height ${finalWidth}x${height}")

                    val estimatedSizeForCanvas =
                        finalWidth * height * 4

                    logMessage("ram checking $estimatedSizeForCanvas")

                    if (canAllocate(estimatedSizeForCanvas)) {

                        logMessage("ram check successfully")

                        val canvasBitmap =
                            Bitmap.createBitmap(finalWidth, height, Bitmap.Config.ARGB_8888)

                        val canvas = Canvas(canvasBitmap)

                        canvas.save()

                        var x = 0f

                        var y = 0.15f * height

                        imageEnhancedPathCopy.forEachIndexed { index, imagesModels ->

                            logMessage("images $index process start")

                            val afterScalingWidthAndHeight =
                                calculateAspectRatioFit(
                                    imagesModels.imageWidth,
                                    imagesModels.imageHeight
                                )

                            if (afterScalingWidthAndHeight == null) {
                                logMessage("image $index after scaling is null")
                                failure.invoke("failed to save image")
                                return@withContext
                            }

                            logMessage("image $index success image resolution after scaling ${afterScalingWidthAndHeight.first}x${afterScalingWidthAndHeight.second}")

                            val estimatedSize =
                                afterScalingWidthAndHeight.first * afterScalingWidthAndHeight.second * 4

                            logMessage("image $index check ram for bitmap creation size $estimatedSize")

                            if (canAllocate(estimatedSize)) {
                                logMessage("image $index success ram for bitmap creation size")

                                logMessage("image $index create bitmap")

                                val bitmap = getBitmapFromResource(imagesModels.croppedPath)

                                bitmap?.let {

                                    logMessage("image $index finally success creating bitmap size ${it.width}x${it.height}")

                                    val scaleMatrix =
                                        Pair(it.width, it.height).scaleImageWithRespectToHeight(
                                            limitHeight
                                        )

                                    canvas.translate(x, y)

                                    y = 0f

                                    canvas.drawBitmap(it, scaleMatrix, filterPaint)

                                    val scaleMatrixValues = scaleMatrix.values()

                                    if (scaleMatrixValues.isEmpty() || scaleMatrixValues.size <= Matrix.MSCALE_X) {
                                        logMessage("image $index matrix is empty")
                                        failure.invoke("failed to save image")
                                        return@withContext
                                    }

                                    x = (scaleMatrixValues[Matrix.MSCALE_X] * it.width) + spacing

                                    if (!it.isRecycled) {
                                        it.recycle()
                                    }

                                    if (index <= (imageEnhancedPathCopy.size / 2)) {

                                        counter += 1

                                        val progress = (counter * 100) / imageEnhancedPathCopy.size

                                        logMessage("update progress $progress")

                                        updateProgress.invoke(progress, "Enhancing your image")
                                    }

                                } ?: run {
                                    logMessage("image $index failure creating bitmap")
                                    failure.invoke("Failed to save image")
                                    return@withContext
                                }

                            } else {
                                logMessage("image $index failed ram for bitmap creation size")
                                failure.invoke("Please close some background apps and try again")
                                return@withContext
                            }
                        }

                        logMessage("checking water mark check")

                        val finalBitmap = if (!isProVersion) {

                            logMessage("create water mark")

                            canvas.restore()

//                            val bitmapWaterMark =
//                                suspendCancellableCoroutine { continuation ->
//                                    context.getBitmapWithGlideCache(waterMarkAsset) { bitmap ->
//                                        if (bitmap != null) {
//                                            continuation.resume(bitmap)
//                                        } else {
//                                            continuation.resume(null)
//                                        }
//                                    }
//                                }
//                            bitmapWaterMark?.let {
//                                val xWatermark = if (canvasBitmap.width == canvasBitmap.height)
//                                    ((canvasBitmap.width.div(2)) - it.width.div(
//                                        2
//                                    )).toFloat()
//                                else
//                                    (canvasBitmap.width - it.width).toFloat()
//                                val yWatermark =
//                                    ((canvasBitmap.height - (0.15 * canvasBitmap.height)) - it.height).toFloat()
//                                canvas.drawBitmap(bitmapWaterMark, xWatermark, yWatermark, null)
//                            }

                            logMessage("success adding water mark")

                            canvasBitmap
                        } else {
                            logMessage("skip adding water mark")
                            canvasBitmap
                        }
                        withContext(Main) {
                            withContext(IO) {
                                kotlin.runCatching {
                                    logMessage("saving image")

                                    // Simulate progress in parallel
                                    val totalSteps = 50
                                    val delayPerStep = 2000L
                                    val progressJob = launch {
                                        for (i in 1..totalSteps) {

                                            if (counter <= (imageEnhancedPathCopy.size)) {

                                                counter += 1

                                                val progress =
                                                    (counter * 100) / imageEnhancedPathCopy.size

                                                logMessage("update progress $progress")

                                                updateProgress.invoke(progress, "Saving")
                                            } else break

                                            delay(delayPerStep)
                                        }
                                    }

                                    finalBitmap.saveMediaToStorage(context) { path ->

                                        path?.let {

                                            updateProgress(100, "Complete")

                                            val filePath = File(it)

                                            logMessage("save result $it and is it exist ${filePath.exists()}")

                                            if (filePath.exists()) {
                                                action.invoke(it)
                                            } else {
                                                failure.invoke("failed to save image")
                                            }
                                        } ?: kotlin.run {

                                            logMessage("error occur while saving")

                                            failure.invoke("failed to save image")
                                        }

                                        progressJob.cancel()

                                        logMessage("clearing canvas bitmap")

                                        if (!finalBitmap.isRecycled) {
                                            finalBitmap.recycle()
                                        }
                                    }
                                }.onFailure {

                                    logMessage("clearing canvas bitmap")

                                    if (!finalBitmap.isRecycled) {
                                        finalBitmap.recycle()
                                    }

                                    logMessage("exception while saving ${it.message}")

                                    failure.invoke("failed to save image")
                                }
                            }
                        }
                    } else {
                        logMessage("failed ram for canvas bitmap creation size")
                        failure.invoke("Please close some background apps and try again")
                        return@withContext
                    }
                } else {
                    logMessage("final width $finalWidth or $limitHeight is zero")
                    failure.invoke("failed to save image")
                    return@withContext
                }
            }.onFailure {
                logMessage("crash in parent catching $it")
                failure.invoke("failed to save image")
                return@onFailure
            }
        }
    }

    suspend fun processAndSaveImagesStitchVertical(
        width: Int,
        height: Int,
        spacing: Int,
        waterMarkAsset: Int,
        isProVersion: Boolean,
        imageEnhancedPath: MutableList<ImagesModel>,
        colorMatrix: ColorMatrix,
        action: (String) -> Unit, failure: (String) -> Unit, updateProgress: (Int, String) -> Unit,
    ) {
        withContext(IO) {
            kotlin.runCatching parentCatching@{
                val imageEnhancedPathCopy = mutableListOf<ImagesModel>()

                imageEnhancedPathCopy.addAll(imageEnhancedPath)

                logMessage("success copy initial images list")

                logMessage("initial images list loop start")

                var finalHeight = 0

                val limitWidth = width.toFloat()

                logMessage("image limited height $limitWidth")

                imageEnhancedPathCopy.forEachIndexed { index, imagesModels ->

                    logMessage("image $index process start")

                    if (isLowOnMemory(context)) {

                        logMessage("image $index ram check memory low")

                        failure.invoke("Please close some background apps and try again")

                        return@withContext
                    }

                    logMessage("image $index ram check success")

                    val imagePath = File(imagesModels.croppedPath)

                    if (imagePath.exists()) {

                        logMessage("image $index path exist")

                        logMessage("image $index get bitmap resolution")

                        getBitmapResolution(imagePath)?.let {

                            logMessage("image $index success image resolution original ${it.first}x${it.second}")

                            val afterScalingWidthAndHeight =
                                calculateAspectRatioFit(
                                    imagesModels.imageWidth,
                                    imagesModels.imageHeight
                                )

                            if (afterScalingWidthAndHeight == null) {
                                logMessage("image $index after scaling is null")
                                failure.invoke("failed to save image")
                                return@withContext
                            }

                            logMessage("image $index success image resolution after scaling ${afterScalingWidthAndHeight.first}x${afterScalingWidthAndHeight.second}")

                            val scaleMatrix =
                                afterScalingWidthAndHeight.scaleImageWithRespectToWidth(limitWidth)

                            val scaleMatrixValues = scaleMatrix.values()

                            if (scaleMatrixValues.isEmpty() || scaleMatrixValues.size <= Matrix.MSCALE_Y) {
                                logMessage("image $index matrix is empty")
                                failure.invoke("failed to save image")
                                return@withContext
                            }

                            finalHeight += ((afterScalingWidthAndHeight.second * scaleMatrixValues[Matrix.MSCALE_Y]).roundToInt() + if (index == imageEnhancedPathCopy.size - 1) 0 else spacing)

                            logMessage("image $index final height $finalHeight")

                            if (imageEnhancedPathCopy.size == index + 1) {
                                logMessage("finalizing fetching canvas height")
                            }

                        } ?: kotlin.run {
                            logMessage("image $index failed bitmap resolution")
                            failure.invoke("failed to save image")
                            return@withContext
                        }

                    } else {
                        failure.invoke("image not found")
                        return@withContext
                    }
                }

                logMessage("------------")

                var counter = 0

                if (finalHeight != 0 && limitWidth != 0f) {

                    val filterPaint = Paint().apply {
                        colorFilter = ColorMatrixColorFilter(colorMatrix)
                    }

                    logMessage("again images list loop start")

                    logMessage("canvas width and height ${width}x${finalHeight}")

                    val estimatedSizeForCanvas =
                        width * finalHeight * 4

                    logMessage("ram checking $estimatedSizeForCanvas")

                    if (canAllocate(estimatedSizeForCanvas)) {

                        logMessage("ram check successfully")

                        val canvasBitmap =
                            Bitmap.createBitmap(width, finalHeight, Bitmap.Config.ARGB_8888)

                        val canvas = Canvas(canvasBitmap)

                        canvas.save()

                        var x = 0f

                        var y = 0f

                        imageEnhancedPathCopy.forEachIndexed { index, imagesModels ->

                            logMessage("images $index process start")

                            val afterScalingWidthAndHeight =
                                calculateAspectRatioFit(
                                    imagesModels.imageWidth,
                                    imagesModels.imageHeight
                                )

                            if (afterScalingWidthAndHeight == null) {
                                logMessage("image $index after scaling is null")
                                failure.invoke("failed to save image")
                                return@withContext
                            }

                            logMessage("image $index success image resolution after scaling ${afterScalingWidthAndHeight.first}x${afterScalingWidthAndHeight.second}")

                            val estimatedSize =
                                afterScalingWidthAndHeight.first * afterScalingWidthAndHeight.second * 4

                            logMessage("image $index check ram for bitmap creation size $estimatedSize")

                            if (canAllocate(estimatedSize)) {
                                logMessage("image $index success ram for bitmap creation size")

                                logMessage("image $index create bitmap")

                                val bitmap = getBitmapFromResource(imagesModels.croppedPath)

                                bitmap?.let {

                                    logMessage("image $index finally success creating bitmap size ${it.width}x${it.height}")

                                    val scaleMatrix =
                                        Pair(it.width, it.height).scaleImageWithRespectToWidth(
                                            limitWidth
                                        )

                                    canvas.translate(x, y)

                                    x = 0f

                                    canvas.drawBitmap(it, scaleMatrix, filterPaint)

                                    if (!it.isRecycled) {
                                        it.recycle()
                                    }

                                    val scaleMatrixValues = scaleMatrix.values()

                                    if (scaleMatrixValues.isEmpty() || scaleMatrixValues.size <= Matrix.MSCALE_Y) {
                                        logMessage("image $index matrix is empty")
                                        failure.invoke("failed to save image")
                                        return@withContext
                                    }

                                    y = (scaleMatrixValues[Matrix.MSCALE_Y] * it.height) + spacing

                                    if (index <= (imageEnhancedPathCopy.size / 2)) {

                                        counter += 1

                                        val progress = (counter * 100) / imageEnhancedPathCopy.size

                                        logMessage("update progress $progress")

                                        updateProgress.invoke(progress, "Enhancing your image")
                                    }

                                } ?: run {
                                    logMessage("image $index failure creating bitmap")
                                    failure.invoke("Failed to save image")
                                    return@withContext
                                }

                            } else {
                                logMessage("image $index failed ram for bitmap creation size")
                                failure.invoke("Please close some background apps and try again")
                                return@withContext
                            }
                        }

                        logMessage("checking water mark check")

                        val finalBitmap = if (!isProVersion) {

                            logMessage("create water mark")

                            canvas.restore()

                            val bitmapWaterMark =
                                suspendCancellableCoroutine { continuation ->
                                    context.getBitmapWithGlideCache(waterMarkAsset) { bitmap ->
                                        if (bitmap != null) {
                                            continuation.resume(bitmap)
                                        } else {
                                            continuation.resume(null)
                                        }
                                    }
                                }
                            bitmapWaterMark?.let {
                                val xWatermark = if (canvasBitmap.width == canvasBitmap.height)
                                    ((canvasBitmap.width.div(2)) - it.width.div(
                                        2
                                    )).toFloat()
                                else
                                    (canvasBitmap.width - it.width).toFloat()
                                val yWatermark =
                                    (canvasBitmap.height - it.height).toFloat()
                                canvas.drawBitmap(bitmapWaterMark, xWatermark, yWatermark, null)
                            }

                            logMessage("success adding water mark")

                            canvasBitmap
                        } else {
                            logMessage("skip adding water mark")
                            canvasBitmap
                        }
                        withContext(Main) {
                            withContext(IO) {
                                kotlin.runCatching {

                                    logMessage("saving image")

                                    // Simulate progress in parallel
                                    val totalSteps = 100
                                    val delayPerStep = 2000L // total 500ms simulation
                                    val progressJob = launch {
                                        for (i in 1..totalSteps) {

                                            if (counter <= (imageEnhancedPathCopy.size)) {

                                                counter += 1

                                                val progress =
                                                    (counter * 100) / imageEnhancedPathCopy.size

                                                logMessage("update progress $progress")

                                                updateProgress.invoke(progress, "Saving")
                                            }
                                            delay(delayPerStep)
                                        }
                                    }

                                    finalBitmap.saveMediaToStorage(context) { path ->
                                        path?.let {

                                            val filePath = File(it)

                                            updateProgress(100, "Complete")

                                            logMessage("save result $it and is it exist ${filePath.exists()}")

                                            if (filePath.exists()) {
                                                action.invoke(it)
                                            } else {
                                                failure.invoke("failed to save image")
                                            }
                                        } ?: kotlin.run {

                                            logMessage("error occur while saving")

                                            failure.invoke("failed to save image")
                                        }

                                        progressJob.cancel() // stop simulated progress

                                        logMessage("clearing canvas bitmap")

                                        if (!finalBitmap.isRecycled) {
                                            finalBitmap.recycle()
                                        }
                                    }

                                }.onFailure {

                                    logMessage("clearing canvas bitmap")

                                    if (!finalBitmap.isRecycled) {
                                        finalBitmap.recycle()
                                    }

                                    logMessage("exception while saving ${it.message}")

                                    failure.invoke("failed to save image")
                                }
                            }
                        }
                    } else {
                        logMessage("failed ram for canvas bitmap creation size")
                        failure.invoke("Please close some background apps and try again")
                        return@withContext
                    }
                } else {
                    logMessage("final height $finalHeight or $limitWidth is zero")
                    failure.invoke("failed to save image")
                    return@withContext
                }
            }.onFailure {
                logMessage("crash in parent catching $it")
                failure.invoke("failed to save image")
                return@onFailure
            }
        }
    }

    private fun Pair<Int, Int>.scaleImageWithRespectToHeight(limitHeight: Float): Matrix {

        val originalWidth = first.toFloat()
        val originalHeight = second.toFloat()

        val scaleFactor = limitHeight / originalHeight

        val matrix = Matrix()
        matrix.setScale(scaleFactor, scaleFactor)
        return matrix
    }

    fun Pair<Int, Int>.scaleImageWithRespectToWidth(targetWidth: Float): Matrix {
        val (originalWidth, originalHeight) = this

        val scale = targetWidth / originalWidth.toFloat()
        val matrix = Matrix()
        matrix.setScale(scale, scale)

        return matrix
    }

    private fun calculateAspectRatioFit(
        originalWidth: Int,
        originalHeight: Int,
    ): Pair<Int, Int>? {

        if (originalWidth == 0 || originalHeight == 0) return null

        val widthRatio = 600f / originalWidth
        val heightRatio = 600f / originalHeight
        val scale = minOf(widthRatio, heightRatio)

        val scaledWidth = (originalWidth * scale).toInt()
        val scaledHeight = (originalHeight * scale).toInt()
        return Pair(scaledWidth, scaledHeight)
    }

    suspend fun processAndSaveImages(
        width: Int,
        height: Int,
        waterMarkAsset: Int,
        isProVersion: Boolean,
        imageEnhancedPath: MutableList<ImagesModel>,
        backgroundState: BackgroundState,
        imagesZoomLevel: Map<Int, Float>, action: (List<String>) -> Unit, updateProgress: (Int) -> Unit,
    ) {

        return withContext(IO) {

            Log.d("ThreadCheck", "runfun thread: ${Thread.currentThread().name}")

            counter = 0

            val savedImagePaths = mutableListOf<String>()

            try {
                val backgroundBitmap = getBackgroundBitmap(context, backgroundState, width, height)
                // Get background dimensions
                val backgroundWidth = backgroundBitmap.width
                val backgroundHeight = backgroundBitmap.height

                counter += 1
                updateProgress.invoke(counter)

                //  for (foregroundImage in imageEnhancedPath) {
                // Loop through ALL images in the list
                imageEnhancedPath.forEachIndexed { index, foregroundImage ->

//                    if (!isActive) break
//
//                    // Check memory
//                    if (isLowOnMemory(context)) {
//                        continue
//                    }
                    if (!isActive) {
                        Log.w("ImageProcessor", "Job cancelled at image $index")
                        return@forEachIndexed
                    }

                    if (isLowOnMemory(context)) {
                        Log.w("ImageProcessor", "Low memory, skipping image $index")
                        return@forEachIndexed
                    }
                    val zoomLevel = imagesZoomLevel[index] ?: 100f
                    val resultBitmap = combineImages(
                        backgroundBitmap,
                        foregroundImage,

                        backgroundWidth,
                        backgroundHeight,
                        zoomLevel,
                    )

                    if (resultBitmap != null) {

                        val finalBitmap = if (!isProVersion) {

                            val canvas = Canvas(resultBitmap)

//                            val bitmapWaterMark =
//                                suspendCancellableCoroutine { continuation ->
//                                    context.getBitmapWithGlideCache(waterMarkAsset) { bitmap ->
//                                        if (bitmap != null) {
//                                            continuation.resume(bitmap)
//                                        } else {
//                                            continuation.resume(null)
//                                        }
//                                    }
//                                }
//                            bitmapWaterMark?.let {
//                                val x = if (resultBitmap.width == resultBitmap.height)
//                                    ((resultBitmap.width.div(2)) - it.width.div(
//                                        2
//                                    )).toFloat()
//                                else
//                                    (resultBitmap.width - it.width).toFloat()
//                                val y = (resultBitmap.height - it.height).toFloat()
//                                canvas.drawBitmap(bitmapWaterMark, x, y, null)
//                            }
                            resultBitmap
                        } else {
                            resultBitmap
                        }

                        Log.i("saving", "processAndSaveImages: before")

                        // Save the processed image
                        finalBitmap.saveMediaToStorage(context) {
                            it?.let {
                                savedImagePaths.add(it)
                            }

                            if (!finalBitmap.isRecycled)
                                finalBitmap.recycle()

                            Log.i("saving", "processAndSaveImages: after")

                            counter += 1
                            updateProgress.invoke(counter)
                        }
                    }
                }
                // }

                if (!backgroundBitmap.isRecycled)
                    backgroundBitmap.recycle()

                action.invoke(savedImagePaths)

            } catch (e: Exception) {
                Log.e("ImageProcessor.TAG", "Error processing and saving images", e)
                action.invoke(emptyList())
            }
        }
    }

    fun canAllocate(requiredBytes: Int): Boolean {
//        kotlin.runCatching {
        val runtime = Runtime.getRuntime()
        val usedMem = runtime.totalMemory() - runtime.freeMemory()
        val maxMem = runtime.maxMemory()
        val available = maxMem - usedMem

        // Add a 10MB safety margin
        val safetyBuffer = 10 * 1024 * 1024
        return requiredBytes < (available - safetyBuffer)
//        }.onFailure {
//            return  isLowOnMemory(context)
//        }
    }

    private fun isLowOnMemory(context: Context): Boolean {
        try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            return memoryInfo.lowMemory || memoryInfo.availMem < 100 * 1024 * 1024 // e.g., 100 MB
        } catch (ex: java.lang.Exception) {
            return false
        }
    }

    /**
     * Combines background and foreground images with applied edits
     */
    private fun combineImages(
        backgroundBitmap: Bitmap,
        foregroundImage: ImagesModel,
        canvasWidth: Int,
        canvasHeight: Int,
        zoomLevel: Float,
    ): Bitmap? {
        try {
            // Create a new bitmap with the same dimensions as the background
            val resultBitmap =
                Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resultBitmap)

            // Draw background first
            canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)

            // Load and process foreground image
            val foregroundBitmap = getBitmapFromPath(foregroundImage.croppedPath)

            if (foregroundBitmap != null) {
                // Apply color matrix if available
                val paint = Paint()
                foregroundImage.colorMatrix?.let {
                    paint.colorFilter = ColorMatrixColorFilter(it)
                }

                // Handle different foreground transformations
                // For simplicity, we're using the fit center matrix here, but this could be
                // extended to support different positioning modes based on user preferences
                val matrix = calculateFitCenterMatrixWithZoom(
                    canvasWidth,
                    canvasHeight,
                    foregroundBitmap.width,
                    foregroundBitmap.height,
                    zoomLevel
                )

                // Apply any alpha/brightness adjustments if needed
                foregroundImage.colorMatrix?.let { matrix ->
                    // The color matrix already contains alpha/brightness adjustments
                    // as seen in the adjustAlpha and adjustBrightness methods in your adapter
                    paint.colorFilter = ColorMatrixColorFilter(matrix)
                }

                // Draw the foreground image with transformations
                canvas.drawBitmap(foregroundBitmap, matrix, paint)

                // Apply any additional transformations or effects here if needed
                // For example, borders, shadows, etc.

                // Clean up
                if (!foregroundBitmap.isRecycled)
                    foregroundBitmap.recycle()
            } else {
                Log.e(
                    "ImageProcessor.TAG",
                    "Failed to load foreground image: ${foregroundImage.croppedPath}"
                )
            }

            return resultBitmap

        } catch (e: Exception) {
            Log.e("ImageProcessor.TAG", "Error combining images", e)
            return null
        }
    }

    /**
     * Gets a bitmap from the specified path
     */
    private fun getBitmapFromPath(path: String?): Bitmap? {
        if (path == null) return null

        return try {
            getBitmapFromResource(path)
        } catch (e: Exception) {
            Log.e("ImageProcessor.TAG", "Error loading bitmap from path: $path", e)
            null
        }
    }

    /**
     * Gets the background bitmap based on the current background state
     */
    private fun getBackgroundBitmap(
        context: Context,
        backgroundState: BackgroundState,
        width: Int,
        height: Int,
    ): Bitmap {

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val backgroundPaint = Paint()

        when (val state = backgroundState) {
            is BackgroundState.Color -> {
                // Draw solid color background
                backgroundPaint.color = state.color
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            }

            is BackgroundState.Transparent -> {
                val drawable = ContextCompat.getDrawable(context, R.drawable.transparent_bg)
                drawable?.let { d ->
                    d.setBounds(0, 0, width, height)
                    d.draw(canvas)
                } ?: run {
                    // Fallback if drawable is missing
                    Log.e(
                        "ImageProcessor.TAG",
                        "getBackgroundBitmap: transparent_bg drawable not found"
                    )
                    canvas.drawColor(Color.WHITE)
                }
            }

            is BackgroundState.Gradient -> {
                val gradientColors = state.gradientList
                if (gradientColors.isNotEmpty()) {
                    val gradient = LinearGradient(
                        0f, 0f, 0f, height.toFloat(),
                        gradientColors.toIntArray(), null, Shader.TileMode.CLAMP
                    )
                    backgroundPaint.shader = gradient
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
                } else {
                    canvas.drawColor(Color.LTGRAY)
                }
            }

            is BackgroundState.Image -> {
                val imageBitmap = state.images?.croppedPath?.let { getBitmapFromPath(it) }
                imageBitmap?.let { imgBitmap ->
                    canvas.drawBitmap(
                        imgBitmap,
                        calculateFitXYMatrix(width, height, imageBitmap.width, imageBitmap.height),
                        null // Use default paint settings
                    )
                    if (!imageBitmap.isRecycled)
                        imgBitmap.recycle()
                } ?: run {
                    canvas.drawColor(Color.WHITE)
                    Log.e(
                        "ImageProcessor.TAG",
                        "getBackgroundBitmap: Failed to load image from path"
                    )
                }
            }

            is BackgroundState.ApiBG -> {
                // Load and draw API background image
                val baseUrl = state.images?.baseUrl
                val file = state.images?.file

                if (baseUrl != null && file != null) {
                    try {
                        // Attempt to load from cache or download
                        val imagePath = getCachedImagePath(context, baseUrl, file)
                        val imageBitmap = getBitmapFromPath(imagePath)

                        imageBitmap?.let { imgBitmap ->
                            canvas.drawBitmap(
                                imgBitmap,
                                calculateFitXYMatrix(
                                    width,
                                    height,
                                    imgBitmap.width,
                                    imgBitmap.height
                                ),
                                null
                            )
                            if (!imageBitmap.isRecycled)
                                imgBitmap.recycle()
                        } ?: run {
                            // Fallback if loading fails
                            canvas.drawColor(Color.WHITE)
                            Log.e(
                                "ImageProcessor.TAG",
                                "getBackgroundBitmap: Failed to load API background"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "ImageProcessor.TAG",
                            "getBackgroundBitmap: Error processing ApiBG",
                            e
                        )
                        canvas.drawColor(Color.WHITE)
                    }
                } else {
                    // Fallback if URL info is missing
                    canvas.drawColor(Color.WHITE)
                    Log.e(
                        "ImageProcessor.TAG",
                        "getBackgroundBitmap: Missing API background URL info"
                    )
                }
            }

            is BackgroundState.EyeDropper -> {
                // Draw background with picked color
                backgroundPaint.color = state.color
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            }

            BackgroundState.None -> {
                // Default white background
                canvas.drawColor(
                    ContextCompat.getColor(
                        context,
                        R.color.whiteBg
                    )
                )
                Log.d("ImageProcessor.TAG", "getBackgroundBitmap: Using default white background")
            }
        }

        return bitmap
    }

    private fun calculateFitXYMatrix(
        viewWidth: Int,
        viewHeight: Int,
        imageWidth: Int,
        imageHeight: Int,
    ): Matrix {
        val matrix = Matrix()

        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()

        matrix.setScale(scaleX, scaleY)
        matrix.postTranslate(0f, 0f) // No translation needed

        return matrix
    }

    /**
     * Returns cached path for API images or attempts to cache them
     */
    private fun getCachedImagePath(context: Context, baseUrl: String, fileName: String): String? {
        // Check if image is already cached
        val cacheDir = context.cacheDir
        val cachedFile = File(cacheDir, "background_images/${fileName}")

        if (cachedFile.exists()) {
            return cachedFile.absolutePath
        }

        // If not cached, try to download (this might be better handled by a proper image loading library)
        try {
            // This is a simplified example - in production, use a proper image loading library
            val url = URL("$baseUrl/$fileName")
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream

                // Ensure parent directories exist
                cachedFile.parentFile?.mkdirs()

                // Save to cache
                FileOutputStream(cachedFile).use { output ->
                    inputStream.copyTo(output)
                }

                return cachedFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("ImageProcessor.TAG", "Failed to download and cache image", e)
        }

        return null
    }


    /**
     * Calculates the transformation matrix to fit an image centered within the given dimensions
     */
    private fun calculateFitCenterMatrix(
        viewWidth: Int,
        viewHeight: Int,
        imageWidth: Int,
        imageHeight: Int,
        zoomLevel: Float,
    ): Matrix {
        val matrix = Matrix()

        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()
        val scaleFactor = min(scaleX, scaleY) // Fit inside

        // Calculate the translation to center the image
        val scaledWidth = imageWidth * scaleFactor
        val scaledHeight = imageHeight * scaleFactor
        val dx = (viewWidth - scaledWidth) / 2f
        val dy = (viewHeight - scaledHeight) / 2f

        matrix.setScale(scaleFactor, scaleFactor)
        matrix.postTranslate(dx, dy)

        return matrix
    }

    private fun calculateFitCenterMatrixWithZoom(
        viewWidth: Int,
        viewHeight: Int,
        imageWidth: Int,
        imageHeight: Int,
        zoomLevel: Float = 100f
    ): Matrix {
        val matrix = Matrix()

        // Calculate base scale to fit image
        val scaleX = viewWidth.toFloat() / imageWidth.toFloat()
        val scaleY = viewHeight.toFloat() / imageHeight.toFloat()
        val baseScaleFactor = min(scaleX, scaleY)

        // Apply zoom: 100 = 1x, 150 = 1.5x, 50 = 0.5x
        val zoomFactor = zoomLevel / 100f
        val finalScaleFactor = baseScaleFactor * zoomFactor

        // Calculate scaled dimensions
        val scaledWidth = imageWidth * finalScaleFactor
        val scaledHeight = imageHeight * finalScaleFactor

        // Center the image
        val dx = (viewWidth - scaledWidth) / 2f
        val dy = (viewHeight - scaledHeight) / 2f

        // Apply transformations
        matrix.setScale(finalScaleFactor, finalScaleFactor)
        matrix.postTranslate(dx, dy)

        return matrix
    }


    /**
     * Saves a bitmap to the gallery with the specified quality
     * @return The path to the saved image
     */
    private fun saveImageToGallery(context: Context, bitmap: Bitmap): String? {
        try {
            // Create a filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "MultiStitch_$timestamp.png"

            // For Android 10+ (API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Ar Drawing"
                    )
                }

                val resolver = context.contentResolver
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    return uri.toString()
                }
            } else {
                // For older Android versions
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(imagesDir, filename)

                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // Make the file visible in the gallery
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(imageFile.toString()),
                    arrayOf("image/png"),
                    null
                )

                return imageFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("ImageProcessor.TAG", "Failed to save image to gallery", e)
        }

        return null
    }

    suspend fun Bitmap.saveMediaToStorage(context: Context, onUriCreated: (String?) -> Unit) {

        var fos: OutputStream? = null

        val filename = "${System.currentTimeMillis()}.jpg"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "Ar Drawing"
                )
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                val imageUri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let {
                    fos = Objects.requireNonNull(it).let {
                        resolver.openOutputStream(it)
                    }

                    fos?.let { compress(Bitmap.CompressFormat.PNG, 100, it) }

                    Objects.requireNonNull(fos)

                    delay(100)

                    fos?.close()

                    it.let {
                        FileUtils().getRealPathFromURI(context, it)?.let {
                            withContext(Main) {
                                onUriCreated(it)
                                withContext(IO) {
                                    try {
                                        MediaScannerConnection.scanFile(
                                            context,
                                            arrayOf<String>(it),
                                            null
                                        ) { _, _ -> }
                                    } catch (ex: java.lang.Exception) {
                                        Log.e("error", "saveMediaToStorage: ", ex)
                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                //These for devices running on android < Q
                val imagesDir =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "Ar Drawing"
                    )
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)

                fos?.use {
                    compress(Bitmap.CompressFormat.PNG, 100, it)
                    delay(100)
                    it.close()
                }
                withContext(Main) {
                    onUriCreated(image.absolutePath)
                    withContext(IO) {
                        try {
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf<String>(image.absolutePath),
                                null
                            ) { _, _ -> }
                        } catch (ex: java.lang.Exception) {
                            Log.e("error", "saveMediaToStorage: ", ex)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            onUriCreated(null)
            Log.d("error", e.toString())
        }
    }

    private val sketchLogger = "RESTORE_REPO"
    suspend fun applyImageSketch(
        packageName: String,
        key: String,
        imgPathFile: File,
        updateToken: (String) -> Unit,
        successCallback: (String?) -> Unit,
        errorCallback: () -> Unit,
        loading: () -> Unit,
    ) {
        var isLoading = true
        try {
            coroutineScope {
                // Launch loading loop within current scope
                val loadingJob = launch {
                    while (isLoading) {
                        delay(if (currentSeconds >= 5000) 2500 else 1000)
                        currentSeconds += 1000
                        runCatching { loading() }.onFailure {
                            isLoading = false
                        }
                    }
                }
                val loadingTime = System.currentTimeMillis()
                val tokenToUse = if (key.isBlank() || retrySketch == 1) {
                    suspendCancellableCoroutine<String?> { cont ->
                        getTokenSketchImage(packageName) { token ->
                            cont.resume(token, null)
                        }
                    }?.also {
                        updateToken(it)
                    } ?: run {
                        isLoading = false
                        errorCallback()
                        return@coroutineScope
                    }
                } else key
                val result = suspendCancellableCoroutine { cont ->
                    getSketchImage(loadingTime, imgPathFile, tokenToUse) { imagePath ->
                        cont.resume(imagePath, null)
                    }
                }

                isLoading = false
                loadingJob.cancel()

                if (result != null) {
                    retrySketch = 0
                    successCallback(result)
                } else {
                    if (retrySketch == 0) {
                        retrySketch++
                        Log.i(sketchLogger, "applyImageRestoration main retry")

                        // retry inside the same scope
                        applyImageSketch(
                            packageName,
                            key,
                            imgPathFile,
                            updateToken,
                            successCallback,
                            errorCallback,
                            loading
                        )
                    } else {
                        retrySketch = 0
                        errorCallback()
                    }
                }
            }
        } catch (e: Exception) {
            Log.i(sketchLogger, "applyImageRestoration exception main: ${e.message}")
            isLoading = false
            errorCallback()
        }
    }

    private fun getTokenSketchImage(packageName: String, getTokenCallback: (String?) -> Unit) {

        kotlin.runCatching {
            Log.i(sketchLogger, "getTokenRestoreImage: requesting")

            "multipart/form-data".toMediaTypeOrNull()?.let { mediaType ->
                val appPackage =
                    packageName.toRequestBody(
                        mediaType
                    )
                sketchRetrofitInterface.getTokenForSketch(
                    appPackage
                ).enqueue(object : retrofit2.Callback<TokenResponse> {
                    override fun onResponse(
                        call: Call<TokenResponse>,
                        response: Response<TokenResponse>,
                    ) {
                        if (response.isSuccessful && response.code() == 200) {

                            Log.i(sketchLogger, "getTokenRestoreImage: response success $response")

                            response.body()?.token?.let { token ->
                                getTokenCallback.invoke(token)
                            } ?: run {
                                getTokenCallback(null)
                            }
                        } else {
                            Log.i(
                                sketchLogger,
                                "getTokenRestoreImage: response not success $response"
                            )
                            getTokenCallback(null)
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        kotlin.runCatching {
                            Log.i(
                                sketchLogger,
                                "getTokenRestoreImage: response failure ${t.message}"
                            )
                        }
                    }
                })
            }
        }.onFailure {
            Log.i(sketchLogger, "getTokenRestoreImage: exception ${it.message}")
        }
    }


    private fun getSketchImage(
        loadingTime: Long,
        imgPathFile: File,
        token: String,
        callback: (String?) -> Unit,
    ) {
        try {
            Log.i(sketchLogger, "getColorizedImage: start")
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            mediaType?.let {
                val requestFile = imgPathFile.asRequestBody(it)
//                val requestFile = imgPathFile.asRequestBody(
//                    "image/*".toMediaTypeOrNull()
//                )
                val contentType = "text/plain".toMediaTypeOrNull()
                contentType?.let {

                    val body = MultipartBody.Part.createFormData(
                        "image",
                        imgPathFile.name,
                        requestFile
                    )
                    sketchRetrofitInterface.uploadImageAndGetSketchImage(
                        token,
                        body,
                    ).enqueue(object : retrofit2.Callback<ImageResponseModelColorize> {
                        override fun onResponse(
                            call: Call<ImageResponseModelColorize>,
                            response: Response<ImageResponseModelColorize>,
                        ) {
                            if (response.isSuccessful && response.code() == 200) {
                                kotlin.runCatching {
                                    val newTime =
                                        abs(System.currentTimeMillis() - loadingTime)
                                    val hours =
                                        TimeUnit.MILLISECONDS.toHours(newTime)
                                    val minutes =
                                        TimeUnit.MILLISECONDS.toMinutes(newTime) % 60
                                    val seconds =
                                        TimeUnit.MILLISECONDS.toSeconds(newTime) % 60
                                    val formattedTime = String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d:%02d",
                                        hours,
                                        minutes,
                                        seconds
                                    )

                                    Log.i(
                                        sketchLogger,
                                        "getRestoreImage: restoreTime: $formattedTime"
                                    )

                                    val bundleData = Bundle().apply {
                                        putString("time", formattedTime)
                                    }
                                    firebaseAnalytics?.logEvent(
                                        "sketch_time",
                                        bundleData
                                    )
                                }

                                Log.i(sketchLogger, "getRestoreImage: success $response")

                                callback(response.body()?.url)
                            } else {

                                Log.i(sketchLogger, "getRestoreImage: failed $response")

                                callback(null)
                            }
                        }

                        override fun onFailure(
                            call: Call<ImageResponseModelColorize>,
                            t: Throwable,
                        ) {
                            Log.i(sketchLogger, "getRestoreImage: failed ${t.message}")
                            callback(null)
                        }
                    })
                }
            }

        } catch (ex: Exception) {
            Log.i(sketchLogger, "Exception occurred: ${ex.localizedMessage}", ex)
            callback(null)
            // null
        }

    }

}