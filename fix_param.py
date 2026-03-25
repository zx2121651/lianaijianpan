import re

file_path = "LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt"

with open(file_path, 'r') as f:
    content = f.read()

# I removed it entirely from T9Keyboard call but now I need to put it back exactly correctly.
pattern = r'KeyboardMode\.T9 -> T9Keyboard\(\n\s+textColor'
replacement = r'KeyboardMode.T9 -> T9Keyboard(\n                currentMode = modeState,\n                textColor'
content = re.sub(pattern, replacement, content)

with open(file_path, 'w') as f:
    f.write(content)
print("Param fixed")
