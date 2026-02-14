package com.fahad.newtruelovebyfahad.ui.fragments.home.frames

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.fahad.newtruelovebyfahad.MainScreenNavigationDirections
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentFrameTypesBinding
import com.project.common.utils.enums.MainMenuOptions
import com.fahad.newtruelovebyfahad.utils.printLog
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FrameTypesFragment : DialogFragment() {

    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var navController: NavController? = null
    private var _binding: FragmentFrameTypesBinding? = null
    private val binding get() = _binding!!
    private var mainMenuOption: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFrameTypesBinding.inflate(inflater, container, false)
        _binding?.initViews()
        return binding.root
    }

    private fun FragmentFrameTypesBinding.initViews() {
        animateViews()
        initListeners()
    }

    private fun FragmentFrameTypesBinding.animateViews() {
        val animation = AnimationUtils.loadAnimation(
            root.context,
            R.anim.fab_view_anim
        )
        singleFramesBtn.startAnimation(animation)
        doubleFramesBtn.startAnimation(animation)
        multiplexFramesBtn.startAnimation(animation)
    }

    private fun FragmentFrameTypesBinding.initListeners() {
        root.setOnSingleClickListener {
            mainMenuOption = null
            navController?.navigateUp()
        }

        singleFramesBtn.setOnSingleClickListener {
            mainMenuOption = MainMenuOptions.SOLO.title
            navController?.navigateUp()
        }

        doubleFramesBtn.setOnSingleClickListener {
            mainMenuOption = MainMenuOptions.DUAL.title
            navController?.navigateUp()
        }

        multiplexFramesBtn.setOnSingleClickListener {
            mainMenuOption = MainMenuOptions.MULTIPLEX.title
            navController?.navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainMenuOption?.let {
            try {
                navController?.navigate(
                    MainScreenNavigationDirections.actionGlobalNavFramesFragment(it,"")
                )
            } catch (ex: Exception) {
                printLog(ex.message.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}