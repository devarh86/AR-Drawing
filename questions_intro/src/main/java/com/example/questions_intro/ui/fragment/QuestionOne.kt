package com.example.questions_intro.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.example.ads.Constants.firebaseAnalytics
import com.example.ads.admobs.utils.loadAndShowNativeOnBoarding
import com.example.ads.crosspromo.helper.show
import com.example.ads.utils.survey
import com.example.inapp.helpers.Constants.isProVersion
import com.example.questions_intro.R
import com.example.questions_intro.databinding.FragmentQuestionOneBinding
import com.example.questions_intro.ui.activity.QuestionsActivity
import com.example.questions_intro.ui.compose_views.FragmentView
import com.example.questions_intro.ui.view_model.QuestionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestionOne : Fragment() {

    private var _binding: FragmentQuestionOneBinding? = null

    private val binding get() = _binding!!

    private val questionsViewModel by activityViewModels<QuestionsViewModel>()

    private val scrollState = LazyGridState()

    private var showSelectedNext: MutableState<Boolean> = mutableStateOf(false)

    private var selectedItems = mutableStateMapOf<Int, Boolean>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionsViewModel.initializing()
        kotlin.runCatching {
            questionsViewModel.getMapForCurrentFragment(0)?.let {
                selectedItems.putAll(it)
                showSelectedNext.value = selectedItems.isNotEmpty()
            }
        }
        logEvent("question_1_view", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuestionOneBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleOwner = viewLifecycleOwner)
            )
            setContent {
                context?.let {
                    FragmentView(1,
                        questionsViewModel.fetchQuestions(1, it),
                        questionsViewModel.screenTwoContent(it),
                        showSelectedNext,
                        selectedItems,

                        onNextClick = {
                            logEvent("question_1_click_next", null)
                            kotlin.runCatching {
                                val map = selectedItems.keys
                                var ids = ""
                                map.sorted().forEach {
                                    ids = if (ids.isBlank())
                                        ids.plus("${it + 1}")
                                    else
                                        ids.plus(",${it + 1}")
                                }
                                logEvent("question_1_click_choose", Bundle().apply {
                                    putString("id_question_1", ids)
                                })
                                Log.i("TAG", "onViewCreated: $ids")

                                getParentActivity()?.navigate(
                                    QuestionOneDirections.actionQuestionOneToQuestionTwo(),
                                    R.id.questionOne
                                )
                            }
                        },
                        onSkipClick = {
                            logEvent("question_1_click_skip", null)
                            getParentActivity()?.skipToIntroActivity()
                        },
                        onBackClick = {

                        },
                        onItemClick = { index, isSelected ->
                            questionsViewModel.initializeStateForFragment(0,index, isSelected)
                            if (isSelected) {
                                getParentActivity()?.loadAndShowNativeAd()
                            }
                        },
                        scrollState,
                        fromSurvey = true,
                    )
                }
            }
        }
    }

    private fun getParentActivity(): QuestionsActivity? {
        activity?.let {
            if (it is QuestionsActivity)
                return it
        }
        return null
    }

    private fun logEvent(event: String, bundle: Bundle?) {
        bundle?.let {
            firebaseAnalytics?.logEvent(event, bundle)
        } ?: run {
            firebaseAnalytics?.logEvent(event, null)
        }

        Log.i("TAG", "firebase_event: $event  $bundle")
    }



//    override fun onResume() {
//        super.onResume()
//        _binding?.composeView?.disposeComposition()
//    }
}