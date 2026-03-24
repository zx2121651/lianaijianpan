package com.lovekey.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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

enum class KeyboardMode {
    QWERTY, T9, HANDWRITING, SYMBOL
}

@Composable
fun LovekeyKeyboard(
    currentPinyinText: String,
    candidateList: List<String>,
    onKeyPress: (String) -> Unit,
    onCandidateSelected: (String) -> Unit
) {
    val boardColor = Color(0xFFFDFBFB)
    val keyColor = Color(0xFFFFFFFF)
    val functionKeyColor = Color(0xFFF3EBEB)
    val accentColor = Color(0xFFE2B4B8)
    val textColor = Color(0xFF4A4443)
    val secondaryTextColor = Color(0xFF988F8E)
    val unselectedTabColor = Color(0xFFD6D1D1)

    val keyCornerRadius = 10.dp

    var currentMode by remember { mutableStateOf(KeyboardMode.QWERTY) }

    val modeState = remember { mutableStateOf(currentMode) }
    LaunchedEffect(modeState.value) { currentMode = modeState.value }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(boardColor)
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.White)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPinyinText.isNotEmpty()) {
                Text(
                    text = currentPinyinText,
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 12.dp)
                )

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
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "全拼",
                        color = if (currentMode == KeyboardMode.QWERTY) accentColor else unselectedTabColor,
                        fontWeight = if (currentMode == KeyboardMode.QWERTY) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { modeState.value = KeyboardMode.QWERTY }
                            .padding(8.dp)
                    )
                    Text(
                        text = "九键",
                        color = if (currentMode == KeyboardMode.T9) accentColor else unselectedTabColor,
                        fontWeight = if (currentMode == KeyboardMode.T9) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { modeState.value = KeyboardMode.T9 }
                            .padding(8.dp)
                    )
                    Text(
                        text = "手写",
                        color = if (currentMode == KeyboardMode.HANDWRITING) accentColor else unselectedTabColor,
                        fontWeight = if (currentMode == KeyboardMode.HANDWRITING) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { modeState.value = KeyboardMode.HANDWRITING }
                            .padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (currentMode) {
            KeyboardMode.QWERTY -> QwertyKeyboard(
                currentMode = modeState,
                textColor = textColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
            KeyboardMode.T9 -> T9Keyboard(
                currentMode = modeState,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
            KeyboardMode.HANDWRITING -> HandwritingKeyboard(
                textColor = textColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
            KeyboardMode.SYMBOL -> SymbolKeyboard(
                currentMode = modeState,
                textColor = textColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
        }
    }
}

@Composable
fun QwertyKeyboard(
    currentMode: MutableState<KeyboardMode>, textColor: Color, keyColor: Color, functionKeyColor: Color, accentColor: Color, keyCornerRadius: androidx.compose.ui.unit.Dp, onKeyPress: (String) -> Unit
) {
    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
        listOf("123", ",", "SPACE", ".", "ENT")
    )

    rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            row.forEach { key ->
                val isFunctionKey = key in listOf("SHIFT", "DEL", "123")
                val isActionKey = key == "ENT"
                val isSpaceKey = key == "SPACE"

                val weight = when {
                    isSpaceKey -> 5f
                    isFunctionKey || isActionKey -> 1.5f
                    else -> 1f
                }

                val bgColor = when {
                    isActionKey -> accentColor
                    isFunctionKey -> functionKeyColor
                    else -> keyColor
                }

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
                            if (key == "123") {
                                currentMode.value = KeyboardMode.SYMBOL
                            } else if (key != "SHIFT") {
                                onKeyPress(key)
                            }
                        },
                    color = bgColor,
                    shadowElevation = 0.5.dp,
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

@Composable
fun T9Keyboard(
    currentMode: MutableState<KeyboardMode>, textColor: Color, secondaryTextColor: Color, keyColor: Color, functionKeyColor: Color, accentColor: Color, keyCornerRadius: androidx.compose.ui.unit.Dp, onKeyPress: (String) -> Unit
) {
    val rows = listOf(
        listOf(Pair("1", "符"), Pair("2", "ABC"), Pair("3", "DEF"), Pair("DEL", "⌫")),
        listOf(Pair("4", "GHI"), Pair("5", "JKL"), Pair("6", "MNO"), Pair("ENT", "发送")),
        listOf(Pair("7", "PQRS"), Pair("8", "TUV"), Pair("9", "WXYZ"), Pair("ENT", "")),
        listOf(Pair("123", "?123"), Pair("0", "SPACE"), Pair(".", "符号"), Pair("ENT", ""))
    )

    Column(modifier = Modifier.padding(horizontal = 6.dp)) {
        for (i in 0 until 4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (j in 0 until 3) {
                    val cell = rows[i][j]
                    val isFunctionKey = cell.first in listOf("DEL", "123", ".", "1")
                    val isSpace = cell.first == "0"

                    val bgColor = if (isFunctionKey) functionKeyColor else keyColor

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(keyCornerRadius))
                            .clickable {
                                if (cell.first == "123" || cell.first == ".") {
                                    currentMode.value = KeyboardMode.SYMBOL
                                } else if (isSpace) {
                                    onKeyPress("SPACE")
                                } else if (!isFunctionKey) {
                                    onKeyPress(cell.first)
                                }
                            },
                        color = bgColor,
                        shadowElevation = 0.5.dp,
                        shape = RoundedCornerShape(keyCornerRadius)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isFunctionKey || isSpace) {
                                Text(
                                    text = if(isSpace) "Lovekey" else if(cell.first=="1") cell.second else cell.first,
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(text = cell.first, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                                Text(text = cell.second, color = secondaryTextColor, fontSize = 10.sp, fontWeight = FontWeight.Light)
                            }
                        }
                    }
                }

                if (i == 0) {
                     Surface(
                        modifier = Modifier
                            .weight(0.8f)
                            .padding(horizontal = 4.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(keyCornerRadius))
                            .clickable { onKeyPress("DEL") },
                        color = functionKeyColor,
                        shadowElevation = 0.5.dp,
                        shape = RoundedCornerShape(keyCornerRadius)
                    ) {
                         Box(contentAlignment = Alignment.Center) {
                             Text("⌫", color = textColor, fontSize = 18.sp)
                         }
                     }
                } else if (i == 1) {
                     Surface(
                        modifier = Modifier
                            .weight(0.8f)
                            .padding(horizontal = 4.dp)
                            .height((54 * 3 + 12 * 2).dp)
                            .clip(RoundedCornerShape(keyCornerRadius))
                            .clickable { onKeyPress("ENT") },
                        color = accentColor,
                        shadowElevation = 0.5.dp,
                        shape = RoundedCornerShape(keyCornerRadius)
                    ) {
                         Box(contentAlignment = Alignment.Center) {
                             Text("发送", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                         }
                     }
                }
            }
        }
    }
}

@Composable
fun HandwritingKeyboard(
    textColor: Color, functionKeyColor: Color, accentColor: Color, keyCornerRadius: androidx.compose.ui.unit.Dp, onKeyPress: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(keyCornerRadius)),
            color = Color.White,
            shadowElevation = 0.5.dp,
            shape = RoundedCornerShape(keyCornerRadius)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "请在此区域手写 (敬请期待)",
                    color = Color(0xFFD6D1D1),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("DEL") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⌫", color = textColor, fontSize = 18.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("重写", color = textColor, fontSize = 14.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("ENT") },
                color = accentColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("发送", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SymbolKeyboard(
    currentMode: MutableState<KeyboardMode>,
    textColor: Color,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
        listOf("ABC", "-", "_", "=", "+", "[", "]", "{", "}", "DEL"),
        listOf("?", ",", "SPACE", ".", "ENT")
    )

    rows.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            row.forEach { key ->
                val isFunctionKey = key in listOf("ABC", "DEL")
                val isActionKey = key == "ENT"
                val isSpaceKey = key == "SPACE"

                val weight = when {
                    isSpaceKey -> 5f
                    isFunctionKey || isActionKey -> 1.5f
                    else -> 1f
                }

                val bgColor = when {
                    isActionKey -> accentColor
                    isFunctionKey -> functionKeyColor
                    else -> keyColor
                }

                val currentTextColor = if (isActionKey) Color.White else textColor

                val displayText = when(key) {
                    "DEL" -> "⌫"
                    "ENT" -> "发送"
                    "SPACE" -> "Lovekey"
                    else -> key
                }

                Surface(
                    modifier = Modifier
                        .weight(weight)
                        .padding(horizontal = 4.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable {
                            if (key == "ABC") {
                                currentMode.value = KeyboardMode.QWERTY
                            } else {
                                onKeyPress(key)
                            }
                        },
                    color = bgColor,
                    shadowElevation = 0.5.dp,
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
