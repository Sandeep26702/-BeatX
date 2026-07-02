package com.sandeep.beatx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sandeep.beatx.R
import com.sandeep.beatx.adapters.SongAdapter
import com.sandeep.beatx.databinding.FragmentHomeBinding
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.viewmodel.HomeViewModel
import com.sandeep.beatx.viewmodel.RecentlyPlayedViewModel
import com.sandeep.beatx.viewmodel.UiState
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private val recentlyPlayedViewModel: RecentlyPlayedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cvSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.fetchSongs()
        }

        observeViewModel()
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> showLoading()
                            is UiState.Success -> showSuccess(state.data)
                            is UiState.Error -> showError(state.message)
                        }
                    }
                }
                launch {
                    recentlyPlayedViewModel.recentlyPlayed.collect { songs ->
                        if (songs.isEmpty()) {
                            binding.tvRecentlyPlayed.visibility = View.GONE
                            binding.rvRecentlyPlayed.visibility = View.GONE
                        } else {
                            binding.tvRecentlyPlayed.visibility = View.VISIBLE
                            binding.rvRecentlyPlayed.visibility = View.VISIBLE
                            
                            binding.rvRecentlyPlayed.adapter = SongAdapter(songs) { song ->
                                val bundle = android.os.Bundle().apply {
                                    putParcelable("song", song)
                                    putParcelableArrayList("queue", java.util.ArrayList(songs))
                                }
                                findNavController().navigate(R.id.playerFragment, bundle)
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
        binding.contentView.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.contentView.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }
    
    private fun showSuccess(songs: List<Song>) {
        binding.progressBar.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.contentView.visibility = View.VISIBLE
        
        setupRecyclerViews(songs)
    }

    private fun setupRecyclerViews(allSongs: List<Song>) {
        val onSongClick = { song: Song ->
            val bundle = android.os.Bundle().apply {
                putParcelable("song", song)
                putParcelableArrayList("queue", java.util.ArrayList(allSongs))
            }
            findNavController().navigate(R.id.playerFragment, bundle)
        }
        val chunk1 = allSongs.take(15)
        val chunk2 = if (allSongs.size > 15) allSongs.subList(15, allSongs.size) else emptyList()
        
        binding.rvTrending.adapter = SongAdapter(chunk1, onSongClick)
        binding.rvNewReleases.adapter = SongAdapter(chunk2, onSongClick)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
