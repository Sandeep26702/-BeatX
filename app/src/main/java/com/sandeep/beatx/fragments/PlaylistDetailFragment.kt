package com.sandeep.beatx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sandeep.beatx.R
import com.sandeep.beatx.adapters.SearchAdapter
import com.sandeep.beatx.databinding.FragmentPlaylistDetailBinding
import com.sandeep.beatx.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch

class PlaylistDetailFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    
    private val playlistViewModel: PlaylistViewModel by viewModels()
    private lateinit var adapter: SearchAdapter
    private var playlistId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        playlistId = arguments?.getInt("playlistId", -1) ?: -1
        val playlistName = arguments?.getString("playlistName", "Playlist") ?: "Playlist"
        
        binding.tvPlaylistName.text = playlistName
        
        setupRecyclerView()
        setupListeners()
        observeData()
    }
    
    private fun setupRecyclerView() {
        adapter = SearchAdapter(emptyList()) { song ->
            val bundle = android.os.Bundle().apply {
                putParcelable("song", song)
                putParcelableArrayList("queue", java.util.ArrayList(adapter.getCurrentData()))
            }
            findNavController().navigate(R.id.playerFragment, bundle)
        }
        
        adapter.setOnItemLongClickListener { song ->
            AlertDialog.Builder(requireContext())
                .setTitle("Remove Song")
                .setMessage("Remove '${song.title}' from playlist?")
                .setPositiveButton("Remove") { _, _ ->
                    playlistViewModel.removeSongFromPlaylist(playlistId, song.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        
        binding.rvSongs.adapter = adapter
    }
    
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnOptions.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menu.add(0, 1, 0, "Rename Playlist")
            popup.menu.add(0, 2, 0, "Delete Playlist")
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> showRenameDialog()
                    2 -> showDeleteDialog()
                }
                true
            }
            popup.show()
        }
    }
    
    private fun showRenameDialog() {
        val editText = EditText(requireContext()).apply {
            setText(binding.tvPlaylistName.text)
            setPadding(48, 32, 48, 32)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Rename Playlist")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    playlistViewModel.renamePlaylist(playlistId, newName)
                    binding.tvPlaylistName.text = newName
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete this playlist?")
            .setPositiveButton("Delete") { _, _ ->
                playlistViewModel.deletePlaylist(playlistId)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun observeData() {
        if (playlistId == -1) return
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                playlistViewModel.getPlaylistWithSongs(playlistId).collect { playlistWithSongs ->
                    if (playlistWithSongs != null) {
                        val songs = playlistWithSongs.songs.map { it.toSong() }
                        if (songs.isEmpty()) {
                            binding.rvSongs.visibility = View.GONE
                            binding.tvEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.rvSongs.visibility = View.VISIBLE
                            binding.tvEmptyState.visibility = View.GONE
                            adapter.updateData(songs)
                        }
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
