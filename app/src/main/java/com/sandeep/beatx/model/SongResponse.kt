package com.sandeep.beatx.model

import com.google.gson.annotations.SerializedName

data class SongResponse(
    @SerializedName("data") val data: List<TrackDto>
)

data class TrackDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("preview") val preview: String,
    @SerializedName("artist") val artist: ArtistDto,
    @SerializedName("album") val album: AlbumDto
)

data class ArtistDto(
    @SerializedName("name") val name: String
)

data class AlbumDto(
    @SerializedName("title") val title: String,
    @SerializedName("cover_medium") val coverMedium: String,
    @SerializedName("cover_xl") val coverXl: String?
)
