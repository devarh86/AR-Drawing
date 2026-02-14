package com.project.common.baseFragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

open class BaseFragment<T : ViewBinding>(private val inflateMethod: (LayoutInflater, ViewGroup?, Boolean) -> T) :
    Fragment() {

    private var _binding: T? = null

    val binding: T get() = _binding!!

    // Make it open, so it can be overridden in child fragments
    open fun T.onCreateView() {}
    open fun T.onViewCreated() {}
    open fun T.onResume() {}

    private var _context: Context? = null
    protected val mContext: Context get() = _context!!

    private var _activity: AppCompatActivity? = null
    protected val mActivity: AppCompatActivity get() = _activity!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
        _activity = context as AppCompatActivity

    }
    // Utility method to show a Toast message
    fun Activity.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateMethod.invoke(inflater, container, false)

        binding.onCreateView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onViewCreated()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}