package com.abdul.pencil_sketch.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentBasePencilSketchBinding
import com.abdul.pencil_sketch.utils.navigateFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BasePencilSketch : Fragment() {

    private var _binding: FragmentBasePencilSketchBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null) {
            _binding = FragmentBasePencilSketchBinding.inflate(inflater, container, false)
            initClick()
        }
        return binding.root
    }

    private fun initClick() {
        activity?.navigateFragment(
            BasePencilSketchDirections.actionBasePencilSketchToGalleryPencilSketch(false),
            R.id.basePencilSketch
        )
    }

}