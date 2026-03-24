import re

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

# Fix the broken replacement from previous command
content = content.replace(
"""        when (currentMode) {
            KeyboardMode.QWERTY -> val shiftState = remember { mutableStateOf(ShiftState.LOWERCASE) }
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
            KeyboardMode.T9 -> T9Keyboard(""",
"""        when (currentMode) {
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
            KeyboardMode.T9 -> T9Keyboard("""
)

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
