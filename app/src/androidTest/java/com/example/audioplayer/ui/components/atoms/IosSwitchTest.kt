package com.example.audioplayer.ui.components.atoms

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IosSwitchTest {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Regresi: animasi spring kenop memantul sedikit di bawah 0 saat dimatikan. Dulu kenop
     * digeser dengan padding, sehingga mematikan saklar (mis. equalizer) membuat aplikasi crash.
     */
    @Test
    fun nyalakanLaluMatikanBerulang_tidakCrash() {
        var checked by mutableStateOf(false)
        rule.setContent { IosSwitch(checked, { checked = it }, Modifier.testTag("switch")) }
        repeat(3) {
            rule.onNodeWithTag("switch").performClick()
            rule.mainClock.advanceTimeBy(1_000)
            rule.onNodeWithTag("switch").performClick()
            rule.mainClock.advanceTimeBy(1_000)
        }
        assertFalse(checked)
    }
}
