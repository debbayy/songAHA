package com.example.audioplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audioplayer.data.AUDIO_PERMISSION
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.components.organisms.ActionSheetHost
import com.example.audioplayer.ui.components.organisms.AlertHost
import com.example.audioplayer.ui.components.organisms.AlertSpec
import com.example.audioplayer.ui.components.organisms.MiniPlayer
import com.example.audioplayer.ui.components.organisms.SearchBar
import com.example.audioplayer.ui.components.organisms.SheetSpec
import com.example.audioplayer.ui.components.organisms.TabBar
import com.example.audioplayer.ui.components.organisms.ToastHost
import com.example.audioplayer.ui.screens.equalizer.EqualizerScreen
import com.example.audioplayer.ui.screens.home.HomeScreen
import com.example.audioplayer.ui.screens.library.AlbumDetailScreen
import com.example.audioplayer.ui.screens.library.AlbumsScreen
import com.example.audioplayer.ui.screens.library.ArtistDetailScreen
import com.example.audioplayer.ui.screens.library.ArtistsScreen
import com.example.audioplayer.ui.screens.library.FavoritesScreen
import com.example.audioplayer.ui.screens.library.FolderDetailScreen
import com.example.audioplayer.ui.screens.library.FoldersScreen
import com.example.audioplayer.ui.screens.library.LibraryScreen
import com.example.audioplayer.ui.screens.library.RecentScreen
import com.example.audioplayer.ui.screens.library.SongsScreen
import com.example.audioplayer.ui.screens.nowplaying.NowPlayingScreen
import com.example.audioplayer.ui.screens.playlists.PlaylistDetailScreen
import com.example.audioplayer.ui.screens.playlists.PlaylistsScreen
import com.example.audioplayer.ui.screens.search.SearchScreen
import com.example.audioplayer.ui.screens.settings.SettingsScreen
import com.example.audioplayer.ui.theme.AppTheme
import com.example.audioplayer.ui.theme.LocalIos
import com.example.audioplayer.ui.theme.LocalWallpaper
import com.example.audioplayer.ui.theme.backdrop
import com.example.audioplayer.ui.theme.edgeFill
import kotlinx.coroutines.launch

@Composable
fun App(vm: PlayerViewModel = viewModel(), nav: NavViewModel = viewModel()) {
    val settings by vm.store.settings.collectAsState()
    AppTheme(settings) {
        val c = LocalIos.current
        val context = LocalContext.current
        SystemBars(darkIcons = !c.isDark && !nav.nowPlayingOpen)

        var sheet by remember { mutableStateOf<SheetSpec?>(null) }
        var alert by remember { mutableStateOf<AlertSpec?>(null) }
        var toast by remember { mutableStateOf<String?>(null) }
        var rootSize by remember { mutableStateOf(IntSize.Zero) }
        val wallpaper = LocalWallpaper.current
        // Wallpaper dimuat async (bisa setelah layout pertama), jadi ukuran layar diteruskan di sini
        LaunchedEffect(wallpaper, rootSize) { wallpaper?.rootSize = rootSize }

        val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            vm.reload()
            if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) vm.setWallpaper(uri) { ok -> if (!ok) toast = "Gambar tidak bisa dibuka" }
        }
        var lyricsTarget by remember { mutableStateOf<Song?>(null) }
        val lyricsPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val song = lyricsTarget
            if (uri != null && song != null) {
                vm.importLyrics(song, uri) { ok -> toast = if (ok) "Lirik Ditambahkan" else "Bukan file lirik" }
            }
        }
        val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) vm.addFolder(uri)
        }

        LaunchedEffect(Unit) {
            fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
            when {
                !granted(AUDIO_PERMISSION) -> audioPermission.launch(AUDIO_PERMISSION)
                Build.VERSION.SDK_INT >= 33 && !granted(Manifest.permission.POST_NOTIFICATIONS) ->
                    notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val actions = remember(vm, nav) {
            AppActions(
                vm = vm, nav = nav,
                showSheet = { sheet = it },
                showAlert = { alert = it },
                toast = { toast = it },
                requestAudioPermission = { audioPermission.launch(AUDIO_PERMISSION) },
                pickFolder = { folderPicker.launch(null) },
                pickWallpaper = {
                    wallpaperPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                pickLyrics = { song ->
                    lyricsTarget = song
                    // .lrc tidak punya MIME baku, jadi semua file ditampilkan
                    lyricsPicker.launch(arrayOf("*/*"))
                },
            )
        }

        CompositionLocalProvider(LocalActions provides actions) {
            val player by vm.player.collectAsState()
            Box(
                Modifier
                    .fillMaxSize()
                    .onSizeChanged { rootSize = it }
                    .backdrop(frosted = false)
                    .background(c.background)
            ) {
                Screens(nav)

                // Fade di tepi bawah supaya bar kaca "melayang" di atas konten
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(150.dp)
                        .edgeFill(c.background, 0f to 0f, 1f to 0.85f)
                )
                BottomBars(nav, hasCurrent = player.current != null)

                AnimatedVisibility(
                    nav.nowPlayingOpen && player.current != null,
                    enter = slideInVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)) { it },
                    exit = slideOutVertically(tween(280)) { it },
                ) { NowPlayingScreen() }

                ActionSheetHost(sheet) { sheet = null }
                ToastHost(toast) { toast = null }
            }
            AlertHost(alert) { alert = null }

            BackHandler(enabled = sheet != null || nav.nowPlayingOpen || nav.stack().size > 1 || nav.tab != Tab.Home) {
                if (sheet != null) sheet = null else nav.back()
            }
        }
    }
}

