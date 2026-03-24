import re

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

# Replace QWERTY 123 click
content = content.replace(
    'if (key != "SHIFT" && key != "123") onKeyPress(key)',
    'if (key == "123") { currentMode = KeyboardMode.SYMBOL } else if (key != "SHIFT") { onKeyPress(key) }'
)
# Add currentMode param to QwertyKeyboard and T9Keyboard
content = content.replace(
    'fun QwertyKeyboard(',
    'fun QwertyKeyboard(currentMode: androidx.compose.runtime.MutableState<KeyboardMode>, '
).replace(
    'QwertyKeyboard(\n                textColor',
    'QwertyKeyboard(currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },\n                textColor'
)

# Update inside QwertyKeyboard
content = content.replace(
    'if (key == "123") { currentMode = KeyboardMode.SYMBOL } else if (key != "SHIFT") { onKeyPress(key) }',
    'if (key == "123") { currentMode.value = KeyboardMode.SYMBOL } else if (key != "SHIFT") { onKeyPress(key) }'
)

# Replace T9Keyboard signature
content = content.replace(
    'fun T9Keyboard(',
    'fun T9Keyboard(currentMode: androidx.compose.runtime.MutableState<KeyboardMode>, '
).replace(
    'T9Keyboard(\n                textColor',
    'T9Keyboard(currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },\n                textColor'
)

# Update inside T9Keyboard for "123" / "符" or "."
content = content.replace(
    '.clickable { },',
    '.clickable { if (cell.first == "123" || cell.first == ".") { currentMode.value = KeyboardMode.SYMBOL } else if (cell.first == "0") { onKeyPress("SPACE") } else if (!isFunctionKey) { onKeyPress(cell.first) } },'
)

# Add Symbol mode switch branch
content = content.replace(
    'KeyboardMode.HANDWRITING -> HandwritingKeyboard(',
    'KeyboardMode.HANDWRITING -> HandwritingKeyboard(\n                textColor = textColor,\n                functionKeyColor = functionKeyColor,\n                accentColor = accentColor,\n                keyCornerRadius = keyCornerRadius,\n                onKeyPress = onKeyPress\n            )\n            KeyboardMode.SYMBOL -> SymbolKeyboard(\n                currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },'
)


with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
