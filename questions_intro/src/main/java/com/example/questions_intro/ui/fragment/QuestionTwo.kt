package com.example.questions_intro.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import com.example.ads.Constants.firebaseAnalytics
import com.example.questions_intro.R
import com.example.questions_intro.databinding.FragmentQuestionOneBinding
import com.example.questions_intro.ui.activity.QuestionsActivity
import com.example.questions_intro.ui.compose_views.FragmentView
import com.example.questions_intro.ui.view_model.QuestionsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuestionTwo : Fragment() {

    private var _binding: FragmentQuestionOneBinding? = null

    private val binding get() = _binding!!

    private val questionsViewModel by activityViewModels<QuestionsViewModel>()

    private var showSelectedNext: MutableState<Boolean> = mutableStateOf(false)

    private val selectedItems = mutableStateMapOf<Int, Boolean>()

    private val scrollState = LazyGridState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionsViewModel.initializing()
        kotlin.runCatching {
            questionsViewModel.getMapForCurrentFragment(1)?.let {
                selectedItems.putAll(it)
                showSelectedNext.value = selectedItems.isNotEmpty()
            }
        }
        logEvent("question_2_view", null)
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
                    FragmentView(
                        2,
                        questionsViewModel.fetchQuestions(2, it),
                        questionsViewModel.screenThreeContent(it),
                        showSelectedNext,
                        selectedItems,
                        onNextClick = {
                            logEvent("question_2_click_next", null)
                            kotlin.runCatching {
                                val map = selectedItems.keys
                                var ids = ""
                                map.sorted().forEach {
                                    if (ids.isBlank())
                                        ids = ids.plus("${it + 1}")
                                    else
                                        ids = ids.plus(",${it + 1}")
                                }
                                logEvent("question_2_click_choose", Bundle().apply {
                                    putString("id_question_2", ids)
                                })
                                Log.i("TAG", "onViewCreated: $ids")

                                getParentActivity()?.navigate(
                                    QuestionTwoDirections.actionQuestionTwoToQuestionThree(),
                                    R.id.questionTwo
                                )
                            }
                        },
                        onSkipClick = {
                            logEvent("question_2_click_skip", null)
                            getParentActivity()?.skipToIntroActivity()
                        },
                        onBackClick = {
                            getParentActivity()?.navigateBack()
                        },
                        onItemClick = { index, isSelected ->
                            questionsViewModel.initializeStateForFragment(1,index, isSelected)
                            if (isSelected) {
                                getParentActivity()?.loadAndShowNativeAd()
                            }
                        },
                        scrollState
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