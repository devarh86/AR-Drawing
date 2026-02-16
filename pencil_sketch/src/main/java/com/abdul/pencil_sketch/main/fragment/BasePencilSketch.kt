package com.abdul.pencil_sketch.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentBasePencilSketchBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.utils.navigateFragment
import com.project.common.model.ImagesModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BasePencilSketch : Fragment() {

    private var _binding: FragmentBasePencilSketchBinding? = null
    private val binding get() = _binding!!

    private val sketchImageViewModel: PencilSketchViewModel by activityViewModels()

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
        activity?.let { mActivity ->
            if (mActivity is PencilSketchActivity) {
                if (mActivity.isOpenFromMain) {

                    if (mActivity.imgPath.isNotEmpty()) {
                        sketchImageViewModel.imageEnhancedPath.add(ImagesModel())
                        sketchImageViewModel.imageEnhancedPath[0].croppedPath = mActivity.imgPath
                        sketchImageViewModel.imageEnhancedPath[0].originalPath = mActivity.imgPath

                        sketchImageViewModel.imageEnhancedPath.clear()
                        sketchImageViewModel.imageEnhancedPath.add(ImagesModel(mActivity.imgPath, mActivity.imgPath))

                        sketchImageViewModel.sketchMode = mActivity.sketchMode
                    }

                    activity?.navigateFragment(
                        BasePencilSketchDirections.actionBasePencilSketchToDrawingFragment(),
                        R.id.basePencilSketch
                    )

                } else {
                    activity?.navigateFragment(
                        BasePencilSketchDirections.actionBasePencilSketchToGalleryPencilSketch(false),
                        R.id.basePencilSketch
                    )
                }
            }
        }


    }

}