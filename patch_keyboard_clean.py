import re

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

# Completely replace QwertyKeyboard to avoid syntax errors
qwerty_clean = """@Composable
fun QwertyKeyboard(
    shiftState: MutableState<ShiftState>,
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
}"""

content = re.sub(r'@Composable\s*fun QwertyKeyboard.*?(?=@Composable\s*fun T9Keyboard)', qwerty_clean + "\n\n", content, flags=re.DOTALL)

# Let's also make sure no extra brackets at the end of file
lines = content.split('\n')
while lines and (lines[-1].strip() == '}' or lines[-1].strip() == ''):
    lines.pop()

# The file should end properly with the SymbolKeyboard block
symbol_kb_check = "\n".join(lines)
if symbol_kb_check.count('{') > symbol_kb_check.count('}'):
    symbol_kb_check += "\n}\n"

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write(symbol_kb_check)
