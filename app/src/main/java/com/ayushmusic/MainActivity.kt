package com.ayushmusic

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture

data class Track(val title: String, val uri: Uri, val online: Boolean)

class MainActivity : ComponentActivity() {
    private var controllerFuture: ListenableFuture<MediaController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF9B5CFF),
                secondary = Color(0xFF1ED760),
                background = Color(0xFF090B10),
                surface = Color(0xFF151923)
            )) {
                val tracks = remember { mutableStateListOf<Track>() }
                var currentIndex by remember { mutableIntStateOf(-1) }
                var playing by remember { mutableStateOf(false) }
                var search by remember { mutableStateOf("") }
                var url by remember { mutableStateOf("") }
                var message by remember { mutableStateOf("Add MP3 files or paste a direct audio stream URL.") }
                var controller by remember { mutableStateOf<MediaController?>(null) }

                DisposableEffect(controllerFuture) {
                    val future = controllerFuture
                    future?.addListener({
                        try {
                            controller = future.get()
                        } catch (_: Exception) {
                            message = "Player service could not start. Reopen the app and try again."
                        }
                    }, ContextCompat.getMainExecutor(this@MainActivity))
                    onDispose { }
                }

                val pickAudio = rememberLauncherForActivityResult(
                    ActivityResultContracts.OpenMultipleDocuments()
                ) { uris ->
                    uris.forEach { uri ->
                        try {
                            contentResolver.takePersistableUriPermission(
                                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) { }
                        val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfter(':')
                            ?.takeIf { it.isNotBlank() } ?: "Local audio"
                        if (tracks.none { it.uri == uri }) tracks.add(Track(name, uri, false))
                    }
                    if (uris.isNotEmpty()) message = "Added ${uris.size} local file(s). Tap a track to play."
                }

                Column(
                    Modifier.fillMaxSize().background(Color(0xFF090B10)).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("♫", color = Color(0xFF9B5CFF), fontSize = 34.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("AyushMusic", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Your music. Your way.", color = Color.LightGray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF1ED760))
                    }
                    Spacer(Modifier.height(18.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF20113B)),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("YOUR SOUNDTRACK", color = Color(0xFFBFA2FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Text("Stream or play your own tracks", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(5.dp))
                            Text("Local MP3 • Direct audio streams • Background playback", color = Color(0xFFD4CBE7), fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = search, onValueChange = { search = it },
                        label = { Text("Search your library") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { pickAudio.launch(arrayOf("audio/*")) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(Modifier.width(6.dp)); Text("Add MP3")
                        }
                        OutlinedButton(onClick = {
                            val clean = url.trim()
                            if (clean.startsWith("https://") && (clean.contains(".mp3") || clean.contains(".m4a") || clean.contains(".aac") || clean.contains(".ogg") || clean.contains(".wav") || clean.contains("stream"))) {
                                val title = clean.substringAfterLast('/').substringBefore('?').ifBlank { "Online stream" }
                                if (tracks.none { it.uri.toString() == clean }) tracks.add(Track(title, Uri.parse(clean), true))
                                url = ""
                                message = "Stream added. Tap it to play."
                            } else {
                                message = "Paste a direct HTTPS audio-file or stream URL. Web-page links (e.g. YouTube pages) are not direct audio streams."
                            }
                        }) { Icon(Icons.Default.Add, contentDescription = null); Text("Add URL") }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = url, onValueChange = { url = it },
                        label = { Text("Direct HTTPS audio URL") },
                        placeholder = { Text("https://example.com/song.mp3") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(message, color = Color(0xFFB8B8C5), fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("YOUR LIBRARY", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${tracks.size} tracks", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        itemsIndexed(tracks.filter { it.title.contains(search, true) || it.uri.toString().contains(search, true) }) { _, track ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF151923)),
                                shape = RoundedCornerShape(13.dp),
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val c = controller
                                    if (c == null) {
                                        message = "Player is starting. Please tap again in a moment."
                                    } else {
                                        val realIndex = tracks.indexOf(track)
                                        c.setMediaItems(tracks.map { MediaItem.fromUri(it.uri) }, realIndex, 0L)
                                        c.prepare()
                                        c.play()
                                        currentIndex = realIndex
                                        playing = true
                                        message = "Playing: ${track.title}"
                                    }
                                }
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(42.dp).background(
                                            Brush.linearGradient(listOf(Color(0xFF6E36D8), Color(0xFF1B9C79))),
                                            RoundedCornerShape(10.dp)
                                        ), contentAlignment = Alignment.Center
                                    ) { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(if (track.online) "ONLINE STREAM" else "LOCAL AUDIO", color = Color(0xFF9B9BAA), fontSize = 10.sp)
                                    }
                                    IconButton(onClick = {
                                        tracks.remove(track)
                                        if (currentIndex >= tracks.size) currentIndex = tracks.lastIndex
                                    }) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray) }
                                }
                            }
                        }
                        if (tracks.isEmpty()) {
                            item {
                                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("♫", fontSize = 42.sp, color = Color(0xFF7650C8))
                                    Text("Your library is empty", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Choose MP3 files or add a direct stream URL above.", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF20232C)), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (currentIndex in tracks.indices) tracks[currentIndex].title else "Nothing playing",
                                color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    controller?.let { c ->
                                        if (c.hasPreviousMediaItem()) c.seekToPreviousMediaItem()
                                        else if (c.mediaItemCount > 0) c.seekTo(0, 0)
                                    }
                                }) { Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White) }
                                FilledIconButton(onClick = {
                                    controller?.let { c ->
                                        if (c.isPlaying) { c.pause(); playing = false }
                                        else if (c.mediaItemCount > 0) { c.play(); playing = true }
                                        else message = "Choose a track first."
                                    }
                                }) {
                                    Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play or pause")
                                }
                                IconButton(onClick = { controller?.seekToNextMediaItem() }) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Use only music you own or are authorized to stream.", color = Color(0xFF777783), fontSize = 10.sp)
                }
            }
        }
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onDestroy()
    }
}
