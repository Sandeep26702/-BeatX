package com.sandeep.beatx.activities

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import coil.load
import com.google.common.util.concurrent.ListenableFuture
import com.sandeep.beatx.R
import com.sandeep.beatx.databinding.ActivityMainBinding
import com.sandeep.beatx.service.PlaybackService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("theme_amoled", false)) {
            setTheme(R.style.Theme_BeatX_AMOLED)
        }
        
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigationView.setupWithNavController(navController)
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.playerFragment, R.id.playlistDetailFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    if (destination.id == R.id.playlistDetailFragment) {
                        updateMiniPlayerVisibility() // Keep mini player visible for playlist detail
                    } else {
                        binding.miniPlayer.root.visibility = View.GONE
                    }
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    updateMiniPlayerVisibility()
                }
            }
        }
        
        setupMiniPlayer()
    }

    private fun setupMiniPlayer() {
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            
            mediaController?.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateMiniPlayerUI()
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon()
                }
            })
            
            updateMiniPlayerUI()
        }, androidx.core.content.ContextCompat.getMainExecutor(this))
        
        binding.miniPlayer.btnMiniPlayPause.setOnClickListener {
            mediaController?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }
        
        binding.miniPlayer.root.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.playerFragment)
        }
    }
    
    private fun updateMiniPlayerUI() {
        val currentItem = mediaController?.currentMediaItem
        if (currentItem != null) {
            val metadata = currentItem.mediaMetadata
            binding.miniPlayer.tvMiniTitle.text = metadata.title
            binding.miniPlayer.tvMiniArtist.text = metadata.artist
            
            val artworkUri = metadata.artworkUri
            if (artworkUri != null) {
                binding.miniPlayer.ivMiniAlbumArt.load(artworkUri) {
                    crossfade(true)
                }
            }
            updatePlayPauseIcon()
            
            // Only show if not on player screen
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val currentDest = navHostFragment.navController.currentDestination?.id
            if (currentDest != R.id.playerFragment && currentDest != R.id.splashFragment && currentDest != R.id.loginFragment) {
                if (binding.miniPlayer.root.visibility == View.GONE) {
                    binding.miniPlayer.root.visibility = View.VISIBLE
                    binding.miniPlayer.root.translationY = binding.miniPlayer.root.height.toFloat()
                    binding.miniPlayer.root.animate().translationY(0f).setDuration(300).start()
                }
            }
        } else {
            if (binding.miniPlayer.root.visibility == View.VISIBLE) {
                binding.miniPlayer.root.animate().translationY(binding.miniPlayer.root.height.toFloat())
                    .setDuration(300).withEndAction {
                        binding.miniPlayer.root.visibility = View.GONE
                    }.start()
            }
        }
    }
    
    private fun updateMiniPlayerVisibility() {
        val currentItem = mediaController?.currentMediaItem
        if (currentItem != null) {
            binding.miniPlayer.root.visibility = View.VISIBLE
            binding.miniPlayer.root.translationY = 0f
        } else {
            binding.miniPlayer.root.visibility = View.GONE
        }
    }
    
    private fun updatePlayPauseIcon() {
        if (mediaController?.isPlaying == true) {
            binding.miniPlayer.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            binding.miniPlayer.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
