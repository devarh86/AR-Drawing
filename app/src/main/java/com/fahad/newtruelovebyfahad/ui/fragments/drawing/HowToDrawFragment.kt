package com.fahad.newtruelovebyfahad.ui.fragments.drawing

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.abdul.pencil_sketch.main.activity.PencilSketchActivity
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentHowDrawBinding
import com.fahad.newtruelovebyfahad.ui.activities.main.MainActivity
import com.fahad.newtruelovebyfahad.ui.fragments.drawing.adapter.SliderAdapterHD
import com.fahad.newtruelovebyfahad.ui.fragments.drawing.adapter.SliderItemHD
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.google.android.material.tabs.TabLayoutMediator
import com.project.common.utils.setDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class HowToDrawFragment : Fragment() {
    private var _binding: FragmentHowDrawBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController

    private lateinit var adapter: SliderAdapterHD
    private var currentMode = "sketch"

    private val args by navArgs<HowToDrawFragmentArgs>()

    val list = listOf(
        SliderItemHD(com.project.common.R.drawable.img_sketch_draw),
        SliderItemHD(com.project.common.R.drawable.img_trace)
    )

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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHowDrawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sliderView()
        listener()
    }

    private fun sliderView() {
        adapter = SliderAdapterHD(list)
        binding.viewPager.addCarouselEffect()
        binding.viewPager.adapter = adapter

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
                    sketchBtn.backgroundTintList = mContext.getColorStateList(com.project.common.R.color.selected_color)
                    sketchBtn.setTextColor(mContext.getColor(com.project.common.R.color.white))
                    traceBtn.backgroundTintList = mContext.getColorStateList(com.project.common.R.color.white)
                    traceBtn.setTextColor(mContext.getColor(com.project.common.R.color.text_color))
                }

                1 -> {
                    currentMode = "trace"
                    traceBtn.backgroundTintList = mContext.getColorStateList(com.project.common.R.color.selected_color)
                    traceBtn.setTextColor(mContext.getColor(com.project.common.R.color.white))
                    sketchBtn.backgroundTintList = mContext.getColorStateList(com.project.common.R.color.white)
                    sketchBtn.setTextColor(mContext.getColor(com.project.common.R.color.text_color))
                }
            }
        }
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

        binding.sketchBtn.setSingleClickListener {
            currentMode = "sketch"
            binding.viewPager.setCurrentItem(0, true)
        }

        binding.traceBtn.setSingleClickListener {
            currentMode = "trace"
            binding.viewPager.setCurrentItem(1, true)
        }

        binding.drawBtn.setSingleClickListener {
            openPencilSketch(path = args.path, mode = currentMode)
        }

        binding.backPress.setSingleClickListener {
            kotlin.runCatching {
                navController.navigateUp()
            }
        }

    }

    private fun openPencilSketch(path: String, mode: String) {
        try {
            activity?.let { mActivity ->
                val intent = Intent(mActivity, PencilSketchActivity::class.java)
                intent.putExtra("fromMain", true)
                intent.putExtra("sketchMode", mode)
                intent.putExtra("imagePath", path)
                getParentActivity()?.getActivityLauncher()?.launch(intent)
//                mActivity.startActivity(intent)
            }
        } catch (ex: Exception) {
        }
    }

    private fun getParentActivity(): MainActivity? {
        activity?.let {
            if (it is MainActivity) {
                return it
            }
        }
        return null
    }


}