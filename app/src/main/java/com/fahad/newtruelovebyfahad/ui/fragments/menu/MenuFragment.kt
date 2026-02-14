package com.fahad.newtruelovebyfahad.ui.fragments.menu

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.fahad.newtruelovebyfahad.R
import com.fahad.newtruelovebyfahad.databinding.FragmentMenuBinding

import com.fahad.newtruelovebyfahad.utils.rateUs
import com.fahad.newtruelovebyfahad.utils.setSingleClickListener
import com.fahad.newtruelovebyfahad.utils.shareApp
import com.project.common.utils.privacyPolicy
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment : DialogFragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mActivity: AppCompatActivity
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        mActivity = context as AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, com.project.common.R.style.DialogTheme_transparent)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        binding.initViews()
        return binding.root
    }

    private fun FragmentMenuBinding.initViews() {
        initListeners()
    }


    private fun FragmentMenuBinding.initListeners() {
        closeBtn.setSingleClickListener { navController.navigateUp() }
        removeAdsTv.setSingleClickListener { mActivity.privacyPolicy() }
        privacyPolicyTv.setSingleClickListener { mActivity.privacyPolicy() }
        shareTv.setSingleClickListener { mActivity.shareApp(getString(com.project.common.R.string.name)) }
        rateUsTv.setSingleClickListener { mActivity.rateUs() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}