package com.fahad.newtruelovebyfahad.ui.activities.save

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.ads.Constants
import com.example.ads.Constants.enablePopUpSave
import com.example.ads.Constants.languageCode
import com.example.ads.admobs.utils.loadAppOpen
import com.example.ads.admobs.utils.loadNewInterstitial
import com.example.ads.admobs.utils.onPauseBanner
import com.example.ads.admobs.utils.showAppOpen
import com.example.ads.utils.interstitialSave
import com.example.inapp.helpers.Constants.isProVersion
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.databinding.SaveCarousalBinding
import com.project.common.utils.ConstantsCommon
import com.project.common.utils.ConstantsCommon.fromSaveAndShare
import com.project.common.utils.ConstantsCommon.isSavedScreenHomeClicked
import com.project.common.utils.ConstantsCommon.saveSession
import com.project.common.utils.createOrShowSnackBarSaved
import com.project.common.utils.eventForGalleryAndEditor
import com.project.common.utils.hideNavigation
import com.project.common.utils.setLocale
import com.project.common.utils.setOnSingleClickListener
import com.project.common.viewmodels.DataStoreViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class SaveAndShareActivity : AppCompatActivity() {

    private var _binding: SaveCarousalBinding? = null

    //  private var _binding: SaveUpdatedUiBinding? = null
    private val binding get() = _binding!!
    private var imagePath: String = ""
    private var imageList: MutableList<String> = mutableListOf()
    private var fromEditor: String = ""
    private var fromSaved: Boolean = false
    var showAppOpen = false
    private var callback: OnBackPressedCallback? = null
    private var startForResult: ActivityResultLauncher<Intent>? = null
    private var alreadyShownSaved = false
    private var eventScreenName = ""
    private val dataStoreViewModel by viewModels<DataStoreViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = SaveCarousalBinding.inflate(layoutInflater)
        // _binding = SaveUpdatedUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kotlin.runCatching {
            setLocale(languageCode)
        }

        onBackPress()
        imagePath = intent.getStringExtra("image_path") ?: ""
        imageList.addAll(intent.getStringArrayListExtra("imagePathsMultiFit") ?: emptyList())
        fromEditor = intent.getStringExtra("from_editor") ?: ""
        fromSaved = intent.getBooleanExtra("from_saved", false)

        if (!fromSaved) {

            fromEditor.let { editor ->
                if (editor.isNotEmpty() && enablePopUpSave) {
                    //showPopUp(editor)
                }
            }
            eventScreenName = "save_new"
            eventForGalleryAndEditor("save_new", "", true)
        } else {
            eventScreenName = "from_saved"
            eventForGalleryAndEditor("from_saved", "", true)
        }

        Constants.showAppOpen = true

        kotlin.runCatching {
            startForResult = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        if (!fromSaved && !alreadyShownSaved) {
                            alreadyShownSaved = true
                            createOrShowSnackBarSaved(
                                _binding?.root,
                                "Image saved successfully in gallery",
                                false,
                                marginTop = _binding?.root?.marginTop ?: 0
                            )
                        }
                    } catch (ex: java.lang.Exception) {
                        Log.e("TAG", "showSaveSuccess: ", ex)
                    }
                }
            }
        }
        init()
        _binding?.initClick()
        runCatching {
            /*   if (!isProVersion() && saveSession != 2 && Constants.showProSave) {
                   val intent = Intent()
                   intent.putExtra("image_path", imagePath)
                   intent.setClassName(
                       applicationContext,
                       "com.fahad.newtruelovebyfahad.ui.activities.pro.ProCarousal"
                   )
                   startForResult?.launch(intent)
               } else {
               }*/
        }
        hideNavigation()
    }

    private fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
        observe(owner, object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        })
    }

    private var isPopUpShown = false


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            hideNavigation()
        }
    }

    fun showAppOpenAd() {
        _binding?.let {

            Handler(Looper.getMainLooper()).postDelayed({
                showAppOpen {
                    _binding?.let {
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadAppOpen()
                            showAppOpen = false
                        }, 800L)
                    }
                }
            }, 600L)
        }
    }


    override fun onPause() {
        super.onPause()
        onPauseBanner()
    }

    private fun onBackPress() {

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isSavedScreenHomeClicked = false
                backPress("back")
            }
        }
        callback?.let { onBackPressedDispatcher.addCallback(this@SaveAndShareActivity, it) }
    }

    private fun backPress(clickFrom: String) {
        kotlin.runCatching {
            val resultIntent = Intent()
            if (fromSaved) {
                if (clickFrom == "back") {
                    resultIntent.putExtra("backpress", false)

                } else {
                    resultIntent.putExtra("where", clickFrom)
                }

            } else {
                if (clickFrom == "back") fromSaveAndShare = true
                loadNewInterstitial(interstitialSave()) {}
                resultIntent.putExtra("where", clickFrom)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupDotsIndicator(
        count: Int,
    ) {
        kotlin.runCatching {
            _binding?.indicatorShare?.removeAllViews()

            if (count <= 1) {
                _binding?.indicatorShare?.gone()
                return
            }

            _binding?.indicatorShare?.visible()
            val dots = arrayOfNulls<ImageView>(count)

            for (i in 0 until count) {
                dots[i] = ImageView(this@SaveAndShareActivity)
                dots[i]?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@SaveAndShareActivity,
                        com.project.common.R.drawable.s_unselected
                    )
                )
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(6, 0, 6, 0)
                _binding?.indicatorShare?.addView(dots[i], params)
            }

            // Set first dot as active
            if (dots.isNotEmpty()) {
                dots[0]?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@SaveAndShareActivity,
                        com.project.common.R.drawable.s_selected
                    )
                )
            }

            // Register page change callback
            _binding?.viewPager?.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    // Update dots
                    for (i in dots.indices) {
                        dots[i]?.setImageDrawable(
                            ContextCompat.getDrawable(
                                this@SaveAndShareActivity,
                                if (i == position) com.project.common.R.drawable.s_selected
                                else com.project.common.R.drawable.s_unselected
                            )
                        )
                    }
                }
            })
        }

    }

    private fun init() {

        this.let {
            _binding?.shimmerView?.isVisible = true
            _binding?.savedImage?.isVisible = true
            _binding?.let { binding ->
                Glide.with(it).load(imagePath).apply {
                    into(binding.savedImage)
                    binding.savedImage.viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            _binding?.let { binding ->
                                if (binding.savedImage.height > 50 && binding.savedImage.width > 50) {
                                    binding.savedImage.viewTreeObserver.removeOnGlobalLayoutListener(
                                        this
                                    )
                                    binding.shimmerView.isVisible = false
                                    binding.shimmerView.stopShimmer()
                                    if (!fromSaved && !alreadyShownSaved && saveSession == 2 || !fromSaved && isProVersion() && alreadyShownSaved) {
                                        alreadyShownSaved = true
                                        createOrShowSnackBarSaved(
                                            _binding?.root,
                                            "Image saved successfully in gallery",
                                            false,
                                            marginTop = _binding?.root?.marginTop ?: 0
                                        )
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }


    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun SaveCarousalBinding.initClick() {

        tikokShare.setOnSingleClickListener {

            try {
                eventForGalleryAndEditor(eventScreenName, "tiktok_button", true)
                shareImageToApp(imagePath, "com.zhiliaoapp.musically")
            } catch (ex: java.lang.Exception) {
                Log.e("error", "initClick: ", ex)
            }
        }

        instaShare.setOnSingleClickListener {
            try {
                eventForGalleryAndEditor(eventScreenName, "insta_button", true)
                try {
                    eventForGalleryAndEditor(eventScreenName, "insta_button", true)
                    // Try full Instagram first, else fallback to Lite
//                    val instagramPackages = listOf(
//                        "com.instagram.android",        // full app
//                        "com.instagram.lite",           // lite version 1
//                        "com.instagram.android.lite"    // lite version 2 (common)
//                    )
                    when {
                        isAppInstalled(this@SaveAndShareActivity, "com.instagram.android") ->
                            shareImageToApp(imagePath, "com.instagram.android")
                        //   isAppInstalled(this@SaveAndShareActivity, "com.instagram.lite") ->
                        isAppInstalled(this@SaveAndShareActivity, "com.instagram.android.lite") ->
                            //    shareImageToApp(imagePath, "com.instagram.lite")
                            shareImageToApp(imagePath, "com.instagram.android.lite")

                        else -> Toast.makeText(
                            this@SaveAndShareActivity,
                            "Instagram not installed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (ex: Exception) {
                    Log.e("error", "initClick: ", ex)
                }
                // shareImageToApp(imagePath, "com.instagram.android")
            } catch (ex: java.lang.Exception) {
                Log.e("error", "initClick: ", ex)
            }
        }





        homeButton.setOnSingleClickListener {
            ConstantsCommon.fromSaved = true
            eventForGalleryAndEditor(eventScreenName, "home", true)
            isSavedScreenHomeClicked = true
            backPress("home")

            /*   kotlin.runCatching {
                       val resultIntent = Intent()
                       resultIntent.putExtra("backpress", true)
                       setResult(RESULT_OK, resultIntent)
                       finish()

               }*/
        }
        seamlessBtn.setOnSingleClickListener {
            backPress("carousal")
        }
        collageBtnN.setOnSingleClickListener {
            backPress("collage")
        }
        storyBtn.setOnSingleClickListener {
            backPress("stories")
        }

        stitchBtn.setOnSingleClickListener {
            backPress("stitch")
        }

        backButton.setOnSingleClickListener {
            backPress("back")
        }


        moreShare.setOnSingleClickListener {
            try {
                eventForGalleryAndEditor(eventScreenName, "more", true)
                shareImage(imagePath)
            } catch (ex: java.lang.Exception) {
                Log.e("error", "initClick: ", ex)
            }
        }


    }

    private fun Activity.shareImage(uri: String) {
        runOnUiThread {
            kotlin.runCatching {
                val newUri = if (fromSaved) {
                    uri.toUri()
                } else {
                    if (uri.isBlank()) {
                        if (imageList.isNotEmpty()) {
                            shareImages(imageList, false)
                        }
                        return@runCatching
                    }
                    val authority = "${packageName}.provider"
                    FileProvider.getUriForFile(this, authority, File(uri))
                }
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "image/*"
                // shareIntent.putExtra(Intent.EXTRA_STREAM, uri.toUri())
                shareIntent.putExtra(Intent.EXTRA_STREAM, newUri)
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${getString(com.project.common.R.string.share_text)} https://play.google.com/store/apps/details?id=$packageName"
                )
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }
    }

    private fun Activity.shareImageToApp(uri: String, appPackage: String) {
        runOnUiThread {
            kotlin.runCatching {
                val newUri = if (fromSaved) {
                    uri.toUri()
                } else {
                    if (uri.isBlank()) {
                        if (imageList.isNotEmpty()) {
                            shareImagesToApp(imageList, appPackage, false)
                        }
                        return@runCatching
                    }
                    val authority = "${this@SaveAndShareActivity.packageName}.provider"
                    FileProvider.getUriForFile(this@SaveAndShareActivity, authority, File(uri))
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, newUri)
                    //   putExtra(Intent.EXTRA_STREAM, uri.toUri())
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${getString(com.project.common.R.string.share_text)} https://play.google.com/store/apps/details?id=$packageName"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage(appPackage) // Set the specific app package
                }
                try {
                    showAppOpen = false
                    startActivity(shareIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun Activity.shareImages(uris: List<String>, fromSaved: Boolean) {
        runOnUiThread {
            kotlin.runCatching {
                val imageUris = uris.map { uriString ->
                    if (fromSaved) {
                        uriString.toUri()
                    } else {
                        val authority = "${packageName}.provider"
                        FileProvider.getUriForFile(this, authority, File(uriString))
                    }
                }

                if (imageUris.isEmpty()) {
                    return@runCatching
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${getString(com.project.common.R.string.share_text)} https://play.google.com/store/apps/details?id=$packageName"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(shareIntent, "Share images via"))
            }
        }
    }

    private fun Activity.shareImagesToApp(
        uris: List<String>,
        appPackage: String,
        fromSaved: Boolean,
    ) {
        runOnUiThread {
            kotlin.runCatching {
                val imageUris = uris.map { uriString ->
                    if (fromSaved) {
                        uriString.toUri()
                    } else {
                        val authority = "${packageName}.provider"
                        FileProvider.getUriForFile(this, authority, File(uriString))
                    }
                }

                if (imageUris.isEmpty()) {
                    return@runCatching
                }

                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${getString(com.project.common.R.string.share_text)} https://play.google.com/store/apps/details?id=$packageName"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage(appPackage) // Target specific app
                }

                try {
                    showAppOpen = false
                    startActivity(shareIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

