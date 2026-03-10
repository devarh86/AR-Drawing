package com.fahad.newtruelovebyfahad.ui.fragments.learning

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.Constants.showCategoriesFrameClickAd
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.databinding.FragmentLearningFramesBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.common.CategoriesRVAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.drawing.DrawingFramesFragmentDirections
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.DrawingFramesRV
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.ConstantsCommon.learningFramesSubData
import com.project.common.utils.enums.MainMenuBlendOptions
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LearningFramesFragment : Fragment() {

    private var _binding: FragmentLearningFramesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private var categoryTagsAdapter: CategoriesRVAdapter? = null

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

        option = "Learning"

        event = when (option) {

            MainMenuBlendOptions.LEARNING.title -> {
                Events.ParamsValues.HomeScreen.LEARNING
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
            Log.d("DrawingFramesFragment", "onCreate: ${frameBody.baseUrl + frameBody.thumb}")

            if (::mActivity.isInitialized && mActivity is MainActivity) {
                (mActivity as MainActivity).frameClick(
                    FrameObject(
                        frameBody.id,
                        frameBody.title,
                        "learning",
                        framesAdapter?.categoryName ?: "",
                        event,
                        frameBody.tags ?: "",
                        frameBody.baseUrl ?: "",
                        frameBody.thumb,
                        frameBody.thumbtype,
                        true,
                        true,
                        frameBody,
                        "list"
                    )
                ) {
                    if (position != -1) framesAdapter?.notifyItemChanged(
                        position
                    )
                }
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
        _binding = FragmentLearningFramesBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    private fun FragmentLearningFramesBinding.initViews() {
        categoriesFramesSubData?.clear()
        initObservers()
        initRecyclerViews()


        backPress.setSingleClickListener {
            kotlin.runCatching {
                navController.navigateUp()
            }
        }
    }

    private fun FragmentLearningFramesBinding.initObservers() {
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
                                                    MainMenuOptions.LEARN.title.lowercase() -> {
                                                        it.children?.forEach {
                                                            it?.apply {
                                                                frames?.let {
                                                                    learningFramesSubData?.set(
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

                                            MainMenuOptions.LEARN.title.lowercase() -> {
                                                learningFramesSubData?.forEach { (key, value) ->
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
                                                    learningFramesSubData?.keys?.toList()
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

    private fun FragmentLearningFramesBinding.initRecyclerViews() {
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
            MainMenuBlendOptions.LEARNING.title -> true
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}