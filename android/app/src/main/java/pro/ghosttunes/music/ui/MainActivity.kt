package pro.ghosttunes.music.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                onDismiss = { showLyrics = false },
            )
        }
    }
}

@Composable
private fun LyricsOverlay(
    title: String,
    artist: String,
    lyrics: String,
    onDismiss: () -> Unit,
) {
    val BgDark = Color(0xFF080810)
    val TextPrimary = Color(0xFFF0EEFF)
    val TextMuted2 = Color(0xFF9896B0)
    val PurpleLight = Color(0xFFC4A8FF)

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(visible) {
        if (!visible) {
            delay(300)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            initialOffsetY = { it },
        ),
        exit = fadeOut(animationSpec = tween(250)) + slideOutVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing),
            targetOffsetY = { it },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D0D1A), Color(0xFF0A0A12), Color(0xFF080810)),
                    ),
                )
                .systemBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { visible = false },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            null,
                            tint = TextPrimary,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Текст песни",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleLight,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { visible = false }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Close, null, tint = TextMuted2, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    artist,
                    fontSize = 15.sp,
                    color = TextMuted2,
                )

                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val scrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                    ) {
                        Text(
                            lyrics,
                            fontSize = 17.sp,
                            color = TextPrimary.copy(alpha = 0.85f),
                            lineHeight = 30.sp,
                            letterSpacing = 0.3.sp,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0D0D1A), Color.Transparent),
                                ),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF080810)),
                                ),
                            ),
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
