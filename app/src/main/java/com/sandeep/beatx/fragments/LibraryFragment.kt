package com.sandeep.beatx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sandeep.beatx.R
import com.sandeep.beatx.adapters.PlaylistAdapter
import com.sandeep.beatx.adapters.SearchAdapter
import com.sandeep.beatx.databinding.FragmentLibraryBinding
import com.sandeep.beatx.viewmodel.FavoriteViewModel
import com.sandeep.beatx.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val playlistViewModel: PlaylistViewModel by viewModels()
    
    private lateinit var favoritesAdapter: SearchAdapter
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupListeners()
        observeData()
    }
    
    private fun setupRecyclerViews() {
        favoritesAdapter = SearchAdapter(emptyList()) { song ->
            val bundle = android.os.Bundle().apply {
                putParcelable("song", song)
                putParcelableArrayList("queue", java.util.ArrayList(favoriteViewModel.favorites.value))
            }
            findNavController().navigate(R.id.playerFragment, bundle)
        }
        binding.rvFavorites.adapter = favoritesAdapter
        
        playlistAdapter = PlaylistAdapter { playlist ->
            val bundle = android.os.Bundle().apply {
                putInt("playlistId", playlist.id)
                putString("playlistName", playlist.name)
            }
            findNavController().navigate(R.id.playlistDetailFragment, bundle)
        }
        binding.rvPlaylists.adapter = playlistAdapter
    }
    
    private fun setupListeners() {
        binding.btnCreatePlaylist.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }
    
    private fun showCreatePlaylistDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Playlist Name"
            setPadding(48, 32, 48, 32)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Create Playlist")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    playlistViewModel.createPlaylist(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    favoriteViewModel.favorites.collect { songs ->
                        if (songs.isEmpty()) {
                            binding.rvFavorites.visibility = View.GONE
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            binding.rvFavorites.visibility = View.VISIBLE
                            binding.emptyView.visibility = View.GONE
                            favoritesAdapter.updateData(songs)
                        }
                    }
                }
                
                launch {
                    playlistViewModel.allPlaylists.collect { playlists ->
                        playlistAdapter.submitList(playlists)
                        if (playlists.isEmpty()) {
                            binding.rvPlaylists.visibility = View.GONE
                            binding.tvPlaylistsLabel.visibility = View.GONE
                        } else {
                            binding.rvPlaylists.visibility = View.VISIBLE
                            binding.tvPlaylistsLabel.visibility = View.VISIBLE
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
