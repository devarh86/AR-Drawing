package com.project.gallery.ui.main

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import com.example.ads.Constants.galleryButtonNewFlow

import com.example.ads.Constants.showAppOpen
import com.project.common.utils.ConstantsCommon.carouselImagesCount
import com.project.common.utils.ConstantsCommon.fromSaveAndShare
import com.project.common.utils.setOnSingleClickListener
import com.project.gallery.compose_views.CreateFolderView
import com.project.gallery.compose_views.ToolBarGallery
import com.project.gallery.data.model.GalleryChildModel
import com.project.gallery.data.model.GalleryModel
import com.project.gallery.data.util.Utils
import com.project.gallery.databinding.FragmentItemListDialogListDialogBinding
import com.project.gallery.ui.adapters.GalleryAdapterForCompose
import com.project.gallery.ui.adapters.GalleryAdapterForSelectedImages
import com.project.gallery.ui.adapters.GalleryFolderAdapter
import com.project.gallery.ui.adapters.GalleryFolderAdapterNew
import com.project.gallery.ui.main.viewmodel.GalleryViewModel
import com.project.gallery.ui.main.viewstate.MainViewState
import com.project.gallery.utils.createOrShowSnackBar
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryListDialogFragment : Fragment() {

    private var fromReplace: Boolean = false

    private var newFlow: Boolean = false

    private var maxCounter: Int = 0

    private var cursor: Cursor? = null

    private var imageLoadingJob: Job? = null

    private var image_uri: Uri? = null

    private var cameraActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    private var galleryImageRecyclerAdapter: GalleryAdapterForCompose? = null

    private var gallerySelectedImageRecyclerAdapter: GalleryAdapterForSelectedImages? = null

    private var textState: MutableState<String> = mutableStateOf("All Photos")

    private var loading: MutableState<Boolean> = mutableStateOf(true)

    private var showDivider: MutableState<Boolean> = mutableStateOf(true)

    private var showTick: MutableState<Boolean> = mutableStateOf(false)

    private var forCollage: MutableState<Boolean> = mutableStateOf(false)

    private var showFolders: MutableState<Boolean> = mutableStateOf(false)
    private var totalImagesCounter: MutableState<Int> = mutableStateOf(0)

    private var fromCollage: Boolean = false
    private var fromCarousel: Boolean = false

    private var imagesList: MutableState<List<GalleryChildModel>> = mutableStateOf(emptyList())

    private var showImages: MutableState<Boolean> = mutableStateOf(false)

    private var currentPath: MutableState<String> = mutableStateOf("")

    companion object {
        var selectedFolderPos = 0
    }

//    private var galleryFolderAdapter: GalleryFolderAdapter? = null
    private var galleryFolderAdapterNew: GalleryFolderAdapterNew? = null

    private var _binding: FragmentItemListDialogListDialogBinding? = null

    private val galleryViewModel: GalleryViewModel by activityViewModels()

    private val binding get() = _binding!!

    private var myListener: OnImageSelection? = null

    interface OnImageSelection {
        fun onSelection(path: String)
        fun setRecyclerViewListener(view: RecyclerView)
        fun onNextClick()
        fun onBackClick()
    }

    private fun openCamera() {
        try {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
            image_uri =
                context?.contentResolver?.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (!forCollage.value || galleryViewModel.selectedImages.size < maxCounter) {
                kotlin.runCatching {
                    showAppOpen = false
                }
                cameraActivityResultLauncher?.launch(cameraIntent)
            } else {
                if (forCollage.value && galleryViewModel.selectedImages.size == maxCounter) {
                    activity?.let {
                        Toast.makeText(
                            it,
                            it.getString(com.project.common.R.string.maximum_images_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            fromCollage = it.getBoolean("from_collage", false)
            fromCarousel = it.getBoolean("from_carousel", false)
        }

        if (fromCollage && !fromCarousel) {
            gallerySelectedImageRecyclerAdapter = GalleryAdapterForSelectedImages(object :
                GalleryAdapterForSelectedImages.OnItemClick {
                override fun onMediaRemove(obj: GalleryChildModel, index: Int) {
                    val position = galleryViewModel.selectedImages.indexOf(obj)
                    if (position != -1) {
                        galleryImageRecyclerAdapter?.removeCounter(obj, obj.indexInAdapter)
                        galleryViewModel.selectedImages.removeAt(position)
                        setCounter(galleryViewModel.selectedImages.size)
                        gallerySelectedImageRecyclerAdapter?.removeItem(position)
                    }
                }
            })

            gallerySelectedImageRecyclerAdapter?.addList(galleryViewModel.selectedImages)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        if (_binding == null) {
            _binding = FragmentItemListDialogListDialogBinding.inflate(inflater, container, false)
            init()
            initClick()
            activity?.let { activity ->
                activity.supportLoaderManager.initLoader(
                    50,
                    null,
                    object : LoaderManager.LoaderCallbacks<Cursor> {
                        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
                            Log.i("loaderManager", "onCreateLoader: createLoader")
                            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                            val projection = arrayOf(
                                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                                MediaStore.Images.Media.BUCKET_ID,
                                MediaStore.Images.Media.DATA
                            )

                            val selection =
                                "${MediaStore.Images.Media.MIME_TYPE} LIKE 'image/%'"

                            val sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"

                            return CursorLoader(
                                activity,
                                uri,
                                projection,
                                selection,
                                null,
                                sortOrder
                            )
                        }

                        override fun onLoaderReset(loader: Loader<Cursor>) {
                            Log.i("loaderManager", "onCreateLoader: onLoaderReset")
                            imageLoadingJob?.cancel()
                        }

                        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
                            try {

                                cursor = data

                                if ((cursor?.count
                                        ?: 0) == 0 && galleryViewModel.allMediaList.size <= 1
                                ) {
                                    loading.value = false
                                    context?.createOrShowSnackBar(
                                        binding.root,
                                        0,
                                        "No Image Found",
                                        false
                                    )

                                    if (galleryViewModel.allMediaList.size == 0) {
                                        galleryViewModel.allMediaList.add(GalleryChildModel("Camera"))
                                        if (galleryViewModel.categoryName.isNotBlank() && galleryViewModel.categoryName.contains(
                                                "Bg Art",
                                                true
                                            ) || galleryViewModel.categoryName.contains("Ghibli Art", true) || galleryViewModel.categoryName.contains("Overlay", true)
                                        ) {
                                            galleryViewModel.allMediaList.add(
                                                GalleryChildModel(
                                                    path = "offline",
                                                    id = com.project.common.R.drawable.bg_art_sample
                                                )
                                            )
                                        }
                                    /*    else if (galleryViewModel.categoryName.isNotBlank() && galleryViewModel.categoryName.contains(
                                                "Object Remover",
                                                true
                                            )
                                        ) {
                                            galleryViewModel.allMediaList.add(
                                                GalleryChildModel(
                                                    path = "offline",
                                                    sample = 1,
                                                    id = com.project.common.R.drawable.sample_1
                                                )
                                            )
                                            galleryViewModel.allMediaList.add(
                                                GalleryChildModel(
                                                    path = "offline",
                                                    sample = 2,
                                                    id = com.project.common.R.drawable.sample_2
                                                )
                                            )
                                        } else if (galleryViewModel.categoryName.isNotBlank()&& galleryViewModel.categoryName.contains("gallery_enhancer")) {
                                            galleryViewModel.allMediaList.add(
                                                GalleryChildModel(
                                                    path = "offline",
                                                    id = com.project.common.R.drawable.enhancer_sample,
                                                    fromCollage = fromCollage,
                                                    selectedImageCounter = 0,
                                                )
                                            )
                                        }*/
                                        else if (galleryViewModel.categoryName.isNotBlank()) {
                                            galleryViewModel.allMediaList.add(
                                                GalleryChildModel(
                                                    path = "offline",
                                                    id = com.project.common.R.drawable.blend_sample,
                                                    fromCollage =fromCollage,
                                                    fromCarousel = if(fromCollage&&fromCarousel)true else false,
                                                    selectedImageCounter = 0,
                                                )
                                            )
                                        }
                                        val allMediaObj = GalleryModel(
                                            "",
                                            false,
                                            "All Photos",
                                            galleryViewModel.allMediaList
                                        )
                                        galleryViewModel.galleryFoldersWithImages.add(allMediaObj)
                                        galleryViewModel.imageLoadedCompleted()
                                    }
                                    cursor?.close()
                                    return
                                }

                                try {
                                    if (cursor?.isAfterLast == true) {
                                        cursor?.close()
                                        return
                                    }
                                } catch (_: Exception) {
                                    return
                                }


                                imageLoadingJob?.cancel()

                                galleryViewModel.startLoadingImages()

                                galleryViewModel.allMediaList.clear()
                                galleryViewModel.galleryFoldersWithImages.clear()

                                galleryViewModel.allMediaList.add(GalleryChildModel("Camera"))

                                if (galleryViewModel.categoryName.isNotBlank() && galleryViewModel.categoryName.contains(
                                        "Bg Art",
                                        true
                                    ) || galleryViewModel.categoryName.contains("Ghibli Art", true) || galleryViewModel.categoryName.contains("Overlay", true)
                                ){
                                    galleryViewModel.allMediaList.add(
                                        GalleryChildModel(
                                            path = "offline",
                                            id = com.project.common.R.drawable.bg_art_sample,
                                        )
                                    )
                                }
                         /*       else if (galleryViewModel.categoryName.isNotBlank() && galleryViewModel.categoryName.contains(
                                        "Object Remover",
                                        true
                                    )
                                ) {
                                    galleryViewModel.allMediaList.add(
                                        GalleryChildModel(
                                            path = "offline",
                                            sample = 1,
                                            id = com.project.common.R.drawable.sample_1
                                        )
                                    )
                                    galleryViewModel.allMediaList.add(
                                        GalleryChildModel(
                                            path = "offline",
                                            sample = 2,
                                            id = com.project.common.R.drawable.sample_2
                                        )
                                    )
                                }else if (galleryViewModel.categoryName.isNotBlank()&& galleryViewModel.categoryName.contains("gallery_enhancer")) {
                                    galleryViewModel.allMediaList.add(
                                        GalleryChildModel(
                                            path = "offline",
                                            id = com.project.common.R.drawable.enhancer_sample,
                                        )
                                    )
                                }*/
                                else if (galleryViewModel.categoryName.isNotBlank()) {
                                    galleryViewModel.allMediaList.add(
                                        GalleryChildModel(
                                            path = "offline",
                                            id = com.project.common.R.drawable.blend_sample,
                                           // id = com.project.common.R.drawable.blend_sample,
                                        )
                                    )
                                }
                                val allMediaObj =
                                    GalleryModel(
                                        "",
                                        false,
                                        "All Photos",
                                        galleryViewModel.allMediaList
                                    )
                                galleryViewModel.galleryFoldersWithImages.add(allMediaObj)

                                imageLoadingJob = lifecycleScope.launch(IO) {
                                    val videoFolders = mutableListOf<Long>()
                                    data?.let {
                                        it.let {
                                            try {
                                                val folderNameColumnIndex =
                                                    it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                                                val bucketId =
                                                    it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                                                val uriIndex =
                                                    it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                                                while (it.moveToNext()) {
                                                    if (!isActive) {
                                                        return@launch
                                                    }
                                                    try {
                                                        var folderName =
                                                            it.getString(folderNameColumnIndex)
                                                                ?: "0"
                                                        val folderId = it.getLong(bucketId)
                                                        val folderCoverUri = it.getString(uriIndex)

                                                        val filePath = File(folderCoverUri)

                                                        val parentPath = filePath?.parent

                                                        if (!videoFolders.contains(folderId)) {
                                                            if (folderName == "0") {
                                                                folderName = "Internal Storage"
                                                            }
                                                            parentPath?.let { path ->
                                                                folderName.let { folderName ->
                                                                    val galleryList: MutableList<GalleryChildModel> =
                                                                        mutableListOf()
                                                                    videoFolders.add(folderId)
                                                                    val galleryModel = GalleryModel(
                                                                        "folder",
                                                                        false,
                                                                        folderName,
                                                                        galleryList,
                                                                        false,
                                                                    )

                                                                    galleryViewModel.updateFolder(
                                                                        galleryModel
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        filePath?.let {
                                                            parentPath?.let { folderPath ->
                                                                val folderIndex =
                                                                    videoFolders.indexOf(folderId)
                                                                galleryViewModel.updateImage(
                                                                    GalleryChildModel(
                                                                        it.absolutePath,
                                                                        false,
                                                                        folderIndex,
                                                                        fromCollage = fromCollage,
                                                                        fromCarousel = if(fromCollage&&fromCarousel)true else false,
                                                                        selectedImageCounter = 0,
                                                                    )
                                                                )
                                                                // fromCollage = fromCollage,
                                                            }
                                                        }
                                                    } catch (ex: Exception) {
                                                        Log.i(
                                                            "getAllVideoFolders",
                                                            "getAllVideoFolders: $it"
                                                        )
                                                    }
                                                }
                                                cursor?.close()
                                                withContext(Main) {
                                                    if (!isActive) {
                                                        return@withContext
                                                    }
                                                    galleryViewModel.imageLoadedCompleted()
                                                }
                                            } catch (ex: java.lang.Exception) {
                                                Log.e("error", "onLoadFinished: ", ex)
                                            }
                                        }
                                    }
                                }
                            } catch (_: Exception) {
                                return
                            }
                        }
                    })

            }
            if (galleryButtonNewFlow && fromCollage&& !fromCarousel) {
                _binding?.let {
                    it.deleteImageImgNewFlow.isVisible = true
                    it.deleteImageImg.isVisible = false
                    it.nextNewFlow.isVisible = true
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainView.postOnAnimation {
            observeData()
        }
    }

    private fun setCounter(counter: Int) {
        kotlin.runCatching {
            if (counter < 1 && fromReplace) {
                showTick.value = false
                if(galleryButtonNewFlow){
                    binding.nextNewFlow.alpha = 0.5f
                }
            } else if (counter < 2 && !fromReplace) {
                showTick.value = false
                if(galleryButtonNewFlow){
                    binding.nextNewFlow.alpha = 0.5f
                }
            } else {
                showTick.value = true
                if(galleryButtonNewFlow) {
                    binding.nextNewFlow.alpha = 1f
                }
            }
            binding.headingTxt.text =
                context?.let {
                    ContextCompat.getString(
                        it,
                        if (fromReplace) com.project.common.R.string.select_1_1_photos else com.project.common.R.string.select_2_9_photos
                    ) + " ($counter)"
                }
        }
    }

    fun setListener(listener: OnImageSelection) {
        myListener = listener
    }

    private fun init() {

        arguments?.let {
            showDivider.value = it.getBoolean("showDivider", true)
            forCollage.value = fromCollage
        }

        fromReplace = arguments?.getBoolean("fromReplace", true) ?: false

        newFlow = arguments?.getBoolean("new_flow", false) ?: false

        if (fromCollage&& !fromCarousel) {
            maxCounter = if (fromReplace) 1 else 9
        }else if(fromCarousel &&fromCollage ){
            maxCounter =  arguments?.getInt("max_count",0)?:0
            Log.i("CURRENTFLO", "init in GALLERY MAX COUNT : $maxCounter")
            totalImagesCounter.value = maxCounter
             /*   if (fromReplace){
                galleryViewModel.selectedImages.clear()
                carouselImagesCount = 1
                1
            }else arguments?.getInt("max_count",0)?:0*/
            Log.i("COUNTERIMG","GAlleryList--maxCounter--$maxCounter")
        }

        showTick.value = fromReplace || fromSaveAndShare

        fromSaveAndShare = false

        if (fromCollage && gallerySelectedImageRecyclerAdapter != null && !fromCarousel) {
            binding.recyclerView.adapter = gallerySelectedImageRecyclerAdapter
            binding.bottomLayoutForSelection.visibility = View.VISIBLE
            setCounter(galleryViewModel.selectedImages.size)

            binding.deleteImageImg.setOnSingleClickListener {
                galleryViewModel.selectedImages.forEach {
                    galleryImageRecyclerAdapter?.removeCounter(it, it.indexInAdapter)
                }

                galleryViewModel.selectedImages.clear()
                gallerySelectedImageRecyclerAdapter?.addList(galleryViewModel.selectedImages)
                setCounter(0)
            }

            if(galleryButtonNewFlow) {
                binding.deleteImageImgNewFlow.setOnSingleClickListener {
                    galleryViewModel.selectedImages.forEach {
                        galleryImageRecyclerAdapter?.removeCounter(it, it.indexInAdapter)
                    }

                    galleryViewModel.selectedImages.clear()
                    gallerySelectedImageRecyclerAdapter?.addList(galleryViewModel.selectedImages)
                    setCounter(0)
                }

                binding.nextNewFlow.setOnSingleClickListener {
                    if (showTick.value) {
                        myListener?.onNextClick()
                    }
                }
            }
        }

        cameraActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    val filePath = image_uri?.let { it1 ->
                        context?.let { it2 ->
                            Utils().getRealPathFromURI(
                                it2,
                                it1
                            )?.let { File(it) }
                        }
                    }

                    filePath?.absolutePath?.let { it1 ->
                        if (!fromCollage) {
                            myListener?.onSelection(it1)
                            if (newFlow) {
                                galleryImageRecyclerAdapter?.resetOldSelection()
                            }
                        }
                        kotlin.runCatching {
                            lifecycleScope.launch(Main) {
                                val obj = GalleryChildModel(it1)
                                galleryViewModel.selectedImages.add(obj)
                                setCounter(galleryViewModel.selectedImages.size)
                                gallerySelectedImageRecyclerAdapter?.addItem(obj)
                            }
                        }
                    }

                    if (selectedFolderPos == 0) {
                        galleryImageRecyclerAdapter?.addList(galleryViewModel.allMediaList)
                    }
                } catch (ex: java.lang.Exception) {
                    Log.e("error", "onCreateView: ", ex)
                }
            } else {
                try {
                    image_uri?.let { orphanUri ->
                        activity?.contentResolver?.delete(orphanUri, null, null)
                    }
                } catch (_: Exception) {
                }
            }
        }

        binding.toolbar.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = viewLifecycleOwner)
            )
            setContent {
                if ((fromCollage || newFlow)&& !fromCarousel) {
                    Log.d("DILISTFRAG", "init: in  if condition ")
                    ToolBarGallery().CreateToolBarCollage(
                        textState,
                        loading = loading,
                        showFolders = showFolders,
                        showPrimaryTick = showTick,
                        fromCollage = fromCollage,
                        alignStart = newFlow
                    ) {
                        when (it) {
                            "folder" -> showFolders.value = !showFolders.value
                            "tick" -> {
                                myListener?.onNextClick()
                            }

                            else -> {
                                myListener?.onBackClick()
                            }
                        }
                    }
                } else {
                    Log.d("DILISTFRAG", "init: in  else condition ")
                    ToolBarGallery().CreateToolBar(
                        textState,
                        loading = loading,
                        showDivider = showDivider,
                        showFolders,
                        showTick,
                        totalImagesCounter
                    ) {
                        when (it) {
                            "folder" -> showFolders.value = !showFolders.value
                            "tick" -> {
                                if (showTick.value) {
                                    myListener?.onNextClick()
                                }
                            }

                            else -> {
                                myListener?.onBackClick()
                            }
                        }
                    }
                }
            }
        }

        binding.mainView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = viewLifecycleOwner)
            )
            setContent {
                if (galleryImageRecyclerAdapter == null) {
                    galleryImageRecyclerAdapter =
                        GalleryAdapterForCompose(object : GalleryAdapterForCompose.OnItemClick {

                            override fun onLimitReached() {
                                activity?.let {
                                    Toast.makeText(
                                        it,
                                        context.getString(com.project.common.R.string.maximum_images_selected),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onMediaRemove(obj: GalleryChildModel, index: Int) {

                                val position = galleryViewModel.selectedImages.indexOf(obj)

                                if (position != -1) {
                                    galleryViewModel.selectedImages.removeAt(position)
                                    setCounter(galleryViewModel.selectedImages.size)
                                    gallerySelectedImageRecyclerAdapter?.removeItem(position)
                                }
                            }

                            override fun onMediaClick(obj: GalleryChildModel): Int {

                                if (!obj.fromCollage) {
//                                    if (obj.path == "offline") {
//                                        if (obj.sample == 1) {
//                                            orSample = 1
//                                        } else if (obj.sample == 2) {
//                                            orSample = 2
//                                        }
//                                        myListener?.onSelection("offline")
//                                    } else {
                                        myListener?.onSelection(obj.path)
                                        galleryViewModel.selectedImages.add(obj)
//                                    }

                                } else {
//                                    if(!obj.fromCarousel){
                                    Log.i("CURRENTFLO", "onMediaClick: selected size--${galleryViewModel.selectedImages.size} ")
                                        if (galleryViewModel.selectedImages.size < maxCounter) {
                                            myListener?.onSelection(obj.path)
                                            galleryViewModel.selectedImages.add(obj)
                                            setCounter(galleryViewModel.selectedImages.size)
                                            gallerySelectedImageRecyclerAdapter?.addItem(obj)
                                            if (galleryViewModel.selectedImages.size >= 4) {
                                                _binding?.recyclerView?.smoothScrollToPosition(
                                                    galleryViewModel.selectedImages.size - 1
                                                )
                                            }
                                            return galleryViewModel.selectedImages.size - 1
                                        }
                                   /* }else if(obj.fromCollage && obj.fromCarousel){
                                        if (galleryViewModel.selectedImages.size < carouselImagesCount) {
                                      //  if (galleryViewModel.selectedImages.size < maxCounter) {
                                            myListener?.onSelection(obj.path)
                                            galleryViewModel.selectedImages.add(obj)
                                            setCounter(galleryViewModel.selectedImages.size)
                                            gallerySelectedImageRecyclerAdapter?.addItem(obj)
                                            if (galleryViewModel.selectedImages.size >= 4) {
                                                _binding?.recyclerView?.smoothScrollToPosition(
                                                    galleryViewModel.selectedImages.size - 1
                                                )
                                            }
                                            return galleryViewModel.selectedImages.size - 1
                                        }
                                    }*/

                                }

                                return galleryViewModel.selectedImages.size
                            }

                            override fun onLongPress(path: String) {
                                currentPath.value = path
                                showImages.value = path.isNotEmpty()
                            }

                            override fun onCameraClick() {
                                openCamera()

//                                if (checkSelfPermission(
//                                        context,
//                                        Manifest.permission.CAMERA
//                                    ) == PackageManager.PERMISSION_DENIED
//                                ) {
//                                    val permission = arrayOf(
//                                        Manifest.permission.CAMERA,
//                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                                    )
//                                    requestPermissions(permission, 112)
//                                } else {
//                                    openCamera()
//                                }
                            }
                        })
                    galleryImageRecyclerAdapter?.maxCounter = maxCounter
                    galleryImageRecyclerAdapter?.newFlow = newFlow
                }

                galleryImageRecyclerAdapter?.let { galleryAdapterForCompose ->
                    ToolBarGallery().CreateGalleryView(
                        galleryAdapterForCompose,
                        imagesList,
                        galleryViewModel.selectedImagesState,
                        showImages,
                        currentPath
                    ) {
                        myListener?.setRecyclerViewListener(it)
                    }
                }

                if (galleryViewModel.galleryFoldersWithImages.isNotEmpty() && selectedFolderPos >= 0 && galleryViewModel.galleryFoldersWithImages.size > selectedFolderPos && galleryViewModel.state.value == MainViewState.Idle) {
                    galleryImageRecyclerAdapter?.addList(galleryViewModel.galleryFoldersWithImages[selectedFolderPos].folderImagesVideoPaths)
                    textState.value =
                        galleryViewModel.galleryFoldersWithImages[selectedFolderPos].folderName.toString()
                }
            }
        }
        binding.folderView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = viewLifecycleOwner)
            )
            setContent {
                if (galleryFolderAdapterNew == null) {
                    galleryFolderAdapterNew =
                        GalleryFolderAdapterNew(object : GalleryFolderAdapterNew.OnFolderClick {
                            override fun folderClick(
                                position: Int,
                                folderImages: MutableList<GalleryChildModel>,
                                folderName: String,
                            ) {
                                selectedFolderPos = position
                                galleryImageRecyclerAdapter?.addList(folderImages)
                                textState.value = folderName
                                galleryFolderAdapterNew?.notifyDataSetChanged()

                               // showFolders.value = false
                            }
                        })
                }

                galleryFolderAdapterNew?.let {
                    //CreateFolderView(folderAdapter = it, showFolders)
                    showFolders.value = true
                    CreateFolderView(folderAdapter = it,showFolders)

                    if (galleryViewModel.state.value == MainViewState.Idle)
                        it.addList(galleryViewModel.galleryFoldersWithImages)
                }
            }
        }
    }

    private fun initClick() {

    }

    private fun observeData() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                galleryViewModel.state.observe(viewLifecycleOwner) { mainViewState ->
                    when (mainViewState) {

                        is MainViewState.Loading -> {
                            loading.value = true
                        }

                        is MainViewState.Error -> {
                            Log.d("FAHAD", "observeData: ")
                        }

                        is MainViewState.Idle -> {
                            loading.value = false
                        }

                        is MainViewState.Success -> {

                            if (galleryViewModel.galleryFoldersWithImages.isNotEmpty()) {

                                if (selectedFolderPos == 0) {
                                    if (galleryViewModel.galleryFoldersWithImages[0].folderImagesVideoPaths.size != galleryImageRecyclerAdapter?.myList?.size) {
                                        galleryImageRecyclerAdapter?.addList(galleryViewModel.galleryFoldersWithImages[0].folderImagesVideoPaths)
                                    }
                                }

                                galleryFolderAdapterNew?.addList(galleryViewModel.galleryFoldersWithImages)
                            }
                            loading.value = false

                            galleryViewModel.resetGalleryState()
                        }

                        is MainViewState.UpdateFolder -> {
                            galleryFolderAdapterNew?.let {
//                                if (mainViewState.obj.) {
                                val obj = mainViewState.obj
                                it.myList.add(obj)
                                val position = it.myList.size
                                it.notifyItemInserted(position)
//                                }
                            }
                        }

                        is MainViewState.UpdateImage -> {
                            try {
                                if (selectedFolderPos == mainViewState.obj.parentIndex + 1 || selectedFolderPos == 0) {
                                    galleryImageRecyclerAdapter?.let {
                                        if (galleryViewModel.galleryFoldersWithImages.isNotEmpty() && galleryViewModel.galleryFoldersWithImages[0].folderImagesVideoPaths.isNotEmpty()) {
                                            val obj =
                                                galleryViewModel.galleryFoldersWithImages[0].folderImagesVideoPaths.last()
                                            if (it.myList.isEmpty()) {
                                                if (obj.path == "Camera") {
                                                    it.myList.add(obj)
                                                    val position = it.myList.size
                                                    it.notifyItemInserted(position)
                                                }
                                            } else {
                                                if (obj.path != "Camera") {
                                                    it.myList.add(obj)
                                                    val position = it.myList.size
                                                    it.notifyItemInserted(position)
                                                }
                                            }
                                        }
                                    }
                                }
                                galleryFolderAdapterNew?.notifyItemChanged(0)
                            } catch (ex: Exception) {
                                Log.e("error", "observeData: ", ex)
                            }
                        }

                        is MainViewState.UpdateTickIcon -> {
                            showTick.value = mainViewState.showPrimaryTick
                            galleryViewModel.resetGalleryState()
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (_binding != null) {
            binding.toolbar.disposeComposition()
            binding.mainView.disposeComposition()
            binding.folderView.disposeComposition()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isDetached && isVisible) {
            binding.toolbar.disposeComposition()
            binding.mainView.disposeComposition()
            binding.folderView.disposeComposition()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            imageLoadingJob?.cancel()
            activity?.supportLoaderManager?.getLoader<Cursor>(50)?.stopLoading()
        } catch (ex: java.lang.Exception) {
            Log.e("error", "onDestroy: ", ex)
        }
        _binding = null
        myListener = null
    }
}