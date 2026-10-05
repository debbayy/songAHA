package com.example.audioplayer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audioplayer.data.AUDIO_PERMISSION
import com.example.audioplayer.ui.components.ActionSheetHost
import com.example.audioplayer.ui.components.AlertHost
import com.example.audioplayer.ui.components.AlertSpec
import com.example.audioplayer.ui.components.Artwork
import com.example.audioplayer.ui.components.Ico
import com.example.audioplayer.ui.components.Icons
import com.example.audioplayer.ui.components.SheetSpec
import com.example.audioplayer.ui.components.ToastHost
import com.example.audioplayer.ui.components.Txt
import com.example.audioplayer.ui.components.bounce
import com.example.audioplayer.ui.components.glass
import com.example.audioplayer.ui.components.noRippleClick
import com.example.audioplayer.ui.screens.AlbumDetailScreen
import com.example.audioplayer.ui.screens.AlbumsScreen
import com.example.audioplayer.ui.screens.ArtistDetailScreen
import com.example.audioplayer.ui.screens.ArtistsScreen
import com.example.audioplayer.ui.screens.FavoritesScreen
import com.example.audioplayer.ui.screens.FolderDetailScreen
import com.example.audioplayer.ui.screens.FoldersScreen
import com.example.audioplayer.ui.screens.HomeScreen
import com.example.audioplayer.ui.screens.LibraryScreen
import com.example.audioplayer.ui.screens.NowPlayingScreen
import com.example.audioplayer.ui.screens.PlaylistDetailScreen
import com.example.audioplayer.ui.screens.PlaylistsScreen
import com.example.audioplayer.ui.screens.RecentScreen
import com.example.audioplayer.ui.screens.SearchScreen
import com.example.audioplayer.ui.screens.SettingsScreen
import com.example.audioplayer.ui.screens.SongsScreen
import com.example.audioplayer.ui.theme.AppTheme
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

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

        val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        val audioPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            vm.reload()
            if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
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
            )
        }

        CompositionLocalProvider(LocalActions provides actions) {
            val player by vm.player.collectAsState()
            Box(Modifier.fillMaxSize().background(c.background)) {
                Screens(nav)

                // Fade di tepi bawah supaya bar kaca "melayang" di atas konten
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, c.background.copy(alpha = 0.85f))))
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
    val activity = LocalContext.current as? ComponentActivity ?: return
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

@Composable
private fun TabBar(nav: NavViewModel) {
    val c = LocalIos.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.weight(1f).height(62.dp).glass(CircleShape).padding(4.dp)) {
            listOf(Tab.Home, Tab.Library, Tab.Playlists).forEach { t ->
                val selected = nav.tab == t
                val bg by animateColorAsState(if (selected) c.fill else Color.Transparent, tween(200), label = "tab")
                val fg = if (selected) c.accent else c.label
                Column(
                    Modifier.weight(1f).fillMaxHeight().clip(CircleShape).background(bg).noRippleClick { nav.select(t) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Ico(t.icon, fg, size = 24.dp)
                    Txt(t.label, IosType.caption2, color = fg)
                }
            }
        }
        Box(Modifier.size(62.dp).glass(CircleShape).bounce { nav.select(Tab.Search) }, Alignment.Center) {
            Ico(Icons.Search, c.label, size = 26.dp)
        }
    }
}

@Composable
private fun SearchBar(nav: NavViewModel) {
    val c = LocalIos.current
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(54.dp).glass(CircleShape).bounce { nav.select(nav.previousTab) }, Alignment.Center) {
            Ico(nav.previousTab.icon, c.label, size = 24.dp)
        }
        Row(
            Modifier.weight(1f).height(54.dp).glass(CircleShape).padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Ico(Icons.Search, c.secondaryLabel, size = 20.dp)
            BasicTextField(
                value = nav.query,
                onValueChange = { nav.query = it },
                singleLine = true,
                textStyle = IosType.body.copy(color = c.label),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focus),
                decorationBox = { inner ->
                    Box {
                        if (nav.query.isEmpty()) Txt("Lagu, album, artis", IosType.body, color = c.secondaryLabel)
                        inner()
                    }
                },
            )
            if (nav.query.isNotEmpty()) {
                Box(Modifier.size(36.dp).noRippleClick { nav.query = "" }, Alignment.Center) {
                    Ico(Icons.CloseCircle, c.tertiaryLabel, size = 20.dp)
                }
            }
        }
    }
}

@Composable
private fun MiniPlayer(onOpen: () -> Unit) {
    val a = LocalActions.current
    val c = LocalIos.current
    val p by a.vm.player.collectAsState()
    val song = p.current ?: return
    Row(
        Modifier.fillMaxWidth().height(58.dp).glass(CircleShape).noRippleClick(onOpen).padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Artwork(song, 42.dp, Modifier.size(42.dp), corner = 21.dp)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Txt(song.title, IosType.subhead.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
            Txt(song.displayArtist, IosType.footnote, color = c.secondaryLabel)
        }
        Box(Modifier.size(44.dp).bounce { a.vm.togglePlay() }, Alignment.Center) {
            Ico(if (p.isPlaying) Icons.Pause else Icons.Play, c.label, size = 26.dp)
        }
        Spacer(Modifier.width(2.dp))
        Box(Modifier.size(44.dp).bounce { a.vm.next() }, Alignment.Center) {
            Ico(Icons.Next, c.label, size = 26.dp)
        }
    }
}
