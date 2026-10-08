package com.example.audioplayer.ui.components.organisms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlphabetIndexTest {

    @get:Rule
    val rule = createComposeRule()

    /** 4 lagu untuk setiap huruf A–Z, sudah urut abjad. */
    private val songs = ('A'..'Z').flatMap { letter -> (1..4).map { "$letter-lagu $it" } }

    @Test
    fun ketukHuruf_melompatKeBagianItu() {
        lateinit var listState: LazyListState
        rule.setContent {
            listState = rememberLazyListState()
            val sections = alphabetSections(songs, firstIndex = 0) { it }
            Box(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize().testTag("list"), state = listState) {
                    items(songs) { BasicText(it, Modifier.fillMaxWidth().height(56.dp)) }
                }
                AlphabetIndex(listState, sections)
            }
        }
        // indeks baru muncul setelah daftar digulir
        rule.onNodeWithTag("list").performTouchInput {
            down(center)
            repeat(5) { moveBy(Offset(0f, -40f)) }
            up()
        }
        rule.onNodeWithText("M").performClick()
        rule.waitForIdle()
        assertEquals(songs.indexOf("M-lagu 1"), listState.firstVisibleItemIndex)
    }
}
