package com.fahad.newtruelovebyfahad.ui.fragments.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentRatingDialogBinding
import com.fahad.newtruelovebyfahad.ui.activities.feedback.FeedbackActivity
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingDialog : BottomSheetDialogFragment() {

    private var _binding: FragmentRatingDialogBinding? = null
    private val binding get() = _binding!!
    private var navController: NavController? = null
    private var mContext: Context? = null
    private var mActivity: Activity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as Activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRatingDialogBinding.inflate(inflater, container, false)
        _binding?.initViews()
        return binding.root
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return try {
            BottomSheetDialog(requireContext(), com.project.common.R.style.BottomSheetDialogNew)
        } catch (e: IllegalStateException) {
            Log.d("RatingDialog", "Error creating BottomSheetDialog", e)
            super.onCreateDialog(savedInstanceState)
        }
    }

    private fun FragmentRatingDialogBinding.initViews() {
        initListeners()
    }

    private fun FragmentRatingDialogBinding.initListeners() {
        closeBtn.setSingleClickListener {
            navController?.navigateUp()
        }


        var ratings = 5f
        rating.rating = ratings
        rating.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                ratings = rating
                topImage.setAnimation(
                    when (rating) {
                        2f -> R.raw.rating_2
                        3f -> R.raw.rating_3
                        4f -> R.raw.rating_4
                        5f -> R.raw.rating_5
                        else -> {
                            R.raw.rating_1
                        }
                    }
                )
                topImage.playAnimation()
            }
        }

        rateBtn.setSingleClickListener {
            if (ratings > 3) {
                mActivity?.initInAppReview()
            } else {
                mActivity?.let {
                    startActivity(Intent(it, FeedbackActivity::class.java))
                }
            }
        }
    }

    private fun Activity.initInAppReview() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
                    kotlin.runCatching {
                    navController?.navigateUp()
                }
                    }
            } else {
                navController?.navigateUp()
                @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
            }
        }
    }
}