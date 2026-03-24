import re

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

# Fix mutable state passings
content = content.replace(
    'QwertyKeyboard(currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },',
    'QwertyKeyboard(currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },'
)

# Add SymbolKeyboard function
symbol_code = """
@Composable
fun SymbolKeyboard(
    currentMode: androidx.compose.runtime.MutableState<KeyboardMode>,
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
}
"""
if "fun SymbolKeyboard" not in content:
    content += symbol_code

# Fix passing state up
content = content.replace(
    'KeyboardMode.QWERTY -> QwertyKeyboard(\n                currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },',
    '''
    // Provide a state that updates the parent enum
    val modeState = remember { mutableStateOf(currentMode) }
    LaunchedEffect(modeState.value) { currentMode = modeState.value }

    when (currentMode) {
        KeyboardMode.QWERTY -> QwertyKeyboard(
            currentMode = modeState,
    '''
)
content = content.replace(
    'KeyboardMode.T9 -> T9Keyboard(\n                currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },',
    'KeyboardMode.T9 -> T9Keyboard(\n                currentMode = modeState,'
)

content = content.replace(
    'KeyboardMode.SYMBOL -> SymbolKeyboard(\n                currentMode = remember { mutableStateOf(currentMode) }.apply { value = currentMode },',
    'KeyboardMode.SYMBOL -> SymbolKeyboard(\n                currentMode = modeState,\n                keyColor = keyColor,'
)

with open('LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
