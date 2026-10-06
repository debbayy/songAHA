package com.example.audioplayer.ui.screens.settings.sections

import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.audioplayer.ui.LocalActions
import com.example.audioplayer.ui.components.atoms.Icons
import com.example.audioplayer.ui.components.atoms.InsetHairline
import com.example.audioplayer.ui.components.molecules.ActionRow
import com.example.audioplayer.ui.components.molecules.RemovableRow
import com.example.audioplayer.ui.components.molecules.SettingsGroup

/** Scan ulang dan daftar folder tambahan (Storage Access Framework). */
@Composable
fun LibrarySection() {
    val a = LocalActions.current
    val folders by a.vm.store.safFolders.collectAsState()

    SettingsGroup("PUSTAKA", footer = "Folder tambahan berguna untuk musik yang tidak terbaca otomatis oleh sistem.") {
        ActionRow("Pindai Ulang Musik", Icons.Refresh) { a.vm.reload(); a.toast("Memindai ulang…") }
        InsetHairline()
        ActionRow("Tambah Folder…", Icons.Folder, onClick = a.pickFolder)
        folders.forEach { uri ->
            InsetHairline()
            RemovableRow(folderLabel(uri)) { a.vm.removeFolder(uri) }
        }
    }
}

private fun folderLabel(uri: String): String = runCatching {
    DocumentsContract.getTreeDocumentId(Uri.parse(uri)).substringAfterLast(':').ifBlank { "Penyimpanan" }
}.getOrDefault(uri)
