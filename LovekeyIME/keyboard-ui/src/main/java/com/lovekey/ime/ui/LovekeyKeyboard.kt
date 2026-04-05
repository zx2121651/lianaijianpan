package com.lovekey.ime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.lovekey.ime.ui.components.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun LovekeyKeyboard(
    currentPinyinText: String,
    candidateList: List<String>,
    t9PinyinCombinations: List<String> = emptyList(),
    currentModeExternal: KeyboardMode = KeyboardMode.QWERTY,
    previousModeExternal: KeyboardMode = KeyboardMode.QWERTY,
    isEnglishModeExternal: Boolean = false,
    enterKeyText: String = "发送",
    onKeyPress: (String) -> Unit,
    onCandidateSelected: (String) -> Unit,
    onSyllableSelected: (String) -> Unit = {},
    onEnglishModeChanged: (Boolean) -> Unit = {},
    onKeyboardModeChanged: (KeyboardMode) -> Unit = {}
) {
    val boardColor = Color(0xFFFDFBFB)
    val keyColor = Color(0xFFFFFFFF)
    val functionKeyColor = Color(0xFFF3EBEB)
    val accentColor = Color(0xFFE2B4B8)
    val textColor = Color(0xFF4A4443)
    val secondaryTextColor = Color(0xFF988F8E)
    val unselectedTabColor = Color(0xFFD6D1D1)

    val keyCornerRadius = 10.dp

    var isEnglishMode by remember { mutableStateOf(isEnglishModeExternal) }
    LaunchedEffect(isEnglishModeExternal) { isEnglishMode = isEnglishModeExternal }

    var isSyllableBarExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentPinyinText) {
        if (currentPinyinText.isEmpty() || t9PinyinCombinations.size <= 1) {
            isSyllableBarExpanded = false
        }
    }


    Box(modifier = Modifier.fillMaxWidth()) {
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPinyinText,
                        color = accentColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(Color(0xFFF0EBEA))
                )

                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 8.dp),
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

                if (t9PinyinCombinations.size > 1) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                isSyllableBarExpanded = !isSyllableBarExpanded
                            }
                            .padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isSyllableBarExpanded) "▲" else "▼",
                            color = accentColor,
                            fontSize = 14.sp
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
                        color = if (currentModeExternal == KeyboardMode.QWERTY) accentColor else unselectedTabColor,
                        fontWeight = if (currentModeExternal == KeyboardMode.QWERTY) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { onKeyboardModeChanged(KeyboardMode.QWERTY) }
                            .padding(8.dp)
                    )
                    Text(
                        text = "九键",
                        color = if (currentModeExternal == KeyboardMode.T9) accentColor else unselectedTabColor,
                        fontWeight = if (currentModeExternal == KeyboardMode.T9) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { onKeyboardModeChanged(KeyboardMode.T9) }
                            .padding(8.dp)
                    )
                    Text(
                        text = "手写",
                        color = if (currentModeExternal == KeyboardMode.HANDWRITING) accentColor else unselectedTabColor,
                        fontWeight = if (currentModeExternal == KeyboardMode.HANDWRITING) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { onKeyboardModeChanged(KeyboardMode.HANDWRITING) }
                            .padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (currentModeExternal) {
            KeyboardMode.QWERTY -> {
                val shiftState = remember { mutableStateOf(ShiftState.LOWERCASE) }
                QwertyKeyboard(
                    shiftState = shiftState,
                                    currentMode = object : MutableState<KeyboardMode> {
                    override var value: KeyboardMode
                        get() = currentModeExternal
                        set(v) { onKeyboardModeChanged(v) }
                    override fun component1() = currentModeExternal
                    override fun component2(): (KeyboardMode) -> Unit = { onKeyboardModeChanged(it) }
                },
                    isEnglishModeExternal = isEnglishMode,
                    enterKeyText = enterKeyText,
                    textColor = textColor,
                    keyColor = keyColor,
                    functionKeyColor = functionKeyColor,
                    accentColor = accentColor,
                    keyCornerRadius = keyCornerRadius,
                    onKeyPress = onKeyPress,
                    onModeChanged = {
                        isEnglishMode = it
                        onEnglishModeChanged(it)
                        if (!it && previousModeExternal == KeyboardMode.T9) {
                            onKeyboardModeChanged(KeyboardMode.T9)
                        }
                    }
                )
            }
            KeyboardMode.T9 -> T9Keyboard(
                                currentMode = object : MutableState<KeyboardMode> {
                    override var value: KeyboardMode
                        get() = currentModeExternal
                        set(v) { onKeyboardModeChanged(v) }
                    override fun component1() = currentModeExternal
                    override fun component2(): (KeyboardMode) -> Unit = { onKeyboardModeChanged(it) }
                },
                isEnglishModeExternal = isEnglishMode,
                enterKeyText = enterKeyText,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress,
                onModeChanged = {
                    isEnglishMode = it
                    onEnglishModeChanged(it)
                }
            )
            KeyboardMode.HANDWRITING -> HandwritingKeyboard(
                enterKeyText = enterKeyText,
                textColor = textColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
            KeyboardMode.SYMBOL -> SymbolKeyboard(
                                currentMode = object : MutableState<KeyboardMode> {
                    override var value: KeyboardMode
                        get() = currentModeExternal
                        set(v) { onKeyboardModeChanged(v) }
                    override fun component1() = currentModeExternal
                    override fun component2(): (KeyboardMode) -> Unit = { onKeyboardModeChanged(it) }
                },
                previousMode = previousModeExternal,
                enterKeyText = enterKeyText,
                textColor = textColor,
                onModeChanged = {
                    isEnglishMode = it
                    onEnglishModeChanged(it)
                },
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
        }

        } // End of Column

        if (isSyllableBarExpanded && t9PinyinCombinations.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isSyllableBarExpanded = false } // Click outside to close
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp)
                        .clickable(enabled = false) {}, // Intercept clicks so they don't close the overlay
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(t9PinyinCombinations) { pinyin ->
                            Text(
                                text = pinyin,
                                color = if (pinyin == currentPinyinText) accentColor else textColor,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = if (pinyin == currentPinyinText) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (pinyin == currentPinyinText) Color(0xFFF5E6E8) else Color(0xFFF7F7F7))
                                    .clickable {
                                        isSyllableBarExpanded = false
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
}


