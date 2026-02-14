package com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.childs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import com.example.ads.Constants.showTodaySpecialFrameClickAd
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentTodaySpecialBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.common.TagsRVAdapter
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragment
import com.fahad.newtruelovebyfahad.ui.fragments.feature.FeaturedFragmentDirections
import com.fahad.newtruelovebyfahad.ui.fragments.feature.adapter.FeatureRV
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.api.apollo.helper.Response
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.utils.ConstantsCommon.featureTodaySpecialData
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TodaySpecialFragment : Fragment() {
    @SuppressLint("StaticFieldLeak")
    companion object {
        var todaySpecialTagsAdapter: TagsRVAdapter? = null
        var todaySpecialFramesAdapter: FeatureRV? = null
    }

    private var _binding: FragmentTodaySpecialBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private val todaySpecialSelectedTags = hashSetOf<TagsRVAdapter.TagModel>()
    private var nativeAd: NativeAd? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todaySpecialTagsAdapter = TagsRVAdapter(emptyList()) { it, _ ->
            selectTags(it)
        }

        todaySpecialFramesAdapter = FeatureRV(
            mContext,
            arrayListOf(),
            nativeAd,
            onClick = { frameBody, position ->
                if (mActivity != null && mActivity is MainActivity) {
                    (mActivity as MainActivity).frameClick(
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
                            showTodaySpecialFrameClickAd,
                            true,
                            frameBody,
                            "list"
                        )
                    ) {
                        if (position > -1) todaySpecialFramesAdapter?.notifyItemChanged(
                            position
                        )
                    }
                }
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
                    todaySpecialFramesAdapter?.updateAd(nativeAd)
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

    override fun onResume() {
        super.onResume()
//        if (_binding != null && ConstantsCommon.notifyAdapterForRewardedAssets) {
//            ConstantsCommon.notifyAdapterForRewardedAssets = false
//            todaySpecialFramesAdapter?.notifyDataSetChanged()
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodaySpecialBinding.inflate(inflater, container, false)
        _binding?.initViews()
//        parentScreen = Events.SubScreens.TODAY_SPECIAL
        return binding.root
    }

    fun hideScreenAds() {
        todaySpecialFramesAdapter?.hideRvAd()

        /*stickyHeaderDecoration?.stopCalculations = true
        if (_binding?.nativeContainer?.isVisible == false) todaySpecialFramesAdapter?.hideRvAd()
        else {
            _binding?.rvPortraitNativeLayout?.adContainer?.gone()
            _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.visible()
        }*/
    }

    fun showScreenAds() {
        todaySpecialFramesAdapter?.showRvAd()

        /*stickyHeaderDecoration?.stopCalculations = false
        if (_binding?.nativeContainer?.isVisible == false) todaySpecialFramesAdapter?.showRvAd()
        else {
            _binding?.rvPortraitNativeLayout?.adContainer?.visible()
            _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.gone()
        }*/
    }

    private fun FragmentTodaySpecialBinding.initViews() {
        if (mActivity?.isNetworkAvailable() == true) {
            _binding?.searchBtn?.isVisible = true
            _binding?.noInternetContainer?.gone()
            _binding?.todaySpecialRv?.visibility = View.VISIBLE
            _binding?.todaySpecialTagsRv?.visibility = View.VISIBLE
        } else {
            _binding?.searchBtn?.isVisible = false
            _binding?.noInternetContainer?.visible()
            _binding?.todaySpecialRv?.visibility = View.INVISIBLE
            _binding?.todaySpecialTagsRv?.visibility = View.INVISIBLE
        }

        searchBtn.setSingleClickListener {

        }
        initRecyclerViews()
        initObservers()
    }

    private fun FragmentTodaySpecialBinding.initRecyclerViews() {
        todaySpecialTagsAdapter?.updateDataList(featureTodaySpecialData?.first)
        todaySpecialTagsRv.adapter = todaySpecialTagsAdapter

        todaySpecialTagsRv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            var lastX = 0
            override fun onInterceptTouchEvent(
                rv: RecyclerView,
                e: MotionEvent
            ): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> lastX = e.x.toInt()
                    MotionEvent.ACTION_MOVE -> {
                        val isScrollingRight = e.x < lastX
                        todaySpecialTagsRv.adapter?.let {
                            FeaturedFragment.mFeaturePager?.isUserInputEnabled =
                                isScrollingRight && (todaySpecialTagsRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == it.itemCount - 1 || !isScrollingRight && (todaySpecialTagsRv.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        lastX = 0
                        FeaturedFragment.mFeaturePager?.isUserInputEnabled = true
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        todaySpecialRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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
                                    ?: -1) >= 0 && !ConstantsCommon.isGoProBottomRvClicked
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
                }
            }
        })
        todaySpecialRv.adapter = todaySpecialFramesAdapter

        /*stickyHeaderDecoration = StickyHeaderDecorationFeature(binding.root, showAd = {
            Log.d("FAHAD", "initRecyclerViews: Decoration todaySpecialFramesAdapter $it")
            if (it) {
                nativeAd?.let {
                    if (_binding?.nativeContainer?.isVisible == false) {
                        todaySpecialFramesAdapter?.hideRvAd()
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
                todaySpecialFramesAdapter?.showRvAd()
                _binding?.nativeContainer?.gone()
            }
        })
        stickyHeaderDecoration?.let {
            todaySpecialRv.addItemDecoration(it)
        }*/
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentTodaySpecialBinding.initObservers() {

        try {
            if (!isProVersion()) {
                if (isProVersion.hasObservers()) {
                    isProVersion.removeObservers(this@TodaySpecialFragment)
                }
                isProVersion.observe(viewLifecycleOwner) {
                    it?.let {
                        if (it) {
                            todaySpecialFramesAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("FAHAD", "initObservers: crash")
        }

        if (!mActivity.isNetworkAvailable()) {
            try {
                if (apiViewModel.offlineFeatureScreen.hasObservers()) {
                    apiViewModel.offlineFeatureScreen.removeObservers(this@TodaySpecialFragment)
                }
                apiViewModel.offlineFeatureScreen.observe(viewLifecycleOwner) {
                    when (it) {
                        is Response.Loading -> {
                            Log.d("Fahad", "initApiObservers: ")
                        }

                        is Response.ShowSlowInternet -> {}

                        is Response.Success -> {
                            todaySpecialFramesAdapter?.clearData()
                            featureTodaySpecialData?.second?.forEach {
                                it?.let {
                                    val frame = FeatureRV.FrameModel(it)
                                    frame.isFavourite =
                                        favouriteFrames.mapNotNull { it?.id }
                                            .contains(frame.frame.id)
                                    todaySpecialFramesAdapter?.updateSingleItem(frame)
                                }
                            }
                            if (isNativeLoaded) {
                                todaySpecialFramesAdapter?.updateAd(nativeAd)
                            }
                            listUpdated = true
                        }

                        is Response.Error -> {
                            Log.d("Fahad", "initApiObservers: ")
                        }
                    }
                }
            } catch (_: Exception) {
                Log.d("FAHAD", "initObservers: crash")
            }
        }

        try {
            if (apiViewModel.featureScreen.hasObservers()) {
                apiViewModel.featureScreen.removeObservers(this@TodaySpecialFragment)
            }
            apiViewModel.featureScreen.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is Response.ShowSlowInternet -> {}

                    is Response.Success -> {
                        todaySpecialFramesAdapter?.clearData()
                        featureTodaySpecialData?.second?.forEach {
                            it?.let {
                                val frame = FeatureRV.FrameModel(it)
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                                todaySpecialFramesAdapter?.updateSingleItem(frame)
                            }
                        }
                        if (isNativeLoaded) {
                            todaySpecialFramesAdapter?.updateAd(nativeAd)
                        }
                        listUpdated = true

                        /*featureTodaySpecialData?.second?.filterNotNull()?.map { frame ->
                            FeatureRV.FrameModel(frame)
                        }?.let {
                            it.map { frame ->
                                frame.isFavourite = favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                            }
                            todaySpecialFramesAdapter?.updateDataList(it)
                        }*/
                    }

                    is Response.Error -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("FAHAD", "initObservers: crash")
        }
    }

    private fun selectTags(tag: TagsRVAdapter.TagModel, fromParent: Boolean = false) {
        todaySpecialSelectedTags.apply {
            if (tag.mSelected) {
                add(tag)
                todaySpecialFramesAdapter?.getCurrentDataList()?.let {
                    val sortedList = it.sortedWith(compareByDescending {
                        it.frame.hashtag?.mapNotNull { it?.title }
                            ?.contains(tag.tag)
                    })
                    sortedList.map { frame ->
                        frame.isFavourite =
                            favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                    }
                    todaySpecialFramesAdapter?.clearData()
                    sortedList.forEach {
                        todaySpecialFramesAdapter?.updateSingleItem(it)
                    }
                    if (isNativeLoaded) {
                        todaySpecialFramesAdapter?.updateAd(nativeAd)
                    }
                    listUpdated = true

                    // todaySpecialFramesAdapter?.updateDataList(sortedList)
                    _binding?.todaySpecialRv?.scrollToPosition(0)
                }
            } else remove(tag)
        }
    }

    fun selectTagsFromSearch(tag: TagsRVAdapter.TagModel) {
        selectTags(tag, true)
        val position = todaySpecialTagsAdapter?.selectTag(tag)
        position?.let { _binding?.todaySpecialRv?.smoothScrollToPosition(it) }
    }
}