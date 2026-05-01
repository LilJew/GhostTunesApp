package pro.ghosttunes.music.presentation.tracks

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.TextFields
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
import pro.ghosttunes.music.domain.model.Album
import pro.ghosttunes.music.domain.model.Artist
import pro.ghosttunes.music.domain.model.Playlist
import pro.ghosttunes.music.domain.model.Track
import pro.ghosttunes.music.presentation.player.PlayerViewModel

// ── Цвета сайта ───────────────────────────────────────────────────────────────
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

// ── Внутренняя навигация ──────────────────────────────────────────────────────
private sealed class HomeView {
    object Artists : HomeView()
    object AllTracks : HomeView()
    data class ArtistDetail(val artist: Artist) : HomeView()
    data class AlbumDetail(val album: Album, val tracks: List<Track>) : HomeView()
}

// ── Главный экран ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(
    onTrackClick: (queue: List<Track>, index: Int) -> Unit,
    viewModel: TracksViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentView by remember { mutableStateOf<HomeView>(HomeView.Artists) }
    var searchQuery by remember { mutableStateOf("") }
    var addToPlaylistTrack by remember { mutableStateOf<Track?>(null) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = currentView !is HomeView.Artists) {
        currentView = HomeView.Artists
        searchQuery = ""
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        containerColor = BgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding),
        ) {
            // ── Топ-бар ──────────────────────────────────────────────────────
            TopBar(
                currentView = currentView,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBack = { currentView = HomeView.Artists; searchQuery = "" },
            )

            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Purple, strokeWidth = 2.dp)
                }
            } else when (val view = currentView) {
                is HomeView.Artists -> {
                    val filteredArtists = remember(uiState.artists, searchQuery) {
                        if (searchQuery.isBlank()) uiState.artists
                        else uiState.artists.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }
                    ArtistsGrid(
                        artists = filteredArtists,
                        onArtistClick = { currentView = HomeView.ArtistDetail(it) },
                        onAllTracksClick = { currentView = HomeView.AllTracks },
                        totalTracks = uiState.tracks.size,
                    )
                }

                is HomeView.AllTracks -> {
                    val filtered = remember(uiState.tracks, searchQuery) {
                        if (searchQuery.isBlank()) uiState.tracks
                        else uiState.tracks.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                                it.artist.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    TrackListContent(
                        tracks = filtered,
                        onTrackClick = onTrackClick,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDislike = { viewModel.rateTrack(it.id, "dislike") },
                        onAddToPlaylist = { addToPlaylistTrack = it },
                    )
                }

                is HomeView.ArtistDetail -> {
                    val filtered = remember(view.artist.tracks, searchQuery) {
                        if (searchQuery.isBlank()) view.artist.tracks
                        else view.artist.tracks.filter {
                            it.title.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    ArtistDetailContent(
                        artist = view.artist,
                        filteredTracks = filtered,
                        onTrackClick = onTrackClick,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDislike = { viewModel.rateTrack(it.id, "dislike") },
                        onAddToPlaylist = { addToPlaylistTrack = it },
                        onAlbumClick = { album ->
                            val albumTracks = uiState.tracks.filter { it.albumId == album.id }
                            currentView = HomeView.AlbumDetail(album, albumTracks)
                        },
                    )
                }

                is HomeView.AlbumDetail -> {
                    val filtered = remember(view.tracks, searchQuery) {
                        if (searchQuery.isBlank()) view.tracks
                        else view.tracks.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    }
                    AlbumDetailContent(
                        album = view.album,
                        filteredTracks = filtered,
                        allTracks = view.tracks,
                        onTrackClick = onTrackClick,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDislike = { viewModel.rateTrack(it.id, "dislike") },
                        onAddToPlaylist = { addToPlaylistTrack = it },
                    )
                }
            }
        }
    }

    addToPlaylistTrack?.let { track ->
        AddToPlaylistSheet(
            track = track,
            playlists = uiState.playlists,
            onAddToPlaylist = { playlistId ->
                viewModel.addToPlaylist(playlistId, track.id)
                addToPlaylistTrack = null
            },
            onCreateNew = { showCreatePlaylistDialog = true },
            onDismiss = { addToPlaylistTrack = null },
        )
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onConfirm = { title -> viewModel.createPlaylist(title); showCreatePlaylistDialog = false },
            onDismiss = { showCreatePlaylistDialog = false },
        )
    }
}

