import re

file_path = "LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt"

with open(file_path, 'r') as f:
    content = f.read()

# Make sure "符号" sets the mode to SYMBOL
# In QWERTYKeyboard, find the clickable logic
pattern = r'if \(key == "123" \|\| key == "\."\)'
new_pattern = r'if (key == "123" || key == "符号")'
content = re.sub(pattern, new_pattern, content)

with open(file_path, 'w') as f:
    f.write(content)
print("QWERTY Clickable logic updated")
