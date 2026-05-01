package pro.ghosttunes.music.presentation.playlists

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import pro.ghosttunes.music.domain.model.Playlist
import pro.ghosttunes.music.domain.model.Track
import pro.ghosttunes.music.presentation.tracks.TracksViewModel
import pro.ghosttunes.music.presentation.tracks.formatMs

private val BgDark  = Color(0xFF080810)
private val BgDark2 = Color(0xFF0F0F1E)
private val BgDark3 = Color(0xFF151525)
private val BgDark4 = Color(0xFF1A1A2E)
private val BgDark5 = Color(0xFF202038)
private val BorderColor  = Color(0x1A9C72F5)
private val BorderColor2 = Color(0x389C72F5)
private val Purple      = Color(0xFF9C72F5)
private val Purple2     = Color(0xFF7C52D5)
private val PurpleLight = Color(0xFFC4A8FF)
private val TextPrimary = Color(0xFFF0EEFF)
private val TextMuted   = Color(0xFF6B6890)
private val TextMuted2  = Color(0xFF9896B0)
private val ColorRed    = Color(0xFFFF5F5F)

// ── Умные плейлисты ───────────────────────────────────────────────────────────
data class SmartPlaylist(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: @Composable () -> Unit,
    val gradient: List<Color>,
    val tracks: List<Track>,
)

private sealed class PlaylistView {
    object Home : PlaylistView()
    data class Detail(
        val title: String,
        val subtitle: String,
        val coverTracks: List<Track>,
        val tracks: List<Track>,
        val accentColor: Color,
        val emoji: String,
    ) : PlaylistView()
}

// ── Главный экран ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onTrackClick: (queue: List<Track>, index: Int) -> Unit,
    viewModel: TracksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentView by remember { mutableStateOf<PlaylistView>(PlaylistView.Home) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val favoriteTracks = remember(uiState.tracks) { uiState.tracks.filter { it.isFavorite } }
    val dislikedTracks = remember(uiState.tracks) { uiState.tracks.filter { it.userRating == "dislike" } }

    BackHandler(enabled = currentView !is PlaylistView.Home) {
        currentView = PlaylistView.Home
    }

    Scaffold(containerColor = BgDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding),
        ) {
            // Топ-бар
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark2)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentView !is PlaylistView.Home) {
                    IconButton(onClick = { currentView = PlaylistView.Home }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = when (val v = currentView) {
                        is PlaylistView.Home -> "Плейлисты"
                        is PlaylistView.Detail -> v.title
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (currentView is PlaylistView.Home) {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, null, tint = PurpleLight)
                    }
                } else {
                    val v = currentView as PlaylistView.Detail
                    Text("${v.tracks.size} тр.", fontSize = 12.sp, color = TextMuted)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))

            when (val view = currentView) {
                is PlaylistView.Home -> {
                    HomeContent(
                        favoriteTracks = favoriteTracks,
                        dislikedTracks = dislikedTracks,
                        userPlaylists = uiState.playlists,
                        allTracks = uiState.tracks,
                        onSmartPlaylistClick = { title, subtitle, tracks, accent, emoji ->
                            currentView = PlaylistView.Detail(
                                title = title,
                                subtitle = subtitle,
                                coverTracks = tracks.take(4),
                                tracks = tracks,
                                accentColor = accent,
                                emoji = emoji,
                            )
                        },
                        onPlaylistClick = { playlist ->
                            val playlistTracks = uiState.tracks // TODO: load playlist tracks
                            currentView = PlaylistView.Detail(
                                title = playlist.title,
                                subtitle = "${playlist.trackCount} треков",
                                coverTracks = emptyList(),
                                tracks = playlistTracks.take(playlist.trackCount),
                                accentColor = Purple,
                                emoji = "🎵",
                            )
                        },
                    )
                }

                is PlaylistView.Detail -> {
                    DetailContent(
                        view = view,
                        onTrackClick = onTrackClick,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDislike = { viewModel.rateTrack(it.id, "dislike") },
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onConfirm = { title -> viewModel.createPlaylist(title); showCreateDialog = false },
            onDismiss = { showCreateDialog = false },
        )
    }
}

