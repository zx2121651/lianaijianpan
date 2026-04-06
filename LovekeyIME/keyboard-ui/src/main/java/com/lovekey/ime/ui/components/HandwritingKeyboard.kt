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

import androidx.compose.ui.text.style.TextAlign
@Composable
fun HandwritingKeyboard(
    enterKeyText: String,
    textColor: Color, functionKeyColor: Color, accentColor: Color, keyCornerRadius: androidx.compose.ui.unit.Dp, onKeyPress: (String) -> Unit
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(keyCornerRadius)),
            color = Color.White,
            shadowElevation = 0.5.dp,
            shape = RoundedCornerShape(keyCornerRadius)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "请在此区域手写 (敬请期待)",
                    color = Color(0xFFD6D1D1),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("DEL") },
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⌫", color = textColor, fontSize = 18.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
},
                color = functionKeyColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("重写", color = textColor, fontSize = 14.sp)
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(keyCornerRadius))
                    .clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
onKeyPress("ENT") },
                color = accentColor,
                shadowElevation = 0.5.dp,
                shape = RoundedCornerShape(keyCornerRadius)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(enterKeyText, color = Color.White, fontSize = if (enterKeyText.length > 2) 13.sp else 16.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
