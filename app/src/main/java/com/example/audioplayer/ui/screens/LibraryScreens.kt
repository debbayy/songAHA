package com.example.audioplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.Route
import com.example.audioplayer.ui.addToPlaylist
import com.example.audioplayer.ui.components.AlertSpec
import com.example.audioplayer.ui.components.Artwork
import com.example.audioplayer.ui.components.EmptyState
import com.example.audioplayer.ui.components.GlassIconButton
import com.example.audioplayer.ui.components.Ico
import com.example.audioplayer.ui.components.Icons
import com.example.audioplayer.ui.components.Page
import com.example.audioplayer.ui.components.PlayShuffleButtons
import com.example.audioplayer.ui.components.SectionHeader
import com.example.audioplayer.ui.components.SheetAction
import com.example.audioplayer.ui.components.SheetSpec
import com.example.audioplayer.ui.components.Txt
import com.example.audioplayer.ui.components.noRippleClick
import com.example.audioplayer.ui.components.songCountLabel
import com.example.audioplayer.ui.newPlaylist
import com.example.audioplayer.ui.theme.IosType
import com.example.audioplayer.ui.theme.LocalIos

@Composable
fun HomeScreen() {
    val a = LocalActions.current
    val vm = a.vm
    val lib by vm.library.collectAsState()
    val loading by vm.loading.collectAsState()
    val granted by vm.hasPermission.collectAsState()
    val recentIds by vm.store.recent.collectAsState()
    val favIds by vm.store.favorites.collectAsState()
    val recent = remember(lib, recentIds) { recentIds.mapNotNull { lib.byId[it] } }
    val favorites = remember(lib, favIds) { lib.songs.filter { it.id in favIds } }

    Page("Beranda", actions = { GlassIconButton(Icons.Gear, { a.nav.push(Route.Settings) }) }) {
        if (!granted && lib.songs.isEmpty()) item { PermissionCard() }
        if (lib.songs.isNotEmpty()) item {
            PlayShuffleButtons({ vm.playSongs(lib.songs) }, { vm.shuffle(lib.songs) }, Modifier.padding(top = 8.dp))
        }
        if (recent.isNotEmpty()) {
            item { SectionHeader("Terakhir Diputar") { a.nav.push(Route.Recent) } }
            item { SongCarousel(recent.take(15)) }
        }
        if (lib.recentAlbums.isNotEmpty()) {
            item { SectionHeader("Baru Ditambahkan") { a.nav.push(Route.Albums) } }
            item { AlbumCarousel(lib.recentAlbums) }
        }
        if (favorites.isNotEmpty()) {
            item { SectionHeader("Favorit") { a.nav.push(Route.Favorites) } }
            item { SongCarousel(favorites.take(15)) }
        }
        if (lib.songs.isNotEmpty() && recent.isEmpty() && lib.recentAlbums.isEmpty()) {
            item { SectionHeader("Lagu") { a.nav.push(Route.Songs) } }
            item { SongCarousel(lib.songs.take(15)) }
        }
        if (granted && !loading && lib.songs.isEmpty()) item {
            EmptyState("Belum Ada Musik", "Salin file musik ke HP kamu, atau pilih folder di Pustaka › Folder.")
        }
    }
}

@Composable
fun LibraryScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val granted by a.vm.hasPermission.collectAsState()
    Page("Pustaka", actions = { GlassIconButton(Icons.Gear, { a.nav.push(Route.Settings) }) }) {
        item { NavRow("Playlist", { a.nav.push(Route.Playlists) }, Icons.Queue, big = true) }
        item { NavRow("Artis", { a.nav.push(Route.Artists) }, Icons.Person, big = true) }
        item { NavRow("Album", { a.nav.push(Route.Albums) }, Icons.Album, big = true) }
        item { NavRow("Lagu", { a.nav.push(Route.Songs) }, Icons.Note, big = true) }
        item { NavRow("Folder", { a.nav.push(Route.Folders) }, Icons.Folder, big = true) }
        item { NavRow("Favorit", { a.nav.push(Route.Favorites) }, Icons.Heart, big = true) }
        item { NavRow("Terakhir Diputar", { a.nav.push(Route.Recent) }, Icons.History, big = true) }
        if (!granted && lib.songs.isEmpty()) item { PermissionCard() }
        if (lib.recentAlbums.isNotEmpty()) {
            item { SectionHeader("Baru Ditambahkan") }
            albumGrid(lib.recentAlbums)
        }
    }
}

