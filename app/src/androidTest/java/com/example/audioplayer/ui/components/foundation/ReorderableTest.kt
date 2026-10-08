package com.example.audioplayer.ui.components.foundation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReorderableTest {

    @get:Rule
    val rule = createComposeRule()

    private var songs by mutableStateOf((1..30).map { "Lagu $it" })
    private lateinit var listState: LazyListState

    private fun setList() = rule.setContent {
        listState = rememberLazyListState()
        val reorder = rememberReorderState(songs, key = { it }, listState) { from, to ->
            songs = songs.toMutableList().apply { add(to, removeAt(from)) }
        }
        LazyColumn(Modifier.fillMaxSize().reorderable(reorder), state = listState) {
            items(reorder.items, key = { it }) { song ->
                BasicText(
                    song,
                    Modifier.fillMaxWidth().height(60.dp).reorderItem(reorder, song, Color.White).testTag(song),
                )
            }
        }
    }

    @Test
    fun tahanLaluGeser_memindahkanLagu() {
        setList()
        rule.onNodeWithTag("Lagu 4").performTouchInput {
            down(center)
            advanceEventTime(1_000) // tahan lebih lama dari batas long-press
            repeat(10) { moveBy(Offset(0f, -height * 0.3f)) } // naik ±3 baris
            up()
        }
        rule.waitForIdle()
        assertEquals(listOf("Lagu 4", "Lagu 1", "Lagu 2", "Lagu 3", "Lagu 5"), songs.take(5))
    }

    @Test
    fun geserTanpaMenahan_tetapMenggulirDanUrutanTidakBerubah() {
        setList()
        val before = songs
        rule.onNodeWithTag("Lagu 6").performTouchInput {
            down(center)
            repeat(10) { moveBy(Offset(0f, -height * 0.5f)) }
            up()
        }
        rule.waitForIdle()
        assertEquals(before, songs)
        assertTrue("daftar seharusnya tergulir", listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)
    }
}
