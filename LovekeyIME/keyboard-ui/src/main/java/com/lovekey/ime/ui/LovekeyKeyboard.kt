package com.lovekey.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
    isEnglishModeExternal: Boolean = false,
    onKeyPress: (String) -> Unit,
    onCandidateSelected: (String) -> Unit,
    onSyllableSelected: (String) -> Unit = {},
    onModeChanged: (Boolean) -> Unit = {}
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

    var previousMode by remember { mutableStateOf(KeyboardMode.QWERTY) }
    LaunchedEffect(currentMode) {
        if (currentMode != KeyboardMode.SYMBOL && currentMode != KeyboardMode.HANDWRITING && !isEnglishModeExternal) {
            previousMode = currentMode
        }
    }

    var isEnglishMode by remember { mutableStateOf(isEnglishModeExternal) }
    LaunchedEffect(isEnglishModeExternal) { isEnglishMode = isEnglishModeExternal }

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
                    isEnglishModeExternal = isEnglishMode,
                    textColor = textColor,
                    keyColor = keyColor,
                    functionKeyColor = functionKeyColor,
                    accentColor = accentColor,
                    keyCornerRadius = keyCornerRadius,
                    onKeyPress = onKeyPress,
                    onModeChanged = {
                        isEnglishMode = it
                        onModeChanged(it)
                        if (!it && previousMode == KeyboardMode.T9) {
                            currentMode = KeyboardMode.T9
                        }
                    }
                )
            }
            KeyboardMode.T9 -> T9Keyboard(
                currentMode = modeState,
                isEnglishModeExternal = isEnglishMode,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress,
                onModeChanged = {
                    previousMode = KeyboardMode.T9
                    isEnglishMode = it
                    onModeChanged(it)
                }
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
                previousMode = previousMode,
                textColor = textColor,
                onModeChanged = {
                    isEnglishMode = it
                    onModeChanged(it)
                },
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
    currentMode: MutableState<KeyboardMode>,
    isEnglishModeExternal: Boolean,
    textColor: Color, keyColor: Color, functionKeyColor: Color, accentColor: Color, keyCornerRadius: androidx.compose.ui.unit.Dp, onKeyPress: (String) -> Unit, onModeChanged: (Boolean) -> Unit
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
                val isFunctionKey = key in listOf("SHIFT", "DEL", "123", "中/英")
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
                    "123" -> "?123"
                    "中/英" -> if (isEnglishModeExternal) "英/中" else "中/英"
                    "SPACE" -> if (isEnglishModeExternal) "Lovekey(英)" else "Lovekey"
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
                        } else if (key == "中/英") {
                            onModeChanged(!isEnglishModeExternal)
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
    isEnglishModeExternal: Boolean,
    textColor: Color,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    secondaryTextColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit,
    onModeChanged: (Boolean) -> Unit
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
                listOf(Pair("1", "分词"), Pair("ABC", "2"), Pair("DEF", "3")),
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
                        val isFunctionKey = cell.first in listOf("1", "123", "中/英")
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
                                    } else if (cell.first == "中/英") {
                                        currentMode.value = KeyboardMode.QWERTY
                                        onModeChanged(true)
                                    } else if (isSpace) {
                                        onKeyPress("SPACE")
                                    } else if (cell.first == "1") {
                                        onKeyPress("1")
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
    previousMode: KeyboardMode,
    textColor: Color,
    onModeChanged: (Boolean) -> Unit,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit
) {
    val categories = listOf("常用", "数字", "中文", "英文", "数学", "网络", "序号")
    var currentCategory by remember { mutableStateOf(categories[0]) }

    val symbolMap = mapOf(
        "常用" to listOf("，", "。", "？", "！", "、", "；", "：", "“", "”", "‘", "’", "（", "）", "【", "】", "《", "》", "…", "—", "～"),
        "数字" to listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "+", "-", "*", "/", "=", "%", ".", ","),
        "中文" to listOf("￥", "·", "ˉ", "ˇ", "¨", "々", "‖", "「", "」", "『", "』", "〔", "〕", "〈", "〉", "〇"),
        "英文" to listOf(",", ".", "?", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "=", "+", "[", "]", "{", "}", "\\", "|", ";", ":", "'", "\"", "<", ">", "/", "~", "`"),
        "数学" to listOf("+", "-", "×", "÷", "=", "≠", "≈", "±", "<", ">", "≤", "≥", "∞", "∝", "∴", "∠", "△", "⊥", "∥", "√", "∫", "∮", "∵", "∷", "∪", "∩", "∈", "⊂", "⊆", "⊇", "π", "°", "℃", "‰"),
        "网络" to listOf("^_^", "T_T", "(╥_╥)", "@_@", "=‿=", "~_~", "-_-||", ">_<", "^o^", "^ω^", "(*^__^*)", "(^o^)/", "Orz", "O(∩_∩)O"),
        "序号" to listOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Left Column for Categories (Tab)
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .background(functionKeyColor)
        ) {
            categories.forEach { category ->
                val isSelected = category == currentCategory
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { currentCategory = category },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) accentColor else textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // "ABC" return key at the bottom of the left column
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE5E5E5)) // slightly darker for function key feeling
                    .clickable {
                        currentMode.value = previousMode
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABC",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right Column for Symbol Grid
        Column(
            modifier = Modifier
                .weight(4.5f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
        ) {
            val currentSymbols = symbolMap[currentCategory] ?: emptyList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (currentCategory == "网络") 3 else 5),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(currentSymbols) { symbol ->
                    Surface(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(keyCornerRadius))
                            .clickable { onKeyPress(symbol) },
                        color = keyColor,
                        shadowElevation = 0.5.dp,
                        shape = RoundedCornerShape(keyCornerRadius)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = symbol,
                                color = textColor,
                                fontSize = if (currentCategory == "网络") 14.sp else 18.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Bottom Action Row for Symbol Keyboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(end = 4.dp)
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
                        .weight(3f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { onKeyPress("SPACE") },
                    color = keyColor,
                    shadowElevation = 0.5.dp,
                    shape = RoundedCornerShape(keyCornerRadius)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Lovekey", color = textColor, fontSize = 15.sp)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(start = 4.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { onKeyPress("ENT") },
                    color = accentColor,
                    shadowElevation = 0.5.dp,
                    shape = RoundedCornerShape(keyCornerRadius)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("发送", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}