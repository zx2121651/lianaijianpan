with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'r') as f:
    content = f.read()

content = content.replace(
"""            QwertyKeyboard(
                shiftState = shiftState,
                currentMode = modeState,
                textColor = textColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )""",
"""            QwertyKeyboard(
                shiftState = shiftState,
                currentMode = modeState,
                textColor = textColor,
                keyColor = keyColor,
                functionKeyColor = functionKeyColor,
                accentColor = accentColor,
                keyCornerRadius = keyCornerRadius,
                onKeyPress = onKeyPress
            )
            }""")

with open('LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt', 'w') as f:
    f.write(content)
