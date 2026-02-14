package com.fahad.newtruelovebyfahad.ui.fragments.template

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.ads.Constants.showTemplateFrameClickAd
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.GetMainScreenQuery
import com.fahad.newtruelovebyfahad.databinding.FragmentTemplatesBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FramesRV
import com.fahad.newtruelovebyfahad.ui.fragments.styles.adapter.CategoriesListRVAdapter
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.enums.MainMenuOptions
import com.project.common.utils.getEventCategoryName
import com.project.common.utils.getProScreen
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class TemplatesFragment : Fragment() {
    private var _binding: FragmentTemplatesBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    var categoriesFramesSubData: LinkedHashMap<String, List<FramesRV.FrameModel>>? = linkedMapOf()
    private var callback: OnBackPressedCallback? = null
    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var framesAdapter: FramesRV? = null
    private val args by navArgs<TemplatesFragmentArgs>()
    private val apiViewModel by activityViewModels<ApiViewModel>()



    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            if (backDecision) {
                setFragmentResult(
                    "show_home_screen_template",
                    bundleOf("show_home_screen_template" to true)
                )
                navController?.navigateUp()
            }
        }
    }

    private fun onBackPress() {

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                kotlin.runCatching {
                    activity?.let {
                        if (it is MainActivity) {
                            it.hideCollapsibleBanner = true
                        }
                    }
                    navController?.navigateUp()
                }
            }
        }
        callback?.let {
            activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback?.remove()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        parentScreen = args.mainScreenMenu

        framesAdapter = FramesRV(
            mContext,
            arrayListOf(),
            null,
            onClick = { frameBody, position ->
//Toast.makeText(requireContext(), "${frameBody.id} ${frameBody.title}", Toast.LENGTH_SHORT).show()
                if (mActivity != null && mActivity is MainActivity) {
                    (mActivity as MainActivity).frameClick(
                        FrameObject(
                            frameBody.id,
                            frameBody.title,
                            Events.Screens.TEMPLATE,
                            "",
                            args.mainScreenMenu.getEventCategoryName(),
                            frameBody.tags ?: "",
                            frameBody.baseUrl ?: "",
                            frameBody.thumb,
                            frameBody.thumbtype,
                            showTemplateFrameClickAd,
                            true,
                            frameBody,
                            "list"
                        )
                    ) {
                        if (position > -1) framesAdapter?.notifyItemChanged(
                            position
                        )
                    }
                }
            },
            onFavouriteClick = {
                apiViewModel.favourite(
                    FavouriteModel(
                        isFavourite = it.isFavourite, frame = it.frame
                    )
                )
            },
            onPurchaseTypeTagClick = {}
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTemplatesBinding.inflate(inflater, container, false)
        binding.initViews()

        onBackPress()

        return binding.root
    }

    private var visibleAd = false

    fun hideScreenAds() {
        visibleAd = true
        if (isProVersion()) {
            framesAdapter?.hideRvAd()
         //   _binding?.proBtn?.pauseAnimation()
         //   _binding?.proBtn?.gone()
        }
    }

    fun showScreenAds() {
        visibleAd = false
    }

    private fun FragmentTemplatesBinding.initViews() {
        titleNameTv.text = args.mainScreenMenu.uppercase()?:"Choose Frame"
        initListener()
        if(args.fromHome){
            initObservers()
        }else{
            initObserversTemplates()
        }

        initRecyclerViews()
    }

    private fun FragmentTemplatesBinding.initListener() {
        homeUpBtn.setSingleClickListener {
            kotlin.runCatching {
                activity?.let {
                    if (it is MainActivity) {
                        it.hideCollapsibleBanner = true
                    }
                }
                navController?.navigateUp()
            }
        }

        if (!isProVersion()) {
            proBtn.setSingleClickListener {
                kotlin.runCatching {
                    activity?.let {
                        startActivity(Intent().apply {
                            setClassName(
                                it.applicationContext,
                                getProScreen()
                            )
                            putExtra("from_frames", false)
                        })
                    }
                }
            }
        } else {
         //   proBtn.pauseAnimation()
            proBtn.gone()
        }
    }

    private var tryCounter = 0

    private fun FragmentTemplatesBinding.initObservers() {
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
                                    loadingView.visible()
                                    framesRv.invisible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {

                                    categoriesFramesSubData?.let {
                                        if (it.isNotEmpty()) {
                                            loadingView.gone()
                                            loadingView.stopShimmer()
                                            if (isNetworkAvailable)
                                                framesRv.visible()
                                            return@observe
                                        }
                                    }

                                    if (isCompleted) {
                                        isCompleted = false
                                        kotlin.runCatching {
                                            val list: MutableList<GetMainScreenQuery.ChildCategory?> =
                                                mutableListOf()
                                            it.data?.childCategories?.let { mainMenuOptions ->
                                                list.addAll(mainMenuOptions)
                                                list.forEach {
                                                    it?.let { parentItem ->
                                                        when (parentItem.title.lowercase()) {
                                                            MainMenuOptions.MAIN.title.lowercase() -> {
                                                                kotlin.runCatching {
//                                                                    it.children?.get(1)?.let{
                                                                    it.children?.forEach {
                                                                        it?.apply {
                                                                            frames?.let { frameObj ->
//
                                                                                Log.i(
                                                                                    "ARGUMENTTAG",
                                                                                    "name--- ${args.mainScreenMenu.lowercase()} and condition is ${title.lowercase() == args.mainScreenMenu.lowercase()}"
                                                                                )
//
                                                                                if (title.lowercase() == args.mainScreenMenu.lowercase()){
//                                                                                if (title.lowercase() =="TopPick".lowercase() ) {

                                                                                    if (_binding?.framesRv?.isComputingLayout != true) {
                                                                                        framesAdapter?.clearData()
                                                                                    }
                                                                                    lifecycleScope.launch(
                                                                                        Dispatchers.IO
                                                                                    ) {
                                                                                        val frames =
                                                                                            frameObj.filterNotNull()
                                                                                                .map { frame ->
                                                                                                    FramesRV.FrameModel(
                                                                                                        frame
                                                                                                    )
                                                                                                }
                                                                                        frames.forEach { frame ->

                                                                                            frame.isFavourite =
                                                                                                withContext(
                                                                                                    Dispatchers.Default
                                                                                                ) {
                                                                                                    ConstantsCommon.favouriteFrames.mapNotNull { it?.id }
                                                                                                        .contains(
                                                                                                            FavouriteTypeConverter.fromJson(
                                                                                                                FavouriteTypeConverter.toJson(
                                                                                                                    frame.frame
                                                                                                                )
                                                                                                            )?.id
                                                                                                        )
                                                                                                }
                                                                                            withContext(
                                                                                                Dispatchers.Main
                                                                                            ) {
                                                                                                loadingView.gone()
                                                                                                loadingView.stopShimmer()
                                                                                                if (isNetworkAvailable)
                                                                                                    framesRv.visible()
                                                                                                framesAdapter?.updateSingleItem(
                                                                                                    frame
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                        categoriesFramesSubData?.put(
                                                                                            title,
                                                                                            frames
                                                                                        )
                                                                                    }
                                                                                        .invokeOnCompletion {
                                                                                            isCompleted = true
                                                                                          //  val targetItemId = 26978

                                                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                                                val targetItemId = args.clickItemId?:0
                                                                                                targetItemId?.let {
                                                                                                    scrollToItemById(it)
                                                                                                }
                                                                                            }

                                                                                        }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

//                                                            MainMenuOptions.TEMPLATES.title.lowercase() -> {
//
//                                                                kotlin.runCatching {
////
//                                                                    it.children?.forEach {
//
//                                                                        it?.apply {
//
//                                                                            frames?.let { frameObj ->
//
//                                                                                if (title.lowercase() == args.mainScreenMenu.lowercase()) {
//
//                                                                                    if (_binding?.framesRv?.isComputingLayout != true) {
//                                                                                        framesAdapter?.clearData()
//                                                                                    }
//                                                                                    lifecycleScope.launch(
//                                                                                        Dispatchers.IO
//                                                                                    ) {
//                                                                                        val frames =
//                                                                                            frameObj.filterNotNull()
//                                                                                                .map { frame ->
//                                                                                                    FramesRV.FrameModel(
//                                                                                                        frame
//                                                                                                    )
//                                                                                                }
//                                                                                        frames.forEach { frame ->
//
//                                                                                            frame.isFavourite =
//                                                                                                withContext(
//                                                                                                    Dispatchers.Default
//                                                                                                ) {
//                                                                                                    ConstantsCommon.favouriteFrames.mapNotNull { it?.id }
//                                                                                                        .contains(
//                                                                                                            FavouriteTypeConverter.fromJson(
//                                                                                                                FavouriteTypeConverter.toJson(
//                                                                                                                    frame.frame
//                                                                                                                )
//                                                                                                            )?.id
//                                                                                                        )
//                                                                                                }
//                                                                                            withContext(
//                                                                                                Dispatchers.Main
//                                                                                            ) {
//                                                                                                loadingView.gone()
//                                                                                                loadingView.stopShimmer()
//                                                                                                if (isNetworkAvailable)
//                                                                                                    framesRv.visible()
//                                                                                                framesAdapter?.updateSingleItem(
//                                                                                                    frame
//                                                                                                )
//                                                                                            }
//                                                                                        }
//                                                                                        categoriesFramesSubData?.put(
//                                                                                            title,
//                                                                                            frames
//                                                                                        )
//                                                                                    }
//                                                                                        .invokeOnCompletion {
//                                                                                            isCompleted =
//                                                                                                true
//                                                                                        }
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }
//                                                                }
//                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }.onFailure {
                                            isCompleted = true
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    Log.d("FAHAD", "initObservers: ERROR")
                                    if (!mActivity.isNetworkAvailable()) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        framesRv.invisible()
                                    } else {
                                        if (tryCounter == 0) {
                                            activity?.let {
                                                if (it is MainActivity) {
                                                    it.refreshTemplateAndFrames()
                                                }
                                            }
                                        }
                                        tryCounter += 1
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
    private fun FragmentTemplatesBinding.initObserversTemplates() {
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
                                    loadingView.visible()
                                    framesRv.invisible()
                                }

                                is Response.ShowSlowInternet -> {}

                                is Response.Success -> {

                                    categoriesFramesSubData?.let {
                                        if (it.isNotEmpty()) {
                                            loadingView.gone()
                                            loadingView.stopShimmer()
                                            if (isNetworkAvailable)
                                                framesRv.visible()
                                            return@observe
                                        }
                                    }

                                    if (isCompleted) {
                                        isCompleted = false
                                        kotlin.runCatching {
                                            val list: MutableList<GetMainScreenQuery.ChildCategory?> =
                                                mutableListOf()
                                            it.data?.childCategories?.let { mainMenuOptions ->
                                                list.addAll(mainMenuOptions)
                                                list.forEach {
                                                    it?.let { parentItem ->
                                                        when (parentItem.title.lowercase()) {
                                                            MainMenuOptions.TEMPLATES.title.lowercase() -> {
                                                                kotlin.runCatching {
//                                                                    it.children?.get(1)?.let{
                                                                    it.children?.forEach {
                                                                        it?.apply {
                                                                            frames?.let { frameObj ->
//
                                                                                Log.i(
                                                                                    "ARGUMENTTAG",
                                                                                    "name--- ${args.mainScreenMenu.lowercase()} and condition is ${title.lowercase() == args.mainScreenMenu.lowercase()}"
                                                                                )
//
                                                                                if (title.lowercase() == args.mainScreenMenu.lowercase()){
//                                                                                if (title.lowercase() =="TopPick".lowercase() ) {

                                                                                    if (_binding?.framesRv?.isComputingLayout != true) {
                                                                                        framesAdapter?.clearData()
                                                                                    }
                                                                                    lifecycleScope.launch(
                                                                                        Dispatchers.IO
                                                                                    ) {
                                                                                        val frames =
                                                                                            frameObj.filterNotNull()
                                                                                                .map { frame ->
                                                                                                    FramesRV.FrameModel(
                                                                                                        frame
                                                                                                    )
                                                                                                }
                                                                                        frames.forEach { frame ->

                                                                                            frame.isFavourite =
                                                                                                withContext(
                                                                                                    Dispatchers.Default
                                                                                                ) {
                                                                                                    ConstantsCommon.favouriteFrames.mapNotNull { it?.id }
                                                                                                        .contains(
                                                                                                            FavouriteTypeConverter.fromJson(
                                                                                                                FavouriteTypeConverter.toJson(
                                                                                                                    frame.frame
                                                                                                                )
                                                                                                            )?.id
                                                                                                        )
                                                                                                }
                                                                                            withContext(
                                                                                                Dispatchers.Main
                                                                                            ) {
                                                                                                loadingView.gone()
                                                                                                loadingView.stopShimmer()
                                                                                                if (isNetworkAvailable)
                                                                                                    framesRv.visible()
                                                                                                framesAdapter?.updateSingleItem(
                                                                                                    frame
                                                                                                )
                                                                                            }
                                                                                        }
                                                                                        categoriesFramesSubData?.put(
                                                                                            title,
                                                                                            frames
                                                                                        )
                                                                                    }
                                                                                        .invokeOnCompletion {
                                                                                            isCompleted = true
                                                                                            //  val targetItemId = 26978

                                                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                                                val targetItemId = args.clickItemId?:0
                                                                                                targetItemId?.let {
                                                                                                    scrollToItemById(it)
                                                                                                }
                                                                                            }

                                                                                        }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

//                                                            MainMenuOptions.TEMPLATES.title.lowercase() -> {
//
//                                                                kotlin.runCatching {
////
//                                                                    it.children?.forEach {
//
//                                                                        it?.apply {
//
//                                                                            frames?.let { frameObj ->
//
//                                                                                if (title.lowercase() == args.mainScreenMenu.lowercase()) {
//
//                                                                                    if (_binding?.framesRv?.isComputingLayout != true) {
//                                                                                        framesAdapter?.clearData()
//                                                                                    }
//                                                                                    lifecycleScope.launch(
//                                                                                        Dispatchers.IO
//                                                                                    ) {
//                                                                                        val frames =
//                                                                                            frameObj.filterNotNull()
//                                                                                                .map { frame ->
//                                                                                                    FramesRV.FrameModel(
//                                                                                                        frame
//                                                                                                    )
//                                                                                                }
//                                                                                        frames.forEach { frame ->
//
//                                                                                            frame.isFavourite =
//                                                                                                withContext(
//                                                                                                    Dispatchers.Default
//                                                                                                ) {
//                                                                                                    ConstantsCommon.favouriteFrames.mapNotNull { it?.id }
//                                                                                                        .contains(
//                                                                                                            FavouriteTypeConverter.fromJson(
//                                                                                                                FavouriteTypeConverter.toJson(
//                                                                                                                    frame.frame
//                                                                                                                )
//                                                                                                            )?.id
//                                                                                                        )
//                                                                                                }
//                                                                                            withContext(
//                                                                                                Dispatchers.Main
//                                                                                            ) {
//                                                                                                loadingView.gone()
//                                                                                                loadingView.stopShimmer()
//                                                                                                if (isNetworkAvailable)
//                                                                                                    framesRv.visible()
//                                                                                                framesAdapter?.updateSingleItem(
//                                                                                                    frame
//                                                                                                )
//                                                                                            }
//                                                                                        }
//                                                                                        categoriesFramesSubData?.put(
//                                                                                            title,
//                                                                                            frames
//                                                                                        )
//                                                                                    }
//                                                                                        .invokeOnCompletion {
//                                                                                            isCompleted =
//                                                                                                true
//                                                                                        }
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }
//                                                                }
//                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }.onFailure {
                                            isCompleted = true
                                        }
                                    }
                                }

                                is Response.Error -> {
                                    Log.d("FAHAD", "initObservers: ERROR")
                                    if (!mActivity.isNetworkAvailable()) {
                                        tryNowPlaceholder.visible()
                                        noResultFoundTv.visible()
                                        loadingView.stopShimmer()
                                        loadingView.gone()
                                        framesRv.invisible()
                                    } else {
                                        if (tryCounter == 0) {
                                            activity?.let {
                                                if (it is MainActivity) {
                                                    it.refreshTemplateAndFrames()
                                                }
                                            }
                                        }
                                        tryCounter += 1
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



    private fun scrollToItemById(itemId: Int) {
        framesAdapter?.let { adapter ->
            val position = adapter.findPositionById(itemId)
            if (position != -1) {
                binding?.framesRv?.let { recyclerView ->
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
                    // First, instantly scroll to nearby position
                  //  layoutManager?.scrollToPositionWithOffset(position, 0)
                    // Then do a small smooth adjustment if needed
                    val itemCount = adapter.itemCount
                    if (position == itemCount - 1) {
                        layoutManager?.scrollToPositionWithOffset(position, (500 *recyclerView.resources.displayMetrics.density).toInt()) // leave 50px space
                    } else {
                        layoutManager?.scrollToPositionWithOffset(position, 0)
                    }
//                    recyclerView.post {
//                        recyclerView.smoothScrollToPosition(position)
//                    }
                }
            }
        }
    }

    /*fun scrollToItemById(itemId: Int) {
        framesAdapter?.let { adapter ->
            val position = adapter.findPositionById(itemId)
            if (position != -1) {
                _binding?.framesRv?.smoothScrollToPosition(position)

            }
        }
    }
*/
    private fun FragmentTemplatesBinding.initRecyclerViews() {
//        categoryListRv.adapter = categoryListAdapter
        framesRv.adapter = framesAdapter
        /*framesRv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            var lastX = 0
            var processComplete = true
            override fun onInterceptTouchEvent(
                rv: RecyclerView,
                e: MotionEvent
            ): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        processComplete = true
                        lastX = e.x.toInt()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (processComplete) {
                            val isScrollingRight = e.x < lastX
                            val delta = abs(e.x.toInt() - lastX) > 220
                            processComplete = !delta
                            framesRv.adapter?.let {
                                if (delta) {
                                    tagsList?.apply {
                                        if (isNotEmpty()) {
                                            tagsList?.get(currentPosition)?.let { key ->
                                                categoriesFramesSubData?.get(key)?.let {
                                                    if (it.isNotEmpty()) {
                                                        _binding?.framesRv?.visible()
                                                        if (isScrollingRight) {
                                                            scrollDirection = 1
                                                            categoryListAdapter?.selectNext(
                                                                currentPosition
                                                            )
                                                        } else {
                                                            scrollDirection = -1
                                                            categoryListAdapter?.selectPrevious(
                                                                currentPosition
                                                            )
                                                        }
                                                    } else {
                                                        categoryListAdapter?.unselect()
                                                    }
                                                } ?: run {
                                                    categoryListAdapter?.unselect()
                                                }
                                            } ?: run {
                                                categoryListAdapter?.unselect()
                                            }
                                        } else {
                                            categoryListAdapter?.unselect()
                                        }
                                    } ?: run {
                                        categoryListAdapter?.unselect()
                                    }
                                }
                            }
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        lastX = 0
                        processComplete = true
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })*/
    }

    override fun onResume() {
        super.onResume()
//        if (!isProVersion()) {
//            _binding?.let {
//                if (!visibleAd)
//                    _binding?.bannerContainer?.visible()
//                if (seeAllAdaptiveBanner) {
//                    mActivity?.onResumeBanner(
//                        binding.adBannerContainer,
//                        binding.crossBannerIv,
//                        binding.bannerLayout.adContainer,
//                        binding.bannerLayout.shimmerViewContainer
//                    )
//                } else {
//                    activity?.onResumeCollapsableBanner(
//                        binding.adBannerContainer,
//                        binding.crossBannerIv,
//                        binding.bannerLayout.adContainer,
//                        binding.bannerLayout.shimmerViewContainer,
//                        lifecycleOwner = viewLifecycleOwner
//                    )
//                }
//            }
//        } else {
//            _binding?.bannerContainer?.gone()
//        }
    }

    override fun onPause() {
        super.onPause()
//        if (seeAllAdaptiveBanner) {
//            onPauseBanner()
//        } else {
//            onPauseCollapsableBanner()
//        }
    }

    private fun checkInternet() {
        _binding?.apply {
            if (!mActivity.isNetworkAvailable()) {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
                loadingView.stopShimmer()
                loadingView.gone()
                framesRv.invisible()
            } else {
                tryNowPlaceholder.gone()
                noResultFoundTv.gone()
                loadingView.gone()
                loadingView.stopShimmer()
                framesRv.visible()
            }
        }
    }
}