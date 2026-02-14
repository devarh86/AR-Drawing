package com.fahad.newtruelovebyfahad.ui.fragments.feature.pager.childs

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.example.ads.Constants.showMostUsedFrameClickAd
import com.example.analytics.Events
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentMostUsedBinding
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
import com.project.common.utils.ConstantsCommon.featureMostUsedData
import com.project.common.utils.getProScreen
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MostUsedFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    companion object {
        var mostUsedTagsAdapter: TagsRVAdapter? = null
        var mostUsedFramesAdapter: FeatureRV? = null
    }

    private var _binding: FragmentMostUsedBinding? = null
    private val binding get() = _binding!!
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private val mostUsedSelectedTags = hashSetOf<TagsRVAdapter.TagModel>()
    private var nativeAd: NativeAd? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore

    // private var stickyHeaderDecoration: StickyHeaderDecorationFeature? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("TAG", "onCreate: mostusedfragment")
        mostUsedTagsAdapter = TagsRVAdapter(emptyList()) { it, position ->
            selectTags(it)
        }

        mostUsedFramesAdapter = FeatureRV(
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
                            showMostUsedFrameClickAd,
                            true,
                            frameBody,
                            "list"
                        )
                    ) {
                        if (position > -1) mostUsedFramesAdapter?.notifyItemChanged(
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
    }

    private var isNativeLoaded = false
    private var listUpdated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMostUsedBinding.inflate(inflater, container, false)
        _binding?.initViews()
//        parentScreen = Events.SubScreens.MOST_USED
        return binding.root
    }

    fun hideScreenAds() {
        mostUsedFramesAdapter?.hideRvAd()

        /*stickyHeaderDecoration?.stopCalculations = true
        if (_binding?.nativeContainer?.isVisible == false) mostUsedFramesAdapter?.hideRvAd()
        else {
            _binding?.rvPortraitNativeLayout?.adContainer?.gone()
            _binding?.rvPortraitNativeLayout?.shimmerViewContainer?.visible()
        }*/
    }

    fun showScreenAds() {
        mostUsedFramesAdapter?.showRvAd()
    }

    private fun FragmentMostUsedBinding.initViews() {

        if (mActivity?.isNetworkAvailable() == true) {
            _binding?.noInternetContainer?.gone()
            _binding?.mostUsedRv?.visibility = View.VISIBLE
            _binding?.mostUsedTagsRv?.visibility = View.VISIBLE
        } else {
            _binding?.noInternetContainer?.visible()
            _binding?.mostUsedRv?.visibility = View.INVISIBLE
            _binding?.mostUsedTagsRv?.visibility = View.INVISIBLE
        }


        searchBtn.setSingleClickListener {

        }

        initRecyclerViews()
        initListener()
    }

    private fun FragmentMostUsedBinding.initRecyclerViews() {
        mostUsedTagsAdapter?.updateDataList(featureMostUsedData?.first)
        mostUsedTagsRv.adapter = mostUsedTagsAdapter
        mostUsedTagsRv.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            var lastX = 0
            override fun onInterceptTouchEvent(
                rv: RecyclerView,
                e: MotionEvent
            ): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> lastX = e.x.toInt()
                    MotionEvent.ACTION_MOVE -> {
                        val isScrollingRight = e.x < lastX
                        mostUsedTagsRv.adapter?.let {
                            FeaturedFragment.mFeaturePager?.isUserInputEnabled =
                                isScrollingRight && (mostUsedTagsRv.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == it.itemCount - 1 || !isScrollingRight && (mostUsedTagsRv.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0
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


        mostUsedRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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
        mostUsedRv.adapter = mostUsedFramesAdapter

        /*stickyHeaderDecoration = StickyHeaderDecorationFeature(binding.root, showAd = {
            Log.d("FAHAD", "initRecyclerViews: Decoration mostUsedFramesAdapter $it")
            if (it) {
                nativeAd?.let {
                    if (_binding?.nativeContainer?.isVisible == false) {
                        mostUsedFramesAdapter?.hideRvAd()
                        mActivity.showNative(R.layout.native_ad_portrait_rv,
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
                mostUsedFramesAdapter?.showRvAd()
                _binding?.nativeContainer?.gone()
            }
        })
        stickyHeaderDecoration?.let { mostUsedRv.addItemDecoration(it) }*/
    }

    private fun initListener() {
        if (!mActivity.isNetworkAvailable()) {
            _binding?.searchBtn?.isVisible = false
            try {
                if (apiViewModel.offlineFeatureScreen.hasObservers()) {
                    apiViewModel.offlineFeatureScreen.removeObservers(this@MostUsedFragment)
                }
                apiViewModel.offlineFeatureScreen.observe(viewLifecycleOwner) {
                    when (it) {
                        is Response.Loading -> {
                            Log.d("Fahad", "initApiObservers: ")
                        }

                        is Response.ShowSlowInternet -> {}

                        is Response.Success -> {
                            mostUsedFramesAdapter?.clearData()
                            featureMostUsedData?.second?.forEach {
                                it?.let {
                                    val frame = FeatureRV.FrameModel(it)
                                    frame.isFavourite =
                                        favouriteFrames.mapNotNull { it?.id }
                                            .contains(frame.frame.id)
                                    mostUsedFramesAdapter?.updateSingleItem(frame)
                                }
                            }

                            if (isNativeLoaded) {
                                mostUsedFramesAdapter?.updateAd(nativeAd)
                            }
                            listUpdated = true
                        }

                        is Response.Error -> {
                            Log.d("Fahad", "initApiObservers: ")
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }else{
            _binding?.searchBtn?.isVisible = true
        }
        try {
            if (apiViewModel.featureScreen.hasObservers()) {
                apiViewModel.featureScreen.removeObservers(this@MostUsedFragment)
            }
            apiViewModel.featureScreen.observe(viewLifecycleOwner) {
                when (it) {
                    is Response.Loading -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }

                    is Response.ShowSlowInternet -> {}

                    is Response.Success -> {
                        mostUsedFramesAdapter?.clearData()
                        featureMostUsedData?.second?.forEach {
                            it?.let {
                                val frame = FeatureRV.FrameModel(it)
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                                mostUsedFramesAdapter?.updateSingleItem(frame)
                            }
                        }

                        if (isNativeLoaded) {
                            mostUsedFramesAdapter?.updateAd(nativeAd)
                        }
                        listUpdated = true

                        /*featureMostUsedData?.second?.filterNotNull()?.map { frame ->
                            FeatureRV.FrameModel(frame)
                        }?.let {
                            it.map { frame ->
                                frame.isFavourite =
                                    favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                            }
                            mostUsedFramesAdapter?.updateDataList(it)
                        }*/
                    }

                    is Response.Error -> {
                        Log.d("Fahad", "initApiObservers: ")
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun selectTags(tag: TagsRVAdapter.TagModel) {
        mostUsedSelectedTags.apply {
            if (tag.mSelected) {
                add(tag)
                mostUsedFramesAdapter?.getCurrentDataList()?.let {
                    val sortedList = it.sortedWith(compareByDescending {
                        it.frame.hashtag?.mapNotNull { it?.title }?.contains(tag.tag)
                    })
                    sortedList.map { frame ->
                        frame.isFavourite =
                            favouriteFrames.mapNotNull { it?.id }.contains(frame.frame.id)
                    }
                    mostUsedFramesAdapter?.clearData()
                    sortedList.forEach {
                        mostUsedFramesAdapter?.updateSingleItem(it)
                    }
                    if (isNativeLoaded) {
                        mostUsedFramesAdapter?.updateAd(nativeAd)
                    }
                    listUpdated = true
                    //mostUsedFramesAdapter?.updateDataList(sortedList)
                    _binding?.mostUsedRv?.smoothScrollToPosition(0)
                }
            } else remove(tag)
        }
    }
}