@Composable
fun SongsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    Page("Lagu") {
        if (lib.songs.isEmpty()) item { EmptyState("Tidak Ada Lagu", "Lagu di perangkat kamu akan muncul di sini.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(lib.songs) }, { a.vm.shuffle(lib.songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(lib.songs, player, a)
    }
}

@Composable
fun AlbumsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    Page("Album") {
        if (lib.albums.isEmpty()) item { EmptyState("Tidak Ada Album", "Album dibaca dari tag lagu di perangkat kamu.") }
        albumGrid(lib.albums)
    }
}

@Composable
fun ArtistsScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    Page("Artis") {
        if (lib.artists.isEmpty()) item { EmptyState("Tidak Ada Artis", "Artis dibaca dari tag lagu di perangkat kamu.") }
        items(lib.artists, key = { it.name }) { artist ->
            NavRow(
                artist.name,
                { a.nav.push(Route.ArtistDetail(artist.name)) },
                leading = { Artwork(artist.songs.first(), 48.dp, Modifier.size(48.dp), corner = 24.dp) },
                subtitle = "${artist.songs.size} lagu",
            )
        }
    }
}

@Composable
fun FoldersScreen() {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    Page("Folder", actions = { GlassIconButton(Icons.Add, a.pickFolder) }) {
        item {
            NavRow("Tambah Folder…", a.pickFolder, leading = { FolderTile(c.accent, Icons.Add) }, subtitle = "Pilih folder musik dari penyimpanan")
        }
        items(lib.folders, key = { it.path }) { f ->
            NavRow(
                f.name, { a.nav.push(Route.FolderDetail(f.path)) },
                leading = { FolderTile(c.accent, Icons.Folder) },
                subtitle = "${f.songs.size} lagu",
            )
        }
    }
}

@Composable
private fun FolderTile(tint: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(LocalIos.current.fill),
        Alignment.Center,
    ) { Ico(icon, tint, size = 26.dp) }
}

