package com.fahad.newtruelovebyfahad.ui.fragments.drawing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.databinding.FragmentDrawingFramesBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.common.CategoriesRVAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.home.HomeForYouFragmentDirections
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.DrawingFramesRV
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.drawingFramesSubData
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.enums.MainMenuBlendOptions
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DrawingFramesFragment : Fragment() {

    private var _binding: FragmentDrawingFramesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private var categoryTagsAdapter: CategoriesRVAdapter? = null
    private var downloadDialog: BottomSheetDialog? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var framesAdapter: DrawingFramesRV? = null
    private var categoriesFramesSubData: LinkedHashMap<String, List<DrawingFramesRV.FrameModel>>? = linkedMapOf()
    private var nativeAd: NativeAd? = null

    private var event = ""
    var option: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()

        option = "Drawing"

        event = when (option) {

            MainMenuBlendOptions.DRAWING.title -> {
                Events.ParamsValues.HomeScreen.DRAWING
            }

            else -> {
                Events.ParamsValues.HomeScreen.DOUBLE_EXPOSURE
            }
        }

        categoryTagsAdapter = CategoriesRVAdapter(emptyList()) { tag, position ->
            _binding?.framesRv?.scrollToPosition(0)
            _binding?.categoryTagsRv?.scrollToPosition(position)
            categoriesFramesSubData?.get(tag)?.let {
                if (it.isNotEmpty()) {
                    categoryTagsAdapter?.select()
                    if (_binding?.framesRv?.isComputingLayout != true) {
                        framesAdapter?.clearData()
                    }
                    framesAdapter?.updateDataList(it)
                    framesAdapter?.categoryName = tag.lowercase()
                } else {
                    categoryTagsAdapter?.unselect()
                }
            } ?: run {
                categoryTagsAdapter?.unselect()
            }
        }

        framesAdapter = DrawingFramesRV(mContext, arrayListOf(), nativeAd, onClick = { frameBody, position ->
            Log.d("DrawingFramesFragment", "onCreate: ${frameBody.baseUrl+frameBody.thumb}")

            kotlin.runCatching {
                navController.navigate(
                    DrawingFramesFragmentDirections.actionDrawingFramesFragmentToHowToDrawFragment(
                        frameBody.baseUrl+frameBody.thumb
                    )
                )
            }

        }, onFavouriteClick = {
            apiViewModel.favourite(
                FavouriteModel(
                    isFavourite = it.isFavourite, frame = it.frame
                )
            )
        }, onPurchaseTypeTagClick = {})
    }

    fun hideScreenAds() {
        if (isProVersion()) {
            framesAdapter?.hideRvAd()
        }
    }

    fun showScreenAds() {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDrawingFramesBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    private fun FragmentDrawingFramesBinding.initViews() {
        categoriesFramesSubData?.clear()
        initObservers()
        initRecyclerViews()


        backPress.setSingleClickListener {
            kotlin.runCatching {
                navController.navigateUp()
            }
        }
    }

    private fun FragmentDrawingFramesBinding.initObservers() {
        try {
            var isCompleted = true
            ConstantsCommon.updateInternetStatusFrames.observe(
                viewLifecycleOwner
            ) {
                checkInternet()
                if (it == true) {
                    try {
                        apiViewModel.mainScreen.observe(viewLifecycleOwner) {
                            when (it) {
                                is Response.Loading -> {
                                    tryNowPlaceholder.gone()
                                    noResultFoundTv.gone()
                                    loadingView.startShimmer()
                                    if (ConstantsCommon.isNetworkAvailable) loadingView.visible()
                                    framesRv.invisible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {
                                    categoriesFramesSubData?.let {
                                        if (it.isNotEmpty()) {
                                            loadingView.gone()
                                            loadingView.stopShimmer()
                                            if (ConstantsCommon.isNetworkAvailable) framesRv.visible()
                                            return@observe
                                        }
                                    }
                                    if (isCompleted) {
                                        isCompleted = false
                                        it.data?.childCategories?.let { mainMenuOptions ->
                                            mainMenuOptions.filterNotNull().forEach {
                                                when (it.title.lowercase()) {
                                                    MainMenuOptions.DRAWING.title.lowercase() -> {
                                                        it.children?.forEach {
                                                            it?.apply {
                                                                frames?.let {
                                                                    drawingFramesSubData?.set(
                                                                        title, it
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        when (option?.lowercase()) {

                                            MainMenuOptions.DRAWING.title.lowercase() -> {
                                                drawingFramesSubData?.forEach { (key, value) ->
                                                    if (_binding?.framesRv?.isComputingLayout != true) {
                                                        framesAdapter?.clearData()
                                                    }
                                                    lifecycleScope.launch(Dispatchers.IO) {
                                                        val frames = value.filterNotNull().map { frame ->
                                                            DrawingFramesRV.FrameModel(frame)
                                                        }
                                                        frames.forEach { frame ->
                                                            Log.d(
                                                                "FAHAD", "initObservers: SUCCESS --- ${frame.frame}"
                                                            )
                                                            frame.isFavourite = withContext(Dispatchers.Default) {
                                                                favouriteFrames.mapNotNull { it?.id }.contains(
                                                                    FavouriteTypeConverter.fromJson(
                                                                        FavouriteTypeConverter.toJson(
                                                                            frame.frame
                                                                        )
                                                                    )?.id
                                                                )
                                                            }
                                                            if (key.lowercase() == "all") {
                                                                withContext(Dispatchers.Main) {
                                                                    categoryTagsRv.visible()
                                                                    loadingView.gone()
                                                                    loadingView.stopShimmer()
                                                                    if (ConstantsCommon.isNetworkAvailable) framesRv.visible()
                                                                    framesAdapter?.updateSingleItem(
                                                                        frame
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        categoriesFramesSubData?.put(
                                                            key, frames
                                                        )
                                                    }.invokeOnCompletion { isCompleted = true }
                                                }
                                                categoryTagsAdapter?.updateDataList(
                                                    drawingFramesSubData?.keys?.toList()
                                                )
                                            }

                                            else -> {

                                            }
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    Log.d("FAHAD", "initObservers: ERROR")
                                    if (!mActivity.isNetworkAvailable()) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
                                        noResultFoundTv.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        framesRv.invisible()
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun FragmentDrawingFramesBinding.initRecyclerViews() {
        categoryTagsRv.adapter = categoryTagsAdapter
        framesRv.adapter = framesAdapter
    }

    private fun checkInternet() {
        _binding?.apply {
            if (!mActivity.isNetworkAvailable()) {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
                loadingView.stopShimmer()
                loadingView.gone()
                framesRv.invisible()
                categoryTagsRv.invisible()
            } else {
                tryNowPlaceholder.gone()
                noResultFoundTv.gone()
                loadingView.stopShimmer()
                loadingView.gone()
                framesRv.visible()
                if (!whichCategory()) categoryTagsRv.visible()
            }
        }
    }

    private fun whichCategory(): Boolean {
        return when (option) {
            MainMenuBlendOptions.DRAWING.title -> true
            else -> false
        }
    }

    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            if (backDecision) {
                navController.navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadDialog?.apply { if (isShowing) dismiss() }
        _binding = null
    }

}