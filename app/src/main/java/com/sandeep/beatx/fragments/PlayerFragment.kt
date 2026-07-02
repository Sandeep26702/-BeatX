package com.sandeep.beatx.fragments

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.sandeep.beatx.R
import com.sandeep.beatx.databinding.FragmentPlayerBinding
import com.sandeep.beatx.model.Song
import com.sandeep.beatx.service.PlaybackService
import com.sandeep.beatx.viewmodel.FavoriteViewModel
import com.sandeep.beatx.viewmodel.RecentlyPlayedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var player: Player? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var progressRunnable: Runnable
    
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val recentlyPlayedViewModel: RecentlyPlayedViewModel by viewModels()
    private var isCurrentSongFavorite = false
    private var currentSong: Song? = null
    private var favoriteJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val passedSong = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("song", Song::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("song")
        }

        setupPlayer(passedSong)
        setupControls()
    }
    
    private fun MediaItem.toSong(): Song {
        return Song(
            id = mediaId,
            title = mediaMetadata.title?.toString() ?: "Unknown Title",
            artist = mediaMetadata.artist?.toString() ?: "Unknown Artist",
            album = mediaMetadata.albumTitle?.toString() ?: "",
            imageUrl = mediaMetadata.artworkUri?.toString() ?: "",
            audioUrl = localConfiguration?.uri?.toString() ?: "",
            duration = ""
        )
    }

    private fun setupPlayer(passedSong: Song?) {
        val allSongs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelableArrayList("queue", Song::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelableArrayList("queue") ?: emptyList()
        }
        
        val sessionToken = SessionToken(requireContext(), ComponentName(requireContext(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(requireContext(), sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            player = controllerFuture?.get()
            
            // If the player is already playing the requested song, we don't need to re-initialize
            val isPlayingSameSong = passedSong != null && player?.currentMediaItem?.mediaId == passedSong.id
            
            if (passedSong != null && !isPlayingSameSong) {
                val startIndex = passedSong.let { song -> allSongs.indexOfFirst { it.id == song.id } }.takeIf { it >= 0 } ?: 0
                
                val mediaItems = allSongs.map { song -> 
                    val metadata = MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(Uri.parse(song.imageUrl))
                        .build()
                        
                    MediaItem.Builder()
                        .setUri(song.audioUrl)
                        .setMediaId(song.id)
                        .setMediaMetadata(metadata)
                        .build()
                }
                
                player?.setMediaItems(mediaItems, startIndex.coerceAtLeast(0), 0L)
                player?.prepare()
                player?.play()
            } else {
                // If returning to player, update UI with current song immediately
                currentSong = passedSong ?: player?.currentMediaItem?.toSong()
                currentSong?.let { 
                    updateUIForSong(it)
                    checkFavoriteStatus(it)
                }
                updatePlayPauseIcon()
            }
            
            player?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    
                    // Match playing item back to original Song list to get full details
                    val matchingSong = allSongs.find { it.id == mediaItem?.mediaId } ?: mediaItem?.toSong()
                    currentSong = matchingSong
                    currentSong?.let { 
                        updateUIForSong(it)
                        checkFavoriteStatus(it)
                        recentlyPlayedViewModel.addSong(it)
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    updatePlayPauseIcon()
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        updateProgressUI()
                    }
                }
            })
            
            setupProgressPolling()
        }, androidx.core.content.ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun updatePlayPauseIcon() {
        if (player?.isPlaying == true) {
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        }
    }
    
    private fun updateUIForSong(song: Song) {
        binding.tvSongName.text = song.title
        binding.tvArtist.text = song.artist
        binding.ivAlbumArt.load(song.imageUrl) {
            crossfade(true)
        }
    }
    
    private fun checkFavoriteStatus(song: Song) {
        favoriteJob?.cancel()
        favoriteJob = viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favoriteViewModel.checkIsFavorite(song.id).collect { isFav ->
                    isCurrentSongFavorite = isFav
                    if (isFav) {
                        binding.btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        binding.btnFavorite.setImageResource(R.drawable.ic_heart)
                    }
                }
            }
        }
    }

    private fun setupControls() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnFavorite.setOnClickListener {
            currentSong?.let { song ->
                favoriteViewModel.toggleFavorite(song, isCurrentSongFavorite)
            }
        }

        binding.btnPlayPause.setOnClickListener {
            player?.let { p -> 
                if (p.isPlaying) {
                    p.pause()
                } else {
                    p.play()
                }
            }
        }

        binding.btnNext.setOnClickListener {
            player?.seekToNextMediaItem()
        }
        
        binding.btnPrevious.setOnClickListener {
            player?.seekToPreviousMediaItem()
        }

        binding.btnShuffle.setOnClickListener {
            player?.let { p ->
                p.shuffleModeEnabled = !p.shuffleModeEnabled
                val toastMsg = if(p.shuffleModeEnabled) "Shuffle On" else "Shuffle Off"
                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRepeat.setOnClickListener {
            player?.let { p ->
                p.repeatMode = if (p.repeatMode == Player.REPEAT_MODE_OFF) {
                    Toast.makeText(context, "Repeat All", Toast.LENGTH_SHORT).show()
                    Player.REPEAT_MODE_ALL
                } else {
                    Toast.makeText(context, "Repeat Off", Toast.LENGTH_SHORT).show()
                    Player.REPEAT_MODE_OFF
                }
            }
        }
        
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    player?.seekTo(progress.toLong())
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnAddToPlaylist.setOnClickListener {
            currentSong?.let { song ->
                val bottomSheet = BottomSheetAddToPlaylist(song)
                bottomSheet.show(childFragmentManager, "AddToPlaylist")
            }
        }

        binding.btnShare.setOnClickListener {
            currentSong?.let { song ->
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Listening to ${song.title} by ${song.artist} on BeatX! \n${song.audioUrl}")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "Share Song"))
            }
        }

        binding.btnSpeed.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
            speeds.forEachIndexed { index, speed ->
                popup.menu.add(0, index, 0, "${speed}x")
            }
            popup.setOnMenuItemClickListener { item ->
                val speed = speeds[item.itemId]
                player?.playbackParameters = PlaybackParameters(speed)
                Toast.makeText(requireContext(), "Speed set to ${speed}x", Toast.LENGTH_SHORT).show()
                true
            }
            popup.show()
        }

        var sleepTimerJob: Job? = null
        binding.btnTimer.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            val options = listOf("15 mins", "30 mins", "45 mins", "1 Hour", "Off")
            val minutes = listOf(15, 30, 45, 60, 0)
            options.forEachIndexed { index, title ->
                popup.menu.add(0, index, 0, title)
            }
            popup.setOnMenuItemClickListener { item ->
                val mins = minutes[item.itemId]
                sleepTimerJob?.cancel()
                if (mins > 0) {
                    sleepTimerJob = viewLifecycleOwner.lifecycleScope.launch {
                        Toast.makeText(requireContext(), "Timer set for $mins minutes", Toast.LENGTH_SHORT).show()
                        delay(mins * 60 * 1000L)
                        player?.pause()
                    }
                } else {
                    Toast.makeText(requireContext(), "Sleep Timer Off", Toast.LENGTH_SHORT).show()
                }
                true
            }
            popup.show()
        }

        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null) return false
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            player?.seekToPreviousMediaItem()
                        } else {
                            player?.seekToNextMediaItem()
                        }
                        return true
                    }
                }
                return false
            }
        })
        
        binding.ivAlbumArt.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        binding.btnQueue.setOnClickListener {
            player?.let { p ->
                val currentIdx = p.currentMediaItemIndex
                val queue = mutableListOf<Song>()
                for (i in currentIdx until p.mediaItemCount) {
                    val mediaItem = p.getMediaItemAt(i)
                    queue.add(
                        Song(
                            id = mediaItem.mediaId,
                            title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown",
                            artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown",
                            album = mediaItem.mediaMetadata.albumTitle?.toString() ?: "",
                            imageUrl = mediaItem.mediaMetadata.artworkUri?.toString() ?: "",
                            audioUrl = mediaItem.localConfiguration?.uri?.toString() ?: "",
                            duration = ""
                        )
                    )
                }
                
                val bottomSheet = BottomSheetQueue(queue) { _, index ->
                    p.seekToDefaultPosition(currentIdx + index)
                }
                bottomSheet.show(childFragmentManager, "QueueSheet")
            }
        }
    }
    
    private fun setupProgressPolling() {
        progressRunnable = Runnable {
            updateProgressUI()
            handler.postDelayed(progressRunnable, 500)
        }
        handler.post(progressRunnable)
    }
    
    private fun updateProgressUI() {
        player?.let { p ->
            val currentPos = p.currentPosition
            val duration = p.duration
            
            if (duration > 0) {
                binding.seekBar.max = duration.toInt()
                binding.seekBar.progress = currentPos.toInt()
                
                binding.tvCurrentTime.text = formatTime(currentPos)
                binding.tvTotalDuration.text = formatTime(duration)
            }
        }
    }
    
    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::progressRunnable.isInitialized) {
            handler.removeCallbacks(progressRunnable)
        }
        controllerFuture?.let { 
            MediaController.releaseFuture(it) 
        }
        controllerFuture = null
        player = null
        favoriteJob?.cancel()
        _binding = null
    }
}
