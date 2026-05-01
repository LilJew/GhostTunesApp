package pro.ghosttunes.music.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import dagger.hilt.android.AndroidEntryPoint
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

    // Full-screen player sheet
    if (showPlayer) {
        PlayerBottomSheet(
            playerViewModel = playerViewModel,
            onDismiss = { showPlayer = false },
        )
    }
}
