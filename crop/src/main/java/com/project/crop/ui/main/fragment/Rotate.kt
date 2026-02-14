package com.project.crop.ui.main.fragment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.project.common.R
import com.project.common.utils.getColorWithSafetyCheck
import com.project.common.utils.setOnSingleClickListener
import com.project.crop.databinding.FragmentRotateBinding
import com.project.crop.ui.main.custom_views.RuleView
import com.project.crop.ui.main.intent.MainIntent
import com.project.crop.ui.main.viewmodel.RotateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Rotate : Fragment(), RuleView.OnValueChangedListener {

    private var _binding: FragmentRotateBinding? = null
    private val binding get() = _binding!!

    private var callback: OnBackPressedCallback? = null

    private val rotateViewModel: RotateViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
//
//    companion object {
//        var drawable: Drawable? = null
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (_binding == null) {
            _binding = FragmentRotateBinding.inflate(inflater, container, false)

            init()

            initClick()
        }

        observeData()

        onBackPress()

        return binding.root
    }

    private fun init() {
        binding.ruleView.setOnValueChangedListener(this)
    }

    private fun initClick() {

        binding.root.setOnSingleClickListener {

        }

        binding.resetRotate.setOnSingleClickListener {
            lifecycleScope.launch(Main) {
                rotateViewModel.currentRotation = 0f
                rotateViewModel.rotateIntent.send(MainIntent.RotateImage(rotateViewModel.currentRotation))
                rotateViewModel.updateCurrentValue()
            }
        }

        binding.rotateImage.setOnSingleClickListener {
            lifecycleScope.launch(Main) {
                var rotation = rotateViewModel.currentRotation + 90f
                if (rotation > 180) {
                    rotation -= 180
                    rotation -= 180
                }
                rotateViewModel.currentRotation = rotation
                rotateViewModel.rotateIntent.send(MainIntent.RotateImage(rotateViewModel.currentRotation))
                rotateViewModel.updateCurrentValue()
            }
        }
        binding.tickIcon.setOnSingleClickListener {
            rotateViewModel.updateTick()
        }
    }

    private fun observeData() {
        rotateViewModel.updateState.observe(viewLifecycleOwner) {
            binding.currentValueText.text = it.toString().plus("\u00B0")
            binding.ruleView.setCurrentValue(it, false)
        }

        rotateViewModel.updateUI.observe(viewLifecycleOwner){
            it?.let {
                if(it == "Update"){
                    updateRotateUiAccordingTheme()
                    rotateViewModel.resetState()
                }
            }
        }
    }

    private fun updateRotateUiAccordingTheme() {
        kotlin.runCatching {
            if(_binding!= null){
                context?.let {cntx->
                binding.rotateRootV.setBackgroundColor(cntx.getColorWithSafetyCheck(R.color.editor_bar_clr))
                    binding.tickIcon.imageTintList = ColorStateList.valueOf(cntx.getColorWithSafetyCheck(R.color.selected_color))
                    binding.resetRotate.imageTintList = ColorStateList.valueOf(cntx.getColorWithSafetyCheck(R.color.btn_icon_clr))
                    binding.rotateImage.imageTintList = ColorStateList.valueOf(cntx.getColorWithSafetyCheck(R.color.btn_icon_clr))
                    binding.textView.setTextColor(cntx.getColorWithSafetyCheck(R.color.tab_txt_clr))
                    binding.currentValueText.setTextColor(cntx.getColorWithSafetyCheck(R.color.selected_color))
                    binding.view.setBackgroundColor(cntx.getColorWithSafetyCheck(R.color.surface_clr))
                    binding.view1.setBackgroundColor(cntx.getColorWithSafetyCheck(R.color.surface_clr))
                    binding.view2.setBackgroundColor(cntx.getColorWithSafetyCheck(R.color.surface_clr))
                    binding.view3.setBackgroundColor(cntx.getColorWithSafetyCheck(R.color.surface_clr))
                    binding.ruleView.toggleTheme()
//                    binding.ruleView.applyTheme(RuleView.Theme.DARK)
                }
            }
        }
    }

    override fun onValueChanged(value: Float) {
        binding.currentValueText.text = value.toString().plus("\u00B0")
        lifecycleScope.launch(Main) {
            rotateViewModel.rotateIntent.send(MainIntent.RotateImage(value))
        }
    }

    override fun onValueChangedComplete(value: Float) {}

    private fun onBackPress() {

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                backPress()
            }
        }
        callback?.let {

            activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner, it)
        }
    }

    private fun backPress() {
        rotateViewModel.updateCancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        callback?.remove()
    }
}