@Composable
fun FolderDetailScreen(path: String) {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val folder = lib.folderByPath[path]
    val songs = folder?.songs.orEmpty()
    Page(folder?.name ?: "Folder") {
        if (songs.isEmpty()) item { EmptyState("Folder Kosong", "Tidak ada lagu di folder ini.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(songs) }, { a.vm.shuffle(songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(songs, player, a)
    }
}

@Composable
fun FavoritesScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val favIds by a.vm.store.favorites.collectAsState()
    val songs = remember(lib, favIds) { lib.songs.filter { it.id in favIds } }
    Page("Favorit") {
        if (songs.isEmpty()) item { EmptyState("Belum Ada Favorit", "Tahan sebuah lagu lalu pilih \"Favoritkan\", atau ketuk ♥ di layar Sedang Diputar.") }
        else item { PlayShuffleButtons({ a.vm.playSongs(songs) }, { a.vm.shuffle(songs) }, Modifier.padding(vertical = 8.dp)) }
        songList(songs, player, a)
    }
}

@Composable
fun RecentScreen() {
    val a = LocalActions.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val ids by a.vm.store.recent.collectAsState()
    val songs = remember(lib, ids) { ids.mapNotNull { lib.byId[it] } }
    Page("Terakhir Diputar") {
        if (songs.isEmpty()) item { EmptyState("Belum Ada Riwayat", "Lagu yang kamu putar akan muncul di sini.") }
        songList(songs, player, a)
    }
}

// ---- Album & Artis (gaya iOS 27: warna halaman mengikuti cover) ---------------------------

@Composable
fun AlbumDetailScreen(key: String) {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val album = lib.albumByKey[key]
    if (album == null) {
        Page("Album") { item { EmptyState("Album Tidak Ditemukan", "Album ini sudah tidak ada di perangkat.") } }
        return
    }
    val tint = rememberArtTint(album.cover, c.fill)
    Page(
        album.title,
        actions = { GlassIconButton(Icons.More, { a.addToPlaylist(album.songs) }) },
        header = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.55f), c.background)))
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Artwork(
                    album.cover, 250.dp,
                    Modifier.size(250.dp).shadow(18.dp, RoundedCornerShape(12.dp)),
                    corner = 12.dp,
                )
                Txt(album.title, IosType.title2, Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp), align = TextAlign.Center, maxLines = 2)
                Txt(
                    album.artist, IosType.title3.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
                    Modifier.padding(top = 2.dp).noRippleClick { a.nav.push(Route.ArtistDetail(album.artist)) },
                    color = c.accent,
                )
                Txt("Album · ${songCountLabel(album.songs)}", IosType.footnote, Modifier.padding(top = 4.dp, bottom = 16.dp), color = c.secondaryLabel)
                PlayShuffleButtons({ a.vm.playSongs(album.songs) }, { a.vm.shuffle(album.songs) })
            }
        },
    ) {
        songList(
            album.songs, player, a, numbered = true,
            subtitle = { if (it.displayArtist != album.artist) it.displayArtist else "" },
        )
    }
}

@Composable
fun ArtistDetailScreen(name: String) {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val artist = lib.artistByName[name]
    if (artist == null) {
        Page(name) { item { EmptyState("Artis Tidak Ditemukan", "Artis ini sudah tidak ada di perangkat.") } }
        return
    }
    val cover = artist.albums.firstOrNull()?.cover ?: artist.songs.first()
    val tint = rememberArtTint(cover, c.background)
    val pageBg = lerp(c.background, tint, if (c.isDark) 0.22f else 0.14f)

    Page(
        artist.name,
        background = pageBg,
        header = {
            Column {
                // Foto artis menyatu dengan konten di bawahnya
                Box(Modifier.fillMaxWidth().aspectRatio(1.05f)) {
                    Artwork(cover, 420.dp, Modifier.fillMaxSize(), corner = 0.dp, iconScale = 0.3f)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(0f to Color(0x33000000), 0.25f to Color.Transparent, 0.6f to Color.Transparent, 1f to pageBg)
                        )
                    )
                    Txt(artist.name, IosType.largeTitle, Modifier.align(Alignment.BottomStart).padding(16.dp), maxLines = 2)
                }
                Txt(
                    songCountLabel(artist.songs) + if (artist.albums.isNotEmpty()) " · ${artist.albums.size} album" else "",
                    IosType.subhead, Modifier.padding(start = 16.dp, bottom = 14.dp), color = c.secondaryLabel,
                )
                PlayShuffleButtons({ a.vm.playSongs(artist.songs) }, { a.vm.shuffle(artist.songs) })
                SectionHeader("Lagu")
            }
        },
    ) {
        songList(artist.songs, player, a, subtitle = { it.album })
        if (artist.albums.isNotEmpty()) {
            item { SectionHeader("Album") }
            item { AlbumCarousel(artist.albums) }
        }
    }
}

// ---- Playlist ---------------------------------------------------------------------------

