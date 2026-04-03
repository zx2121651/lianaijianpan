package com.lovekey.ime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovekey.ime.ui.KeyboardMode
import com.lovekey.ime.ui.ShiftState

@Composable
fun QwertyKeyboard(
    shiftState: MutableState<ShiftState>,
    currentMode: MutableState<KeyboardMode>,
    isEnglishModeExternal: Boolean,
    enterKeyText: String,
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
                    "ENT" -> enterKeyText
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