@Composable
private fun SystemBars(darkIcons: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    LaunchedEffect(darkIcons) {
        val style = if (darkIcons) SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        else SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}

@Composable
private fun Screens(nav: NavViewModel) {
    val holder = rememberSaveableStateHolder()
    AnimatedContent(
        targetState = nav.tab to nav.current,
        transitionSpec = {
            val duration = 320
            when {
                initialState.first != targetState.first -> fadeIn(tween(180)) togetherWith fadeOut(tween(120))
                nav.lastWasPop ->
                    (slideInHorizontally(tween(duration)) { -it / 4 } + fadeIn(tween(duration)))
                        .togetherWith(slideOutHorizontally(tween(duration)) { it })
                        .apply { targetContentZIndex = -1f }
                else ->
                    slideInHorizontally(tween(duration)) { it }
                        .togetherWith(slideOutHorizontally(tween(duration)) { -it / 4 } + fadeOut(tween(duration)))
                        .apply { targetContentZIndex = 1f }
            }
        },
        label = "nav",
    ) { (tab, route) ->
        holder.SaveableStateProvider("${tab.name}/$route") {
            when (route) {
                Route.Home -> HomeScreen()
                Route.Library -> LibraryScreen()
                Route.Playlists -> PlaylistsScreen()
                Route.Search -> SearchScreen()
                Route.Songs -> SongsScreen()
                Route.Albums -> AlbumsScreen()
                Route.Artists -> ArtistsScreen()
                Route.Folders -> FoldersScreen()
                Route.Favorites -> FavoritesScreen()
                Route.Recent -> RecentScreen()
                Route.Settings -> SettingsScreen()
                Route.Equalizer -> EqualizerScreen()
                is Route.AlbumDetail -> AlbumDetailScreen(route.key)
                is Route.ArtistDetail -> ArtistDetailScreen(route.name)
                is Route.FolderDetail -> FolderDetailScreen(route.path)
                is Route.PlaylistDetail -> PlaylistDetailScreen(route.id)
            }
        }
    }
}

/** Mini player + tab bar kaca yang melayang (iOS 26/27). Di tab Cari, tab bar berubah jadi kolom pencarian. */
@Composable
private fun BoxScope.BottomBars(nav: NavViewModel, hasCurrent: Boolean) {
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Column(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AnimatedVisibility(hasCurrent && !imeVisible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            MiniPlayer { nav.nowPlayingOpen = true }
        }
        AnimatedContent(nav.tab == Tab.Search, transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) }, label = "bar") { searching ->
            if (searching) SearchBar(nav) else TabBar(nav)
        }
    }
}