@Composable
fun PlaylistsScreen() {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val playlists by a.vm.store.playlists.collectAsState()
    Page("Playlist", actions = { GlassIconButton(Icons.Add, { a.newPlaylist { a.nav.push(Route.PlaylistDetail(it)) } }) }) {
        item {
            NavRow(
                "Playlist Baru…", { a.newPlaylist { a.nav.push(Route.PlaylistDetail(it)) } },
                leading = {
                    Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(c.fill), Alignment.Center) {
                        Ico(Icons.Add, c.accent, size = 30.dp)
                    }
                },
                subtitle = "Kumpulkan lagu favoritmu",
            )
        }
        items(playlists, key = { it.id }) { p ->
            val first = p.songIds.firstNotNullOfOrNull { lib.byId[it] }
            NavRow(
                p.name, { a.nav.push(Route.PlaylistDetail(p.id)) },
                leading = { Artwork(first, 56.dp, Modifier.size(56.dp), corner = 8.dp) },
                subtitle = "${p.songIds.size} lagu",
            )
        }
    }
}

@Composable
fun PlaylistDetailScreen(id: String) {
    val a = LocalActions.current
    val c = LocalIos.current
    val lib by a.vm.library.collectAsState()
    val player by a.vm.player.collectAsState()
    val playlists by a.vm.store.playlists.collectAsState()
    val playlist = playlists.firstOrNull { it.id == id }
    if (playlist == null) {
        Page("Playlist") { item { EmptyState("Playlist Dihapus", "Playlist ini sudah tidak ada.") } }
        return
    }
    val songs = remember(lib, playlist) { playlist.songIds.mapNotNull { lib.byId[it] } }
    val tint = rememberArtTint(songs.firstOrNull(), c.fill)

    fun menu() = a.showSheet(
        SheetSpec(
            title = playlist.name,
            subtitle = "${songs.size} lagu",
            actions = listOf(
                SheetAction("Tambah ke Antrean", Icons.Queue) { a.vm.addToQueue(songs); a.toast("Ditambahkan ke Antrean") },
                SheetAction("Ubah Nama", Icons.Pencil) {
                    a.showAlert(AlertSpec("Ubah Nama Playlist", initial = playlist.name, confirm = "Simpan") {
                        a.vm.store.renamePlaylist(id, it)
                    })
                },
                SheetAction("Hapus Playlist", Icons.Trash, destructive = true) {
                    a.showAlert(
                        AlertSpec(
                            "Hapus \"${playlist.name}\"?", "Lagu di perangkat tidak ikut terhapus.",
                            input = false, confirm = "Hapus", destructive = true,
                        ) {
                            a.nav.pop()
                            a.vm.store.deletePlaylist(id)
                        }
                    )
                },
            ),
        )
    )

    Page(
        playlist.name,
        actions = { GlassIconButton(Icons.More, { menu() }) },
        header = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.5f), c.background)))
                    .statusBarsPadding()
                    .padding(top = 64.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Artwork(songs.firstOrNull(), 230.dp, Modifier.size(230.dp).shadow(18.dp, RoundedCornerShape(12.dp)), corner = 12.dp)
                Txt(playlist.name, IosType.title2, Modifier.padding(top = 18.dp, start = 24.dp, end = 24.dp), align = TextAlign.Center, maxLines = 2)
                Txt("Playlist · ${songCountLabel(songs)}", IosType.footnote, Modifier.padding(top = 4.dp, bottom = 16.dp), color = c.secondaryLabel)
                if (songs.isNotEmpty()) PlayShuffleButtons({ a.vm.playSongs(songs) }, { a.vm.shuffle(songs) })
            }
        },
    ) {
        if (songs.isEmpty()) item {
            EmptyState("Playlist Masih Kosong", "Tahan sebuah lagu lalu pilih \"Tambah ke Playlist…\".")
        }
        songList(
            songs, player, a,
            extra = { _, i ->
                listOf(SheetAction("Hapus dari Playlist", Icons.Trash, destructive = true) {
                    // indeks di songs bisa berbeda dengan songIds kalau ada lagu yang hilang
                    val target = songs[i].id
                    var seen = -1
                    val realIndex = playlist.songIds.indexOfFirst { sid -> if (lib.byId.containsKey(sid)) seen++; sid == target && seen == i }
                    if (realIndex >= 0) a.vm.store.removeFromPlaylist(id, realIndex)
                })
            },
        )
        item { Spacer(Modifier.height(8.dp)) }
    }
}