// ── Главная страница плейлистов ───────────────────────────────────────────────
@Composable
private fun HomeContent(
    favoriteTracks: List<Track>,
    dislikedTracks: List<Track>,
    userPlaylists: List<Playlist>,
    allTracks: List<Track>,
    onSmartPlaylistClick: (String, String, List<Track>, Color, String) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Умные плейлисты
        item {
            Text(
                "Умные плейлисты",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
            )
        }

        // Любимое
        item {
            SmartPlaylistCard(
                title = "Любимое",
                subtitle = "${favoriteTracks.size} треков · лайки",
                emoji = "❤️",
                gradient = listOf(Color(0xFF3A1060), Color(0xFF1A0A3E)),
                accentColor = Purple,
                coverTracks = favoriteTracks.take(4),
                onClick = {
                    onSmartPlaylistClick("Любимое", "${favoriteTracks.size} треков", favoriteTracks, Purple, "❤️")
                },
            )
        }

        // Фу говно
        item {
            SmartPlaylistCard(
                title = "Фу говно",
                subtitle = "${dislikedTracks.size} треков · дизлайки",
                emoji = "🤮",
                gradient = listOf(Color(0xFF3A0A0A), Color(0xFF1E0808)),
                accentColor = ColorRed,
                coverTracks = dislikedTracks.take(4),
                onClick = {
                    onSmartPlaylistClick("Фу говно", "${dislikedTracks.size} треков", dislikedTracks, ColorRed, "🤮")
                },
            )
        }

        // Пользовательские плейлисты
        if (userPlaylists.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Мои плейлисты",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                )
            }

            items(userPlaylists, key = { it.id }) { playlist ->
                UserPlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun SmartPlaylistCard(
    title: String,
    subtitle: String,
    emoji: String,
    gradient: List<Color>,
    accentColor: Color,
    coverTracks: List<Track>,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(Brush.horizontalGradient(gradient), RoundedCornerShape(16.dp))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Мозаика из обложек или эмодзи
            Box(modifier = Modifier.size(64.dp)) {
                if (coverTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(accentColor.copy(0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Text(emoji, fontSize = 28.sp) }
                } else {
                    CoverMosaic(tracks = coverTracks, size = 64.dp, cornerRadius = 12.dp)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(subtitle, fontSize = 12.sp, color = TextMuted2)
            }

            Icon(Icons.Filled.PlayCircle, null, tint = accentColor, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun UserPlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark3, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Brush.linearGradient(listOf(BgDark4, BgDark5)), RoundedCornerShape(10.dp))
                .border(1.dp, BorderColor2, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("🎵", fontSize = 22.sp) }

        Column(modifier = Modifier.weight(1f)) {
            Text(playlist.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${playlist.trackCount} треков", fontSize = 12.sp, color = TextMuted)
        }

        Icon(Icons.Filled.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

// ── Мозаика из 4 обложек (как у Spotify) ─────────────────────────────────────
@Composable
private fun CoverMosaic(tracks: List<Track>, size: Dp, cornerRadius: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius)),
    ) {
        val half = size / 2
        val covers = tracks.take(4)
        if (covers.size == 1) {
            AsyncImage(model = covers[0].coverUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Row {
                Column(modifier = Modifier.size(half)) {
                    covers.getOrNull(0)?.let {
                        AsyncImage(model = it.coverUrl, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.size(half))
                    }
                    covers.getOrNull(2)?.let {
                        AsyncImage(model = it.coverUrl, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.size(half))
                    }
                }
                Column(modifier = Modifier.size(half)) {
                    covers.getOrNull(1)?.let {
                        AsyncImage(model = it.coverUrl, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.size(half))
                    }
                    covers.getOrNull(3)?.let {
                        AsyncImage(model = it.coverUrl, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.size(half))
                    }
                }
            }
        }
    }
}

// ── Детальная страница плейлиста ──────────────────────────────────────────────
@Composable
private fun DetailContent(
    view: PlaylistView.Detail,
    onTrackClick: (queue: List<Track>, index: Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDislike: (Track) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Hero
        item {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                // Размытый фон из первой обложки
                view.coverTracks.firstOrNull()?.coverUrl?.let { url ->
                    AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().blur(50.dp).alpha(0.35f))
                } ?: Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(view.accentColor.copy(0.3f), BgDark))))

                Box(modifier = Modifier.fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, BgDark))))

                Column(
                    modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Обложка-мозаика или эмодзи
                    if (view.coverTracks.isNotEmpty()) {
                        CoverMosaic(tracks = view.coverTracks, size = 100.dp, cornerRadius = 14.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(view.accentColor.copy(0.15f), RoundedCornerShape(14.dp))
                                .border(1.dp, view.accentColor.copy(0.3f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text(view.emoji, fontSize = 44.sp) }
                    }
                    Text("Плейлист", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = view.accentColor, letterSpacing = 1.2.sp)
                    Text(view.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text(view.subtitle, fontSize = 12.sp, color = TextMuted2)
                }
            }
        }

        // Кнопки
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { if (view.tracks.isNotEmpty()) onTrackClick(view.tracks, 0) },
                    colors = ButtonDefaults.buttonColors(containerColor = view.accentColor),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Слушать", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { if (view.tracks.isNotEmpty()) onTrackClick(view.tracks.shuffled(), 0) },
                    border = BorderStroke(1.dp, view.accentColor.copy(0.5f)),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted2),
                ) {
                    Icon(Icons.Filled.Shuffle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Перемешать", fontSize = 13.sp)
                }
            }
        }

        // Заголовок таблицы
        item {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("#", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp))
                Text("Трек", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text("Время", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End, modifier = Modifier.width(42.dp))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        }

        if (view.tracks.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(view.emoji, fontSize = 48.sp)
                        Text("Пусто", color = TextMuted, fontSize = 15.sp)
                        Text(
                            when (view.title) {
                                "Любимое" -> "Нажми 👍 на трек чтобы добавить"
                                "Фу говно" -> "Нажми 👎 на трек чтобы добавить"
                                else -> "Добавь треки в плейлист"
                            },
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        itemsIndexed(view.tracks, key = { _, t -> t.id }) { index, track ->
            PlaylistTrackRow(
                number = index + 1,
                track = track,
                accentColor = view.accentColor,
                onPlay = { onTrackClick(view.tracks, index) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDislike = { onDislike(track) },
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Анимированная кнопка рейтинга ─────────────────────────────────────────────
@Composable
private fun AnimatedRatingButton(
    active: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.45f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        finishedListener = { pressed = false },
        label = "ratingScale",
    )
    IconButton(
        onClick = { pressed = true; onClick() },
        modifier = Modifier.size(32.dp),
    ) {
        Icon(
            if (active) activeIcon else inactiveIcon,
            contentDescription = contentDescription,
            tint = if (active) activeColor else TextMuted,
            modifier = Modifier.size(15.dp).scale(scale),
        )
    }
}

// ── Строка трека в плейлисте ──────────────────────────────────────────────────
@Composable
private fun PlaylistTrackRow(
    number: Int,
    track: Track,
    accentColor: Color,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDislike: () -> Unit,
) {
    val isDisliked = track.userRating == "dislike"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(start = 16.dp, end = 8.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$number", fontSize = 12.sp, color = TextMuted, modifier = Modifier.width(28.dp))

        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(7.dp)).background(BgDark5),
        )
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = if (isDisliked) TextMuted else TextPrimary,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, fontSize = 11.sp, color = TextMuted,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        AnimatedRatingButton(
            active = track.isFavorite,
            activeIcon = Icons.Filled.ThumbUp,
            inactiveIcon = Icons.Outlined.ThumbUp,
            activeColor = Purple,
            contentDescription = "Любимое",
            onClick = onToggleFavorite,
        )
        AnimatedRatingButton(
            active = isDisliked,
            activeIcon = Icons.Filled.ThumbDown,
            inactiveIcon = Icons.Outlined.ThumbDown,
            activeColor = ColorRed,
            contentDescription = "Фу говно",
            onClick = onDislike,
        )

        Text(track.durationFormatted, fontSize = 11.sp, color = TextMuted,
            textAlign = TextAlign.End, modifier = Modifier.width(36.dp))
    }
}

// ── Диалог создания плейлиста ─────────────────────────────────────────────────
@Composable
private fun CreatePlaylistDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgDark2,
        title = { Text("Новый плейлист", color = TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Название", color = TextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = BorderColor2,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text("Создать", color = Purple)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted2) } },
    )
}
