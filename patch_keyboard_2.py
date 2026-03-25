import re

file_path = "LovekeyIME/keyboard-ui/src/main/java/com/lovekey/ime/ui/LovekeyKeyboard.kt"

with open(file_path, 'r') as f:
    content = f.read()

# Let's fix the T9Keyboard parameter replacement which might have been broken by my regex if the function body wasn't correctly matched.
# I'll manually replace the entire T9Keyboard function.

t9_start = content.find("@Composable\nfun T9Keyboard")
t9_end = content.find("@Composable\nfun HandwritingKeyboard")

new_t9_code = """@Composable
fun T9Keyboard(
    textColor: Color,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    secondaryTextColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left Column (5 keys)
        Column(
            modifier = Modifier.weight(1.1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val leftKeys = listOf(",", ".", "?", "!", "符号")
            leftKeys.forEach { key ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { onKeyPress(key) },
                    color = functionKeyColor,
                    shadowElevation = 0.5.dp,
                    shape = RoundedCornerShape(keyCornerRadius)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = key, color = textColor, fontSize = if (key == "符号") 16.sp else 22.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Middle Column (3x4 Grid)
        Column(
            modifier = Modifier.weight(3.3f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val middleRows = listOf(
                listOf(Pair("@#", ""), Pair("ABC", "2"), Pair("DEF", "3")),
                listOf(Pair("GHI", "4"), Pair("JKL", "5"), Pair("MNO", "6")),
                listOf(Pair("PQRS", "7"), Pair("TUV", "8"), Pair("WXYZ", "9")),
                listOf(Pair("123", ""), Pair("0", ""), Pair("中/英", ""))
            )

            middleRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { cell ->
                        val isFunctionKey = cell.first in listOf("@#", "123", "中/英")
                        val isSpace = cell.first == "0"
                        val bgColor = if (isFunctionKey) functionKeyColor else keyColor

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = 4.dp, vertical = 3.dp)
                                .clip(RoundedCornerShape(keyCornerRadius))
                                .clickable {
                                    if (isSpace) {
                                        onKeyPress("SPACE")
                                    } else if (cell.second.isNotEmpty()) {
                                        onKeyPress(cell.second)
                                    } else {
                                        onKeyPress(cell.first)
                                    }
                                },
                            color = bgColor,
                            shadowElevation = 0.5.dp,
                            shape = RoundedCornerShape(keyCornerRadius)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isFunctionKey) {
                                    Text(text = cell.first, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                } else if (isSpace) {
                                    Text(text = "SPACE", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                } else {
                                    Text(text = cell.first, color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                    Text(text = cell.second, color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Light)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Right Column (3 keys, last one is taller)
        Column(
            modifier = Modifier.weight(1.1f).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("DEL") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⌫", color = textColor, fontSize = 20.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("CLEAR") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("重输", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(horizontal = 4.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { onKeyPress("ENT") },
                color = accentColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("发送", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

"""

if t9_start != -1 and t9_end != -1:
    content = content[:t9_start] + new_t9_code + content[t9_end:]
    with open(file_path, 'w') as f:
        f.write(content)
    print("T9Keyboard replaced manually")
else:
    print("Could not find T9Keyboard bounds")
