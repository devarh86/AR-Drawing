package com.fahad.newtruelovebyfahad.ui.fragments.favourite

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.ads.Constants.showFavouriteFrameClickAd
import com.example.ads.Constants.showRewardAdFavourite
import com.example.analytics.Events
import com.fahad.newtruelovebyfahad.databinding.FragmentFavouriteBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.feature.adapter.FeatureRV
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.repo.room.helper.FavouriteTypeConverter
import com.project.common.repo.room.model.FavouriteModel
import com.project.common.utils.ConstantsCommon.favouriteFrames
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavouriteFragment : Fragment() {

    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    private var favouriteFramesAdapter: FeatureRV? = null
    private val apiViewModel by activityViewModels<ApiViewModel>()
    private var downloadDialog: BottomSheetDialog? = null
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
        navController = findNavController()
        favouriteFramesAdapter = FeatureRV(
            mContext,
            arrayListOf(),
            nativeAd,
            onClick = { frameBody, position ->

                if (mActivity!= null && mActivity is MainActivity) {
                    (mActivity as MainActivity).frameClick(
                        FrameObject(
                            frameBody.id,
                            frameBody.title,
                            Events.Screens.FAVOURITE,
                            "",
                            Events.Screens.FAVOURITE,
                            frameBody.tags ?: "",
                            frameBody.baseUrl ?: "",
                            frameBody.thumb,
                            frameBody.thumbtype,
                            showFavouriteFrameClickAd,
                            showRewardAdFavourite,
                            frameBody,
                            "list"
                        )
                    ) {
                        if (position > -1) favouriteFramesAdapter?.notifyItemChanged(
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
                    favouriteFramesAdapter?.updateAd(nativeAd)
                }
                isNativeLoaded = true
            },
            failedAction = {
                nativeAd = null
                isNativeLoaded = false
            }
        )*/
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hideAd(){
        if(_binding?.favouriteRv?.isComputingLayout == false) {
            favouriteFramesAdapter?.notifyDataSetChanged()
        }
    }

    private var isNativeLoaded = false
    private var listUpdated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        _binding?.initViews()
        return binding.root
    }

    private fun FragmentFavouriteBinding.initViews() {
        initRecyclerViews()
        initObservers()
        initListeners()
    }

    private fun FragmentFavouriteBinding.initListeners() {

        tryNowBtn.setSingleClickListener {
            navController?.navigateUp()
        }
    }

    private fun FragmentFavouriteBinding.initObservers() {
        try {
            apiViewModel.favouriteFrames.observe(viewLifecycleOwner) {
                it?.let {
                    if (it.isNotEmpty()) {
                        placeholderContainer.gone()
                        favouriteRv.visible()
                        favouriteFrames =
                            it.mapNotNull { FavouriteTypeConverter.fromJson(it.frame) }
                        favouriteFrames.filterNotNull().map { frame ->
                            FeatureRV.FrameModel(frame, true)
                        }.let {
                            favouriteFramesAdapter?.clearData()
                            it.forEach {
                                favouriteFramesAdapter?.updateSingleItem(it)
                            }
                            if (isNativeLoaded) {
                                favouriteFramesAdapter?.updateAd(nativeAd)
                            }
                            listUpdated = true
                        }
                    } else {
                        favouriteFrames = emptyList()
                        placeholderContainer.visible()
                        favouriteRv.gone()
                    }
                } ?: run {
                    favouriteFrames = emptyList()
                    placeholderContainer.visible()
                    favouriteRv.gone()
                }
            }
        } catch (_: Exception) {
            Log.d("FAHAD", "initObservers: crash")
        }
    }

    private fun FragmentFavouriteBinding.initRecyclerViews() {
        favouriteRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                runCatching {
//                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager?
//                    val firstVisibleItemPositions: IntArray? =
//                        layoutManager?.findFirstVisibleItemPositions(null)
//                    if (firstVisibleItemPositions != null && firstVisibleItemPositions.isNotEmpty()) {
//                        firstVisibleItemPositions[0].let { position ->
//                            if (position == 0 && (layoutManager.findViewByPosition(0)?.top ?: -1) >= 0) {
//                                if (mActivity is MainActivity) {
//                                    (mActivity as? MainActivity)?.showBottomBar()
//                                }
//                            } else {
//                                if (mActivity is MainActivity) {
//                                    (mActivity as? MainActivity)?.hideBottomBar()
//                                }
//                            }
//                        }
//                    } else {
//                        if (mActivity is MainActivity) {
//                            (mActivity as? MainActivity)?.hideBottomBar()
//                        }
//                    }
//                }
//            }
        })
        favouriteRv.adapter = favouriteFramesAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        downloadDialog?.apply { if (isShowing) dismiss() }
        _binding = null
    }

    /*fun hideScreenAds() {
        _binding?.bannerContainer?.invisible()
    }

    fun showScreenAds() {
        _binding?.bannerContainer?.visible()
    }*/
}