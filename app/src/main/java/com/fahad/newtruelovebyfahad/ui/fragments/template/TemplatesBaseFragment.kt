package com.fahad.newtruelovebyfahad.ui.fragments.template

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.Constants.showCategoriesFrameTemplateClickAd

import com.example.ads.admobs.utils.onPauseBanner
import com.example.ads.admobs.utils.onResumeBanner
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentBaseTemplatesBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.FrameObject
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.home.adapter.FrameRecyclerAdapterHomeParent
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.navigateFragment
import com.fahad.newtruelovebyfahad.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.project.common.datastore.FrameDataStore
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.isNetworkAvailable
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.getEventCategoryName
import com.project.common.viewmodels.ApiViewModel
import com.project.common.viewmodels.HomeAndTemplateViewModel
import com.project.common.viewmodels.ViewStates
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TemplatesBaseFragment : Fragment() {

    private var _binding: FragmentBaseTemplatesBinding? = null
    private val binding get() = _binding!!

    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null

    @Inject
    lateinit var frameDataStore: FrameDataStore
    private var recyclerParentAdapter: FrameRecyclerAdapterHomeParent? = null
    private val homeAndTemplateViewModel by activityViewModels<HomeAndTemplateViewModel>()

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
         Manifest.permission.READ_MEDIA_IMAGES
    )
    else arrayOf(

        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private val activityLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val backDecision = result.data?.getBooleanExtra("backpress", false) ?: false
            if (backDecision) {
                activity?.let {
                    if (it is MainActivity) {
                        it.showHomeScreen()
                    }
                }
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

        eventForGalleryAndEditor(Events.Screens.TEMPLATE_BASE, "", true)

        parentScreen = Events.Screens.TEMPLATE_BASE

        kotlin.runCatching {
            setFragmentResultListener("show_home_screen_template") { _, bundle ->
                val showHomeScreen = bundle.getBoolean("show_home_screen_template")
                Log.i("TAG", "onCreate featurefragment: $showHomeScreen")
                if (showHomeScreen) {
                    activity?.let {
                        if (it is MainActivity) {
                            it.showHomeScreen()
                        }
                    }
                }
            }
        }

        recyclerParentAdapter = FrameRecyclerAdapterHomeParent(onCategorySeeAllClick = {

            kotlin.runCatching {

                if(mActivity != null && mActivity is MainActivity){
                    (mActivity as MainActivity).eventForCategoryClick(FrameObject(
                        screenName = Events.Screens.TEMPLATE_BASE,
                        categoryName = it.lowercase(),
                        from = "see_all",
                        frameBody = ""
                    ))
                }

                val direction = TemplatesBaseFragmentDirections.actionNavTemplatesBaseToNavTemplates(it.lowercase())
                activity?.navigateFragment(
                    direction,
                    R.id.nav_templates_base
                )
            }
        }, onThumbClick = { frameBody, position, _, tagTitle, categoryName ->
//Toast.makeText(requireContext(), "${frameBody.id} ${frameBody.title}", Toast.LENGTH_SHORT).show()
            if (mActivity != null && mActivity is MainActivity) {
                (mActivity as MainActivity).frameClick(
                    FrameObject(
                        frameBody.id,
                        frameBody.title,
                        Events.Screens.TEMPLATE_BASE,
                        "",
                        categoryName.getEventCategoryName(),
                        frameBody.tags ?: "",
                        frameBody.baseUrl ?: "",
                        frameBody.thumb,
                        frameBody.thumbtype,
                        showCategoriesFrameTemplateClickAd,
                        true,
                        frameBody,
                        "list"
                    )
                ) {
                    if (position > -1) recyclerParentAdapter?.updateNotifyChangesForChild(
                        position
                    )
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBaseTemplatesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        _binding?.templateParentRecyclerView?.adapter = null
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeAndTemplateViewModel.currentScreen = "template"

        binding.initViews()

        kotlin.runCatching {
            _binding?.templateParentRecyclerView?.apply {
                setHasFixedSize(true)
                itemAnimator = null
                adapter = recyclerParentAdapter
            }
        }
        initObservers()
    }

    private var visibleAd = false

    @SuppressLint("NotifyDataSetChanged")
    fun hideScreenAds() {
        visibleAd = true
        if (isProVersion()) {
            beforePro = true
        }
        _binding?.bannerContainer?.gone()
    }

    fun showScreenAds() {
        visibleAd = false
        _binding?.bannerContainer?.visible()
    }

    private fun FragmentBaseTemplatesBinding.initViews() {}

    private var beforePro = false

    @SuppressLint("NotifyDataSetChanged")
    private fun initObservers() {

        try {
            if (homeAndTemplateViewModel.templateScreen.hasObservers()) {
                homeAndTemplateViewModel.templateScreen.removeObservers(this)
            }
            homeAndTemplateViewModel.templateScreen.observe(viewLifecycleOwner) { it ->

                when (it) {

                    is ViewStates.Idle -> {
                        Log.i("TAG", "initLiveData: idle")

                    }

                    is ViewStates.Loading -> {
                        Log.i("TAG", "initLiveData: loading")

                        Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                    }

                    is ViewStates.Error -> {
                        Log.i("TAG", "initLiveData: Error")

                        kotlin.runCatching {
                            _binding?.apply {
                                if (!isNetworkAvailable && templateParentRecyclerView.isVisible) {
                                    tryNowPlaceholder.visible()
                                    noResultFoundTv.visible()
                                    templateParentRecyclerView.invisible()
                                } else {
//                                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    is ViewStates.UpdateObject -> {
                        Log.i("TAG", "initLiveData: UpdateObject")
                        kotlin.runCatching {
                            recyclerParentAdapter?.let { adapter ->
                                if (adapter.items.isNotEmpty() && adapter.items.last() != it.objectValue) {
                                    recyclerParentAdapter?.addItem(it.objectValue)
                                } else if (adapter.items.isEmpty()) {
                                    recyclerParentAdapter?.addItem(it.objectValue)
                                }
                                it
                            }
                        }
                    }

                    is ViewStates.Offline -> {
                        Log.i("TAG", "initLiveData: Offline")
                        kotlin.runCatching {
                            recyclerParentAdapter?.setList(it.list)

                            _binding?.apply {

                                if (templateParentRecyclerView.isVisible) {
                                    tryNowPlaceholder.visible()
                                    noResultFoundTv.visible()
                                    templateParentRecyclerView.invisible()
                                }
                            }
                        }
                    }

                    is ViewStates.UpdateList -> {
                        Log.i("TAG", "initLiveData: UpdateList")

                        kotlin.runCatching {
                            val list = it.list
                            recyclerParentAdapter?.let {
                                if (isNetworkAvailable && it.items.size != list.size) {
                                    it.setList(list)
                                } else if (beforePro || ConstantsCommon.notifyAdapterForRewardedAssets) {
                                    ConstantsCommon.notifyAdapterForRewardedAssets = false
                                    beforePro = false
                                    it.setList(list)
                                }

                                if (isNetworkAvailable) {
                                    _binding?.apply {
                                        if (!templateParentRecyclerView.isVisible) {
                                            tryNowPlaceholder.gone()
                                            noResultFoundTv.gone()
                                            templateParentRecyclerView.visible()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isProVersion()) {
            _binding?.let {
                if (!visibleAd)
                    _binding?.bannerContainer?.visible()
                mActivity?.onResumeBanner(
                    binding.adBannerContainer,
                    binding.crossBannerIv,
                    binding.bannerLayout.adContainer,
                    binding.bannerLayout.shimmerViewContainer
                )
            }
        } else {
            _binding?.bannerContainer?.gone()
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseBanner()
    }
}