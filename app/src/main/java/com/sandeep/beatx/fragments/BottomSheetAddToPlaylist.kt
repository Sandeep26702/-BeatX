package com.sandeep.beatx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sandeep.beatx.adapters.PlaylistAdapter
import com.sandeep.beatx.databinding.BottomSheetAddToPlaylistBinding
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch

class BottomSheetAddToPlaylist(private val song: Song) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddToPlaylistBinding? = null
    private val binding get() = _binding!!
    
    private val playlistViewModel: PlaylistViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlaylistAdapter { playlist ->
            playlistViewModel.addSongToPlaylist(playlist.id, song)
            Toast.makeText(requireContext(), "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        
        binding.rvBottomSheetPlaylists.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvBottomSheetPlaylists.adapter = adapter
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playlistViewModel.allPlaylists.collect { playlists ->
                    if (playlists.isEmpty()) {
                        binding.tvEmptyPlaylists.visibility = View.VISIBLE
                        binding.rvBottomSheetPlaylists.visibility = View.GONE
                    } else {
                        binding.tvEmptyPlaylists.visibility = View.GONE
                        binding.rvBottomSheetPlaylists.visibility = View.VISIBLE
                        adapter.submitList(playlists)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
