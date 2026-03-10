package com.fahad.newtruelovebyfahad.ui.fragments.learning

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.fahad.newtruelovebyfahad.databinding.FragmentSubFramesBinding
import com.fahad.newtruelovebyfahad.ui.fragments.learning.adapter.SubFrameItem
import com.fahad.newtruelovebyfahad.ui.fragments.learning.adapter.SubFramesGridAdapter
import com.fahad.newtruelovebyfahad.utils.gone
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.visible
import com.project.common.utils.ConstantsCommon
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubFramesFragment : Fragment() {

    private var _binding: FragmentSubFramesBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController
    private var adapter: SubFramesGridAdapter? = null


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
        _binding = FragmentSubFramesBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        adapter = SubFramesGridAdapter()
        binding.framesRv.adapter = adapter

        binding.backPress.setSingleClickListener {
            navController.navigateUp()
        }

        binding.drawBtn.setSingleClickListener {
            navController.navigate(SubFramesFragmentDirections.actionSubFramesFragmentToDrawFragment())
        }

        showLoading()
        renderData()
    }

    private fun renderData() {
        val frame = ConstantsCommon.currentFrameMain
        val frameBaseUrl = frame?.baseUrl.orEmpty()

        binding.titleTV.text = frame?.title

        val items = frame?.files.orEmpty().mapNotNull { fileObj ->
            val file = fileObj?.file.orEmpty().trim()
            if (file.isBlank()) {
                return@mapNotNull null
            }

            val baseUrl = fileObj?.baseUrl.orEmpty().ifBlank { frameBaseUrl }
            val fullUrl = buildFullUrl(baseUrl, file)
            SubFrameItem(file = file, baseUrl = baseUrl, fullUrl = fullUrl)
        }

        if (items.isEmpty()) {
            showEmptyState()
            return
        }

        hideLoading()
        binding.tryNowPlaceholder.gone()
        binding.noResultFoundTv.gone()
        binding.framesRv.visible()
        adapter?.submitList(items.reversed())
    }

    private fun showLoading() {
        binding.loadingView.visible()
        binding.loadingView.startShimmer()
        binding.framesRv.gone()
        binding.tryNowPlaceholder.gone()
        binding.noResultFoundTv.gone()
    }

    private fun hideLoading() {
        binding.loadingView.stopShimmer()
        binding.loadingView.gone()
    }

    private fun showEmptyState() {
        hideLoading()
        binding.framesRv.gone()
        binding.tryNowPlaceholder.visible()
        binding.noResultFoundTv.visible()
        binding.noResultFoundTv.text = getString(com.project.common.R.string.no_result_found)
    }

    private fun buildFullUrl(baseUrl: String, file: String): String {
        if (file.startsWith("http", ignoreCase = true) || file.startsWith("file://", ignoreCase = true)) {
            return file
        }

        val cleanBase = baseUrl.trim().trimEnd('/')
        val cleanFile = file.trim().trimStart('/')
        return if (cleanBase.isBlank()) cleanFile else "$cleanBase/$cleanFile"
    }

    override fun onDestroyView() {
        binding.loadingView.stopShimmer()
        binding.framesRv.adapter = null
        adapter = null
        _binding = null
        super.onDestroyView()
    }
}
