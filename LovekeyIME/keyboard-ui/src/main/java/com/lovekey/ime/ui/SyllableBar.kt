package com.lovekey.ime.ui

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
fun SyllableBar(
    currentPinyinText: String,
    t9PinyinCombinations: List<String>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSyllableSelected: (String) -> Unit,
    accentColor: Color,
    textColor: Color,
    view: View
) {
    if (currentPinyinText.isEmpty()) return

    // Heuristically segment the pinyin for display.
    // If it's from T9, it might already have apostrophes. If not, we just show it as one block for now,
    // or split by apostrophe if it exists.
    val chips = currentPinyinText.split("'").filter { it.isNotEmpty() }

    Column(modifier = Modifier.fillMaxWidth().zIndex(10f)) {
        // Main collapsed row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(chips) { chip ->
                    Text(
                        text = chip,
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x11000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (t9PinyinCombinations.size > 1) {
                Box(
                    modifier = Modifier
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onExpandedChange(!isExpanded)
                        }
                        .padding(start = 8.dp, end = 4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        color = accentColor,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Expanded Panel
        if (isExpanded && t9PinyinCombinations.size > 1) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp)
                    .clickable(enabled = false) {}, // Intercept clicks
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(t9PinyinCombinations) { pinyin ->
                        val isSelected = pinyin == currentPinyinText
                        Text(
                            text = pinyin.replace("'", " "),
                            color = if (isSelected) accentColor else textColor,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFF5E6E8) else Color(0xFFF7F7F7))
                                .clickable {
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onExpandedChange(false)
                                    onSyllableSelected(pinyin)
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
