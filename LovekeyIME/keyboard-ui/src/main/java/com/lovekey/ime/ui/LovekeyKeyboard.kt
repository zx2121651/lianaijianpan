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


enum class ShiftState {
    LOWERCASE, UPPERCASE, CAPSLOCK
}

enum class KeyboardMode {
    QWERTY, T9, HANDWRITING, SYMBOL
}

@Composable
fun LovekeyKeyboard(
    currentPinyinText: String,
    candidateList: List<String>,
    t9PinyinCombinations: List<String> = emptyList(),
    onKeyPress: (String) -> Unit,
    onCandidateSelected: (String) -> Unit,
    onSyllableSelected: (String) -> Unit = {}
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

    var isSyllableBarExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentPinyinText) {
        if (currentPinyinText.isEmpty() || t9PinyinCombinations.size <= 1) {
            isSyllableBarExpanded = false
        }
    }

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
                Row(
                    modifier = Modifier.clickable {
                        if (t9PinyinCombinations.size > 1) {
                            isSyllableBarExpanded = !isSyllableBarExpanded
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPinyinText,
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = if (t9PinyinCombinations.size > 1) 4.dp else 12.dp)
                    )
                    if (t9PinyinCombinations.size > 1) {
                        Text(
                            text = if (isSyllableBarExpanded) "▲" else "▼",
                            color = accentColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color(0xFFF0EBEA))
                )

                if (isSyllableBarExpanded && t9PinyinCombinations.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(t9PinyinCombinations) { pinyin ->
                            Text(
                                text = pinyin,
                                color = if (pinyin == currentPinyinText) accentColor else textColor,
                                fontSize = 16.sp,
                                fontWeight = if (pinyin == currentPinyinText) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (pinyin == currentPinyinText) Color(0xFFF5E6E8) else Color.Transparent)
                                    .clickable {
                                        isSyllableBarExpanded = false
                                        onSyllableSelected(pinyin)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
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
            KeyboardMode.QWERTY -> {
                val shiftState = remember { mutableStateOf(ShiftState.LOWERCASE) }
                QwertyKeyboard(
                    shiftState = shiftState,
                    currentMode = modeState,
                    textColor = textColor,
                    keyColor = keyColor,
                    functionKeyColor = functionKeyColor,
                    accentColor = accentColor,
                    keyCornerRadius = keyCornerRadius,
                    onKeyPress = onKeyPress
                )
            }
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
    shiftState: MutableState<ShiftState>,
    currentMode: MutableState<KeyboardMode>, textColor: Color, keyColor: Color, functionKeyColor: Color, accentColor: Color, keyCornerRadius: androidx.compose.ui.unit.Dp, onKeyPress: (String) -> Unit
) {
    val rows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
        listOf("符号", "123", "SPACE", "中/英", "ENT")
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
                    isFunctionKey -> if (key == "SHIFT" && shiftState.value != ShiftState.LOWERCASE) Color(0xFFD6D1D1) else functionKeyColor
                    else -> keyColor
                }

                val currentTextColor = if (isActionKey) Color.White else textColor

                val displayText = when(key) {
                    "SHIFT" -> if (shiftState.value == ShiftState.LOWERCASE) "⇧" else "⬆"
                    "DEL" -> "⌫"
                    "ENT" -> "发送"
                    "SPACE" -> "Lovekey"
                    "123" -> "?123"
                    else -> if (shiftState.value != ShiftState.LOWERCASE && key.length == 1) key.uppercase() else key
                }

                val showPopup = !isFunctionKey && !isSpaceKey && !isActionKey

                KeyboardKey(
                    text = displayText,
                    modifier = Modifier.weight(weight),
                    bgColor = bgColor,
                    textColor = currentTextColor,
                    fontSize = if (isFunctionKey || isSpaceKey || isActionKey) 15.sp else 23.sp,
                    fontWeight = if (isFunctionKey || isActionKey) FontWeight.Medium else FontWeight.Light,
                    keyCornerRadius = keyCornerRadius,
                    showPopup = showPopup,
                    onClick = {
                        if (key == "SHIFT") {
                            shiftState.value = when (shiftState.value) {
                                ShiftState.LOWERCASE -> ShiftState.UPPERCASE
                                ShiftState.UPPERCASE -> ShiftState.LOWERCASE
                                ShiftState.CAPSLOCK -> ShiftState.LOWERCASE
                            }
                        } else if (key == "123") {
                            currentMode.value = KeyboardMode.SYMBOL
                        } else {
                            val keyToSend = if (key.length == 1 && shiftState.value != ShiftState.LOWERCASE) {
                                key.uppercase()
                            } else {
                                key
                            }
                            onKeyPress(keyToSend)
                            if (shiftState.value == ShiftState.UPPERCASE) {
                                shiftState.value = ShiftState.LOWERCASE
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun T9Keyboard(
    currentMode: MutableState<KeyboardMode>,
    textColor: Color,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    secondaryTextColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left Column (5 keys)
        Column(
            modifier = Modifier.weight(1.1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val leftKeys = listOf(",", ".", "?", "!", "符号")
            leftKeys.forEach { key ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { if (key == "符号") currentMode.value = KeyboardMode.SYMBOL else onKeyPress(key) },
                    color = functionKeyColor,
                    shadowElevation = 0.5.dp,
                    shape = RoundedCornerShape(keyCornerRadius)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = key, color = textColor, fontSize = if (key == "符号") 16.sp else 22.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Middle Column (3x4 Grid)
        Column(
            modifier = Modifier.weight(3.3f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val middleRows = listOf(
                listOf(Pair("@#", ""), Pair("ABC", "2"), Pair("DEF", "3")),
                listOf(Pair("GHI", "4"), Pair("JKL", "5"), Pair("MNO", "6")),
                listOf(Pair("PQRS", "7"), Pair("TUV", "8"), Pair("WXYZ", "9")),
                listOf(Pair("123", ""), Pair("0", ""), Pair("中/英", ""))
            )

            middleRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { cell ->
                        val isFunctionKey = cell.first in listOf("@#", "123", "中/英")
                        val isSpace = cell.first == "0"
                        val bgColor = if (isFunctionKey) functionKeyColor else keyColor

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp, vertical = 3.dp)
                                .clip(RoundedCornerShape(keyCornerRadius))
                                .clickable {
                                    if (cell.first == "123" || cell.first == "符号") {
                                        currentMode.value = KeyboardMode.SYMBOL
                                    } else if (isSpace) {
                                        onKeyPress("SPACE")
                                    } else if (cell.second.isNotEmpty()) {
                                        onKeyPress(cell.second)
                                    } else {
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
                                if (isFunctionKey) {
                                    Text(text = cell.first, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                } else if (cell.first == "123" || cell.first == "符号") {
                                        currentMode.value = KeyboardMode.SYMBOL
                                    } else if (isSpace) {
                                    Text(text = "SPACE", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                } else {
                                    Text(text = cell.first, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                    Text(text = cell.second, color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Light)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Column (3 keys, last one is taller)
        Column(
            modifier = Modifier.weight(1.1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("DEL") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⌫", color = textColor, fontSize = 20.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("CLEAR") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("重输", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
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
}}}}}