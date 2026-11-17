package com.liquidmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: PlayerViewModel = viewModel()
            LiquidApp(vm)
        }
    }
}

private fun fmt(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return String.format(Locale.US, "%d:%02d", s / 60, s % 60)
}

@Composable
private fun accentOf(hue: Float): Color {
    val dark = LocalPal.current.dark
    return Color.hsv(hue, if (dark) 0.55f else 0.80f, if (dark) 1f else 0.85f)
}

@Composable
private fun rememberPosition(vm: PlayerViewModel): State<Long> =
    produceState(0L, vm.currentId, vm.isPlaying) {
        while (true) {
            value = vm.position()
            delay(250)
        }
    }

private fun matches(s: Song, q: String): Boolean =
    q.isEmpty() || s.title.contains(q, true) || s.artist.contains(q, true) || s.album.contains(q, true)

private data class Group(val key: Long, val name: String, val sub: String, val songs: List<Song>)

@Composable
fun LiquidApp(vm: PlayerViewModel) {
    val ctx = LocalContext.current
    val sysDark = isSystemInDarkTheme()
    val pal = if (vm.dark ?: sysDark) DarkPal else LightPal

    val perm = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(ctx, perm) == PackageManager.PERMISSION_GRANTED)
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        if (ok && Build.VERSION.SDK_INT >= 33) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    LaunchedEffect(granted) { if (granted) vm.loadSongs() }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showNp by rememberSaveable { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalPal provides pal,
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        val cur = vm.current
        Box(Modifier.fillMaxSize()) {
            LiquidBackground(cur?.hue ?: 215f)

            if (!granted) {
                PermissionCard { launcher.launch(perm) }
            } else {
                Library(vm, sysDark, tab)
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (cur != null) MiniPlayer(vm, cur) { showNp = true }
                    TabBar(tab, cur?.hue ?: 215f) { tab = it }
                }
            }

            // Registered after Library's handler so it wins: Back closes the player first.
            BackHandler(enabled = showNp) { showNp = false }
            AnimatedVisibility(
                visible = showNp && cur != null,
                enter = slideInVertically(tween(420)) { it },
                exit = slideOutVertically(tween(320)) { it },
            ) {
                if (cur != null) NowPlaying(vm, cur) { showNp = false }
            }
        }
    }
}

@Composable
private fun PermissionCard(onAllow: () -> Unit) {
    val p = LocalPal.current
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().glass(RoundedCornerShape(34.dp)).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Rounded.MusicNote, null, tint = p.fg, modifier = Modifier.size(56.dp))
            T("Liquid Music", size = 26.sp, weight = FontWeight.Bold)
            T(
                "عشان أعرض وأشغّل الأغاني اللي على جهازك محتاج صلاحية الوصول للصوتيات. الأغاني بتفضل على جهازك ومبتتبعتش لأي مكان.",
                size = 15.sp, color = p.muted, maxLines = 6,
            )
            Box(
                Modifier.clip(CircleShape).background(accentOf(215f)).clickable(onClick = onAllow)
                    .padding(horizontal = 28.dp, vertical = 14.dp)
            ) { T("السماح بالوصول للأغاني", weight = FontWeight.SemiBold, color = Color(0xFF07090F)) }
        }
    }
}

@Composable
private fun Library(vm: PlayerViewModel, sysDark: Boolean, tab: Int) {
    val p = LocalPal.current
    var query by rememberSaveable { mutableStateOf("") }
    var detail by remember { mutableStateOf<Pair<String, List<Song>>?>(null) }
    LaunchedEffect(tab) { detail = null }
    BackHandler(enabled = detail != null) { detail = null }

    val q = query.trim()
    val songs = vm.songs
    val filtered = remember(songs, q) { songs.filter { matches(it, q) } }
    val favSongs = remember(filtered, vm.favs) { filtered.filter { it.id in vm.favs } }
    val albums = remember(filtered) {
        filtered.groupBy { it.albumId }
            .map { (id, l) -> Group(id, l[0].album, l[0].artist, l) }
            .sortedBy { it.name.lowercase() }
    }
    val artists = remember(filtered) {
        filtered.groupBy { it.artistId }
            .map { (id, l) -> Group(id, l[0].artist, "${l.size} أغنية", l) }
            .sortedBy { it.name.lowercase() }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            T("Liquid Music", size = 28.sp, weight = FontWeight.Bold)
            IconButton(onClick = { vm.toggleDark(sysDark) }, modifier = Modifier.glass(CircleShape)) {
                Icon(if (p.dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, null, tint = p.fg)
            }
        }

        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(color = p.fg, fontSize = 16.sp),
            cursorBrush = SolidColor(p.fg),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(
                    Modifier.fillMaxWidth().height(52.dp).glass(RoundedCornerShape(26.dp)).padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Search, null, tint = p.muted)
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) T("ابحث عن أغنية أو فنان أو ألبوم", color = p.muted)
                        inner()
                    }
                }
            },
        )
        Spacer(Modifier.height(12.dp))

        val d = detail
        if (d != null) {
            DetailHeader(vm, d.first, d.second) { detail = null }
            SongList(vm, d.second, showActions = false)
        } else when (tab) {
            0 -> SongList(vm, filtered, showActions = true, loading = vm.loading, hasAny = songs.isNotEmpty())
            1 -> LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(bottom = 220.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                gridItems(albums, key = { it.key }) { g ->
                    Column(
                        Modifier.clip(RoundedCornerShape(22.dp)).clickable { detail = g.name to g.songs },
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Art(g.songs[0], g.songs[0].hue, Modifier.fillMaxWidth().aspectRatio(1f), 22.dp)
                        T(g.name, weight = FontWeight.SemiBold)
                        T(g.sub, size = 13.sp, color = p.muted)
                    }
                }
            }
            2 -> LazyColumn(contentPadding = PaddingValues(bottom = 220.dp)) {
                items(artists, key = { it.key }) { g ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .clickable { detail = g.name to g.songs }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Art(g.songs[0], g.songs[0].hue, Modifier.size(54.dp), 27.dp)
                        Column(Modifier.weight(1f)) {
                            T(g.name, weight = FontWeight.SemiBold)
                            T(g.sub, size = 13.sp, color = p.muted)
                        }
                    }
                }
            }
            else -> SongList(vm, favSongs, showActions = true, emptyText = "لسه ماضفتش أغاني للمفضلة. دوس على القلب جنب أي أغنية.")
        }
    }
}

