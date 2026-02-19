package com.abdul.pencil_sketch.main.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.abdul.pencil_sketch.R
import com.abdul.pencil_sketch.databinding.FragmentHowToDrawBinding
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.abdul.pencil_sketch.main.fragment.adapter.SliderAdapterSketch
import com.abdul.pencil_sketch.main.fragment.adapter.SliderItemSketch
import com.abdul.pencil_sketch.main.viewmodel.PencilSketchViewModel
import com.abdul.pencil_sketch.utils.navigateFragment
import com.project.common.utils.setDrawable
import com.project.common.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class HowToDrawFragment : Fragment() {

    private var _binding: FragmentHowToDrawBinding? = null
    private val binding get() = _binding!!

    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController

    private lateinit var adapter: SliderAdapterSketch
    private val sketchImageViewModel: PencilSketchViewModel by activityViewModels()

    val list = listOf(
        SliderItemSketch(com.project.common.R.drawable.img_sketch_draw),
        SliderItemSketch(com.project.common.R.drawable.img_trace)
    )

    private var currentMode = "sketch"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_binding == null) {
            _binding = FragmentHowToDrawBinding.inflate(inflater, container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sliderView()
        listener()
    }

    private fun sliderView() {
        adapter = SliderAdapterSketch(list)
        binding.viewPager.addCarouselEffect()
        binding.viewPager.adapter = adapter
        updateButtonUI(binding.viewPager.currentItem)

        var lastSelected: ImageView = binding.dot1

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                lastSelected.setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_un_selected))

                when (position) {
                    0 -> {
                        binding.dot1.setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_select))
                        lastSelected = binding.dot1
                    }

                    1 -> {
                        binding.dot2.setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_select))
                        lastSelected = binding.dot2
                    }

                    else -> {
                        binding.dot2.setImageDrawable(context.setDrawable(com.project.common.R.drawable.round_select))
                        lastSelected = binding.dot2
                    }
                }

                adapter.setSelectedPosition(position)
                updateButtonUI(position)

            }
        })
    }

    private fun updateButtonUI(position: Int) {
        _binding?.apply {
            when (position) {
                0 -> {
                    currentMode = "sketch"
                    drawBtn.text = getString(com.project.common.R.string.with_camera)
                    updateModeSelectionUI(isCameraSelected = true)
                }

                1 -> {
                    currentMode = "trace"
                    drawBtn.text = getString(com.project.common.R.string.without_camera)
                    updateModeSelectionUI(isCameraSelected = false)
                }
            }
        }
    }

    private fun updateModeSelectionUI(isCameraSelected: Boolean) {
        val selectedColor = ContextCompat.getColor(mContext, com.project.common.R.color.selected_color)
        val unSelectedColor = ContextCompat.getColor(mContext, com.project.common.R.color.text_color_drawing_screen)

        binding.camTV.setTextColor(if (isCameraSelected) selectedColor else unSelectedColor)
        binding.brushTV.setTextColor(if (isCameraSelected) unSelectedColor else selectedColor)

        binding.camIV.imageTintList = ColorStateList.valueOf(if (isCameraSelected) selectedColor else unSelectedColor)
        binding.brushIV.imageTintList = ColorStateList.valueOf(if (isCameraSelected) unSelectedColor else selectedColor)

        binding.cameraLine.visibility = if (isCameraSelected) View.VISIBLE else View.INVISIBLE
        binding.brushLine.visibility = if (isCameraSelected) View.INVISIBLE else View.VISIBLE
    }

    fun ViewPager2.addCarouselEffect(enableZoom: Boolean = true) {
        clipChildren = false    // No clipping the left and right items
        clipToPadding = false   // Show the viewpager in full width without clipping the padding
        offscreenPageLimit = 3  // Render the left and right items
        (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER // Remove the scroll effect

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer((20 * Resources.getSystem().displayMetrics.density).toInt()))
        if (enableZoom) {
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = (0.80f + r * 0.20f)
            }
        }
        setPageTransformer(compositePageTransformer)
    }

    private fun listener() {

        binding.withCameraLayout.setOnSingleClickListener {
            currentMode = "sketch"
            binding.viewPager.setCurrentItem(0, true)
        }

        binding.withOutCameraLayout.setOnSingleClickListener {
            currentMode = "trace"
            binding.viewPager.setCurrentItem(1, true)
        }

        binding.drawBtn.setOnSingleClickListener {
            activity?.let { mActivity ->
                if (mActivity is PencilSketchActivity) {
                    mActivity.sketchMode = currentMode
                    sketchImageViewModel.sketchMode = currentMode
                    mActivity.navigateFragment(
                        HowToDrawFragmentDirections.actionHowToDrawFragmentToDrawingFragment(),
                        R.id.howToDrawFragment
                    )
                }
            }
        }

        binding.backPress.setOnSingleClickListener {
            kotlin.runCatching {
                navController.navigateUp()
            }
        }

    }

}
