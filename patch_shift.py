import re

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

# Add ShiftState enum
shift_enum = """
enum class ShiftState {
    LOWERCASE, UPPERCASE, CAPSLOCK
}
"""
if "enum class ShiftState" not in content:
    content = content.replace('enum class KeyboardMode {', shift_enum + '\nval dummy = 0\nenum class KeyboardMode {')
    content = content.replace('\nval dummy = 0\n', '\n')


# Add ShiftState to QwertyKeyboard
content = content.replace(
    'fun QwertyKeyboard(\n    currentMode:',
    'fun QwertyKeyboard(\n    shiftState: MutableState<ShiftState>,\n    currentMode:'
)

content = content.replace(
    'QwertyKeyboard(\n                currentMode = modeState,',
    'val shiftState = remember { mutableStateOf(ShiftState.LOWERCASE) }\n            QwertyKeyboard(\n                shiftState = shiftState,\n                currentMode = modeState,'
)

# Update SHIFT logic
replacement_shift = """
                        .clickable {
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
                        },
"""
content = re.sub(r'\.clickable\s*\{\s*if\s*\(key\s*==\s*"123"\)\s*\{[^}]+\}\s*else\s*if\s*\(key\s*!=\s*"SHIFT"\)\s*\{\s*onKeyPress\(key\)\s*\}\s*\},', replacement_shift, content, count=1)


# Update display text for Shift
content = content.replace(
    '"SHIFT" -> "⇧"',
    '"SHIFT" -> if (shiftState.value == ShiftState.LOWERCASE) "⇧" else "⬆"'
)

# Update display text for characters
content = content.replace(
    'else -> key\n                }',
    'else -> if (shiftState.value != ShiftState.LOWERCASE && key.length == 1) key.uppercase() else key\n                }'
)

# Update bgColor for Shift
content = content.replace(
    'isFunctionKey -> functionKeyColor',
    'isFunctionKey -> if (key == "SHIFT" && shiftState.value != ShiftState.LOWERCASE) Color(0xFFD6D1D1) else functionKeyColor'
)

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