@Composable
private fun DetailHeader(vm: PlayerViewModel, title: String, list: List<Song>, onBack: () -> Unit) {
    val p = LocalPal.current
    Row(
        Modifier.fillMaxWidth().padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.glass(CircleShape)) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = p.fg)
        }
        Column(Modifier.weight(1f)) {
            T(title, size = 20.sp, weight = FontWeight.Bold)
            T("${list.size} أغنية", size = 13.sp, color = p.muted)
        }
    }
}

@Composable
private fun SongList(
    vm: PlayerViewModel,
    list: List<Song>,
    showActions: Boolean,
    loading: Boolean = false,
    hasAny: Boolean = true,
    emptyText: String? = null,
) {
    val p = LocalPal.current
    if (list.isEmpty()) {
        val msg = when {
            loading -> "جاري تحميل أغانيك..."
            emptyText != null -> emptyText
            !hasAny -> "مفيش أغاني على الجهاز. حط ملفات صوتية في فولدر Music وارجع."
            else -> "ملقيناش نتيجة."
        }
        Box(Modifier.fillMaxWidth().glass(RoundedCornerShape(26.dp)).padding(24.dp)) {
            T(msg, color = p.muted, maxLines = 4)
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 220.dp)) {
        item(key = "actions") {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ActionPill("تشغيل", Icons.Rounded.PlayArrow, filled = true, modifier = Modifier.weight(1f)) {
                    vm.play(list[0], list)
                }
                ActionPill("عشوائي", Icons.Rounded.Shuffle, filled = false, modifier = Modifier.weight(1f)) {
                    vm.playShuffled(list)
                }
            }
        }
        items(list, key = { it.id }) { SongRow(it, vm, list) }
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    filled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val p = LocalPal.current
    val accent = accentOf(215f)
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier
            .height(46.dp)
            .then(if (filled) Modifier.clip(shape).background(accent) else Modifier.glass(shape))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val c = if (filled) Color(0xFF07090F) else p.fg
        Icon(icon, null, tint = c, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(6.dp))
        T(label, weight = FontWeight.SemiBold, color = c)
    }
}

@Composable
private fun SongRow(song: Song, vm: PlayerViewModel, queue: List<Song>) {
    val p = LocalPal.current
    val playing = vm.currentId == song.id
    val fav = song.id in vm.favs
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (playing) p.fg.copy(alpha = 0.08f) else Color.Transparent)
            .clickable { vm.play(song, queue) }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Art(song, song.hue, Modifier.size(52.dp), 14.dp)
        Column(Modifier.weight(1f)) {
            T(song.title, weight = FontWeight.SemiBold, color = if (playing) accentOf(song.hue) else p.fg)
            T("${song.artist} · ${song.album}", size = 13.sp, color = p.muted)
        }
        IconButton(onClick = { vm.toggleFav(song.id) }) {
            Icon(
                if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "مفضلة",
                tint = if (fav) Color(0xFFFF3B5C) else p.muted,
            )
        }
        T(fmt(song.durationMs), size = 12.sp, color = p.muted)
    }
}

