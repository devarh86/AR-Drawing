package com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs

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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.ads.Constants.showRecentlyUsedFrameClickAd
import com.example.analytics.Events
import com.fahad.newtruelovebyfahad.databinding.FragmentRecentlyUsedBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.adapter.RecentRV
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.repo.room.helper.RecentTypeConverter
import com.project.common.utils.getProScreen
import com.project.common.viewmodels.ApiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentlyUsedFragment : Fragment() {

    private var _binding: FragmentRecentlyUsedBinding? = null
    private val binding get() = _binding!!
    private var downloadDialog: BottomSheetDialog? = null

    private val apiViewModel by activityViewModels<ApiViewModel>()
    private var recentAdapter: RecentRV? = null
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    private val activityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
                if (backDecision) {
                    (mActivity as? MainActivity)?.showHomeScreen()
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
        recentAdapter = RecentRV(
            mContext,
            emptyList()
        ) { frameBody ->
            RecentTypeConverter.fromJson(frameBody.frame)?.let { frameBodyNew ->
                if (mActivity != null && mActivity is MainActivity) {
                    (mActivity as MainActivity).frameClick(
                        FrameObject(
                            frameBodyNew.id,
                            frameBodyNew.title,
                            Events.SubScreens.RECENTLY_USED,
                            "",
                            Events.SubScreens.RECENTLY_USED,
                            frameBodyNew.tags ?: "",
                            frameBodyNew.baseUrl ?: "",
                            frameBodyNew.thumb,
                            frameBodyNew.thumbtype,
                            showRecentlyUsedFrameClickAd,
                            false,
                            frameBodyNew,
                            "list"
                        )
                    ) {}
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecentlyUsedBinding.inflate(inflater, container, false)
        _binding?.initViews()

        return binding.root
    }

    private fun FragmentRecentlyUsedBinding.initViews() {
        initObservers()
        initRecyclerViews()
        initListeners()
    }

    private fun FragmentRecentlyUsedBinding.initListeners() {
        tryNowBtn.setSingleClickListener {
            navController?.navigateUp()
        }
    }

    private fun FragmentRecentlyUsedBinding.initRecyclerViews() {
        recentlyRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
           /* override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                runCatching {
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager?
                    val firstVisibleItemPositions: IntArray? =
                        layoutManager?.findFirstVisibleItemPositions(null)
                    if (firstVisibleItemPositions != null && firstVisibleItemPositions.isNotEmpty()) {
                        firstVisibleItemPositions[0].let { position ->
                            if (position == 0 && (layoutManager.findViewByPosition(0)?.top ?: -1) >= 0) {
                                if (mActivity is MainActivity) {
                                    (mActivity as? MainActivity)?.showBottomBar()
                                }
                            } else {
                                if (mActivity is MainActivity) {
                                    (mActivity as? MainActivity)?.hideBottomBar()
                                }
                            }
                        }
                    } else {
                        if (mActivity is MainActivity) {
                            (mActivity as? MainActivity)?.hideBottomBar()
                        }
                    }
                }
            }*/
        })
        recentlyRv.adapter = recentAdapter
    }

    fun hideAds() {
        if (_binding?.recentlyRv?.isComputingLayout == false) {
            recentAdapter?.notifyDataSetChanged()
        }
    }

    private fun FragmentRecentlyUsedBinding.initObservers() {
        try {
            apiViewModel.recentsFrames.observe(viewLifecycleOwner) {
                it?.let {
                    recentAdapter?.updateList(it.reversed())
                    if (it.isEmpty()) {
                        tryNowPlaceholder.visible()
                        noResultFoundTv.visible()
                        tryNowBtn.visible()
                        recentlyRv.gone()
                    } else {
                        tryNowPlaceholder.gone()
                        noResultFoundTv.gone()
                        tryNowBtn.gone()
                        recentlyRv.visible()
                    }
                }
            }
        } catch (_: Exception) {
            Log.d("FAHAD", "initObservers: crash")
        }
    }

    override fun onPause() {
        super.onPause()
        downloadDialog?.apply { if (isShowing) dismiss() }
    }
}