import re

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

# Replace the specific Surface block in QwertyKeyboard
old_block = """                Surface(
                    modifier = Modifier
                        .weight(weight)
                        .padding(horizontal = 4.dp)
                        .height(46.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
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

new_block = """                val showPopup = !isFunctionKey && !isSpaceKey && !isActionKey

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
                )"""

content = content.replace(old_block, new_block)

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
