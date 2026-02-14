package com.example.effect.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.fahad.newtruelovebyfahad.GetStickersQuery
import com.project.sticker.databinding.FragmentStickerPackBinding
import com.example.effect.ui.adapters.StickersPacksAdapter
import com.example.effect.ui.viewmodel.StickerViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StickerPacks : Fragment() {

    private var _binding: FragmentStickerPackBinding? = null
    private val binding get() = _binding!!

    private val stickerViewModel: StickerViewModel by activityViewModels()

    private var recyclerAdapter: StickersPacksAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (_binding == null) {

            _binding = FragmentStickerPackBinding.inflate(inflater, container, false)

            init()

            initClick()
        }

        return binding.root
    }

    private fun init() {

        recyclerAdapter = StickersPacksAdapter(object : StickersPacksAdapter.OnItemClick {
            override fun onPackClick(pack: GetStickersQuery.Sticker, position: Int) {
                stickerViewModel.updateSticker(pack)
            }
        })

        binding.recyclerView.adapter = recyclerAdapter

        setList()
    }

    private fun setList() {
        arguments?.getInt("position")?.let {
            if (it < stickerViewModel.stickersCategoriesAndData.size && it >= 0) {
                recyclerAdapter?.addList(stickerViewModel.stickersCategoriesAndData[it].packList)
            }
        }
    }

    private fun initClick() {}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        System.gc()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        recyclerAdapter = null
    }
}