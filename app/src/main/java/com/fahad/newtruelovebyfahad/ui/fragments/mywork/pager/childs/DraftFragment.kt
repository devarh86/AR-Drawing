package com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.ads.Constants
import com.example.ads.admobs.utils.showInterstitial
import com.example.analytics.Constants.firebaseAnalytics
import com.example.analytics.Constants.parentScreen
import com.example.analytics.Events
import com.fahad.newtruelovebyfahad.databinding.FragmentDraftBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.adapter.MyWorkRV
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.isNetworkAvailable
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.showToast
import com.project.common.utils.ConstantsCommon
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "DraftFragment"

@AndroidEntryPoint
class DraftFragment : Fragment() {

    private var _binding: FragmentDraftBinding? = null
    private val binding get() = _binding!!
    private var draftAdapter: MyWorkRV? = null
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDraftBinding.inflate(inflater, container, false)
        _binding?.initViews()
        return binding.root
    }

    private fun FragmentDraftBinding.initViews() {
        initRecyclerView()
        initObservers()
        initListener()
    }

    private fun FragmentDraftBinding.initListener() {
        tryNowBtn.setSingleClickListener {
            navController?.navigateUp()
        }
    }

    private fun FragmentDraftBinding.initObservers() {

    }

    private fun FragmentDraftBinding.initRecyclerView() {
        draftAdapter = MyWorkRV(
            mContext, arrayListOf()
        ) { frameBody ->
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            ) else arrayOf(

                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            Log.d(TAG, "initRecyclerView: frameBody $frameBody")

            (mActivity as Permissions).checkAndRequestPermissions(*permissions, action = {
                mContext?.let {
                    if (it.isNetworkAvailable()) {
                        parentScreen = Events.SubScreens.DRAFT
                        firebaseAnalytics?.logEvent(
                            Events.SubScreens.DRAFT,
                            Bundle().apply {
                                putString(
                                    Events.ParamsKeys.SUB_SCREEN,
                                    Events.SubScreens.DRAFT
                                )
                                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                                putString(
                                    Events.ParamsKeys.FROM,
                                    Events.ParamsValues.RECYCLER_VIEW
                                )
                                putString(Events.ParamsKeys.FRAME_ID, frameBody.id.toString())
                            })
                        mActivity?.showInterstitial(
                            loadedAction = {
                                ConstantsCommon.resetCurrentFrames()
                                ConstantsCommon.isDraft = true
                                ConstantsCommon.parentId = frameBody.id
                                ConstantsCommon.type = frameBody.type
                                ConstantsCommon.editor = frameBody.editor
                                ConstantsCommon.selectedId = frameBody.selectedId
                                /*val intent = Intent(
                                    it,
                                    when (frameBody.editor) {
                                        "photo_editor" -> PhotoEditorActivity::class.java
                                        MainMenuOptions.BLEND.title -> BlendEffectEditorActivity::class.java
                                        else -> BlendEffectEditorActivity::class.java
                                    }
                                )
                                activity?.let {
                                    if (it is MainActivity) {
                                        it.getActivityLauncher().launch(intent)
                                    }
                                }*/
                            },
                            failedAction = {
                                ConstantsCommon.resetCurrentFrames()
                                ConstantsCommon.isDraft = true
                                ConstantsCommon.parentId = frameBody.id
                                ConstantsCommon.type = frameBody.type
                                ConstantsCommon.ratio = frameBody.ratio
                                ConstantsCommon.editor = frameBody.editor
                                ConstantsCommon.selectedId = frameBody.selectedId
                                /*val intent = Intent(
                                    it,
                                    when (frameBody.editor) {
                                        "photo_editor" -> PhotoEditorActivity::class.java
                                        MainMenuOptions.BLEND.title -> BlendEffectEditorActivity::class.java
                                        else -> BlendEffectEditorActivity::class.java
                                    }
                                )
                                //  activityLauncher.launch(intent)
                                activity?.let {
                                    if (it is MainActivity) {
                                        it.getActivityLauncher().launch(intent)
                                    }
                                }*/
                            }, showAd = Constants.showDraftFrameClickAd, onCheck = true
                        )
                    } else {
                        it.showToast("Connect to Internet!")
                    }
                }
            }, declineAction = {})
        }

        draftRv.adapter = draftAdapter
    }

}