package com.lovekey.ime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun KeyboardKey(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color,
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    keyCornerRadius: Dp,
    showPopup: Boolean = true,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        // Weight must be applied inside RowScope, so we pass Modifier instead
modifier = modifier

            .padding(horizontal = 4.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(keyCornerRadius))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        color = bgColor,
        shadowElevation = 0.5.dp,
        shape = RoundedCornerShape(keyCornerRadius)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }

        if (isPressed && showPopup && text.length == 1) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -120)
            ) {
                Box(
                    modifier = modifier
                        .size(54.dp, 64.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = Color(0xFF4A4443),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}
