package com.fahad.newtruelovebyfahad.ui.activities.feedback

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateMargins
import com.bumptech.glide.Glide
import com.example.ads.Constants.languageCode
import com.example.ads.crosspromo.helper.show
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.ActivityFeedbackBinding
import com.fahad.newtruelovebyfahad.utils.invisible
import com.fahad.newtruelovebyfahad.utils.showToast
import com.project.common.utils.getNotchHeight
import com.project.common.utils.setLocale
import com.project.common.utils.setStatusBarNavBarColor
import com.project.common.utils.setOnSingleClickListener
import kotlin.math.roundToInt

class FeedbackActivity : AppCompatActivity() {

    private val binding by lazy { ActivityFeedbackBinding.inflate(layoutInflater) }
    private val feedbackList = arrayListOf(
//        Suggestions("User Interface"),
//        Suggestions("Speed"),
//        Suggestions("Content"),
//        Suggestions("Performance"),
//        Suggestions("App Crashes"),
//        Suggestions("User Experience"),
//        Suggestions("Feature Requests"),
//        Suggestions("Suggestions"),
//        Suggestions("Other Issues")
        Suggestions("UI"),
        Suggestions("Speed"),
        Suggestions("Content"),
        Suggestions("Performance"),
        Suggestions("Crashes"),
        Suggestions("UX"),
        Suggestions("Feature"),
        Suggestions("Suggest"),
        Suggestions("Other")
    )
    private val suggestionList = arrayListOf(
        Suggestions("Frames"),
        Suggestions("Interface"),
        Suggestions("Editing"),
        Suggestions("Others")
    )
    val titleFeedback: String = "Feedback"
    val subTitleFeedback: String = "Share your thoughts about the issue you encountered"
    val titleFeedback1: String = "Share Your Thoughts"
    val subTitleFeedback1: String = "Share your experience with this app"
    val titleSuggestion: String = "Suggestion"
    val subTitleSuggestion: String = "Tell us what you would want to be improve ?"

    private var feedbackTags: HashSet<String> = HashSet()
    private var feedbackImage: Uri? = null
    private var feedbackImageName: String? = null

    private val galleryPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val data = it.data
            val imgUri = data?.data
            feedbackImage = imgUri
            feedbackImage?.let {
                feedbackImageName = getFileNameFromUri(this@FeedbackActivity, it)
                binding.screenshotIv.show()
                Glide.with(this).load(feedbackImage).into(binding.screenshotIv)
                binding.deleteIv.show()
            }
            feedbackImageName?.let {
                binding.fileName.show()
                binding.fileName.text = feedbackImageName
            }
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }
        return fileName
    }

    private var feedbackAdapter: FeedbackRV? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        kotlin.runCatching {
            setLocale(languageCode)
        }
//        binding.root.layoutParams.let {
//            if (it is FrameLayout.LayoutParams) {
//                it.updateMargins(0, getNotchHeight(), 0, 0)
//            }
//        }

        setStatusBarNavBarColor(com.project.common.R.drawable.status_top_white)
        val check = intent.getBooleanExtra("feedback", true)

        if (check) {
            val heading = intent.getIntExtra("heading", 0)
            if (heading == 0) {
                binding.title.text = titleFeedback
                binding.subTitle.text = subTitleFeedback
            } else {
                binding.title.text = titleFeedback1
                binding.subTitle.text = subTitleFeedback1
            }
        } else {
            binding.title.text = titleSuggestion
            binding.subTitle.text = subTitleSuggestion
        }

        feedbackAdapter = FeedbackRV(
            if (check) {
                feedbackList
            } else {
                suggestionList
            }
        ) {
            feedbackTags.add(it.text)
        }

        binding.feedbacksRv.adapter = feedbackAdapter

        binding.addImage.setOnSingleClickListener {
            kotlin.runCatching {
                galleryPicker.launch(
                    Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                )
            }
        }

        binding.deleteIv.setOnSingleClickListener {
            feedbackImage?.let {
                feedbackImage = null
                binding.screenshotIv.invisible()
                feedbackImageName = null
                binding.fileName.invisible()
                binding.deleteIv.invisible()
            }
        }

        binding.backIv.setOnSingleClickListener {
            finish()
        }

        binding.submitBtn.setOnSingleClickListener {
            binding.descriptionEdtv.text?.let {
                if (it.toString().isNotBlank()) {
                    sendEmailFeedback(feedbackTags.toList(), it.toString(), feedbackImage)
                } else {
                    showToast("Enter Feedback...")
                }
            }
        }
    }

    fun Context.sendEmailFeedback(
        tags: List<String>? = null,
        feedbackText: String? = "",
        imagePath: Uri? = null
    ) {
        try {
            val subject: String = String.format(
                resources.getString(com.project.common.R.string.feedback),
                resources.getString(R.string.app_name)
            )

            val selectorIntent = Intent(Intent.ACTION_SENDTO)
            selectorIntent.data = Uri.parse("mailto:")
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("contact.xenapps1@gmail.com"))
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "${resources.getString(com.project.common.R.string.app_name_new)}$subject"
            )
            var formatting = ""
            tags?.forEach { formatting = "$formatting#$it " }
            feedbackText?.let { formatting = "$formatting\n$it" }
            emailIntent.putExtra(Intent.EXTRA_TEXT, formatting)
            imagePath?.let { emailIntent.putExtra(Intent.EXTRA_STREAM, imagePath) }
            emailIntent.selector = selectorIntent
            startActivity(
                Intent.createChooser(
                    emailIntent,
                    resources.getString(com.project.common.R.string.send_mail)
                )
            )
        } catch (e: Exception) {
            Log.d("FAHAD", "sendEmailFeedback: ${e.message}")
        }
    }
}