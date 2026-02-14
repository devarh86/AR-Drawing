package com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.childs

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.ads.Constants.showForYouFrameClickAd

import com.example.ads.admobs.utils.showInterstitial
import com.example.ads.admobs.utils.showRewardedInterstitial
import com.example.ads.crosspromo.helper.hide
import com.example.ads.crosspromo.helper.show
import com.example.ads.dialogs.createProFramesDialog
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentForYouBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.pro.ProActivity
import com.fahad.newtruelovebyfahad.ui.fragments.common.TagsRVAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragment
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragment.Companion.downloadDialog
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragment.Companion.mFeaturePager
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragmentDirections
import com.fahad.newtruelovebyfahad.ui.fragments.feature.adapter.FeatureRV
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.enums.FrameThumbType
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.google.android.gms.ads.nativead.NativeAd
import com.project.common.datastore.FrameDataStore
import com.example.ads.dialogs.createDownloadingDialog
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.repo.room.model.RecentsModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.ConstantsCommon.featureForYouData
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.getProScreen
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class ForYouFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    companion object {
        var forYouTagsAdapter: TagsRVAdapter? = null
    }

    private var forYouFramesAdapter: FeatureRV? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var _binding: FragmentForYouBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private val forYouSelectedTags = hashSetOf<TagsRVAdapter.TagModel>()
    private var nativeAd: NativeAd? = null
    // private var stickyHeaderDecoration: StickyHeaderDecorationFeature? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("TAG", "onCreate: foryoufragment $this")

        forYouTagsAdapter = TagsRVAdapter(emptyList()) { it, _ ->
            selectTags(it)
        }
        forYouFramesAdapter = FeatureRV(
            mContext,
            arrayListOf(),
            nativeAd,
            onClick = { frameBody, position ->

                if (!isNetworkAvailable) {
                    forYouFramesAdapter?.let {
                        kotlin.runCatching {
                            if (!frameBody.thumb.contains("android_asset")) {
                                kotlin.runCatching {
                                    mActivity?.let {
                                        Toast.makeText(
                                            mActivity,
                                            "Please connect to internet",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                return@FeatureRV
                            }
                        }
                    }
                }

                val permissions =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(

                        Manifest.permission.READ_MEDIA_IMAGES
                    )
//                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//                        arrayOf(
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.READ_MEDIA_IMAGES,
//                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
//                        )
//                    }
                    else arrayOf(
                         Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )

                (mActivity as Permissions).checkAndRequestPermissions(*permissions, action =
                {
                    mActivity?.let { it ->
                        if (it is MainActivity) {
                            it.eventForFrameClick(
                                FrameObject(
                                    frameBody.id,
                                    frameBody.title,
                                    Events.Screens.FEATURE,
                                    "",
                                    Events.Screens.FEATURE,
                                    frameBody.tags ?: "",
                                    frameBody.baseUrl ?: "",
                                    frameBody.thumb,
                                    frameBody.thumbtype,
                                    showForYouFrameClickAd,
                                    true,
                                    frameBody,
                                    "list"
                                )
                            )
                        }
                    }

                    mContext?.let {
                        if (frameBody.tags?.isNotEmpty() == true && frameBody.tags != "Free" && !isProVersion() && !ConstantsCommon.rewardedAssetsList.contains(
                                frameBody.id
                            )
                        ) {
                            mActivity?.createProFramesDialog(
                                true,
                                thumb = "${frameBody.baseUrl}${frameBody.thumb}",
                                thumbType = ContextCompat.getDrawable(
                                    it,
                                    when (frameBody.thumbtype.lowercase()) {
                                        FrameThumbType.PORTRAIT.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_portrait
                                        FrameThumbType.LANDSCAPE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_landscape
                                        FrameThumbType.SQUARE.type.lowercase() -> com.project.common.R.drawable.frame_placeholder_squre
                                        else -> com.project.common.R.drawable.frame_placeholder_portrait
                                    }
                                ),
                                action = {

                                    downloadDialog =
                                        mActivity?.createDownloadingDialog(
                                            frameBody.baseUrl,
                                            frameBody.thumb,
                                            frameBody.thumbtype
                                        )
                                    mActivity?.let {
                                        if (it is MainActivity) {
                                            it.downloadDialog = downloadDialog
                                        }
                                    }
                                    mActivity?.showRewardedInterstitial(true,
                                        loadedAction = {

                                            apiViewModel.addToRecent(RecentsModel(frame = frameBody))

                                            lifecycleScope.launch(Dispatchers.IO) {
                                                frameDataStore.writeUnlockedId(frameBody.id)
                                                ConstantsCommon.rewardedAssetsList.add(frameBody.id)
                                                withContext(Main) {
                                                    if (position != -1) forYouFramesAdapter?.notifyItemChanged(
                                                        position
                                                    )
                                                    val onlineOrOfflineFrame =
                                                        if (frameBody.thumb.contains("android_asset")) {
                                                            false
                                                        } else {
                                                            isNetworkAvailable
                                                        }
                                                    apiViewModel.getFrame(
                                                        frameBody.id,
                                                        onlineOrOfflineFrame
                                                    )
                                                }
                                            }.invokeOnCompletion {
                                            }
                                        },
                                        failedAction = {
                                            downloadDialog?.apply { if (isShowing) dismiss() }
                                        }
                                    )
                                },
                                goProAction = {
                                    try {
                                        mActivity?.applicationContext?.let { it1 ->
                                            startActivity(
                                                Intent().apply {
                                                    setClassName(
                                                        it1,
                                                        getProScreen()
                                                    )
                                                    putExtra("from_frames", false)
                                                }
                                            )
                                        }
                                    } catch (_: Exception) {
                                    }
                                },
                                dismissAction = {},
                                frameBody.tags?.lowercase() == "paid"
                            )
                        } else {

                            downloadDialog = mActivity?.createDownloadingDialog(
                                frameBody.baseUrl, frameBody.thumb, frameBody.thumbtype
                            )

                            mActivity?.let {
                                if (it is MainActivity) {
                                    it.downloadDialog = downloadDialog
                                }
                            }

                            apiViewModel.addToRecent(RecentsModel(frame = frameBody))

                            mActivity.showInterstitial(
                                loadedAction = {
                                    val onlineOrOfflineFrame =
                                        if (frameBody.thumb.contains("android_asset")) {
                                            false
                                        } else {
                                            isNetworkAvailable
                                        }
                                    apiViewModel.getFrame(
                                        frameBody.id,
                                        onlineOrOfflineFrame
                                    )
                                },
                                failedAction = {
                                    val onlineOrOfflineFrame =
                                        if (frameBody.thumb.contains("android_asset")) {
                                            false
                                        } else {
                                            isNetworkAvailable
                                        }
                                    apiViewModel.getFrame(
                                        frameBody.id,
                                        onlineOrOfflineFrame
                                    )
                                }, showAd = showForYouFrameClickAd, onCheck = true
                            )
                        }
                    }
                }, declineAction =
                {})
            },
            onFavouriteClick = {

                apiViewModel.favourite(
                    FavouriteModel(
                        isFavourite = it.isFavourite,
                        frame = it.frame
                    )
                )
            },
            onPurchaseTypeTagClick = {}
        )

        /*mActivity.loadNative(
            loadedAction = {
                nativeAd = it
                if (listUpdated) {
                    forYouFramesAdapter?.updateAd(nativeAd)
                }
                isNativeLoaded = true
            },
            failedAction = {
                nativeAd = null
                isNativeLoaded = false
            }
        )*/
    }

    private var isNativeLoaded = false
    private var listUpdated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentForYouBinding.inflate(inflater, container, false)
        _binding?.initViews()
//        parentScreen = Events.SubScreens.FOR_YOU
        return binding.root
    }

    fun hideScreenAds() {
        forYouFramesAdapter?.hideRvAd()

        /*stickyHeaderDecoration?.stopCalculations = true
        if (_binding?.nativeContainer?.isVisible == false) forYouFramesAdapter?.hideRvAd()
        else {
            _binding?.rvPortraitNativeLayout?.adContainer?.gone()
            _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.visible()
        }*/
    }

    fun showScreenAds() {
        forYouFramesAdapter?.showRvAd()

        /*stickyHeaderDecoration?.stopCalculations = false
        if (_binding?.nativeContainer?.isVisible == false) forYouFramesAdapter?.showRvAd() else {
            _binding?.rvPortraitNativeLayout?.adContainer?.visible()
            _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.gone()
        }*/
    }

    private fun FragmentForYouBinding.initViews() {

        searchBtn.setSingleClickListener {

        }

        initRecyclerViews()
        initObserver()
    }

    private fun FragmentForYouBinding.initRecyclerViews() {
        forYouTagsAdapter?.updateDataList(featureForYouData?.first)
        forYouRv.itemAnimator = null
        forYouTagsRv.adapter = forYouTagsAdapter
        forYouTagsRv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            var lastX = 0
            override fun onInterceptTouchEvent(
                rv: RecyclerView,
                e: MotionEvent,
            ): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> lastX = e.x.toInt()
                    MotionEvent.ACTION_MOVE -> {
                        val isScrollingRight = e.x < lastX
                        forYouTagsRv.adapter?.let {
                            mFeaturePager?.isUserInputEnabled =
                                isScrollingRight && (forYouTagsRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == it.itemCount - 1 || !isScrollingRight && (forYouTagsRv.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        lastX = 0
                        mFeaturePager?.isUserInputEnabled = true
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        var isFirstScroll = true
        var internetState = true

        forYouRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                activity?.let {
                    kotlin.runCatching {
                        if (it is MainActivity) {
                            if (dy <= 0) {
                                it.goProBottom(true)
                            } else {
                                it.goProBottom(false)
                            }
                        }
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                runCatching {

                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager?
                    val firstVisibleItemPositions: IntArray? =
                        layoutManager?.findFirstVisibleItemPositions(null)
                    if (firstVisibleItemPositions != null && firstVisibleItemPositions.isNotEmpty()) {
                        firstVisibleItemPositions[0].let { position ->
                            if (position == 0 && (layoutManager.findViewByPosition(0)?.top
                                    ?: -1) >= 0
                            ) {
                                parentFragment?.let {
                                    if (it is FeaturedFragment) {
                                        it.viewPagerScrollEnable(true)
                                    }
                                }
                            } else {
                                parentFragment?.let {
                                    if (it is FeaturedFragment) {
                                        it.viewPagerScrollEnable(false)
                                    }
                                }
                            }
                        }
                    } else {
                        parentFragment?.let {
                            if (it is FeaturedFragment) {
                                it.viewPagerScrollEnable(false)
                            }
                        }
                    }

                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            Log.d("FAHAD_RV", "onScrollStateChanged: IDLE")
                            isFirstScroll = true
                        }

                        RecyclerView.SCROLL_STATE_DRAGGING -> {
                            Log.d("FAHAD_RV", "onScrollStateChanged: DRAGGING")
                            if (isFirstScroll) {
                                Log.d("FAHAD_RV", "onScrollStateChanged: DRAGGING is First Check")
                                internetState = mActivity?.isNetworkAvailable() ?: false
                                isFirstScroll = false
                            }
                        }

                        RecyclerView.SCROLL_STATE_SETTLING -> {}
                    }
                    val lastVisibleItemPositions: IntArray? =
                        layoutManager?.findLastVisibleItemPositions(null)
                    if (lastVisibleItemPositions != null && lastVisibleItemPositions.isNotEmpty() && !internetState) {
                        lastVisibleItemPositions[0].let { position ->
                            if (position in 10..12 && (layoutManager.findViewByPosition(position)?.top
                                    ?: -1) >= 0
                            ) {
                                _binding?.internetToast?.show()
                            } else {
                                _binding?.internetToast?.hide()
                            }
                        }
                    } else {
                        _binding?.internetToast?.hide()
                    }
                }
            }
        })

        forYouRv.adapter = forYouFramesAdapter
//        forYouRv.itemAnimator = null

        /*stickyHeaderDecoration = StickyHeaderDecorationFeature(binding.root, showAd = {
            Log.d("FAHAD", "initRecyclerViews: Decoration forYouFramesAdapter $it")
            if (it) {
                nativeAd?.let {
                    if (_binding?.nativeContainer?.isVisible == false) {
                        forYouFramesAdapter?.hideRvAd()
                        mActivity.showNative(
                            R.layout.native_ad_portrait_rv,
                            it,
                            loadedAction = {
                                _binding?.nativeContainer?.visible()
                                _binding?.rvPortraitNativeLayout?.adContainer?.visible()
                                _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.gone()
                                _binding?.rvPortraitNativeLayout?.adContainer?.removeAllViews()
                                if (it?.parent != null) {
                                    (it.parent as ViewGroup).removeView(it)
                                }
                                _binding?.rvPortraitNativeLayout?.adContainer?.addView(it)
                            },
                            failedAction = {
                                _binding?.nativeContainer?.gone()
                                _binding?.rvPortraitNativeLayout?.adContainer?.gone()
                                _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.gone()
                            })
                    }
                }
            } else {
                forYouFramesAdapter?.showRvAd()
                _binding?.nativeContainer?.gone()
            }
        })
        stickyHeaderDecoration?.let {
            forYouRv.addItemDecoration(it)
        }*/
    }

    private fun FragmentForYouBinding.initObserver() {
        try {
            if (apiViewModel.offlineFeatureScreen.hasObservers()) {
                apiViewModel.offlineFeatureScreen.removeObservers(this@ForYouFragment)
            }
            apiViewModel.offlineFeatureScreen.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is Response.ShowSlowInternet -> {}

                    is Response.Success -> {
                        forYouFramesAdapter?.clearData()
                        featureForYouData?.second?.forEach {
                            it?.let {
                                val frame = FeatureRV.FrameModel(it)
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }
                                        .contains(frame.frame.id)
                                forYouFramesAdapter?.updateSingleItem(frame)
                            }
                        }

                        if (isNativeLoaded) {
                            forYouFramesAdapter?.updateAd(nativeAd)
                        }
                        listUpdated = true

                        /*featureForYouData?.second?.filterNotNull()?.map { frame ->
                            FeatureRV.FrameModel(frame)
                        }?.let {
                            it.map { frame ->
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                            }
                            forYouFramesAdapter?.updateDataList(it)
                        }*/
                    }

                    is Response.Error -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("Fahad", "initApiObservers: ")
        }
        try {
            if (apiViewModel.featureScreen.hasObservers()) {
                apiViewModel.featureScreen.removeObservers(this@ForYouFragment)
            }
            apiViewModel.featureScreen.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is Response.ShowSlowInternet -> {}

                    is Response.Success -> {
                        forYouFramesAdapter?.clearData()
                        featureForYouData?.second?.forEach {
                            it?.let {
                                val frame = FeatureRV.FrameModel(it)
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                                forYouFramesAdapter?.updateSingleItem(frame)
                            }
                        }

                        if (isNativeLoaded) {
                            forYouFramesAdapter?.updateAd(nativeAd)
                        }
                        listUpdated = true

                        /*featureForYouData?.second?.filterNotNull()?.map { frame ->
                            FeatureRV.FrameModel(frame)
                        }?.let {
                            it.map { frame ->
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                            }
                            forYouFramesAdapter?.updateDataList(it)
                        }*/
                    }

                    is Response.Error -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("Fahad", "initApiObservers: ")
        }
    }

    private fun selectTags(tag: TagsRVAdapter.TagModel, fromParent: Boolean = false) {
        forYouSelectedTags.apply {
            if (tag.mSelected) {
                add(tag)
                forYouFramesAdapter?.getCurrentDataList()?.let {
                    val sortedList = it.sortedWith(compareByDescending {
                        it.frame.hashtag?.mapNotNull { it?.title }?.contains(tag.tag)
                    })
                    sortedList.map { frame ->
                        frame.isFavourite =
                            favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                    }
                    forYouFramesAdapter?.clearData()
                    sortedList.forEach {
                        forYouFramesAdapter?.updateSingleItem(it)
                    }
                    if (isNativeLoaded) {
                        forYouFramesAdapter?.updateAd(nativeAd)
                    }
                    listUpdated = true
                    //forYouFramesAdapter?.updateDataList(sortedList)
                    _binding?.forYouRv?.smoothScrollToPosition(0)
                }
            } else remove(tag)
        }
    }

    fun selectTagsFromSearch(tag: TagsRVAdapter.TagModel) {
        selectTags(tag, true)
        val position = forYouTagsAdapter?.selectTag(tag)
        position?.let { _binding?.forYouTagsRv?.smoothScrollToPosition(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TAG", "onCreate: foryoufragment onDestroy $this")
    }

    override fun onResume() {
        super.onResume()
//        if (_binding != null && ConstantsCommon.notifyAdapterForRewardedAssets) {
//            ConstantsCommon.notifyAdapterForRewardedAssets = false
//            forYouFramesAdapter?.notifyDataSetChanged()
//        }
    }
}