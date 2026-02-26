package com.fahad.newtruelovebyfahad.ui.fragments.mywork.pager.childs

import android.Manifest
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.analytics.Constants
import com.example.analytics.Events
import com.fahad.newtruelovebyfahad.databinding.FragmentSavedBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.activities.save.SaveAndShareActivity
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.MyWorkViewModel
import com.fahad.newtruelovebyfahad.ui.fragments.mywork.adapter.SavedRV
import com.fahad.newtruelovebyfahad.utils.Permissions
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.showToast
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private var savedAdapter: SavedRV? = null
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private val viewModel by activityViewModels<MyWorkViewModel>()
    private var deletedImageUri: Uri? = null
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
    private var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deleteUriFromExternalStorage(deletedImageUri ?: return@launch)
                    }
                }
                mContext?.let {
                    mActivity?.checkAndRequestPermissionsNow(
                        action = {
                            viewModel.getImages(it)
                            it.showToast("Photo deleted successfully")
                        },
                        declineAction = {},
                    )
                }
            } else {
                mContext?.showToast("Photo couldn't be deleted")
            }
        }

    private suspend fun deleteUriFromExternalStorage(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                mActivity?.checkAndRequestPermissionsNow(
                    action = {
                        mActivity?.contentResolver?.delete(fileUri, null, null)
                        mContext?.let { viewModel.getImages(it) }
                    },
                    declineAction = {},
                )
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        mActivity?.let {
                            MediaStore.createDeleteRequest(
                                it.contentResolver,
                                listOf(fileUri)
                            ).intentSender
                        }
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }

                    else -> {
                        deleteFile(fileUri.path)
                        null
                    }
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }

    private fun deleteFile(path: String?) {
        val file = path?.let { File(it) }
        if (file?.exists() == true) {
            if (file.delete()) {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(path),
                    null
                ) { _, _ ->
                    mContext?.let {
                        mActivity?.checkAndRequestPermissionsNow(
                            action = {
                                viewModel.getImages(it)
                            },
                            declineAction = {},
                        )
                    }
                }
                mContext?.showToast("Photo deleted successfully")
            } else {
                mContext?.showToast("Photo couldn't be deleted")
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
        mActivity?.let {
            savedAdapter = SavedRV(
                it,
                emptyList(),
                myWorkRVCallback = {
                    mActivity?.let { activity ->
                        Constants.firebaseAnalytics?.logEvent(
                            Events.Screens.MY_WORK,
                            Bundle().apply {
                                putString(Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SAVED)
                                putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                                putString(Events.ParamsKeys.FROM, Events.ParamsValues.RECYCLER_VIEW)
                            })
                        try {
                            val intent = Intent(activity, SaveAndShareActivity::class.java)
                            intent.putExtra("from_saved", true)
                            intent.putExtra("image_path", it.toString())
                            if (activity is MainActivity) {
                                activity.getActivityLauncher().launch(intent)
                            }
                            //   activityLauncher.launch(intent)
                        } catch (_: Exception) {
                        }
                        //startActivity(intent)
                    }
                },
                myWorkRVShareCallback = {
                    Constants.firebaseAnalytics?.logEvent(Events.Screens.MY_WORK, Bundle().apply {
                        putString(
                            Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SAVED
                        )
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                        putString(Events.ParamsKeys.FROM, Events.ParamsValues.RECYCLER_VIEW)
                        putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.SHARE)
                    })
                    mActivity?.shareImage(it.toString())
                },
                myWorkRVDeleteCallback = {
                    Constants.firebaseAnalytics?.logEvent(Events.Screens.MY_WORK, Bundle().apply {
                        putString(
                            Events.ParamsKeys.SUB_SCREEN, Events.SubScreens.SAVED
                        )
                        putString(Events.ParamsKeys.ACTION, Events.ParamsValues.CLICKED)
                        putString(Events.ParamsKeys.FROM, Events.ParamsValues.RECYCLER_VIEW)
                        putString(Events.ParamsKeys.BUTTON, Events.ParamsValues.DELETE)
                    })
                    deletedImageUri = it
                    lifecycleScope.launch {
                        deleteUriFromExternalStorage(it)
                    }
                }
            )
        }
    }

    private fun Activity.shareImage(uri: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri.toUri())
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "${getString(com.project.common.R.string.share_text)} https://play.google.com/store/apps/details?id=$packageName"
            )
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (_: Exception) {
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        _binding?.initViews()

        Events.SubScreens.DRAFT
        return binding.root
    }

    private fun FragmentSavedBinding.initViews() {
        mActivity?.checkAndRequestPermissionsNow(
            action = {
                mContext?.let {
                    viewModel.getImages(it)
                }
            },
            declineAction = {},
        )
        initRecyclerView()
        initObservers()
        initListeners()
    }

    private fun FragmentSavedBinding.initListeners() {
        tryNowBtn.setOnSingleClickListener { navController?.navigateUp() }
        backImg.setOnSingleClickListener { navController?.navigateUp() }
    }

    private fun FragmentSavedBinding.initObservers() {
        viewModel.imageList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                mContext?.let { context ->
                    savedAdapter?.updateList(context, it)
                }
                tryNowPlaceholder.gone()
                noResultFoundTv.gone()
                tryNowBtn.gone()
                savedRv.visible()
            } else {
                tryNowPlaceholder.visible()
                noResultFoundTv.visible()
                tryNowBtn.visible()
                savedRv.gone()
            }
        }
    }

    private fun FragmentSavedBinding.initRecyclerView() {
        savedRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        savedRv.adapter = savedAdapter
    }

    private fun Activity.checkAndRequestPermissionsNow(
        action: () -> Unit,
        declineAction: () -> Unit
    ) {
        val appPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA)
        else arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
        (mActivity as? Permissions)?.checkAndRequestPermissions(
            appPermissions = appPermissions,
            action = action,
            declineAction = declineAction
        )
    }
}