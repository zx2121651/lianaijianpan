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
    // 优雅内敛的浪漫主题 (Subtle Elegant Romance)
    val boardColor = Color(0xFFFDFBFB) // 非常浅的暖灰/灰粉底色 (Off-white with a hint of warmth)
    val keyColor = Color(0xFFFFFFFF) // 纯白按键
    val functionKeyColor = Color(0xFFF3EBEB) // 淡淡的灰藕粉色功能键
    val accentColor = Color(0xFFE2B4B8) // 强调色（回车键）：莫兰迪玫瑰金
    val textColor = Color(0xFF4A4443) // 文字：深棕灰色（比纯黑柔和）
    val secondaryTextColor = Color(0xFF988F8E) // 拼音提示文字：浅灰褐

    val keyCornerRadius = 10.dp // 优雅的微圆角，而非浮夸的气泡

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(boardColor)
            .padding(bottom = 12.dp)
    ) {
        // 候选词栏 (Candidate View Bar)
        if (currentPinyinText.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.White)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPinyinText,
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 12.dp)
                )

                // 极细的分割线
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color(0xFFF0EBEA))
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(candidateList) { candidate ->
                        Text(
                            text = candidate,
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onCandidateSelected(candidate) }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(52.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 键盘区 (Keyboard View)
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
                    .padding(horizontal = 6.dp, vertical = 6.dp), // 稍微增加行距让键盘有呼吸感
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { key ->
                    val isFunctionKey = key in listOf("SHIFT", "DEL", "123")
                    val isActionKey = key == "ENT"
                    val isSpaceKey = key == "SPACE"

                    // 权重
                    val weight = when {
                        isSpaceKey -> 5f
                        isFunctionKey || isActionKey -> 1.5f
                        else -> 1f
                    }

                    // 背景色
                    val bgColor = when {
                        isActionKey -> accentColor // 强调色给回车
                        isFunctionKey -> functionKeyColor
                        else -> keyColor
                    }

                    // 文字颜色
                    val currentTextColor = if (isActionKey) Color.White else textColor

                    val displayText = when(key) {
                        "SHIFT" -> "⇧"
                        "DEL" -> "⌫"
                        "ENT" -> "发送"
                        "SPACE" -> "Lovekey"
                        "123" -> "?123"
                        else -> key
                    }

                    Surface(
                        modifier = Modifier
                            .weight(weight)
                            .padding(horizontal = 4.dp)
                            .height(46.dp)
                            .clip(RoundedCornerShape(keyCornerRadius))
                            .clickable {
                                if (key != "SHIFT" && key != "123") {
                                    onKeyPress(key)
                                }
                            },
                        color = bgColor,
                        shadowElevation = 0.5.dp, // 极其微弱的投影，避免厚重感
                        shape = RoundedCornerShape(keyCornerRadius)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayText,
                                color = currentTextColor,
                                fontSize = if (isFunctionKey || isSpaceKey || isActionKey) 15.sp else 23.sp,
                                fontWeight = if (isFunctionKey || isActionKey) FontWeight.Medium else FontWeight.Light
                            )
                        }
                    }
                }
            }
        }
    }
}