// ── Топ-бар с брендингом и поиском ───────────────────────────────────────────
@Composable
private fun TopBar(
    currentView: HomeView,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark2)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (currentView !is HomeView.Artists) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Brush.linearGradient(listOf(Purple, Purple2)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("♪", fontSize = 14.sp, color = Color.White) }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = when (currentView) {
                    is HomeView.Artists -> "GhostTunes"
                    is HomeView.AllTracks -> "Все треки"
                    is HomeView.ArtistDetail -> currentView.artist.name
                    is HomeView.AlbumDetail -> currentView.album.title
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (currentView is HomeView.Artists) PurpleLight else TextPrimary,
                modifier = Modifier.weight(1f),
            )
            when (val v = currentView) {
                is HomeView.ArtistDetail -> Text("${v.artist.tracks.size} тр.", fontSize = 12.sp, color = TextMuted)
                is HomeView.AlbumDetail -> Text("${v.tracks.size} тр.", fontSize = 12.sp, color = TextMuted)
                else -> {}
            }
        }
        Spacer(Modifier.height(10.dp))
        // Поисковая строка
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgDark3, RoundedCornerShape(20.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            when (val v = currentView) {
                                is HomeView.Artists -> "Поиск исполнителей..."
                                is HomeView.AllTracks -> "Поиск треков..."
                                is HomeView.ArtistDetail -> "Поиск в ${v.artist.name}..."
                                is HomeView.AlbumDetail -> "Поиск в ${v.album.title}..."
                            },
                            color = TextMuted, fontSize = 13.sp,
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// ── Сетка исполнителей ────────────────────────────────────────────────────────
@Composable
private fun ArtistsGrid(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    onAllTracksClick: () -> Unit,
    totalTracks: Int,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        // Кнопка "Все треки" — занимает всю ширину
        item(span = { GridItemSpan(2) }) {
            AllTracksCard(totalTracks = totalTracks, onClick = onAllTracksClick)
        }

        if (artists.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎤", fontSize = 40.sp)
                        Text("Исполнители не найдены", color = TextMuted, fontSize = 14.sp)
                    }
                }
            }
        }

        items(artists, key = { it.name }) { artist ->
            ArtistCard(artist = artist, onClick = { onArtistClick(artist) })
        }

        item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun AllTracksCard(totalTracks: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF1A1A35), BgDark4)),
                RoundedCornerShape(14.dp),
            )
            .border(1.dp, BorderColor2, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Brush.linearGradient(listOf(Purple, Purple2)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text("Все треки", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("$totalTracks треков", fontSize = 11.sp, color = TextMuted)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ArtistCard(artist: Artist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark3, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Аватар + имя
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (artist.cover != null) {
                AsyncImage(
                    model = artist.cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, BorderColor2, CircleShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Brush.linearGradient(listOf(BgDark4, BgDark5)), CircleShape)
                        .border(2.dp, BorderColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("🎤", fontSize = 28.sp) }
            }
            Text(
                artist.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                buildString {
                    append("${artist.tracks.size} тр.")
                    if (artist.albums.isNotEmpty()) append(" · ${artist.albums.size} альб.")
                },
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
        }

    }
}

// ── Детальный экран артиста ───────────────────────────────────────────────────
@Composable
private fun ArtistDetailContent(
    artist: Artist,
    filteredTracks: List<Track>,
    onTrackClick: (queue: List<Track>, index: Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDislike: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
    onAlbumClick: (Album) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Hero-блок с фоном
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                // Размытый фон
                if (artist.cover != null) {
                    AsyncImage(
                        model = artist.cover,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().blur(40.dp).alpha(0.35f),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(BgDark4, BgDark))),
                    )
                }
                // Градиент снизу
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, BgDark))),
                )
                // Контент
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (artist.cover != null) {
                        AsyncImage(
                            model = artist.cover,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(2.dp, BorderColor2, CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .background(BgDark4, CircleShape)
                                .border(2.dp, BorderColor, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text("🎤", fontSize = 40.sp) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Исполнитель", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = PurpleLight, letterSpacing = 1.2.sp)
                        Text(artist.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(
                            buildString {
                                append("${artist.tracks.size} треков")
                                if (artist.albums.isNotEmpty()) append(" · ${artist.albums.size} альбомов")
                            },
                            fontSize = 12.sp, color = TextMuted2,
                        )
                    }
                }
            }
        }

        // Кнопки действий
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { if (artist.tracks.isNotEmpty()) onTrackClick(artist.tracks, 0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Слушать", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        val shuffled = artist.tracks.shuffled()
                        if (shuffled.isNotEmpty()) onTrackClick(shuffled, 0)
                    },
                    border = BorderStroke(1.dp, BorderColor2),
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

        // Заголовок списка треков
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("#", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp))
                Text("Трек", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text("Время", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End, modifier = Modifier.width(42.dp))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        }

        if (filteredTracks.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Нет треков", color = TextMuted)
                }
            }
        }

        itemsIndexed(filteredTracks, key = { _, t -> t.id }) { index, track ->
            TrackRow(
                number = index + 1,
                track = track,
                onPlay = { onTrackClick(filteredTracks, index) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDislike = { onDislike(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
            )
        }

        // ── Дискография ───────────────────────────────────────────────────────
        if (artist.albums.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Дискография",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                    )
                    Text(
                        "${artist.albums.size} альбомов",
                        fontSize = 12.sp,
                        color = Purple,
                    )
                }
            }

            items(artist.albums, key = { "alb_${it.id}" }) { album ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAlbumClick(album) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (album.coverUrl != null) {
                        AsyncImage(
                            model = album.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(BgDark4, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("💿", fontSize = 22.sp) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            album.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            buildString {
                                if (album.year != null) append("${album.year} · ")
                                val count = artist.tracks.count { it.albumId == album.id }
                                append("$count треков")
                            },
                            fontSize = 11.sp,
                            color = TextMuted,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Экран альбома ─────────────────────────────────────────────────────────────
@Composable
private fun AlbumDetailContent(
    album: Album,
    filteredTracks: List<Track>,
    allTracks: List<Track>,
    onTrackClick: (queue: List<Track>, index: Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDislike: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Hero
        item {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                if (album.coverUrl != null) {
                    AsyncImage(
                        model = album.coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().blur(40.dp).alpha(0.4f),
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BgDark4, BgDark))))
                }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, BgDark))))
                Row(
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (album.coverUrl != null) {
                        AsyncImage(
                            model = album.coverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, BorderColor2, RoundedCornerShape(12.dp)),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(BgDark4, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("💿", fontSize = 44.sp) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Альбом", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = PurpleLight, letterSpacing = 1.2.sp)
                        Text(album.title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(
                            buildString {
                                append(album.artist)
                                if (album.year != null) append(" · ${album.year}")
                                append(" · ${allTracks.size} треков")
                            },
                            fontSize = 12.sp, color = TextMuted2,
                        )
                    }
                }
            }
        }

        // Кнопки действий
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { if (allTracks.isNotEmpty()) onTrackClick(allTracks, 0) },
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Слушать", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { if (allTracks.isNotEmpty()) onTrackClick(allTracks.shuffled(), 0) },
                    border = BorderStroke(1.dp, BorderColor2),
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

        // Заголовок списка
        item {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("#", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                Text("Трек", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Время", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End, modifier = Modifier.width(42.dp))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        }

        if (filteredTracks.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Нет треков", color = TextMuted)
                }
            }
        }

        itemsIndexed(filteredTracks, key = { _, t -> t.id }) { index, track ->
            TrackRow(
                number = index + 1,
                track = track,
                onPlay = { onTrackClick(filteredTracks, index) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDislike = { onDislike(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Список всех треков ────────────────────────────────────────────────────────
@Composable
private fun TrackListContent(
    tracks: List<Track>,
    onTrackClick: (queue: List<Track>, index: Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDislike: (Track) -> Unit,
    onAddToPlaylist: (Track) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("#", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp))
                Text("Трек", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Text("Время", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End, modifier = Modifier.width(42.dp))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        }

        if (tracks.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🎵", fontSize = 40.sp)
                        Text("Нет треков", color = TextMuted, fontSize = 14.sp)
                    }
                }
            }
        }

        itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
            TrackRow(
                number = index + 1,
                track = track,
                onPlay = { onTrackClick(tracks, index) },
                onToggleFavorite = { onToggleFavorite(track) },
                onDislike = { onDislike(track) },
                onAddToPlaylist = { onAddToPlaylist(track) },
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

// ── Строка трека ──────────────────────────────────────────────────────────────
@Composable
fun TrackRow(
    number: Int,
    track: Track,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDislike: () -> Unit,
    onAddToPlaylist: () -> Unit,
) {
    val isDisliked = track.userRating == "dislike"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$number", fontSize = 12.sp, color = TextMuted, modifier = Modifier.width(28.dp))

        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp)).background(BgDark5),
        )
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDisliked) TextMuted else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(track.artist, fontSize = 11.sp, color = TextMuted, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (isDisliked) {
                    Text("· фу говно", fontSize = 10.sp, color = Color(0xFFFF5F5F))
                } else if (track.isFavorite) {
                    Text("· Любимое", fontSize = 10.sp, color = Purple)
                }
            }
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
            activeColor = Color(0xFFFF5F5F),
            contentDescription = "Фу говно",
            onClick = onDislike,
        )

        Text(
            track.durationFormatted,
            fontSize = 11.sp,
            color = TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp),
        )
    }
}

