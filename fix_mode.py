import re

file_path = "LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt"

with open(file_path, 'r') as f:
    content = f.read()

# I removed currentMode from T9Keyboard parameters but left it in the call site! Let me remove it from the call site.
pattern = r'KeyboardMode\.T9 -> T9Keyboard\(\n\s+currentMode = modeState,\n'
replacement = r'KeyboardMode.T9 -> T9Keyboard(\n'
content = re.sub(pattern, replacement, content)

# I should add `currentMode: MutableState<KeyboardMode>` to T9Keyboard and handle "123" and "符号" similarly so it can switch modes!
# Re-add it to T9Keyboard parameters instead!

t9_def_pattern = r'@Composable\nfun T9Keyboard\(\n\s+textColor: Color,'
t9_def_replacement = r'@Composable\nfun T9Keyboard(\n    currentMode: MutableState<KeyboardMode>,\n    textColor: Color,'
content = re.sub(t9_def_pattern, t9_def_replacement, content)

# Also fix the clicking logic in T9Keyboard for "123" and "符号"
t9_click_pattern = r'if \(isSpace\)'
t9_click_replacement = r'if (cell.first == "123" || cell.first == "符号") {\n                                        currentMode.value = KeyboardMode.SYMBOL\n                                    } else if (isSpace)'
content = re.sub(t9_click_pattern, t9_click_replacement, content)

# Let's check Left Column keys click as well
t9_left_click = r'\.clickable \{ onKeyPress\(key\) \}'
t9_left_click_replacement = r'.clickable { if (key == "符号") currentMode.value = KeyboardMode.SYMBOL else onKeyPress(key) }'
content = re.sub(t9_left_click, t9_left_click_replacement, content)


with open(file_path, 'w') as f:
    f.write(content)
print("Fixed mode parameters in T9")
