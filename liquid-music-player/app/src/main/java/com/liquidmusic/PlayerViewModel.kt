package com.liquidmusic

import android.app.Application
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val artistId: Long,
    val durationMs: Long,
) {
    val uri: Uri
        get() = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    val artUri: Uri
        get() = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    val hue: Float
        get() = ((albumId * 37L) % 360L).toFloat()
}

object MusicRepo {
    private const val UNKNOWN_ARTIST = "فنان غير معروف"
    private const val UNKNOWN_ALBUM = "ألبوم غير معروف"

    fun load(ctx: Context): List<Song> {
        val out = ArrayList<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val order = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        ctx.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, order
        )?.use { c ->
            val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val iAlbumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val iArtistId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val artist = c.getString(iArtist)
                val album = c.getString(iAlbum)
                out.add(
                    Song(
                        id = c.getLong(iId),
                        title = c.getString(iTitle) ?: "بدون عنوان",
                        artist = if (artist.isNullOrBlank() || artist == MediaStore.UNKNOWN_STRING) UNKNOWN_ARTIST else artist,
                        album = if (album.isNullOrBlank() || album == MediaStore.UNKNOWN_STRING) UNKNOWN_ALBUM else album,
                        albumId = c.getLong(iAlbumId),
                        artistId = c.getLong(iArtistId),
                        durationMs = c.getLong(iDur),
                    )
                )
            }
        }
        return out
    }
}

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("liquid_music", Context.MODE_PRIVATE)

    var songs by mutableStateOf<List<Song>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var favs by mutableStateOf(
        (prefs.getStringSet("favs", emptySet()) ?: emptySet()).mapNotNull { it.toLongOrNull() }.toSet()
    ); private set
    var dark by mutableStateOf<Boolean?>(
        when (prefs.getInt("dark", -1)) { 0 -> false; 1 -> true; else -> null }
    ); private set

    var currentId by mutableStateOf<Long?>(null); private set
    var isPlaying by mutableStateOf(false); private set
    var shuffle by mutableStateOf(false); private set
    var repeat by mutableIntStateOf(Player.REPEAT_MODE_OFF); private set

    val current: Song? get() = songs.firstOrNull { it.id == currentId }

    private var controller: MediaController? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) { this@PlayerViewModel.isPlaying = isPlaying }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            currentId = mediaItem?.mediaId?.toLongOrNull()
        }
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) { shuffle = shuffleModeEnabled }
        override fun onRepeatModeChanged(repeatMode: Int) { repeat = repeatMode }
    }

    init {
        connect()
    }

    private fun connect() {
        val ctx = getApplication<Application>()
        val token = SessionToken(ctx, ComponentName(ctx, PlaybackService::class.java))
        val future = MediaController.Builder(ctx, token).buildAsync()
        future.addListener({
            try {
                val c = future.get()
                controller = c
                c.addListener(listener)
                currentId = c.currentMediaItem?.mediaId?.toLongOrNull()
                isPlaying = c.isPlaying
                shuffle = c.shuffleModeEnabled
                repeat = c.repeatMode
            } catch (_: Exception) {
            }
        }, ContextCompat.getMainExecutor(ctx))
    }

    fun loadSongs() {
        if (loading) return
        loading = true
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { MusicRepo.load(getApplication()) }
            songs = list
            loading = false
        }
    }

    private fun Song.toItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(artUri)
                    .build()
            )
            .build()

    fun play(song: Song, queue: List<Song>) {
        val c = controller ?: return
        val list = if (queue.any { it.id == song.id }) queue else listOf(song)
        val index = list.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        c.setMediaItems(list.map { it.toItem() }, index, 0L)
        c.prepare()
        c.play()
    }

    fun playShuffled(queue: List<Song>) {
        val c = controller ?: return
        if (queue.isEmpty()) return
        c.shuffleModeEnabled = true
        c.setMediaItems(queue.map { it.toItem() }, queue.indices.random(), 0L)
        c.prepare()
        c.play()
    }

    fun toggle() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() { controller?.seekToNext() }
    fun prev() { controller?.seekToPrevious() }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }
    fun position(): Long = controller?.currentPosition ?: 0L

    fun toggleShuffle() { controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
    fun cycleRepeat() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun toggleFav(id: Long) {
        favs = if (id in favs) favs - id else favs + id
        prefs.edit().putStringSet("favs", favs.map { it.toString() }.toSet()).apply()
    }

    fun toggleDark(systemDark: Boolean) {
        val now = dark ?: systemDark
        dark = !now
        prefs.edit().putInt("dark", if (!now) 1 else 0).apply()
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        super.onCleared()
    }
}