// ── MiniPlayer ────────────────────────────────────────────────────────────────
@Composable
fun MiniPlayer(playerViewModel: PlayerViewModel = hiltViewModel(), onExpand: () -> Unit) {
    val state by playerViewModel.playerState.collectAsState()
    val track = state.currentTrack ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF5080810))
            .border(BorderStroke(1.dp, BorderColor)),
    ) {
        val progress = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs else 0f
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(BgDark5)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(Purple, PurpleLight))),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onExpand)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = track.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(BgDark5),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(track.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(track.artist, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = playerViewModel::skipPrevious, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.SkipPrevious, null, tint = TextMuted2, modifier = Modifier.size(22.dp))
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(TextPrimary, CircleShape)
                    .clickable(onClick = playerViewModel::togglePlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    null, tint = BgDark, modifier = Modifier.size(18.dp),
                )
            }
            IconButton(onClick = playerViewModel::skipNext, modifier = Modifier.size(38.dp)) {
                Icon(Icons.Filled.SkipNext, null, tint = TextMuted2, modifier = Modifier.size(22.dp))
            }
        }
    }
}

// ── Полный плеер ──────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheet(
    playerViewModel: PlayerViewModel = hiltViewModel(),
    tracksViewModel: TracksViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val state by playerViewModel.playerState.collectAsState()
    val track = state.currentTrack ?: return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgDark2,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 48.dp),
        ) {
            // Топ: закрыть + заголовок + меню
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.KeyboardArrowDown, null, tint = TextPrimary, modifier = Modifier.size(28.dp))
                }
                Text("Плеер", modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.MoreVert, null, tint = TextPrimary)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Большая обложка
            AsyncImage(
                model = track.coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgDark5),
            )

            Spacer(Modifier.height(20.dp))

            // Аватар + название + кнопки
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = track.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(BgDark5),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(track.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track.artist, fontSize = 13.sp, color = TextMuted2, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.Share, null, tint = TextMuted2, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Filled.MoreHoriz, null, tint = TextMuted2, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Прогресс
            val progress = if (state.durationMs > 0) state.positionMs.toFloat() / state.durationMs else 0f
            Slider(
                value = progress,
                onValueChange = { playerViewModel.seekTo((it * state.durationMs).toLong()) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Purple,
                    activeTrackColor = Purple,
                    inactiveTrackColor = BgDark5,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth().offset(y = (-8).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatMs(state.positionMs), fontSize = 11.sp, color = TextMuted)
                Text(formatMs(state.durationMs), fontSize = 11.sp, color = TextMuted)
            }

            Spacer(Modifier.height(4.dp))

            // Главные кнопки: dislike | prev | play | next | like
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { tracksViewModel.rateTrack(track.id, "dislike") }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        if (track.userRating == "dislike") Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                        null,
                        tint = if (track.userRating == "dislike") Color(0xFFFF5F5F) else TextMuted2,
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(onClick = playerViewModel::skipPrevious, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.SkipPrevious, null, tint = TextPrimary, modifier = Modifier.size(36.dp))
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Brush.radialGradient(listOf(PurpleLight, Purple, Purple2)), CircleShape)
                        .clickable(onClick = playerViewModel::togglePlayPause),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        null, tint = BgDark, modifier = Modifier.size(34.dp),
                    )
                }
                IconButton(onClick = playerViewModel::skipNext, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Filled.SkipNext, null, tint = TextPrimary, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { playerViewModel.toggleFavorite(track) }, modifier = Modifier.size(44.dp)) {
                    Icon(
                        if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        null,
                        tint = if (track.isFavorite) Purple else TextMuted2,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Вторичные кнопки: repeat | lyrics | shuffle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf(
                    Icons.Filled.Repeat to "Повтор",
                    Icons.Filled.TextFields to "Текст",
                    Icons.Filled.Shuffle to "Перемешать",
                ).forEach { (icon, _) ->
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .background(BgDark3, RoundedCornerShape(14.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .size(52.dp),
                    ) {
                        Icon(icon, null, tint = TextMuted2, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

// ── Диалоги плейлистов ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    track: Track,
    playlists: List<Playlist>,
    onAddToPlaylist: (String) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = BgDark2) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Добавить в плейлист", style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            playlists.forEach { playlist ->
                ListItem(
                    headlineContent = { Text(playlist.title, color = TextPrimary) },
                    supportingContent = { Text("${playlist.trackCount} треков", color = TextMuted) },
                    leadingContent = { Icon(Icons.Filled.QueueMusic, null, tint = Purple) },
                    modifier = Modifier.clickable { onAddToPlaylist(playlist.id) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            ListItem(
                headlineContent = { Text("Создать плейлист", color = TextPrimary) },
                leadingContent = { Icon(Icons.Filled.Add, null, tint = Purple) },
                modifier = Modifier.clickable(onClick = onCreateNew),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun CreatePlaylistDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
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

fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    return "%d:%02d".format(totalSec / 60, totalSec % 60)
}
