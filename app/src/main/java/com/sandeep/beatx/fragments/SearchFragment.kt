package com.sandeep.beatx.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.sandeep.beatx.R
import com.sandeep.beatx.adapters.SearchAdapter
import com.sandeep.beatx.databinding.FragmentSearchBinding
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.viewmodel.SearchViewModel
import com.sandeep.beatx.viewmodel.UiState
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var searchAdapter: SearchAdapter
    private var currentSongs: List<Song> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchLogic()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter(emptyList()) { song ->
            val bundle = android.os.Bundle().apply {
                putParcelable("song", song)
                putParcelableArrayList("queue", java.util.ArrayList(currentSongs))
            }
            findNavController().navigate(R.id.playerFragment, bundle)
        }
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupSearchLogic() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.setQuery(binding.etSearch.text.toString())
                true
            } else {
                false
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.btnRetry.setOnClickListener {
            viewModel.setQuery(viewModel.lastQuery)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> showSuccess(state.data)
                        is UiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.rvSearchResults.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
        
        // Hide retry for "No songs found" vs actual errors
        if (message.contains("No songs found")) {
            binding.btnRetry.visibility = View.GONE
        } else {
            binding.btnRetry.visibility = View.VISIBLE
        }
    }
    
    private fun showSuccess(songs: List<Song>) {
        binding.progressBar.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        
        if (songs.isEmpty() && binding.etSearch.text.isNullOrBlank()) {
            binding.rvSearchResults.visibility = View.GONE
        } else {
            binding.rvSearchResults.visibility = View.VISIBLE
            currentSongs = songs
            searchAdapter.updateData(songs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
