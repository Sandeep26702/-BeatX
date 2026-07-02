package com.sandeep.beatx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sandeep.beatx.adapters.SearchAdapter
import com.sandeep.beatx.databinding.BottomSheetQueueBinding
import com.sandeep.beatx.model.Song

class BottomSheetQueue(
    private val queueSongs: List<Song>,
    private val onSongClick: (Song, Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetQueueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SearchAdapter(queueSongs) { song ->
            val index = queueSongs.indexOf(song)
            onSongClick(song, index)
            dismiss()
        }
        
        binding.rvQueue.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQueue.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
