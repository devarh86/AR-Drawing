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


        var ratings = 0

        rate1.setSingleClickListener {
            ratings = 1
            rate1.setImageResource(com.project.common.R.drawable.rate1_us)
            rate2.setImageResource(com.project.common.R.drawable.rate2_s)
            rate3.setImageResource(com.project.common.R.drawable.rate3_s)
            rate4.setImageResource(com.project.common.R.drawable.rate4_s)
            rate5.setImageResource(com.project.common.R.drawable.rate5_s)
            topImage.setAnimation(R.raw.rating_1)
            topImage.playAnimation()
        }

        rate2.setSingleClickListener {
            ratings = 2
            rate2.setImageResource(com.project.common.R.drawable.rate2_us)
            rate1.setImageResource(com.project.common.R.drawable.rate1_s)
            rate3.setImageResource(com.project.common.R.drawable.rate3_s)
            rate4.setImageResource(com.project.common.R.drawable.rate4_s)
            rate5.setImageResource(com.project.common.R.drawable.rate5_s)
            topImage.setAnimation(R.raw.rating_2)
            topImage.playAnimation()
        }

        rate3.setSingleClickListener {
            ratings = 2
            rate3.setImageResource(com.project.common.R.drawable.rate3_us)
            rate1.setImageResource(com.project.common.R.drawable.rate1_s)
            rate2.setImageResource(com.project.common.R.drawable.rate2_s)
            rate4.setImageResource(com.project.common.R.drawable.rate4_s)
            rate5.setImageResource(com.project.common.R.drawable.rate5_s)
            topImage.setAnimation(R.raw.rating_3)
            topImage.playAnimation()
        }

        rate4.setSingleClickListener {
            ratings = 4
            rate4.setImageResource(com.project.common.R.drawable.rate4_us)
            rate1.setImageResource(com.project.common.R.drawable.rate1_s)
            rate2.setImageResource(com.project.common.R.drawable.rate2_s)
            rate3.setImageResource(com.project.common.R.drawable.rate3_s)
            rate5.setImageResource(com.project.common.R.drawable.rate5_s)
            topImage.setAnimation(R.raw.rating_4)
            topImage.playAnimation()
        }

        rate5.setSingleClickListener {
            ratings = 5
            rate5.setImageResource(com.project.common.R.drawable.rate5_us)
            rate1.setImageResource(com.project.common.R.drawable.rate1_s)
            rate2.setImageResource(com.project.common.R.drawable.rate2_s)
            rate3.setImageResource(com.project.common.R.drawable.rate3_s)
            rate4.setImageResource(com.project.common.R.drawable.rate4_s)
            topImage.setAnimation(R.raw.rating_5)
            topImage.playAnimation()
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