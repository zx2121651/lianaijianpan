import re

file_path = "LovekeyIME/app/src/main/java/com/lovekey/ime/LovekeyIMEService.kt"

with open(file_path, 'r') as f:
    content = f.read()

# Add handling for "CLEAR" key in onKeyPress block
clear_block = r'''                            "DEL" -> {'''
new_clear_block = r'''                            "CLEAR" -> {
                                rawInput = ""
                                displayPinyinText = ""
                                candidateList = emptyList()
                                t9PinyinCombinations = emptyList()
                                if (isBound) PinyinDecoderService.nativeImResetSearch()
                            }
                            "DEL" -> {'''
content = content.replace(clear_block, new_clear_block)

# Handle the new bottom row QWERTY keys like "符号", "中/英"
# If they are just passed to onKeyPress, we should catch them and do nothing or handle appropriately.
# But wait, QWERTY mode handles "符号" by changing state internally in LovekeyKeyboard, we should ensure it doesn't leak into `rawInput`!
# Ah, I added `currentMode.value = KeyboardMode.SYMBOL` inside QWERTYKeyboard's clickable for `符号`! So it won't even call onKeyPress.
# Let me double check test_qwerty.py and LovekeyKeyboard.kt

# What about "中/英"? It calls onKeyPress("中/英"). We should handle it.
zh_en_block = r'''                            else -> {
                                // Detect if it's a digit from T9'''
new_zh_en_block = r'''                            "中/英" -> {
                                // Toggle Chinese/English mode (Placeholder for future implementation)
                                // Currently we just clear or ignore.
                            }
                            else -> {
                                // Detect if it's a digit from T9'''
content = content.replace(zh_en_block, new_zh_en_block)


with open(file_path, 'w') as f:
    f.write(content)

print("LovekeyIMEService.kt updated for CLEAR and 中/英 keys")
