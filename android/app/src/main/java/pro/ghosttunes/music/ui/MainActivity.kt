package pro.ghosttunes.music.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import pro.ghosttunes.music.presentation.player.PlayerViewModel
import pro.ghosttunes.music.presentation.playlists.PlaylistsScreen
import pro.ghosttunes.music.presentation.tracks.MiniPlayer
import pro.ghosttunes.music.presentation.tracks.PlayerBottomSheet
import pro.ghosttunes.music.presentation.tracks.TracksScreen
import pro.ghosttunes.music.ui.theme.MusicTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MusicTheme { MusicApp() } }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Tracks : Screen("tracks", "Музыка", Icons.Filled.LibraryMusic)
    object Favorites : Screen("favorites", "Избранное", Icons.Filled.Favorite)
    object Playlists : Screen("playlists", "Плейлисты", Icons.Filled.QueueMusic)
}

@Composable
fun MusicApp() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsState()
    var showPlayer by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }

    val tabs = listOf(Screen.Tracks, Screen.Favorites, Screen.Playlists)

    Scaffold(
        bottomBar = {
            Column {
                // Mini player above nav bar
                if (playerState.currentTrack != null) {
                    MiniPlayer(
                        playerViewModel = playerViewModel,
                        onExpand = { showPlayer = true },
                    )
                }
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDest = navBackStackEntry?.destination
                    tabs.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tracks.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Tracks.route) {
                TracksScreen(
                    onTrackClick = { queue, index ->
                        playerViewModel.playQueue(queue, index)
                        showPlayer = true
                    },
                )
            }
            composable(Screen.Favorites.route) {
                // FavoritesScreen() — same pattern as TracksScreen with observeFavorites
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Favorites — coming soon")
                }
            }
            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    onTrackClick = { queue, index ->
                        playerViewModel.playQueue(queue, index)
                        showPlayer = true
                    },
                )
            }
        }
    }

    // Full-screen player sheet — hidden when lyrics shown
    if (showPlayer && !showLyrics) {
        PlayerBottomSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showPlayer = false },
            onShowLyrics = { showLyrics = true },
        )
    }

    // Текст песни — поверх всего, плеер скрыт
    if (showLyrics) {
        playerState.currentTrack?.let { track ->
            LyricsOverlay(
                title = track.title,
                artist = track.artist,
                lyrics = track.lyrics ?: "",
                positionMs = playerState.positionMs,
                durationMs = playerState.durationMs,
                onSeek = { playerViewModel.seekTo(it) },
                onDismiss = { showLyrics = false },
            )
        }
    }
}

private data class LrcLine(val timeMs: Long?, val text: String)

private fun parseLrc(raw: String): Pair<Boolean, List<LrcLine>> {
    val lrcRegex = Regex("""^\[(\d{1,2}):(\d{2})(?:[.:](\d{1,3}))?\](.*)""")
    val parsed = mutableListOf<LrcLine>()
    var hasTimestamps = false
    for (line in raw.split('\n')) {
        val m = lrcRegex.find(line.trim()) ?: continue
        hasTimestamps = true
        val ms = m.groupValues[1].toLong() * 60_000 +
            m.groupValues[2].toLong() * 1_000 +
            m.groupValues[3].padEnd(3, '0').take(3).toLong()
        parsed.add(LrcLine(ms, m.groupValues[4].trim()))
    }
    return if (hasTimestamps) {
        true to parsed.sortedBy { it.timeMs }
    } else {
        false to raw.split('\n').map { LrcLine(null, it) }
    }
}

@Composable
private fun LyricsOverlay(
    title: String,
    artist: String,
    lyrics: String,
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val BgTop = Color(0xFF0D0D1A)
    val BgDark = Color(0xFF080810)
    val TextPrimary = Color(0xFFF0EEFF)
    val TextMuted = Color(0xFF6B6890)
    val TextMuted2 = Color(0xFF9896B0)

    val (isLrc, lines) = remember(lyrics) { parseLrc(lyrics) }

    val activeIndex by remember(positionMs, isLrc, lines, durationMs) {
        derivedStateOf {
            if (lines.isEmpty()) return@derivedStateOf 0
            if (isLrc) {
                var idx = 0
                for (i in lines.indices) {
                    val t = lines[i].timeMs ?: continue
                    if (positionMs >= t) idx = i else break
                }
                idx
            } else {
                if (durationMs <= 0L) 0
                else minOf(
                    (positionMs.toFloat() / durationMs * lines.size).toInt(),
                    lines.size - 1,
                )
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(activeIndex) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -600,
            )
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(visible) { if (!visible) { delay(300); onDismiss() } }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(400, easing = FastOutSlowInEasing)) { it },
        exit = fadeOut(tween(250)) + slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgDark)))
                .systemBarsPadding(),
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { visible = false }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.KeyboardArrowDown, null, tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(artist, fontSize = 12.sp, color = TextMuted2)
                    }
                    IconButton(onClick = { visible = false }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.Close, null, tint = TextMuted2, modifier = Modifier.size(18.dp))
                    }
                }

                if (!isLrc) {
                    Text(
                        "Текст без таймстампов — синхронизация недоступна",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }

                // Lyrics list
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        itemsIndexed(lines) { i, line ->
                            val isActive = i == activeIndex
                            val isPast = i < activeIndex

                            val fontSizeAnim by animateFloatAsState(
                                targetValue = if (isActive) 28f else 22f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                label = "fontSize$i",
                            )
                            val alphaAnim by animateFloatAsState(
                                targetValue = when {
                                    isActive -> 1f
                                    isPast -> 0.32f
                                    else -> 0.18f
                                },
                                animationSpec = tween(400),
                                label = "alpha$i",
                            )

                            if (line.text.isBlank()) {
                                Spacer(Modifier.height(14.dp))
                            } else {
                                Text(
                                    text = line.text,
                                    fontSize = fontSizeAnim.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = TextPrimary.copy(alpha = alphaAnim),
                                    lineHeight = (fontSizeAnim * 1.45f).sp,
                                    letterSpacing = if (isActive) (-0.5).sp else 0.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) {
                                            if (isLrc) {
                                                lines[i].timeMs?.let { onSeek(it) }
                                            } else if (durationMs > 0L) {
                                                onSeek(i.toLong() * durationMs / lines.size)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                )
                            }
                        }
                    }

                    // Fade masks
                    Box(
                        Modifier.fillMaxWidth().height(100.dp).align(Alignment.TopCenter)
                            .background(Brush.verticalGradient(listOf(BgTop, Color.Transparent))),
                    )
                    Box(
                        Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, BgDark))),
                    )
                }
            }
        }
    }
}
