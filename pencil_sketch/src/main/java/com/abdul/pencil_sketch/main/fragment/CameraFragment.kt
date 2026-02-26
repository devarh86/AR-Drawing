package com.abdul.pencil_sketch.main.fragment

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentCameraBinding
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.utils.navigateFragment
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController

    private val sketchImageViewModel: PencilSketchViewModel by activityViewModels()
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

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
            _binding = FragmentCameraBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
        listener()
    }

    private fun listener() {
        binding.apply {

            flashIV.setOnSingleClickListener {
                if (camera?.cameraInfo?.torchState?.value == TorchState.ON) {
                    camera?.cameraControl?.enableTorch(false)
                    flashIV.setImageResource(R.drawable.ic_flash_on)
                } else {
                    camera?.cameraControl?.enableTorch(true)
                    flashIV.setImageResource(R.drawable.ic_flash_on)
                }
            }

            captureIV.setOnSingleClickListener {
                takeIt()
            }

            rotateIV.setOnSingleClickListener {
                toggleCamera()
            }

            backPress.setOnSingleClickListener {
                navController.navigateUp()
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
                    it.setSurfaceProvider(binding.surfaceView.surfaceProvider)
                }

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val useCaseGroup = UseCaseGroup.Builder()
                .setViewPort(binding.surfaceView.viewPort ?: return@addListener)
                .addUseCase(imageCapture ?: return@addListener)
                .addUseCase(preview)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                Log.d("CheckCameraTAG", "Use case binding failed b4")
                camera = cameraProvider?.bindToLifecycle(viewLifecycleOwner, camerafacing, useCaseGroup)
                Log.d("CheckCameraTAG", "Camera $camera")
            } catch (exc: Exception) {
                Log.e("CheckCameraTAG", "${exc.message}")
            }
        }, ContextCompat.getMainExecutor(mContext))
    }

    private fun toggleCamera() {
        val newCameraSelector = when (camera?.cameraInfo?.lensFacing ?: CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_BACK -> {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            CameraSelector.LENS_FACING_FRONT -> {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            else -> {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        }

        try {
            startCamera(newCameraSelector)
        } catch (exc: Exception) {
            Log.e(ContentValues.TAG, "Use case binding failed", exc)
        }
    }

    private fun takeIt() {
        captureImage()
    }

    private fun captureImage() {
        val outputDirectory = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile = File(
            outputDirectory,
            "${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(mContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val filePath = savedUri.path ?: ""


                    sketchImageViewModel.cameraPath = filePath

                    mActivity.navigateFragment(
                        CameraFragmentDirections.actionCameraFragmentToSaveAndShareFragment(),
                        R.id.cameraFragment
                    )

                }

                override fun onError(exception: ImageCaptureException) {

                }
            }
        )
    }

}