@Composable
private fun MiniPlayer(vm: PlayerViewModel, song: Song, onOpen: () -> Unit) {
    val p = LocalPal.current
    val accent = accentOf(song.hue)
    val pos by rememberPosition(vm)
    val frac = (pos.toFloat() / song.durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
    Box(
        Modifier
            .padding(horizontal = 14.dp)
            .fillMaxWidth()
            .glass(RoundedCornerShape(28.dp), strong = true)
            .clickable(onClick = onOpen)
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Art(song, song.hue, Modifier.size(46.dp), 12.dp)
            Column(Modifier.weight(1f)) {
                T(song.title, size = 14.sp, weight = FontWeight.SemiBold)
                T(song.artist, size = 12.sp, color = p.muted)
            }
            IconButton(onClick = { vm.toggle() }) {
                Icon(
                    if (vm.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "تشغيل / إيقاف", tint = p.fg, modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "التالي", tint = p.fg, modifier = Modifier.size(28.dp))
            }
        }
        Box(
            Modifier.align(Alignment.BottomStart).padding(horizontal = 24.dp).fillMaxWidth().height(2.dp)
                .clip(RoundedCornerShape(2.dp)).background(p.fg.copy(alpha = 0.12f))
        ) {
            Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(accent))
        }
    }
}

@Composable
private fun TabBar(tab: Int, hue: Float, onTab: (Int) -> Unit) {
    val p = LocalPal.current
    val accent by animateColorAsState(accentOf(hue), tween(600), label = "tabAccent")
    val items = listOf(
        "الأغاني" to Icons.Rounded.MusicNote,
        "الألبومات" to Icons.Rounded.Album,
        "الفنانين" to Icons.Rounded.Person,
        "المفضلة" to Icons.Rounded.Favorite,
    )
    Row(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().glass(RoundedCornerShape(34.dp), strong = true).padding(6.dp)
    ) {
        items.forEachIndexed { i, (label, icon) ->
            val sel = i == tab
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (sel) accent.copy(alpha = 0.22f) else Color.Transparent)
                    .clickable { onTab(i) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(icon, contentDescription = label, tint = if (sel) accent else p.muted, modifier = Modifier.size(24.dp))
                T(label, size = 11.sp, color = if (sel) accent else p.muted)
            }
        }
    }
}

@Composable
private fun NowPlaying(vm: PlayerViewModel, song: Song, onClose: () -> Unit) {
    val p = LocalPal.current
    val accent = accentOf(song.hue)
    val onAccent = if (p.dark) Color(0xFF07090F) else Color.White
    val pos by rememberPosition(vm)
    var drag by remember { mutableStateOf<Float?>(null) }
    val scale by animateFloatAsState(if (vm.isPlaying) 1f else 0.88f, tween(450), label = "artScale")
    val fav = song.id in vm.favs
    val dur = song.durationMs.coerceAtLeast(1L).toFloat()
    val shown = drag ?: pos.toFloat().coerceIn(0f, dur)

    Column(
        Modifier
            .fillMaxSize()
            .background(p.bg.copy(alpha = 0.78f))
            .pointerInput(Unit) { detectTapGestures { } }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.glass(CircleShape)) {
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "إغلاق", tint = p.fg)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                T("التشغيل الآن", size = 14.sp, color = p.muted)
            }
            Spacer(Modifier.size(48.dp))
        }
        Spacer(Modifier.weight(1f))
        Art(
            song, song.hue,
            Modifier.fillMaxWidth().aspectRatio(1f).graphicsLayer { scaleX = scale; scaleY = scale },
            corner = 36.dp,
        )
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                T(song.title, size = 24.sp, weight = FontWeight.Bold)
                T("${song.artist} · ${song.album}", color = p.muted)
            }
            IconButton(onClick = { vm.toggleFav(song.id) }, modifier = Modifier.glass(CircleShape)) {
                Icon(
                    if (fav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "مفضلة",
                    tint = if (fav) Color(0xFFFF3B5C) else p.fg,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(Modifier.fillMaxWidth()) {
                Slider(
                    value = shown,
                    onValueChange = { drag = it },
                    onValueChangeFinished = {
                        drag?.let { vm.seekTo(it.toLong()) }
                        drag = null
                    },
                    valueRange = 0f..dur,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = accent,
                        inactiveTrackColor = p.fg.copy(alpha = 0.18f),
                    ),
                )
                Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    T(fmt(shown.toLong()), size = 12.sp, color = p.muted)
                    T(fmt(song.durationMs), size = 12.sp, color = p.muted)
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { vm.toggleShuffle() }) {
                        Icon(Icons.Rounded.Shuffle, "عشوائي", tint = if (vm.shuffle) accent else p.muted, modifier = Modifier.size(26.dp))
                    }
                    IconButton(onClick = { vm.prev() }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Rounded.SkipPrevious, "السابق", tint = p.fg, modifier = Modifier.size(38.dp))
                    }
                    Box(
                        Modifier.size(78.dp).clip(CircleShape).background(accent).clickable { vm.toggle() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (vm.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "تشغيل / إيقاف", tint = onAccent, modifier = Modifier.size(40.dp),
                        )
                    }
                    IconButton(onClick = { vm.next() }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Rounded.SkipNext, "التالي", tint = p.fg, modifier = Modifier.size(38.dp))
                    }
                    IconButton(onClick = { vm.cycleRepeat() }) {
                        Icon(
                            if (vm.repeat == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                            "تكرار",
                            tint = if (vm.repeat != Player.REPEAT_MODE_OFF) accent else p.muted,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
