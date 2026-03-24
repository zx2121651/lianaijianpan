import re

# Fix KeyPopup.kt modifier issue
with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/KeyPopup.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'modifier = Modifier\n            .weight(weight, fill = true)',
    '// Weight must be applied inside RowScope, so we pass Modifier instead\nmodifier = Modifier\n'
)
# Modify signature to take a pre-configured modifier
content = content.replace(
    'fun KeyboardKey(\n    text: String,\n    weight: Float,',
    'fun KeyboardKey(\n    text: String,\n    modifier: Modifier = Modifier,'
)
content = content.replace(
    'modifier = Modifier\n',
    'modifier = modifier\n'
)

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/KeyPopup.kt', 'w') as f:
    f.write(content)

# Fix LovekeyKeyboard.kt
with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

old_broken = """                val showPopup = !isFunctionKey && !isSpaceKey && !isActionKey

                KeyboardKey(
                    text = displayText,
                    weight = weight,
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
                }"""

new_fixed = """                val showPopup = !isFunctionKey && !isSpaceKey && !isActionKey

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
                )"""

content = content.replace(old_broken, new_fixed)

# Now fix the unresolvable shiftState in SymbolKeyboard
symbol_old = """                        if (key == "ABC") {
                            currentMode.value = KeyboardMode.QWERTY
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
                        }"""
symbol_new = """                        if (key == "ABC") {
                            currentMode.value = KeyboardMode.QWERTY
                        } else {
                            onKeyPress(key)
                        }"""
# Wait, let's just search and replace blindly for shiftState in SymbolKeyboard area
# A simpler way is just to write a regex that matches `if (key == "ABC") { ... } else { ... shiftState ... }` and replaces it.
content = re.sub(r'if \(key == "ABC"\) \{\s*currentMode\.value = KeyboardMode\.QWERTY\s*\} else \{\s*val keyToSend = if[^}]+\}[^}]+\}[^}]+\}[^\n]+', symbol_new, content, count=1, flags=re.DOTALL)


# One more thing: I'll just rewrite the SymbolKeyboard part to be safe
content = re.sub(r'fun SymbolKeyboard\(.*?\}\s*\}\s*\}\s*\}', """fun SymbolKeyboard(
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
}""", content, flags=re.DOTALL)

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
