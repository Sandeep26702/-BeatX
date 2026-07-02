package com.sandeep.beatx.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val imageUrl: String,
    val audioUrl: String,
    val duration: String
) : Parcelable
