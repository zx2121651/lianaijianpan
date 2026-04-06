package com.lovekey.ime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovekey.ime.ui.KeyboardMode
import com.lovekey.ime.ui.ShiftState

@Composable
fun T9Keyboard(
    currentMode: MutableState<KeyboardMode>,
    isEnglishModeExternal: Boolean,
    enterKeyText: String,
    textColor: Color,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    secondaryTextColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit,
    onModeChanged: (Boolean) -> Unit,
    onCursorMove: (Int) -> Unit = {}
) {
    val view = LocalView.current
    val currentOnKeyPress by androidx.compose.runtime.rememberUpdatedState(onKeyPress)
    val currentOnModeChanged by androidx.compose.runtime.rememberUpdatedState(onModeChanged)
    val currentOnCursorMove by androidx.compose.runtime.rememberUpdatedState(onCursorMove)

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
                        .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
if (key == "符号") currentMode.value = KeyboardMode.SYMBOL else onKeyPress(key) },
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
                                .pointerInput(Unit) {
                                    kotlinx.coroutines.coroutineScope {
                                        launch {
                                            detectTapGestures(
                                                onPress = {
                                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                },
                                                onTap = {
                                                    if (cell.first == "123" || cell.first == "符号") {
                                                        currentMode.value = KeyboardMode.SYMBOL
                                                    } else if (cell.first == "中/英") {
                                                        currentMode.value = KeyboardMode.QWERTY
                                                        currentOnModeChanged.invoke(true)
                                                    } else if (isSpace) {
                                                        currentOnKeyPress.invoke("SPACE")
                                                    } else if (cell.first == "1") {
                                                        currentOnKeyPress.invoke("1")
                                                    } else if (cell.second.isNotEmpty()) {
                                                        currentOnKeyPress.invoke(cell.second)
                                                    } else {
                                                        currentOnKeyPress.invoke(cell.first)
                                                    }
                                                }
                                            )
                                        }
                                        if (isSpace) {
                                            launch {
                                                var accumulatedDrag = 0f
                                                detectHorizontalDragGestures(
                                                    onDragStart = {
                                                        accumulatedDrag = 0f
                                                    },
                                                    onDragEnd = {},
                                                    onDragCancel = {},
                                                    onHorizontalDrag = { change, dragAmount ->
                                                        change.consume()
                                                        accumulatedDrag += dragAmount
                                                        val threshold = 30f
                                                        if (accumulatedDrag > threshold) {
                                                            currentOnCursorMove.invoke(1)
                                                            accumulatedDrag -= threshold
                                                        } else if (accumulatedDrag < -threshold) {
                                                            currentOnCursorMove.invoke(-1)
                                                            accumulatedDrag += threshold
                                                        }
                                                    }
                                                )
                                            }
                                        }
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
                                    Text(text = cell.first, color = textColor, fontSize = if (enterKeyText.length > 2) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
                                } else if (cell.first == "123" || cell.first == "符号") {
                                    Text(text = cell.first, color = textColor, fontSize = if (enterKeyText.length > 2) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
                                } else if (isSpace) {
                                    Text(text = "SPACE", color = textColor, fontSize = if (enterKeyText.length > 2) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
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
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("DEL") },
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
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("CLEAR") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("重输", color = textColor, fontSize = if (enterKeyText.length > 2) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("ENT") },
                color = accentColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(enterKeyText, color = Color.White, fontSize = if (enterKeyText.length > 2) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
