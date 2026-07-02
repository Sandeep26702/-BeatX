package com.sandeep.beatx.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sandeep.beatx.databinding.ItemSearchResultBinding
import com.sandeep.beatx.model.Song

class SearchAdapter(
    private var songs: List<Song>,
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var onSongLongClick: ((Song) -> Unit)? = null

    fun setOnItemLongClickListener(listener: (Song) -> Unit) {
        onSongLongClick = listener
    }

    class SearchViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.tvTitle.text = song.title
        holder.binding.tvArtist.text = song.artist
        holder.binding.ivAlbumArt.load(song.imageUrl) {
            crossfade(true)
        }
        
        holder.binding.root.setOnClickListener {
            onSongClick(song)
        }
        
        holder.binding.root.setOnLongClickListener {
            onSongLongClick?.invoke(song)
            true
        }
    }

    override fun getItemCount() = songs.size
    
    fun updateData(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
    
    fun getCurrentData(): List<Song> = songs
}
