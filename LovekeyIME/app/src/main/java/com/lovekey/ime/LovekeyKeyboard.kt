package com.lovekey.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LovekeyKeyboard(
    currentPinyinText: String,
    candidateList: List<String>,
    onKeyPress: (String) -> Unit,
    onCandidateSelected: (String) -> Unit
) {
    // Colors based on the generated "Ethereal Input" design system
    val boardColor = Color(0xFFF9F9F9)
    val keyColor = Color(0xFFFFFFFF)
    val functionKeyColor = Color(0xFFE1E2EC)
    val textColor = Color(0xFF2D3335)
    val secondaryTextColor = Color(0xFF5A6061)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(boardColor)
            .padding(bottom = 12.dp)
    ) {
        // Candidate View Bar
        if (currentPinyinText.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.White)
                    .shadow(elevation = 1.dp, spotColor = Color.LightGray)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPinyinText,
                    color = secondaryTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // Vertical divider
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color(0xFFEBEBEB))
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(candidateList) { candidate ->
                        Text(
                            text = candidate,
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onCandidateSelected(candidate) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(52.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Keyboard View
        val rows = listOf(
            listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
            listOf("123", ",", "SPACE", ".", "ENT")
        )

        rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    val isFunctionKey = key in listOf("SHIFT", "DEL", "123", "ENT")
                    val isSpaceKey = key == "SPACE"

                    // Determine key weight and styling
                    val weight = when {
                        isSpaceKey -> 5f
                        isFunctionKey -> 1.5f
                        else -> 1f
                    }

                    val bgColor = if (isFunctionKey) functionKeyColor else keyColor

                    val displayText = when(key) {
                        "SHIFT" -> "⇧"
                        "DEL" -> "⌫"
                        "ENT" -> "↵"
                        "SPACE" -> "拼音"
                        "123" -> "?123"
                        else -> key
                    }

                    Surface(
                        modifier = Modifier
                            .weight(weight)
                            .padding(horizontal = 3.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                // Skip non-implemented keys for now
                                if (key != "SHIFT" && key != "123") {
                                    onKeyPress(key)
                                }
                            },
                        color = bgColor,
                        shadowElevation = 1.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayText,
                                color = textColor,
                                fontSize = if (isFunctionKey || isSpaceKey) 16.sp else 22.sp,
                                fontWeight = if (isFunctionKey) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
