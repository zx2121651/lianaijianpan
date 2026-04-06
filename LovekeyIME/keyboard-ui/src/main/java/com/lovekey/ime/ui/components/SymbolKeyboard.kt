package com.lovekey.ime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lovekey.ime.ui.KeyboardMode
import com.lovekey.ime.ui.ShiftState

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
@Composable
fun SymbolKeyboard(
    currentMode: MutableState<KeyboardMode>,
    previousMode: KeyboardMode,
    enterKeyText: String,
    textColor: Color,
    onModeChanged: (Boolean) -> Unit,
    keyColor: Color,
    functionKeyColor: Color,
    accentColor: Color,
    keyCornerRadius: androidx.compose.ui.unit.Dp,
    onKeyPress: (String) -> Unit
) {
    val view = LocalView.current
    val categories = listOf("表情", "常用", "数字", "中文", "英文", "数学", "网络", "序号")
    var currentCategory by remember { mutableStateOf(categories[0]) }

    val symbolMap = mapOf(
                "表情" to listOf(
            "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎", "😍", "😘", "😗", "😙", "😚", "☺", "🙂", "🤗", "🤩", "🤔", "🤨", "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪", "😫", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔", "😕", "🙃", "🤑", "😲", "☹", "🙁", "😖", "😞", "😟", "😤", "😢", "😭", "😦", "😧", "😨", "😩", "🤯", "😬", "😰", "😱", "😳", "🤪", "😵", "😡", "😠", "🤬", "😷", "🤒", "🤕", "🤢", "🤮", "🤧", "😇", "🤠", "🤡", "🤥", "🤫", "🤭", "🧐", "🤓", "😈", "👿", "👹", "👺", "💀", "👻", "👽", "👾", "🤖", "💩", "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾", "🙈", "🙉", "🙊", "🐵", "🐒", "🦍", "🐶", "🐕", "🐩", "🐺", "🦊", "🦝", "🐱", "🐈", "🦁", "🐯", "🐅", "🐆", "🐴", "🐎", "🦄", "🦓", "🦌", "🐮", "🐂", "🐃", "🐄", "🐷", "🐖", "🐗", "🐽", "🐏", "🐑", "🐐", "🐪", "🐫", "🦙", "🦒", "🐘", "🦏", "🦛", "🐭", "🐁", "🐀", "🐹", "🐰", "🐇", "🐿", "🦔", "🦇", "熊", "🐨", "熊猫", "🦘", "🦡", "🐾", "🦃", "🐔", "🐓", "🐣", "🐤", "🐥", "🐦", "🐧", "🕊", "🦅", "鸭", "🦢", "🦉", "🦚", "鹦鹉", "🐸", "🐊", "🐢", "🦎", "🐍", "🐲", "🐉", "🦕", "🦖", "🐳", "🐋", "🐬", "鱼", "热带鱼", "🐡", "鲨鱼", "🐙", "🐚", "蟹", "虾", "🦑", "蜗牛", "🦋", "🐛", "蚂蚁", "🐝", "瓢虫", "蟋蟀", "蜘蛛", "🕸", "🦂", "蚊子", "微生物", "💐", "樱花", "💮", "🌹", "🥀", "🌺", "🌻", "🌼", "郁金香", "🌱", "松树", "🌳", "🌴", "🌵", "🌾", "🌿", "🍀", "枫叶", "🍂", "🍃"
        ),
        "常用" to listOf("，", "。", "？", "！", "、", "；", "：", "“", "”", "‘", "’", "（", "）", "【", "】", "《", "》", "…", "—", "～"),
        "数字" to listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "+", "-", "*", "/", "=", "%", ".", ","),
        "中文" to listOf("￥", "·", "ˉ", "ˇ", "¨", "々", "‖", "「", "」", "『", "』", "〔", "〕", "〈", "〉", "〇"),
        "英文" to listOf(",", ".", "?", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "=", "+", "[", "]", "{", "}", "\\", "|", ";", ":", "'", "\"", "<", ">", "/", "~", "`"),
        "数学" to listOf("+", "-", "×", "÷", "=", "≠", "≈", "±", "<", ">", "≤", "≥", "∞", "∝", "∴", "∠", "△", "⊥", "∥", "√", "∫", "∮", "∵", "∷", "∪", "∩", "∈", "⊂", "⊆", "⊇", "π", "°", "℃", "‰"),
        "网络" to listOf("^_^", "T_T", "(╥_╥)", "@_@", "=‿=", "~_~", "-_-||", ">_<", "^o^", "^ω^", "(*^__^*)", "(^o^)/", "Orz", "O(∩_∩)O"),
        "序号" to listOf("①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ", "Ⅴ", "Ⅵ", "Ⅶ", "Ⅷ", "Ⅸ", "Ⅹ")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Left Column for Categories (Tab)
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .background(functionKeyColor)
        ) {
            categories.forEach { category ->
                val isSelected = category == currentCategory
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
currentCategory = category },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) accentColor else textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // "ABC" return key at the bottom of the left column
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE5E5E5)) // slightly darker for function key feeling
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
currentMode.value = previousMode
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABC",
                    color = textColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Right Column for Symbol Grid
        Column(
            modifier = Modifier
                .weight(4.5f)
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
        ) {
            val currentSymbols = symbolMap[currentCategory] ?: emptyList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (currentCategory == "网络") 3 else 5),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(currentSymbols) { symbol ->
                    Surface(
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(keyCornerRadius))
                            .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress(symbol) },
                        color = keyColor,
                        shadowElevation = 0.5.dp,
                        shape = RoundedCornerShape(keyCornerRadius)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = symbol,
                                color = textColor,
                                fontSize = if (currentCategory == "网络") 14.sp else 18.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Bottom Action Row for Symbol Keyboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("DEL") },
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
                        .weight(3f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("SPACE") },
                    color = keyColor,
                    shadowElevation = 0.5.dp,
                    shape = RoundedCornerShape(keyCornerRadius)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Lovekey", color = textColor, fontSize = 15.sp)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .padding(start = 4.dp)
                        .clip(RoundedCornerShape(keyCornerRadius))
                        .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("ENT") },
                    color = accentColor,
                    shadowElevation = 0.5.dp,
                    shape = RoundedCornerShape(keyCornerRadius)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(enterKeyText, color = Color.White, fontSize = if (enterKeyText.length > 2) 13.sp else 15.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
